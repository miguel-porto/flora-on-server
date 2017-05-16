package pt.floraon.driver;

import jline.internal.Log;
import pt.floraon.occurrences.entities.Inventory;
import pt.floraon.occurrences.entities.InventoryList;
import pt.floraon.occurrences.entities.newOBSERVED_IN;
import pt.floraon.taxonomy.entities.TaxEnt;

import java.util.Iterator;

/**
 * Created by miguel on 26-03-2017.
 */
public abstract class GOccurrenceDriver extends BaseFloraOnDriver implements IOccurrenceDriver {
    public GOccurrenceDriver(IFloraOn driver) {
        super(driver);
    }

    @Override
    public void matchTaxEntNames(Inventory inventory, boolean createNew, InventoryList inventories) throws FloraOnException {
        INodeWorker nwd = driver.getNodeWorkerDriver();
        for(newOBSERVED_IN oi : inventory.getUnmatchedOccurrences()) {
            TaxEnt te, matched;
            Log.info("Verbose name: "+ oi.getVerbTaxon());
            if(oi.getVerbTaxon() == null) continue;

            if(oi.getVerbTaxon().trim().equals("")) {
                Log.info("    Empty name, clearing");
//                if(inventories != null) inventories.addNoMatch(oi);
                oi.setTaxEntMatch("");
                continue;
            }

            try {
                te = TaxEnt.parse(oi.getVerbTaxon());
            } catch (FloraOnException e) {
                if(inventories != null) inventories.addParseError(oi);
                oi.setTaxEntMatch("");
                continue;
            }
            Log.info("    Parsed name: "+ te.getFullName(false));
            matched = nwd.getTaxEnt(te);
            if(matched == null) {
                if (createNew) {
                    matched = nwd.createTaxEntFromTaxEnt(te);
                    Log.warn("    No match, created new taxon");
                    if(inventories != null) inventories.addNoMatch(oi);
                    oi.setTaxEntMatch(matched.getID());
                } else {
                    Log.warn("    No match, do you want to add new taxon?");
                    if(inventories != null) inventories.addNoMatch(oi);
                    oi.setTaxEntMatch("");
                }
            } else {
                Log.info("    Matched name: " + matched.getFullName(false), " -- ", matched.getID());
                oi.setTaxEntMatch(matched.getID());
            }
        }
    }

    @Override
    public void matchTaxEntNames(InventoryList inventories, boolean createNew) throws FloraOnException {
        for(Inventory i : inventories)
            matchTaxEntNames(i, createNew, inventories);
    }

    @Override
    public InventoryList matchTaxEntNames(Iterator<Inventory> inventories, boolean createNew) throws FloraOnException {
        InventoryList inventoryList = new InventoryList();
        while(inventories.hasNext())
            matchTaxEntNames(inventories.next(), createNew, inventoryList);
        return inventoryList;
    }
}
