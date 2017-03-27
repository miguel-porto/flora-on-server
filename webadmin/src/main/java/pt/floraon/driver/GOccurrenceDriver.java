package pt.floraon.driver;

import jline.internal.Log;
import pt.floraon.occurrences.entities.Inventory;
import pt.floraon.occurrences.entities.InventoryList;
import pt.floraon.occurrences.entities.newOBSERVED_IN;
import pt.floraon.taxonomy.entities.TaxEnt;

/**
 * Created by miguel on 26-03-2017.
 */
public abstract class GOccurrenceDriver extends BaseFloraOnDriver implements IOccurrenceDriver {
    public GOccurrenceDriver(IFloraOn driver) {
        super(driver);
    }

    @Override
    public void matchTaxEntNames(InventoryList inventories) throws FloraOnException {
        INodeWorker nwd = driver.getNodeWorkerDriver();
        for(Inventory i : inventories) {
            for(newOBSERVED_IN oi : i.getUnmatchedOccurrences()) {
                TaxEnt te, matched;
                Log.info("Verbose name: "+ oi.getVerbTaxon());
                try {
                    te = TaxEnt.parse(oi.getVerbTaxon());
                } catch (FloraOnException e) {
                    inventories.addParseError(oi);
                    continue;
                }
                Log.info("    Parsed name: "+ te.getFullName(false));
                matched = nwd.getTaxEnt(te);
                if(matched == null) {
                    Log.warn("    No match");
                    inventories.addNoMatch(oi);
                } else {
                    Log.info("    Matched name: " + matched.getFullName(false), " -- ", matched.getID());
                    oi.setTaxEntMatch(matched.getID());
                }
            }
        }

    }
}
