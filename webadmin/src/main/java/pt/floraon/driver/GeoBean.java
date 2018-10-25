package pt.floraon.driver;

import pt.floraon.geometry.Precision;

/**
 * Created by miguel on 16-04-2017.
 */
public interface GeoBean {
    void setLatitude(Float latitude);

    /**
     * The getter for the inventory latitude field. No processing is done. Can be null.
     * @return
     */
    Float getLatitude();
    void setLongitude(Float longitude);

    /**
     * The getter for the inventory longitude field. No processing is done. Can be null.
     * @return
     */
    Float getLongitude();

    void _setUTMX(Float x);
    Float _getsetUTMX();
    void _setUTMY(Float y);
    Float _getsetUTMY();

    Precision getPrecision();
    void setPrecision(String precision) throws FloraOnException;
    void setPrecision(Precision precision);

    String getVerbLocality();
    void setVerbLocality(String verbLocality);
    String getLocality();
    void setLocality(String locality);
    String getMunicipality();
    void setMunicipality(String municipality);
    String getProvince();
    void setProvince(String province);
    String getCounty();
    void setCounty(String county);
}
