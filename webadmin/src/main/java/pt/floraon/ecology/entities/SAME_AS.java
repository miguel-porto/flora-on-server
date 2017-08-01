package pt.floraon.ecology.entities;

import com.google.gson.JsonObject;
import pt.floraon.driver.Constants;
import pt.floraon.driver.entities.GeneralDBEdge;

/**
 * Created by miguel on 23-07-2017.
 */
public class SAME_AS extends GeneralDBEdge {
    public SAME_AS() {
        super();
    }

    public SAME_AS(String from, String to) {
        super(from, to);
    }

    @Override
    public Constants.RelTypes getType() {
        return Constants.RelTypes.SAME_AS;
    }

    @Override
    public String getTypeAsString() {
        return this.getType().toString();
    }

}
