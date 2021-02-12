package pt.floraon.occurrences.fields.parsers;

import pt.floraon.driver.FloraOnException;
import pt.floraon.driver.parsers.FieldParser;
import pt.floraon.occurrences.Messages;
import pt.floraon.occurrences.entities.Inventory;
import pt.floraon.occurrences.fields.FieldReflection;

import java.lang.reflect.InvocationTargetException;

/**
 * All field parsers must extend this class.
 */
public abstract class GlobalFieldParser implements FieldParser {

    public abstract Object preProcessValue(String inputValue) throws IllegalArgumentException;

    public abstract Class getType(String inputFieldName);

    /**
     * Return true if the special case has been processed by the implementation. Otherwise, an error is thrown.
     * @param inventory
     * @param inputFieldName
     * @param processedValue
     * @return
     */
    public abstract boolean processSpecialCases(Inventory inventory, String inputFieldName, Object processedValue);

    @Override
    public void parseValue(String inputValue, String inputFieldName, Object bean) throws IllegalArgumentException, FloraOnException {
        if (inputValue == null) return;
        Inventory inventory = (Inventory) bean;

        Object processedValue = this.preProcessValue(inputValue);
        if(processedValue == null) return;
        try {
            FieldReflection.setFieldValueAny(inventory, inputFieldName, getType(inputFieldName), processedValue);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            if(!processSpecialCases(inventory, inputFieldName, processedValue)) {
                e.printStackTrace();
                throw new IllegalArgumentException(Messages.getString("error.1", inputFieldName));
            }
        }
    }
}
