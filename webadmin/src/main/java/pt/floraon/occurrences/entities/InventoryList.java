package pt.floraon.occurrences.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by miguel on 22-02-2017.
 */
public class InventoryList extends ArrayList<Inventory> implements Serializable {
    private List<newOBSERVED_IN> parseErrors = new ArrayList<>();
    private List<newOBSERVED_IN> noMatches = new ArrayList<>();

    public void addParseError(newOBSERVED_IN error) {
        this.parseErrors.add(error);
    }

    public void addNoMatch(newOBSERVED_IN noMatch) {
        this.noMatches.add(noMatch);
    }

    public List<newOBSERVED_IN> getParseErrors() {
        return parseErrors;
    }

    public List<newOBSERVED_IN> getNoMatches() {
        return noMatches;
    }
}
