package pt.floraon.ecology.entities;

import com.google.gson.JsonObject;
import pt.floraon.driver.Constants;
import pt.floraon.driver.DatabaseException;
import pt.floraon.driver.entities.NamedDBNode;

/**
 * Created by miguel on 23-07-2017.
 */
public class Habitat extends NamedDBNode {
    private String description;
    private Constants.HabitatFacet habitatFacet;
    private Integer level;

    public Habitat() {
        super();
    }

    public Habitat(String name, String description) throws DatabaseException {
        this(name, description, null);
    }

    public Habitat(String name, String description, Constants.HabitatFacet facet) throws DatabaseException {
        super(name);
        this.description = description;
        this.habitatFacet = facet;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Constants.HabitatFacet getHabitatFacet() {
        return habitatFacet;
    }

    public void setHabitatFacet(Constants.HabitatFacet habitatFacet) {
        this.habitatFacet = habitatFacet;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    @Override
    public Constants.NodeTypes getType() {
        return Constants.NodeTypes.habitat;
    }

    @Override
    public String getTypeAsString() {
        return this.getType().toString();
    }

}
