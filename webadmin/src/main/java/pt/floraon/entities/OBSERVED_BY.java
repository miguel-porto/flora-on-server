package pt.floraon.entities;

import pt.floraon.driver.Constants.RelTypes;

public class OBSERVED_BY extends GeneralDBEdge {
	protected Boolean isMainObserver;
	public OBSERVED_BY(Boolean isMainObserver) {
		this.isMainObserver=isMainObserver;
	}
	@Override
	public RelTypes getType() {
		return RelTypes.OBSERVED_BY;
	}

}
