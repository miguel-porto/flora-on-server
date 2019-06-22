package pt.floraon.occurrences.fields.parsers;

import pt.floraon.occurrences.entities.Inventory;

/**
 * General field parser for String-based fields.
 * Created by miguel on 14-02-2017.
 */
public class GeneralFieldParser extends GlobalFieldParser {
    @Override
    public Object preProcessValue(String inputValue) {
        return inputValue;
    }

    @Override
    public Class getType(String inputFieldName) {
        return String.class;
    }

    @Override
    public boolean processSpecialCases(Inventory inventory, String inputFieldName, Object processedValue) {
        switch (inputFieldName.toLowerCase()) {
            case "inventoryid": // special cases: this one is for updating data
                inventory.setID((String) processedValue);
                break;

            default:
                return false;
        }
        return true;
    }

    /*
    @Override
    public void parseValue(String inputValue, String inputFieldName, Object bean) throws IllegalArgumentException {
        if (inputValue == null) return;
        Inventory inventory = (Inventory) bean;

        try {
            // try reflection. If setter is not found, handle special cases.
            FieldReflection.setFieldValueString(inventory, inputFieldName, inputValue);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            switch (inputFieldName.toLowerCase()) {
                case "inventoryid": // special cases: this one is for updating data
                    inventory.setID(inputValue);
                    break;

                default:
                    e.printStackTrace();
                    throw new IllegalArgumentException(Messages.getString("error.1", inputFieldName));
            }
        }


        switch (inputFieldName.toLowerCase()) {
            case "code":
                inventory.setCode(inputValue);
                break;

            case "habitat":
                inventory.setHabitat(inputValue);
                break;

            case "threats":
                inventory.setThreats(inputValue);
                break;

            case "inventoryid":
                inventory.setID(inputValue);
                break;

            case "gpscode":
            case "comment":
            case "privatecomment":
            case "labeldata":
            case "specificthreats":
            case "accession":
            case "coverindex":
                if(inventory.getUnmatchedOccurrences().size() == 0)
                    inventory.getUnmatchedOccurrences().add(new OBSERVED_IN(true));
                for(OBSERVED_IN obs : inventory.getUnmatchedOccurrences())
                    switch (inputFieldName.toLowerCase()) {
                        case "gpscode": obs.setGpsCode(inputValue); break;
                        case "comment": obs.setComment(inputValue); break;
                        case "privatecomment": obs.setPrivateComment(inputValue); break;
                        case "labeldata": obs.setLabelData(inputValue); break;
                        case "specificthreats": obs.setSpecificThreats(inputValue); break;
                        case "accession": obs.setAccession(inputValue); break;
                        case "coverindex": obs.setCoverIndex(inputValue); break;
                    }
                break;

            default:
                throw new IllegalArgumentException(Messages.getString("error.1", inputFieldName));
        }

    }
    */
}
