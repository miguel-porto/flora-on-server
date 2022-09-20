package pt.floraon.redlistdata.occurrences;

import pt.floraon.driver.Constants;
import pt.floraon.driver.interfaces.IFloraOn;
import pt.floraon.driver.interfaces.OccurrenceFilter;
import pt.floraon.driver.utils.StringUtils;
import pt.floraon.geometry.IPolygonTheme;
import pt.floraon.geometry.Point2D;
import pt.floraon.geometry.Polygon;
import pt.floraon.geometry.PolygonTheme;
import pt.floraon.occurrences.OccurrenceConstants;
import pt.floraon.occurrences.entities.Inventory;
import pt.floraon.occurrences.entities.Occurrence;
import pt.floraon.redlistdata.entities.RedListSettings;

import java.util.*;

/**
 * A filter to filter out the occurrences without coordinates and optionally with other predicates.
 */
public class BasicOccurrenceFilter implements OccurrenceFilter {
    /**
     * The minimum and maximum years considered for including occurrences
     * Note that records without a year are treated specially.
     */
    private Integer minimumYear = null, maximumYear = null;
    /**
     * Do not consider any occurrence inserted after this date
     */
    private Date cutRecordsAfter = null;
//    private boolean includeDoubtful = true;
    private boolean ensureCurrentlyExisting = false;
    private Integer minimumPrecision;
    private boolean includeUnmatchedTaxa = true;
    private boolean includeHigherRanks = true;
    private boolean includeExcludedRecords = true;
    private boolean onlyWild = false;
    private final List<OccurrenceConstants.ConfidenceInIdentifiction> allowedConfidence = new ArrayList<>(
            Arrays.asList(OccurrenceConstants.ConfidenceInIdentifiction.values()));
    /**
     * A polygon theme to clip occurrences. May have any number of polygons.
     */
    private IPolygonTheme clippingPolygon;

    /**
     * Create a filter that only filters out those outside polygon.
     * @param clippingPolygon A set of polygons, all the occurrences lying outside are excluded. Pass NULL to not exclude anything.
     */
    public BasicOccurrenceFilter(IPolygonTheme clippingPolygon) {
        this.clippingPolygon = clippingPolygon;
    }

    /**
     * Creates a new filter that only filters out records without coordinates.
     * @return
     */
    public static BasicOccurrenceFilter create() {
        return new BasicOccurrenceFilter(null);
    }

    public BasicOccurrenceFilter withMinimumPrecision(Integer minimumPrecision) {
        this.minimumPrecision = minimumPrecision;
        return this;
    }

    /**
     * Exclude records inserted after the given date
     * @param cutRecordsAfter
     * @return
     */
    public BasicOccurrenceFilter cutRecordsAfter(Date cutRecordsAfter) {
        this.cutRecordsAfter = cutRecordsAfter;
        return this;
    }

    public BasicOccurrenceFilter withValidTaxaOnly() {
        this.includeUnmatchedTaxa = false;
        return this;
    }

    public BasicOccurrenceFilter withSpeciesOrLowerRankOnly() {
        this.includeHigherRanks = false;
        return this;
    }

    public BasicOccurrenceFilter withMaximumYear(Integer maxYear) {
        this.maximumYear = maxYear;
        return this;
    }

    public BasicOccurrenceFilter withMinimumYear(Integer minYear) {
        this.minimumYear = minYear;
        return this;
    }

    public BasicOccurrenceFilter withInsidePolygonTheme(IPolygonTheme polygonTheme) {
        this.clippingPolygon = polygonTheme;
        return this;
    }

    /**
     * Only include ConfidenceInIdentifiction.CERTAIN
     * @return
     */
    public BasicOccurrenceFilter withOnlyCertain() {
//        this.includeDoubtful = false;
        this.allowedConfidence.clear();
        this.allowedConfidence.add(OccurrenceConstants.ConfidenceInIdentifiction.CERTAIN);
        this.allowedConfidence.add(OccurrenceConstants.ConfidenceInIdentifiction.NULL); // we assume NULL is certain
        return this;
    }

    /**
     * Include all confidence levels
     * @return
     */
    public BasicOccurrenceFilter withDoubtful() {
//        this.includeDoubtful = true;
        this.allowedConfidence.clear();
        this.allowedConfidence.addAll(Arrays.asList(OccurrenceConstants.ConfidenceInIdentifiction.values()));
        return this;
    }

    public BasicOccurrenceFilter withOnlyAllowConfidenceLevels(OccurrenceConstants.ConfidenceInIdentifiction[] confidence) {
        this.allowedConfidence.clear();
        this.allowedConfidence.addAll(Arrays.asList(confidence));
        this.allowedConfidence.add(OccurrenceConstants.ConfidenceInIdentifiction.NULL); // we assume NULL is certain
        return this;
    }

    /**
     * Ensure that it was not destroyed, it was detected, and it has reasonable precision
     * @return
     */
    public BasicOccurrenceFilter withEnsureCurrentlyExisting() {
        this.ensureCurrentlyExisting = true;
        return this;
    }

    /**
     * Include destroyed, imprecise and absence records.
     * @return
     */
    public BasicOccurrenceFilter withoutEnsureCurrentlyExisting() {
        this.ensureCurrentlyExisting = false;
        return this;
    }

    /**
     * Only include those that were not marked for exclusion (field excludeReason)
     * @return
     */
    public BasicOccurrenceFilter withoutExcluded() {
        this.includeExcludedRecords = false;
        return this;
    }

    /**
     * Include those marked for exclusion
     * @return
     */
    public BasicOccurrenceFilter withExcluded() {
        this.includeExcludedRecords = true;
        return this;
    }

    /**
     * Include records of any date
     * @return
     */
    public BasicOccurrenceFilter withoutDateFilter() {
        this.minimumYear = null;
        this.maximumYear = null;
        return this;
    }

    /**
     * Include only the records used to make the published current maps and using the user set clipping polygon
     * @param driver
     * @param territory
     * @return
     */
    public static BasicOccurrenceFilter RedListCurrentMapFilter(IFloraOn driver, String territory) {
        RedListSettings rls = driver.getRedListSettings(territory);
        return RedListCurrentMapFilter(driver, territory, rls.getClippingPolygon());
     }

    /**
     * Include only the records used to make the published current maps, contained in the given polygon
     * @param driver
     * @param territory
     * @param polygonWKT
     * @return
     */
    public static BasicOccurrenceFilter RedListCurrentMapFilter(IFloraOn driver, String territory, String polygonWKT) {
        return RedListCurrentMapFilter(driver, territory, StringUtils.isStringEmpty(polygonWKT) ? null : new PolygonTheme(polygonWKT));
    }

    /**
     * Include only the records used to make the published current maps, contained in the given polygon
     * @param driver
     * @param territory
     * @param polygon
     * @return
     */
    public static BasicOccurrenceFilter RedListCurrentMapFilter(IFloraOn driver, String territory, PolygonTheme polygon) {
        RedListSettings rls = driver.getRedListSettings(territory);
        BasicOccurrenceFilter out = new BasicOccurrenceFilter(polygon);
        out.minimumYear = rls.getHistoricalThreshold() + 1;
//        out.includeDoubtful = false;
        out.withOnlyCertain();
        out.includeExcludedRecords = false;
        out.ensureCurrentlyExisting = true;
        out.cutRecordsAfter = rls.getCutRecordsInsertedAfter();
        return out;
    }

    public static BasicOccurrenceFilter RedListHistoricalMapFilter(IFloraOn driver, String territory, PolygonTheme polygon) {
        RedListSettings rls = driver.getRedListSettings(territory);
        BasicOccurrenceFilter out = new BasicOccurrenceFilter(polygon);
        out.maximumYear = rls.getHistoricalThreshold();
//        out.includeDoubtful = false;
        out.withOnlyCertain();
        out.includeExcludedRecords = false;
        out.ensureCurrentlyExisting = true;
        out.cutRecordsAfter = rls.getCutRecordsInsertedAfter();
        return out;
    }

    /**
     * Include only the records used to make the published historical maps
     * @param driver
     * @param territory
     * @return
     */
    public static BasicOccurrenceFilter RedListHistoricalMapFilter(IFloraOn driver, String territory) {
        RedListSettings rls = driver.getRedListSettings(territory);
        return BasicOccurrenceFilter.RedListHistoricalMapFilter(driver, territory, rls.getClippingPolygon());
    }

    @Override
    public boolean enter(Inventory inv) {
        Occurrence so = (Occurrence) inv;
//        System.out.println(so.getDataSource()+": "+ so.getOccurrence().getVerbTaxon()+", "+ so.getLatitude()+", "+so.getVerbLocality()+", "+so._getDate()+", "+so.getOccurrence().getPrivateComment());
        boolean wasDestroyed;
//        Gson gs = new GsonBuilder().setPrettyPrinting().create();
//System.out.println("Enter? "+ so._getLatitude()+", "+so._getLongitude()+" Y:"+so.getYear());
        if(Constants.isNullOrNoData(so._getLatitude()) || Constants.isNullOrNoData(so._getLongitude())) return false;

        boolean enter = true;
        wasDestroyed = so.getOccurrence().getPresenceStatus() != null && so.getOccurrence().getPresenceStatus() == OccurrenceConstants.PresenceStatus.DESTROYED;

        if(ensureCurrentlyExisting) {
            // if it was destroyed, then this will not appear in current records
            enter &= !(wasDestroyed && minimumYear != null && maximumYear == null);
            // ensure it is a presence record (not an absence record)
            enter &= !(so.getOccurrence().getAbundance() != null && !so.getOccurrence().getAbundance().wasDetected());
            // if precision is bad and date is empty, it's too uncertain, so exclude
            enter &= !(so.getPrecision() != null && so.getPrecision()._isPrecisionWorseThan(100) && so._isDateEmpty());
        }

        // format: enter &= !(<excluding condition>);
        if(cutRecordsAfter != null && so.getOccurrence().getDateInserted() != null)
            enter &= !(so.getOccurrence().getDateInserted().after(cutRecordsAfter));

        if(this.minimumPrecision != null)
            enter &= !(so.getPrecision() != null && so.getPrecision()._isPrecisionWorseThan(this.minimumPrecision));

        enter &= !(!this.includeUnmatchedTaxa && so.getOccurrence().getTaxEnt() == null);
        enter &= !(!this.includeHigherRanks && so.getOccurrence().getTaxEnt() != null && !so.getOccurrence().getTaxEnt().isSpeciesOrInferior());

        enter &= !(minimumYear != null && so.getYear() != null && so.getYear() != 0 && so.getYear() < minimumYear);
        enter &= !(maximumYear != null && so.getYear() != null && so.getYear() != 0 && so.getYear() > maximumYear && !wasDestroyed);

        if(!includeExcludedRecords)
            enter &= !(so.getOccurrence().getPresenceStatus() != null &&
                    so.getOccurrence().getPresenceStatus() != OccurrenceConstants.PresenceStatus.ASSUMED_PRESENT &&
                    so.getOccurrence().getPresenceStatus() != OccurrenceConstants.PresenceStatus.NULL &&
                    !wasDestroyed);

        // only include allowed confidence levels
        enter &= !(so.getOccurrence().getConfidence() != null && !allowedConfidence.contains(so.getOccurrence().getConfidence()));
/*
        if(!includeDoubtful) {
            enter &= !(so.getOccurrence().getConfidence() == OccurrenceConstants.ConfidenceInIdentifiction.ALMOST_SURE
                    || so.getOccurrence().getConfidence() == OccurrenceConstants.ConfidenceInIdentifiction.DOUBTFUL);
        }
*/
        enter &= !(onlyWild && so.getOccurrence().getNaturalization() != null &&
                so.getOccurrence().getNaturalization() != OccurrenceConstants.OccurrenceNaturalization.WILD &&
                so.getOccurrence().getNaturalization() != OccurrenceConstants.OccurrenceNaturalization.NULL);

        // Records that do not have a year are excluded from historical datasets except if marked as destroyed.
        // They're only included in the current dataset.
        enter &= !(maximumYear != null && (so.getYear() == null || so.getYear() == 0) && !wasDestroyed);

        if(enter && clippingPolygon != null) {
            boolean tmp2 = false;
            for(Map.Entry<String, Polygon> po : clippingPolygon) {
                if(po.getValue().contains(new Point2D(so._getLongitude(), so._getLatitude()))) {
                    tmp2 = true;
                    break;
                }
            }
            enter = tmp2;
        }

        return enter;

    }
}
