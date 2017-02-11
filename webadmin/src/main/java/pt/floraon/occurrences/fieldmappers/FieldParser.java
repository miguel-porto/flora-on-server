package pt.floraon.occurrences.fieldmappers;

import pt.floraon.occurrences.entities.newOccurrence;

/**
 * Created by miguel on 08-02-2017.
 */
public interface FieldParser {
    void parseValue(String inputValue, String inputFieldName, newOccurrence occurrence) throws IllegalArgumentException;
}
