package pt.floraon.occurrences.fieldmappers;

import pt.floraon.occurrences.entities.newOccurrence;

import java.util.Map;

/**
 * Created by miguel on 09-02-2017.
 */
public class AliasFieldParser implements FieldParser {
    private String baseName;
    private Map<String, FieldParser> mappings;

    public AliasFieldParser(String baseName, Map<String, FieldParser> mappings) {
        this.baseName = baseName;
        this.mappings = mappings;
    }

    @Override
    public void parseValue(String inputValue, String inputFieldName, newOccurrence occurrence) throws IllegalArgumentException {
        this.mappings.get(this.baseName).parseValue(inputValue, this.baseName, occurrence);
    }
}
