package pt.floraon.redlistdata.occurrences;

import pt.floraon.driver.interfaces.OccurrenceFilter;
import pt.floraon.geometry.IPolygonTheme;
import pt.floraon.geometry.Point2D;
import pt.floraon.geometry.Polygon;
import pt.floraon.occurrences.OccurrenceConstants;
import pt.floraon.occurrences.entities.Inventory;
import pt.floraon.occurrences.entities.Occurrence;

import java.util.Map;

/**
 * A filter to filter out the occurrences without coordinates and optionally with other predicates.
 */
public class BasicOccurrenceFilter implements OccurrenceFilter {
    /**
     * The minimum and maximum years considered for including occurrences
     */
    private Integer minimumYear, maximumYear;
    private boolean includeDoubtful = true;
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

    public static BasicOccurrenceFilter WithCoordinatesFilter() {
        return new BasicOccurrenceFilter(null, null, true, null);
    }

    @Override
    public boolean enter(Inventory inv) {
        Occurrence so = (Occurrence) inv;
//        System.out.println(so.getDataSource()+": "+ so.getOccurrence().getVerbTaxon()+", "+ so.getLatitude()+", "+so.getVerbLocality()+", "+so._getDate()+", "+so.getOccurrence().getPrivateComment());
        boolean wasDestroyed;
//        Gson gs = new GsonBuilder().setPrettyPrinting().create();
//System.out.println("Enter? "+ so._getLatitude()+", "+so._getLongitude()+" Y:"+so.getYear());
        if(so._getLatitude() == null || so._getLongitude() == null) return false;

        boolean enter;
//        if(minimumYear == null && maximumYear == null && clippingPolygon == null) return true;
        // if it was destroyed, then this will go forced into historical record
        wasDestroyed = so.getOccurrence().getPresenceStatus() != null && so.getOccurrence().getPresenceStatus() == OccurrenceConstants.PresenceStatus.DESTROYED;

        enter = !(wasDestroyed && !includeDoubtful && minimumYear != null && maximumYear == null);
        // format: enter &= !(<excluding condition>);
        enter &= !(minimumYear != null && so.getYear() != null && so.getYear() != 0 && so.getYear() < minimumYear);
        enter &= !(maximumYear != null && so.getYear() != null && so.getYear() != 0 && so.getYear() > maximumYear && !wasDestroyed);
        if(!includeDoubtful) {
            enter &= !(so.getOccurrence().getConfidence() == OccurrenceConstants.ConfidenceInIdentifiction.ALMOST_SURE
                    || so.getOccurrence().getConfidence() == OccurrenceConstants.ConfidenceInIdentifiction.DOUBTFUL);
            enter &= !(so.getOccurrence().getPresenceStatus() != null && so.getOccurrence().getPresenceStatus() != OccurrenceConstants.PresenceStatus.ASSUMED_PRESENT && !wasDestroyed);
            enter &= !(so.getOccurrence().getNaturalization() != null && so.getOccurrence().getNaturalization() != OccurrenceConstants.OccurrenceNaturalization.WILD);
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
