package pt.floraon.occurrences.fields.parsers;

import pt.floraon.driver.parsers.FieldParser;
import pt.floraon.occurrences.Messages;
import pt.floraon.occurrences.entities.Inventory;
import pt.floraon.occurrences.entities.OBSERVED_IN;

/**
 * Created by miguel on 12-02-2017.
 */
public class IntegerParser implements FieldParser {
    @Override
    public void parseValue(String inputValue, String inputFieldName, Object bean) throws IllegalArgumentException {
        if(inputValue == null || inputValue.trim().equals("")) return;
        Inventory occurrence = (Inventory) bean;
        Integer v;
        try {
            v = ((Float) Float.parseFloat(inputValue)).intValue();
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

            case "elevation":
                occurrence.setElevation(v.floatValue());
                break;

            case "hasspecimen":
                if(occurrence.getUnmatchedOccurrences().size() == 0)
                    occurrence.getUnmatchedOccurrences().add(new OBSERVED_IN(true));
                for(OBSERVED_IN obs : occurrence.getUnmatchedOccurrences())
                    obs.setHasSpecimen(v);
                break;

            default:
                throw new IllegalArgumentException(Messages.getString("error.1", inputFieldName));
        }
    }
}
