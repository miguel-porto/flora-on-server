package pt.floraon.entities;

import com.arangodb.entity.EntityFactory;

public class GeneralDBEdge {
	protected String _id;
	protected String _from;
	protected String _to;
	
	public String toJSONString() {
		return EntityFactory.toJsonString(this);
	}
}
