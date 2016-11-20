package pt.floraon.redlistdata.entities;

/**
 * Created by miguel on 19-11-2016.
 */
public class UsesAndTrade {
    private String description;
    private String[] uses = new String[0];
    private boolean traded;
    private RedListEnums.Overexploitation overexploitation;

    public String getDescription() {
        return description;
    }

    public String[] getUses() {
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

    public void setUses(String[] uses) {
        this.uses = uses;
    }

    public void setTraded(boolean traded) {
        this.traded = traded;
    }

    public void setOverexploitation(RedListEnums.Overexploitation overexploitation) {
        this.overexploitation = overexploitation;
    }
}
