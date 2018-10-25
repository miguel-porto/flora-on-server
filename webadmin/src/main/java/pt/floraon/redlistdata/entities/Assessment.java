package pt.floraon.redlistdata.entities;

import pt.floraon.driver.DiffableBean;
import pt.floraon.driver.datatypes.SafeHTMLString;
import pt.floraon.redlistdata.RedListEnums;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static pt.floraon.driver.utils.StringUtils.cleanArray;
import static pt.floraon.driver.utils.StringUtils.isArrayEmpty;

/**
 * Created by miguel on 23-11-2016.
 */
public class Assessment implements DiffableBean {
    private RedListEnums.RedListCategories category;
    private RedListEnums.CRTags subCategory;
    private RedListEnums.AssessmentCriteria[] criteria;
    private SafeHTMLString justification;
    private SafeHTMLString finalJustification;
    private String[] authors;
    private String collaborators;
    private String[] evaluator;
    private String[] reviewer;
    private RedListEnums.YesNoLikelyUnlikely propaguleImmigration;
    private RedListEnums.YesNoLikelyUnlikely decreaseImmigration;
    private RedListEnums.YesNoLikelyUnlikely isSink;
    private RedListEnums.UpDownList upDownListing;
    private SafeHTMLString upDownListingJustification;
    private RedListEnums.TextStatus textStatus;
    private RedListEnums.AssessmentStatus assessmentStatus;
    private RedListEnums.ReviewStatus reviewStatus;
    private RedListEnums.PublicationStatus publicationStatus;
    private RedListEnums.ValidationStatus validationStatus;
    private List<PreviousAssessment> previousAssessmentList;

    public RedListEnums.YesNoLikelyUnlikely getPropaguleImmigration() {
        return propaguleImmigration == null ? RedListEnums.YesNoLikelyUnlikely.NOT_KNOWN : propaguleImmigration;
    }

    public RedListEnums.YesNoLikelyUnlikely getDecreaseImmigration() {
        return decreaseImmigration == null ? RedListEnums.YesNoLikelyUnlikely.NOT_KNOWN : decreaseImmigration;
    }

    public RedListEnums.YesNoLikelyUnlikely getIsSink() {
        return isSink == null ? RedListEnums.YesNoLikelyUnlikely.NOT_KNOWN : isSink;
    }

    /**
     * @return The base category assigned, i.e., before up/downlisting
     */
    public RedListEnums.RedListCategories getCategory() {
        return category;
    }

    public RedListEnums.CRTags getSubCategory() {
        if(category == null) return null;
        return category.isTrigger() ? (subCategory == RedListEnums.CRTags.NO_TAG ? null : subCategory) : null;
    }

    public RedListEnums.AssessmentCriteria[] getCriteria() {
        return isArrayEmpty(criteria) ? new RedListEnums.AssessmentCriteria[0] : criteria;
    }

    /**
     * @return The HTML-formatted fully qualified IUCN category and subcategory
     */
    public String _getCategoryAsString() {
        if(this.getAdjustedCategory() == null) return "";
        String subcat = "";
        if(this.getCategory() != null
                && this.getCategory() == RedListEnums.RedListCategories.CR
                && this.getSubCategory() != null
                && this.getSubCategory() != RedListEnums.CRTags.NO_TAG) {
            subcat = "<sup>*" + this.getSubCategory().toString() + "</sup>";
        }
        return this.getAdjustedCategory().getShortTag() + subcat;
    }

    /**
     * @return The fully qualified IUCN category and subcategory in verbose format
     */
    public String _getCategoryVerboseAsString(boolean htmlFormatted) {
        if(this.getAdjustedCategory() == null) return "";
        String subcat = "";
        if(this.getCategory() != null
                && this.getCategory() == RedListEnums.RedListCategories.CR
                && this.getSubCategory() != null
                && this.getSubCategory() != RedListEnums.CRTags.NO_TAG) {
            if(htmlFormatted)
                subcat = " <span style=\"font-size:0.75em\">(" + this.getSubCategory().getLabel() + ")</span>";
            else
                subcat = " (" + this.getSubCategory().getLabel() + ")";
        }
        return this.getAdjustedCategory().getLabel() + subcat;
    }

    public String _getCriteriaAsString() {
        if(isArrayEmpty(criteria)) return "";
        StringBuilder sb = new StringBuilder();
        int last = 0;
        for (int i = 0; i < criteria.length; i++) {
            if(i > 0 && criteria[i].getCriteria().equals(criteria[i - 1].getCriteria())) {
                if(criteria[i].getSubCriteria().equals(criteria[i - 1].getSubCriteria())) {
                    if(criteria[i].getSubsubCriteria().equals(criteria[i - 1].getSubsubCriteria())) {
                        sb.append("," + criteria[i].getSubsubsubCriteria());
                        last = 3;
                    } else {
                        if(last == 3) sb.append(")");
                        sb.append(criteria[i].getSubsubCriteria())
                                .append(criteria[i].getSubsubsubCriteria().equals("") ? "" : ("(" + criteria[i].getSubsubsubCriteria()));
                        last = criteria[i].getSubsubsubCriteria().equals("") ? 2 : 3;
                    }
                } else {
                    if(last == 3) sb.append(")");
                    sb.append("+").append(criteria[i].getSubCriteria())
                            .append(criteria[i].getSubsubCriteria())
                            .append(criteria[i].getSubsubsubCriteria().equals("") ? "" : ("(" + criteria[i].getSubsubsubCriteria()));
                    last = criteria[i].getSubsubsubCriteria().equals("") ? 1 : 3;
                }
            } else {
                if(last == 3) sb.append(")");
                if (i > 0) sb.append("; ");
                sb.append(criteria[i].getCriteria())
                        .append(criteria[i].getSubCriteria())
                        .append(criteria[i].getSubsubCriteria())
                        .append(criteria[i].getSubsubsubCriteria().equals("") ? "" : ("(" + criteria[i].getSubsubsubCriteria()));
                last = criteria[i].getSubsubsubCriteria().equals("") ? 0 : 3;
            }
        }
        if(last == 3) sb.append(")");
        return sb.toString();
    }

    public SafeHTMLString getJustification() {
        return justification == null ? SafeHTMLString.emptyString() : justification;
    }

    public SafeHTMLString getFinalJustification() {
        return finalJustification == null ?
                new SafeHTMLString(getJustification().isEmpty()
                        ? getUpDownListingJustification().toString()
                        : (getJustification().toString() + " " + getUpDownListingJustification()))
                : finalJustification;
    }

    public RedListEnums.UpDownList getUpDownListing() {
        return upDownListing == null ? RedListEnums.UpDownList.NONE : upDownListing;
    }

    public SafeHTMLString getUpDownListingJustification() {
        return upDownListingJustification == null ? SafeHTMLString.emptyString() : upDownListingJustification;
    }

    public String[] getAuthors() {
        return isArrayEmpty(authors) ? new String[0] : authors;
    }

    public String getCollaborators() {
        return collaborators == null ? "" : collaborators;
    }

    public String[] getEvaluator() {
        return isArrayEmpty(evaluator) ? new String[0] : evaluator;
    }

    public String[] getReviewer() {
        return isArrayEmpty(reviewer) ? new String[0] : reviewer;
    }

    public boolean containsAuthor(String userId) {
        return Arrays.asList(this.getAuthors()).contains(userId);
    }

    public boolean containsReviewer(String userId) {
        return Arrays.asList(this.getReviewer()).contains(userId);
    }

    public boolean containsEvaluator(String userId) {
        return Arrays.asList(this.getEvaluator()).contains(userId);
    }

    /**
     * Only returns evaluated if the texts are marked as ready.
     * @return
     */
    public RedListEnums.AssessmentStatus getAssessmentStatus() {
        return assessmentStatus == null ? RedListEnums.AssessmentStatus.NOT_EVALUATED :
                (assessmentStatus == RedListEnums.AssessmentStatus.PRELIMINARY && getTextStatus() != RedListEnums.TextStatus.READY
                ? RedListEnums.AssessmentStatus.NOT_EVALUATED : assessmentStatus);
    }

    public RedListEnums.TextStatus getTextStatus() {
        return textStatus == null ? RedListEnums.TextStatus.NO_TEXT : textStatus;
    }

    public RedListEnums.ReviewStatus getReviewStatus() {
        return reviewStatus == null ? RedListEnums.ReviewStatus.NOT_REVISED : reviewStatus;
    }

    public RedListEnums.PublicationStatus getPublicationStatus() {
        return publicationStatus == null ? RedListEnums.PublicationStatus.NOT_PUBLISHED : publicationStatus;
    }

    public RedListEnums.ValidationStatus getValidationStatus() {
        return validationStatus == null ? RedListEnums.ValidationStatus.IN_ANALYSIS : validationStatus;
    }

    public void setValidationStatus(RedListEnums.ValidationStatus validationStatus) {
        this.validationStatus = validationStatus;
    }

    public List<PreviousAssessment> getPreviousAssessmentList() {
        return this.previousAssessmentList == null ? Collections.<PreviousAssessment>emptyList() : this.previousAssessmentList;
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

    public void setCriteria(RedListEnums.AssessmentCriteria[] criteria) {
        this.criteria = criteria;
    }

    public void setJustification(SafeHTMLString justification) {
        this.justification = justification;
    }

    public void setFinalJustification(SafeHTMLString finalJustification) {
        this.finalJustification = finalJustification;
    }

    public void setAuthors(String[] authors) {
        this.authors = cleanArray(authors);
    }

    public void setCollaborators(String collaborators) {
        this.collaborators = collaborators;
    }

    public void setEvaluator(String[] evaluator) {
        this.evaluator = cleanArray(evaluator);
    }

    public void setReviewer(String[] reviewer) {
        this.reviewer = cleanArray(reviewer);
    }

    public void setUpDownListing(RedListEnums.UpDownList upDownListing) {
        this.upDownListing = upDownListing;
    }

    public void setUpDownListingJustification(SafeHTMLString upDownListingJustification) {
        this.upDownListingJustification = upDownListingJustification;
    }

    public void setAssessmentStatus(RedListEnums.AssessmentStatus assessmentStatus) {
        this.assessmentStatus = assessmentStatus;
    }

    public void setTextStatus(RedListEnums.TextStatus textStatus) {
        this.textStatus = textStatus;
    }

    public void setReviewStatus(RedListEnums.ReviewStatus reviewStatus) {
        this.reviewStatus = reviewStatus;
    }

    public void setPublicationStatus(RedListEnums.PublicationStatus publicationStatus) {
        this.publicationStatus = publicationStatus;
    }

    public void setPreviousAssessmentList(List<PreviousAssessment> previousAssessmentList) {
        this.previousAssessmentList = previousAssessmentList;
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
        if(upDownListing == null) return category;
        switch(upDownListing) {
            case UPLIST: return category.getUplistCategory();
            case DOWNLIST: return category.getDownlistCategory();
            default: return category;
        }
    }

    public void addPreviousAssessment(int year, RedListEnums.RedListCategories category) {
        if(this.previousAssessmentList == null || this.previousAssessmentList.size() == 0)
            this.previousAssessmentList = new ArrayList<PreviousAssessment>();
        this.previousAssessmentList.add(new PreviousAssessment(year, category));
    }

    /**
     * Gets the status of this taxon, assuming the sequential order:
     * PublicationStatus.PUBLISHED => ReviewStatus.REVISED_PUBLISHING => AssessmentStatus.PRELIMINARY => TextStatus.READY
     * @return
     */
    public String[] fetchSequentialAssessmentStatus() {
        if(this.getPublicationStatus() == RedListEnums.PublicationStatus.PUBLISHED)
            return new String[] {this.getPublicationStatus().getLabel(), "EmptyString"};
        // not published
        if(this.getReviewStatus() != RedListEnums.ReviewStatus.NOT_REVISED)
            return new String[] {this.getAssessmentStatus().getLabel(), this.getReviewStatus().getLabel()};
        // not revised
        if(this.getAssessmentStatus() == RedListEnums.AssessmentStatus.PRELIMINARY)
            return new String[] {this.getAssessmentStatus().getLabel(), this.getReviewStatus().getLabel()};
        // not assessed
        if(this.getTextStatus() == RedListEnums.TextStatus.READY)
            return new String[] {this.getTextStatus().getLabel(), this.getAssessmentStatus().getLabel()};
        // texts not ready
        if(this.textStatus == null)
            return new String[] {"EmptyString", "EmptyString"};
        else
            return new String[] {this.getTextStatus().getLabel(), "EmptyString"};

/*
        if(this.reviewStatus != null && this.reviewStatus != RedListEnums.ReviewStatus.REVISED_PUBLISHING)
            return new String[] {this.assessmentStatus == null ? "EmptyString" : this.assessmentStatus.getLabel(), this.reviewStatus.getLabel()};
        if(this.textStatus != null && this.textStatus != RedListEnums.TextStatus.READY)
            return new String[] {this.textStatus.getLabel(), "EmptyString"};
        if(this.assessmentStatus != null)
            return new String[] {this.textStatus == null ? "EmptyString" : this.textStatus.getLabel(), this.assessmentStatus.getLabel()};
        return this.publicationStatus == null ? null
            : new String[] {this.reviewStatus == null ? "EmptyString" : this.reviewStatus.getLabel(), this.publicationStatus.getLabel()};
*/
    }
}
