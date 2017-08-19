package pt.floraon.redlistdata.entities;

import pt.floraon.driver.DiffableBean;
import pt.floraon.driver.SafeHTMLString;
import pt.floraon.driver.utils.StringUtils;
import pt.floraon.redlistdata.RedListEnums;

import static pt.floraon.driver.utils.StringUtils.cleanArray;

/**
 * Created by miguel on 18-11-2016.
 */
public class Ecology implements DiffableBean {
    private SafeHTMLString description;
    private String[] habitatTypes;  // NOTE: this is the database ID of the habitat
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

    public SafeHTMLString getDescription() {
        return description == null ? SafeHTMLString.emptyString() : description;
    }

    public String[] getHabitatTypes() {
        return StringUtils.isArrayEmpty(habitatTypes) ? new String[0] : habitatTypes;
    }

    public String getGenerationLength() {
        return generationLength == null ? "" : generationLength;
    }

    public String getGenerationLengthJustification() {
        return generationLengthJustification == null ? "" : generationLengthJustification;
    }

    public void setDescription(SafeHTMLString description) {
        this.description = description;
    }

    public void setHabitatTypes(String[] habitatTypes) {
        this.habitatTypes = cleanArray(habitatTypes);
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
