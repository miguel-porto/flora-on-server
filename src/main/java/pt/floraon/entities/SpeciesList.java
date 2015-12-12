package pt.floraon.entities;

import java.io.IOException;
import java.util.Iterator;

import com.arangodb.ArangoException;
import com.arangodb.entity.marker.VertexEntity;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

import pt.floraon.driver.FloraOnGraph;
import pt.floraon.server.Constants;
import pt.floraon.server.FloraOnException;
import pt.floraon.server.Constants.NodeTypes;

public class SpeciesList extends GeneralNodeWrapper {
	public SpeciesListVertex baseNode;
	private VertexEntity<SpeciesListVertex> vertexEntity=null;
	private FloraOnGraph graph;

	public SpeciesList(Float latitude, Float longitude, Integer year, Integer month, Integer day,Integer precision) {
		super.baseNode=new SpeciesListVertex(latitude, longitude, year, month, day, precision);
		this.baseNode=(SpeciesListVertex)super.baseNode;
	}

	public SpeciesList(FloraOnGraph graph,Float latitude, Float longitude, Integer year, Integer month, Integer day,Integer precision) throws ArangoException {
		super.baseNode=new SpeciesListVertex(latitude, longitude, year, month, day, precision);
		this.baseNode=(SpeciesListVertex)super.baseNode;
		this.graph=graph;
		this.vertexEntity=graph.driver.graphCreateVertex(Constants.TAXONOMICGRAPHNAME, NodeTypes.specieslist.toString(), this.baseNode, false);
		super.baseNode._id=this.vertexEntity.getDocumentHandle();
	}
	
	public SpeciesList(FloraOnGraph graph,Float latitude,Float longitude,Integer year,Integer month,Integer day,Integer precision,Integer area,String pubNotes,Boolean complete,String privNotes,String habitat) throws ArangoException {
		super.baseNode=new SpeciesListVertex(latitude, longitude, year, month, day, precision, area, pubNotes, complete, privNotes, habitat);
		this.baseNode=(SpeciesListVertex)super.baseNode;
		this.graph=graph;
		this.vertexEntity=graph.driver.graphCreateVertex(Constants.TAXONOMICGRAPHNAME, NodeTypes.specieslist.toString(), this.baseNode, false);
		super.baseNode._id=this.vertexEntity.getDocumentHandle();
	}

	public SpeciesList(SpeciesListVertex slv) {
		super.baseNode=slv;
		this.baseNode=(SpeciesListVertex)super.baseNode;
	}
	
	public SpeciesList(FloraOnGraph fog,SpeciesListVertex slv) {
		super.baseNode=slv;
		this.baseNode=(SpeciesListVertex)super.baseNode;
		this.graph=fog;
	}
	
	/**
	 * Constructs a new species list from a JSON document as documented in the wiki.
	 * @param graph
	 * @param sl A {@link JsonObject} as documented <a href="https://github.com/miguel-porto/flora-on-server/wiki/Document-formats-for-uploading-data">here</a>.
	 * @throws FloraOnException
	 * @throws ArangoException
	 */
	public SpeciesList(FloraOnGraph graph,JsonObject sl) throws FloraOnException, ArangoException {
		if(!(sl.has("latitude") && sl.has("longitude") && sl.has("precision") && sl.has("authors") && sl.has("taxa"))) throw new FloraOnException("Species list document must have at least the fields latitude, longitude, precision, authors, taxa.");
		JsonElement tmp;
		// TODO HERE!!!
		super.baseNode=new SpeciesListVertex(
			sl.get("latitude").getAsFloat()
			, sl.get("longitude").getAsFloat()
			, (tmp=sl.get("year")) == null ? null : tmp.getAsInt()
			, (tmp=sl.get("month")) == null ? null : tmp.getAsInt()
			, (tmp=sl.get("day")).isJsonNull() ? null : tmp.getAsInt()
			, sl.get("precision").getAsInt()
			, (tmp=sl.get("area")) == null ? null : tmp.getAsInt()
			, (tmp=sl.get("pubNotes")) == null ? null : tmp.getAsString()
			, (tmp=sl.get("complete")) == null ? null : tmp.getAsBoolean()
			, (tmp=sl.get("privNotes")).isJsonNull() ? null : tmp.getAsString()
			, (tmp=sl.get("habitat")) == null ? null : tmp.getAsString()
			);
		this.baseNode=(SpeciesListVertex)super.baseNode;
		this.graph=graph;
		this.vertexEntity=graph.driver.graphCreateVertex(Constants.TAXONOMICGRAPHNAME, NodeTypes.specieslist.toString(), this.baseNode, false);
		super.baseNode._id=this.vertexEntity.getDocumentHandle();
	}

	public void setPrecision(Integer precision) {
		baseNode.precision=precision;
		this.dirty=true;
	}
	
	public Integer getPrecision() {
		return baseNode.precision;
	}

	@Override
	public String toString() {
		return "Species list at "+baseNode.location[0]+"º "+baseNode.location[1]+"º on "+baseNode.year+"/"+baseNode.month+"/"+baseNode.day;
	}
	/**
	 * Create a new OBSERVED_BY between this species list and an author. If it exists already, nothing happens.
	 * @param aut The {@link Author}
	 * @param isMainAuthor Whether this is the main author (whose name comes first) or a secondary author (order arbitrary)
	 * @return 1 if relation was created, 0 if not
	 * @throws IOException
	 * @throws ArangoException
	 */
	public int setObservedBy(Author aut,Boolean isMainAuthor) throws FloraOnException, ArangoException {
// TODO if it is main observer, can only be one!
		if(baseNode._id==null) throw new FloraOnException("Species list not attached to DB");
		String query=String.format("FOR au IN author FILTER au.idAut==%2$d UPSERT {_from:'%1$s',_to:au._id} INSERT {_from:'%1$s',_to:au._id,main:%3$b} UPDATE {} IN OBSERVED_BY RETURN OLD ? 0 : 1",baseNode.getID(),aut.baseNode.idAut,isMainAuthor);
//		System.out.println(query);
		return this.graph.driver.executeAqlQuery(query,null,null,Integer.class).getUniqueResult();
	}

	/**
	 * Create a new OBSERVED_BY between this species list and an author. If it exists already, nothing happens.
	 * @param idaut The ID of the author
	 * @param isMainAuthor
	 * @return
	 * @throws IOException
	 * @throws ArangoException
	 */
	public int setObservedBy(int idaut,Boolean isMainAuthor) throws IOException, ArangoException {
		if(baseNode._id==null || this.graph==null) throw new IOException("Species list not attached to DB");
		String query=String.format("FOR au IN author FILTER au.idAut==%2$d UPSERT {_from:'%1$s',_to:au._id} INSERT {_from:'%1$s',_to:au._id,main:%3$b} UPDATE {} IN OBSERVED_BY RETURN OLD ? 0 : 1",baseNode.getID(),idaut,isMainAuthor);
//		System.out.println(query);
		return this.graph.driver.executeAqlQuery(query,null,null,Integer.class).getUniqueResult();
	}

	/**
	 * Gets all the species of this species list
	 * @return
	 */
	public Iterator<TaxEntVertex> getSpecies() {
		// TODO Gets all the species of this species list
		return null;
	}

	@Override
	public void commit() {
		// TODO Auto-generated method stub
		
	}
}
