package pt.floraon.redlistdata.entities;

import pt.floraon.driver.DiffableBean;
import pt.floraon.driver.datatypes.IntegerInterval;
import pt.floraon.driver.datatypes.SafeHTMLString;
import pt.floraon.driver.utils.StringUtils;
import pt.floraon.redlistdata.RedListEnums;
import pt.floraon.redlistdata.threats.Threat;

/**
 * Created by miguel on 20-11-2016.
 */
public class Threats implements DiffableBean {
    private SafeHTMLString description;
    private IntegerInterval numberOfLocations;
    private SafeHTMLString numberOfLocationsJustification;
    private RedListEnums.DeclineNrLocations declineNrLocations;
    private SafeHTMLString declineNrLocationsJustification;
    private RedListEnums.YesNoNA extremeFluctuationsNrLocations;
    private SafeHTMLString extremeFluctuationsNrLocationsJustification;
    private Threat[] threats;

    public RedListEnums.DeclineNrLocations getDeclineNrLocations() {
        return declineNrLocations == null ? RedListEnums.DeclineNrLocations.NO_INFORMATION : declineNrLocations;
    }

    public SafeHTMLString getDeclineNrLocationsJustification() {
        return declineNrLocationsJustification == null ? SafeHTMLString.emptyString() : declineNrLocationsJustification;
    }

    public SafeHTMLString getNumberOfLocationsJustification() {
        return numberOfLocationsJustification == null ? SafeHTMLString.emptyString() : numberOfLocationsJustification;
    }

    public SafeHTMLString getDescription() {
        return description == null ? SafeHTMLString.emptyString() : description;
    }

    public IntegerInterval getNumberOfLocations() {
        return numberOfLocations;
    }

    public RedListEnums.YesNoNA getExtremeFluctuationsNrLocations() {
        return extremeFluctuationsNrLocations == null ? RedListEnums.YesNoNA.NO_DATA : extremeFluctuationsNrLocations;
    }

    public SafeHTMLString getExtremeFluctuationsNrLocationsJustification() {
        return extremeFluctuationsNrLocationsJustification == null ? SafeHTMLString.emptyString() : extremeFluctuationsNrLocationsJustification;
    }

    public Threat[] getThreats() {
        return StringUtils.isArrayEmpty(threats)
                ? new Threat[0]
                : threats;
    }

    public void setDescription(SafeHTMLString description) {
        this.description = description;
    }

    public void setNumberOfLocations(IntegerInterval numberOfLocations) {
        this.numberOfLocations = numberOfLocations;
    }

    public void setNumberOfLocationsJustification(SafeHTMLString numberOfLocationsJustification) {
        this.numberOfLocationsJustification = numberOfLocationsJustification;
    }

    public void setDeclineNrLocations(RedListEnums.DeclineNrLocations declineNrLocations) {
        this.declineNrLocations = declineNrLocations;
    }

    public void setDeclineNrLocationsJustification(SafeHTMLString declineNrLocationsJustification) {
        this.declineNrLocationsJustification = declineNrLocationsJustification;
    }

    public void setExtremeFluctuationsNrLocations(RedListEnums.YesNoNA extremeFluctuationsNrLocations) {
        this.extremeFluctuationsNrLocations = extremeFluctuationsNrLocations;
    }

    public void setExtremeFluctuationsNrLocationsJustification(SafeHTMLString extremeFluctuationsNrLocationsJustification) {
        this.extremeFluctuationsNrLocationsJustification = extremeFluctuationsNrLocationsJustification;
    }

    public void setThreats(Threat[] threats) {
        this.threats = threats;
    }
}
