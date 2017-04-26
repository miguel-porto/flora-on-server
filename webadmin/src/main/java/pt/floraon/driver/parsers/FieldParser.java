package pt.floraon.driver.parsers;

import pt.floraon.driver.FloraOnException;

/**
 * Created by miguel on 08-02-2017.
 */
public interface FieldParser {
    void parseValue(String inputValue, String inputFieldName, Object bean) throws IllegalArgumentException, FloraOnException;
}
