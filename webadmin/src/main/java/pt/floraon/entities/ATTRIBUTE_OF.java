package pt.floraon.entities;

import pt.floraon.driver.Constants.RelTypes;

public class ATTRIBUTE_OF extends GeneralDBEdge {

	@Override
	public RelTypes getType() {
		return RelTypes.ATTRIBUTE_OF;
	}
	/*public ATTRIBUTE_OF(String from,String to) {
		this._from=from;
		this._to=to;
	}*/
}
