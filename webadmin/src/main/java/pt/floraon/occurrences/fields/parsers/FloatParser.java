package pt.floraon.occurrences.fields.parsers;

import pt.floraon.driver.Constants;
import pt.floraon.occurrences.entities.Inventory;

public class FloatParser extends GlobalFieldParser {
    @Override
    public Object preProcessValue(String inputValue) throws IllegalArgumentException {
        if(inputValue == null) return null;
        if(inputValue.trim().equals("")) return Constants.NODATA;
        Float v;
        try {
            v = Float.parseFloat(inputValue);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(e);
        }
        return v;
    }

    @Override
    public Class getType(String inputFieldName) {
        return Float.class;
    }

    @Override
    public boolean processSpecialCases(Inventory inventory, String inputFieldName, Object processedValue) {
        return false;
    }
}
