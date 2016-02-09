package pt.floraon.entities;

public abstract class GeneralDBNode {
	protected String _id,_key;
	
	/**
	 * 
	 * @return The document handle
	 */
	public String getID() {
		return _id;
	}
	
	public void setID(String id) {
		this._id=id;
	}

	public void setKey(String key) {
		this._key=key;
	}

	public GeneralDBNode(GeneralDBNode n) {
		this._id=n._id;
		this._key=n._key;
	}
	
	public GeneralDBNode() {
		this._id=null;
		this._key=null;
	}
}
