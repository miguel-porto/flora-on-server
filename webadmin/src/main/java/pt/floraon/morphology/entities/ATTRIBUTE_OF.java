package pt.floraon.morphology.entities;

import com.google.gson.JsonObject;

import pt.floraon.driver.Constants.RelTypes;
import pt.floraon.driver.entities.GeneralDBEdge;

public class ATTRIBUTE_OF extends GeneralDBEdge {

	public ATTRIBUTE_OF() {
		super();
	}

	public ATTRIBUTE_OF(String from, String to) {
		super(from, to);
	}

	@Override
	public String getTypeAsString() {
		return this.getType().toString();
	}

	@Override
	public RelTypes getType() {
		return RelTypes.ATTRIBUTE_OF;
	}

}
