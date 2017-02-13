package pt.floraon.occurrences.entities;

import org.apache.commons.csv.CSVRecord;
import pt.floraon.driver.FloraOnException;
import pt.floraon.occurrences.fieldparsers.FieldParser;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This is an abstract entity representing an inventory, which is composed of the inventory data and the list of taxa.
 * Created by miguel on 08-02-2017.
 */
public class Inventory implements Serializable {
    private InventoryData speciesList;
    private List<newOBSERVED_IN> observedIn = new ArrayList<>();

    public InventoryData getSpeciesList() {
        return speciesList;
    }

    public void setSpeciesList(InventoryData speciesList) {
        this.speciesList = speciesList;
    }

    public List<newOBSERVED_IN> getObservedIn() {
        return observedIn;
    }

    public void setObservedIn(List<newOBSERVED_IN> observedIn) {
        this.observedIn = observedIn;
    }

    public void addObservedIn(newOBSERVED_IN observedIn) {
        this.observedIn.add(observedIn);
    }

    public static Inventory fromCSVline(CSVRecord record, Map<String, FieldParser> fieldParsers, Inventory existingInventory) throws FloraOnException {
//        newOBSERVED_IN obs = new newOBSERVED_IN();
        if(existingInventory == null) {
            existingInventory = new Inventory();
        }
//        existingInventory.addObservedIn(obs);
        existingInventory.setSpeciesList(new InventoryData());
        Map<String, String> map = record.toMap();

        for(Map.Entry<String, String> e : map.entrySet()) {
            fieldParsers.get(e.getKey()).parseValue(e.getValue(), e.getKey(), existingInventory);
        }
        return existingInventory;
    }
}
