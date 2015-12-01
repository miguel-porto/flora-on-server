package pt.floraon.entities;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.commons.csv.CSVPrinter;

import com.arangodb.ArangoException;
import com.arangodb.entity.marker.VertexEntity;
import com.google.gson.internal.LinkedTreeMap;

import pt.floraon.driver.FloraOnGraph;
import pt.floraon.results.ResultItem;
import pt.floraon.server.Constants;
import pt.floraon.server.FloraOnException;
import pt.floraon.server.Constants.NativeStatus;
import pt.floraon.server.Constants.NodeTypes;
import pt.floraon.server.Constants.PhenologicalStates;
import pt.floraon.server.Constants.TaxonRanks;

/**
 * A wrapper for the taxonomic nodes, providing a series of convenience functions.
 * @author Miguel Porto
 *
 */
public class TaxEnt extends GeneralNodeWrapper implements ResultItem {
	public TaxEntVertex baseNode;
	private VertexEntity<TaxEntVertex> vertexEntity=null;
	{
		this.dirty=false;
		this.vertexEntity=null;
	}

	public TaxEnt(TaxEntVertex tev) {
		super.baseNode=tev;
		this.baseNode=(TaxEntVertex)super.baseNode;
	}
	
	public TaxEnt(FloraOnGraph graph,TaxEntVertex tev) {
		super.baseNode=tev;
		this.baseNode=(TaxEntVertex)super.baseNode;
		this.graph=graph;
	}
	
	public TaxEnt(LinkedTreeMap<String,Object> tev) {
		super.baseNode=new TaxEntVertex(tev);
		this.baseNode=(TaxEntVertex)super.baseNode;
	}

	public TaxEnt(FloraOnGraph graph,VertexEntity<TaxEntVertex> ve) {
		super.baseNode=ve.getEntity();
		this.baseNode=(TaxEntVertex)super.baseNode;
		this.graph=graph;
		this.vertexEntity=ve;
		this.baseNode._id=this.vertexEntity.getDocumentHandle();
		this.baseNode._key=this.vertexEntity.getDocumentKey();
	}

	/**
	 * Create a new detached node (i.e. not saved in DB)
	 * @param name
	 * @param rank
	 * @param annotation
	 */
	public TaxEnt(String name,Integer rank,String author,String annotation) {
		super.baseNode=new TaxEntVertex(name,rank,author,annotation,null,null);
		this.baseNode=(TaxEntVertex)super.baseNode;
	}

	/**
	 * Create a new node and add it to DB.
	 * @param graph The graph instance
	 * @param tname The name
	 * @throws ArangoException
	 */
	public TaxEnt(FloraOnGraph graph,TaxEntName tname,Boolean current) throws ArangoException {
		super.baseNode=new TaxEntVertex(tname.name,tname.rank == null ? null : tname.rank.getValue(),tname.author,null,current,null);
		this.baseNode=(TaxEntVertex)super.baseNode;
		this.graph=graph;
		this.vertexEntity=graph.driver.graphCreateVertex(Constants.TAXONOMICGRAPHNAME, NodeTypes.taxent.toString(), this.baseNode, false);
		this.baseNode._id=this.vertexEntity.getDocumentHandle();
		this.baseNode._key=this.vertexEntity.getDocumentKey();
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
		super.baseNode=new TaxEntVertex(name,rank == null ? null : rank.getValue(),author,annotation,current,null);
		this.baseNode=(TaxEntVertex)super.baseNode;
		this.graph=graph;
		this.vertexEntity=graph.driver.graphCreateVertex(Constants.TAXONOMICGRAPHNAME, NodeTypes.taxent.toString(), this.baseNode, false);
		this.baseNode._id=this.vertexEntity.getDocumentHandle();
		this.baseNode._key=this.vertexEntity.getDocumentKey();
	}
	
	/**
	 * Creates a TaxEnt from an existing node, by its handle.
	 * @param graph
	 * @param handle
	 * @return
	 * @throws ArangoException
	 */
	public static TaxEnt fromHandle(FloraOnGraph graph,String handle) throws ArangoException {
		return new TaxEnt(graph,graph.driver.getDocument(handle, TaxEntVertex.class).getEntity());
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
		return baseNode.name;
	}

	public void setName(String name) {
		if(Objects.equals(name, baseNode.name)) return;
		baseNode.name = name;
		this.dirty=true;
	}
	
	/**
	 * Gets the taxon name with authorship and annotations.
	 * @return
	 */
	public String getFullName() {
		return this.getName()+(baseNode.author!=null ? " "+this.getAuthor() : "")+(baseNode.annotation!=null ? " ["+this.getAnnotation()+"]" : "");
	}

	public TaxonRanks getRank() {
		return TaxonRanks.getRankFromValue(baseNode.rank);
	}
	
	public Integer getRankValue() {
		return baseNode.rank;
	}

	public void setRank(Integer rank) {
		if(Objects.equals(rank, baseNode.rank)) return;
		baseNode.rank = rank;
		this.dirty=true;
	}

	public String getAnnotation() {
		return baseNode.annotation;
	}

	public void setAnnotation(String annotation) {
		if(Objects.equals(annotation, baseNode.annotation)) return;
		baseNode.annotation = annotation;
		this.dirty=true;
	}

	public String getAuthor() {
		return baseNode.author;
	}

	public void setAuthor(String author) {
		if(Objects.equals(author, baseNode.author)) return;
		baseNode.author = author;
		this.dirty=true;
	}

	public Boolean getCurrent() {
		return baseNode.current;
	}

	public void setCurrent(Boolean current) {
		if(Objects.equals(current, baseNode.current)) return;
		baseNode.current = current;
		this.dirty=true;
	}

	public Integer getOldId() {
		return baseNode.oldId;
	}

	public void setOldId(Integer oldId) {
		if(Objects.equals(oldId, baseNode.oldId)) return;
		baseNode.oldId = oldId;
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
		if(baseNode._id==null) throw new IOException("Node "+baseNode.name+" not attached to DB");
// TODO didn't test whether it gets updated!
		this.graph.driver.graphUpdateVertex(Constants.TAXONOMICGRAPHNAME, NodeTypes.taxent.toString(), baseNode._key, new TaxEntVertex(this), true);
		this.dirty=false;
	}
		
	public int setObservedIn(SpeciesList slist,Short doubt,Short validated,PhenologicalStates state,String uuid,Integer weight,String pubnotes,NativeStatus nstate,String dateInserted) throws FloraOnException, ArangoException {
		if(baseNode._id==null) throw new FloraOnException("Node "+baseNode.name+" not attached to DB");
		OBSERVED_IN a=new OBSERVED_IN(doubt,validated,state,uuid,weight,pubnotes,nstate,dateInserted,baseNode._id,slist.baseNode._id);
		String query=String.format(
			"UPSERT {_from:'%1$s',_to:'%2$s'} INSERT %3$s UPDATE %3$s IN OBSERVED_IN RETURN OLD ? 0 : 1"
			,baseNode._id
			,slist.baseNode._id
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
	public void toCSVLine(CSVPrinter rec) throws IOException {
		rec.print(this.getFullName());
	}

	@Override
	public String toHTMLLine() {
		return "<tr><td>"+this.getFullName()+"</td></tr>";
	}

	@Override
	public String[] toStringArray() {
		// TODO Auto-generated method stub
		return null;
	}
}
