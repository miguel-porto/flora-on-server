package pt.floraon.occurrences;

import pt.floraon.driver.interfaces.OccurrenceFilter;
import pt.floraon.occurrences.entities.Occurrence;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * An iterator for occurrences, implementing a filter at each iteration.
 */
public class OccurrenceIterator implements Iterator<Occurrence> {
    private Iterator<Occurrence> cursor;
    private OccurrenceFilter[] filter = new OccurrenceFilter[0];
    private Occurrence nextItem = null;

    public OccurrenceIterator(Iterator<Occurrence> cursor) {
        this.cursor = cursor;
    }

    public OccurrenceIterator(Iterator<Occurrence> cursor, OccurrenceFilter[] filter) {
        this.cursor = cursor;
        this.filter = filter == null ? new OccurrenceFilter[0] : filter;
    }

    @Override
    public boolean hasNext() {
        if(filter == null) {
            if(cursor.hasNext())
                nextItem = cursor.next();
            else
                return false;
            return true;
        } else {
            boolean enter;
            do {
                if(cursor.hasNext())
                    nextItem = cursor.next();
                else
                    return false;

                enter = true;
                for(OccurrenceFilter of : this.filter) {
                    enter &= of.enter(nextItem);
                }
            } while(!enter);
            return true;
        }
    }

    @Override
    public Occurrence next() {
        Occurrence out;
        if(nextItem == null)
            throw new NoSuchElementException();

        out = nextItem;
        nextItem = null;
        return out;
    }

    @Override
    public void remove() {

    }
}
