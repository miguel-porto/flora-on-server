package pt.floraon.redlistdata.dataproviders;

import pt.floraon.driver.FloraOnException;
import pt.floraon.driver.IFloraOn;
import pt.floraon.occurrences.entities.Inventory;
import pt.floraon.occurrences.entities.newOBSERVED_IN;
import pt.floraon.taxonomy.entities.CanonicalName;
import pt.floraon.taxonomy.entities.TaxEnt;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by miguel on 24-03-2017.
 */
public class InternalDataProvider extends ExternalDataProvider {
    private IFloraOn driver;

    public InternalDataProvider(IFloraOn driver) {
        this.driver = driver;
    }

    @Override
    public void executeOccurrenceQuery(String taxEntId, Integer oldId) throws FloraOnException, IOException {
        if(taxEntId == null) throw new FloraOnException("Must specify as taxon");

        Iterator<Inventory> it = driver.getOccurrenceDriver().getOccurrencesOfTaxon(driver.asNodeKey(taxEntId));

        occurrenceList = new ArrayList<>();

        while(it.hasNext()) {
            Inventory inv = it.next();
            String[] mainObserver = inv.getObserverNames();
            newOBSERVED_IN te = inv.getTaxa()[0];
            CanonicalName cn = te.getTaxEnt().getCanonicalName();

            this.occurrenceList.add(new SimpleOccurrence(this.getDataSource(), inv.getLatitude(), inv.getLongitude(), inv.getYear()
                    , inv.getMonth(), inv.getDay(), mainObserver.length == 0 ? "" : mainObserver[0]
                    , cn.getGenus(), cn.getSpecificEpithet(), cn.getInfraRanksAsString(), null, 0
                    , te.getTaxEnt().getOldId(), 0, true, null) );
        }
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
