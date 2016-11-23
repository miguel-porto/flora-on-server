package pt.floraon.redlistdata.entities;

/**
 * Created by miguel on 20-11-2016.
 */
public class Conservation {
    private String description;
    private RedListEnums.YesNoNA conservationPlans;
    private RedListEnums.YesNoNA exSituConservation;
    private String[] proposedConservationActions = new String[0];

    public String getDescription() {
        return description;
    }

    public RedListEnums.YesNoNA getConservationPlans() {
        return conservationPlans;
    }

    public RedListEnums.YesNoNA getExSituConservation() {
        return exSituConservation;
    }

    public String[] getProposedConservationActions() {
        return proposedConservationActions;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setConservationPlans(RedListEnums.YesNoNA conservationPlans) {
        this.conservationPlans = conservationPlans;
    }

    public void setExSituConservation(RedListEnums.YesNoNA exSituConservation) {
        this.exSituConservation = exSituConservation;
    }

    public void setProposedConservationActions(String[] proposedConservationActions) {
        this.proposedConservationActions = proposedConservationActions;
    }
}
