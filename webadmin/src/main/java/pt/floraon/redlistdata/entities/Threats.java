package pt.floraon.redlistdata.entities;

/**
 * Created by miguel on 20-11-2016.
 */
public class Threats {
    private String description;
    private Integer numberOfLocations;
    private String numberOfLocationsJustification;
    private RedListEnums.DeclineNrLocations declineNrLocations;
    private String declineNrLocationsJustification;
    private RedListEnums.YesNoNA extremeFluctuationsNrLocations;
    private String extremeFluctuationsNrLocationsJustification;

    public RedListEnums.DeclineNrLocations getDeclineNrLocations() {
        return declineNrLocations;
    }

    public String getDeclineNrLocationsJustification() {
        return declineNrLocationsJustification;
    }

    public String getNumberOfLocationsJustification() {
        return numberOfLocationsJustification;
    }

    public String getDescription() {
        return description;
    }

    public Integer getNumberOfLocations() {
        return numberOfLocations;
    }

    public RedListEnums.YesNoNA getExtremeFluctuationsNrLocations() {
        return extremeFluctuationsNrLocations;
    }

    public String getExtremeFluctuationsNrLocationsJustification() {
        return extremeFluctuationsNrLocationsJustification;
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
