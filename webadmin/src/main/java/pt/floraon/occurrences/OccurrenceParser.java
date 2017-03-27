package pt.floraon.occurrences;

import pt.floraon.driver.FloraOnException;
import pt.floraon.driver.IFloraOn;
import pt.floraon.occurrences.entities.Inventory;
import pt.floraon.occurrences.fieldparsers.*;

import javax.xml.parsers.FactoryConfigurationError;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Created by miguel on 26-03-2017.
 */
public class OccurrenceParser {
    private IFloraOn driver;
    /**
     * Holds the aliases mappings
     */
    private Map<String, FieldParser> fieldMappings = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

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
        fieldMappings.put("day", new IntegerParser());
        fieldMappings.put("code", new PlainTextParser());
        fieldMappings.put("ano", new AliasFieldParser("year", fieldMappings));
        fieldMappings.put("código", new AliasFieldParser("code", fieldMappings));
        fieldMappings.put("observers", new UserListParser(userMap, driver));
        fieldMappings.put("collectors", new UserListParser(userMap, driver));
        fieldMappings.put("determiners", new UserListParser(userMap, driver));
    }

    /**
     * Checks if all passed names are recognized.
     * @param names
     * @throws FloraOnException If any name is not recognized.
     */
    public void checkFieldNames(Set<String> names) throws FloraOnException {
        for(String name : names)
            if(!fieldMappings.containsKey(name)) throw new FloraOnException(Messages.getString("error.1", name));
    }

    public void parseField(String value, String field, Inventory inventory) throws FloraOnException {
        fieldMappings.get(field).parseValue(value, field, inventory);
    }
}
