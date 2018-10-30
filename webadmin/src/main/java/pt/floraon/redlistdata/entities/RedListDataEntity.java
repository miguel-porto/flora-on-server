package pt.floraon.redlistdata.entities;

import com.arangodb.velocypack.annotations.Expose;
import pt.floraon.driver.DiffableBean;
import pt.floraon.driver.Constants;
import pt.floraon.driver.datatypes.IntegerInterval;
import pt.floraon.driver.datatypes.SafeHTMLString;
import pt.floraon.driver.entities.GeneralDBNode;
import pt.floraon.driver.interfaces.Flaggable;
import pt.floraon.driver.utils.StringUtils;
import pt.floraon.redlistdata.RedListEnums;
import pt.floraon.taxonomy.entities.TaxEnt;
import pt.floraon.driver.results.InferredStatus;

import java.text.ParseException;
import java.util.*;

import static pt.floraon.driver.Constants.*;

/**
 * A JavaBean representing all the data fields for the red list sheets. There can be only one sheet per TaxEnt per
 * territory.
 * Thanks to André Carapeto, who carefully designed all the fields needed for this class and its subclasses!
 * Created by Miguel Porto & André Carapeto on 11-11-2016.
 */
public class RedListDataEntity extends GeneralDBNode implements DiffableBean, Flaggable {
    /**
     * The full TaxEnt database entity. Note this is not stored in the DB, must be fetched by {@link RedListDataEntity#taxEntID}
     */
    @Expose(serialize = false, deserialize = true)
    private TaxEnt taxEnt;

    @Expose(serialize = false, deserialize = false)
    private transient Set<String> responsibleAuthors_Texts;
    @Expose(serialize = false, deserialize = false)
    private transient Set<String> responsibleAuthors_Assessment;
    @Expose(serialize = false, deserialize = false)
    private transient Set<String> responsibleAuthors_Revision;
    @Expose(serialize = false, deserialize = false)
    private transient boolean flag = false;

    /**
     * The ID of the TaxEnt
     */
    private String taxEntID;
    /**
     * The inferred statuses of this TaxEnt in this territory. This is computed upon initialization and stored.
     * Changes in the checklist must be synced.
     */
    private InferredStatus inferredStatus;
    /**
     * Arbitrary textual tags used for filtering the whole list
     */
    private String[] tags;
    /**
     * Whether the taxon has taxonomic problems
     */
//    private boolean hasTaxonomicProblems;
    /**
     * Textual decription of the taxonomic problems
     */
    private SafeHTMLString taxonomicProblemDescription;
    /**
     * The fields pertaining to the geographical distribution
     */
    private GeographicalDistribution geographicalDistribution = new GeographicalDistribution();
    /**
     * The fields pertaining to the population
     */
    private Population population = new Population();
    /**
     * The fields pertaining to the ecology
     */
    private Ecology ecology = new Ecology();
    /**
     * The fields pertaining to the uses and trade
     */
    private UsesAndTrade usesAndTrade = new UsesAndTrade();
    /**
     * The fields pertaining to the threats
     */
    private Threats threats = new Threats();
    /**
     * The fields pertaining to conservation measures
     */
    private Conservation conservation = new Conservation();
    /**
     * The fields pertaining to red list assessment
     */
    private Assessment assessment = new Assessment();

    private String dateAssessed;

    private String datePublished;
    /**
     * The history of all changes made to this data sheet
     */
    private List<Revision> revisions;

    private SafeHTMLString reviewerComments, replyToReviewer, otherInformation, validationComments, replyToValidation;

    private String coverPhotoUrl;

    public RedListDataEntity() {
    }

    public RedListDataEntity(String taxEntID, InferredStatus inferredStatus) {
        this.taxEntID = taxEntID;
        this.inferredStatus = inferredStatus;
    }

    public TaxEnt getTaxEnt() {
        return taxEnt;
    }

    public void setTaxEnt(TaxEnt taxEnt) {
        this.taxEnt = taxEnt;
    }

    public InferredStatus getInferredStatus() {
        return inferredStatus;
    }

    public void setInferredStatus(InferredStatus inferredStatus) {
        this.inferredStatus = inferredStatus;
    }

    public String getTaxEntID() {
        return taxEntID;
    }

    public String[] getTags() {
        return tags == null ? new String[0] : tags;
    }

    public String[] _getHTMLEscapedTags() {
        if(tags == null) return new String[0];
        List<String> out = new ArrayList<>();
        for(String s : tags)
            if(s != null) out.add(StringUtils.sanitizeHtmlId(s));
        return out.toArray(new String[out.size()]);
    }

    @Override
    public boolean _getFlag() {
        return this.flag;
    }

    @Override
    public void _setFlag(boolean flag) {
        this.flag = flag;
    }

    public String _getSingleLetterTag() {
        // TODO: this is not general!! It depends on user tags.
        if(StringUtils.isArrayEmpty(tags)) return "";
        for(String t : tags) {
            if(t == null) break;
            if (t.toLowerCase().equals("lista alvo")) return "A";
            if (t.toLowerCase().equals("lista b")) return "B";
            if (t.toLowerCase().equals("lista preliminar")) return "P";
        }
        return "";
    }

    public SafeHTMLString getTaxonomicProblemDescription() {
        return taxonomicProblemDescription == null ? new SafeHTMLString("") : taxonomicProblemDescription;
    }

    public SafeHTMLString getReviewerComments() {
        return reviewerComments == null ? new SafeHTMLString("") : reviewerComments;
    }

    public SafeHTMLString getValidationComments() {
        return validationComments == null ? new SafeHTMLString("") : validationComments;
    }

    public SafeHTMLString getReplyToReviewer() {
        return replyToReviewer == null ? new SafeHTMLString("") : replyToReviewer;
    }

    public SafeHTMLString getReplyToValidation() {
        return replyToValidation == null ? new SafeHTMLString("") : replyToValidation;
    }

    public String getCoverPhotoUrl() {
        return coverPhotoUrl;
    }

    public SafeHTMLString getOtherInformation() {
        return otherInformation == null ? new SafeHTMLString("") : otherInformation;
    }

    public GeographicalDistribution getGeographicalDistribution() {
        return geographicalDistribution;
    }

    public Population getPopulation() {
        return population;
    }

    public Ecology getEcology() { return ecology; }

    public UsesAndTrade getUsesAndTrade() { return usesAndTrade; }

    public Threats getThreats() { return threats;}

    public Conservation getConservation() { return conservation; }

    public Assessment getAssessment() { return assessment; }

    public String getDateAssessed() {
        return dateAssessed;
    }

    public String getDatePublished() {
        return datePublished;
    }

    public Integer _getYearPublished() {
        if(datePublished == null) return null;
        try {
            Calendar cal = new GregorianCalendar();
            cal.setTime(dateTimeFormat.get().parse(datePublished));
            return cal.get(Calendar.YEAR);
        } catch (ParseException e) {
            return null;
        }
    }

    public void setTaxEntID(String taxEntID) {
        this.taxEntID = taxEntID;
    }

    public void setTags(String[] tags) {
        this.tags = StringUtils.cleanArray(tags, false);
    }

    public void setTaxonomicProblemDescription(SafeHTMLString taxonomicProblemDescription) {
        this.taxonomicProblemDescription = taxonomicProblemDescription;
    }

    public void setReviewerComments(SafeHTMLString reviewerComments) {
        this.reviewerComments = reviewerComments;
    }

    public void setValidationComments(SafeHTMLString validationComments) {
        this.validationComments = validationComments;
    }

    public void setReplyToReviewer(SafeHTMLString replyToReviewer) {
        this.replyToReviewer = replyToReviewer;
    }

    public void setReplyToValidation(SafeHTMLString replyToValidation) {
        this.replyToValidation = replyToValidation;
    }

    public void setCoverPhotoUrl(String coverPhotoUrl) {
        this.coverPhotoUrl = coverPhotoUrl;
    }

    public void setOtherInformation(SafeHTMLString otherInformation) {
        this.otherInformation = otherInformation;
    }

    /*******
     * GeographicalDistribution fields
     *******/

    public void setGeographicalDistribution_Description(String description) {
        this.geographicalDistribution.setDescription(new SafeHTMLString(description));
    }

    public void setGeographicalDistribution_EOO(Double EOO) {
        this.geographicalDistribution.setEOO(EOO);
    }

    public void setGeographicalDistribution_AOO(Double AOO) {
        this.geographicalDistribution.setAOO(AOO);
    }

    public void setGeographicalDistribution_historicalEOO(Double EOO) {
        this.geographicalDistribution.setHistoricalEOO(EOO);
    }

    public void setGeographicalDistribution_historicalAOO(Double AOO) {
        this.geographicalDistribution.setHistoricalAOO(AOO);
    }

    public void setGeographicalDistribution_DeclineDistribution(String declineDistribution) {
        try {
            this.geographicalDistribution.setDeclineDistribution(RedListEnums.DeclineDistribution.valueOf(declineDistribution));
        } catch (IllegalArgumentException e) {
            this.geographicalDistribution.setDeclineDistribution(RedListEnums.DeclineDistribution.NO_INFORMATION);
        }
    }

    public void setGeographicalDistribution_DeclineDistributionJustification(String declineDistributionJustification) {
        this.getGeographicalDistribution().setDeclineDistributionJustification(new SafeHTMLString(declineDistributionJustification));
    }

    public void setGeographicalDistribution_ExtremeFluctuations(String extremeFluctuations) {
        try {
            this.geographicalDistribution.setExtremeFluctuations(RedListEnums.ExtremeFluctuations.valueOf(extremeFluctuations));
        } catch (IllegalArgumentException e) {
            this.geographicalDistribution.setExtremeFluctuations(RedListEnums.ExtremeFluctuations.NO_INFORMATION);
        }
    }

    public void setGeographicalDistribution_ElevationRange(Integer[] elevationRange) {
        this.geographicalDistribution.setElevationRange(elevationRange);
    }

    /*******
     * Population fields
     *******/

    public void setPopulation_Description(String description) {
        this.population.setDescription(new SafeHTMLString(description));
    }

    public void setPopulation_PopulationTrend(String populationTrend) {
        this.population.setPopulationTrend(new IntegerInterval(populationTrend));
    }

/*
    public void setPopulation_PopulationSizeReduction(String populationSizeReduction) {
        try {
            this.population.setPopulationSizeReduction(RedListEnums.PopulationSizeReduction.valueOf(populationSizeReduction));
        } catch (IllegalArgumentException e) {
            this.population.setPopulationSizeReduction(RedListEnums.PopulationSizeReduction.NO_INFORMATION);
        }
    }
*/

    public void setPopulation_PopulationSizeReduction(String[] populationSizeReduction) {
        this.population.setPopulationSizeReduction(StringUtils.stringArrayToEnumArray(populationSizeReduction, RedListEnums.PopulationSizeReduction.class));
    }


    public void setPopulation_PopulationSizeReductionJustification(String populationSizeReductionJustification) {
        this.population.setPopulationSizeReductionJustification(new SafeHTMLString(populationSizeReductionJustification));
    }

    public void setPopulation_NrMatureIndividualsCategory(String nrMatureIndividualsCategory) {
        try {
            this.population.setNrMatureIndividualsCategory(RedListEnums.NrMatureIndividuals.valueOf(nrMatureIndividualsCategory));
        } catch (IllegalArgumentException e) {
            this.population.setNrMatureIndividualsCategory(RedListEnums.NrMatureIndividuals.NO_DATA);
        }
    }

    public void setPopulation_NrMatureIndividualsExact(String nrMatureIndividualsExact) {
        this.population.setNrMatureIndividualsExact(new IntegerInterval(nrMatureIndividualsExact));
    }

    public void setPopulation_NrMatureIndividualsDescription(String nrMatureIndividualsDescription) {
        this.population.setNrMatureIndividualsDescription(new SafeHTMLString(nrMatureIndividualsDescription));
    }

    public void setPopulation_TypeOfEstimate(String typeOfEstimate) {
        try {
            this.population.setTypeOfEstimate(RedListEnums.TypeOfPopulationEstimate.valueOf(typeOfEstimate));
        } catch (IllegalArgumentException e) {
            this.population.setTypeOfEstimate(RedListEnums.TypeOfPopulationEstimate.NO_DATA);
        }
    }

    public void setPopulation_PopulationDecline(String populationDecline) {
        try {
            this.population.setPopulationDecline(RedListEnums.DeclinePopulation.valueOf(populationDecline));
        } catch (IllegalArgumentException e) {
            this.population.setPopulationDecline(RedListEnums.DeclinePopulation.NO_INFORMATION);
        }
    }

    public void setPopulation_PopulationDeclinePercent(String populationDeclinePercent) {
        this.population.setPopulationDeclinePercent(new IntegerInterval(populationDeclinePercent));
    }

    public void setPopulation_PopulationDeclineJustification(String populationDeclineJustification) {
        this.population.setPopulationDeclineJustification(new SafeHTMLString(populationDeclineJustification));
    }

    public void setPopulation_SeverelyFragmented(String severelyFragmented) {
        try {
            this.population.setSeverelyFragmented(RedListEnums.SeverelyFragmented.valueOf(severelyFragmented));
        } catch (IllegalArgumentException e) {
            this.population.setSeverelyFragmented(RedListEnums.SeverelyFragmented.NO_INFORMATION);
        }
    }

    public void setPopulation_SeverelyFragmentedJustification(String populationSeverelyFragmentedJustification) {
        this.population.setSeverelyFragmentedJustification(new SafeHTMLString(populationSeverelyFragmentedJustification));
    }

    public void setPopulation_ExtremeFluctuations(String extremeFluctuations) {
        try {
            this.population.setExtremeFluctuations(RedListEnums.YesNoNA.valueOf(extremeFluctuations));
        } catch (IllegalArgumentException e) {
            this.population.setExtremeFluctuations(RedListEnums.YesNoNA.NO_DATA);
        }
    }

    public void setPopulation_ExtremeFluctuationsJustification(String extremeFluctuationsJustification) {
        this.population.setExtremeFluctuationsJustification(new SafeHTMLString(extremeFluctuationsJustification));
    }

    public void setPopulation_NrMatureEachSubpop(String nrMatureEachSubpop) {
        try {
            this.population.setNrMatureEachSubpop(RedListEnums.NrMatureEachSubpop.valueOf(nrMatureEachSubpop));
        } catch (IllegalArgumentException e) {
            this.population.setNrMatureEachSubpop(RedListEnums.NrMatureEachSubpop.NO_DATA);
        }
    }

    public void setPopulation_PercentMatureOneSubpop(String percentMatureOneSubpop) {
        try {
            this.population.setPercentMatureOneSubpop(RedListEnums.PercentMatureOneSubpop.valueOf(percentMatureOneSubpop));
        } catch (IllegalArgumentException e) {
            this.population.setPercentMatureOneSubpop(RedListEnums.PercentMatureOneSubpop.NO_DATA);
        }
    }

    /*******
     * Ecology fields
     *******/

    public void setEcology_Description(String description) {
        this.ecology.setDescription(new SafeHTMLString(description));
    }

    public void setEcology_HabitatTypes(String[] habitatTypes) {
        this.ecology.setHabitatTypes(habitatTypes);
    }

    public void setEcology_GenerationLength(String generationLength) {
        this.ecology.setGenerationLength(new IntegerInterval(generationLength));
    }

    public void setEcology_GenerationLengthJustification(String generationLengthJustification) {
        this.ecology.setGenerationLengthJustification(new SafeHTMLString(generationLengthJustification));
    }

    public void setEcology_DeclineHabitatQuality(String declineHabitatQuality) {
        try {
            this.ecology.setDeclineHabitatQuality(RedListEnums.DeclineHabitatQuality.valueOf(declineHabitatQuality));
        } catch (IllegalArgumentException e) {
            this.ecology.setDeclineHabitatQuality(RedListEnums.DeclineHabitatQuality.NO_INFORMATION);
        }
    }

    public void setEcology_DeclineHabitatQualityJustification(String declineHabitatQualityJustification) {
        this.ecology.setDeclineHabitatQualityJustification(new SafeHTMLString(declineHabitatQualityJustification));
    }

    /*******
     * Uses and trade fields
     *******/

    public void setUsesAndTrade_Description(String description) {
        this.getUsesAndTrade().setDescription(new SafeHTMLString(description));
    }

    public void setUsesAndTrade_Uses(String[] uses) {
/*
        List<RedListEnums.Uses> tmp = new ArrayList<>();
        for(String s : uses) {
            try {
                tmp.add(RedListEnums.Uses.valueOf(s));
            } catch (IllegalArgumentException e) {
                Log.warn("Use " + (s.length() == 0 ? "<empty>" : s) + " not found");
            }
        }
        this.getUsesAndTrade().setUses(tmp.toArray(new RedListEnums.Uses[0]));
*/
        this.getUsesAndTrade().setUses(StringUtils.stringArrayToEnumArray(uses, RedListEnums.Uses.class));
    }

    public void setUsesAndTrade_Traded(boolean traded) {
        this.getUsesAndTrade().setTraded(traded);
    }

    public void setUsesAndTrade_Overexploitation(String overexploitation) {
        try {
            this.getUsesAndTrade().setOverexploitation(RedListEnums.Overexploitation.valueOf(overexploitation));
        } catch (IllegalArgumentException e) {
            this.getUsesAndTrade().setOverexploitation(RedListEnums.Overexploitation.NO_DATA);
        }
    }

    /*******
     * Threat fields
     *******/

    public void setThreats_Description(String description) {
        this.getThreats().setDescription(new SafeHTMLString(description));
    }

    public void setThreats_NumberOfLocations(String numberOfLocations) {
        this.getThreats().setNumberOfLocations(new IntegerInterval(numberOfLocations));
    }

    public void setThreats_NumberOfLocationsJustification(String numberOfLocationsJustification) {
        this.getThreats().setNumberOfLocationsJustification(new SafeHTMLString(numberOfLocationsJustification));
    }

    public void setThreats_DeclineNrLocations(String declineNrLocations) {
        try {
            this.threats.setDeclineNrLocations(RedListEnums.DeclineNrLocations.valueOf(declineNrLocations));
        } catch (IllegalArgumentException e) {
            this.threats.setDeclineNrLocations(RedListEnums.DeclineNrLocations.NO_INFORMATION);
        }
    }

    public void setThreats_DeclineNrLocationsJustification(String declineNrLocationsJustification) {
        this.threats.setDeclineNrLocationsJustification(new SafeHTMLString(declineNrLocationsJustification));
    }

    public void setThreats_ExtremeFluctuationsNrLocations(String extremeFluctuationsNrLocations) {
        try {
            this.threats.setExtremeFluctuationsNrLocations(RedListEnums.YesNoNA.valueOf(extremeFluctuationsNrLocations));
        } catch (IllegalArgumentException e) {
            this.threats.setExtremeFluctuationsNrLocations(RedListEnums.YesNoNA.NO_DATA);
        }
    }

    public void setThreats_ExtremeFluctuationsNrLocationsJustification(String extremeFluctuationsNrLocationsJustification) {
        this.threats.setExtremeFluctuationsNrLocationsJustification(new SafeHTMLString(extremeFluctuationsNrLocationsJustification));
    }

    public void setThreats_Threats(String[] threats) {
        this.threats.setThreats(StringUtils.stringArrayToEnumArray(threats, RedListEnums.Threats.class));
    }

    /*******
     * Conservation fields
     *******/

    public void setConservation_Description(String description) {
        this.conservation.setDescription(new SafeHTMLString(description));
    }

    public void setConservation_LegalProtection(String[] legalProtection) {
        this.conservation.setLegalProtection(legalProtection);
    }

    public void setConservation_ConservationPlans(String conservationPlans) {
        try {
            this.conservation.setConservationPlans(RedListEnums.YesNoNA.valueOf(conservationPlans));
        } catch (IllegalArgumentException e) {
            this.conservation.setConservationPlans(RedListEnums.YesNoNA.NO_DATA);
        }
    }

    public void setConservation_ExSituConservation(String exSituConservation) {
        try {
            this.conservation.setExSituConservation(RedListEnums.YesNoNA.valueOf(exSituConservation));
        } catch (IllegalArgumentException e) {
            this.conservation.setExSituConservation(RedListEnums.YesNoNA.NO_DATA);
        }
    }

    public void setConservation_ProposedConservationActions(String[] proposedConservationActions) {
/*
        List<RedListEnums.ProposedConservationActions> tmp = new ArrayList<>();
        for(String s : proposedConservationActions) {
            try {
                tmp.add(RedListEnums.ProposedConservationActions.valueOf(s));
            } catch (IllegalArgumentException e) {
                Log.warn("Conservation action "+s+" not found");
            }
        }
        this.conservation.setProposedConservationActions(tmp.toArray(new RedListEnums.ProposedConservationActions[0]));
*/
        this.conservation.setProposedConservationActions(StringUtils.stringArrayToEnumArray(proposedConservationActions, RedListEnums.ProposedConservationActions.class));
    }

    public void setConservation_ProposedStudyMeasures(String[] proposedStudyMeasures) {
/*
        List<RedListEnums.ProposedStudyMeasures> tmp = new ArrayList<>();
        for(String s : proposedStudyMeasures) {
            try {
                tmp.add(RedListEnums.ProposedStudyMeasures.valueOf(s));
            } catch (IllegalArgumentException e) {
                Log.warn("Study measure "+s+" not found");
            }
        }
        this.conservation.setProposedStudyMeasures(tmp.toArray(new RedListEnums.ProposedStudyMeasures[0]));
*/
        this.conservation.setProposedStudyMeasures(StringUtils.stringArrayToEnumArray(proposedStudyMeasures, RedListEnums.ProposedStudyMeasures.class));
    }

    public void setConservation_ConservationPlansJustification(String conservationPlansJustification) {
        this.conservation.setConservationPlansJustification(new SafeHTMLString(conservationPlansJustification));
    }

    public void setConservation_ExSituConservationJustification(String exSituConservationJustification) {
        this.conservation.setExSituConservationJustification(new SafeHTMLString(exSituConservationJustification));
    }

    /*******
     * Assessment fields
     *******/

    public void setAssessment_Category(String category) {
        this.assessment.setCategory(RedListEnums.RedListCategories.valueOf(category));
    }

    public void setAssessment_SubCategory(String subCategory) {
        try {
            this.assessment.setSubCategory(RedListEnums.CRTags.valueOf(subCategory));
        } catch (IllegalArgumentException e) {
            this.assessment.setSubCategory(RedListEnums.CRTags.NO_TAG);
        }
    }

    public void setAssessment_PropaguleImmigration(String propaguleImmigration) {
        try {
            this.assessment.setPropaguleImmigration(RedListEnums.YesNoLikelyUnlikely.valueOf(propaguleImmigration));
        } catch (IllegalArgumentException e) {
            this.assessment.setPropaguleImmigration(RedListEnums.YesNoLikelyUnlikely.NOT_KNOWN);
        }
    }

    public void setAssessment_DecreaseImmigration(String decreaseImmigration) {
        try {
            this.assessment.setDecreaseImmigration(RedListEnums.YesNoLikelyUnlikely.valueOf(decreaseImmigration));
        } catch (IllegalArgumentException e) {
            this.assessment.setDecreaseImmigration(RedListEnums.YesNoLikelyUnlikely.NOT_KNOWN);
        }
    }

    public void setAssessment_IsSink(String isSink) {
        try {
            this.assessment.setIsSink(RedListEnums.YesNoLikelyUnlikely.valueOf(isSink));
        } catch (IllegalArgumentException e) {
            this.assessment.setIsSink(RedListEnums.YesNoLikelyUnlikely.NOT_KNOWN);
        }
    }

    public void setAssessment_UpDownListing(String upDownListing) {
        try {
            this.assessment.setUpDownListing(RedListEnums.UpDownList.valueOf(upDownListing));
        } catch (IllegalArgumentException e) {
            this.assessment.setUpDownListing(RedListEnums.UpDownList.NONE);
        }
    }

    public void setAssessment_UpDownListingJustification(String upDownListingJustification) {
        this.assessment.setUpDownListingJustification(new SafeHTMLString(upDownListingJustification));
    }

    public void setAssessment_Criteria(String[] criteria) {
        this.getAssessment().setCriteria(StringUtils.stringArrayToEnumArray(criteria, RedListEnums.AssessmentCriteria.class));
    }

    public void setAssessment_Justification(String justification) {
        this.assessment.setJustification(new SafeHTMLString(justification));
    }

    public void setAssessment_FinalJustification(String finalJustification) {
        this.assessment.setFinalJustification(new SafeHTMLString(finalJustification));
    }

    public void setAssessment_PreviousAssessmentListYear(Integer[] previousAssessmentListYear) {
        List<PreviousAssessment> out;
        if(this.getAssessment().getPreviousAssessmentList() == null || this.getAssessment().getPreviousAssessmentList().size() == 0) {
            out = new ArrayList<>();
            PreviousAssessment tmp;
            for (int i = 0; i < previousAssessmentListYear.length; i++) {
                tmp = new PreviousAssessment();
                tmp.setYear(previousAssessmentListYear[i]);
                out.add(tmp);
            }
        } else {
            out = this.getAssessment().getPreviousAssessmentList();
            for (int i = 0; i < previousAssessmentListYear.length; i++) {
                out.get(i).setYear(previousAssessmentListYear[i]);
            }
        }
        this.getAssessment().setPreviousAssessmentList(out);
    }

    public void setAssessment_PreviousAssessmentListCategory(String[] previousAssessmentListCategory) {
        List<PreviousAssessment> out;
        if (this.getAssessment().getPreviousAssessmentList() == null || this.getAssessment().getPreviousAssessmentList().size() == 0) {
            out = new ArrayList<>();
            PreviousAssessment tmp;
            for (String aPreviousAssessmentListCategory : previousAssessmentListCategory) {
                tmp = new PreviousAssessment();
                if (aPreviousAssessmentListCategory != null && !aPreviousAssessmentListCategory.trim().equals("")) {
                    try {
                        tmp.setCategory(RedListEnums.RedListCategories.valueOf(aPreviousAssessmentListCategory));
                    } catch (IllegalArgumentException e) {
                    }
                }
                out.add(tmp);
            }
        } else {    // TODO HERE blank category
            out = this.getAssessment().getPreviousAssessmentList();
            for (int i = 0; i < previousAssessmentListCategory.length; i++) {
                if(previousAssessmentListCategory[i] != null && !previousAssessmentListCategory[i].trim().equals("")) {
                    try {
                        out.get(i).setCategory(RedListEnums.RedListCategories.valueOf(previousAssessmentListCategory[i]));
                    } catch (IllegalArgumentException e) {
                    }
                }
            }
        }
        this.getAssessment().setPreviousAssessmentList(out);
    }

    public void setAssessment_Authors(String[] authors) {
        this.assessment.setAuthors(authors);
    }

    public void setAssessment_Collaborators(String collaborators) {
        this.assessment.setCollaborators(collaborators);
    }

    public void setAssessment_Evaluator(String[] evaluator) {
        this.assessment.setEvaluator(evaluator);
    }

    public void setAssessment_Reviewer(String[] reviewer) {
        this.assessment.setReviewer(reviewer);
    }

    public void setAssessment_AssessmentStatus(String assessmentStatus) {
        try {
            this.assessment.setAssessmentStatus(RedListEnums.AssessmentStatus.valueOf(assessmentStatus));
        } catch (IllegalArgumentException e) {
            this.assessment.setAssessmentStatus(RedListEnums.AssessmentStatus.NOT_EVALUATED);
        }
    }

    public void setAssessment_TextStatus(String textStatus) {
        try {
            this.assessment.setTextStatus(RedListEnums.TextStatus.valueOf(textStatus));
        } catch (IllegalArgumentException e) {
            this.assessment.setTextStatus(RedListEnums.TextStatus.NO_TEXT);
        }
    }

    public void setAssessment_ReviewStatus(String reviewStatus) {
        try {
            this.assessment.setReviewStatus(RedListEnums.ReviewStatus.valueOf(reviewStatus));
        } catch (IllegalArgumentException e) {
            this.assessment.setReviewStatus(RedListEnums.ReviewStatus.NOT_REVISED);
        }
    }

    public void setAssessment_PublicationStatus(String publicationStatus) {
        try {
            this.assessment.setPublicationStatus(RedListEnums.PublicationStatus.valueOf(publicationStatus));
        } catch (IllegalArgumentException e) {
            this.assessment.setPublicationStatus(RedListEnums.PublicationStatus.NOT_PUBLISHED);
        }
    }

    public void setAssessment_ValidationStatus(String validationStatus) {
        try {
            this.assessment.setValidationStatus(RedListEnums.ValidationStatus.valueOf(validationStatus));
        } catch (IllegalArgumentException e) {
            this.assessment.setValidationStatus(RedListEnums.ValidationStatus.IN_ANALYSIS);
        }
    }

    public void setDateAssessed(String dateAssessed) {
        this.dateAssessed = dateAssessed;
    }

    public void setDatePublished(String datePublished) {
        this.datePublished = datePublished;
    }

    /**
     * Updates the date assessed with the current date and time
     */
    public void updateDateAssessed() {
        this.dateAssessed = dateTimeFormat.get().format(new Date());
    }

    /**
     * Updates the date published with the current date and time
     */
    public void updateDatePublished() {
        this.datePublished = dateTimeFormat.get().format(new Date());
    }

    public void addRevision(String user) {
        if(this.revisions == null || this.revisions.size() == 0) this.revisions = new ArrayList<Revision>();
        this.revisions.add(new Revision(user));
//        for(Revision r : this.revisions)
//            System.out.println(r.getDateTimeSaved().toString()+" "+r.getUser());
    }

    public List<Revision> getRevisions() {
        return this.revisions == null ? Collections.<Revision>emptyList() : this.revisions;
    }

    public void setRevisions(List<Revision> revisions) {
        this.revisions = revisions;
    }

    public void addResponsibleForTexts(String id) {
        if(this.responsibleAuthors_Texts == null)
            this.responsibleAuthors_Texts = new HashSet<>();
        this.responsibleAuthors_Texts.add(id);
    }

    public void addResponsibleForAssessment(String id) {
        if(this.responsibleAuthors_Assessment == null)
            this.responsibleAuthors_Assessment = new HashSet<>();
        this.responsibleAuthors_Assessment.add(id);
    }

    public void addResponsibleForRevision(String id) {
        if(this.responsibleAuthors_Revision == null)
            this.responsibleAuthors_Revision = new HashSet<>();
        this.responsibleAuthors_Revision.add(id);
    }

    public boolean hasResponsibleForTexts() {
        return this.responsibleAuthors_Texts != null && this.responsibleAuthors_Texts.size() > 0;
    }

    public Set<String> getResponsibleAuthors_Texts() {
        return responsibleAuthors_Texts;
    }

    public Set<String> getResponsibleAuthors_Assessment() {
        return responsibleAuthors_Assessment;
    }

    public Set<String> getResponsibleAuthors_Revision() {
        return responsibleAuthors_Revision;
    }

    /**
     * Checks whether the assigned assessment criteria are valid, taking into account the info of the fields.
     * @return
     */
    public List<String> validateCriteria() {
        List<String> warns = new ArrayList<>();
        Population pop = getPopulation();
        GeographicalDistribution dist = getGeographicalDistribution();
        Threats thr = getThreats();
        Set<String> alc = new HashSet<>();
        Set<String> critB = new HashSet<>();
        RedListEnums.RedListCategories cat = getAssessment().getCategory();
        List<RedListEnums.PopulationSizeReduction> psr = pop._getPopulationSizeReductionAsList();

        for(RedListEnums.AssessmentCriteria cr : getAssessment().getCriteria()) {
            // first check the validity of the 5 major criteria
            switch(cr.getCriteria()) {
                case "A":
                    if(alc.contains("A")) break;
                    if(psr.contains(RedListEnums.PopulationSizeReduction.NO_INFORMATION)
                            || psr.contains(RedListEnums.PopulationSizeReduction.NO_REDUCTION)
                            || pop.getPopulationTrend() == null || getPopulation().getPopulationTrend().getMinValue() == null || getPopulation().getPopulationTrend().getMinValue() < 30
                            || pop.getPopulationSizeReductionJustification().isEmpty()) {
                        warns.add("DataSheet.msg.warning.2");
                        alc.add("A");
                        break;
                    }
                    switch(cr.getSubCriteria()) {
                        case "1":
                            if(alc.contains("A1")) break;
                            if(!psr.contains(RedListEnums.PopulationSizeReduction.DECREASE_REVERSIBLE)
                                    || pop.getPopulationTrend() == null || getPopulation().getPopulationTrend().getMinValue() == null || getPopulation().getPopulationTrend().getMinValue() < 30) {
                                warns.add("DataSheet.msg.warning.6.1");
                                alc.add("A1");
                            }
                            break;

                        case "2":
                            if(alc.contains("A2")) break;
                            if(!psr.contains(RedListEnums.PopulationSizeReduction.DECREASE_IRREVERSIBLE)
                                    || pop.getPopulationTrend() == null || getPopulation().getPopulationTrend().getMinValue() == null || getPopulation().getPopulationTrend().getMinValue() < 30) {
                                warns.add("DataSheet.msg.warning.6.2");
                                alc.add("A2");
                            }
                            break;

                        case "3":
                            if(alc.contains("A3")) break;
                            if(!psr.contains(RedListEnums.PopulationSizeReduction.POSSIBLE_DECREASE_FUTURE)
                                    || pop.getPopulationTrend() == null || getPopulation().getPopulationTrend().getMinValue() == null || getPopulation().getPopulationTrend().getMinValue() < 30) {
                                warns.add("DataSheet.msg.warning.6.3");
                                alc.add("A3");
                            }
                            break;

                        case "4":
                            if(alc.contains("A4")) break;
                            if(!psr.contains(RedListEnums.PopulationSizeReduction.DECREASE_PAST_FUTURE)
                                    || pop.getPopulationTrend() == null || getPopulation().getPopulationTrend().getMinValue() == null || getPopulation().getPopulationTrend().getMinValue() < 30) {
                                warns.add("DataSheet.msg.warning.6.4");
                                alc.add("A4");
                            }
                            break;

                    }
                    break;

                case "B":
                    // either EOO or AOO must be filled. But they are automatic, skip global validation.

                    switch(cr.getSubCriteria()) {
                        case "1":
                            if(alc.contains("B1")) break;
                            if(dist.getEOO() == null || dist.getEOO() > 20000) {
                                warns.add("DataSheet.msg.warning.7.1");
                                alc.add("B1");
                            }
                            break;

                        case "2":
                            if(alc.contains("B2")) break;
                            if(dist.getAOO() == null || dist.getAOO() > 2000) {
                                warns.add("DataSheet.msg.warning.7.2");
                                alc.add("B2");
                            }
                            break;
                    }

                    switch(cr.getSubsubCriteria()) {
                        case "a":
                            if(alc.contains("B2a")) break;
                            critB.add("Bxa");
                            if(!(pop.getSeverelyFragmented() == RedListEnums.SeverelyFragmented.SEVERELY_FRAGMENTED
                                    && !StringUtils.cleanText(pop.getSeverelyFragmentedJustification().toString()).equals(""))
                                    && !(thr.getNumberOfLocations() != null && (thr.getNumberOfLocations().getMinValue() == null || thr.getNumberOfLocations().getMinValue() <= 10)
                                    && !StringUtils.cleanText(thr.getNumberOfLocationsJustification().toString()).equals(""))) {
                                warns.add("DataSheet.msg.warning.8.1");
                                alc.add("B2a");
                            }
                            break;

                        case "b":
                            critB.add("Bxb");
                            switch (cr.getSubsubsubCriteria()) {
                                case "i":
                                    if(alc.contains("B2bi")) break;
                                    if(StringUtils.cleanText(dist.getDeclineDistributionJustification().toString()).equals("")
                                            || (dist.getDeclineDistribution() != RedListEnums.DeclineDistribution.DECLINE_EOO
                                            && dist.getDeclineDistribution() != RedListEnums.DeclineDistribution.DECLINE_EOO_AOO)) {
                                        warns.add("DataSheet.msg.warning.8.2");
                                        alc.add("B2bi");
                                    }
                                    break;

                                case "ii":
                                    if(alc.contains("B2bii")) break;
                                    if(StringUtils.cleanText(dist.getDeclineDistributionJustification().toString()).equals("")
                                            || (dist.getDeclineDistribution() != RedListEnums.DeclineDistribution.DECLINE_AOO
                                            && dist.getDeclineDistribution() != RedListEnums.DeclineDistribution.DECLINE_EOO_AOO)) {
                                        warns.add("DataSheet.msg.warning.8.3");
                                        alc.add("B2bii");
                                    }
                                    break;

                                case "iii":
                                    if(alc.contains("B2biii")) break;
                                    if(StringUtils.cleanText(getEcology().getDeclineHabitatQualityJustification().toString()).equals("")
                                            || getEcology().getDeclineHabitatQuality() != RedListEnums.DeclineHabitatQuality.CONTINUED_DECLINE) {
                                        warns.add("DataSheet.msg.warning.8.4");
                                        alc.add("B2biii");
                                    }
                                    break;

                                case "iv":
                                    if(alc.contains("B2biv")) break;
                                    if(StringUtils.cleanText(thr.getDeclineNrLocationsJustification().toString()).equals("")
                                            || thr.getDeclineNrLocations() != RedListEnums.DeclineNrLocations.CONTINUED_DECLINE) {
                                        warns.add("DataSheet.msg.warning.8.5");
                                        alc.add("B2biv");
                                    }
                                    break;

                                case "v":
                                    if(alc.contains("B2bv")) break;
                                    if(StringUtils.cleanText(pop.getPopulationDeclineJustification().toString()).equals("")
                                            || pop.getPopulationDecline() != RedListEnums.DeclinePopulation.CONTINUED_DECLINE) {
                                        warns.add("DataSheet.msg.warning.8.6");
                                        alc.add("B2bv");
                                    }
                                    break;
                            }
                            break;

                        case "c":
                            critB.add("Bxc");
                            switch (cr.getSubsubsubCriteria()) {
                                case "i":
                                    if(alc.contains("B2ci")) break;
                                    if(dist.getExtremeFluctuations() != RedListEnums.ExtremeFluctuations.EOO_AOO
                                            && dist.getExtremeFluctuations() != RedListEnums.ExtremeFluctuations.EOO) {
                                        warns.add("DataSheet.msg.warning.8.7");
                                        alc.add("B2ci");
                                    }
                                    break;

                                case "ii":
                                    if(alc.contains("B2cii")) break;
                                    if(dist.getExtremeFluctuations() != RedListEnums.ExtremeFluctuations.EOO_AOO
                                            && dist.getExtremeFluctuations() != RedListEnums.ExtremeFluctuations.AOO) {
                                        warns.add("DataSheet.msg.warning.8.8");
                                        alc.add("B2cii");
                                    }
                                    break;

                                case "iii":
                                    if(alc.contains("B2ciii")) break;
                                    if(thr.getExtremeFluctuationsNrLocationsJustification().isEmpty()
                                            || thr.getExtremeFluctuationsNrLocations() != RedListEnums.YesNoNA.YES) {
                                        warns.add("DataSheet.msg.warning.8.9");
                                        alc.add("B2ciii");
                                    }
                                    break;

                                case "iv":
                                    if(alc.contains("B2civ")) break;
                                    if(pop.getExtremeFluctuationsJustification().isEmpty()
                                            || pop.getExtremeFluctuations() != RedListEnums.YesNoNA.YES) {
                                        warns.add("DataSheet.msg.warning.8.10");
                                        alc.add("B2civ");
                                    }
                                    break;
                            }
                            break;
                    }

                    break;

                case "C":
                    if(alc.contains("C")) break;
//                    Integer nr = StringUtils.getMaxOfInterval(pop.getNrMatureIndividualsExact());
                    Integer nr = pop.getNrMatureIndividualsExact().getMaxValue();
                    if(pop.getNrMatureIndividualsCategory() == RedListEnums.NrMatureIndividuals.NO_DATA
                            || pop.getNrMatureIndividualsCategory() == RedListEnums.NrMatureIndividuals.GT_10000
                            || (nr != null && nr > 10000)) {
                        warns.add("DataSheet.msg.warning.3");
                        alc.add("C");
                        break;
                    }

                    switch(cr.getSubCriteria()) {
                        case "1":
                            if(alc.contains("C1")) break;
                            if(pop.getPopulationDecline() != RedListEnums.DeclinePopulation.CONTINUED_DECLINE
                                    || StringUtils.cleanText(pop.getPopulationDeclineJustification().toString()).equals("")
                                    || pop.getPopulationDeclinePercent() == null || pop.getPopulationDeclinePercent().getMaxValue() == null || pop.getPopulationDeclinePercent().getMaxValue() < 10) {
                                warns.add("DataSheet.msg.warning.9.1");
                                alc.add("C1");
                            }
                            break;

                        case "2":
                            if(alc.contains("C2")) break;
                            if(pop.getPopulationDecline() != RedListEnums.DeclinePopulation.CONTINUED_DECLINE
                                    || StringUtils.cleanText(pop.getPopulationDeclineJustification().toString()).equals("")) {
                                warns.add("DataSheet.msg.warning.9.2");
                                alc.add("C2");
                                break;
                            }

                            switch(cr.getSubsubCriteria()) {
                                case "a":
                                    switch(cr.getSubsubsubCriteria()) {
                                        case "i":
                                            if(alc.contains("C2ai")) break;
                                            if(pop.getNrMatureEachSubpop() != RedListEnums.NrMatureEachSubpop.LT_50
                                                    && pop.getNrMatureEachSubpop() != RedListEnums.NrMatureEachSubpop.LT_250
                                                    && pop.getNrMatureEachSubpop() != RedListEnums.NrMatureEachSubpop.LT_1000) {
                                                warns.add("DataSheet.msg.warning.9.3");
                                                alc.add("C2ai");
                                            }
                                            break;

                                        case "ii":
                                            if(alc.contains("C2aii")) break;
                                            if(pop.getPercentMatureOneSubpop() == RedListEnums.PercentMatureOneSubpop.NO_DATA) {
                                                warns.add("DataSheet.msg.warning.9.4");
                                                alc.add("C2aii");
                                            }
                                            break;
                                    }
                                    break;

                                case "b":
                                    if(alc.contains("C2b")) break;
                                    if(StringUtils.cleanText(pop.getExtremeFluctuationsJustification().toString()).equals("")
                                            || pop.getExtremeFluctuations() != RedListEnums.YesNoNA.YES) {
                                        warns.add("DataSheet.msg.warning.9.5");
                                        alc.add("C2b");
                                    }
                                    break;
                            }
                            break;
                    }
                    break;

                case "D":
                    Integer nr1 = pop.getNrMatureIndividualsExact().getMaxValue();
                    switch(cr.getSubCriteria()) {
                        case "1":
                            if (alc.contains("D1")) break;
                            Boolean D1 = pop.getNrMatureIndividualsCategory().isLessThanOrEqual(RedListEnums.NrMatureIndividuals.BET_250_1000);
                            if ((D1 != null && !D1) || (nr1 != null && nr1 > 1000)) {
                                warns.add("DataSheet.msg.warning.10");
                                alc.add("D1");
                            }
                            break;

                        case "2":
                            if (alc.contains("D2")) break;
                            if (!(dist.getAOO() != null && dist.getAOO() <= 20)
                                    && !(thr.getNumberOfLocations() != null && thr.getNumberOfLocations().overlapsWith(null, 5)
                                        && !StringUtils.cleanText(thr.getNumberOfLocationsJustification().toString()).equals(""))) {
                                warns.add("DataSheet.msg.warning.11");
                                alc.add("D2");
                            }
                            break;

                        case "":
                            if(alc.contains("D")) break;
                            if(!isNrMatureIndividualsLessThan(250)) {
                                warns.add("DataSheet.msg.warning.4");
                                alc.add("D");
                                break;
                            }
                            break;
                    }
                    break;

                case "E":
                    if(alc.contains("E")) break;
                    if(pop._getPopulationSizeReductionAsList().contains(RedListEnums.PopulationSizeReduction.NO_INFORMATION)
                            || pop._getPopulationSizeReductionAsList().contains(RedListEnums.PopulationSizeReduction.NO_REDUCTION)
                            || pop.getPopulationTrend() == null || getPopulation().getPopulationTrend().getMinValue() == null || getPopulation().getPopulationTrend().getMinValue() < 10
                            || pop.getPopulationSizeReductionJustification() == null
                            || StringUtils.cleanText(pop.getPopulationSizeReductionJustification().toString()).equals("")) {
                        warns.add("DataSheet.msg.warning.5");
                        alc.add("E");
                    }
                    break;
            }
        }
        if(critB.size() == 1)   // criterion B must have 2 subcriteria
            warns.add("DataSheet.msg.warning.B2");

        if(cat != null && (cat == RedListEnums.RedListCategories.CR || cat == RedListEnums.RedListCategories.EN || cat == RedListEnums.RedListCategories.VU)
                && getAssessment().getCriteria().length == 0)
            warns.add("DataSheet.msg.warning.nocriteria");

        // now validade the category taking into account the criteria and the thresholds
        alc.clear();
        for(RedListEnums.AssessmentCriteria cr : getAssessment().getCriteria()) {
            switch(cr.getCriteria()) {
                case "A":
                    switch (cr.getSubCriteria()) {
                        case "1":
                            if(pop.getPopulationTrend() != null) {
                                if (cat == RedListEnums.RedListCategories.CR && !alc.contains("A1-CR") && (pop.getPopulationTrend() == null || pop.getPopulationTrend().getMinValue() == null || pop.getPopulationTrend().getMinValue() < 90)) {
                                    warns.add("DataSheet.msg.warning.A1.CR");
                                    alc.add("A1-CR");
                                    break;
                                }
                                if (cat == RedListEnums.RedListCategories.EN && !alc.contains("A1-EN") && (pop.getPopulationTrend() == null || pop.getPopulationTrend().getMinValue() == null || pop.getPopulationTrend().getMinValue() < 70)) {
                                    warns.add("DataSheet.msg.warning.A1.EN");
                                    alc.add("A1-EN");
                                    break;
                                }
                                if (cat == RedListEnums.RedListCategories.VU && !alc.contains("A1-VU") && (pop.getPopulationTrend() == null || pop.getPopulationTrend().getMinValue() == null || pop.getPopulationTrend().getMinValue() < 50)) {
                                    warns.add("DataSheet.msg.warning.A1.VU");
                                    alc.add("A1-VU");
                                    break;
                                }
                            }
                            break;

                        case "2":
                        case "3":
                        case "4":
                            if(pop.getPopulationTrend() != null) {
                                if (cat == RedListEnums.RedListCategories.CR && !alc.contains("A2-CR") && (pop.getPopulationTrend() == null || pop.getPopulationTrend().getMinValue() == null || pop.getPopulationTrend().getMinValue() < 80)) {
                                    warns.add("DataSheet.msg.warning.A2.CR");
                                    alc.add("A2-CR");
                                    break;
                                }
                                if (cat == RedListEnums.RedListCategories.EN && !alc.contains("A2-EN") && (pop.getPopulationTrend() == null || pop.getPopulationTrend().getMinValue() == null || pop.getPopulationTrend().getMinValue() < 50)) {
                                    warns.add("DataSheet.msg.warning.A2.EN");
                                    alc.add("A2-EN");
                                    break;
                                }
                                if (cat == RedListEnums.RedListCategories.VU && !alc.contains("A2-VU") && (pop.getPopulationTrend() == null || pop.getPopulationTrend().getMinValue() == null || pop.getPopulationTrend().getMinValue() < 30)) {
                                    warns.add("DataSheet.msg.warning.A2.VU");
                                    alc.add("A2-VU");
                                    break;
                                }
                            }
                            break;
                    }

                    break;

                case "B":
                    switch (cr.getSubCriteria()) {
                        case "1":
                            if(cat == RedListEnums.RedListCategories.CR && !alc.contains("B1-CR") && (dist.getEOO() == null || dist.getEOO() >= 100)) {
                                warns.add("DataSheet.msg.warning.B1.CR");
                                alc.add("B1-CR");
                                break;
                            }
                            if(cat == RedListEnums.RedListCategories.EN && !alc.contains("B1-EN") && (dist.getEOO() == null || dist.getEOO() >= 5000)) {
                                warns.add("DataSheet.msg.warning.B1.EN");
                                alc.add("B1-EN");
                                break;
                            }
                            if(cat == RedListEnums.RedListCategories.VU && !alc.contains("B1-VU") && (dist.getEOO() == null || dist.getEOO() >= 20000)) {
                                warns.add("DataSheet.msg.warning.B1.VU");
                                alc.add("B1-VU");
                                break;
                            }
                            break;

                        case "2":
                            if(cat == RedListEnums.RedListCategories.CR && !alc.contains("B2-CR") && (dist.getAOO() == null || dist.getAOO() >= 10)) {
                                warns.add("DataSheet.msg.warning.B2.CR");
                                alc.add("B2-CR");
                                break;
                            }
                            if(cat == RedListEnums.RedListCategories.EN && !alc.contains("B2-EN") && (dist.getAOO() == null || dist.getAOO() >= 500)) {
                                warns.add("DataSheet.msg.warning.B2.EN");
                                alc.add("B2-EN");
                                break;
                            }
                            if(cat == RedListEnums.RedListCategories.VU && !alc.contains("B2-VU") && (dist.getAOO() == null || dist.getAOO() >= 2000)) {
                                warns.add("DataSheet.msg.warning.B2.VU");
                                alc.add("B2-VU");
                                break;
                            }
                            break;
                    }

                    switch (cr.getSubsubCriteria()) {
                        case "a":
                            if(pop.getSeverelyFragmented() == RedListEnums.SeverelyFragmented.SEVERELY_FRAGMENTED) break;
                            if(thr.getNumberOfLocations() == null && !alc.contains("BA")) {
                                warns.add("DataSheet.msg.warning.B.a");
                                alc.add("BA");
                                break;
                            }

                            if(cat == RedListEnums.RedListCategories.CR && !alc.contains("BA-CR") && (thr.getNumberOfLocations() == null || (thr.getNumberOfLocations().getMinValue() != null && thr.getNumberOfLocations().getMinValue() > 1))) {
                                warns.add("DataSheet.msg.warning.B.a.CR");
                                alc.add("BA-CR");
                                break;
                            }
                            if(cat == RedListEnums.RedListCategories.EN && !alc.contains("BA-EN") && (thr.getNumberOfLocations() == null || (thr.getNumberOfLocations().getMinValue() != null && thr.getNumberOfLocations().getMinValue() > 5))) {
                                warns.add("DataSheet.msg.warning.B.a.EN");
                                alc.add("BA-EN");
                                break;
                            }
                            if(cat == RedListEnums.RedListCategories.VU && !alc.contains("BA-VU") && (thr.getNumberOfLocations() == null || (thr.getNumberOfLocations().getMinValue() != null && thr.getNumberOfLocations().getMinValue() > 10))) {
                                warns.add("DataSheet.msg.warning.B.a.VU");
                                alc.add("BA-VU");
                                break;
                            }

                            break;
                    }
                    break;

                case "C":
                    if(cat == RedListEnums.RedListCategories.CR && !isNrMatureIndividualsLessThan(250) && !alc.contains("C-CR")) {
                        warns.add("DataSheet.msg.warning.C.CR");
                        alc.add("C-CR");
                        break;
                    }
                    if(cat == RedListEnums.RedListCategories.EN && !isNrMatureIndividualsLessThan(2500) && !alc.contains("C-EN")) {
                        warns.add("DataSheet.msg.warning.C.EN");
                        alc.add("C-EN");
                        break;
                    }
                    if(cat == RedListEnums.RedListCategories.VU && !isNrMatureIndividualsLessThan(10000) && !alc.contains("C-VU")) {
                        warns.add("DataSheet.msg.warning.C.VU");
                        alc.add("C-VU");
                        break;
                    }
                    switch(cr.getSubCriteria()) {
                        case "1":
                            if(cat == RedListEnums.RedListCategories.CR && !alc.contains("C1-CR") && (pop.getPopulationDeclinePercent() == null || pop.getPopulationDeclinePercent().getMaxValue() == null || pop.getPopulationDeclinePercent().getMaxValue() < 25)) {
                                warns.add("DataSheet.msg.warning.C1.CR");
                                alc.add("C1-CR");
                                break;
                            }
                            if(cat == RedListEnums.RedListCategories.EN && !alc.contains("C1-EN") && (pop.getPopulationDeclinePercent() == null || pop.getPopulationDeclinePercent().getMaxValue() == null || pop.getPopulationDeclinePercent().getMaxValue() < 20)) {
                                warns.add("DataSheet.msg.warning.C1.EN");
                                alc.add("C1-EN");
                                break;
                            }
                            if(cat == RedListEnums.RedListCategories.VU && !alc.contains("C1-VU") && (pop.getPopulationDeclinePercent() == null || pop.getPopulationDeclinePercent().getMaxValue() == null || pop.getPopulationDeclinePercent().getMaxValue() < 10)) {
                                warns.add("DataSheet.msg.warning.C1.VU");
                                alc.add("C1-VU");
                                break;
                            }
                            break;

                        case "2":
                            if(cr.getSubsubCriteria().equals("a")) {
                                switch (cr.getSubsubsubCriteria()) {
                                    case "i":
                                        if(cat == RedListEnums.RedListCategories.CR
                                                && pop.getNrMatureEachSubpop() != RedListEnums.NrMatureEachSubpop.LT_50 && !alc.contains("C2ai-CR")) {
                                            warns.add("DataSheet.msg.warning.C2ai.CR");
                                            alc.add("C2ai-CR");
                                            break;
                                        }
                                        if(cat == RedListEnums.RedListCategories.EN
                                                && pop.getNrMatureEachSubpop() != RedListEnums.NrMatureEachSubpop.LT_50
                                                && pop.getNrMatureEachSubpop() != RedListEnums.NrMatureEachSubpop.LT_250
                                                && !alc.contains("C2ai-EN")) {
                                            warns.add("DataSheet.msg.warning.C2ai.EN");
                                            alc.add("C2ai-EN");
                                            break;
                                        }
                                        if(cat == RedListEnums.RedListCategories.VU
                                                && pop.getNrMatureEachSubpop() != RedListEnums.NrMatureEachSubpop.LT_50
                                                && pop.getNrMatureEachSubpop() != RedListEnums.NrMatureEachSubpop.LT_250
                                                && pop.getNrMatureEachSubpop() != RedListEnums.NrMatureEachSubpop.LT_1000
                                                && !alc.contains("C2ai-VU")) {
                                            warns.add("DataSheet.msg.warning.C2ai.VU");
                                            alc.add("C2ai-VU");
                                            break;
                                        }
                                        break;

                                    case "ii":
                                        if(cat == RedListEnums.RedListCategories.CR
                                                && pop.getPercentMatureOneSubpop() != RedListEnums.PercentMatureOneSubpop.BT_90_100
                                                && !alc.contains("C2aii-CR")) {
                                            warns.add("DataSheet.msg.warning.C2aii.CR");
                                            alc.add("C2aii-CR");
                                            break;
                                        }
                                        if(cat == RedListEnums.RedListCategories.EN
                                                && pop.getPercentMatureOneSubpop() != RedListEnums.PercentMatureOneSubpop.BT_95_100
                                                && !alc.contains("C2aii-EN")) {
                                            warns.add("DataSheet.msg.warning.C2aii.EN");
                                            alc.add("C2aii-EN");
                                            break;
                                        }
                                        if(cat == RedListEnums.RedListCategories.VU
                                                && pop.getPercentMatureOneSubpop() != RedListEnums.PercentMatureOneSubpop.LT_1000
                                                && !alc.contains("C2aii-VU")) {
                                            warns.add("DataSheet.msg.warning.C2aii.VU");
                                            alc.add("C2aii-VU");
                                            break;
                                        }
                                        break;
                                }
                            }
                            break;
                    }
                    break;

                case "D":
                    switch(cr.getSubCriteria()) {
                        case "1":
                            if (cat != RedListEnums.RedListCategories.VU && !alc.contains("D1-VU")) {
                                warns.add("DataSheet.msg.warning.D1.VU");
                                alc.add("D1-VU");
                            }
                            break;

                        case "2":
                            if (cat != RedListEnums.RedListCategories.VU && !alc.contains("D2-VU")) {
                                warns.add("DataSheet.msg.warning.D2.VU");
                                alc.add("D2-VU");
                            }
                            break;

                        case "":
                            if(cat != RedListEnums.RedListCategories.CR && cat != RedListEnums.RedListCategories.EN && ! alc.contains("D-ENCR")) {
                                warns.add("DataSheet.msg.warning.D");
                                alc.add("D-ENCR");
                                break;
                            }

                            if (cat == RedListEnums.RedListCategories.CR && !isNrMatureIndividualsLessThan(50) && !alc.contains("D-CR")) {
                                warns.add("DataSheet.msg.warning.D.CR");
                                alc.add("D-CR");
                                break;
                            }
                            if (cat == RedListEnums.RedListCategories.EN && !isNrMatureIndividualsLessThan(250) && !alc.contains("D-EN")) {
                                warns.add("DataSheet.msg.warning.D.EN");
                                alc.add("D-EN");
                                break;
                            }

                            break;
                    }
                    break;
            }
        }
        return warns;
    }

    private Boolean isNrMatureIndividualsLessThan(int nr) {
        if(getPopulation().getNrMatureIndividualsCategory() == RedListEnums.NrMatureIndividuals.EXACT_NUMBER
                && getPopulation().getNrMatureIndividualsExact().getMaxValue() <= nr) return true;

        switch(getPopulation().getNrMatureIndividualsCategory()) {
            case BET_50_250: return 250 <= nr;
            case BET_250_1000: return 1000 <= nr;
            case BET_1000_2500: return 2500 <= nr;
            case BET_2500_10000: return 10000 <= nr;
            case GT_10000: return 15000 <= nr;
            case LT_50: return 50 <= nr;
        }
        return false;
    }

    @Override
    public Constants.NodeTypes getType() {
        return null;
    }

    @Override
    public String getTypeAsString() {
        return null;
    }

}
