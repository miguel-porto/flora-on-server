package pt.floraon.driver;

import java.util.regex.Pattern;

import com.arangodb.ArangoException;

public class ArangoKey {
	private static Pattern keyPattern=Pattern.compile("^[a-zA-Z_]+/[0-9]+$");
	private String key;
	
	public static ArangoKey fromString(String id) throws ArangoException {
		if(!ArangoKey.keyPattern.matcher(id).matches()) throw new ArangoException("Invalid key provided: "+id);
		ArangoKey out=new ArangoKey();
		out.key=id;
		return out;
	}
	
	@Override
	public String toString() {
		return this.key;
	}
}
