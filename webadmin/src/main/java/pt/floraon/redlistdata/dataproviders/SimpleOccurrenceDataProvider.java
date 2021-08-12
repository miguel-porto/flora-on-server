package pt.floraon.redlistdata.dataproviders;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import edu.emory.mathcs.backport.java.util.Collections;
import pt.floraon.driver.FloraOnException;
import pt.floraon.occurrences.dataproviders.DataProviderTranslator;
import pt.floraon.occurrences.entities.Occurrence;
import pt.floraon.occurrences.entities.Inventory;
import pt.floraon.taxonomy.entities.TaxEnt;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

/**
 * Created by miguel on 02-11-2016.
 */
public abstract class SimpleOccurrenceDataProvider implements Iterable<Occurrence> {
    protected List<Occurrence> occurrenceList;
//    protected Iterator<Occurrence> occurrences;

    public int size() {
        return this.occurrenceList.size();
    }

    /**
     * Executes a query and updates the Iterable list of occurrences with the results.
     * @throws FloraOnException
     * @throws IOException
     */
    public void executeOccurrenceQuery(TaxEnt taxon) throws FloraOnException, IOException {
        if(taxon == null)
            executeOccurrenceQuery((Iterator<TaxEnt>) null);
        else
            executeOccurrenceQuery(java.util.Collections.singletonList(taxon).iterator());
    }

    public void executeOccurrenceQuery(TaxEnt taxon, Object flags) throws FloraOnException, IOException {
        executeOccurrenceQuery(java.util.Collections.singletonList(taxon).iterator(), flags);
    }

    /**
     * Executes a query and updates the Iterable list of occurrences with the results.
     * The query involves all infrataxa.
     * @throws FloraOnException
     * @throws IOException
     */
    public abstract void executeOccurrenceQuery(Iterator<TaxEnt> taxon) throws FloraOnException, IOException;

    public void executeOccurrenceQuery(Iterator<TaxEnt> taxon, Object flags) throws FloraOnException, IOException {
        executeOccurrenceQuery(taxon);
    }

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
    public Iterator<Occurrence> iterator() {
        return occurrenceList == null ? Collections.emptyList().iterator() : occurrenceList.iterator();
//        return occurrences == null ? Collections.emptyList().iterator() : occurrences;
    }

/*
    */
/**
     * A conversion iterator from Iterator<Inventory> to Iterator<Occurrence>
*/
    public class SimpleOccurrenceIterator implements Iterator<Occurrence> {
        Iterator<Inventory> inventoryIterator;

        SimpleOccurrenceIterator(Iterator<Inventory> inventoryIterator) {
            this.inventoryIterator = inventoryIterator;
        }

        @Override
        public boolean hasNext() {
            return inventoryIterator.hasNext();
        }

        @Override
        public Occurrence next() {
            return new Occurrence(SimpleOccurrenceDataProvider.this.getDataSource(), inventoryIterator.next());
        }

        @Override
        public void remove() {

        }
    }

    protected <T> void readBigJsonFromStream(JsonReader jsonReader, Class<T> type, DataProviderTranslator translator) throws IOException {
        occurrenceList = new ArrayList<>();
        Gson gson = new Gson();
        jsonReader.beginObject();
        while (jsonReader.hasNext()) {
            if(jsonReader.peek() == JsonToken.BEGIN_ARRAY) {
                jsonReader.beginArray();
                while (jsonReader.hasNext()) {
                    T o = gson.fromJson(jsonReader, type);
                    Occurrence so = translator.translate(o);
                    occurrenceList.add(so);
                }
                jsonReader.endArray();
            } else jsonReader.skipValue();
        }
        jsonReader.endObject();
        jsonReader.close();
    }

    protected <T> void readBigJsonFromStream(InputStream stream, Class<T> type, DataProviderTranslator translator) throws IOException {
        readBigJsonFromStream(new JsonReader(new InputStreamReader(stream)), type, translator);
    }
}
