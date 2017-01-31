package pt.floraon.redlistdata.entities;

import pt.floraon.driver.DiffableBean;
import pt.floraon.redlistdata.RedListEnums;

/**
 * Created by miguel on 18-11-2016.
 */
public class Ecology implements DiffableBean {
    private String description;
    private RedListEnums.HabitatTypes[] habitatTypes;
    private String generationLength;
    private String generationLengthJustification;
    private RedListEnums.DeclineHabitatQuality declineHabitatQuality;
    private String declineHabitatQualityJustification;

    public String getDeclineHabitatQualityJustification() {
        return declineHabitatQualityJustification == null ? "" : declineHabitatQualityJustification;
    }

    public RedListEnums.DeclineHabitatQuality getDeclineHabitatQuality() {
        return declineHabitatQuality == null ? RedListEnums.DeclineHabitatQuality.NO_INFORMATION : declineHabitatQuality;
    }

    public String getDescription() {
        return description == null ? "" : description;
    }

    public RedListEnums.HabitatTypes[] getHabitatTypes() {
        return habitatTypes == null
                || habitatTypes.length == 0
                || habitatTypes[0] == null
                ? new RedListEnums.HabitatTypes[]{null}
                : habitatTypes;
//        return habitatTypes == null ? new RedListEnums.HabitatTypes[0] : habitatTypes;
    }

    public String getGenerationLength() {
        return generationLength == null ? "" : generationLength;
    }

    public String getGenerationLengthJustification() {
        return generationLengthJustification == null ? "" : generationLengthJustification;
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
