package pt.floraon.occurrences;

import pt.floraon.driver.parsers.CSVParser;
import pt.floraon.driver.parsers.FieldParser;
import pt.floraon.driver.FloraOnException;
import pt.floraon.driver.interfaces.IFloraOn;
import pt.floraon.driver.utils.StringUtils;
import pt.floraon.occurrences.entities.Inventory;
import pt.floraon.occurrences.fieldparsers.*;

import java.util.*;

/**
 * Created by miguel on 26-03-2017.
 */
public class OccurrenceParser implements CSVParser {
    private IFloraOn driver;
    /**
     * Holds the aliases mappings
     */
    private Map<String, FieldParser> fieldMappings = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    private Map<String, FieldParser> fieldMappingsSecondRound = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    /**
     * Holds a map os user names to DB IDs. This will be updated as needed.
     */
    Map<String, String> userMap = new HashMap<>();

    public OccurrenceParser(IFloraOn driver) {
        this.driver = driver;
        fieldMappings.put("latitude", new LatitudeLongitudeParser());
        fieldMappings.put("longitude", new LatitudeLongitudeParser());
        fieldMappings.put("coordinates", new LatitudeLongitudeParser());
        fieldMappings.put("wkt_geom", new LatitudeLongitudeParser());
        fieldMappings.put("x", new UTMCoordinateParser());
        fieldMappings.put("y", new UTMCoordinateParser());
        fieldMappings.put("mgrs", new UTMCoordinateParser());
        fieldMappings.put("elevation", new IntegerParser());
        fieldMappings.put("taxa", new TaxaParser());
        fieldMappings.put("year", new IntegerParser());
        fieldMappings.put("month", new IntegerParser());
        fieldMappings.put("date", new DateParser());
        fieldMappings.put("day", new IntegerParser());
        fieldMappings.put("precision", new LocalityParser());
        fieldMappings.put("locality", new LocalityParser());
        fieldMappings.put("municipality", new LocalityParser());
        fieldMappings.put("province", new LocalityParser());
        fieldMappings.put("county", new LocalityParser());
        fieldMappings.put("code", new PlainTextParser());
        fieldMappings.put("gpscode", new PlainTextParser());
        fieldMappings.put("verblocality", new LocalityParser());

        fieldMappings.put("habitat", new PlainTextParser());
        fieldMappings.put("threats", new PlainTextParser());
        fieldMappings.put("ano", new AliasFieldParser("year", fieldMappings));
        fieldMappings.put("código", new AliasFieldParser("code", fieldMappings));
        fieldMappings.put("inventário", new AliasFieldParser("code", fieldMappings));
        fieldMappings.put("data", new AliasFieldParser("date", fieldMappings));
        fieldMappings.put("time", new AliasFieldParser("date", fieldMappings));
        fieldMappings.put("gps", new AliasFieldParser("gpscode", fieldMappings));
        fieldMappings.put("gps code", new AliasFieldParser("gpscode", fieldMappings));
        fieldMappings.put("name", new AliasFieldParser("gpscode", fieldMappings));
        fieldMappings.put("z", new AliasFieldParser("elevation", fieldMappings));
        fieldMappings.put("região", new AliasFieldParser("province", fieldMappings));
        fieldMappings.put("concelho", new AliasFieldParser("municipality", fieldMappings));
        fieldMappings.put("altitude", new AliasFieldParser("elevation", fieldMappings));
        fieldMappings.put("observers", new UserListParser(userMap, driver, false));
        fieldMappings.put("collectors", new UserListParser(userMap, driver, false));
        fieldMappings.put("determiners", new UserListParser(userMap, driver, false));
        fieldMappings.put("inventoryid", new PlainTextParser());

        fieldMappingsSecondRound.put("abundance", new NumericIntervalParser());
        fieldMappingsSecondRound.put("typeofestimate", new EnumParser());
        fieldMappingsSecondRound.put("comment", new PlainTextParser());
        fieldMappingsSecondRound.put("privateComment", new PlainTextParser());
        fieldMappingsSecondRound.put("phenostate", new EnumParser());
        fieldMappingsSecondRound.put("confidence", new EnumParser());
        fieldMappingsSecondRound.put("presencestatus", new EnumParser());
        fieldMappingsSecondRound.put("hasphoto", new EnumParser());
        fieldMappingsSecondRound.put("hasspecimen", new IntegerParser());
        fieldMappingsSecondRound.put("occurrenceuuid", new UUIDParser());
        fieldMappingsSecondRound.put("observationlatitude", new LatitudeLongitudeParser());
        fieldMappingsSecondRound.put("observationlongitude", new LatitudeLongitudeParser());
        fieldMappingsSecondRound.put("observationcoordinates", new LatitudeLongitudeParser());
        fieldMappingsSecondRound.put("labeldata", new PlainTextParser());
        fieldMappingsSecondRound.put("specificthreats", new PlainTextParser());
        fieldMappingsSecondRound.put("accession", new PlainTextParser());
        fieldMappingsSecondRound.put("codHerbario", new AliasFieldParser("accession", fieldMappingsSecondRound));
        fieldMappingsSecondRound.put("coverIndex", new PlainTextParser());
        fieldMappingsSecondRound.put("excludeReason", new AliasFieldParser("presencestatus", fieldMappingsSecondRound));
        fieldMappingsSecondRound.put("privatenote", new AliasFieldParser("privateComment", fieldMappingsSecondRound));
    }

    @Override
    public void registerParser(String fieldName, FieldParser parser) {
        fieldMappings.put(fieldName, parser);
    }

    public Map<String, String> getUserMap() {
        return this.userMap;
    }

    /**
     * Checks if all passed names are recognized.
     * @param names
     * @throws FloraOnException If any name is not recognized.
     */
    public void checkFieldNames(Set<String> names) throws FloraOnException {
        List<String> errors = new ArrayList<>();

        for(String name : names) {
            name = name.replaceAll("\t", "");
            if(name.trim().length() == 0) continue;
            if (!fieldMappings.containsKey(name.toLowerCase()) && !fieldMappingsSecondRound.containsKey(name.toLowerCase()))
                errors.add(Messages.getString("error.1", name));
        }
        if(errors.size() > 0)
            throw new FloraOnException(StringUtils.implode("; ", errors.toArray(new String[errors.size()])));
    }

    @Override
    public void parseFields(Map<String, String> keyValues, Object bean) throws FloraOnException {
        List<String> errors = new ArrayList<>();
        Inventory inv = (Inventory) bean;
//        checkFieldNames(keyValues.keySet());
        // first round
        Set<String> intersection = new HashSet<>(keyValues.keySet());
        intersection.retainAll(fieldMappings.keySet());
        for(String key : intersection) {
            key = key.replaceAll("\t", "");
            if (!fieldMappings.containsKey(key.toLowerCase())) continue;
            try {
                String tmp = keyValues.get(key);
                if(tmp != null) tmp = tmp.trim();
//                if(tmp != null) System.out.println("Col: "+key.toLowerCase()+" value: "+Hex.encodeHexString(tmp.getBytes()));
                fieldMappings.get(key.toLowerCase()).parseValue(tmp, key.toLowerCase(), inv);
            } catch(IllegalArgumentException | FloraOnException e) {
                errors.add(e.getMessage());
            }
        }

        // second round
        intersection = new HashSet<>(keyValues.keySet());
        intersection.retainAll(fieldMappingsSecondRound.keySet());
        for(String key : intersection) {
            key = key.replaceAll("\t", "");
            if (!fieldMappingsSecondRound.containsKey(key.toLowerCase())) continue;
            try {
                String tmp = keyValues.get(key);
                if(tmp != null) tmp = tmp.trim();
//                if(tmp != null) System.out.println("Col: "+key.toLowerCase()+" value: "+Hex.encodeHexString(tmp.getBytes()));
                fieldMappingsSecondRound.get(key.toLowerCase()).parseValue(tmp, key.toLowerCase(), inv);
            } catch(IllegalArgumentException | FloraOnException e) {
                errors.add(e.getMessage());
            }
        }
        if(errors.size() > 0)
            throw new FloraOnException(StringUtils.implode("; ", errors.toArray(new String[errors.size()])));
    }
}