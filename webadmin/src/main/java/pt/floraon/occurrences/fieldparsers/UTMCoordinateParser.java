package pt.floraon.occurrences.fieldparsers;

import pt.floraon.driver.GeoBean;
import pt.floraon.driver.parsers.FieldParser;
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
        try {
            v = Float.parseFloat(inputValue);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(e);
        }

        switch(inputFieldName.toLowerCase()) {
            case "x":
                occurrence._setUTMX(v);
                break;

            case "y":
                occurrence._setUTMY(v);
                break;

            default:
                throw new IllegalArgumentException(Messages.getString("error.1", inputFieldName));
        }
    }
}
