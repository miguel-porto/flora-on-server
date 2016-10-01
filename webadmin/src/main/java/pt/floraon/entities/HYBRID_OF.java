package pt.floraon.entities;

import com.google.gson.JsonObject;

import pt.floraon.driver.Constants.RelTypes;

public class HYBRID_OF extends GeneralDBEdge {
	
	@Override
	public String getTypeAsString() {
		return this.getType().toString();
	}

	@Override
	public RelTypes getType() {
		return RelTypes.HYBRID_OF;
	}

	@Override
	public JsonObject toJson() {
		return super._toJson();
	}

	@Override
	public String toJsonString() {
		return this.toJson().toString();
	}

}
