package pt.floraon.entities;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.arangodb.ArangoException;
import com.arangodb.CursorResult;
import com.arangodb.NonUniqueResultException;
import com.arangodb.entity.marker.VertexEntity;
import com.google.gson.internal.LinkedTreeMap;

import pt.floraon.driver.Constants;
import pt.floraon.driver.FloraOnDriver;
import pt.floraon.driver.FloraOnException;
import pt.floraon.driver.TaxonomyException;
import pt.floraon.driver.Constants.NativeStatus;
import pt.floraon.driver.Constants.NodeTypes;
import pt.floraon.driver.Constants.PhenologicalStates;
import pt.floraon.driver.Constants.RelTypes;
import pt.floraon.driver.Constants.TaxonRanks;

/**
 * A wrapper for the taxonomic nodes, providing a series of convenience functions.
 * @author Miguel Porto
 *
 */
public class TaxEnt extends GeneralNodeWrapper {
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
	
	public TaxEnt(FloraOnDriver graph,TaxEntVertex tev) {
		super.baseNode=tev;
		this.baseNode=(TaxEntVertex)super.baseNode;
		this.graph=graph;
	}
	
	public TaxEnt(LinkedTreeMap<String,Object> tev) throws TaxonomyException {
		super.baseNode=new TaxEntVertex(tev);
		this.baseNode=(TaxEntVertex)super.baseNode;
	}

	public TaxEnt(FloraOnDriver graph,VertexEntity<TaxEntVertex> ve) {
		super.baseNode=ve.getEntity();
		this.baseNode=(TaxEntVertex)super.baseNode;
		this.graph=graph;
		this.vertexEntity=ve;
		this.baseNode._id=this.vertexEntity.getDocumentHandle();
		this.baseNode._key=this.vertexEntity.getDocumentKey();
	}

	/**
	 * Create a new detached taxon (i.e. not saved in DB)
	 * @param name
	 * @param rank
	 * @param annotation
	 * @throws TaxonomyException 
	 */
	public TaxEnt(String name,String author,Integer rank,String annotation, Boolean current) throws TaxonomyException {
		super.baseNode=new TaxEntVertex(name,rank,author,annotation,current,null);
		this.baseNode=(TaxEntVertex)super.baseNode;
	}

	/**
	 * Creates a new taxon and adds it to DB.
	 * @param driver
	 * @param name
	 * @param author
	 * @param rank
	 * @param annotation
	 * @param current
	 * @return
	 * @throws TaxonomyException
	 * @throws ArangoException
	 */
	public static TaxEnt newFromName(FloraOnDriver driver,String name,String author,TaxonRanks rank,String annotation,Boolean current) throws TaxonomyException, ArangoException {
		TaxEnt out=new TaxEnt(name, author, rank == null ? null : rank.getValue(), annotation, current);
		out.graph=driver;
		out.vertexEntity=driver.driver.graphCreateVertex(Constants.TAXONOMICGRAPHNAME, NodeTypes.taxent.toString(), out.baseNode, false);
		out.baseNode._id=out.vertexEntity.getDocumentHandle();
		out.baseNode._key=out.vertexEntity.getDocumentKey();
		return out;
	}

	/**
	 * Creates a new taxon and adds it to DB.
	 * @param driver
	 * @param te
	 * @return
	 * @throws TaxonomyException
	 * @throws ArangoException
	 */
	public static TaxEnt newFromTaxEnt(FloraOnDriver driver,TaxEnt te) throws TaxonomyException, ArangoException {
		return TaxEnt.newFromName(driver, te.baseNode.getName(), te.baseNode.getAuthor(), te.baseNode.getRank(), te.baseNode.getAnnotation(), te.baseNode.current);
	}
	/**
	 * Create a new node and add it to DB.
	 * @param graph The graph instance
	 * @param tname The name
	 * @throws ArangoException
	 * @throws TaxonomyException 
	 */
	/*private TaxEnt(FloraOnDriver graph,TaxEntName tname,Boolean current) throws ArangoException, TaxonomyException {
		super.baseNode=new TaxEntVertex(tname.name,tname.rank == null ? null : tname.rank.getValue(),tname.author,null,current,null);
		this.baseNode=(TaxEntVertex)super.baseNode;
		this.graph=graph;
		this.vertexEntity=graph.driver.graphCreateVertex(Constants.TAXONOMICGRAPHNAME, NodeTypes.taxent.toString(), this.baseNode, false);
		this.baseNode._id=this.vertexEntity.getDocumentHandle();
		this.baseNode._key=this.vertexEntity.getDocumentKey();
	}*/

	/**
	 * Create a new node and add it to DB.
	 * @param graph The graph instance
	 * @param name
	 * @param author
	 * @param rank
	 * @param annotation
	 * @param current
	 * @throws ArangoException
	 * @throws TaxonomyException 
	 */
/*	private TaxEnt(FloraOnDriver graph,String name,String author,TaxonRanks rank,String annotation,Boolean current) throws ArangoException, TaxonomyException {
		super.baseNode=new TaxEntVertex(name,rank == null ? null : rank.getValue(),author,annotation,current,null);
		this.baseNode=(TaxEntVertex)super.baseNode;
		this.graph=graph;
		this.vertexEntity=graph.driver.graphCreateVertex(Constants.TAXONOMICGRAPHNAME, NodeTypes.taxent.toString(), this.baseNode, false);
		this.baseNode._id=this.vertexEntity.getDocumentHandle();
		this.baseNode._key=this.vertexEntity.getDocumentKey();
	}*/
		
	/*public static TaxEnt newFromName(FloraOnDriver driver,TaxEntName tname,Boolean current) throws TaxonomyException, ArangoException {
		return new TaxEnt(driver, tname, current);
	}*/
	
	public static TaxEnt parse(String name) throws TaxonomyException {
		name=name.replaceAll(" +", " ").trim();
		if(name.equals("")) {
			throw new TaxonomyException("Taxon must have a name");
		}
		// extract the authority between braces (I don't use regex cause it's too simple)
		int a=name.indexOf('{');
		int b=name.indexOf('}');
		String author=null,name1;
		if(a>-1 && b>-1) {
			if(b>a+1) {
				author=name.substring(a+1, b-0).trim();
				name1=name.substring(0,a).trim();
			} else {
				name1=name.substring(0,a).trim();
			}
		} else name1=name;
		return new TaxEnt(name1, author, null, null, null);
	}

	/**
	 * Creates a TaxEnt from an existing node in the DB, by its handle.
	 * @param graph
	 * @param handle
	 * @return
	 * @throws ArangoException
	 */
	public static TaxEnt newFromHandle(FloraOnDriver graph,String handle) throws ArangoException {
		return new TaxEnt(graph,graph.driver.getDocument(handle, TaxEntVertex.class).getEntity());
	}
	
	/**
	 * Gets the corresponding VertexEntity in the DB
	 * @return
	 */
	public VertexEntity<TaxEntVertex> getVertexEntity() {
		return this.vertexEntity;
	}

	public void setName(String name) throws TaxonomyException {
		if(Objects.equals(name, baseNode.name)) return;
		if(name==null || name.trim().length()==0) throw new TaxonomyException("Taxon must have a name");
		baseNode.name = name;
		this.dirty=true;
	}
	
	public void setRank(Integer rank) {
		if(Objects.equals(rank, baseNode.rank)) return;
		baseNode.rank = rank;
		this.dirty=true;
	}

	public void setAnnotation(String annotation) {
		if(Objects.equals(annotation, baseNode.annotation)) return;
		if(annotation!=null && annotation.trim().length()==0) annotation=null;
		baseNode.annotation = annotation;
		this.dirty=true;
	}

	public void setAuthor(String author) {
		if(Objects.equals(author, baseNode.author)) return;
		if(author!=null && author.trim().length()==0) author=null;
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
	 * @throws FloraOnException 
	 */
	@Override
	public void commit() throws ArangoException, FloraOnException {
		if(!this.dirty) return;
		if(baseNode._id==null) throw new FloraOnException("Node "+baseNode.name+" not attached to DB");
		this.graph.driver.graphUpdateVertex(Constants.TAXONOMICGRAPHNAME, NodeTypes.taxent.toString(), baseNode._key, new TaxEntVertex(this), false);
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
		return this.baseNode.getRankValue()==TaxonRanks.SPECIES.getValue();
	}
	
	public Boolean isSpeciesOrInferior() {
		return this.baseNode.getRankValue()>=TaxonRanks.SPECIES.getValue();
	}
	
	public Boolean isHybrid() {
		// TODO hybrids!
		return null;
	}
	
	public boolean isLeafNode() throws FloraOnException, ArangoException {
		if(this.graph==null) throw new FloraOnException("Node "+baseNode.name+" not attached to DB");
		String query="RETURN LENGTH(FOR e IN PART_OF FILTER e._to=='"+baseNode._id+"' RETURN e)";
		return this.graph.driver.executeAqlQuery(query,null,null,Integer.class).getUniqueResult()==0;
	}
	
	/**
	 * Gets the chain of synonyms associated with this taxon (excluding self). Note that only true SYNONYMs are returned (no PART_OF).
	 * @return
	 * @throws ArangoException
	 * @throws FloraOnException 
	 * @throws IOException 
	 */
	public CursorResult<TaxEntVertex> getSynonyms() throws ArangoException, FloraOnException {
		if(this.graph==null) throw new FloraOnException("Node "+baseNode.name+" not attached to DB");
		String query=String.format("FOR v IN TRAVERSAL(%1$s, %2$s, '%3$s', 'any',{paths:false}) FILTER v.vertex._id!='%3$s' RETURN v.vertex"
			,NodeTypes.taxent.toString(),RelTypes.SYNONYM.toString(),this.baseNode._id
		);
		return this.graph.driver.executeAqlQuery(query,null,null,TaxEntVertex.class);
	}
	
	/**
	 * Sets this taxon as a synonym of given taxon. Automatically sets this taxon to not current.
	 * @param tev
	 * @throws ArangoException 
	 * @throws IOException 
	 * @throws FloraOnException 
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws SecurityException 
	 * @throws NoSuchMethodException 
	 */
	public void setSynonymOf(TaxEntVertex tev) throws ArangoException, IOException, FloraOnException {
		if(this.getID().equals(tev.getID())) throw new TaxonomyException("Cannot add a synonym of itself");
		if(tev.current) this.setCurrent(false);
		this.commit();
		try {
			new GeneralNodeWrapperImpl(this.graph, this.baseNode).createRelationshipTo(tev, RelTypes.SYNONYM);
		} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Tests whether this taxon can be inserted as a child of given taxon, following the rules of nomenclature.
	 * @param taxon
	 * @return
	 * @throws TaxonomyException 
	 */
	public void canBeChildOf(TaxEntVertex taxon) throws TaxonomyException {
		if(baseNode.rank <= taxon.getRankValue()) throw new TaxonomyException("Rank must be lower than parent rank");
		if(this.isSpeciesOrInferior()) {
			if(baseNode.getName().toLowerCase().indexOf(taxon.getName().toLowerCase()+" ") != 0) throw new TaxonomyException("Name must include all superior taxa up to genus");
			// TODO: more tests for name validity
		}
	}

	/**
	 * Gets the current taxonomic parent of this taxon.
	 * @return
	 * @throws ArangoException
	 * @throws TaxonomyException if there is more than one current parent (this is a taxonomic violation)
	 */
	public TaxEntVertex getParentTaxon() throws ArangoException, TaxonomyException {
		String query=String.format("FOR n IN NEIGHBORS(%1$s,%2$s,'%3$s','outbound',{current:true},{includeData:true}) RETURN n"
			,NodeTypes.taxent.toString(),RelTypes.PART_OF.toString(),this.baseNode._id);
		TaxEntVertex out;
		try {
			out=this.graph.driver.executeAqlQuery(query,null,null,TaxEntVertex.class).getUniqueResult();
		} catch (NonUniqueResultException e) {
			throw new TaxonomyException("The taxon "+this.baseNode.getFullName()+" has more than one current parent taxon. This must be fixed.");
		}
		return out; 
	}
	/**
	 * Gets the parents of an hybrid
	 * @return
	 */
	public List<TaxEnt> getHybridAncestry() {
		// TODO get parent nodes
		return new ArrayList<TaxEnt>();
	}
}
