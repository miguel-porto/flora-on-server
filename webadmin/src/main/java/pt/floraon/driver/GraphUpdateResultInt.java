package pt.floraon.driver;

import java.util.*;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import pt.floraon.driver.interfaces.IFloraOn;
import pt.floraon.driver.interfaces.INodeKey;

public abstract class GraphUpdateResultInt {
	protected transient String jsonRepresentation=null;
	protected transient Set<String> documentHandles=null;
	protected transient IFloraOn driver;

	public GraphUpdateResultInt() {
	}
	
	public GraphUpdateResultInt(String json) {
		this.jsonRepresentation=json;
	}
	
	public GraphUpdateResultInt(IFloraOn graph, INodeKey id) {
		this.driver=graph;
		this.documentHandles=new HashSet<>();
		this.documentHandles.add(id.toString());
	}

	public GraphUpdateResultInt(IFloraOn graph, String[] ids) {
		this.driver=graph;
		this.documentHandles=new HashSet<>();

		this.documentHandles.addAll(Arrays.asList(ids));
	}

	public GraphUpdateResultInt(IFloraOn graph, List<String> ids) {
		this.driver=graph;
		this.documentHandles = new HashSet<>(ids);
	}

	protected static JsonObject emptyResultJson() {
		JsonObject out=new JsonObject();
		out.add("nodes", new JsonArray());
		out.add("links", new JsonArray());
		return out;
	}

	/**
	 * Returns a JSON representation of the results
	 */
	@Override
	public String toString() {
		return this.toJsonObject().toString();
	}

	public Set<String> getUpdatedHandles() {
		return documentHandles;
	}

	public abstract JsonElement toJsonObject();
}
