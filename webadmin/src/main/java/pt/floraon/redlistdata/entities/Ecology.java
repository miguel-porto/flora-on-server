package pt.floraon.redlistdata.entities;

import pt.floraon.driver.DiffableBean;
import pt.floraon.driver.datatypes.IntegerInterval;
import pt.floraon.driver.datatypes.SafeHTMLString;
import pt.floraon.driver.utils.StringUtils;
import pt.floraon.redlistdata.RedListEnums;

import static pt.floraon.driver.utils.StringUtils.cleanArray;

/**
 * Created by miguel on 18-11-2016.
 */
public class Ecology implements DiffableBean {
    private SafeHTMLString description;
    private String[] habitatTypes;  // NOTE: this is the database ID of the habitat
    private IntegerInterval generationLength;
    private SafeHTMLString generationLengthJustification;
    private RedListEnums.DeclineHabitatQuality declineHabitatQuality;
    private SafeHTMLString declineHabitatQualityJustification;

    public SafeHTMLString getDeclineHabitatQualityJustification() {
        return declineHabitatQualityJustification == null ? SafeHTMLString.emptyString() : declineHabitatQualityJustification;
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

    public IntegerInterval getGenerationLength() {
        return generationLength;
    }

    public SafeHTMLString getGenerationLengthJustification() {
        return generationLengthJustification == null ? SafeHTMLString.emptyString() : generationLengthJustification;
    }

    public void setDescription(SafeHTMLString description) {
        this.description = description;
    }

    public void setHabitatTypes(String[] habitatTypes) {
        this.habitatTypes = cleanArray(habitatTypes);
    }

    public void setGenerationLength(IntegerInterval generationLength) {
        this.generationLength = generationLength;
    }

    public void setGenerationLengthJustification(SafeHTMLString generationLengthJustification) {
        this.generationLengthJustification = generationLengthJustification;
    }

    public void setDeclineHabitatQuality(RedListEnums.DeclineHabitatQuality declineHabitatQuality) {
        this.declineHabitatQuality = declineHabitatQuality;
    }

    public void setDeclineHabitatQualityJustification(SafeHTMLString declineHabitatQualityJustification) {
        this.declineHabitatQualityJustification = declineHabitatQualityJustification;
    }
}
