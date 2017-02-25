package pt.floraon.occurrences.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * This is an abstract entity representing an inventory, which is composed of the inventory data, the list of taxa and
 * additionally some convenience fields.
 * Created by miguel on 08-02-2017.
 */
public class Inventory implements Serializable {
    private InventoryData inventoryData;
    private List<newOBSERVED_IN> observedIn = new ArrayList<>();

    public InventoryData getInventoryData() {
        return inventoryData;
    }

    public Inventory() {
        this.inventoryData = new InventoryData();
    }

    public Inventory(Inventory other) {
        this.inventoryData = other.inventoryData;
        this.observedIn = other.observedIn;
    }

    public void setInventoryData(InventoryData inventoryData) {
        this.inventoryData = inventoryData;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Inventory inventory = (Inventory) o;

        return inventoryData.equals(inventory.inventoryData);
    }

    @Override
    public int hashCode() {
        return inventoryData.hashCode();
    }
}
