package pt.floraon.driver.interfaces;

import pt.floraon.occurrences.entities.Inventory;

/**
 * A filter to filter occurrences in or out an iterator
 */
public interface OccurrenceFilter {
    boolean enter(Inventory simpleOccurrence);
}
