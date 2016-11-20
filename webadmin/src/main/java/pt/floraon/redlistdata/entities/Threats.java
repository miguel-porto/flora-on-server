package pt.floraon.redlistdata.entities;

/**
 * Created by miguel on 20-11-2016.
 */
public class Threats {
    private String description;
    private Integer numberOfLocations;

    public String getDescription() {
        return description;
    }

    public Integer getNumberOfLocations() {
        return numberOfLocations;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setNumberOfLocations(Integer numberOfLocations) {
        this.numberOfLocations = numberOfLocations;
    }
}
