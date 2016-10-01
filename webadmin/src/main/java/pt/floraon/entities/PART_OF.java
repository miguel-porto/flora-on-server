package pt.floraon.entities;

import com.google.gson.JsonObject;

import pt.floraon.driver.Constants.RelTypes;

public class PART_OF extends GeneralDBEdge {
/*	public boolean current;
	
	public PART_OF() {
		this.current=true;
	}
	
	public PART_OF(boolean current) {
		this.current=current;
	}*/
	
	@Override
	public String getTypeAsString() {
		return this.getType().toString();
	}

	@Override
	public RelTypes getType() {
		return RelTypes.PART_OF;
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
