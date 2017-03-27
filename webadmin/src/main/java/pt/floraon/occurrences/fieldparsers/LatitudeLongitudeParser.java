package pt.floraon.occurrences.fieldparsers;

import pt.floraon.occurrences.Messages;
import pt.floraon.occurrences.entities.Inventory;

/**
 * A converter for latitude and longitude coordinates. Handles the following formats:
 * - decimal degrees
 * - DDºmm'ss''
 * Created by miguel on 08-02-2017.
 */
public class LatitudeLongitudeParser implements FieldParser {
    @Override
    public void parseValue(String inputValue, String inputFieldName, Inventory occurrence) throws IllegalArgumentException {
        if(inputValue == null || inputValue.trim().equals("")) return;
// TODO parse other lat long formats
        float v;
        try {
            v = Float.parseFloat(inputValue);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(e);
        }

        switch(inputFieldName) {
            case "latitude":
                occurrence.setLatitude(v);
                break;

            case "longitude":
                occurrence.setLongitude(v);
                break;

            default:
                throw new IllegalArgumentException(Messages.getString("error.1", inputFieldName));
        }
    }
}
