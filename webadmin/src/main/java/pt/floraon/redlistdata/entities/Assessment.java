package pt.floraon.redlistdata.entities;

/**
 * Created by miguel on 23-11-2016.
 */
public class Assessment {
    private RedListEnums.RedListCategories category;
    private RedListEnums.CRTags subCategory;
    private String criteria;
    private String justification;
    private String[] authors = new String[0];
    private String collaborators;
    private String[] evaluator = new String[0];
    private String[] reviewer = new String[0];
    private RedListEnums.AssessmentStatus assessmentStatus;
    private RedListEnums.YesNoLikelyUnlikely propaguleImmigration;
    private RedListEnums.YesNoLikelyUnlikely decreaseImmigration;
    private RedListEnums.YesNoLikelyUnlikely isSink;

    public RedListEnums.YesNoLikelyUnlikely getPropaguleImmigration() {
        return propaguleImmigration;
    }

    public RedListEnums.YesNoLikelyUnlikely getDecreaseImmigration() {
        return decreaseImmigration;
    }

    public RedListEnums.YesNoLikelyUnlikely getIsSink() {
        return isSink;
    }

    public RedListEnums.RedListCategories getCategory() {
        return category;
    }

    public RedListEnums.CRTags getSubCategory() {
        if(category == null) return null;
        return category.isTrigger() ? (subCategory == RedListEnums.CRTags.NO_TAG ? null : subCategory) : null;
    }

    public String getCriteria() {
        return criteria;
    }

    public String getJustification() {
        return justification;
    }

    public String[] getAuthors() {
        return authors;
    }

    public String getCollaborators() {
        return collaborators;
    }

    public String[] getEvaluator() {
        return evaluator;
    }

    public String[] getReviewer() {
        return reviewer;
    }

    public RedListEnums.AssessmentStatus getAssessmentStatus() {
        return assessmentStatus;
    }

    public void setCategory(RedListEnums.RedListCategories category) {
        this.category = category.getOriginalCategory();
    }

    public void setSubCategory(RedListEnums.CRTags subCategory) {
        this.subCategory = subCategory;
    }

    public void setPropaguleImmigration(RedListEnums.YesNoLikelyUnlikely propaguleImmigration) {
        this.propaguleImmigration = propaguleImmigration;
    }

    public void setDecreaseImmigration(RedListEnums.YesNoLikelyUnlikely decreaseImmigration) {
        this.decreaseImmigration = decreaseImmigration;
    }

    public void setIsSink(RedListEnums.YesNoLikelyUnlikely isSink) {
        this.isSink = isSink;
    }

    public void setCriteria(String criteria) {
        this.criteria = criteria;
    }

    public void setJustification(String justification) {
        this.justification = justification;
    }

    public void setAuthors(String[] authors) {
        this.authors = authors;
    }

    public void setCollaborators(String collaborators) {
        this.collaborators = collaborators;
    }

    public void setEvaluator(String[] evaluator) {
        this.evaluator = evaluator;
    }

    public void setReviewer(String[] reviewer) {
        this.reviewer = reviewer;
    }

    public void setAssessmentStatus(RedListEnums.AssessmentStatus assessmentStatus) {
        this.assessmentStatus = assessmentStatus;
    }

    /* *****************************************/
    /* Convenience functions for functionality */
    /* *****************************************/

    /**
     * Suggests uplist or downlist according to the answers to the rescue effect questions
     * @return
     */
    public RedListEnums.UpDownList suggestUpDownList() {
        if(
                this.propaguleImmigration == RedListEnums.YesNoLikelyUnlikely.NOT_KNOWN
                        || this.propaguleImmigration == RedListEnums.YesNoLikelyUnlikely.NO
                        ||  (
                        (this.propaguleImmigration == RedListEnums.YesNoLikelyUnlikely.YES
                                || this.propaguleImmigration == RedListEnums.YesNoLikelyUnlikely.LIKELY)
                                && (this.decreaseImmigration == RedListEnums.YesNoLikelyUnlikely.YES
                                || this.decreaseImmigration == RedListEnums.YesNoLikelyUnlikely.NOT_KNOWN)
                                && (this.isSink == RedListEnums.YesNoLikelyUnlikely.NO
                                || this.isSink == RedListEnums.YesNoLikelyUnlikely.NOT_KNOWN)
                )
                ) return RedListEnums.UpDownList.NONE;

        if(
                (this.propaguleImmigration == RedListEnums.YesNoLikelyUnlikely.YES
                        || this.propaguleImmigration == RedListEnums.YesNoLikelyUnlikely.LIKELY)
                        && (this.decreaseImmigration == RedListEnums.YesNoLikelyUnlikely.NO
                        || this.decreaseImmigration == RedListEnums.YesNoLikelyUnlikely.UNLIKELY)
                ) return RedListEnums.UpDownList.DOWNLIST;

        if(
                (this.propaguleImmigration == RedListEnums.YesNoLikelyUnlikely.YES
                        || this.propaguleImmigration == RedListEnums.YesNoLikelyUnlikely.LIKELY)
                        && (this.decreaseImmigration == RedListEnums.YesNoLikelyUnlikely.YES
                        || this.decreaseImmigration == RedListEnums.YesNoLikelyUnlikely.NOT_KNOWN)
                        && (this.isSink == RedListEnums.YesNoLikelyUnlikely.YES
                        || this.isSink == RedListEnums.YesNoLikelyUnlikely.LIKELY)
                ) return RedListEnums.UpDownList.UPLIST;

        return RedListEnums.UpDownList.NONE;
    }

    /**
     * Gets the category after being adjusted by the uplist/downlist choice
     * @return
     */
    public RedListEnums.RedListCategories getAdjustedCategory() {
        if(category == null) return null;
        switch(suggestUpDownList()) {
            case UPLIST: return category.getUplistCategory();
            case DOWNLIST: return category.getDownlistCategory();
            default: return category;
        }
    }

}
