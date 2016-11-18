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
    private Integer populationTrend;
    private RedListEnums.SeverelyFragmented severelyFragmented;
    private RedListEnums.ExtremeFluctuations extremeFluctuations;

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

    public RedListEnums.SeverelyFragmented getSeverelyFragmented() {
        return severelyFragmented;
    }

    public RedListEnums.ExtremeFluctuations getExtremeFluctuations() {
        return extremeFluctuations;
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

    public void setSeverelyFragmented(RedListEnums.SeverelyFragmented severelyFragmented) {
        this.severelyFragmented = severelyFragmented;
    }

    public void setExtremeFluctuations(RedListEnums.ExtremeFluctuations extremeFluctuations) {
        this.extremeFluctuations = extremeFluctuations;
    }
}
