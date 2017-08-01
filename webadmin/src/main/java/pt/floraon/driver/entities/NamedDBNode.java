package pt.floraon.driver.entities;

import com.google.gson.JsonObject;

import pt.floraon.driver.DatabaseException;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public abstract class NamedDBNode extends GeneralDBNode implements Serializable {
	protected String name;

	public NamedDBNode(String name) throws DatabaseException {
		super();
		if(name != null && name.trim().length() == 0) throw new DatabaseException("Node must have a name");
		this.name = name != null ? name.trim() : null;
	}

	public NamedDBNode(Object name) throws DatabaseException {
		super();
		if(name != null && name.toString().trim().length() == 0) throw new DatabaseException("Node must have a name");
		this.name = name != null ? name.toString().trim() : null;
	}

	public NamedDBNode(NamedDBNode n) throws DatabaseException {
		super(n);
		if(n.name != null && n.name.trim().length() == 0) throw new DatabaseException("Node must have a name");
		this.name = n.name != null ? n.name.trim() : null;
	}
	
	public NamedDBNode() {
		super();
	}

	/**
	 * Gets the canonical name of the node
	 * @return
	 */
	public String getName() {
		return this.name;
	}

	public String _getNameURLEncoded() {
		if(this.name == null) return null;
		try {
			return URLEncoder.encode(this.name, StandardCharsets.UTF_8.name());
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
	}

	public void setName(String name) throws DatabaseException {
		if(name != null && name.trim().length() == 0) throw new DatabaseException("Node must have a name");
		this.name = name != null ? name.trim() : null;
	}

	/**
	 * Gets the processed name of the node, subclasses must override if they want to provide some functionality, otherwise the name is returned.
	 * @return
	 */
	public String getFullName() {
		return this.getName();
	}
	
	@Override
	public JsonObject toJson() {
		JsonObject out = super.toJson();
		out.addProperty("fullName", this.getFullName());
		return out;
	}

}
