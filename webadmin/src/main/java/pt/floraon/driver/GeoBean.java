package pt.floraon.driver;

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
}
