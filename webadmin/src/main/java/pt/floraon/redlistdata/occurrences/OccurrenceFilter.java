package pt.floraon.redlistdata.occurrences;

import pt.floraon.redlistdata.dataproviders.SimpleOccurrence;

/**
 * A filter to filter occurrences in or out an iterator
 */
public interface OccurrenceFilter {
    boolean enter(SimpleOccurrence simpleOccurrence);
}
