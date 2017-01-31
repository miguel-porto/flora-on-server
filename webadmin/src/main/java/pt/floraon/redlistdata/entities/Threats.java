package pt.floraon.redlistdata.entities;

import pt.floraon.driver.DiffableBean;
import pt.floraon.redlistdata.RedListEnums;

/**
 * Created by miguel on 20-11-2016.
 */
public class Threats implements DiffableBean {
    private String description;
    private Integer numberOfLocations;
    private String numberOfLocationsJustification;
    private RedListEnums.DeclineNrLocations declineNrLocations;
    private String declineNrLocationsJustification;
    private RedListEnums.YesNoNA extremeFluctuationsNrLocations;
    private String extremeFluctuationsNrLocationsJustification;

    public RedListEnums.DeclineNrLocations getDeclineNrLocations() {
        return declineNrLocations == null ? RedListEnums.DeclineNrLocations.NO_INFORMATION : declineNrLocations;
    }

    public String getDeclineNrLocationsJustification() {
        return declineNrLocationsJustification == null ? "" : declineNrLocationsJustification;
    }

    public String getNumberOfLocationsJustification() {
        return numberOfLocationsJustification == null ? "" : numberOfLocationsJustification;
    }

    public String getDescription() {
        return description == null ? "" : description;
    }

    public Integer getNumberOfLocations() {
        return numberOfLocations;
    }

    public RedListEnums.YesNoNA getExtremeFluctuationsNrLocations() {
        return extremeFluctuationsNrLocations == null ? RedListEnums.YesNoNA.NO_DATA : extremeFluctuationsNrLocations;
    }

    public String getExtremeFluctuationsNrLocationsJustification() {
        return extremeFluctuationsNrLocationsJustification == null ? "" : extremeFluctuationsNrLocationsJustification;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setNumberOfLocations(Integer numberOfLocations) {
        this.numberOfLocations = numberOfLocations;
    }

    public void setNumberOfLocationsJustification(String numberOfLocationsJustification) {
        this.numberOfLocationsJustification = numberOfLocationsJustification;
    }

    public void setDeclineNrLocations(RedListEnums.DeclineNrLocations declineNrLocations) {
        this.declineNrLocations = declineNrLocations;
    }

    public void setDeclineNrLocationsJustification(String declineNrLocationsJustification) {
        this.declineNrLocationsJustification = declineNrLocationsJustification;
    }

    public void setExtremeFluctuationsNrLocations(RedListEnums.YesNoNA extremeFluctuationsNrLocations) {
        this.extremeFluctuationsNrLocations = extremeFluctuationsNrLocations;
    }

    public void setExtremeFluctuationsNrLocationsJustification(String extremeFluctuationsNrLocationsJustification) {
        this.extremeFluctuationsNrLocationsJustification = extremeFluctuationsNrLocationsJustification;
    }
}
