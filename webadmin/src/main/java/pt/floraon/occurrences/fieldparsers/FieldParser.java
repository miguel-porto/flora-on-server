package pt.floraon.occurrences.fieldparsers;

import pt.floraon.driver.FloraOnException;
import pt.floraon.occurrences.entities.Inventory;

/**
 * Created by miguel on 08-02-2017.
 */
public interface FieldParser {
    void parseValue(String inputValue, String inputFieldName, Inventory occurrence) throws IllegalArgumentException, FloraOnException;
}
