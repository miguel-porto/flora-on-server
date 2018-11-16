package pt.floraon.occurrences.fields.parsers;

import pt.floraon.driver.parsers.FieldParser;
import pt.floraon.driver.FloraOnException;
import pt.floraon.occurrences.Messages;
import pt.floraon.occurrences.entities.Inventory;

/**
 * Created by miguel on 31-03-2017.
 */
public class BooleanParser implements FieldParser {
    @Override
    public void parseValue(String inputValue, String inputFieldName, Object bean) throws IllegalArgumentException, FloraOnException {
        boolean value = false;
        Inventory occurrence = (Inventory) bean;

        if (inputValue != null && !inputValue.trim().equals("")) {
            if(inputValue.toLowerCase().equals("1") || inputValue.toLowerCase().equals("true")
                    || inputValue.toLowerCase().equals("sim") || inputValue.toLowerCase().equals("yes")
                    || inputValue.toLowerCase().equals("y") || inputValue.toLowerCase().equals("s")
                    || inputValue.toLowerCase().equals("t"))
                value = true;
        }
//System.out.println(inputFieldName.toLowerCase()+": "+value);
        switch (inputFieldName.toLowerCase()) {

            default:
                throw new IllegalArgumentException(Messages.getString("error.1", inputFieldName));
        }
    }
}
