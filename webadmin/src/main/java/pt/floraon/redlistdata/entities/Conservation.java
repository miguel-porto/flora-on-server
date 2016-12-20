package pt.floraon.redlistdata.entities;

/**
 * Created by miguel on 20-11-2016.
 */
public class Conservation {
    private String description;
    private RedListEnums.YesNoNA conservationPlans;
    private String conservationPlansJustification;
    private RedListEnums.YesNoNA exSituConservation;
    private String exSituConservationJustification;
    private String[] proposedConservationActions = new String[0];

    public String getDescription() {
        return description;
    }

    public RedListEnums.YesNoNA getConservationPlans() {
        return conservationPlans;
    }

    public String getConservationPlansJustification() {
        return conservationPlansJustification;
    }

    public String getExSituConservationJustification() {
        return exSituConservationJustification;
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

    public void setConservationPlansJustification(String conservationPlansJustification) {
        this.conservationPlansJustification = conservationPlansJustification;
    }

    public void setExSituConservationJustification(String exSituConservationJustification) {
        this.exSituConservationJustification = exSituConservationJustification;
    }
}
