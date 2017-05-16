package pt.floraon.driver.results;

import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import pt.floraon.driver.FloraOnException;
import pt.floraon.driver.Constants.DocumentType;
import pt.floraon.driver.IFloraOn;
import pt.floraon.driver.GraphUpdateResultInt;
import pt.floraon.driver.INodeKey;

/**
 * Represents a response composed of an array of _nodes and an array of _links. It's intended for updating a graph with new or updated _nodes.
 * It can be constructed from an array of document handles, or directly from a JSON string (in this case, no processing is done).
 * If constructed from document handles, when converting toString() a query is issued to fetch the respective documents.
 * @author miguel
 *
 */
public class GraphUpdateResult extends GraphUpdateResultInt {
	private transient JsonArray _nodes;
	private transient JsonArray _links;
	
	public GraphUpdateResult() {
		super();
	}

	public GraphUpdateResult(String json) {
		super(json);
	}

	public GraphUpdateResult(String nodes, String links) {
		super("{\"nodes\": " + nodes + ", \"links\": " + links + "}");
	}

	public GraphUpdateResult(IFloraOn graph, String id) throws FloraOnException {
		super(graph, graph.asNodeKey(id));
	}
	
	public GraphUpdateResult(IFloraOn graph, INodeKey id) {
		super(graph, id);
	}
	
	public GraphUpdateResult(IFloraOn graph, String[] ids) {
		super(graph, ids);
	}

	public GraphUpdateResult(IFloraOn graph, List<String> ids) {
		super(graph, ids);
	}

	@Override
	public JsonElement toJsonObject() {
		if(this.jsonRepresentation != null) return (new JsonParser()).parse(this.jsonRepresentation);
		if(this._nodes == null && this._links == null && this.documentHandles == null) return GraphUpdateResult.emptyResultJson();
		this._nodes = new JsonArray();
		this._links = new JsonArray();
		if(this.documentHandles != null) {
			for(String id : this.documentHandles) {
				try {
					if(this.driver.asNodeKey(id).getDocumentType() == DocumentType.VERTEX) {
						this._nodes.add(this.driver.getNodeWorkerDriver().getNode(this.driver.asNodeKey(id)).toJson());
					} else {
						this._links.add(this.driver.getNodeWorkerDriver().getNode(this.driver.asNodeKey(id)).toJson());
					}
				} catch(FloraOnException e) {
					System.out.println("Skipped " + id + e.getMessage());
					// skip ID
				}
			}
			JsonObject out=new JsonObject();
			out.add("nodes", this._nodes);
			out.add("links", this._links);
			return out;
		}
		return GraphUpdateResult.emptyResultJson();
	}
	

	public static GraphUpdateResult emptyResult() {
		return new GraphUpdateResult();
	}
		
}
