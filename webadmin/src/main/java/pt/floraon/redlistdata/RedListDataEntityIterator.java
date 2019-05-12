package pt.floraon.redlistdata;

import pt.floraon.occurrences.entities.Occurrence;
import pt.floraon.redlistdata.entities.RedListDataEntity;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * An iterator of {@link RedListDataEntity} that implements a filter (or filters) upon iteration.
 */
public class RedListDataEntityIterator implements Iterator<RedListDataEntity> {
    private Iterator<RedListDataEntity> cursor;
    private RedListDataFilter[] filter = new RedListDataFilter[0];
    private RedListDataEntity nextItem = null;
    private boolean consumed = true;

    public RedListDataEntityIterator(Iterator<RedListDataEntity> cursor) {
        this.cursor = cursor;
    }

    public RedListDataEntityIterator(Iterator<RedListDataEntity> cursor, RedListDataFilter[] filter) {
        this.cursor = cursor;
        this.filter = filter == null ? new RedListDataFilter[0] : filter;
    }

    public RedListDataEntityIterator(Iterator<RedListDataEntity> cursor, RedListDataFilter filter) {
        this(cursor, new RedListDataFilter[] {filter});
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
                if(cursor.hasNext()) {
                    nextItem = cursor.next();
                } else
                    return false;

                enter = true;
                for(RedListDataFilter of : this.filter) {
                    enter &= of.enter(nextItem);
                }
            } while(!enter);
            consumed = false;
            return true;
        }
    }

    @Override
    public RedListDataEntity next() {
        Occurrence out;
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
