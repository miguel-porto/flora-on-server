package pt.floraon.entities;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.arangodb.ArangoException;
import com.arangodb.entity.EntityFactory;
import com.arangodb.entity.marker.VertexEntity;
import com.google.gson.internal.LinkedTreeMap;

import pt.floraon.dbworker.FloraOnGraph;
import pt.floraon.dbworker.TaxEntName;
import pt.floraon.server.Constants;
import pt.floraon.server.Constants.AllRelTypes;
import pt.floraon.server.Constants.NativeStatus;
import pt.floraon.server.Constants.NodeTypes;
import pt.floraon.server.Constants.PhenologicalStates;
import pt.floraon.server.Constants.TaxonRanks;

/**
 * A wrapper for the taxonomic nodes, providing a series of convenience functions.
 * @author Miguel Porto
 *
 */
public class TaxEnt extends TaxEntVertex implements VertexWrapper,ResultItem {
	private VertexEntity<TaxEntVertex> vertexEntity=null;
	private FloraOnGraph graph;
	private Boolean dirty;
	{
		this.dirty=false;
		this.vertexEntity=null;
	}

	public TaxEnt(TaxEntVertex tev) {
		super(tev);
	}
	
	public TaxEnt(FloraOnGraph graph,TaxEntVertex tev) {
		super(tev);
		this.graph=graph;
	}

	public TaxEnt(LinkedTreeMap<String,Object> tev) {
		super(tev);
	}

	public TaxEnt(FloraOnGraph graph,VertexEntity<TaxEntVertex> ve) {
		super(ve.getEntity());
		this.graph=graph;
		this.vertexEntity=ve;
		super._id=this.vertexEntity.getDocumentHandle();
		super._key=this.vertexEntity.getDocumentKey();
	}

	/**
	 * Create a new detached node (i.e. not saved in DB)
	 * @param name
	 * @param rank
	 * @param annotation
	 */
	public TaxEnt(String name,Integer rank,String author,String annotation) {
		super(name,rank,author,annotation,null,null);
	}

	/**
	 * Create a new node and add it to DB.
	 * @param graph The graph instance
	 * @param tname The name
	 * @throws ArangoException
	 */
	public TaxEnt(FloraOnGraph graph,TaxEntName tname,Boolean current) throws ArangoException {
		super(tname.name,tname.rank == null ? null : tname.rank.getValue(),tname.author,null,current,null);
		this.graph=graph;
		this.vertexEntity=graph.driver.graphCreateVertex(Constants.TAXONOMICGRAPHNAME, NodeTypes.taxent.toString(), new TaxEntVertex(this), false);
		super._id=this.vertexEntity.getDocumentHandle();
		super._key=this.vertexEntity.getDocumentKey();
	}

	/**
	 * Create a new node and add it to DB.
	 * @param graph The graph instance
	 * @param name
	 * @param author
	 * @param rank
	 * @param annotation
	 * @param current
	 * @throws ArangoException
	 */
	public TaxEnt(FloraOnGraph graph,String name,String author,TaxonRanks rank,String annotation,Boolean current) throws ArangoException {
		super(name,rank == null ? null : rank.getValue(),author,annotation,current,null);
		this.graph=graph;
		this.vertexEntity=graph.driver.graphCreateVertex(Constants.TAXONOMICGRAPHNAME, NodeTypes.taxent.toString(), new TaxEntVertex(this), false);
		super._id=this.vertexEntity.getDocumentHandle();
		super._key=this.vertexEntity.getDocumentKey();
	}
	
	/**
	 * Gets the corresponding VertexEntity in the DB
	 * @return
	 */
	public VertexEntity<TaxEntVertex> getVertexEntity() {
		return this.vertexEntity;
	}

	/**
	 * Gets the taxon canonical name.
	 * @return
	 */
	public String getName() {
		return name;
	}

	public void setName(String name) {
		if(Objects.equals(name, this.name)) return;
		this.name = name;
		this.dirty=true;
	}
	
	/**
	 * Gets the taxon name with authorship and annotations.
	 * @return
	 */
	public String getFullName() {
		return this.getName()+(this.author!=null ? " "+this.getAuthor() : "")+(this.annotation!=null ? " ["+this.getAnnotation()+"]" : "");
	}

	public TaxonRanks getRank() {
		return TaxonRanks.getRankFromValue(this.rank);
	}
	
	public Integer getRankValue() {
		return rank;
	}

	public void setRank(Integer rank) {
		if(Objects.equals(rank, this.rank)) return;
		this.rank = rank;
		this.dirty=true;
	}

	public String getAnnotation() {
		return annotation;
	}

	public void setAnnotation(String annotation) {
		if(Objects.equals(annotation, this.annotation)) return;
		this.annotation = annotation;
		this.dirty=true;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		if(Objects.equals(author, this.author)) return;
		this.author = author;
		this.dirty=true;
	}

	public Boolean getCurrent() {
		return current;
	}

	public void setCurrent(Boolean current) {
		if(Objects.equals(current, this.current)) return;
		this.current = current;
		this.dirty=true;
	}

	public Integer getOldId() {
		return oldId;
	}

	public void setOldId(Integer oldId) {
		if(Objects.equals(oldId, this.oldId)) return;
		this.oldId = oldId;
		this.dirty=true;
	}

	/**
	 * Flushes all changes in the node to the DB
	 * @throws IOException 
	 * @throws ArangoException 
	 */
	@Override
	public void saveToDB() throws IOException, ArangoException {
		if(!this.dirty) return;
		if(this._id==null) throw new IOException("Node "+this.name+" not attached to DB");
// TODO check whether it gets updated!
		this.graph.driver.graphUpdateVertex(Constants.TAXONOMICGRAPHNAME, NodeTypes.taxent.toString(), this._key, new TaxEntVertex(this), true);
		this.dirty=false;
	}
	
	/**
	 * Sets in the DB this taxent node as PART_OF another taxent node. Only adds a new relation if it doesn't exist. 
	 * @param parent
	 * @throws IOException
	 * @throws ArangoException
	 */
	public int setPartOf(TaxEnt parent) throws IOException, ArangoException {	// TODO: Optimize this with a single AQL query
		if(this._id==null) throw new IOException("Node "+this.name+" not attached to DB");

		// checks whether there is already a PART_OF relation between these two nodes
		Integer nrel=this.graph.driver.executeAqlQuery(
			"FOR v IN GRAPH_NEIGHBORS('"+Constants.TAXONOMICGRAPHNAME
			+"','"+this._id
			+"',{edgeCollectionRestriction:'"+AllRelTypes.PART_OF.toString()
			+"'}) FILTER v=='"+parent._id+"' COLLECT WITH COUNT INTO l RETURN l",null,null,Integer.class).getUniqueResult();
		
		if(nrel==0) {
			this.graph.driver.createEdge(AllRelTypes.PART_OF.toString(), new PART_OF(true), this._id, parent._id, false, false);
			return 1;
		} else return 0;
		//this.graph.driver.graphCreateEdge(Constants.GRAPHNAME, AllRelTypes.PART_OF.toString(), this.vertexEntity.getDocumentHandle(), parent.getID(), new PART_OF(true), false);
	}
	
	public int setObservedIn(SpeciesList slist,Short doubt,Short validated,PhenologicalStates state,String uuid,Integer weight,String pubnotes,NativeStatus nstate,String dateInserted) throws IOException, ArangoException {
		if(this._id==null) throw new IOException("Node "+this.name+" not attached to DB");
		OBSERVED_IN a=new OBSERVED_IN(doubt,validated,state,uuid,weight,pubnotes,nstate,dateInserted,this._id,slist._id);
		String query=String.format(
			"UPSERT {_from:'%1$s',_to:'%2$s'} INSERT %3$s UPDATE %3$s IN OBSERVED_IN RETURN OLD ? 0 : 1"
			,this._id
			,slist._id
			,a.toJSONString());
		//System.out.println(query);
		return this.graph.driver.executeAqlQuery(query,null,null,Integer.class).getUniqueResult();

		/*
		String query=String.format("FOR v IN GRAPH_EDGES ('%1$s',{_from:'%2$s',_to:'%3$s'},{}) COLLECT WITH COUNT INTO l RETURN l",Constants.TAXONOMICGRAPHNAME,this.getID(),slist.getID());
		Integer nrel=this.graph.driver.executeAqlQuery(query,null,null,Integer.class).getUniqueResult();
		if(nrel==0) {
			this.graph.driver.createEdge(AllRelTypes.OBSERVED_IN.toString(), new OBSERVED_IN(doubt,state), this.getID(), slist.getID(), false, false);
			return 1;
		} else return 0;*/
	}

	
	public Boolean isSpecies() {
		return this.getRankValue()==TaxonRanks.SPECIES.getValue();
	}
	
	public Boolean isSpeciesOrInferior() {
		return this.getRankValue()>=TaxonRanks.SPECIES.getValue();
	}
	
	public Boolean isHybrid() {
		// TODO hybrids!
		return null;
	}
	
	public List<TaxEnt> getParentNodes() {
		// TODO
		return new ArrayList<TaxEnt>();
	}

	@Override
	public String toCSVLine() {
		return this.getFullName();
	}

	@Override
	public String toHTMLLine() {
		return "<tr><td>"+this.getFullName()+"</td></tr>";
	}
}
