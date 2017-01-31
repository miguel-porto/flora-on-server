package pt.floraon.redlistdata.entities;

import com.arangodb.velocypack.annotations.Expose;
import com.google.gson.JsonObject;
import jline.internal.Log;
import pt.floraon.driver.DiffableBean;
import pt.floraon.driver.Constants;
import pt.floraon.driver.entities.GeneralDBNode;
import pt.floraon.redlistdata.RedListEnums;
import pt.floraon.taxonomy.entities.TaxEnt;
import pt.floraon.driver.results.InferredStatus;

import java.lang.reflect.Array;
import java.text.ParseException;
import java.util.*;

import static pt.floraon.driver.Constants.cleanArray;
import static pt.floraon.driver.Constants.dateTimeFormat;

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
    private boolean hasTaxonomicProblems;
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
        return tags;
    }

    public boolean getHasTaxonomicProblems() {
        return hasTaxonomicProblems;
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

    public Integer getYearPublished() {
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
        this.tags = cleanArray(tags);
    }

    public void setHasTaxonomicProblems(boolean hasTaxonomicProblems) {
        this.hasTaxonomicProblems = hasTaxonomicProblems;
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

    public void setPopulation_NrMatureIndividualsExact(Long nrMatureIndividualsExact) {
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
        this.ecology.setHabitatTypes(stringArrayToEnumArray(habitatTypes, RedListEnums.HabitatTypes.class));
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
        this.getUsesAndTrade().setUses(stringArrayToEnumArray(uses, RedListEnums.Uses.class));
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
        this.conservation.setProposedConservationActions(stringArrayToEnumArray(proposedConservationActions, RedListEnums.ProposedConservationActions.class));
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
        this.conservation.setProposedStudyMeasures(stringArrayToEnumArray(proposedStudyMeasures, RedListEnums.ProposedStudyMeasures.class));
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

    public void setAssessment_Criteria(String criteria) {
        this.assessment.setCriteria(criteria);
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

    private <T extends Enum<T>> T[] stringArrayToEnumArray(String[] stringArray, Class<T> clazz) {
        List<T> tmp = new ArrayList<>();
        boolean addNull = false;
        for(String s : stringArray) {
            if(s.length() == 0) {
                addNull = true;
                continue;
            }
            try {
                tmp.add(T.valueOf(clazz, s));
            } catch (IllegalArgumentException e) {
                Log.warn("Enum value " + s + " not found in " + clazz.toString());
            }
        }

        // this is to distinguish between a null value and an empty array
        if(tmp.size() == 0 && addNull) tmp.add(null);
        return tmp.toArray((T[]) Array.newInstance(clazz, tmp.size()));
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
