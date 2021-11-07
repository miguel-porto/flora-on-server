package pt.floraon.arangodriver;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pt.floraon.driver.Constants;
import pt.floraon.driver.FloraOnException;
import pt.floraon.driver.interfaces.INodeKey;
import pt.floraon.driver.Constants.DocumentType;

public class ArangoKey implements INodeKey {
	private static Pattern keyPattern = Pattern.compile("^([a-zA-Z_]+)/([0-9a-zA-Z_-]+)$");
	private String key = null;
	private String collection = null;
	private String id = null;
	
	public ArangoKey(String id) throws FloraOnException {
		if(id==null) return;
		Matcher mat = ArangoKey.keyPattern.matcher(id);
		if(!mat.matches()) throw new FloraOnException("Invalid key provided: "+id);
		this.id=id;
		this.collection = mat.group(1);
		this.key = mat.group(2);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		ArangoKey arangoKey = (ArangoKey) o;

		return id.equals(arangoKey.id);
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public String toString() {
		return this.getID();
	}
	
	@Override
	public String getID() {
		return this.id;
	}

	@Override
	public String getDBKey() {
		return this.key;
	}

	@Override
	public String getCollection() {
		return this.collection;
	}

	@Override
	public DocumentType getDocumentType() {
		try {
			Constants.RelTypes.valueOf(this.collection);
		} catch (IllegalArgumentException e) {
			try {
				Constants.NodeTypes.valueOf(this.collection);
			} catch (IllegalArgumentException e1) {
				return DocumentType.NONE;
			}
			return DocumentType.VERTEX;
		}
		return DocumentType.EDGE;
	}

}
