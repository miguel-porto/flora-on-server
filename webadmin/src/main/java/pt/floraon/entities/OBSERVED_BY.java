package pt.floraon.entities;

public class OBSERVED_BY extends GeneralDBEdge {
	protected Boolean isMainObserver;
	public OBSERVED_BY(Boolean isMainObserver) {
		this.isMainObserver=isMainObserver;
	}
}
