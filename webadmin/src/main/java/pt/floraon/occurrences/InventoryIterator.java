package pt.floraon.occurrences;

import pt.floraon.driver.interfaces.OccurrenceFilter;
import pt.floraon.occurrences.entities.Inventory;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class InventoryIterator implements Iterator<Inventory> {
    private Iterator<Inventory> cursor;
    private OccurrenceFilter[] filter = new OccurrenceFilter[0];
    private Inventory nextItem = null;

    public InventoryIterator(Iterator<Inventory> cursor) {
        this.cursor = cursor;
    }

    public InventoryIterator(Iterator<Inventory> cursor, OccurrenceFilter[] filter) {
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
    public Inventory next() {
        Inventory out;
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
