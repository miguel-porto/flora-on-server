package pt.floraon.entities;

import pt.floraon.driver.Constants.RelTypes;

public class HAS_QUALITY extends GeneralDBEdge {
/*	public HAS_QUALITY() {}
	public HAS_QUALITY(String from,String to) {
		this._from=from;
		this._to=to;
	}*/

	@Override
	public RelTypes getType() {
		return RelTypes.HAS_QUALITY;
	}

}
