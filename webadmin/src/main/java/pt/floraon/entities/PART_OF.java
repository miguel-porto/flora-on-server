package pt.floraon.entities;

import pt.floraon.driver.Constants.RelTypes;

public class PART_OF extends GeneralDBEdge {
	public boolean current;
	
	public PART_OF() {
		this.current=true;
	}
	
	public PART_OF(boolean current) {
		this.current=current;
	}
	@Override
	public RelTypes getType() {
		return RelTypes.PART_OF;
	}

}
