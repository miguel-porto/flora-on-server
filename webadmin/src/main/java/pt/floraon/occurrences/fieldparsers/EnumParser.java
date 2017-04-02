package pt.floraon.occurrences.fieldparsers;

import pt.floraon.driver.FloraOnException;
import pt.floraon.occurrences.Messages;
import pt.floraon.occurrences.entities.Inventory;
import pt.floraon.occurrences.entities.newOBSERVED_IN;
import pt.floraon.redlistdata.RedListEnums;

/**
 * Created by miguel on 30-03-2017.
 */
public class EnumParser implements FieldParser {
    @Override
    public void parseValue(String inputValue, String inputFieldName, Inventory occurrence) throws IllegalArgumentException, FloraOnException {
        if (inputValue == null || inputValue.trim().equals("")) return;

        switch (inputFieldName.toLowerCase()) {
            case "typeofestimate":
                if(occurrence.getUnmatchedOccurrences().size() == 0)
                    occurrence.getUnmatchedOccurrences().add(new newOBSERVED_IN());

                RedListEnums.TypeOfPopulationEstimate value;
                switch(inputValue.toLowerCase()) {
                    case "e":   // quantitative estimation
                    case "estimativa":
                    case "estimate":
                        value = RedListEnums.TypeOfPopulationEstimate.APPROXIMATE_COUNT;
                        break;

                    case "c":   // quantitative estimation
                    case "contagem":
                    case "count":
                        value = RedListEnums.TypeOfPopulationEstimate.EXACT_COUNT;
                        break;

                    case "r":   // quantitative estimation
                    case "rough":
                        value = RedListEnums.TypeOfPopulationEstimate.ROUGH_ESTIMATE;
                        break;

                    default:
                        try {
                            value = RedListEnums.TypeOfPopulationEstimate.valueOf(inputValue);
                        } catch(IllegalArgumentException e) {
                            throw new IllegalArgumentException(inputValue + " not understood, use one of 'e', 'c' or 'r'");
                        }
                }

                for(newOBSERVED_IN obs : occurrence.getUnmatchedOccurrences())
                    obs.setTypeOfEstimate(value);
                break;

            default:
                throw new IllegalArgumentException(Messages.getString("error.1", inputFieldName));
        }

    }
}
