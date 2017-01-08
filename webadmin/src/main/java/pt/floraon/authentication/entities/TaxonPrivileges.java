package pt.floraon.authentication.entities;

import jline.internal.Log;
import pt.floraon.authentication.Privileges;
import pt.floraon.driver.FloraOnException;
import pt.floraon.driver.IFloraOn;
import pt.floraon.driver.INodeKey;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by miguel on 07-01-2017.
 */
public class TaxonPrivileges {
    /**
     * The INodeKeys of the taxa to which the privileges apply.
     */
    private String[] applicableTaxa;
    private Set<Privileges> privileges;

    public TaxonPrivileges() {}

    public TaxonPrivileges(String[] taxa, String[] privileges) {
        this.applicableTaxa = taxa;
        if(this.privileges == null) this.privileges = new HashSet<>();

        for(String p : privileges) {
            try {
                this.privileges.add(Privileges.valueOf(p));
            } catch (IllegalArgumentException e) {
                Log.warn("Privilege " + p + " not found.");
            }
        }
    }

    public TaxonPrivileges(String[] taxa, Set<Privileges> privileges) {
        this.applicableTaxa = taxa;
        this.privileges = privileges;
    }

    public String[] getApplicableTaxa() {
        return applicableTaxa;
    }

    public void setApplicableTaxa(String[] applicableTaxa) {
        this.applicableTaxa = applicableTaxa;
    }

    public Set<Privileges> getPrivileges() {
        return privileges == null ? Collections.<Privileges>emptySet() : privileges;
    }

    public void setPrivileges(Set<Privileges> privileges) {
        this.privileges = privileges;
    }

    public Set<Privileges> getPrivilegesForTaxon(IFloraOn driver, INodeKey taxonId) throws FloraOnException {
        for (String taxon : this.applicableTaxa) {
            if (driver.wrapTaxEnt(taxonId).isInfrataxonOf(driver.asNodeKey(taxon))) {    // is the taxon covered by the privileges?
                return this.privileges;
            }
        }

        return Collections.<Privileges>emptySet();
    }
}
