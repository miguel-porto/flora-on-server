package pt.floraon.redlistdata.entities;

import pt.floraon.driver.DiffableBean;
import pt.floraon.redlistdata.RedListEnums;

/**
 * Created by miguel on 19-11-2016.
 */
public class UsesAndTrade implements DiffableBean {
    private String description;
    private RedListEnums.Uses[] uses;
    private boolean traded;
    private RedListEnums.Overexploitation overexploitation;

    public String getDescription() {
        return description == null ? "" : description;
    }

    public RedListEnums.Uses[] getUses() {
        return uses == null
                || uses.length == 0
                || uses[0] == null
                ? new RedListEnums.Uses[]{RedListEnums.Uses.UNKNOWN}
                : uses;
    }

    public boolean isTraded() {
        return traded;
    }

    public RedListEnums.Overexploitation getOverexploitation() {
        return overexploitation == null ? RedListEnums.Overexploitation.NO_DATA : overexploitation;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setUses(RedListEnums.Uses[] uses) {
        this.uses = uses;
    }

    public void setTraded(boolean traded) {
        this.traded = traded;
    }

    public void setOverexploitation(RedListEnums.Overexploitation overexploitation) {
        this.overexploitation = overexploitation;
    }

}
