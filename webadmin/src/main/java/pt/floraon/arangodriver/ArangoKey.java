package pt.floraon.arangodriver;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pt.floraon.driver.FloraOnException;
import pt.floraon.driver.INodeKey;

public class ArangoKey implements INodeKey {
	private static Pattern keyPattern=Pattern.compile("^[a-zA-Z_]+/([0-9]+)$");
	private String key=null;
	private String id=null;
	
	public ArangoKey(String id) throws FloraOnException {
		if(id==null) return;
		Matcher mat=ArangoKey.keyPattern.matcher(id);
		if(!mat.matches()) throw new FloraOnException("Invalid key provided: "+id);
		this.id=id;
		this.key=mat.group(1);
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

}
