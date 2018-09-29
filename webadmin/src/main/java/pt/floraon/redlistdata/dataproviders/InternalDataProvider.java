package pt.floraon.redlistdata.dataproviders;

import jline.internal.Log;
import pt.floraon.driver.FloraOnException;
import pt.floraon.driver.interfaces.IFloraOn;
import pt.floraon.occurrences.entities.Inventory;
import pt.floraon.taxonomy.entities.TaxEnt;

import java.io.IOException;
import java.util.*;

/**
 * Created by miguel on 24-03-2017.
 * TODO: this should be iterator!
 */
public class InternalDataProvider extends SimpleOccurrenceDataProvider {
    private IFloraOn driver;

    public InternalDataProvider(IFloraOn driver) {
        this.driver = driver;
    }

    @Override
    public void executeOccurrenceQuery(Iterator<TaxEnt> taxa) throws FloraOnException, IOException {
        if(taxa == null) throw new FloraOnException("Must specify a taxon");
//        List<Inventory> result = new ArrayList<>();
        Iterator<Inventory> it;
        this.occurrenceList = new ArrayList<>();

        Log.info("Executing internal query");
        while(taxa.hasNext()) {
            TaxEnt te = taxa.next();
            it = driver.getOccurrenceDriver().getOccurrencesOfTaxon(driver.asNodeKey(te.getID()));
//            occurrences = new SimpleOccurrenceIterator(it);
            while(it.hasNext())
                this.occurrenceList.add(new SimpleOccurrence(this.getDataSource(), it.next()));
        }


//        Gson gs = new GsonBuilder().setPrettyPrinting().create();
/*
        it = result.iterator();
        while(it.hasNext()) {
            Inventory inv = it.next();
            this.occurrenceList.add(new SimpleOccurrence(this.getDataSource(), inv));

        }*/
    }

    @Override
    public boolean canQueryText() {
        return false;
    }

    @Override
    public void executeOccurrenceTextQuery(String query) throws FloraOnException, IOException {

    }

    @Override
    public Map<String, Object> executeInfoQuery(Object query) throws FloraOnException, IOException {
        return null;
    }

    @Override
    public String getDataSource() {
        return "Red list database";
    }

}
