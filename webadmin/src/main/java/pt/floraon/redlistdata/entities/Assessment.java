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
        this.category = category;
    }

    public void setSubCategory(RedListEnums.CRTags subCategory) {
        this.subCategory = subCategory;
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
}
