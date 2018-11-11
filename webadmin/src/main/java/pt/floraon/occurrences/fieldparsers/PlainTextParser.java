package pt.floraon.occurrences.fieldparsers;

import pt.floraon.driver.parsers.FieldParser;
import pt.floraon.driver.FloraOnException;
import pt.floraon.occurrences.Messages;
import pt.floraon.occurrences.entities.Inventory;
import pt.floraon.occurrences.entities.OBSERVED_IN;

/**
 * Created by miguel on 14-02-2017.
 */
public class PlainTextParser implements FieldParser {
    @Override
    public void parseValue(String inputValue, String inputFieldName, Object bean) throws IllegalArgumentException, FloraOnException {
//        if (inputValue == null || inputValue.trim().equals("")) return;
        if (inputValue == null) return;
        Inventory inventory = (Inventory) bean;

//        if(inputValue.trim().equals("")) inputValue = null;

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

            case "inventoryid": // TODO what is this for?
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
}
