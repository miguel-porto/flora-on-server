package pt.floraon.entities;

import pt.floraon.driver.Constants.RelTypes;

public class BELONGS_TO extends GeneralDBEdge {

	@Override
	public RelTypes getType() {
		return RelTypes.BELONGS_TO;
	}

}
