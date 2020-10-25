package pt.floraon.occurrences.fields.parsers;

import pt.floraon.driver.parsers.FieldParser;
import pt.floraon.driver.FloraOnException;
import pt.floraon.occurrences.Messages;
import pt.floraon.occurrences.entities.Inventory;
import pt.floraon.occurrences.entities.OBSERVED_IN;

import java.util.UUID;

/**
 * Created by miguel on 07-04-2017.
 */
public class UUIDParser implements FieldParser {
    @Override
    public void parseValue(String inputValue, String inputFieldName, Object bean) throws IllegalArgumentException, FloraOnException {
        if (inputValue == null || inputValue.trim().equals("")) return;
        UUID uuid = UUID.fromString(inputValue);
        Inventory occurrence = (Inventory) bean;

        switch (inputFieldName.toLowerCase()) {
            case "occurrenceuuid":
            case "uuid":
                if(occurrence.getUnmatchedOccurrences().size() == 0)
                    occurrence.getUnmatchedOccurrences().add(new OBSERVED_IN(true));

                occurrence.getUnmatchedOccurrences().get(0).setUuid(uuid);
                for (int i = 1; i < occurrence.getUnmatchedOccurrences().size(); i++)
                    occurrence.getUnmatchedOccurrences().get(i).setUuid(uuid);
//                    occurrence.getUnmatchedOccurrences().get(i).setUuid(UUID.randomUUID());

                break;

            default:
                throw new IllegalArgumentException(Messages.getString("error.1", inputFieldName));
        }
    }
}
