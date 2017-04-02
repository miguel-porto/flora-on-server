package pt.floraon.occurrences.fieldparsers;

import pt.floraon.driver.FloraOnException;
import pt.floraon.occurrences.Messages;
import pt.floraon.occurrences.entities.Inventory;
import pt.floraon.occurrences.entities.newOBSERVED_IN;

/**
 * Created by miguel on 14-02-2017.
 */
public class PlainTextParser implements FieldParser {
    @Override
    public void parseValue(String inputValue, String inputFieldName, Inventory occurrence) throws IllegalArgumentException, FloraOnException {
        if (inputValue == null || inputValue.trim().equals("")) return;

        switch (inputFieldName.toLowerCase()) {
            case "code":
                occurrence.setCode(inputValue);
                break;

            case "locality":
                occurrence.setLocality(inputValue);
                break;

            case "habitat":
                occurrence.setHabitat(inputValue);
                break;

            case "threats":
                occurrence.setThreats(inputValue);
                break;

            case "abundance":
                if(occurrence.getUnmatchedOccurrences().size() == 0)
                    occurrence.getUnmatchedOccurrences().add(new newOBSERVED_IN());
                //throw new FloraOnException("No taxa specified, cannot set abundance");
                for(newOBSERVED_IN obs : occurrence.getUnmatchedOccurrences())
                    obs.setAbundance(inputValue);
                break;

            case "gpscode":
                if(occurrence.getUnmatchedOccurrences().size() == 0)
                    occurrence.getUnmatchedOccurrences().add(new newOBSERVED_IN());
                for(newOBSERVED_IN obs : occurrence.getUnmatchedOccurrences())
                    obs.setGpsCode(inputValue);
                break;

            case "comment":
                if(occurrence.getUnmatchedOccurrences().size() == 0)
                    occurrence.getUnmatchedOccurrences().add(new newOBSERVED_IN());
//                if(occurrence.getUnmatchedOccurrences().size() == 0) throw new FloraOnException("No taxa specified, cannot set occurrence comment");
                for(newOBSERVED_IN obs : occurrence.getUnmatchedOccurrences())
                    obs.setComment(inputValue);
                break;

            default:
                throw new IllegalArgumentException(Messages.getString("error.1", inputFieldName));
        }
    }
}
