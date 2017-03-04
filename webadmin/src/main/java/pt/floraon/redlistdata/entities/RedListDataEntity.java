package pt.floraon.redlistdata.entities;

import com.arangodb.velocypack.annotations.Expose;
import com.google.gson.JsonObject;
import pt.floraon.driver.DiffableBean;
import pt.floraon.driver.Constants;
import pt.floraon.driver.entities.GeneralDBNode;
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
public class RedListDataEntity extends GeneralDBNode implements DiffableBean {
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
    private String taxonomicProblemDescription;
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

    public RedListDataEntity() {
    }

    public RedListDataEntity(String taxEntID, InferredStatus inferredStatus) {
        this.taxEntID = taxEntID;
        this.inferredStatus = inferredStatus;
    }

    public TaxEnt getTaxEnt() {
        return taxEnt;
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

    public String getTaxonomicProblemDescription() {
        return taxonomicProblemDescription == null ? "" : taxonomicProblemDescription;
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
            cal.setTime(dateTimeFormat.parse(datePublished));
            return cal.get(Calendar.YEAR);
        } catch (ParseException e) {
            return null;
        }
    }

    public void setTaxEntID(String taxEntID) {
        this.taxEntID = taxEntID;
    }

    public void setTags(String[] tags) {
        this.tags = StringUtils.cleanArray(tags);
    }

    public void setTaxonomicProblemDescription(String taxonomicProblemDescription) {
        this.taxonomicProblemDescription = taxonomicProblemDescription;
    }

    /*******
     * GeographicalDistribution fields
     *******/

    public void setGeographicalDistribution_Description(String description) {
        this.geographicalDistribution.setDescription(description);
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
        this.getGeographicalDistribution().setDeclineDistributionJustification(declineDistributionJustification);
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
        this.population.setDescription(description);
    }

    public void setPopulation_PopulationTrend(Integer populationTrend) {
        this.population.setPopulationTrend(populationTrend);
    }

    public void setPopulation_PopulationSizeReduction(String populationSizeReduction) {
        try {
            this.population.setPopulationSizeReduction(RedListEnums.PopulationSizeReduction.valueOf(populationSizeReduction));
        } catch (IllegalArgumentException e) {
            this.population.setPopulationSizeReduction(RedListEnums.PopulationSizeReduction.NO_INFORMATION);
        }
    }

    public void setPopulation_PopulationSizeReductionJustification(String populationSizeReductionJustification) {
        this.population.setPopulationSizeReductionJustification(populationSizeReductionJustification);
    }

    public void setPopulation_NrMatureIndividualsCategory(String nrMatureIndividualsCategory) {
        try {
            this.population.setNrMatureIndividualsCategory(RedListEnums.NrMatureIndividuals.valueOf(nrMatureIndividualsCategory));
        } catch (IllegalArgumentException e) {
            this.population.setNrMatureIndividualsCategory(RedListEnums.NrMatureIndividuals.NO_DATA);
        }
    }

    public void setPopulation_NrMatureIndividualsExact(String nrMatureIndividualsExact) {
        this.population.setNrMatureIndividualsExact(nrMatureIndividualsExact);
    }

    public void setPopulation_NrMatureIndividualsDescription(String nrMatureIndividualsDescription) {
        this.population.setNrMatureIndividualsDescription(nrMatureIndividualsDescription);
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

    public void setPopulation_PopulationDeclinePercent(Integer populationDeclinePercent) {
        this.population.setPopulationDeclinePercent(populationDeclinePercent);
    }

    public void setPopulation_PopulationDeclineJustification(String populationDeclineJustification) {
        this.population.setPopulationDeclineJustification(populationDeclineJustification);
    }

    public void setPopulation_SeverelyFragmented(String severelyFragmented) {
        try {
            this.population.setSeverelyFragmented(RedListEnums.SeverelyFragmented.valueOf(severelyFragmented));
        } catch (IllegalArgumentException e) {
            this.population.setSeverelyFragmented(RedListEnums.SeverelyFragmented.NO_INFORMATION);
        }
    }

    public void setPopulation_SeverelyFragmentedJustification(String populationSeverelyFragmentedJustification) {
        this.population.setSeverelyFragmentedJustification(populationSeverelyFragmentedJustification);
    }

    public void setPopulation_ExtremeFluctuations(String extremeFluctuations) {
        try {
            this.population.setExtremeFluctuations(RedListEnums.YesNoNA.valueOf(extremeFluctuations));
        } catch (IllegalArgumentException e) {
            this.population.setExtremeFluctuations(RedListEnums.YesNoNA.NO_DATA);
        }
    }

    public void setPopulation_ExtremeFluctuationsJustification(String extremeFluctuationsJustification) {
        this.population.setExtremeFluctuationsJustification(extremeFluctuationsJustification);
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
        this.ecology.setDescription(description);
    }

    public void setEcology_HabitatTypes(String[] habitatTypes) {
/*
        List<RedListEnums.HabitatTypes> tmp = new ArrayList<>();
        for(String s : habitatTypes) {
            try {
                tmp.add(RedListEnums.HabitatTypes.valueOf(s));
            } catch (IllegalArgumentException e) {
                Log.warn("Habitat "+s+" not found");
            }
        }
        this.getEcology().setHabitatTypes(tmp.toArray(new RedListEnums.HabitatTypes[0]));
*/
        this.ecology.setHabitatTypes(StringUtils.stringArrayToEnumArray(habitatTypes, RedListEnums.HabitatTypes.class));
    }

    public void setEcology_GenerationLength(String generationLength) {
        this.ecology.setGenerationLength(generationLength);
    }

    public void setEcology_GenerationLengthJustification(String generationLengthJustification) {
        this.ecology.setGenerationLengthJustification(generationLengthJustification);
    }

    public void setEcology_DeclineHabitatQuality(String declineHabitatQuality) {
        try {
            this.ecology.setDeclineHabitatQuality(RedListEnums.DeclineHabitatQuality.valueOf(declineHabitatQuality));
        } catch (IllegalArgumentException e) {
            this.ecology.setDeclineHabitatQuality(RedListEnums.DeclineHabitatQuality.NO_INFORMATION);
        }
    }

    public void setEcology_DeclineHabitatQualityJustification(String declineHabitatQualityJustification) {
        this.ecology.setDeclineHabitatQualityJustification(declineHabitatQualityJustification);
    }

    /*******
     * Uses and trade fields
     *******/

    public void setUsesAndTrade_Description(String description) {
        this.getUsesAndTrade().setDescription(description);
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
        this.getThreats().setDescription(description);
    }

    public void setThreats_NumberOfLocations(Integer numberOfLocations) {
        this.getThreats().setNumberOfLocations(numberOfLocations);
    }

    public void setThreats_NumberOfLocationsJustification(String numberOfLocationsJustification) {
        this.getThreats().setNumberOfLocationsJustification(numberOfLocationsJustification);
    }

    public void setThreats_DeclineNrLocations(String declineNrLocations) {
        try {
            this.threats.setDeclineNrLocations(RedListEnums.DeclineNrLocations.valueOf(declineNrLocations));
        } catch (IllegalArgumentException e) {
            this.threats.setDeclineNrLocations(RedListEnums.DeclineNrLocations.NO_INFORMATION);
        }
    }

    public void setThreats_DeclineNrLocationsJustification(String declineNrLocationsJustification) {
        this.threats.setDeclineNrLocationsJustification(declineNrLocationsJustification);
    }

    public void setThreats_ExtremeFluctuationsNrLocations(String extremeFluctuationsNrLocations) {
        try {
            this.threats.setExtremeFluctuationsNrLocations(RedListEnums.YesNoNA.valueOf(extremeFluctuationsNrLocations));
        } catch (IllegalArgumentException e) {
            this.threats.setExtremeFluctuationsNrLocations(RedListEnums.YesNoNA.NO_DATA);
        }
    }

    public void setThreats_ExtremeFluctuationsNrLocationsJustification(String extremeFluctuationsNrLocationsJustification) {
        this.threats.setExtremeFluctuationsNrLocationsJustification(extremeFluctuationsNrLocationsJustification);
    }

    public void setThreats_Threats(String[] threats) {
        this.threats.setThreats(StringUtils.stringArrayToEnumArray(threats, RedListEnums.Threats.class));
    }

    /*******
     * Conservation fields
     *******/

    public void setConservation_Description(String description) {
        this.conservation.setDescription(description);
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
        this.conservation.setConservationPlansJustification(conservationPlansJustification);
    }

    public void setConservation_ExSituConservationJustification(String exSituConservationJustification) {
        this.conservation.setExSituConservationJustification(exSituConservationJustification);
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
        this.assessment.setUpDownListingJustification(upDownListingJustification);
    }

    public void setAssessment_Criteria(String[] criteria) {
        this.getAssessment().setCriteria(StringUtils.stringArrayToEnumArray(criteria, RedListEnums.AssessmentCriteria.class));
    }

    public void setAssessment_Justification(String justification) {
        this.assessment.setJustification(justification);
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
        this.dateAssessed = dateTimeFormat.format(new Date());
    }

    /**
     * Updates the date published with the current date and time
     */
    public void updateDatePublished() {
        this.datePublished = dateTimeFormat.format(new Date());
    }

    public void addRevision(String user) {
        if(this.revisions == null || this.revisions.size() == 0) this.revisions = new ArrayList<Revision>();
        this.revisions.add(new Revision(user));
//        for(Revision r : this.revisions)
//            System.out.println(r.getDateTimeSaved().toString()+" "+r.getUser());
    }

    public List<Revision> getRevisions() {
        return this.revisions == null ? Collections.EMPTY_LIST : this.revisions;
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
        Set<String> alc = new HashSet<>();

        for(RedListEnums.AssessmentCriteria cr : getAssessment().getCriteria()) {
            // first check the validity of the 5 major criteria
            switch(cr.getCriteria()) {
                case "A":
                    if(alc.contains("A")) break;
                    if(pop.getPopulationSizeReduction() == RedListEnums.PopulationSizeReduction.NO_INFORMATION
                            || pop.getPopulationSizeReduction() == RedListEnums.PopulationSizeReduction.NO_REDUCTION
                            || pop.getPopulationTrend() == null || getPopulation().getPopulationTrend() < 30
                            || pop.getPopulationSizeReductionJustification() == null
                            || StringUtils.cleanText(pop.getPopulationSizeReductionJustification()).equals("")) {
                        warns.add("DataSheet.msg.warning.2");
                        alc.add("A");
                        break;
                    }
                    switch(cr.getSubCriteria()) {
                        case "1":
                            if(alc.contains("A1")) break;
                            if(pop.getPopulationSizeReduction() != RedListEnums.PopulationSizeReduction.DECREASE_REVERSIBLE
                                    || pop.getPopulationTrend() == null || getPopulation().getPopulationTrend() < 30) {
                                warns.add("DataSheet.msg.warning.6.1");
                                alc.add("A1");
                            }
                            break;

                        case "2":
                            if(alc.contains("A2")) break;
                            if(pop.getPopulationSizeReduction() != RedListEnums.PopulationSizeReduction.DECREASE_IRREVERSIBLE
                                    || pop.getPopulationTrend() == null || getPopulation().getPopulationTrend() < 30) {
                                warns.add("DataSheet.msg.warning.6.2");
                                alc.add("A2");
                            }
                            break;

                        case "3":
                            if(alc.contains("A3")) break;
                            if(pop.getPopulationSizeReduction() != RedListEnums.PopulationSizeReduction.POSSIBLE_DECREASE_FUTURE
                                    || pop.getPopulationTrend() == null || getPopulation().getPopulationTrend() < 30) {
                                warns.add("DataSheet.msg.warning.6.3");
                                alc.add("A3");
                            }
                            break;

                        case "4":
                            if(alc.contains("A4")) break;
                            if(pop.getPopulationSizeReduction() != RedListEnums.PopulationSizeReduction.DECREASE_PAST_FUTURE
                                    || pop.getPopulationTrend() == null || getPopulation().getPopulationTrend() < 30) {
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
                            if(!(pop.getSeverelyFragmented() == RedListEnums.SeverelyFragmented.SEVERELY_FRAGMENTED
                                    && !StringUtils.cleanText(pop.getSeverelyFragmentedJustification()).equals(""))
                                    && !(getThreats().getNumberOfLocations() != null && getThreats().getNumberOfLocations() <= 10
                                    && !StringUtils.cleanText(getThreats().getNumberOfLocationsJustification()).equals(""))) {
                                warns.add("DataSheet.msg.warning.8.1");
                                alc.add("B2a");
                            }
                            break;

                        case "b":
                            switch (cr.getSubsubsubCriteria()) {
                                case "i":
                                    if(alc.contains("B2bi")) break;
                                    if(StringUtils.cleanText(dist.getDeclineDistributionJustification()).equals("")
                                            || (dist.getDeclineDistribution() != RedListEnums.DeclineDistribution.DECLINE_EOO
                                            && dist.getDeclineDistribution() != RedListEnums.DeclineDistribution.DECLINE_EOO_AOO)) {
                                        warns.add("DataSheet.msg.warning.8.2");
                                        alc.add("B2bi");
                                    }
                                    break;

                                case "ii":
                                    if(alc.contains("B2bii")) break;
                                    if(StringUtils.cleanText(dist.getDeclineDistributionJustification()).equals("")
                                            || (dist.getDeclineDistribution() != RedListEnums.DeclineDistribution.DECLINE_AOO
                                            && dist.getDeclineDistribution() != RedListEnums.DeclineDistribution.DECLINE_EOO_AOO)) {
                                        warns.add("DataSheet.msg.warning.8.3");
                                        alc.add("B2bii");
                                    }
                                    break;

                                case "iii":
                                    if(alc.contains("B2biii")) break;
                                    if(StringUtils.cleanText(getEcology().getDeclineHabitatQualityJustification()).equals("")
                                            || getEcology().getDeclineHabitatQuality() != RedListEnums.DeclineHabitatQuality.CONTINUED_DECLINE) {
                                        warns.add("DataSheet.msg.warning.8.4");
                                        alc.add("B2biii");
                                    }
                                    break;

                                case "iv":
                                    if(alc.contains("B2biv")) break;
                                    if(StringUtils.cleanText(getThreats().getDeclineNrLocationsJustification()).equals("")
                                            || getThreats().getDeclineNrLocations() != RedListEnums.DeclineNrLocations.CONTINUED_DECLINE) {
                                        warns.add("DataSheet.msg.warning.8.5");
                                        alc.add("B2biv");
                                    }
                                    break;

                                case "v":
                                    if(alc.contains("B2bv")) break;
                                    if(StringUtils.cleanText(pop.getPopulationDeclineJustification()).equals("")
                                            || pop.getPopulationDecline() != RedListEnums.DeclinePopulation.CONTINUED_DECLINE) {
                                        warns.add("DataSheet.msg.warning.8.6");
                                        alc.add("B2bv");
                                    }
                                    break;
                            }
                            break;

                        case "c":
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
                                    if(StringUtils.cleanText(pop.getExtremeFluctuationsJustification()).equals("")
                                            || pop.getExtremeFluctuations() != RedListEnums.YesNoNA.YES) {
                                        warns.add("DataSheet.msg.warning.8.9");
                                        alc.add("B2ciii");
                                    }
                                    break;

                                case "iv":
                                    if(alc.contains("B2civ")) break;
                                    if(StringUtils.cleanText(getThreats().getExtremeFluctuationsNrLocationsJustification()).equals("")
                                            || getThreats().getExtremeFluctuationsNrLocations() != RedListEnums.YesNoNA.YES) {
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
                    Integer nr = StringUtils.getMaxOfInterval(pop.getNrMatureIndividualsExact());
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
                                    || StringUtils.cleanText(pop.getPopulationDeclineJustification()).equals("")
                                    || pop.getPopulationDeclinePercent() == null || pop.getPopulationDeclinePercent() < 10) {
                                warns.add("DataSheet.msg.warning.9.1");
                                alc.add("C1");
                            }
                            break;

                        case "2":
                            if(alc.contains("C2")) break;
                            if(pop.getPopulationDecline() != RedListEnums.DeclinePopulation.CONTINUED_DECLINE
                                    || StringUtils.cleanText(pop.getPopulationDeclineJustification()).equals("")) {
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
                                    if(StringUtils.cleanText(pop.getExtremeFluctuationsJustification()).equals("")
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
                    if(alc.contains("D")) break;
                    if(pop.getNrMatureIndividualsCategory() == RedListEnums.NrMatureIndividuals.NO_DATA
                            && getThreats().getNumberOfLocations() == null) {
                        warns.add("DataSheet.msg.warning.4");
                        alc.add("D");
                        break;
                    }

                    Integer nr1 = StringUtils.getMaxOfInterval(pop.getNrMatureIndividualsExact());
                    switch(cr.getSubCriteria()) {
                        case "1":
                            if(alc.contains("D1")) break;
                            if((pop.getNrMatureIndividualsCategory() != RedListEnums.NrMatureIndividuals.LT_50
                                    && pop.getNrMatureIndividualsCategory() != RedListEnums.NrMatureIndividuals.BET_50_250
                                    && pop.getNrMatureIndividualsCategory() != RedListEnums.NrMatureIndividuals.BET_250_1000
                                    && pop.getNrMatureIndividualsCategory() != RedListEnums.NrMatureIndividuals.EXACT_NUMBER)
                                || (nr1 != null && nr1 > 1000)) {
                                warns.add("DataSheet.msg.warning.10");
                                alc.add("D1");
                            }
                            break;

                        case "2":
                            if(alc.contains("D2")) break;
                            if(!(dist.getAOO() != null && dist.getAOO() <= 20)
                                    && !(getThreats().getNumberOfLocations() != null && getThreats().getNumberOfLocations() <= 6
                                        && !StringUtils.cleanText(getThreats().getNumberOfLocationsJustification()).equals(""))) {
                                warns.add("DataSheet.msg.warning.11");
                                alc.add("D2");
                            }
                            break;
                    }
                    break;

                case "E":
                    if(alc.contains("E")) break;
                    if(pop.getPopulationSizeReduction() == RedListEnums.PopulationSizeReduction.NO_INFORMATION
                            || pop.getPopulationSizeReduction() == RedListEnums.PopulationSizeReduction.NO_REDUCTION
                            || pop.getPopulationTrend() == null || getPopulation().getPopulationTrend() < 10
                            || pop.getPopulationSizeReductionJustification() == null
                            || StringUtils.cleanText(pop.getPopulationSizeReductionJustification()).equals("")) {
                        warns.add("DataSheet.msg.warning.5");
                        alc.add("E");
                    }
                    break;
            }
        }
        return warns;
    }

    @Override
    public Constants.NodeTypes getType() {
        return null;
    }

    @Override
    public String getTypeAsString() {
        return null;
    }

    @Override
    public JsonObject toJson() {
        return null;
    }

    @Override
    public String toJsonString() {
        return null;
    }

}
