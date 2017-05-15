package pt.floraon.occurrences.fieldparsers;

import pt.floraon.driver.GeoBean;
import pt.floraon.driver.parsers.FieldParser;
import pt.floraon.geometry.*;
import pt.floraon.occurrences.Messages;

/**
 * Created by miguel on 26-04-2017.
 */
public class UTMCoordinateParser implements FieldParser {
    @Override
    public void parseValue(String inputValue, String inputFieldName, Object bean) throws IllegalArgumentException {
        GeoBean occurrence = (GeoBean) bean;
        if(inputValue == null || inputValue.trim().equals("")) return;

        Float v;

        switch(inputFieldName.toLowerCase()) {
            case "x":
                try {
                    v = Float.parseFloat(inputValue);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException(e);
                }
                occurrence._setUTMX(v);
                break;

            case "y":
                try {
                    v = Float.parseFloat(inputValue);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException(e);
                }
                occurrence._setUTMY(v);
                break;
            // TODO UTM zone parsing, military grid, etc.

            case "mgrs":
/*
                CoordinateConversion2 cc = new CoordinateConversion2();
                double[] ll;
                try {
                    ll = cc.mgrutm2LatLon(inputValue);
                } catch (Throwable e) {
                    throw new IllegalArgumentException(e);
                }
                occurrence.setLatitude((float) ll[0]);
                occurrence.setLongitude((float) ll[1]);
                String utm = cc.latLon2UTM(ll[0], ll[1]);
*/
                UTMCoordinate utm = CoordinateConversion.MGRSToUTM(inputValue);
                LatLongCoordinate ll = CoordinateConversion.UtmToLatLonWGS84(utm.getXZone(), utm.getYZone(), utm.getX(), utm.getY());
                occurrence.setLatitude(ll.getLatitude());
                occurrence.setLongitude(ll.getLongitude());
                occurrence.setPrecision(utm.getPrecision());
                System.out.println(utm.toString());
                break;

            default:
                throw new IllegalArgumentException(Messages.getString("error.1", inputFieldName));
        }
    }
}
