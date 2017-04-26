package pt.floraon.occurrences;

import pt.floraon.driver.parsers.CSVParser;
import pt.floraon.driver.parsers.FieldParser;
import pt.floraon.driver.FloraOnException;
import pt.floraon.driver.IFloraOn;
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
        fieldMappings.put("x", new UTMCoordinateParser());
        fieldMappings.put("y", new UTMCoordinateParser());
        fieldMappings.put("elevation", new IntegerParser());
        fieldMappings.put("taxa", new TaxaParser());
        fieldMappings.put("year", new IntegerParser());
        fieldMappings.put("month", new IntegerParser());
        fieldMappings.put("date", new DateParser());
        fieldMappings.put("day", new IntegerParser());
        fieldMappings.put("code", new PlainTextParser());
        fieldMappings.put("gpscode", new PlainTextParser());
        fieldMappings.put("locality", new PlainTextParser());
        fieldMappings.put("verblocality", new PlainTextParser());

        fieldMappings.put("habitat", new PlainTextParser());
        fieldMappings.put("threats", new PlainTextParser());
        fieldMappings.put("ano", new AliasFieldParser("year", fieldMappings));
        fieldMappings.put("código", new AliasFieldParser("code", fieldMappings));
        fieldMappings.put("data", new AliasFieldParser("date", fieldMappings));
        fieldMappings.put("gps", new AliasFieldParser("gpscode", fieldMappings));
        fieldMappings.put("z", new AliasFieldParser("elevation", fieldMappings));
        fieldMappings.put("altitude", new AliasFieldParser("elevation", fieldMappings));
        fieldMappings.put("observers", new UserListParser(userMap, driver, false));
        fieldMappings.put("collectors", new UserListParser(userMap, driver, false));
        fieldMappings.put("determiners", new UserListParser(userMap, driver, false));
        fieldMappings.put("inventoryid", new PlainTextParser());

        fieldMappingsSecondRound.put("abundance", new PlainTextParser());
        fieldMappingsSecondRound.put("typeofestimate", new EnumParser());
        fieldMappingsSecondRound.put("comment", new PlainTextParser());
        fieldMappingsSecondRound.put("phenostate", new EnumParser());
        fieldMappingsSecondRound.put("hasphoto", new BooleanParser());
        fieldMappingsSecondRound.put("hasspecimen", new BooleanParser());
        fieldMappingsSecondRound.put("occurrenceuuid", new UUIDParser());
        fieldMappingsSecondRound.put("observationlatitude", new LatitudeLongitudeParser());
        fieldMappingsSecondRound.put("observationlongitude", new LatitudeLongitudeParser());
        fieldMappingsSecondRound.put("observationcoordinates", new LatitudeLongitudeParser());
        fieldMappingsSecondRound.put("labeldata", new PlainTextParser());

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
        for(String name : names) {
            if (!fieldMappings.containsKey(name.toLowerCase()) && !fieldMappingsSecondRound.containsKey(name.toLowerCase()))
                throw new FloraOnException(Messages.getString("error.1", name));
        }
    }

    @Override
    public void parseFields(Map<String, String> keyValues, Object bean) throws FloraOnException {
        Inventory inv = (Inventory) bean;
        checkFieldNames(keyValues.keySet());
        // first round
        Set<String> intersection = new HashSet<>(keyValues.keySet());
        intersection.retainAll(fieldMappings.keySet());
        for(String key : intersection) {
            try {
                fieldMappings.get(key.toLowerCase()).parseValue(keyValues.get(key), key.toLowerCase(), inv);
            } catch(IllegalArgumentException e) {
                throw new FloraOnException(e.getMessage());
            }
        }

        // second round
        intersection = new HashSet<>(keyValues.keySet());
        intersection.retainAll(fieldMappingsSecondRound.keySet());
        for(String key : intersection) {
            try {
                fieldMappingsSecondRound.get(key.toLowerCase()).parseValue(keyValues.get(key), key.toLowerCase(), inv);
            } catch(IllegalArgumentException e) {
                throw new FloraOnException(e.getMessage());
            }
        }
    }
}
