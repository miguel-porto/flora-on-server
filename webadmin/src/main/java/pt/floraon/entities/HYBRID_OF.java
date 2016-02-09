package pt.floraon.entities;

import pt.floraon.driver.Constants.RelTypes;

public class HYBRID_OF extends GeneralDBEdge {
	@Override
	public RelTypes getType() {
		return RelTypes.HYBRID_OF;
	}

}
