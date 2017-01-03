package pt.floraon.redlistdata.entities;

/**
 * Created by miguel on 16-11-2016.
 */
public class GeographicalDistribution {
    private String description;
    private Double EOO;
    private Double AOO;
    private RedListEnums.DeclineDistribution declineDistribution;
    private RedListEnums.ExtremeFluctuations extremeFluctuations;
    private String declineDistributionJustification;
    private Integer[] elevationRange;

    public String getDescription() {
        return description;
    }

    public Double getEOO() {
        return EOO;
    }

    public Double getAOO() {
        return AOO;
    }

    public RedListEnums.DeclineDistribution getDeclineDistribution() {
        return declineDistribution;
    }

    public RedListEnums.ExtremeFluctuations getExtremeFluctuations() {
        return extremeFluctuations;
    }

    public Integer[] getElevationRange() {
        return elevationRange;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setEOO(Double EOO) {
        this.EOO = EOO;
    }

    public void setAOO(Double AOO) {
        this.AOO = AOO;
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

    public String getDeclineDistributionJustification() {
        return declineDistributionJustification;
    }

    public void setDeclineDistributionJustification(String declineDistributionJustification) {
        this.declineDistributionJustification = declineDistributionJustification;
    }
}
