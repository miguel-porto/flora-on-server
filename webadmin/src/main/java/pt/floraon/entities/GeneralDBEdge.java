package pt.floraon.entities;

import pt.floraon.driver.Constants.RelTypes;

public abstract class GeneralDBEdge extends DBEntity {
	public GeneralDBEdge() {
		super();
	}
	
	public GeneralDBEdge(GeneralDBEdge n) {
		super(n);
	}

	protected String _from;
	protected String _to;
	
	/**
	 * Returns the DB ID of the vertex from which this edge departs.
	 * @return
	 */
	public String getFrom() {
		return this._from;
	}

	/**
	 * Returns the DB ID of the vertex to which this edge goes.
	 * @return
	 */
	public String getTo() {
		return this._to;
	}

	/**
	 * Gets the type of this edge, as an enum value.
	 * @return
	 */
	public abstract RelTypes getType();
}
