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
	
	public abstract RelTypes getType();
}
