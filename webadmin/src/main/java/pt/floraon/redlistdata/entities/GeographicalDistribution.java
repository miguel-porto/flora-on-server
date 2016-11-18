package pt.floraon.redlistdata.entities;

/**
 * Created by miguel on 16-11-2016.
 */
public class GeographicalDistribution {
    private String description;
    private Long EOO;
    private Long AOO;
    private RedListEnums.DeclineDistribution declineDistribution;
    private Integer[] elevationRange;

    public String getDescription() {
        return description;
    }

    public Long getEOO() {
        return EOO;
    }

    public Long getAOO() {
        return AOO;
    }

    public RedListEnums.DeclineDistribution getDeclineDistribution() {
        return declineDistribution;
    }

    public Integer[] getElevationRange() {
        return elevationRange;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setEOO(Long EOO) {
        this.EOO = EOO;
    }

    public void setAOO(Long AOO) {
        this.AOO = AOO;
    }

    public void setDeclineDistribution(RedListEnums.DeclineDistribution declineDistribution) {
        this.declineDistribution = declineDistribution;
    }

    public void setElevationRange(Integer[] elevationRange) {
        this.elevationRange = elevationRange;
    }
}
