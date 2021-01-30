package pt.floraon.occurrences.fields.parsers;

import pt.floraon.driver.parsers.FieldParser;
import pt.floraon.driver.FloraOnException;
import pt.floraon.occurrences.Messages;
import pt.floraon.occurrences.entities.Inventory;

/**
 * Created by miguel on 31-03-2017.
 */
public class BooleanParser extends GlobalFieldParser {
    @Override
    public Object preProcessValue(String inputValue) throws IllegalArgumentException {
        if(inputValue == null || inputValue.trim().equals("")) return null;
        Boolean v = null;
        try {
            if(inputValue.equalsIgnoreCase("1") || inputValue.equalsIgnoreCase("true")
                    || inputValue.equalsIgnoreCase("sim") || inputValue.equalsIgnoreCase("yes")
                    || inputValue.equalsIgnoreCase("y") || inputValue.equalsIgnoreCase("s")
                    || inputValue.equalsIgnoreCase("t"))
                v = true;
            if(inputValue.equalsIgnoreCase("0") || inputValue.equalsIgnoreCase("false")
                    || inputValue.equalsIgnoreCase("não") || inputValue.equalsIgnoreCase("no")
                    || inputValue.equalsIgnoreCase("n") || inputValue.equalsIgnoreCase("nao")
                    || inputValue.equalsIgnoreCase("f"))
                v = false;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(e);
        }
        return v;
    }

    @Override
    public Class getType(String inputFieldName) {
        return Boolean.class;
    }

    @Override
    public boolean processSpecialCases(Inventory inventory, String inputFieldName, Object processedValue) {
        return false;
    }
}
