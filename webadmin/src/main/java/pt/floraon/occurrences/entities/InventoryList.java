package pt.floraon.occurrences.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by miguel on 22-02-2017.
 */
public class InventoryList extends ArrayList<Inventory> implements Serializable {
    private List<newOBSERVED_IN> parseErrors = new ArrayList<>();

    public void addParseError(newOBSERVED_IN error) {
        this.parseErrors.add(error);
    }

    public List<newOBSERVED_IN> getParseErrors() {
        return parseErrors;
    }


}
