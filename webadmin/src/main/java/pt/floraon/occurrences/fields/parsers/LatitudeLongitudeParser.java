package pt.floraon.occurrences.fields.parsers;

import pt.floraon.driver.Constants;
import pt.floraon.driver.parsers.FieldParser;
import pt.floraon.driver.GeoBean;
import pt.floraon.occurrences.Messages;
import pt.floraon.occurrences.entities.Inventory;
import pt.floraon.occurrences.entities.OBSERVED_IN;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A converter for latitude and longitude coordinates. Handles the following formats:
 * - decimal degrees
 * - DDºmm'ss''
 * Created by miguel on 08-02-2017.
 */
public class LatitudeLongitudeParser implements FieldParser {
    private static final Pattern coordParse = Pattern.compile("^(?<lat>[0-9-]+(\\.[0-9]+)?)[\\s,;]+(?<lng>[0-9-]+(\\.[0-9]+)?)$");
    private static final Pattern DMSParseSingle = Pattern.compile("^ *(?<deg>[0-9-]+). *(?<min>[0-9]+). *(?<sec>[0-9.]+)..? *(?<let>[nsNSewoEWO]) *$");
    private static final Pattern DMSParse = Pattern.compile("^ *(?<latdeg>[0-9-]+). *(?<latmin>[0-9]+). *(?<latsec>[0-9.]+)..? *" +
            "(?<latlet>[nsNS])[ ,;]+(?<lngdeg>[0-9-]+). *(?<lngmin>[0-9]+). *(?<lngsec>[0-9.]+)..? *(?<lnglet>[ewoEWO]) *$");
    private static final Pattern wktParse = Pattern.compile("pointz? *\\( *(?<lng>[0-9-]+(\\.[0-9]+)?) (?<lat>[0-9-]+(\\.[0-9]+)?)( [0-9.-]+)? *\\)", Pattern.CASE_INSENSITIVE);

    static private Float[] getFromDMS(Matcher mat) {
        float latdeg = Float.parseFloat(mat.group("latdeg"));
        float latmin = Float.parseFloat(mat.group("latmin"));
        float latsec = Float.parseFloat(mat.group("latsec"));
        String latlet = mat.group("latlet");
//        System.out.println(latdeg+"|"+latmin+"|"+latsec+ " "+latlet);
        float lat = DMS2Dec(latdeg, latmin, latsec, latlet);

        float lngdeg = Float.parseFloat(mat.group("lngdeg"));
        float lngmin = Float.parseFloat(mat.group("lngmin"));
        float lngsec = Float.parseFloat(mat.group("lngsec"));
        String lnglet = mat.group("lnglet");
//        System.out.println(lngdeg+"|"+lngmin+"|"+lngsec+ " "+lnglet);

        float lng = DMS2Dec(lngdeg, lngmin, lngsec, lnglet);
        return new Float[]{lat, lng};
    }

    static private float DMS2Dec(float deg, float min, float sec, String letter) {
        float dec = deg + (min + sec / 60) / 60;
        if((letter.toUpperCase().equals("S") || letter.toUpperCase().equals("W") || letter.toUpperCase().equals("O")) && dec > 0) dec = -dec;
        return dec;
    }

    @Override
    public void parseValue(String inputValue, String inputFieldName, Object bean) throws IllegalArgumentException {
        GeoBean occurrence = (GeoBean) bean;
        Inventory inventory;
        if(inputValue == null) return;
// TODO parse other lat long formats
        Float v = null;
        Matcher mat = null;
        boolean isDMS = false;
        if(inputValue.trim().equals("") || inputValue.trim().equals("*")) {
            v = Constants.NODATA;
            mat = coordParse.matcher(String.format(Locale.ROOT,"%.14f %.14f", Constants.NODATA, Constants.NODATA));
            mat.find();
        } else {
            try {
                v = Float.parseFloat(inputValue);
            } catch (NumberFormatException e) {
                mat = coordParse.matcher(inputValue);
                if (!mat.find()) {
                    mat = wktParse.matcher(inputValue);
                    if (!mat.find()) {
                        mat = DMSParse.matcher(inputValue);
                        if(!mat.find()) {
                            mat = DMSParseSingle.matcher(inputValue);
                            if(!mat.find())
                                throw new NumberFormatException("Could not understand the coordinate \"" + inputValue + "\"");
                            float deg = Float.parseFloat(mat.group("deg"));
                            float min = Float.parseFloat(mat.group("min"));
                            float sec = Float.parseFloat(mat.group("sec"));
                            String let = mat.group("let");
                            v = DMS2Dec(deg, min, sec, let);
                        } else
                            isDMS = true;
                    }
                }
            }
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

            case "inventorycoordinates":
                if(mat == null) throw new IllegalArgumentException(Messages.getString("error.1", inputFieldName));
                inventory = (Inventory) occurrence;
                if(isDMS) {
                    Float[] ll = getFromDMS(mat);
                    inventory.setLatitude(ll[0]);
                    inventory.setLongitude(ll[1]);
                } else {
                    inventory.setLatitude(Float.parseFloat(mat.group("lat")));
                    inventory.setLongitude(Float.parseFloat(mat.group("lng")));
                }
                break;

            case "coordinates":
            case "wkt_geom":
                if(mat == null) throw new IllegalArgumentException(Messages.getString("error.1", inputFieldName));
                if(isDMS) {
                    Float[] ll = getFromDMS(mat);
                    occurrence.setLatitude(ll[0]);
                    occurrence.setLongitude(ll[1]);
                } else {
                    occurrence.setLatitude(Float.parseFloat(mat.group("lat")));
                    occurrence.setLongitude(Float.parseFloat(mat.group("lng")));
                }
                break;

            case "observationlatitude":
                if(v == null) throw new IllegalArgumentException(Messages.getString("error.1", inputFieldName));
                inventory = (Inventory) occurrence;
                if(inventory.getUnmatchedOccurrences().size() == 0)
                    inventory.getUnmatchedOccurrences().add(new OBSERVED_IN(true));
                for(OBSERVED_IN obs : inventory.getUnmatchedOccurrences())
                    obs.setObservationLatitude(v);
                break;

            case "observationlongitude":
                if(v == null) throw new IllegalArgumentException(Messages.getString("error.1", inputFieldName));
                inventory = (Inventory) occurrence;
                if(inventory.getUnmatchedOccurrences().size() == 0)
                    inventory.getUnmatchedOccurrences().add(new OBSERVED_IN(true));
                for(OBSERVED_IN obs : inventory.getUnmatchedOccurrences())
                    obs.setObservationLongitude(v);
                break;

            case "observationcoordinates":
                if(mat == null) throw new IllegalArgumentException(Messages.getString("error.1", inputFieldName));
                inventory = (Inventory) occurrence;
                if(inventory.getUnmatchedOccurrences().size() == 0)
                    inventory.getUnmatchedOccurrences().add(new OBSERVED_IN(true));
                for(OBSERVED_IN obs : inventory.getUnmatchedOccurrences()) {
                    if(isDMS) {
                        Float[] ll = getFromDMS(mat);
                        obs.setObservationLatitude(ll[0]);
                        obs.setObservationLongitude(ll[1]);
                    } else {
                        obs.setObservationLatitude(Float.parseFloat(mat.group("lat")));
                        obs.setObservationLongitude(Float.parseFloat(mat.group("lng")));
                    }
                }
                break;

            default:
                throw new IllegalArgumentException(Messages.getString("error.1", inputFieldName));
        }
    }
}
