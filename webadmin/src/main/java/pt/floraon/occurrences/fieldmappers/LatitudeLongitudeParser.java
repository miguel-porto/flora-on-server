package pt.floraon.occurrences.fieldmappers;

import pt.floraon.occurrences.entities.newOccurrence;

/**
 * A converter for latitude and longitude coordinates. Handles the following formats:
 * - decimal degrees
 * - DDºmm'ss''
 * Created by miguel on 08-02-2017.
 */
public class LatitudeLongitudeParser implements FieldParser {
    @Override
    public void parseValue(String inputValue, String inputFieldName, newOccurrence occurrence) throws IllegalArgumentException {

        System.out.println("parsing "+inputFieldName+": "+inputValue);
        switch(inputFieldName) {
            case "latitude":
                occurrence.getSpeciesList().setLatitude(Float.parseFloat(inputValue));
                break;

            case "longitude":
                occurrence.getSpeciesList().setLongitude(Float.parseFloat(inputValue));
                break;

            default:
                throw new IllegalArgumentException("Field " + inputFieldName + " not recognized.");
        }
    }
}
