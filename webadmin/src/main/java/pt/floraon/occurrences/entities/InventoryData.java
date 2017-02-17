package pt.floraon.occurrences.entities;

import com.google.gson.JsonObject;
import pt.floraon.driver.Constants;
import pt.floraon.driver.DiffableBean;
import pt.floraon.driver.entities.GeneralDBNode;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Created by miguel on 05-02-2017.
 */
public class InventoryData extends GeneralDBNode implements Serializable, DiffableBean {
    private Float latitude, longitude;
    private String spatialRS;
    private Float elevation;
    private String geometry;
    private Integer precision, gridPrecision, year, month, day;
    private boolean complete;
    private String habitat, pubNotes, privNotes, geology;
    private String[] tags, observers, collectors, dets;
    private String verbLocality, locality, municipality, province, county;
    private String code;
    private String threats;
    private String maintainer;
    private Float area, totalCover, meanHeight;
    private String aspect;
    private Integer slope;

    public InventoryData(InventoryData other) {
        super(other);
        this.latitude = other.latitude;
        this.longitude = other.longitude;
        this.spatialRS = other.spatialRS;
        this.elevation = other.elevation;
        this.geometry = other.geometry;
        this.precision = other.precision;
        this.gridPrecision = other.gridPrecision;
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
    }

    public InventoryData() { }

    public Float getLatitude() {
        return latitude;
    }

    public void setLatitude(Float latitude) {
        this.latitude = latitude;
    }

    public Float getLongitude() {
        return longitude;
    }

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

    public Integer getPrecision() {
        return precision;
    }

    public void setPrecision(Integer precision) {
        this.precision = precision;
    }

    public Integer getGridPrecision() {
        return gridPrecision;
    }

    public void setGridPrecision(Integer gridPrecision) {
        this.gridPrecision = gridPrecision;
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
        this.month = month;
    }

    public Integer getDay() {
        return day;
    }

    public void setDay(Integer day) {
        if(day < 1 || day > 31) throw new IllegalArgumentException("Invalid day");
        this.day = day;
    }

    public boolean isComplete() {
        return complete;
    }

    public void setComplete(boolean complete) {
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
        return observers;
    }

    public void setObservers(String[] observers) {
        this.observers = observers;
    }

    public String[] getCollectors() {
        return collectors;
    }

    public void setCollectors(String[] collectors) {
        this.collectors = collectors;
    }

    public String[] getDets() {
        return dets;
    }

    public void setDets(String[] dets) {
        this.dets = dets;
    }

    public String getVerbLocality() {
        return verbLocality;
    }

    public void setVerbLocality(String verbLocality) {
        this.verbLocality = verbLocality;
    }

    public String getLocality() {
        return locality;
    }

    public void setLocality(String locality) {
        this.locality = locality;
    }

    public String getMunicipality() {
        return municipality;
    }

    public void setMunicipality(String municipality) {
        this.municipality = municipality;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCounty() {
        return county;
    }

    public void setCounty(String county) {
        this.county = county;
    }

    public String getCode() {
        return code;
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

    @Override
    public Constants.NodeTypes getType() {
        return Constants.NodeTypes.specieslist;
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

        InventoryData that = (InventoryData) o;

        if(code != null) return code.equals(that.code);
        if (latitude != null ? !latitude.equals(that.latitude) : that.latitude != null) return false;
        if (longitude != null ? !longitude.equals(that.longitude) : that.longitude != null) return false;
        if (year != null ? !year.equals(that.year) : that.year != null) return false;
        if (month != null ? !month.equals(that.month) : that.month != null) return false;
        if (day != null ? !day.equals(that.day) : that.day != null) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(observers, that.observers)) return false;
        return code != null ? code.equals(that.code) : that.code == null;
    }

    /**
     * Note that this implementation of hashCode and equals assume that if an inventory is in the same place, same date
     * and same observers, then it is the same inventory, no matter the other fields.
     * @return
     */
    @Override
    public int hashCode() {
        int result = code != null ? code.hashCode() : 0;
        if(code != null) return result;     // code rules!
        result = 31 * result + (latitude != null ? latitude.hashCode() : 0);
        result = 31 * result + (longitude != null ? longitude.hashCode() : 0);
        result = 31 * result + (year != null ? year.hashCode() : 0);
        result = 31 * result + (month != null ? month.hashCode() : 0);
        result = 31 * result + (day != null ? day.hashCode() : 0);
        result = 31 * result + Arrays.hashCode(observers);
        return result;
    }
}
