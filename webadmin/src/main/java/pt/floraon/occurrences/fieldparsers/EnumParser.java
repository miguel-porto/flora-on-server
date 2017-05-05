package pt.floraon.occurrences.fieldparsers;

import pt.floraon.driver.Constants;
import pt.floraon.driver.parsers.FieldParser;
import pt.floraon.driver.FloraOnException;
import pt.floraon.driver.utils.StringUtils;
import pt.floraon.occurrences.Messages;
import pt.floraon.occurrences.OccurrenceConstants;
import pt.floraon.occurrences.entities.Inventory;
import pt.floraon.occurrences.entities.newOBSERVED_IN;
import pt.floraon.redlistdata.RedListEnums;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by miguel on 30-03-2017.
 */
public class EnumParser implements FieldParser {
    static Map<String, Constants.PhenologicalStates> phenologicalStatesMap = new HashMap<>();
    static Map<String, OccurrenceConstants.ConfidenceInIdentifiction> confidenceMap = new HashMap<>();
    static {
        phenologicalStatesMap.put("f", Constants.PhenologicalStates.FLOWER);
        phenologicalStatesMap.put("flower", Constants.PhenologicalStates.FLOWER);
        phenologicalStatesMap.put("flor", Constants.PhenologicalStates.FLOWER);
        phenologicalStatesMap.put("d", Constants.PhenologicalStates.DISPERSION);
        phenologicalStatesMap.put("dispersion", Constants.PhenologicalStates.DISPERSION);
        phenologicalStatesMap.put("dispersão", Constants.PhenologicalStates.DISPERSION);
        phenologicalStatesMap.put("fd", Constants.PhenologicalStates.FLOWER_DISPERSION);
        phenologicalStatesMap.put("df", Constants.PhenologicalStates.FLOWER_DISPERSION);
        phenologicalStatesMap.put("v", Constants.PhenologicalStates.VEGETATIVE);
        phenologicalStatesMap.put("vegetative", Constants.PhenologicalStates.VEGETATIVE);
        phenologicalStatesMap.put("r", Constants.PhenologicalStates.RESTING);
        phenologicalStatesMap.put("rest", Constants.PhenologicalStates.RESTING);
        phenologicalStatesMap.put("dormancy", Constants.PhenologicalStates.RESTING);
        phenologicalStatesMap.put("c", Constants.PhenologicalStates.FRUIT);
        phenologicalStatesMap.put("fruto", Constants.PhenologicalStates.FRUIT);
        phenologicalStatesMap.put("fruit", Constants.PhenologicalStates.FRUIT);

        confidenceMap.put("c", OccurrenceConstants.ConfidenceInIdentifiction.CERTAIN);
        confidenceMap.put("a", OccurrenceConstants.ConfidenceInIdentifiction.ALMOST_SURE);
        confidenceMap.put("d", OccurrenceConstants.ConfidenceInIdentifiction.DOUBTFUL);
    }

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

                    case "g":   // quantitative estimation
                    case "grosseira":
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
                if(phenologicalStatesMap.containsKey(inputValue.toLowerCase()))
                    value1 = phenologicalStatesMap.get(inputValue.toLowerCase());
                else {
                    try {
                        value1 = Constants.PhenologicalStates.valueOf(inputValue);
                    } catch(IllegalArgumentException e) {
                        throw new IllegalArgumentException(inputValue + " not understood, possible options: "
                                + StringUtils.implode(", ", phenologicalStatesMap.keySet().toArray(new String[0])));
                    }
                }

                for(newOBSERVED_IN obs : occurrence.getUnmatchedOccurrences())
                    obs.setPhenoState(value1);
                break;

            case "confidence":
                if(occurrence.getUnmatchedOccurrences().size() == 0)
                    occurrence.getUnmatchedOccurrences().add(new newOBSERVED_IN(true));

                OccurrenceConstants.ConfidenceInIdentifiction value2;
                if(confidenceMap.containsKey(inputValue.toLowerCase()))
                    value2 = confidenceMap.get(inputValue.toLowerCase());
                else {
                    try {
                        value2 = OccurrenceConstants.ConfidenceInIdentifiction.valueOf(inputValue);
                    } catch(IllegalArgumentException e) {
                        throw new IllegalArgumentException(inputValue + " not understood, possible options: "
                                + StringUtils.implode(", ", confidenceMap.keySet().toArray(new String[0])));
                    }
                }

                for(newOBSERVED_IN obs : occurrence.getUnmatchedOccurrences())
                    obs.setConfidence(value2);
                break;

            default:
                throw new IllegalArgumentException(Messages.getString("error.1", inputFieldName));
        }

    }
}
