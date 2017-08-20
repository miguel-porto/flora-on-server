package pt.floraon.redlistdata.entities;

import pt.floraon.driver.DiffableBean;
import pt.floraon.driver.datatypes.SafeHTMLString;
import pt.floraon.redlistdata.RedListEnums;

/**
 * Created by miguel on 16-11-2016.
 */
public class GeographicalDistribution implements DiffableBean {
    private SafeHTMLString description;
    private Double EOO, historicalEOO;
    private Double AOO, historicalAOO;
    private RedListEnums.DeclineDistribution declineDistribution;
    private RedListEnums.ExtremeFluctuations extremeFluctuations;
    private String declineDistributionJustification;
    private Integer[] elevationRange;

    public SafeHTMLString getDescription() {
        return description == null ? new SafeHTMLString("") : description;
    }

    public Double getEOO() {
        return EOO;
    }

    public Double getAOO() {
        return AOO;
    }

    public Double getHistoricalEOO() {
        return historicalEOO;
    }

    public Double getHistoricalAOO() {
        return historicalAOO;
    }

    public RedListEnums.DeclineDistribution getDeclineDistribution() {
        return declineDistribution == null ? RedListEnums.DeclineDistribution.NO_INFORMATION : declineDistribution;
    }

    public String getDeclineDistributionJustification() {
        return declineDistributionJustification == null ? "" : declineDistributionJustification;
    }

    public RedListEnums.ExtremeFluctuations getExtremeFluctuations() {
        return extremeFluctuations == null ? RedListEnums.ExtremeFluctuations.NO_INFORMATION : extremeFluctuations;
    }

    public Integer[] getElevationRange() {
        return elevationRange == null ? new Integer[] {null, null} : elevationRange;
    }

    public void setDescription(SafeHTMLString description) {
        this.description = description;
    }

    public void setEOO(Double EOO) {
        this.EOO = EOO;
    }

    public void setAOO(Double AOO) {
        this.AOO = AOO;
    }

    public void setHistoricalEOO(Double EOO) {
        this.historicalEOO = EOO;
    }

    public void setHistoricalAOO(Double AOO) {
        this.historicalAOO = AOO;
    }

    public void setExtremeFluctuations(RedListEnums.ExtremeFluctuations extremeFluctuations) {
        this.extremeFluctuations = extremeFluctuations;
    }

    public void setElevationRange(Integer[] elevationRange) {
        this.elevationRange = elevationRange;
    }

    public void setDeclineDistribution(RedListEnums.DeclineDistribution declineDistribution) {
        this.declineDistribution = declineDistribution;
    }

    public void setDeclineDistributionJustification(String declineDistributionJustification) {
        this.declineDistributionJustification = declineDistributionJustification;
    }

}
