package pt.floraon.taxonomy.entities;

import com.google.gson.JsonObject;

import pt.floraon.driver.Constants.RelTypes;
import pt.floraon.driver.entities.GeneralDBEdge;

public class SYNONYM extends GeneralDBEdge {

	public SYNONYM() {
		super();
	}

	public SYNONYM(String from, String to) {
		super(from, to);
	}

	@Override
	public String getTypeAsString() {
		return this.getType().toString();
	}

	@Override
	public RelTypes getType() {
		return RelTypes.SYNONYM;
	}

}
