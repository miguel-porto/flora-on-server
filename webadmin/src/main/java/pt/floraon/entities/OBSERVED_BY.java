package pt.floraon.entities;

import com.google.gson.JsonObject;

import pt.floraon.driver.Constants.RelTypes;

public class OBSERVED_BY extends GeneralDBEdge {
	protected Boolean isMainObserver;

	public OBSERVED_BY() {
		super();
	}

	public OBSERVED_BY(String from, String to) {
		super(from, to);
	}

	public OBSERVED_BY(Boolean isMainObserver) {
		this();
		this.isMainObserver=isMainObserver;
	}
	
	@Override
	public String getTypeAsString() {
		return this.getType().toString();
	}

	@Override
	public RelTypes getType() {
		return RelTypes.OBSERVED_BY;
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
