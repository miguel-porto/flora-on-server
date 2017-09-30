package pt.floraon.redlistdata.entities;

import pt.floraon.driver.DiffableBean;
import pt.floraon.driver.datatypes.NumericInterval;
import pt.floraon.driver.datatypes.SafeHTMLString;
import pt.floraon.driver.utils.StringUtils;
import pt.floraon.redlistdata.RedListEnums;

import java.util.Arrays;
import java.util.List;

/**
 * Created by miguel on 16-11-2016.
 */
public class Population implements DiffableBean {
    private SafeHTMLString description;
    private RedListEnums.NrMatureIndividuals nrMatureIndividualsCategory;
    private NumericInterval nrMatureIndividualsExact;
    private SafeHTMLString nrMatureIndividualsDescription;
    private RedListEnums.TypeOfPopulationEstimate typeOfEstimate;
    private RedListEnums.DeclinePopulation populationDecline;
    private NumericInterval populationDeclinePercent;
    private SafeHTMLString populationDeclineJustification;
    // population size reduction
    private NumericInterval populationTrend;
    private RedListEnums.PopulationSizeReduction[] populationSizeReduction;
    private SafeHTMLString populationSizeReductionJustification;

    private RedListEnums.SeverelyFragmented severelyFragmented;
    private SafeHTMLString severelyFragmentedJustification;

    private RedListEnums.YesNoNA extremeFluctuations;
    private SafeHTMLString extremeFluctuationsJustification;

    private RedListEnums.NrMatureEachSubpop nrMatureEachSubpop;
    private RedListEnums.PercentMatureOneSubpop percentMatureOneSubpop;

    public SafeHTMLString getDescription() {
        return description == null ? SafeHTMLString.emptyString() : description;
    }

    public RedListEnums.NrMatureIndividuals getNrMatureIndividualsCategory() {
        return nrMatureIndividualsCategory == null ? RedListEnums.NrMatureIndividuals.NO_DATA : nrMatureIndividualsCategory;
    }

    public NumericInterval getNrMatureIndividualsExact() {
        return nrMatureIndividualsExact;
    }

    public SafeHTMLString getNrMatureIndividualsDescription() {
        return nrMatureIndividualsDescription == null ? SafeHTMLString.emptyString() : nrMatureIndividualsDescription;
    }

    public RedListEnums.TypeOfPopulationEstimate getTypeOfEstimate() {
        return typeOfEstimate == null ? RedListEnums.TypeOfPopulationEstimate.NO_DATA : typeOfEstimate;
    }

    public RedListEnums.DeclinePopulation getPopulationDecline() {
        return populationDecline == null ? RedListEnums.DeclinePopulation.NO_INFORMATION : populationDecline;
    }

    public NumericInterval getPopulationTrend() {
        return populationTrend;
    }

    public RedListEnums.PopulationSizeReduction[] getPopulationSizeReduction() {
        return StringUtils.isArrayEmpty(populationSizeReduction)
                ? new RedListEnums.PopulationSizeReduction[]{RedListEnums.PopulationSizeReduction.NO_INFORMATION}
                : populationSizeReduction;
//        return populationSizeReduction == null ? RedListEnums.PopulationSizeReduction.NO_INFORMATION : populationSizeReduction;
    }

    public List<RedListEnums.PopulationSizeReduction> _getPopulationSizeReductionAsList() {
        return Arrays.asList(this.getPopulationSizeReduction());
    }

    public boolean _isAnyPopulationSizeReductionSelected() {
        for(RedListEnums.PopulationSizeReduction psr : this.getPopulationSizeReduction())
            if(psr.isTrigger()) return true;
        return false;
    }

    public SafeHTMLString getPopulationSizeReductionJustification() {
        return populationSizeReductionJustification == null ? SafeHTMLString.emptyString() : populationSizeReductionJustification;
    }

    public RedListEnums.SeverelyFragmented getSeverelyFragmented() {
        return severelyFragmented == null ? RedListEnums.SeverelyFragmented.NO_INFORMATION : severelyFragmented;
    }

    public SafeHTMLString getSeverelyFragmentedJustification() {
        return severelyFragmentedJustification == null ? SafeHTMLString.emptyString() : severelyFragmentedJustification;
    }

    public RedListEnums.YesNoNA getExtremeFluctuations() {
        return extremeFluctuations == null ? RedListEnums.YesNoNA.NO_DATA : extremeFluctuations;
    }

    public SafeHTMLString getExtremeFluctuationsJustification() {
        return extremeFluctuationsJustification == null ? SafeHTMLString.emptyString() : extremeFluctuationsJustification;
    }

    public NumericInterval getPopulationDeclinePercent() {
        return populationDeclinePercent;
    }

    public SafeHTMLString getPopulationDeclineJustification() {
        return populationDeclineJustification == null ? SafeHTMLString.emptyString() : populationDeclineJustification;
    }

    public RedListEnums.NrMatureEachSubpop getNrMatureEachSubpop() {
        return nrMatureEachSubpop == null ? RedListEnums.NrMatureEachSubpop.NO_DATA : nrMatureEachSubpop;
    }

    public RedListEnums.PercentMatureOneSubpop getPercentMatureOneSubpop() {
        return percentMatureOneSubpop == null ? RedListEnums.PercentMatureOneSubpop.NO_DATA : percentMatureOneSubpop;
    }

    public void setDescription(SafeHTMLString description) {
        this.description = description;
    }

    public void setNrMatureIndividualsCategory(RedListEnums.NrMatureIndividuals nrMatureIndividualsCategory) {
        this.nrMatureIndividualsCategory = nrMatureIndividualsCategory;
    }

    public void setNrMatureIndividualsExact(NumericInterval nrMatureIndividualsExact) {
        this.nrMatureIndividualsExact = nrMatureIndividualsExact;
    }

    public void setNrMatureIndividualsDescription(SafeHTMLString nrMatureIndividualsDescription) {
        this.nrMatureIndividualsDescription = nrMatureIndividualsDescription;
    }

    public void setTypeOfEstimate(RedListEnums.TypeOfPopulationEstimate typeOfEstimate) {
        this.typeOfEstimate = typeOfEstimate;
    }

    public void setPopulationDecline(RedListEnums.DeclinePopulation populationDecline) {
        this.populationDecline = populationDecline;
    }

    public void setPopulationTrend(NumericInterval populationTrend) {
        this.populationTrend = populationTrend;
    }

    public void setPopulationSizeReduction(RedListEnums.PopulationSizeReduction[] populationSizeReduction) {
        this.populationSizeReduction = populationSizeReduction;
    }

    public void setPopulationSizeReductionJustification(SafeHTMLString populationSizeReductionJustification) {
        this.populationSizeReductionJustification = populationSizeReductionJustification;
    }

    public void setSeverelyFragmented(RedListEnums.SeverelyFragmented severelyFragmented) {
        this.severelyFragmented = severelyFragmented;
    }

    public void setExtremeFluctuations(RedListEnums.YesNoNA extremeFluctuations) {
        this.extremeFluctuations = extremeFluctuations;
    }

    public void setPopulationDeclinePercent(NumericInterval populationDeclinePercent) {
        this.populationDeclinePercent = populationDeclinePercent;
    }

    public void setPopulationDeclineJustification(SafeHTMLString populationDeclineJustification) {
        this.populationDeclineJustification = populationDeclineJustification;
    }

    public void setSeverelyFragmentedJustification(SafeHTMLString severelyFragmentedJustification) {
        this.severelyFragmentedJustification = severelyFragmentedJustification;
    }

    public void setExtremeFluctuationsJustification(SafeHTMLString extremeFluctuationsJustification) {
        this.extremeFluctuationsJustification = extremeFluctuationsJustification;
    }

    public void setNrMatureEachSubpop(RedListEnums.NrMatureEachSubpop nrMatureEachSubpop) {
        this.nrMatureEachSubpop = nrMatureEachSubpop;
    }

    public void setPercentMatureOneSubpop(RedListEnums.PercentMatureOneSubpop percentMatureOneSubpop) {
        this.percentMatureOneSubpop = percentMatureOneSubpop;
    }
}
