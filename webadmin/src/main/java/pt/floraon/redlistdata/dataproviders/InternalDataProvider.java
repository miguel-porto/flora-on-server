package pt.floraon.redlistdata.dataproviders;

import pt.floraon.driver.FloraOnException;
import pt.floraon.driver.interfaces.IFloraOn;
import pt.floraon.occurrences.entities.Inventory;
import pt.floraon.taxonomy.entities.TaxEnt;

import java.io.IOException;
import java.util.*;

/**
 * Created by miguel on 24-03-2017.
 */
public class InternalDataProvider extends SimpleOccurrenceDataProvider {
    private IFloraOn driver;

    public InternalDataProvider(IFloraOn driver) {
        this.driver = driver;
    }

    @Override
    public void executeOccurrenceQuery(Iterator<TaxEnt> taxa) throws FloraOnException, IOException {
        if(taxa == null) throw new FloraOnException("Must specify a taxon");
        List<Inventory> result = new ArrayList<>();
        Iterator<Inventory> it;

        while(taxa.hasNext()) {
            TaxEnt te = taxa.next();
            it = driver.getOccurrenceDriver().getOccurrencesOfTaxon(driver.asNodeKey(te.getID()));
            while(it.hasNext())
                result.add(it.next());
        }

        occurrenceList = new ArrayList<>();
//        Gson gs = new GsonBuilder().setPrettyPrinting().create();

        it = result.iterator();
        while(it.hasNext()) {
            Inventory inv = it.next();
/*
            String[] mainObserver = inv._getObserverNames();
            SimpleOccurrence so;
            OBSERVED_IN te = inv._getTaxa()[0];
            CanonicalName cn = te.getTaxEnt().getCanonicalName();
*/
//            System.out.println(gs.toJson(inv));
            this.occurrenceList.add(new SimpleOccurrence(this.getDataSource(), inv));
/*
            this.occurrenceList.add(so = new SimpleOccurrence(this.getDataSource(), inv._getLatitude(), inv._getLongitude(), inv.getYear()
                    , inv.getMonth(), inv.getDay(), mainObserver.length == 0 ? "" : mainObserver[0]
                    , cn.getGenus(), cn.getSpecificEpithet(), cn.getInfraRanksAsString(), te.getComment(), 0
                    , te.getTaxEnt().getOldId(), 0, te.getConfidence(), te.getPhenoState()) );
*/

        }
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
