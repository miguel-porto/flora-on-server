package pt.floraon.redlistdata.entities;

import pt.floraon.driver.DiffableBean;
import pt.floraon.driver.utils.StringUtils;
import pt.floraon.redlistdata.RedListEnums;

/**
 * Created by miguel on 20-11-2016.
 */
public class Conservation implements DiffableBean {
    private String description;
    private RedListEnums.YesNoNA conservationPlans;
    private String conservationPlansJustification;
    private RedListEnums.YesNoNA exSituConservation;
    private String exSituConservationJustification;
    private RedListEnums.ProposedConservationActions[] proposedConservationActions;
    private RedListEnums.ProposedStudyMeasures[] proposedStudyMeasures;

    public String getDescription() {
        return description == null ? "" : description;
    }

    public RedListEnums.YesNoNA getConservationPlans() {
        return conservationPlans == null ? RedListEnums.YesNoNA.NO_DATA : conservationPlans;
    }

    public String getConservationPlansJustification() {
        return conservationPlansJustification == null ? "" : conservationPlansJustification;
    }

    public RedListEnums.YesNoNA getExSituConservation() {
        return exSituConservation == null ? RedListEnums.YesNoNA.NO_DATA : exSituConservation;
    }

    public String getExSituConservationJustification() {
        return exSituConservationJustification == null ? "" : exSituConservationJustification;
    }

    public RedListEnums.ProposedConservationActions[] getProposedConservationActions() {
        return StringUtils.isArrayEmpty(proposedConservationActions)
                ? new RedListEnums.ProposedConservationActions[]{RedListEnums.ProposedConservationActions.NO_MEASURES}
                : proposedConservationActions;
    }

    public RedListEnums.ProposedStudyMeasures[] getProposedStudyMeasures() {
        return StringUtils.isArrayEmpty(proposedStudyMeasures)
                ? new RedListEnums.ProposedStudyMeasures[]{RedListEnums.ProposedStudyMeasures.NO_STUDIES}
                : proposedStudyMeasures;
/*
        return proposedStudyMeasures == null
                || proposedStudyMeasures.length == 0
                || proposedStudyMeasures[0] == null
                ? new RedListEnums.ProposedStudyMeasures[]{RedListEnums.ProposedStudyMeasures.NO_STUDIES}
                : proposedStudyMeasures;
*/
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
