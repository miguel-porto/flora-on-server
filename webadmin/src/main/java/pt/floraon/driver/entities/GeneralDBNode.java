package pt.floraon.driver.entities;

import pt.floraon.driver.Constants.NodeTypes;

public abstract class GeneralDBNode extends DBEntity {
	public GeneralDBNode() {
		super();
	}
	
	public GeneralDBNode(GeneralDBNode n) {
		super(n);
	}
	
	public abstract NodeTypes getType();
}
