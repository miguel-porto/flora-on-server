package pt.floraon.redlistdata.dataproviders;

import edu.emory.mathcs.backport.java.util.Collections;
import pt.floraon.driver.FloraOnException;
import pt.floraon.occurrences.entities.Inventory;
import pt.floraon.taxonomy.entities.TaxEnt;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by miguel on 02-11-2016.
 */
public abstract class SimpleOccurrenceDataProvider implements Iterable<SimpleOccurrence> {
    protected List<SimpleOccurrence> occurrenceList;
//    protected Iterator<SimpleOccurrence> occurrences;

    public int size() {
        return this.occurrenceList.size();
    }

    /**
     * Executes a query and updates the Iterable list of occurrences with the results.
     * @throws FloraOnException
     * @throws IOException
     */
    public void executeOccurrenceQuery(TaxEnt taxon) throws FloraOnException, IOException {
        executeOccurrenceQuery(Arrays.asList(taxon).iterator());
    }

    /**
     * Executes a query and updates the Iterable list of occurrences with the results.
     * The query involves all infrataxa.
     * @throws FloraOnException
     * @throws IOException
     */
    public abstract void executeOccurrenceQuery(Iterator<TaxEnt> taxon) throws FloraOnException, IOException;

    public abstract boolean canQueryText();

    /**
     * Executes a query and updates the Iterable list of occurrences with the results.
     * This query is merely textual.
     * @param query
     * @throws FloraOnException
     * @throws IOException
     */
    public abstract void executeOccurrenceTextQuery(String query) throws FloraOnException, IOException;

    /**
     * Executes a query and returns arbitrary data about a taxon.
     * TODO: this shouldn't be here, it's just a workaround for fetching info in Flora-On
     * @param query
     * @throws FloraOnException
     * @throws IOException
     */
    public abstract Map<String, Object> executeInfoQuery(Object query) throws FloraOnException, IOException;

    public abstract String getDataSource();

    @Override
    public Iterator<SimpleOccurrence> iterator() {
        return occurrenceList == null ? Collections.emptyList().iterator() : occurrenceList.iterator();
//        return occurrences == null ? Collections.emptyList().iterator() : occurrences;
    }

/*
    */
/**
     * A conversion iterator from Iterator<Inventory> to Iterator<SimpleOccurrence>
*/
    public class SimpleOccurrenceIterator implements Iterator<SimpleOccurrence> {
        Iterator<Inventory> inventoryIterator;

        SimpleOccurrenceIterator(Iterator<Inventory> inventoryIterator) {
            this.inventoryIterator = inventoryIterator;
        }

        @Override
        public boolean hasNext() {
            return inventoryIterator.hasNext();
        }

        @Override
        public SimpleOccurrence next() {
            return new SimpleOccurrence(SimpleOccurrenceDataProvider.this.getDataSource(), inventoryIterator.next());
        }

        @Override
        public void remove() {

        }
    }

}
