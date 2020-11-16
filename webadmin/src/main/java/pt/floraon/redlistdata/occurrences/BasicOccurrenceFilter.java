package pt.floraon.redlistdata.occurrences;

import pt.floraon.driver.Constants;
import pt.floraon.driver.interfaces.IFloraOn;
import pt.floraon.driver.interfaces.OccurrenceFilter;
import pt.floraon.geometry.IPolygonTheme;
import pt.floraon.geometry.Point2D;
import pt.floraon.geometry.Polygon;
import pt.floraon.geometry.PolygonTheme;
import pt.floraon.occurrences.OccurrenceConstants;
import pt.floraon.occurrences.entities.Inventory;
import pt.floraon.occurrences.entities.Occurrence;
import pt.floraon.redlistdata.entities.RedListSettings;

import java.util.Date;
import java.util.Map;

/**
 * A filter to filter out the occurrences without coordinates and optionally with other predicates.
 */
public class BasicOccurrenceFilter implements OccurrenceFilter {
    /**
     * The minimum and maximum years considered for including occurrences
     */
    private Integer minimumYear, maximumYear;
    /**
     * Do not consider any occurrence inserted after this date
     */
    private Date cutRecordsAfter = null;
    private boolean includeDoubtful = true;
    private Integer minimumPrecision;
    private boolean includeUnmatchedTaxa = true;
    private boolean includeHigherRanks = true;
    /**
     * A polygon theme to clip occurrences. May have any number of polygons.
     */
    private IPolygonTheme clippingPolygon;

    public BasicOccurrenceFilter(IPolygonTheme clippingPolygon) {
        this(null, null, true, clippingPolygon);
    }

    /**
     *
     * @param minimumYear The minimum year to be considered for inclusion. Note that records without a year are treated specially.
     * @param maximumYear
     * @param includeDoubtful
     * @param clippingPolygon A set of polygons, all the occurrences lying outside are excluded.
     */
    public BasicOccurrenceFilter(Integer minimumYear, Integer maximumYear, boolean includeDoubtful, IPolygonTheme clippingPolygon) {
        this.minimumYear = minimumYear;
        this.maximumYear = maximumYear;
        this.includeDoubtful = includeDoubtful;
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

    public BasicOccurrenceFilter withInsidePolygonTheme(IPolygonTheme polygonTheme) {
        this.clippingPolygon = polygonTheme;
        return this;
    }

    /**
     * Only include ConfidenceInIdentifiction.CERTAIN
     * @return
     */
    public BasicOccurrenceFilter withNotDoubtful() {
        this.includeDoubtful = false;
        return this;
    }

    public BasicOccurrenceFilter withDoubtful() {
        this.includeDoubtful = true;
        return this;
    }

    /**
     * Include only the records used to make the published current maps.
     * @param driver
     * @param territory
     * @return
     */
    public static BasicOccurrenceFilter OnlyCurrentAndCertainRecords(IFloraOn driver, String territory) {
        RedListSettings rls = driver.getRedListSettings(territory);
        return OnlyCurrentAndCertainRecordsInPolygon(driver, territory, rls.getClippingPolygon());
     }

    /**
     * Include only the records used to make the published current maps, contained in the given polygon
     * @param driver
     * @param territory
     * @param polygonWKT
     * @return
     */
    public static BasicOccurrenceFilter OnlyCurrentAndCertainRecordsInPolygon(IFloraOn driver, String territory, String polygonWKT) {
        return OnlyCurrentAndCertainRecordsInPolygon(driver, territory, new PolygonTheme(polygonWKT));
    }

    /**
     * Include only the records used to make the published current maps, contained in the given polygon
     * @param driver
     * @param territory
     * @param polygon
     * @return
     */
    public static BasicOccurrenceFilter OnlyCurrentAndCertainRecordsInPolygon(IFloraOn driver, String territory, PolygonTheme polygon) {
        RedListSettings rls = driver.getRedListSettings(territory);
        BasicOccurrenceFilter out = new BasicOccurrenceFilter(rls.getHistoricalThreshold() + 1
                , null, false, polygon);
        out.cutRecordsAfter = rls.getCutRecordsInsertedAfter();
        return out;
    }

    public static BasicOccurrenceFilter OnlyCertainRecords(IFloraOn driver, String territory) {
        RedListSettings rls = driver.getRedListSettings(territory);
        return OnlyCertainRecordsInPolygon(driver, territory, rls.getClippingPolygon());
    }

    public static BasicOccurrenceFilter OnlyCertainRecordsInPolygon(IFloraOn driver, String territory, PolygonTheme polygon) {
        RedListSettings rls = driver.getRedListSettings(territory);
        BasicOccurrenceFilter out = new BasicOccurrenceFilter(null, null, false, polygon);
        out.cutRecordsAfter = rls.getCutRecordsInsertedAfter();
        return out;
    }

    public static BasicOccurrenceFilter OnlyHistoricalAndCertainRecordsInPolygon(IFloraOn driver, String territory, PolygonTheme polygon) {
        RedListSettings rls = driver.getRedListSettings(territory);
        BasicOccurrenceFilter out = new BasicOccurrenceFilter(null, rls.getHistoricalThreshold()
                , false, polygon);
        out.cutRecordsAfter = rls.getCutRecordsInsertedAfter();
        return out;
    }

    /**
     * Include only the records used to make the published historical maps
     * @param driver
     * @param territory
     * @return
     */
    public static BasicOccurrenceFilter OnlyHistoricalAndCertainRecords(IFloraOn driver, String territory) {
        RedListSettings rls = driver.getRedListSettings(territory);
        return BasicOccurrenceFilter.OnlyHistoricalAndCertainRecordsInPolygon(driver, territory, rls.getClippingPolygon());
    }

    @Override
    public boolean enter(Inventory inv) {
        Occurrence so = (Occurrence) inv;
//        System.out.println(so.getDataSource()+": "+ so.getOccurrence().getVerbTaxon()+", "+ so.getLatitude()+", "+so.getVerbLocality()+", "+so._getDate()+", "+so.getOccurrence().getPrivateComment());
        boolean wasDestroyed;
//        Gson gs = new GsonBuilder().setPrettyPrinting().create();
//System.out.println("Enter? "+ so._getLatitude()+", "+so._getLongitude()+" Y:"+so.getYear());
        if(Constants.isNullOrNoData(so._getLatitude()) || Constants.isNullOrNoData(so._getLongitude())) return false;

        boolean enter;
//        if(minimumYear == null && maximumYear == null && clippingPolygon == null) return true;
        // if it was destroyed, then this will go forced into historical record
        wasDestroyed = so.getOccurrence().getPresenceStatus() != null && so.getOccurrence().getPresenceStatus() == OccurrenceConstants.PresenceStatus.DESTROYED;

        enter = !(wasDestroyed && !includeDoubtful && minimumYear != null && maximumYear == null);
        // format: enter &= !(<excluding condition>);

        if(cutRecordsAfter != null && so.getOccurrence().getDateInserted() != null)
            enter &= !(so.getOccurrence().getDateInserted().after(cutRecordsAfter));

        if(this.minimumPrecision != null)
            enter &= !(so.getPrecision() != null && so.getPrecision()._isPrecisionWorseThan(this.minimumPrecision));

        enter &= !(!this.includeUnmatchedTaxa && so.getOccurrence().getTaxEnt() == null);
        enter &= !(!this.includeHigherRanks && so.getOccurrence().getTaxEnt() != null && !so.getOccurrence().getTaxEnt().isSpeciesOrInferior());

        enter &= !(minimumYear != null && so.getYear() != null && so.getYear() != 0 && so.getYear() < minimumYear);
        enter &= !(maximumYear != null && so.getYear() != null && so.getYear() != 0 && so.getYear() > maximumYear && !wasDestroyed);
        if(!includeDoubtful) {
            enter &= !(so.getOccurrence().getConfidence() == OccurrenceConstants.ConfidenceInIdentifiction.ALMOST_SURE
                    || so.getOccurrence().getConfidence() == OccurrenceConstants.ConfidenceInIdentifiction.DOUBTFUL);
            enter &= !(so.getOccurrence().getPresenceStatus() != null && so.getOccurrence().getPresenceStatus() != OccurrenceConstants.PresenceStatus.ASSUMED_PRESENT && !wasDestroyed);
//            enter &= !(so.getOccurrence().getNaturalization() != null && so.getOccurrence().getNaturalization() != OccurrenceConstants.OccurrenceNaturalization.WILD);
            enter &= !(so.getOccurrence().getAbundance() != null && !so.getOccurrence().getAbundance().wasDetected());
            enter &= !(so.getPrecision() != null && so.getPrecision()._isPrecisionWorseThan(100) && so._isDateEmpty());
        }
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
