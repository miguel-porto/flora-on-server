package pt.floraon.geocoding;

import pt.floraon.driver.parsers.CSVParser;
import pt.floraon.driver.FloraOnException;
import pt.floraon.geocoding.entities.Toponym;
import pt.floraon.driver.parsers.FieldParser;
import pt.floraon.occurrences.fields.parsers.AliasFieldParser;
import pt.floraon.occurrences.fields.parsers.LatitudeLongitudeParser;
import pt.floraon.occurrences.fields.parsers.LocalityParser;

import java.util.Map;
import java.util.TreeMap;

/**
 * Created by miguel on 16-04-2017.
 */
public class ToponomyParser implements CSVParser {
    private Map<String, FieldParser> fieldMappings = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    public ToponomyParser() {
        fieldMappings.put("latitude", new LatitudeLongitudeParser());
        fieldMappings.put("longitude", new LatitudeLongitudeParser());
        fieldMappings.put("locality", new LocalityParser());
        fieldMappings.put("municipality", new LocalityParser());
        fieldMappings.put("province", new LocalityParser());
        fieldMappings.put("county", new LocalityParser());
        fieldMappings.put("type", new LocalityParser());
        fieldMappings.put("name", new AliasFieldParser("locality", fieldMappings));
        fieldMappings.put("distrito", new AliasFieldParser("province", fieldMappings));
        fieldMappings.put("concelho", new AliasFieldParser("municipality", fieldMappings));
        fieldMappings.put("município", new AliasFieldParser("municipality", fieldMappings));
        fieldMappings.put("freguesia", new AliasFieldParser("county", fieldMappings));
    }

    @Override
    public void registerParser(String fieldName, FieldParser parser) {
        fieldMappings.put(fieldName, parser);
    }

    @Override
    public void parseFields(Map<String, String> keyValues, Object bean) throws FloraOnException {
        Toponym topo = (Toponym) bean;
        for(String key : keyValues.keySet()) {
            if(fieldMappings.get(key) == null) continue;
            try {
                fieldMappings.get(key).parseValue(keyValues.get(key), key, topo);
            } catch(IllegalArgumentException e) {
                throw new FloraOnException(e.getMessage());
            }
        }

    }
}
