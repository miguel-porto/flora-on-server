package pt.floraon.occurrences.fieldparsers;

import pt.floraon.driver.parsers.FieldParser;
import pt.floraon.driver.GeoBean;
import pt.floraon.occurrences.Messages;
import pt.floraon.occurrences.entities.Inventory;
import pt.floraon.occurrences.entities.newOBSERVED_IN;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A converter for latitude and longitude coordinates. Handles the following formats:
 * - decimal degrees
 * - DDºmm'ss''
 * Created by miguel on 08-02-2017.
 */
public class LatitudeLongitudeParser implements FieldParser {
    private static Pattern coordParse = Pattern.compile("^(?<lat>[0-9-]+.[0-9]+)[ ,;]+(?<lng>[0-9-]+.[0-9]+)$");

    @Override
    public void parseValue(String inputValue, String inputFieldName, Object bean) throws IllegalArgumentException {
        GeoBean occurrence = (GeoBean) bean;
        Inventory inv;
        if(inputValue == null || inputValue.trim().equals("")) return;
// TODO parse other lat long formats
        Float v = null;
        Matcher mat = null;
        try {
            v = Float.parseFloat(inputValue);
        } catch (NumberFormatException e) {
            mat = coordParse.matcher(inputValue);
            if(!mat.find())
                throw new IllegalArgumentException(e);
        }

        switch(inputFieldName.toLowerCase()) {
            case "latitude":
                if(v == null) throw new IllegalArgumentException(Messages.getString("error.1", inputFieldName));
                occurrence.setLatitude(v);
                break;

            case "longitude":
                if(v == null) throw new IllegalArgumentException(Messages.getString("error.1", inputFieldName));
                occurrence.setLongitude(v);
                break;

            case "coordinates":
                if(mat == null) throw new IllegalArgumentException(Messages.getString("error.1", inputFieldName));
                occurrence.setLatitude(Float.parseFloat(mat.group("lat")));
                occurrence.setLongitude(Float.parseFloat(mat.group("lng")));
                break;

            case "observationlatitude":
                if(v == null) throw new IllegalArgumentException(Messages.getString("error.1", inputFieldName));
                inv = (Inventory) occurrence;
                if(inv.getUnmatchedOccurrences().size() == 0)
                    inv.getUnmatchedOccurrences().add(new newOBSERVED_IN(true));
                for(newOBSERVED_IN obs : inv.getUnmatchedOccurrences())
                    obs.setObservationLatitude(v);
                break;

            case "observationlongitude":
                if(v == null) throw new IllegalArgumentException(Messages.getString("error.1", inputFieldName));
                inv = (Inventory) occurrence;
                if(inv.getUnmatchedOccurrences().size() == 0)
                    inv.getUnmatchedOccurrences().add(new newOBSERVED_IN(true));
                for(newOBSERVED_IN obs : inv.getUnmatchedOccurrences())
                    obs.setObservationLongitude(v);
                break;

            case "observationcoordinates":
                if(mat == null) throw new IllegalArgumentException(Messages.getString("error.1", inputFieldName));
                inv = (Inventory) occurrence;
                if(inv.getUnmatchedOccurrences().size() == 0)
                    inv.getUnmatchedOccurrences().add(new newOBSERVED_IN(true));
                for(newOBSERVED_IN obs : inv.getUnmatchedOccurrences()) {
                    obs.setObservationLatitude(Float.parseFloat(mat.group("lat")));
                    obs.setObservationLongitude(Float.parseFloat(mat.group("lng")));
                }
                break;

            default:
                throw new IllegalArgumentException(Messages.getString("error.1", inputFieldName));
        }
    }
}
