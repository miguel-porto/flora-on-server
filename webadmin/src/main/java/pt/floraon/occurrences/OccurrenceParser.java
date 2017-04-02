package pt.floraon.occurrences;

import pt.floraon.driver.FloraOnException;
import pt.floraon.driver.IFloraOn;
import pt.floraon.occurrences.entities.Inventory;
import pt.floraon.occurrences.fieldparsers.*;

import javax.xml.parsers.FactoryConfigurationError;
import java.util.*;

/**
 * Created by miguel on 26-03-2017.
 */
public class OccurrenceParser {
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
        fieldMappings.put("taxa", new TaxaParser());
        fieldMappings.put("year", new IntegerParser());
        fieldMappings.put("month", new IntegerParser());
        fieldMappings.put("date", new DateParser());
        fieldMappings.put("day", new IntegerParser());
        fieldMappings.put("code", new PlainTextParser());
        fieldMappings.put("gpsCode", new PlainTextParser());
        fieldMappings.put("locality", new PlainTextParser());
        fieldMappings.put("habitat", new PlainTextParser());
        fieldMappings.put("threats", new PlainTextParser());
        fieldMappings.put("ano", new AliasFieldParser("year", fieldMappings));
        fieldMappings.put("código", new AliasFieldParser("code", fieldMappings));
        fieldMappings.put("observers", new UserListParser(userMap, driver));
        fieldMappings.put("collectors", new UserListParser(userMap, driver));
        fieldMappings.put("determiners", new UserListParser(userMap, driver));

        fieldMappingsSecondRound.put("abundance", new PlainTextParser());
        fieldMappingsSecondRound.put("typeOfEstimate", new EnumParser());
        fieldMappingsSecondRound.put("comment", new PlainTextParser());
        fieldMappingsSecondRound.put("hasPhoto", new BooleanParser());
        fieldMappingsSecondRound.put("hasSpecimen", new BooleanParser());
    }

    /**
     * Checks if all passed names are recognized.
     * @param names
     * @throws FloraOnException If any name is not recognized.
     */
    public void checkFieldNames(Set<String> names) throws FloraOnException {
        for(String name : names) {
            if (!fieldMappings.containsKey(name) && !fieldMappingsSecondRound.containsKey(name))
                throw new FloraOnException(Messages.getString("error.1", name));
        }
    }

    public void parseFields(Map<String, String> keyValues, Inventory inv) throws FloraOnException {
        checkFieldNames(keyValues.keySet());
        // first round
        Set<String> intersection = new HashSet<>(keyValues.keySet());
        intersection.retainAll(fieldMappings.keySet());
        for(String key : intersection) {
            try {
                fieldMappings.get(key).parseValue(keyValues.get(key), key, inv);
            } catch(IllegalArgumentException e) {
                throw new FloraOnException(e.getMessage());
            }
        }

        // second round
        intersection = new HashSet<>(keyValues.keySet());
        intersection.retainAll(fieldMappingsSecondRound.keySet());
        for(String key : intersection) {
            try {
                fieldMappingsSecondRound.get(key).parseValue(keyValues.get(key), key, inv);
            } catch(IllegalArgumentException e) {
                throw new FloraOnException(e.getMessage());
            }
        }
    }
}
