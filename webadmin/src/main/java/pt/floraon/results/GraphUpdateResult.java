package pt.floraon.results;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.arangodb.ArangoDriver;
import com.arangodb.ArangoException;
import com.arangodb.entity.DocumentEntity;
import com.arangodb.entity.EntityFactory;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import pt.floraon.driver.FloraOnException;
import pt.floraon.driver.FloraOn;
import pt.floraon.driver.GraphUpdateResultInt;
import pt.floraon.driver.INodeKey;

/**
 * Represents a response composed of an array of nodes and an array of links. It's intended for updating a graph with new or updated nodes.
 * It can be constructed from an array of document handles, or directly from a JSON string (in this case, no processing is done).
 * If constructed from document handles, when converting toString() a query is issued to fetch the respective documents.
 * @author miguel
 *
 */
public class GraphUpdateResult extends GraphUpdateResultInt {
	@SuppressWarnings("rawtypes")
	private List<Map> nodes=null;
	@SuppressWarnings("rawtypes")
	private List<Map> links=null;

	private ArangoDriver dbDriver;
	
	public GraphUpdateResult() {
		super();
	}

	public GraphUpdateResult(String json) {
		super(json);
	}

	public GraphUpdateResult(FloraOn graph,String id) throws FloraOnException {
		super(graph, graph.asNodeKey(id));
		this.dbDriver=(ArangoDriver) graph.getArangoDriver();
	}
	
	public GraphUpdateResult(FloraOn graph,INodeKey id) {
		super(graph, id);
		this.dbDriver=(ArangoDriver) graph.getArangoDriver();
	}
	
	public GraphUpdateResult(FloraOn graph,String[] ids) {
		super(graph, ids);
		this.dbDriver=(ArangoDriver) graph.getArangoDriver();
	}

	@Override
	@SuppressWarnings("rawtypes")
	public JsonElement toJsonObject() {
		if(this.jsonRepresentation!=null) return (new JsonParser()).parse(this.jsonRepresentation);
		if(this.nodes==null && this.links==null && this.documentHandles==null) return GraphUpdateResult.emptyResultJson();
		
		if(this.documentHandles!=null) {
			Map<?,?> tmp;
			Object tmp1;
			this.links=new ArrayList<Map>();
			this.nodes=new ArrayList<Map>();
			try {
				Iterator<DocumentEntity<Map>> dc=dbDriver.executeDocumentQuery("FOR d IN DOCUMENT("+EntityFactory.toJsonString(this.documentHandles)+") RETURN MERGE(d,{type:PARSE_IDENTIFIER(d._id).collection})", null, null, Map.class).iterator();
				while(dc.hasNext()) {
					tmp=dc.next().getEntity();
					tmp1=tmp.get("_from");
					if(tmp1!=null) {	// it's an edge
						this.links.add(tmp);
					} else {	// it's a vertex
						this.nodes.add(tmp);
					}
				}
			} catch (ArangoException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			JsonObject out=new JsonObject();
			out.add("nodes", EntityFactory.toJsonElement(this.nodes, false));
			out.add("links", EntityFactory.toJsonElement(this.links, false));
			return out;
		}
		return GraphUpdateResult.emptyResultJson();
	}
	

	public static GraphUpdateResult emptyResult() {
		return new GraphUpdateResult();
	}
		
}
