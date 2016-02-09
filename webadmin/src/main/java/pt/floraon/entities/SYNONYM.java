package pt.floraon.entities;

import pt.floraon.driver.Constants.RelTypes;

public class SYNONYM extends GeneralDBEdge {
	public boolean current=true;
	@Override
	public RelTypes getType() {
		return RelTypes.SYNONYM;
	}

}
