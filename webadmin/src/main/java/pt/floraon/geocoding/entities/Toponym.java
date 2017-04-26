package pt.floraon.geocoding.entities;

import com.google.gson.JsonObject;
import pt.floraon.driver.Constants;
import pt.floraon.driver.entities.NamedDBNode;

/**
 * Created by miguel on 16-04-2017.
 */
public class Toponym extends NamedDBNode {
    private Float latitude, longitude, elevation;
    private String toponymType;

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
    public JsonObject toJson() {
        return null;
    }

    @Override
    public String toJsonString() {
        return null;
    }
}
