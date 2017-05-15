package pt.floraon.driver;

import pt.floraon.geometry.Precision;

/**
 * Created by miguel on 16-04-2017.
 */
public interface GeoBean {
    void setLatitude(Float latitude);
    Float getLatitude();
    void setLongitude(Float longitude);
    Float getLongitude();

    void _setUTMX(Float x);
    Float _getsetUTMX();
    void _setUTMY(Float y);
    Float _getsetUTMY();

    Precision getPrecision();
    void setPrecision(String precision) throws FloraOnException;
    void setPrecision(Precision precision);
}
