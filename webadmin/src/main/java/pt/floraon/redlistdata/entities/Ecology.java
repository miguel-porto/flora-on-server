package pt.floraon.redlistdata.entities;

/**
 * Created by miguel on 18-11-2016.
 */
public class Ecology {
    private String description;
    private RedListEnums.HabitatTypes[] habitatTypes = new RedListEnums.HabitatTypes[0];
    private RedListEnums.GenerationLength generationLength;

    public String getDescription() {
        return description;
    }

    public RedListEnums.HabitatTypes[] getHabitatTypes() {
        return habitatTypes;
    }

    public RedListEnums.GenerationLength getGenerationLength() {
        return generationLength;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setHabitatTypes(RedListEnums.HabitatTypes[] habitatTypes) {
        this.habitatTypes = habitatTypes;
    }

    public void setGenerationLength(RedListEnums.GenerationLength generationLength) {
        this.generationLength = generationLength;
    }
}
