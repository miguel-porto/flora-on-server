package pt.floraon.driver.entities;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
/**
 * Represents either a node or an edge in the graph.
 * All graph entities must extend this class.
 * @author miguel
 *
 */
public abstract class DBEntity implements Serializable {
	protected String _id = null, _key = null;
	
	/**
	 * 
	 * @return The document ID
	 */
	public String getID() {
		return this._id;
	}

	public String getKey() {
		return this._key;
	}

	public String _getIDURLEncoded() {
		if(this._id == null) return null;
		try {
			return URLEncoder.encode(this._id, StandardCharsets.UTF_8.name());
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public void setID(String id) {
		this._id=id;
	}

	public void setKey(String key) {
		this._key=key;
	}

	public DBEntity() {
	}
	
	public DBEntity(DBEntity n) {
		this._id=n._id;
		this._key=n._key;
	}

	/**
	 * This setter and getter are just for using as a javabean in HTML forms
	 * @return
	 */
	public String getDatabaseId() { return this._id; }

	public void setDatabaseId(String id) { this._id = id; }

	/**
	 * Gets the collection, i.e. the canonical class name.
	 * @return
	 */
	public abstract String getTypeAsString();

	/**
	 * Serializes this entity without any processing.
	 * @return
	 */
	protected JsonObject _toJson() {
		Gson gson = new Gson();
		JsonObject out = gson.toJsonTree(this).getAsJsonObject();
		out.addProperty("type", this.getTypeAsString());
		return out;
	}

	public abstract JsonObject toJson();
	public abstract String toJsonString();
}
