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
    private RedListEnums.ProposedConservationActions[] proposedConservationActions;
    private RedListEnums.ProposedStudyMeasures[] proposedStudyMeasures;

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

    public RedListEnums.ProposedConservationActions[] getProposedConservationActions() {
        return proposedConservationActions == null ? new RedListEnums.ProposedConservationActions[]{RedListEnums.ProposedConservationActions.NO_MEASURES} : proposedConservationActions;
    }

    public RedListEnums.ProposedStudyMeasures[] getProposedStudyMeasures() {
        return proposedStudyMeasures == null ? new RedListEnums.ProposedStudyMeasures[]{RedListEnums.ProposedStudyMeasures.NO_STUDIES} : proposedStudyMeasures;
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

    public void setProposedConservationActions(RedListEnums.ProposedConservationActions[] proposedConservationActions) {
        this.proposedConservationActions = proposedConservationActions;
    }

    public void setProposedStudyMeasures(RedListEnums.ProposedStudyMeasures[] proposedStudyMeasures) {
        this.proposedStudyMeasures = proposedStudyMeasures;
    }

    public void setConservationPlansJustification(String conservationPlansJustification) {
        this.conservationPlansJustification = conservationPlansJustification;
    }

    public void setExSituConservationJustification(String exSituConservationJustification) {
        this.exSituConservationJustification = exSituConservationJustification;
    }
}
