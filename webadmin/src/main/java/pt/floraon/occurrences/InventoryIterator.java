package pt.floraon.occurrences;

import pt.floraon.driver.interfaces.OccurrenceFilter;
import pt.floraon.occurrences.entities.Inventory;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * An iterator for inventories, implementing a filter at each iteration.
 */
public class InventoryIterator implements Iterator<Inventory> {
    private Iterator<Inventory> cursor;
    private OccurrenceFilter[] filter = new OccurrenceFilter[0];
    private Inventory nextItem = null;
    private boolean consumed = true;

    public InventoryIterator(Iterator<Inventory> cursor) {
        this.cursor = cursor;
    }

    public InventoryIterator(Iterator<Inventory> cursor, OccurrenceFilter[] filter) {
        this.cursor = cursor;
        this.filter = filter == null ? new OccurrenceFilter[0] : filter;
    }

    @Override
    public boolean hasNext() {
        if(!consumed) return true;
        if(filter == null) {
            if(cursor.hasNext())
                nextItem = cursor.next();
            else
                return false;
            consumed = false;
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
            consumed = false;
            return true;
        }
    }

    @Override
    public Inventory next() {
        Inventory out;
        if(nextItem == null)
            throw new NoSuchElementException();

        consumed = true;
        return nextItem;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
