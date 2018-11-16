package pt.floraon.occurrences.fields.parsers;

import pt.floraon.driver.FloraOnException;
import pt.floraon.driver.GeoBean;
import pt.floraon.driver.parsers.FieldParser;
import pt.floraon.geocoding.entities.Toponym;
import pt.floraon.occurrences.Messages;

/**
 * Created by miguel on 19-05-2017.
 */
public class LocalityParser implements FieldParser {
    @Override
    public void parseValue(String inputValue, String inputFieldName, Object bean) throws IllegalArgumentException, FloraOnException {
        if (inputValue == null) return;
        GeoBean occurrence = (GeoBean) bean;

        switch (inputFieldName.toLowerCase()) {
            case "verblocality":
                occurrence.setVerbLocality(inputValue);
                break;

/*
            case "locality":
                occurrence.setLocality(inputValue);
                break;

            case "municipality":
                occurrence.setMunicipality(inputValue);
                break;

            case "province":
                occurrence.setProvince(inputValue);
                break;

            case "county":
                occurrence.setCounty(inputValue);
                break;
*/
            case "precision":
                occurrence.setPrecision(inputValue);
                break;

            case "type":
                ((Toponym) occurrence).setToponymType(inputValue);
                break;

            default:
                throw new IllegalArgumentException(Messages.getString("error.1", inputFieldName));
        }
    }
}
