package pt.floraon.morphology.entities;

import com.google.gson.JsonObject;

import pt.floraon.driver.Constants.RelTypes;
import pt.floraon.driver.entities.GeneralDBEdge;

public class HAS_QUALITY extends GeneralDBEdge {
	public HAS_QUALITY() {
		super();
	}

	public HAS_QUALITY(String from, String to) {
		super(from, to);
	}

	@Override
	public String getTypeAsString() {
		return this.getType().toString();
	}

	@Override
	public RelTypes getType() {
		return RelTypes.HAS_QUALITY;
	}

}
