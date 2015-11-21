package pt.floraon.results;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.arangodb.ArangoException;
import com.arangodb.entity.DocumentEntity;
import com.arangodb.entity.EntityFactory;

import pt.floraon.dbworker.FloraOnGraph;

/**
 * Represents a response composed of an array of nodes and an array of links. It's intended for updating a graph with new or updated nodes.
 * It can be constructed from an array of document handles, or directly from a JSON string (in this case, no processing is done).
 * If constructed from document handles, when converting toString() a query is issued to fetch the respective documents.
 * @author miguel
 *
 */
public class GraphUpdateResult {
	@SuppressWarnings("rawtypes")
	private List<Map> nodes=null;
	@SuppressWarnings("rawtypes")
	private List<Map> links=null;
	private String jsonRepresentation=null;
	private List<String> documentHandles=null;
	private FloraOnGraph graph;
		
	/**
	 * Returns a JSON representation of the results
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public String toString() {
		if(this.jsonRepresentation!=null) return this.jsonRepresentation;
		if(this.nodes==null && this.links==null && this.documentHandles==null) return "{\"nodes\":[],\"links\":[]}";
		
		if(this.documentHandles!=null) {
			Map<?,?> tmp;
			Object tmp1;
			this.links=new ArrayList<Map>();
			this.nodes=new ArrayList<Map>();
			try {
				Iterator<DocumentEntity<Map>> dc=graph.driver.executeDocumentQuery("FOR d IN DOCUMENT("+EntityFactory.toJsonString(this.documentHandles)+") RETURN MERGE(d,{type:PARSE_IDENTIFIER(d._id).collection})", null, null, Map.class).iterator();
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
			return "{\"nodes\":"+EntityFactory.toJsonString(this.nodes)+",\"links\":"+EntityFactory.toJsonString(this.links)+"}";
		}
		return "{\"nodes\":[],\"links\":[]}";
	}
	
	public static GraphUpdateResult emptyResult() {
		return new GraphUpdateResult();
	}
	
	public static GraphUpdateResult fromJson(String json) {
		GraphUpdateResult out=new GraphUpdateResult();
		out.jsonRepresentation=json;
		return out;
	}
	
	public static GraphUpdateResult fromHandle(FloraOnGraph graph,String id) {
		GraphUpdateResult out=new GraphUpdateResult();
		out.graph=graph;
		out.documentHandles=new ArrayList<String>();
		out.documentHandles.add(id);
		return out;
	}
	
	public static GraphUpdateResult fromHandles(FloraOnGraph graph,String[] ids) {
		GraphUpdateResult out=new GraphUpdateResult();
		out.graph=graph;
		out.documentHandles=new ArrayList<String>();
		out.documentHandles.addAll(Arrays.asList(ids));
		return out;
	}

}
