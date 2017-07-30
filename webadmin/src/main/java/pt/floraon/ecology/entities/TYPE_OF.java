package pt.floraon.ecology.entities;

import com.google.gson.JsonObject;
import pt.floraon.driver.Constants;
import pt.floraon.driver.entities.GeneralDBEdge;

/**
 * Created by miguel on 23-07-2017.
 */
public class TYPE_OF extends GeneralDBEdge {
    public TYPE_OF() {
        super();
    }

    public TYPE_OF(String from, String to) {
        super(from, to);
    }

    @Override
    public Constants.RelTypes getType() {
        return Constants.RelTypes.TYPE_OF;
    }

    @Override
    public String getTypeAsString() {
        return this.getType().toString();
    }

    @Override
    public JsonObject toJson() {
        return super._toJson();
    }

    @Override
    public String toJsonString() {
        return null;
    }
}
