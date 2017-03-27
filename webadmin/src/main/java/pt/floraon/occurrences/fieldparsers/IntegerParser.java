package pt.floraon.occurrences.fieldparsers;

import pt.floraon.occurrences.Messages;
import pt.floraon.occurrences.entities.Inventory;

/**
 * Created by miguel on 12-02-2017.
 */
public class IntegerParser implements FieldParser {
    @Override
    public void parseValue(String inputValue, String inputFieldName, Inventory occurrence) throws IllegalArgumentException {
        if(inputValue == null || inputValue.trim().equals("")) return;
        Integer v;
        try {
            v = Integer.parseInt(inputValue);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(e);
        }

        switch(inputFieldName.toLowerCase()) {
            case "year":
                occurrence.setYear(v);
                break;

            case "month":
                occurrence.setMonth(v);
                break;

            case "day":
                occurrence.setDay(v);
                break;

            default:
                throw new IllegalArgumentException(Messages.getString("error.1", inputFieldName));
        }
    }
}
