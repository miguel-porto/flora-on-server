package pt.floraon.redlistdata.entities;

import com.google.gson.JsonObject;
import jline.internal.Log;
import pt.floraon.driver.Constants;
import pt.floraon.driver.entities.GeneralDBNode;
import pt.floraon.taxonomy.entities.TaxEnt;
import pt.floraon.driver.results.InferredStatus;

import java.util.ArrayList;
import java.util.List;

/**
 * A JavaBean representing all the data fields for the red list sheets. There can be only one sheet per TaxEnt per
 * territory.
 * Created by miguel on 11-11-2016.
 */
public class RedListDataEntity extends GeneralDBNode {
    /**
     * The full TaxEnt database entity. Note this is not stored in the DB, must be fetched by {@link RedListDataEntity#taxEntID}
     */
    private transient TaxEnt taxEnt;
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
        return taxonomicProblemDescription;
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

    public void setTaxEntID(String taxEntID) {
        this.taxEntID = taxEntID;
    }

    public void setTags(String[] tags) {
        this.tags = tags;
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

    public void setGeographicalDistribution_EOO(Long EOO) {
        this.geographicalDistribution.setEOO(EOO);
    }

    public void setGeographicalDistribution_AOO(Long AOO) {
        this.geographicalDistribution.setAOO(AOO);
    }

    public void setGeographicalDistribution_DeclineDistribution(String declineDistribution) {
        try {
            this.geographicalDistribution.setDeclineDistribution(RedListEnums.DeclineDistribution.valueOf(declineDistribution));
        } catch (IllegalArgumentException e) {
            this.geographicalDistribution.setDeclineDistribution(RedListEnums.DeclineDistribution.NO_INFORMATION);
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

    public void setPopulation_PopulationTrend(Integer populationTrend) {
        this.population.setPopulationTrend(populationTrend);
    }

    public void setPopulation_SeverelyFragmented(String severelyFragmented) {
        try {
            this.population.setSeverelyFragmented(RedListEnums.SeverelyFragmented.valueOf(severelyFragmented));
        } catch (IllegalArgumentException e) {
            this.population.setSeverelyFragmented(RedListEnums.SeverelyFragmented.NO_INFORMATION);
        }
    }

    public void setPopulation_ExtremeFluctuations(String extremeFluctuations) {
        try {
            this.population.setExtremeFluctuations(RedListEnums.YesNoNA.valueOf(extremeFluctuations));
        } catch (IllegalArgumentException e) {
            this.population.setExtremeFluctuations(RedListEnums.YesNoNA.NO_DATA);
        }
    }

    /*******
     * Ecology fields
     *******/

    public void setEcology_Description(String description) {
        this.ecology.setDescription(description);
    }

    public void setEcology_HabitatTypes(String[] habitatTypes) {
        List<RedListEnums.HabitatTypes> tmp = new ArrayList<>();
        for(String s : habitatTypes) {
            try {
                tmp.add(RedListEnums.HabitatTypes.valueOf(s));
            } catch (IllegalArgumentException e) {
                Log.warn("Habitat "+s+" not found");
            }
        }
        this.getEcology().setHabitatTypes(tmp.toArray(new RedListEnums.HabitatTypes[0]));
    }

    public void setEcology_GenerationLength(String generationLength) {
        try {
            this.ecology.setGenerationLength(RedListEnums.GenerationLength.valueOf(generationLength));
        } catch (IllegalArgumentException e) {
            this.ecology.setGenerationLength(RedListEnums.GenerationLength.NO_DATA);
        }
    }

    /*******
     * Uses and trade fields
     *******/

    public void setUsesAndTrade_Description(String description) {
        this.getUsesAndTrade().setDescription(description);
    }

    public void setUsesAndTrade_Uses(String[] uses) {
        List<RedListEnums.Uses> tmp = new ArrayList<>();
        for(String s : uses) {
            try {
                tmp.add(RedListEnums.Uses.valueOf(s));
            } catch (IllegalArgumentException e) {
                Log.warn("Use "+s+" not found");
            }
        }
        this.getUsesAndTrade().setUses(tmp.toArray(new RedListEnums.Uses[0]));
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
        this.conservation.setProposedConservationActions(proposedConservationActions);
    }

    /*******
     * Assessment fields
     *******/

    public void setAssessment_Category(String category) {
        this.assessment.setCategory(RedListEnums.RedListCategories.valueOf(category));
    }

    public void setAssessment_Criteria(String criteria) {
        this.assessment.setCriteria(criteria);
    }

    public void setAssessment_Justification(String justification) {
        this.assessment.setJustification(justification);
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
        this.assessment.setAssessmentStatus(RedListEnums.AssessmentStatus.valueOf(assessmentStatus));
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
