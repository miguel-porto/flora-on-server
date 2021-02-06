package pt.floraon.occurrences.fields.parsers;

import pt.floraon.occurrences.entities.Inventory;

/**
 * Created by miguel on 12-02-2017.
 */
public class IntegerParser extends GlobalFieldParser {
    @Override
    public Object preProcessValue(String inputValue) throws IllegalArgumentException {
        if(inputValue == null || inputValue.trim().equals("") || inputValue.trim().equalsIgnoreCase("na")) return null;
        Integer v;
        try {
            v = ((Float) Float.parseFloat(inputValue)).intValue();
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(e);
        }
        return v;
    }

    @Override
    public Class getType(String inputFieldName) {
        return Integer.class;
    }

    @Override
    public boolean processSpecialCases(Inventory inventory, String inputFieldName, Object processedValue) {
        return false;
    }
/*
    @Override
    public void parseValue(String inputValue, String inputFieldName, Object bean) throws IllegalArgumentException {
        if(inputValue == null || inputValue.trim().equals("")) return;
        Inventory inventory = (Inventory) bean;
        Integer v;
        try {
            v = ((Float) Float.parseFloat(inputValue)).intValue();
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(e);
        }

        try {
            // try reflection. If setter is not found, handle special cases.
            FieldReflection.setFieldValueInteger(inventory, inputFieldName, v);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            switch (inputFieldName.toLowerCase()) {
                default:
                    e.printStackTrace();
                    throw new IllegalArgumentException(Messages.getString("error.1", inputFieldName));
            }
        }

/*
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
    */
}
