package pt.floraon.occurrences.entities;

import pt.floraon.driver.Constants;
import pt.floraon.occurrences.TaxonomicQuestion;
import pt.floraon.taxonomy.entities.TaxEnt;

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
    private Set<String> noMatches = new LinkedHashSet<>();
    private Set<String> verboseErrors = new LinkedHashSet<>();
    private Set<String> verboseWarnings = new LinkedHashSet<>();
    private Map<String, TaxonomicQuestion> questions = new HashMap<>();

    public Set<String> getVerboseErrors() {
        return verboseErrors;
    }

    public void setVerboseErrors(Set<String> verboseErrors) {
        this.verboseErrors = verboseErrors;
    }

    public Set<String> getVerboseWarnings() {
        return verboseWarnings;
    }

    public void setVerboseWarnings(Set<String> verboseWarnings) {
        this.verboseWarnings = verboseWarnings;
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
        this.noMatches.add(noMatch.getVerbTaxon());
    }

    public void addQuestion(String verbTaxon, UUID occurrenceUUID, TaxEnt taxEnt) {
        TaxonomicQuestion tmp = questions.get(verbTaxon);
        if(tmp != null) {
            tmp.addOccurrenceUUID(occurrenceUUID);
            if(taxEnt != null) tmp.addOption(taxEnt);
        } else {
            tmp = new TaxonomicQuestion(taxEnt, verbTaxon, occurrenceUUID);
            questions.put(verbTaxon, tmp);
        }
    }

    public Map<String, TaxonomicQuestion> getQuestions() {
        return questions;
    }

    public List<newOBSERVED_IN> getParseErrors() {
        return parseErrors;
    }

    public Set<String> getNoMatches() {
        return noMatches;
    }

    public void setUploadDate() {
        this.uploadDate = new Date();
    }

    public String getUploadDate() {
        return Constants.dateFormat.format(this.uploadDate);
    }
}
