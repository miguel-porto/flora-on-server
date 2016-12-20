package pt.floraon.redlistdata.entities;

/**
 * Created by miguel on 16-11-2016.
 */
public class Population {
    private String description;
    private RedListEnums.NrMatureIndividuals nrMatureIndividualsCategory;
    private Long nrMatureIndividualsExact;
    private String nrMatureIndividualsDescription;
    private RedListEnums.TypeOfPopulationEstimate typeOfEstimate;
    private RedListEnums.DeclinePopulation populationDecline;
    private Integer populationDeclinePercent;
    private String populationDeclineJustification;
    // population size reduction
    private Integer populationTrend;
    private RedListEnums.PopulationSizeReduction populationSizeReduction;
    private String populationSizeReductionJustification;

    private RedListEnums.SeverelyFragmented severelyFragmented;
    private String severelyFragmentedJustification;

    private RedListEnums.YesNoNA extremeFluctuations;
    private String extremeFluctuationsJustification;

    private RedListEnums.NrMatureEachSubpop nrMatureEachSubpop;
    private RedListEnums.PercentMatureOneSubpop percentMatureOneSubpop;

    public String getDescription() {
        return description;
    }

    public RedListEnums.NrMatureIndividuals getNrMatureIndividualsCategory() {
        return nrMatureIndividualsCategory;
    }

    public Long getNrMatureIndividualsExact() {
        return nrMatureIndividualsExact;
    }

    public String getNrMatureIndividualsDescription() {
        return nrMatureIndividualsDescription;
    }

    public RedListEnums.TypeOfPopulationEstimate getTypeOfEstimate() {
        return typeOfEstimate;
    }

    public RedListEnums.DeclinePopulation getPopulationDecline() {
        return populationDecline;
    }

    public Integer getPopulationTrend() {
        return populationTrend;
    }

    public RedListEnums.PopulationSizeReduction getPopulationSizeReduction() {
        return populationSizeReduction;
    }

    public String getPopulationSizeReductionJustification() {
        return populationSizeReductionJustification;
    }

    public RedListEnums.SeverelyFragmented getSeverelyFragmented() {
        return severelyFragmented;
    }

    public String getSeverelyFragmentedJustification() {
        return severelyFragmentedJustification;
    }

    public RedListEnums.YesNoNA getExtremeFluctuations() {
        return extremeFluctuations;
    }

    public String getExtremeFluctuationsJustification() {
        return extremeFluctuationsJustification;
    }

    public Integer getPopulationDeclinePercent() {
        return populationDeclinePercent;
    }

    public String getPopulationDeclineJustification() {
        return populationDeclineJustification;
    }

    public RedListEnums.NrMatureEachSubpop getNrMatureEachSubpop() {
        return nrMatureEachSubpop;
    }

    public RedListEnums.PercentMatureOneSubpop getPercentMatureOneSubpop() {
        return percentMatureOneSubpop;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setNrMatureIndividualsCategory(RedListEnums.NrMatureIndividuals nrMatureIndividualsCategory) {
        this.nrMatureIndividualsCategory = nrMatureIndividualsCategory;
    }

    public void setNrMatureIndividualsExact(Long nrMatureIndividualsExact) {
        this.nrMatureIndividualsExact = nrMatureIndividualsExact;
    }

    public void setNrMatureIndividualsDescription(String nrMatureIndividualsDescription) {
        this.nrMatureIndividualsDescription = nrMatureIndividualsDescription;
    }

    public void setTypeOfEstimate(RedListEnums.TypeOfPopulationEstimate typeOfEstimate) {
        this.typeOfEstimate = typeOfEstimate;
    }

    public void setPopulationDecline(RedListEnums.DeclinePopulation populationDecline) {
        this.populationDecline = populationDecline;
    }

    public void setPopulationTrend(Integer populationTrend) {
        this.populationTrend = populationTrend;
    }

    public void setPopulationSizeReduction(RedListEnums.PopulationSizeReduction populationSizeReduction) {
        this.populationSizeReduction = populationSizeReduction;
    }

    public void setPopulationSizeReductionJustification(String populationSizeReductionJustification) {
        this.populationSizeReductionJustification = populationSizeReductionJustification;
    }

    public void setSeverelyFragmented(RedListEnums.SeverelyFragmented severelyFragmented) {
        this.severelyFragmented = severelyFragmented;
    }

    public void setExtremeFluctuations(RedListEnums.YesNoNA extremeFluctuations) {
        this.extremeFluctuations = extremeFluctuations;
    }

    public void setPopulationDeclinePercent(Integer populationDeclinePercent) {
        this.populationDeclinePercent = populationDeclinePercent;
    }

    public void setPopulationDeclineJustification(String populationDeclineJustification) {
        this.populationDeclineJustification = populationDeclineJustification;
    }

    public void setSeverelyFragmentedJustification(String severelyFragmentedJustification) {
        this.severelyFragmentedJustification = severelyFragmentedJustification;
    }

    public void setExtremeFluctuationsJustification(String extremeFluctuationsJustification) {
        this.extremeFluctuationsJustification = extremeFluctuationsJustification;
    }

    public void setNrMatureEachSubpop(RedListEnums.NrMatureEachSubpop nrMatureEachSubpop) {
        this.nrMatureEachSubpop = nrMatureEachSubpop;
    }

    public void setPercentMatureOneSubpop(RedListEnums.PercentMatureOneSubpop percentMatureOneSubpop) {
        this.percentMatureOneSubpop = percentMatureOneSubpop;
    }
}
