package pt.floraon.occurrences;

import pt.floraon.driver.utils.StringUtils;
import pt.floraon.taxonomy.entities.TaxEnt;

import java.io.Serializable;
import java.util.*;

/**
 * Represents a question for taxonomic disambiguation, when processing ambiguous taxon names.
 * Created by miguel on 03-06-2017.
 */
public class TaxonomicQuestion implements Serializable {
    private Set<UUID> occurrenceUUIDs;
    private Set<TaxEnt> options;
    private String verbTaxon;

    public Set<UUID> getOccurrenceUUID() {
        return occurrenceUUIDs;
    }

    public String getOccurrenceUUIDs() {
        List<String> tmp = new ArrayList<>();
        for(UUID u : this.occurrenceUUIDs)
            tmp.add(u.toString());
        return StringUtils.implode(",", tmp.toArray(new String[tmp.size()]));
    }

    public Set<TaxEnt> getOptions() {
        return options;
    }

    public void addOption(TaxEnt taxEnt) {
        this.options.add(taxEnt);
    }

    public void addOccurrenceUUID(UUID uuid) {
        this.occurrenceUUIDs.add(uuid);
    }

    public String getVerbTaxon() {
        return verbTaxon;
    }

    public void setVerbTaxon(String verbTaxon) {
        this.verbTaxon = verbTaxon;
    }

    public TaxonomicQuestion(TaxEnt option, String verbTaxon, UUID occurrenceUUID) {
        this.verbTaxon = verbTaxon;
        this.options = new HashSet<>();
        if(option != null) this.options.add(option);
        this.occurrenceUUIDs = new HashSet<>();
        this.occurrenceUUIDs.add(occurrenceUUID);
    }
}
