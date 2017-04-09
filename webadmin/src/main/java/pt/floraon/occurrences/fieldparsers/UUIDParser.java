package pt.floraon.occurrences.fieldparsers;

import pt.floraon.driver.FloraOnException;
import pt.floraon.occurrences.Messages;
import pt.floraon.occurrences.entities.Inventory;
import pt.floraon.occurrences.entities.newOBSERVED_IN;

import java.util.UUID;

/**
 * Created by miguel on 07-04-2017.
 */
public class UUIDParser implements FieldParser {
    @Override
    public void parseValue(String inputValue, String inputFieldName, Inventory occurrence) throws IllegalArgumentException, FloraOnException {
        if (inputValue == null || inputValue.trim().equals("")) return;
        UUID uuid = UUID.fromString(inputValue);

        switch (inputFieldName.toLowerCase()) {
            case "occurrenceuuid":
                if(occurrence.getUnmatchedOccurrences().size() == 0)
                    occurrence.getUnmatchedOccurrences().add(new newOBSERVED_IN(true));

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
