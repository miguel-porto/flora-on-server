package pt.floraon.redlistdata.dataproviders;

import pt.floraon.driver.FloraOnException;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by miguel on 02-11-2016.
 */
public abstract class ExternalDataProvider implements Iterable<SimpleOccurrence> {
    protected List<SimpleOccurrence> occurrenceList;

    public int size() {
        return this.occurrenceList.size();
    }

    /**
     * Executes a query and updates the Iterable list of occurrences with the results.
     * @throws FloraOnException
     * @throws IOException
     */
    public abstract void executeOccurrenceQuery(String newId, Integer oldId) throws FloraOnException, IOException;

    /**
     * Executes a query and returns arbitrary data about a taxon.
     * @param query
     * @throws FloraOnException
     * @throws IOException
     */
    public abstract Map<String, Object> executeInfoQuery(Object query) throws FloraOnException, IOException;

    public abstract String getDataSource();

    @Override
    public Iterator<SimpleOccurrence> iterator() {
        return occurrenceList.iterator();
    }

}
