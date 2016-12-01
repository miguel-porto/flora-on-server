package pt.floraon.redlistdata.entities;

/**
 * Created by miguel on 19-11-2016.
 */
public class UsesAndTrade {
    private String description;
    private RedListEnums.Uses[] uses = new RedListEnums.Uses[0];
    private boolean traded;
    private RedListEnums.Overexploitation overexploitation;

    public String getDescription() {
        return description;
    }

    public RedListEnums.Uses[] getUses() {
        return uses;
    }

    public boolean isTraded() {
        return traded;
    }

    public RedListEnums.Overexploitation getOverexploitation() {
        return overexploitation;
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
