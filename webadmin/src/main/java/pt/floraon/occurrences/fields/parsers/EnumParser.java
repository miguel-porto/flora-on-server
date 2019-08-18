package pt.floraon.occurrences.fields.parsers;

import pt.floraon.driver.Constants;
import pt.floraon.driver.parsers.FieldParser;
import pt.floraon.driver.FloraOnException;
import pt.floraon.occurrences.Messages;
import pt.floraon.occurrences.OccurrenceConstants;
import pt.floraon.occurrences.entities.Inventory;
import pt.floraon.occurrences.entities.OBSERVED_IN;
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
            case "hasphoto":
            case "phenostate":
            case "confidence":
            case "presencestatus":
            case "naturalization":
                if(occurrence.getUnmatchedOccurrences().size() == 0)
                    occurrence.getUnmatchedOccurrences().add(new OBSERVED_IN(true));

                for(OBSERVED_IN obs : occurrence.getUnmatchedOccurrences())
                    switch (inputFieldName.toLowerCase()) {
                        case "typeofestimate":
                            obs.setTypeOfEstimate(RedListEnums.TypeOfPopulationEstimate.getValueFromAcronym(inputValue.toLowerCase()));
                            break;

                        case "hasphoto":
                            obs.setHasPhoto(RedListEnums.HasPhoto.getValueFromAcronym(inputValue.toLowerCase()));
                            break;

                        case "phenostate":
                            obs.setPhenoState(Constants.PhenologicalStates.getValueFromAcronym(inputValue.toLowerCase()));
                            break;

                        case "confidence":
                            obs.setConfidence(OccurrenceConstants.ConfidenceInIdentifiction.getValueFromAcronym(inputValue.toLowerCase()));
                            break;

                        case "presencestatus":
                            obs.setPresenceStatus(OccurrenceConstants.PresenceStatus.getValueFromAcronym(inputValue.toLowerCase()));
                            break;

                        case "naturalization":
                            obs.setNaturalization(OccurrenceConstants.OccurrenceNaturalization.getValueFromAcronym(inputValue.toLowerCase()));
                            break;
                    }

                break;

            default:
                throw new IllegalArgumentException(Messages.getString("error.1", inputFieldName));
        }

    }
}
