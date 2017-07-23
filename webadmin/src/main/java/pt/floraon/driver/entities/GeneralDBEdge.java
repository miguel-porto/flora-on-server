package pt.floraon.driver.entities;

import pt.floraon.driver.Constants.RelTypes;

public abstract class GeneralDBEdge extends DBEntity {
	protected String _from;
	protected String _to;

	public GeneralDBEdge() {
		super();
	}
	
	public GeneralDBEdge(GeneralDBEdge n) {
		super(n);
		this._from = n._from;
		this._to = n._to;
	}

	public GeneralDBEdge(String from, String to) {
		this._from = from;
		this._to = to;
	}

	public void setFrom(String _from) {
		this._from = _from;
	}

	public void setTo(String _to) {
		this._to = _to;
	}

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
