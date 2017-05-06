package pt.floraon.occurrences.entities;

import pt.floraon.driver.Constants;

import java.io.Serializable;
import java.util.*;

/**
 * Represents a list of inventories.
 * Created by miguel on 22-02-2017.
 */
public class InventoryList extends ArrayList<Inventory> implements Serializable {
    private String fileName;
    private Date uploadDate;
    private List<newOBSERVED_IN> parseErrors = new ArrayList<>();
    private List<newOBSERVED_IN> noMatches = new ArrayList<>();
    private Set<String> verboseErrors = new LinkedHashSet<>();

    public Set<String> getVerboseErrors() {
        return verboseErrors;
    }

    public void setVerboseErrors(Set<String> verboseErrors) {
        this.verboseErrors = verboseErrors;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

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

    public void setUploadDate() {
        this.uploadDate = new Date();
    }

    public String getUploadDate() {
        return Constants.dateFormat.format(this.uploadDate);
    }
}
