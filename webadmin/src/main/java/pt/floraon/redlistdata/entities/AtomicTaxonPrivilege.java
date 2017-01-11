package pt.floraon.redlistdata.entities;

import pt.floraon.authentication.Privileges;

import java.util.List;

/**
 * Created by miguel on 09-01-2017.
 */
public class AtomicTaxonPrivilege {
    private String taxonName;
    private String taxEntId;
    private List<String> privileges;
    private String userId;

    public String getTaxonName() {
        return taxonName;
    }

    public String getTaxEntId() {
        return taxEntId;
    }

    public List<String> getPrivileges() {
        return privileges;
    }

    public String getUserId() {
        return userId;
    }

    public boolean isResponsibleForTexts() {
        return Privileges.isResponsibleForTexts(privileges);
    }

    public boolean isResponsibleForAssessment() {
        return Privileges.isResponsibleForAssessment(privileges);
    }

    public boolean isResponsibleForRevision() {
        return Privileges.isResponsibleForRevision(privileges);
    }
}
