package pt.floraon.occurrences.fieldparsers;

import pt.floraon.driver.FloraOnException;
import pt.floraon.occurrences.entities.Inventory;

/**
 * Created by miguel on 14-02-2017.
 */
public class PlainTextParser implements FieldParser {
    @Override
    public void parseValue(String inputValue, String inputFieldName, Inventory occurrence) throws IllegalArgumentException, FloraOnException {
        if (inputValue == null || inputValue.trim().equals("")) return;

        switch (inputFieldName.toLowerCase()) {
            case "code":
                occurrence.getInventoryData().setCode(inputValue);
                break;
        }
    }
}
