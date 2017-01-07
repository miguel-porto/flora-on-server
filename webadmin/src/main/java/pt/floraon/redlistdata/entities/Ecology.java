package pt.floraon.redlistdata.entities;

/**
 * Created by miguel on 18-11-2016.
 */
public class Ecology {
    private String description;
    private RedListEnums.HabitatTypes[] habitatTypes;
    private String generationLength;
    private String generationLengthJustification;
    private RedListEnums.DeclineHabitatQuality declineHabitatQuality;
    private String declineHabitatQualityJustification;

    public String getDeclineHabitatQualityJustification() {
        return declineHabitatQualityJustification;
    }

    public RedListEnums.DeclineHabitatQuality getDeclineHabitatQuality() {
        return declineHabitatQuality;
    }

    public String getDescription() {
        return description;
    }

    public RedListEnums.HabitatTypes[] getHabitatTypes() {
        return habitatTypes == null ? new RedListEnums.HabitatTypes[0] : habitatTypes;
    }

    public String getGenerationLength() {
        return generationLength;
    }

    public String getGenerationLengthJustification() {
        return generationLengthJustification;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setHabitatTypes(RedListEnums.HabitatTypes[] habitatTypes) {
        this.habitatTypes = habitatTypes;
    }

    public void setGenerationLength(String generationLength) {
        this.generationLength = generationLength;
    }

    public void setGenerationLengthJustification(String generationLengthJustification) {
        this.generationLengthJustification = generationLengthJustification;
    }

    public void setDeclineHabitatQuality(RedListEnums.DeclineHabitatQuality declineHabitatQuality) {
        this.declineHabitatQuality = declineHabitatQuality;
    }

    public void setDeclineHabitatQualityJustification(String declineHabitatQualityJustification) {
        this.declineHabitatQualityJustification = declineHabitatQualityJustification;
    }
}
