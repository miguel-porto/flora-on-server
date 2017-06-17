package pt.floraon.occurrences.entities;

import com.arangodb.velocypack.annotations.Expose;
import com.google.gson.JsonObject;
import org.jfree.util.Log;
import pt.floraon.driver.Constants;
import pt.floraon.driver.DiffableBean;
import pt.floraon.driver.FloraOnException;
import pt.floraon.driver.GeoBean;
import pt.floraon.driver.entities.GeneralDBNode;
import pt.floraon.driver.utils.StringUtils;
import pt.floraon.geometry.*;

import java.io.Serializable;
import java.util.*;

/**
 * Represents the data associated with an inventory, including unmatched taxa. Those that are matched will be converted
 * to graph links and removed from this entity.
 * Created by miguel on 05-02-2017.
 */
public class Inventory extends GeneralDBNode implements Serializable, DiffableBean, GeoBean {
    private Float latitude, longitude;
    private String spatialRS;
    private Float elevation;
    private String geometry;
    private Integer year, month, day;
    private Precision precision;
    private Boolean complete;
    private String habitat, pubNotes, privNotes, geology;
    private String[] tags, observers, collectors, dets;
    private String verbLocality, locality, municipality, province, county;
    private String code;
    private String threats;
    private String maintainer;
    private Float area, totalCover, meanHeight;
    private String aspect;
    private Integer slope;
    /**
     * This list holds the occurrencces ({@link newOBSERVED_IN}) that are not yet matched to the taxonomic graph.
     * Occurrences that are matched are removed from this list and converted into graph links.
     * All new occurrences shall go in here.
     */
    private List<newOBSERVED_IN> unmatchedOccurrences;

    /**
     * This list shall be populated, when needed, with all matched occurrences in this inventory TODO: this is a workaround for now...
     */
    @Expose(serialize = false)
    private newOBSERVED_IN[] taxa;
    /**
     * This list shall be populated, when needed, with the observer names.
     * Remember that only the observer IDs are stored in the field "observers"
     */
    @Expose(serialize = false)
    private String[] observerNames;

    @Expose(serialize = false)
    private Float utmX, utmY;

    public Inventory(Inventory other) {
        super(other);
        this.latitude = other.latitude;
        this.longitude = other.longitude;
        this.spatialRS = other.spatialRS;
        this.elevation = other.elevation;
        this.geometry = other.geometry;
        this.precision = other.precision;
        this.year = other.year;
        this.month = other.month;
        this.day = other.day;
        this.complete = other.complete;
        this.habitat = other.habitat;
        this.pubNotes = other.pubNotes;
        this.privNotes = other.privNotes;
        this.geology = other.geology;
        this.tags = other.tags;
        this.observers = other.observers;
        this.collectors = other.collectors;
        this.dets = other.dets;
        this.verbLocality = other.verbLocality;
        this.locality = other.locality;
        this.municipality = other.municipality;
        this.province = other.province;
        this.county = other.county;
        this.code = other.code;
        this.threats = other.threats;
        this.maintainer = other.maintainer;
        this.area = other.area;
        this.totalCover = other.totalCover;
        this.meanHeight = other.meanHeight;
        this.aspect = other.aspect;
        this.slope = other.slope;
        this.observerNames = other.observerNames;
    }

    public Inventory() { }

    /**
     * Check if latitude-longitude coordinates are set. If not, try to convert from UTM, if set.
     */
    private void checkGeographicCoordinates() {
        if((latitude == null || longitude == null) && utmX != null && utmY != null) {
            // TODO support for UTM zones!!
            LatLongCoordinate llc = CoordinateConversion.UtmToLatLonWGS84(29, 'S', utmX.longValue(), utmY.longValue());
            this.latitude = llc.getLatitude();
            this.longitude = llc.getLongitude();
        }
    }
    /**
     * Returns the latitude of the inventory, OR, if there is only one observation, returns latitude of that observation, if set.
     * @return
     */
    @Override
    public Float getLatitude() {
        checkGeographicCoordinates();
        return (_getTaxa() != null && _getTaxa().length == 1) ? (_getTaxa()[0].getObservationLatitude() == null ?
                latitude : _getTaxa()[0].getObservationLatitude()) : latitude;
//        return latitude == null ? ((_getTaxa() != null && _getTaxa().length == 1) ? _getTaxa()[0].getObservationLatitude() : null) : latitude;
    }

    @Override
    public void setLatitude(Float latitude) {
        this.latitude = latitude;
    }

    @Override
    public Float getLongitude() {
        checkGeographicCoordinates();
        return (_getTaxa() != null && _getTaxa().length == 1) ? (_getTaxa()[0].getObservationLongitude() == null ?
                longitude : _getTaxa()[0].getObservationLongitude()) : longitude;
//        return longitude == null ? ((_getTaxa() != null && _getTaxa().length == 1) ? _getTaxa()[0].getObservationLongitude() : null) : longitude;
    }

    @Override
    public void _setUTMX(Float x) {
        this.utmX = x;
    }

    @Override
    public Float _getsetUTMX() {
        return this.utmX;
    }

    @Override
    public void _setUTMY(Float y) {
        this.utmY = y;
    }

    @Override
    public Float _getsetUTMY() {
        return this.utmY;
    }

    @Override
    public void setLongitude(Float longitude) {
        this.longitude = longitude;
    }

    public String getSpatialRS() {
        return spatialRS;
    }

    public void setSpatialRS(String spatialRS) {
        this.spatialRS = spatialRS;
    }

    public Float getElevation() {
        return elevation;
    }

    public void setElevation(Float elevation) {
        this.elevation = elevation;
    }

    public String getGeometry() {
        return geometry;
    }

    public void setGeometry(String geometry) {
        this.geometry = geometry;
    }

    public Precision getPrecision() {
        return precision;
    }

    public void setPrecision(String precision) throws FloraOnException {
        this.precision = new Precision(precision);
    }

    public void setPrecision(Precision precision) {
        this.precision = precision;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Integer getMonth() {
        return month;
    }

    public void setMonth(Integer month) {
        if(month != null && (month < 1 || month > 12)) {
            Log.warn("Invalid month " + month);
            return;
        }
        this.month = month;
    }

    public Integer getDay() {
        return day;
    }

    public void setDay(Integer day) {
        if(day != null && (day < 1 || day > 31)) {
            Log.warn("Invalid day " + day);
            return;
//            throw new IllegalArgumentException("Invalid day " + day);
        }
        this.day = day;
    }

    public String _getDate() {
/*
        Calendar c = new GregorianCalendar();
        if(year != null) c.set(Calendar.YEAR, year);
        if(month != null) c.set(Calendar.MONTH, month);
        if(day != null) c.set(Calendar.DAY_OF_MONTH, day);
        return Constants.dateFormat.format(c.getTime());
*/
        StringBuilder sb = new StringBuilder();
        sb.append(day == null ? "--" : day).append("/")
                .append(month == null ? "--" : month).append("/")
                .append(year == null ? "----" : year);
        return sb.toString();
    }

    public String _getDateYMD() {
        StringBuilder sb = new StringBuilder();
        sb.append(year == null ? "----" : year).append("/")
                .append(month == null ? "--" : month).append("/")
                .append(day == null ? "--" : day);
        return sb.toString();
    }

    public String _getCoordinates() {
        if(this.getLatitude() == null || this.getLongitude() == null)
            return "*";
        else
            return String.format(Locale.ROOT, "%.5f, %.5f", this.getLatitude(), this.getLongitude());
    }

    public UTMCoordinate _getUTMCoordinates() {
        return CoordinateConversion.LatLonToUtmWGS84(this.getLatitude(), this.getLongitude(), 0);
    }

    public Boolean getComplete() {
        return complete;
    }

    public void setComplete(Boolean complete) {
        this.complete = complete;
    }

    public String getHabitat() {
        return habitat;
    }

    public void setHabitat(String habitat) {
        this.habitat = habitat;
    }

    public String getPubNotes() {
        return pubNotes;
    }

    public void setPubNotes(String pubNotes) {
        this.pubNotes = pubNotes;
    }

    public String getPrivNotes() {
        return privNotes;
    }

    public void setPrivNotes(String privNotes) {
        this.privNotes = privNotes;
    }

    public String getGeology() {
        return geology;
    }

    public void setGeology(String geology) {
        this.geology = geology;
    }

    public String[] getTags() {
        return tags;
    }

    public void setTags(String[] tags) {
        this.tags = tags;
    }

    public String[] getObservers() {
        return StringUtils.isArrayEmpty(observers) ? new String[0] : observers;
    }

    public void setObservers(String[] observers) {
        this.observers = observers;
    }

    public String[] getCollectors() {
        return StringUtils.isArrayEmpty(collectors) ? new String[0] : collectors;
    }

    public void setCollectors(String[] collectors) {
        this.collectors = collectors;
    }

    public String[] getDets() {
        return StringUtils.isArrayEmpty(dets) ? new String[0] : dets;
    }

    public void setDets(String[] dets) {
        this.dets = dets;
    }

    @Override
    public String getVerbLocality() {
        return verbLocality;
    }

    @Override
    public void setVerbLocality(String verbLocality) {
        this.verbLocality = verbLocality;
    }

    @Override
    public String getLocality() {
        return locality;
    }

    @Override
    public void setLocality(String locality) {
        this.locality = locality;
    }

    @Override
    public String getMunicipality() {
        return municipality;
    }

    @Override
    public void setMunicipality(String municipality) {
        this.municipality = municipality;
    }

    @Override
    public String getProvince() {
        return province;
    }

    @Override
    public void setProvince(String province) {
        this.province = province;
    }

    @Override
    public String getCounty() {
        return county;
    }

    @Override
    public void setCounty(String county) {
        this.county = county;
    }

    public String getCode() {
        return code == null ? ((_getTaxa() != null && _getTaxa().length == 1) ? _getTaxa()[0].getGpsCode() : null) : code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getThreats() {
        return threats;
    }

    public void setThreats(String threats) {
        this.threats = threats;
    }

    public String getMaintainer() {
        return maintainer;
    }

    public void setMaintainer(String maintainer) {
        this.maintainer = maintainer;
    }

    public Float getArea() {
        return area;
    }

    public void setArea(Float area) {
        this.area = area;
    }

    public Float getTotalCover() {
        return totalCover;
    }

    public void setTotalCover(Float totalCover) {
        this.totalCover = totalCover;
    }

    public Float getMeanHeight() {
        return meanHeight;
    }

    public void setMeanHeight(Float meanHeight) {
        this.meanHeight = meanHeight;
    }

    public String getAspect() {
        return aspect;
    }

    public void setAspect(String aspect) {
        this.aspect = aspect;
    }

    public Integer getSlope() {
        return slope;
    }

    public void setSlope(Integer slope) {
        this.slope = slope;
    }

    public List<newOBSERVED_IN> getUnmatchedOccurrences() {
        return unmatchedOccurrences == null ? (this.unmatchedOccurrences = new ArrayList<>()) : unmatchedOccurrences;
    }

    public void setUnmatchedOccurrences(List<newOBSERVED_IN> unmatchedOccurrences) {
        this.unmatchedOccurrences = unmatchedOccurrences;
    }

    /* ****************************************/
    /* Getters for transient fields           */
    /* ****************************************/

    public String[] _getObserverNames() {
        return StringUtils.isArrayEmpty(this.observerNames) ? new String[0] : this.observerNames;
    }

    public void _setObserverNames(String[] observerNames) {
        this.observerNames = observerNames;
    }

    public newOBSERVED_IN[] _getTaxa() {
        return StringUtils.isArrayEmpty(this.taxa) ?
                (unmatchedOccurrences == null ?
                        new newOBSERVED_IN[0] : unmatchedOccurrences.toArray(new newOBSERVED_IN[unmatchedOccurrences.size()]))
                : this.taxa;
    }

    public List<newOBSERVED_IN> _getOccurrences() {
        // FIXME: this should return the occurrences that are graph links aswell!
        return getUnmatchedOccurrences();
    }

    /**
     * Gets a textual summary of the taxa.
     * @param nTaxa How many taxa to show
     * @return
     */
    public String _getSampleTaxa(int nTaxa) {
        newOBSERVED_IN[] tmp = _getTaxa();
        if(tmp.length == 0) return "[sem taxa]";
        List<String> tmp1 = new ArrayList<>();
        int i;
        for (i = 0; i < nTaxa && i < tmp.length; i++) {
            if(tmp[i].getTaxEnt() == null) {
                if(tmp[i].getVerbTaxon() == null || tmp[i].getVerbTaxon().equals(""))
                    tmp1.add("[sem nome]");
                else
                    tmp1.add(tmp[i].getVerbTaxon());
            } else
                tmp1.add("<i>" + tmp[i].getTaxEnt().getName() + "</i>");
        }
        if(i < tmp.length) tmp1.add("... e mais " + (tmp.length - i));
        return StringUtils.implode(", ", tmp1.toArray(new String[tmp1.size()]));
    }

    @Override
    public Constants.NodeTypes getType() {
        return Constants.NodeTypes.inventory;
    }

    @Override
    public String getTypeAsString() {
        return this.getType().toString();
    }

    @Override
    public JsonObject toJson() {
        return null;
    }

    @Override
    public String toJsonString() {
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Inventory that = (Inventory) o;

        if(code != null) return code.equals(that.code);
        if ((precision != null && precision._isImprecise()) || (that.precision != null && that.precision._isImprecise())
            || (precision != null ? !precision.equals(that.precision) : that.precision != null)) return false;
        if (getLatitude() != null ? !getLatitude().equals(that.getLatitude()) : that.getLatitude() != null) return false;
        if (getLongitude() != null ? !getLongitude().equals(that.getLongitude()) : that.getLongitude() != null) return false;
        if (year != null ? !year.equals(that.year) : that.year != null) return false;
        if (month != null ? !month.equals(that.month) : that.month != null) return false;
        if (day != null ? !day.equals(that.day) : that.day != null) return false;
        if (municipality != null ? !municipality.equals(that.municipality) : that.municipality != null) return false;
        if (county != null ? !county.equals(that.county) : that.county != null) return false;
        if (locality != null ? !locality.equals(that.locality) : that.locality != null) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(observers, that.observers)) return false;
        return code != null ? code.equals(that.code) : that.code == null;
    }

    /**
     * NOTE that this implementation of hashCode and equals assume that if an inventory is in the same place, same date
     * and same observers, then it is the same inventory, no matter the other fields. If the inventory is imprecise, it
     * will never be equal to another one.
     * @return
     */
    @Override
    public int hashCode() {
        int result = code != null ? code.hashCode() : 0;    // NOTE: we don't use the getter here cause the getter does some processing to avoid nulls. here we want the real inventory code as is.
        if(code != null && !code.equals("")) return result;     // code rules!
        result = 31 * result + (precision != null ? precision.hashCode() : 0);
        result = 31 * result + (getLatitude() != null ? getLatitude().hashCode() : 0);
        result = 31 * result + (getLongitude() != null ? getLongitude().hashCode() : 0);
        result = 31 * result + (year != null ? year.hashCode() : 0);
        result = 31 * result + (month != null ? month.hashCode() : 0);
        result = 31 * result + (day != null ? day.hashCode() : 0);
        result = 31 * result + (municipality != null ? municipality.hashCode() : 0);
        result = 31 * result + (county != null ? county.hashCode() : 0);
        result = 31 * result + (locality != null ? locality.hashCode() : 0);
        result = 31 * result + (observers != null ? Arrays.hashCode(observers) : 0);
        return result;
    }
}
