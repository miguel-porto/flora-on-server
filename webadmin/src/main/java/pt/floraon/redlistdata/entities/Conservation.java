package pt.floraon.redlistdata.entities;

import pt.floraon.driver.DiffableBean;
import pt.floraon.driver.datatypes.SafeHTMLString;
import pt.floraon.driver.utils.StringUtils;
import pt.floraon.redlistdata.RedListEnums;

/**
 * Created by miguel on 20-11-2016.
 */
public class Conservation implements DiffableBean {
    private SafeHTMLString description;
    private RedListEnums.YesNoNA conservationPlans;
    private SafeHTMLString conservationPlansJustification;
    private RedListEnums.YesNoNA exSituConservation;
    private SafeHTMLString exSituConservationJustification;
    private RedListEnums.ProposedConservationActions[] proposedConservationActions;
    private RedListEnums.ProposedStudyMeasures[] proposedStudyMeasures;
    private String[] legalProtection;

    public SafeHTMLString getDescription() {
        return description == null ? SafeHTMLString.emptyString() : description;
    }

    public RedListEnums.YesNoNA getConservationPlans() {
        return conservationPlans == null ? RedListEnums.YesNoNA.NO_DATA : conservationPlans;
    }

    public SafeHTMLString getConservationPlansJustification() {
        return conservationPlansJustification == null ? SafeHTMLString.emptyString() : conservationPlansJustification;
    }

    public RedListEnums.YesNoNA getExSituConservation() {
        return exSituConservation == null ? RedListEnums.YesNoNA.NO_DATA : exSituConservation;
    }

    public SafeHTMLString getExSituConservationJustification() {
        return exSituConservationJustification == null ? SafeHTMLString.emptyString() : exSituConservationJustification;
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
    }

    public String[] getLegalProtection() {
        return StringUtils.isArrayEmpty(legalProtection)
                ? new String[0]
                : legalProtection;
    }

    public void setDescription(SafeHTMLString description) {
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

    public void setConservationPlansJustification(SafeHTMLString conservationPlansJustification) {
        this.conservationPlansJustification = conservationPlansJustification;
    }

    public void setExSituConservationJustification(SafeHTMLString exSituConservationJustification) {
        this.exSituConservationJustification = exSituConservationJustification;
    }

    public void setLegalProtection(String[] legalProtection) {
        this.legalProtection = StringUtils.cleanArray(legalProtection);
    }
}
