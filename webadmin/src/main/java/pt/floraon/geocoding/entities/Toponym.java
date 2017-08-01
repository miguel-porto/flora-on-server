package pt.floraon.geocoding.entities;

import com.google.gson.JsonObject;
import pt.floraon.driver.Constants;
import pt.floraon.driver.FloraOnException;
import pt.floraon.driver.GeoBean;
import pt.floraon.driver.entities.NamedDBNode;
import pt.floraon.geometry.Precision;

/**
 * Created by miguel on 16-04-2017.
 */
public class Toponym extends NamedDBNode implements GeoBean {
    private Float latitude, longitude, elevation;
    private String toponymType;
    private String locality, municipality, province, county;

    @Override
    public Float getLatitude() {
        return latitude;
    }

    @Override
    public void setLatitude(Float latitude) {
        this.latitude = latitude;
    }

    @Override
    public Float getLongitude() {
        return longitude;
    }

    @Override
    public void setLongitude(Float longitude) {
        this.longitude = longitude;
    }

    @Override
    public void _setUTMX(Float x) {

    }

    @Override
    public Float _getsetUTMX() {
        return null;
    }

    @Override
    public void _setUTMY(Float y) {

    }

    @Override
    public Float _getsetUTMY() {
        return null;
    }

    @Override
    public Precision getPrecision() {
        return null;
    }

    @Override
    public void setPrecision(String precision) throws FloraOnException {

    }

    @Override
    public void setPrecision(Precision precision) {

    }

    @Override
    public String getVerbLocality() {
        return null;
    }

    @Override
    public void setVerbLocality(String verbLocality) {

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

    public Float getElevation() {
        return elevation;
    }

    public void setElevation(Float elevation) {
        this.elevation = elevation;
    }

    public String getToponymType() {
        return toponymType;
    }

    public void setToponymType(String toponymType) {
        this.toponymType = toponymType;
    }

    @Override
    public Constants.NodeTypes getType() {
        return Constants.NodeTypes.toponym;
    }

    @Override
    public String getTypeAsString() {
        return this.getType().toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Toponym toponym = (Toponym) o;

        if (!latitude.equals(toponym.latitude)) return false;
        if (!longitude.equals(toponym.longitude)) return false;
        if (toponymType != null ? !toponymType.equals(toponym.toponymType) : toponym.toponymType != null) return false;
        return locality.equals(toponym.locality);
    }

    @Override
    public int hashCode() {
        int result = latitude.hashCode();
        result = 31 * result + longitude.hashCode();
        result = 31 * result + (toponymType != null ? toponymType.hashCode() : 0);
        result = 31 * result + locality.hashCode();
        return result;
    }
}
