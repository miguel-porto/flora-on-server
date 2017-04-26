package pt.floraon.driver.parsers;

import pt.floraon.driver.FloraOnException;
import pt.floraon.driver.parsers.FieldParser;

import java.util.Map;

/**
 * Created by miguel on 16-04-2017.
 */
public interface CSVParser {
    void registerParser(String fieldName, FieldParser parser);
    void parseFields(Map<String, String> keyValues, Object bean) throws FloraOnException;
}
