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
        Inventory occurrence = (Inventory) bean;

//        if(inputValue.trim().equals("")) inputValue = null;

        switch (inputFieldName.toLowerCase()) {
            case "code":
                occurrence.setCode(inputValue);
                break;

            case "habitat":
                occurrence.setHabitat(inputValue);
                break;

            case "threats":
                occurrence.setThreats(inputValue);
                break;

            case "inventoryid":
                occurrence.setID(inputValue);
                break;

            case "gpscode":
            case "comment":
            case "privatenote":
            case "labeldata":
            case "specificthreats":
            case "accession":
                if(occurrence.getUnmatchedOccurrences().size() == 0)
                    occurrence.getUnmatchedOccurrences().add(new OBSERVED_IN(true));
                for(OBSERVED_IN obs : occurrence.getUnmatchedOccurrences())
                    switch (inputFieldName.toLowerCase()) {
                        case "gpscode": obs.setGpsCode(inputValue); break;
                        case "comment": obs.setComment(inputValue); break;
                        case "privatenote": obs.setPrivateComment(inputValue); break;
                        case "labeldata": obs.setLabelData(inputValue); break;
                        case "specificthreats": obs.setSpecificThreats(inputValue); break;
                        case "accession": obs.setAccession(inputValue); break;
                    }
                break;

            default:
                throw new IllegalArgumentException(Messages.getString("error.1", inputFieldName));
        }
    }
}
