package pt.floraon.occurrences.fieldparsers;

import pt.floraon.driver.Constants;
import pt.floraon.driver.parsers.FieldParser;
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
    public void parseValue(String inputValue, String inputFieldName, Object bean) throws IllegalArgumentException, FloraOnException {
        if (inputValue == null) return;
        Inventory occurrence = (Inventory) bean;

        switch (inputFieldName.toLowerCase()) {
            case "typeofestimate":
                if(occurrence.getUnmatchedOccurrences().size() == 0)
                    occurrence.getUnmatchedOccurrences().add(new newOBSERVED_IN(true));

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

                    case "":    // TODO: how to clear field?
                        value = null;
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

            case "phenostate":
                if(occurrence.getUnmatchedOccurrences().size() == 0)
                    occurrence.getUnmatchedOccurrences().add(new newOBSERVED_IN(true));

                Constants.PhenologicalStates value1;
                switch(inputValue.toLowerCase()) {
                    case "f":
                    case "flower":
                    case "flor":
                        value1 = Constants.PhenologicalStates.FLOWER;
                        break;

                    case "d":
                    case "dispersion":
                    case "dispersão":
                        value1 = Constants.PhenologicalStates.DISPERSION;
                        break;

                    case "v":
                    case "vegetative":
                        value1 = Constants.PhenologicalStates.VEGETATIVE;
                        break;

                    case "":    // TODO: how to clear field?
                        value1 = null;
                        break;

                    default:
                        try {
                            value1 = Constants.PhenologicalStates.valueOf(inputValue);
                        } catch(IllegalArgumentException e) {
                            throw new IllegalArgumentException(inputValue + " not understood, use one of 'f', 'd' or 'v'");
                        }
                }

                for(newOBSERVED_IN obs : occurrence.getUnmatchedOccurrences())
                    obs.setPhenoState(value1);
                break;

            default:
                throw new IllegalArgumentException(Messages.getString("error.1", inputFieldName));
        }

    }
}
