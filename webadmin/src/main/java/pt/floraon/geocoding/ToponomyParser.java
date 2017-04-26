package pt.floraon.geocoding;

import pt.floraon.driver.parsers.CSVParser;
import pt.floraon.driver.FloraOnException;
import pt.floraon.geocoding.entities.Toponym;
import pt.floraon.driver.parsers.FieldParser;
import pt.floraon.occurrences.Messages;
import pt.floraon.occurrences.fieldparsers.LatitudeLongitudeParser;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Created by miguel on 16-04-2017.
 */
public class ToponomyParser implements CSVParser {
    private Map<String, FieldParser> fieldMappings = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    public ToponomyParser() {
        fieldMappings.put("latitude", new LatitudeLongitudeParser());
    }

    @Override
    public void registerParser(String fieldName, FieldParser parser) {

    }

    @Override
    public void parseFields(Map<String, String> keyValues, Object bean) throws FloraOnException {
        Toponym topo = (Toponym) bean;
        for(String name : keyValues.keySet()) {
            if (!fieldMappings.containsKey(name))
                throw new FloraOnException(Messages.getString("error.1", name));
        }

        for(String key : keyValues.keySet()) {
            try {
                fieldMappings.get(key).parseValue(keyValues.get(key), key, topo);
            } catch(IllegalArgumentException e) {
                throw new FloraOnException(e.getMessage());
            }
        }

    }
}
