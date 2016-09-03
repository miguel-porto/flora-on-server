package pt.floraon.entities;

import com.arangodb.entity.EntityFactory;

import pt.floraon.driver.Constants.RelTypes;

public abstract class GeneralDBEdge {
	protected String _id;
	protected String _from;
	protected String _to;
	
	public String toJSONString() {
		return EntityFactory.toJsonString(this);
	}
	
	public String getID() {
		return this._id;
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

	public abstract RelTypes getType();
}
