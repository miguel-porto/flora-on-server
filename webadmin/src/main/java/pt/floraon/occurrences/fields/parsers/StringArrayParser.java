package pt.floraon.occurrences.fields.parsers;

import pt.floraon.occurrences.entities.Inventory;

/**
 * Parses a comma- or plus-separated list of words into a string array.
 */
public class StringArrayParser extends GlobalFieldParser {
    @Override
    public Object preProcessValue(String inputValue) throws IllegalArgumentException {
        String[] spl = inputValue.split("\\+");
        if(spl.length == 1) spl = inputValue.split(",");
        for(int i=0; i < spl.length; i++)
            spl[i] = spl[i].trim();
        return spl;
    }

    @Override
    public Class getType(String inputFieldName) {
        return String[].class;
    }

    @Override
    public boolean processSpecialCases(Inventory inventory, String inputFieldName, Object processedValue) {
        return false;
    }
}
