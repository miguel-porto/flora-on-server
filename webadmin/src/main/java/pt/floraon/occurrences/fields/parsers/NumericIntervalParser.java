package pt.floraon.occurrences.fields.parsers;

import pt.floraon.driver.FloraOnException;
import pt.floraon.occurrences.Abundance;
import pt.floraon.driver.parsers.FieldParser;
import pt.floraon.occurrences.Messages;
import pt.floraon.occurrences.entities.Inventory;
import pt.floraon.occurrences.entities.OBSERVED_IN;

public class NumericIntervalParser implements FieldParser {
    @Override
    public void parseValue(String inputValue, String inputFieldName, Object bean) throws IllegalArgumentException, FloraOnException {
        if (inputValue == null) return;
        Inventory occurrence = (Inventory) bean;

        switch (inputFieldName.toLowerCase()) {
            case "abundance":
                if(occurrence.getUnmatchedOccurrences().size() == 0)
                    occurrence.getUnmatchedOccurrences().add(new OBSERVED_IN(true));
                for(OBSERVED_IN obs : occurrence.getUnmatchedOccurrences())
                    switch (inputFieldName.toLowerCase()) {
                        case "abundance": obs.setAbundance(new Abundance(inputValue)); break;
                    }
                break;

            default:
                throw new IllegalArgumentException(Messages.getString("error.1", inputFieldName));
        }
    }
}
