package pt.floraon.dbworker;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import com.arangodb.ArangoConfigure;
import com.arangodb.ArangoDriver;
import com.arangodb.ArangoException;
import com.arangodb.CursorResult;
import com.arangodb.NonUniqueResultException;
import com.arangodb.VertexCursor;
import com.arangodb.entity.CollectionOptions;
import com.arangodb.entity.CollectionType;
import com.arangodb.entity.EdgeDefinitionEntity;
import com.arangodb.entity.EntityFactory;
import com.arangodb.entity.StringsResultEntity;
import com.arangodb.entity.UserEntity;
import com.arangodb.entity.marker.VertexEntity;
import com.arangodb.util.GraphVerticesOptions;
import com.google.gson.internal.LinkedTreeMap;

import pt.floraon.entities.Attribute;
import pt.floraon.entities.AttributeVertex;
import pt.floraon.entities.Author;
import pt.floraon.entities.AuthorVertex;
import pt.floraon.entities.Character;
import pt.floraon.entities.GeneralDBNode;
import pt.floraon.entities.GeneralDBNodeImpl;
import pt.floraon.entities.GeneralNodeWrapperImpl;
import pt.floraon.entities.SpeciesList;
import pt.floraon.entities.SpeciesListVertex;
import pt.floraon.entities.TaxEnt;
import pt.floraon.entities.TaxEntVertex;
import pt.floraon.queryparser.Match;
import pt.floraon.results.ChecklistEntry;
import pt.floraon.results.GraphUpdateResult;
import pt.floraon.results.Occurrence;
import pt.floraon.results.SimpleTaxonResult;
import pt.floraon.server.Constants;
import pt.floraon.server.Constants.TaxonRanks;

import static pt.floraon.server.Constants.*;

public class FloraOnGraph {
	public ArangoDriver driver;
	public final GeneralQueries dbGeneralQueries;
	public final NodeWorker dbNodeWorker;
	public final DataUploader dbDataUploader;
	public final SpecificQueries dbSpecificQueries;
	
	public FloraOnGraph(String dbname) throws ArangoException {
        ArangoConfigure configure = new ArangoConfigure();
        configure.init();
        configure.setDefaultDatabase("flora");
        this.dbGeneralQueries=new FloraOnGraph.GeneralQueries();
        this.dbNodeWorker=new FloraOnGraph.NodeWorker();
        this.dbDataUploader=new FloraOnGraph.DataUploader();
        this.dbSpecificQueries=new FloraOnGraph.SpecificQueries();
        
        driver = new ArangoDriver(configure);
/*
        driver.createAqlFunction("flora::testCode", "function (config, vertex, path) {"
    		+ "if(!vertex.name) return ['exclude','prune'];"
    		+ "}");
        */
        try {
			StringsResultEntity dbs=driver.getDatabases();
			if(!dbs.getResult().contains(dbname))
				initializeNewGraph(dbname);
			else
				driver.setDefaultDatabase(dbname);        
		} catch (ArangoException e) {
			System.err.println("ERROR initializing the graph: "+e.getMessage());
			e.printStackTrace();
			throw new ArangoException(e.getMessage());
		}
        //driver.createFulltextIndex("taxent", "name");
	}
	
	/**
	 * Initializes a new database from scratch. Creates collections, graphs, etc.
	 * @param dbname
	 * @throws ArangoException
	 */
	private void initializeNewGraph(String dbname) throws ArangoException {
		System.out.println("Initializing a fresh new database");
		/*				UserEntity ue;
		ue=new UserEntity();*/
		UserEntity[] ue=new UserEntity[0];
		driver.createDatabase(dbname, ue);
		driver.setDefaultDatabase(dbname);
		
		// create a collection for each nodetype
		for(NodeTypes nt:NodeTypes.values()) {
			driver.createCollection(nt.toString());
		}
		
		CollectionOptions co=new CollectionOptions();
		co.setType(CollectionType.EDGE);
		for(AllRelTypes nt:AllRelTypes.values()) {
			driver.createCollection(nt.toString(),co);
		}

		List<EdgeDefinitionEntity> edgeDefinitions = new ArrayList<EdgeDefinitionEntity>();

		// taxonomic relations
		EdgeDefinitionEntity edgeDefinition = new EdgeDefinitionEntity();
		// define the edgeCollection to store the edges
		edgeDefinition.setCollection(AllRelTypes.PART_OF.toString());
		// define a set of collections where an edge is going out...
		List<String> from = new ArrayList<String>();
		// and add one or more collections
		from.add(NodeTypes.taxent.toString());
		edgeDefinition.setFrom(from);
		 // repeat this for the collections where an edge is going into  
		List<String> to = new ArrayList<String>();
		to.add(NodeTypes.taxent.toString());
		edgeDefinition.setTo(to);
		edgeDefinitions.add(edgeDefinition);

		edgeDefinition = new EdgeDefinitionEntity();
		edgeDefinition.setCollection(AllRelTypes.HYBRID_OF.toString());
		from = new ArrayList<String>();
		from.add(NodeTypes.taxent.toString());
		edgeDefinition.setFrom(from);
		to = new ArrayList<String>();
		to.add(NodeTypes.taxent.toString());
		edgeDefinition.setTo(to);
		edgeDefinitions.add(edgeDefinition);

		edgeDefinition = new EdgeDefinitionEntity();
		edgeDefinition.setCollection(AllRelTypes.SYNONYM.toString());
		from = new ArrayList<String>();
		from.add(NodeTypes.taxent.toString());
		edgeDefinition.setFrom(from);
		to = new ArrayList<String>();
		to.add(NodeTypes.taxent.toString());
		edgeDefinition.setTo(to);
		edgeDefinitions.add(edgeDefinition);

		// species list subgraph
		edgeDefinition = new EdgeDefinitionEntity();
		edgeDefinition.setCollection(AllRelTypes.OBSERVED_IN.toString());
		from = new ArrayList<String>();
		from.add(NodeTypes.taxent.toString());
		edgeDefinition.setFrom(from);
		to = new ArrayList<String>();
		to.add(NodeTypes.specieslist.toString());
		edgeDefinition.setTo(to);
		edgeDefinitions.add(edgeDefinition);

		edgeDefinition = new EdgeDefinitionEntity();
		edgeDefinition.setCollection(AllRelTypes.OBSERVED_BY.toString());
		from = new ArrayList<String>();
		from.add(NodeTypes.specieslist.toString());
		edgeDefinition.setFrom(from);
		to = new ArrayList<String>();
		to.add(NodeTypes.author.toString());
		edgeDefinition.setTo(to);
		edgeDefinitions.add(edgeDefinition);

		// attributes <- taxent
		edgeDefinition = new EdgeDefinitionEntity();
		edgeDefinition.setCollection(AllRelTypes.HAS_QUALITY.toString());
		from = new ArrayList<String>();
		from.add(NodeTypes.taxent.toString());
		edgeDefinition.setFrom(from);
		to = new ArrayList<String>();
		to.add(NodeTypes.attribute.toString());
		edgeDefinition.setTo(to);
		edgeDefinitions.add(edgeDefinition);

		// characters <- attributes
		edgeDefinition = new EdgeDefinitionEntity();
		edgeDefinition.setCollection(AllRelTypes.ATTRIBUTE_OF.toString());
		from = new ArrayList<String>();
		from.add(NodeTypes.attribute.toString());
		edgeDefinition.setFrom(from);
		to = new ArrayList<String>();
		to.add(NodeTypes.character.toString());
		edgeDefinition.setTo(to);
		edgeDefinitions.add(edgeDefinition);

		driver.createGraph(Constants.TAXONOMICGRAPHNAME, edgeDefinitions, null, true);
		driver.createGeoIndex(NodeTypes.specieslist.toString(), false, "location");
		driver.createHashIndex("author", true, "idAut");
		driver.createHashIndex("taxent", true, true, "oldId");
		driver.createHashIndex("taxent", false, true, "rank");
	}
	
	/**
	 * Gets the complete list of taxa in the DB
	 * @return
	 */
	public List<ChecklistEntry> getCheckList() {
		List<ChecklistEntry> chklst=new ArrayList<ChecklistEntry>();
        @SuppressWarnings("rawtypes")
		CursorResult<List> vertexCursor;
        @SuppressWarnings("rawtypes")
        Iterator<List> vertexIterator;
    	GraphVerticesOptions gvo=new GraphVerticesOptions();
    	List<String> vcr=new ArrayList<String>();
    	vcr.add("taxent");
    	gvo.setVertexCollectionRestriction(vcr);
    	String query=String.format(
			"FOR v IN GRAPH_TRAVERSAL('%1$s',"	//		LENGTH(EDGES(%2$s,v._id,'inbound'))
			+ "FOR v IN taxent FILTER v.isSpeciesOrInf==true && LENGTH(FOR e IN PART_OF FILTER e._to==v._id RETURN e)==0 RETURN v"	// leaf nodes
			+ ",'outbound',{paths:false,filterVertices:[%3$s],vertexFilterMethod:['exclude']}) COLLECT a=v[*].vertex RETURN a"
   			, Constants.TAXONOMICGRAPHNAME,AllRelTypes.PART_OF.toString(),Constants.CHECKLISTFIELDS);

    	try {
    		// traverse all leaf nodes outwards
    		vertexCursor=this.driver.executeAqlQuery(query, null, null, List.class);
			vertexIterator = vertexCursor.iterator();
			
			ChecklistEntry chk;
			while (vertexIterator.hasNext()) {
				@SuppressWarnings("unchecked")
				List<LinkedTreeMap<String,Object>> entry1 = vertexIterator.next();
				chk=new ChecklistEntry();
				for(LinkedTreeMap<String,Object> tev:entry1) {
					TaxEnt te=new TaxEnt(tev);
					if(te.isSpeciesOrInferior()) {
						if(chk.canonicalName==null) {
							chk.taxon=te.getFullName();
							chk.canonicalName=te.getName();
						}
					}
					switch(te.getRank()) {
					case GENUS:
						chk.genus=te.getName();
						break;
					case FAMILY:
						chk.family=te.getName();
						break;
					case ORDER:
						chk.order=te.getName();
						break;
					default:
						break;
					}
				}
				chklst.add(chk);
			}
		} catch (ArangoException e) {
			e.printStackTrace();
		}
    	return chklst;
	}
	
	//
	/**
	 * Execute a text query that filters nodes by their name, and returns all species (or inferior rank) downstream the filtered nodes.
	 * @param q	Query text
	 * @return An Iterator of {@link TaxEntVertex}
	 * @throws ArangoException 
	 */
	/*
    public Iterator<TaxEnt> speciesTextQuery(String q) throws ArangoException {
    	// TODO put vertex collection restrictions in the options
    	q=q.toLowerCase();
    	String query=String.format("FOR v IN UNIQUE(FLATTEN(FOR v IN GRAPH_TRAVERSAL('%1$s',FOR v IN GRAPH_VERTICES('%1$s',{},{vertexCollectionRestriction:%3$s}) "
    			+ "FILTER LIKE(v.name,'%%%2$s%%',true) RETURN v,'inbound',{paths:false,filterVertices:[{isSpeciesOrInf:true}],vertexFilterMethod:['exclude']}) "
    			+ "RETURN v[*].vertex)) FILTER LENGTH(GRAPH_EDGES('taxgraph',v,{direction:'inbound'}))==0 RETURN v", Constants.TAXONOMICGRAPHNAME,q,Constants.QUERYCOLLECTIONRESTRICTIONS_PARTIALMATCH);
    	CursorResult<TaxEnt> vertexCursor=this.driver.executeAqlQuery(query, null, null, TaxEnt.class);
    	//System.out.println(query);
    	return vertexCursor.iterator();
    }*/

	/**
	 * A class that groups methods to add, update, remove or fetch nodes. 
	 * @author miguel
	 *
	 */
	public final class NodeWorker {
		/**
		 * Fetches one author with the given idAut
		 * @param idaut
		 * @return Null if not found, otherwise an {@link Author}
		 */
		public Author getAuthorById(int idaut) {
			String query="RETURN GRAPH_VERTICES('taxgraph',{idAut:"+idaut+"})[0]";
			AuthorVertex vertexCursor=null;
			try {
				vertexCursor = driver.executeAqlQuery(query, null, null, AuthorVertex.class).getUniqueResult();
			} catch (ArangoException e) {
				System.err.println("More than one author with this ID?!");
				return null;
			}
			if(vertexCursor==null)
				return null;
			else
				return new Author(FloraOnGraph.this,vertexCursor);
		}

		/**
		 * Fetches one {@link TaxEnt} with the given idEnt
		 * @param oldId Legacy ID (when importing from other DB)
		 * @return
		 */
		public TaxEnt getTaxEntById(int oldId) {
			//String query="RETURN GRAPH_VERTICES('taxgraph',{oldId:"+oldId+"},{vertexCollectionRestriction:'taxent'})[0]";
			String query="FOR v IN taxent FILTER v.oldId=="+oldId+" RETURN v";
			TaxEntVertex vertexCursor=null;
			try {
				vertexCursor = driver.executeAqlQuery(query, null, null, TaxEntVertex.class).getUniqueResult();
			} catch (ArangoException e) {
				System.err.println("More than one taxon with this ID?!");
				return null;
			}
			if(vertexCursor==null)
				return null;
			else
				return new TaxEnt(FloraOnGraph.this,vertexCursor);
		}

		/**
		 * Fetches an iterator of {@link TaxEnt}s with the given idEnts
		 * @param oldIds An array of idEnts
		 * @return
		 */
		public Iterator<TaxEntVertex> getTaxEntsByIds(int[] oldIds) {
			String query="FOR v IN taxent FILTER v.oldId IN "+EntityFactory.toJsonString(oldIds)+" RETURN v";
			Iterator<TaxEntVertex> vertexCursor=null;
			try {
				vertexCursor = driver.executeAqlQuery(query, null, null, TaxEntVertex.class).iterator();
			} catch (ArangoException e) {
				System.err.println("More than one taxon with this ID?!");
				return null;
			}
			return vertexCursor;
		}

	    /**
	     * Create a new taxonomic node and immediately add it to DB.
	     * @param name
	     * @param author
	     * @param rank
	     * @param annotation
	     * @param current
	     * @return The document handle of the new node.
	     * @throws ArangoException
	     */
	    public GraphUpdateResult createTaxEntNode(String name,String author,TaxonRanks rank,String annotation,Boolean current) throws ArangoException {
	    	return GraphUpdateResult.fromHandle(FloraOnGraph.this, new TaxEnt(FloraOnGraph.this,name,author,rank,annotation,current).getID());
	    }

	    /**
	     * Gets only one taxon node, or none, based only on taxon name. The name must not be ambiguous.
	     * @param q
	     * @return
	     * @throws QueryException
	     * @throws ArangoException
	     * @throws TaxonomyException
	     */
	    public TaxEnt findTaxEnt(String q) throws QueryException, ArangoException, TaxonomyException {
	    	return findTaxEnt(new TaxEntName(q));
	    }
	    
	    /**
		 * Gets only one node, or none, based on name and rank.
		 * NOTE: if rank is "norank", it is ignored.
	     * @param q
	     * @return
	     * @throws QueryException
	     * @throws ArangoException 
	     */
		public TaxEnt findTaxEnt(TaxEntName q) throws QueryException, ArangoException {
	// NOTE: this AQL query is slower than the code below!	
			/*String query=String.format("FOR v IN %1$s FILTER LOWER(v.name)=='%2$s' RETURN v",NodeTypes.taxent.toString(),q.name.trim().toLowerCase());
			TaxEntVertex tev=this.driver.executeAqlQuery(query, null, null, TaxEntVertex.class).getUniqueResult();
			if(tev==null)
				return null;
			else
				return new TaxEnt(FloraOnGraph.this,tev);*/
			
	    	if(q.name==null || q.name.equals("")) throw new QueryException("Invalid blank name.");
	    	TaxEnt n;
	    	try {
	    		VertexCursor<TaxEntVertex> vc=driver.graphGetVertexCursor(Constants.TAXONOMICGRAPHNAME, TaxEntVertex.class, new TaxEntVertex(q.name,null,null,null,null,null), null, null);
	    		VertexEntity<TaxEntVertex> ve1=vc.getUniqueResult();

	    		if(ve1==null)	// node doesn't exist
	    			return null;
	    		else
	    			n=new TaxEnt(FloraOnGraph.this,ve1);

	    		if(q.rank==null || q.rank.getValue().equals(TaxonRanks.NORANK.getValue()) || n.getRankValue()==null) return n; else {
					if(!n.getRankValue().equals(q.rank.getValue())) return null; else return n;
				}	    		
	    	} catch (NonUniqueResultException e) {	// multiple nodes with this name. Search the one of the right rank
	    		VertexCursor<TaxEntVertex> vc=driver.graphGetVertexCursor(Constants.TAXONOMICGRAPHNAME, TaxEntVertex.class, new TaxEntVertex(q.name,null,null,null), null, null);
				if(q.rank==null || q.rank.getValue().equals(TaxonRanks.NORANK.getValue())) throw new QueryException("More than one node with name "+q.name+". You must disambiguate.");

				Iterator<VertexEntity<TaxEntVertex>> ns=vc.iterator();
				n=null;
				TaxEnt n1;
				while(ns.hasNext()) {
					//n1=ns.next().getEntity();
					n1=new TaxEnt(FloraOnGraph.this,ns.next());
					if(n1.getRankValue().equals(q.rank.getValue()) || n1.getRankValue().equals(TaxonRanks.NORANK.getValue())) {
						if(n!=null) throw new QueryException("More than one node with name "+q.name+" and rank "+q.rank); else n=n1;
					}
				}
				return n;
	    	}
		}

		public String[] deleteTaxEntNode(TaxEntName nodename) throws QueryException, ArangoException {
	    	TaxEnt te=findTaxEnt(nodename);
	    	if(te!=null) return deleteNode(te.getID());
	    	return new String[0];
	    }
		
		/**
		 * Deletes one node and all connected edges
		 * @param id The document handle
		 * @return An array of the deleted document handles
		 * @throws ArangoException
		 */
		public String[] deleteNode(String id) throws ArangoException {
			List<String> deleted=new ArrayList<String>();
			String tmp;
			String query=String.format("FOR e IN GRAPH_EDGES('%1$s','%2$s') RETURN e"
				,Constants.TAXONOMICGRAPHNAME,id);
			System.out.println(query);
			Iterator<String> vertexCursor=driver.executeAqlQuery(query, null, null, String.class).iterator();
			while(vertexCursor.hasNext()) {
				tmp=vertexCursor.next();
				driver.deleteDocument(tmp);
				deleted.add(tmp);
			}
			driver.deleteDocument(id);
			deleted.add(id);
			return deleted.toArray(new String[0]);
		}
	    
	    public Attribute findAttribute(String name) throws ArangoException {
	    	String query="FOR v IN attribute FILTER v.name=='"+name+"' RETURN v";
			AttributeVertex vertexCursor=driver.executeAqlQuery(query, null, null, AttributeVertex.class).getUniqueResult();
			if(vertexCursor==null)
				return null;
			else
				return new Attribute(FloraOnGraph.this,vertexCursor);
	    }

		/**
		 * Gets the direct neighbors of the given vertex, off all facets.
		 * @param id The vertex's document handle
		 * @return A JSON string with an array of vertices ('nodes') and an array of edges ('links') of the form {nodes[],links:[]}
		 * @throws ArangoException
		 */
		public GraphUpdateResult getNeighbors(String id, Facets[] facets) {
			if(id==null) return GraphUpdateResult.emptyResult();
			AllRelTypes[] art=AllRelTypes.getRelTypesOfFacets(facets);
			String query=String.format("RETURN {nodes:(FOR n IN APPEND(['%2$s'],GRAPH_NEIGHBORS('%1$s','%2$s',{edgeCollectionRestriction:%3$s})) "
				+ "LET v=DOCUMENT(n) RETURN MERGE(v,{type:PARSE_IDENTIFIER(v._id).collection}))"//{id:v._id,r:v.rank,t:PARSE_IDENTIFIER(v._id).collection,n:v.name,c:v.current})"
				+ ",links:(FOR n IN GRAPH_EDGES('%1$s','%2$s',{edgeCollectionRestriction:%3$s}) "
				+ "LET d=DOCUMENT(n) RETURN MERGE(d,{type:PARSE_IDENTIFIER(d).collection}))}"	//source:d._from,target:d._to,
				,Constants.TAXONOMICGRAPHNAME,id,EntityFactory.toJsonString(art)
			);
			
	/*		String query=String.format("FOR p IN GRAPH_TRAVERSAL('%1$s','%2$s','any',{paths:true,maxDepth:1}) "
				+ "RETURN {nodes:(FOR v IN p[*].vertex RETURN {id:v._id,r:v.rank,t:PARSE_IDENTIFIER(v._id).collection,n:v.name,c:v.current})"
				+ ",links:(FOR e IN FLATTEN(FOR ed IN p[*].path.edges RETURN ed) COLLECT a=e._id LET d=DOCUMENT(a) LET ty=PARSE_IDENTIFIER(d).collection "
				+ "FILTER ty=='PART_OF'"
				+ "RETURN {id:d._id,source:d._from,target:d._to,current:d.current,type:ty})}",Constants.TAXONOMICGRAPHNAME,id);*/
			//System.out.println(query);//System.out.println(res);
			String res;
			try {
				res = driver.executeAqlQueryJSON(query, null, null);
			} catch (ArangoException e) {
				System.err.println(e.getErrorMessage());
				return GraphUpdateResult.emptyResult();
			}
			// NOTE: server responses are always an array, but here we always have one element, so we remove the []
			return (res==null || res.equals("[]")) ? GraphUpdateResult.emptyResult() : GraphUpdateResult.fromJson(res.substring(1, res.length()-1));
		}

		public GeneralDBNode getNode(String id) throws ArangoException {
			return driver.getDocument(id, GeneralDBNode.class).getEntity();
		}

		public GeneralNodeWrapperImpl getNodeWrapper(String id) {
			try {
				return new GeneralNodeWrapperImpl(FloraOnGraph.this,driver.getDocument(id, GeneralDBNodeImpl.class).getEntity());
			} catch (ArangoException e) {
				System.err.println(e.getErrorMessage());
				return null;
			}
		}

		/**
		 * Gets the links between given nodes (in the ID array), of the given facets. Does not expand any node.
		 * @param id An array of document handles
		 * @param facets The link facets to load
		 * @return
		 * @throws ArangoException
		 */
		public GraphUpdateResult getRelationshipsBetween(String[] id, Facets[] facets) throws ArangoException {
			AllRelTypes[] art=AllRelTypes.getRelTypesOfFacets(facets);
			String query=String.format("RETURN {nodes:(FOR n IN %2$s "
				+ "LET v=DOCUMENT(n) RETURN MERGE(v,{type:PARSE_IDENTIFIER(v._id).collection}))"//{id:v._id,r:v.rank,t:PARSE_IDENTIFIER(v._id).collection,n:v.name,c:v.current})"
				+ ",links:(FOR n IN GRAPH_EDGES('%1$s',%2$s,{edgeCollectionRestriction:%3$s}) "
				+ "LET d=DOCUMENT(n) FILTER d._from IN %2$s && d._to IN %2$s"
				+ "RETURN MERGE(d,{type:PARSE_IDENTIFIER(d).collection}))}"	//{id:d._id,source:d._from,target:d._to,current:d.current,type:PARSE_IDENTIFIER(d).collection})}"
				,Constants.TAXONOMICGRAPHNAME,EntityFactory.toJsonString(id),EntityFactory.toJsonString(art)
			);
			String res=driver.executeAqlQueryJSON(query, null, null);
			// NOTE: server responses are always an array, but here we always have one element, so we remove the []
			return (res==null || res.equals("[]")) ? GraphUpdateResult.emptyResult() : GraphUpdateResult.fromJson(res.substring(1, res.length()-1));
		}
	}
	
	/**
	 * A class that groups methods for higher-level queries.
	 * @author miguel
	 *
	 */
	public final class GeneralQueries {
	    /**
	     * Execute a text query that filters nodes by their name, and returns all species (or inferior rank) downstream the filtered nodes.
	     * <b>This is the main query function.</b> 
	     * @param q The query as a String. It is matched as a whole to the node 'name' attribute.
	     * @param matchtype Type of match desired (exact, partial or prefix).
	     * @param onlyLeafNodes true to return only leaf nodes. If false, all species or inferior rank nodes are returned.
	     * @param collections Node collections to be searched for matches. They must have a 'name' attribute.
	     * @return A list of {@link SimpleTaxonResult}
	     * @throws ArangoException
	     */
	    public List<SimpleTaxonResult> speciesTextQuerySimple(String q,StringMatchTypes matchtype,boolean onlyLeafNodes,String[] collections,TaxonRanks rank) throws ArangoException {
	    	// TODO put vertex collection restrictions in the options
	    	String query;
	    	q=q.toLowerCase().trim();
	    	String filter="";
	    	switch(matchtype) {
	    	case EXACT:
	    		filter="LOWER(v.name)=='%2$s'";
				break;
			case PARTIAL:
				filter="LIKE(v.name,'%%%2$s%%',true)";
				break;
			case PREFIX:
				filter="LIKE(v.name,'%2$s%%',true)";
				break;
			default:
				break;
	    	}
	    	String leaf=onlyLeafNodes ? " FILTER nedg==0" : "";
	    	
	    	if(rank!=null) {
	    		if(filter=="")
	    			filter="v.rank=="+rank.getValue().toString();
	    		else
	    			filter+=" && v.rank=="+rank.getValue().toString();
	    	}
	    	
	    	if(collections==null) {
	    		collections=new String[1];
	    		collections[0]="taxent";
	    	}
// FIXME SYNONYMS are bidirectional!
	    	if(collections.length==1) {	// if there's only one collection, it's faster not to use GRAPH_VERTICES (as of 2.7)
	    		query=String.format("LET base=(FOR v IN %3$s FILTER "+filter+" RETURN v._id) FOR o IN FLATTEN("
					+ "FOR v IN base FOR v1 IN GRAPH_TRAVERSAL('%1$s',v,'inbound',{paths:true,filterVertices:[{isSpeciesOrInf:true}],vertexFilterMethod:['exclude']}) "
					+ "RETURN FLATTEN(FOR v2 IN v1[*] LET nedg=LENGTH(FOR e IN PART_OF FILTER e._to==v2.vertex._id RETURN e)"+leaf+" "
					+ "RETURN {source:v,name:v2.vertex.name,_key:v2.vertex._key,leaf:nedg==0,edges: (FOR ed IN v2.path.edges RETURN PARSE_IDENTIFIER(ed._id).collection)})) "
					+ "COLLECT k=o._key,n=o.name,l=o.leaf INTO gr RETURN {name:n,_key:k,leaf:l,match:gr[*].o.source,reltypes:UNIQUE(FLATTEN(gr[*].o.edges))}"
					,Constants.TAXONOMICGRAPHNAME,q,collections[0]);
/*
		    	query=String.format("LET base=(FOR v IN %3$s FILTER "+filter+" RETURN v._id) "
		        		+ "FOR o IN FLATTEN(FOR v IN base "
		    				+ "FOR v1 IN GRAPH_TRAVERSAL('%1$s',v,'inbound',{paths:false,filterVertices:[{isSpeciesOrInf:true}],vertexFilterMethod:['exclude']}) "
		    				+ "RETURN (FOR v2 IN v1[*].vertex LET nedg=LENGTH(FOR e IN PART_OF FILTER e._to==v2._id RETURN e)"+leaf+" "		//LET nedg=LENGTH(GRAPH_EDGES('%1$s',v2,{direction:'inbound'}))"+leaf+" "
		    				+ "RETURN {source:v,name:v2.name,_key:v2._key,leaf:nedg==0})) "
		    				+ "COLLECT k=o._key,n=o.name,l=o.leaf INTO gr RETURN {name:n,_key:k,match:gr[*].o.source,leaf:l}"
		    				,Constants.TAXONOMICGRAPHNAME,q,collections[0]);*/
			} else {
				StringBuilder sb=new StringBuilder();
				sb.append("[");
				for(int i=0;i<collections.length-1;i++) {
					sb.append("'").append(collections[i]).append("',");
				}
				sb.append("'").append(collections[collections.length-1]).append("']");

				query=String.format("LET base=(FOR v IN GRAPH_VERTICES('%1$s',{},{vertexCollectionRestriction:%3$s}) FILTER "+filter+" RETURN v._id) "
					+ "FOR o IN FLATTEN(FOR v IN base FOR v1 IN GRAPH_TRAVERSAL('%1$s',v,'inbound',{paths:true,filterVertices:[{isSpeciesOrInf:true}],vertexFilterMethod:['exclude']}) "
					+ "RETURN FLATTEN(FOR v2 IN v1[*] LET nedg=LENGTH(FOR e IN PART_OF FILTER e._to==v2.vertex._id RETURN e)"+leaf+" "
					+ "RETURN {source:v,name:v2.vertex.name,_key:v2.vertex._key,leaf:nedg==0,edges: (FOR ed IN v2.path.edges RETURN PARSE_IDENTIFIER(ed._id).collection)})) "
					+ "COLLECT k=o._key,n=o.name,l=o.leaf INTO gr RETURN {name:n,_key:k,leaf:l,match:gr[*].o.source,reltypes:UNIQUE(FLATTEN(gr[*].o.edges))}"
					,Constants.TAXONOMICGRAPHNAME,q,collections[0]);
/*				
		    	query=String.format("LET base=(FOR v IN GRAPH_VERTICES('%1$s',{},{vertexCollectionRestriction:%3$s}) FILTER "+filter+" RETURN v._id) "
		        		+ "FOR o IN FLATTEN(FOR v IN base "
		    				+ "FOR v1 IN GRAPH_TRAVERSAL('%1$s',v,'inbound',{paths:false,filterVertices:[{isSpeciesOrInf:true}],vertexFilterMethod:['exclude']}) "
		    				+ "RETURN (FOR v2 IN v1[*].vertex LET nedg=LENGTH(FOR e IN PART_OF FILTER e._to==v2._id RETURN e)"+leaf+" "		//LET nedg=LENGTH(GRAPH_EDGES('%1$s',v2,{direction:'inbound'}))"+leaf+" "
		    				+ "RETURN {source:v,name:v2.name,_key:v2._key,leaf:nedg==0})) "
		    				+ "COLLECT k=o._key,n=o.name,l=o.leaf INTO gr RETURN {name:n,_key:k,leaf:l,match:gr[*].o.source}"
		    				,Constants.TAXONOMICGRAPHNAME,q,sb.toString());*/
			}
	/*    	
	    	String query=String.format("FOR v IN UNIQUE(FLATTEN(FOR v IN GRAPH_TRAVERSAL('%1$s',"
	    			//+ "FOR v IN attribute "
	    			+ "FOR v IN GRAPH_VERTICES('%1$s',{},{vertexCollectionRestriction:%3$s}) "
	    			+ "FILTER "+filter+" RETURN v,'inbound',{paths:false,filterVertices:[{isSpeciesOrInf:true}],vertexFilterMethod:['exclude']}) "
	    			+ "RETURN v[*].vertex)) LET nedg=LENGTH(GRAPH_EDGES('taxgraph',v,{direction:'inbound'})) "+leaf+" RETURN {name:v.name,_key:v._key,leaf:nedg==0}"
	    			, Constants.TAXONOMICGRAPHNAME,q,vertexCollectionRestrictions);*/
	    	//System.out.println(query);
	    	CursorResult<SimpleTaxonResult> vertexCursor=driver.executeAqlQuery(query, null, null, SimpleTaxonResult.class);
	    	return vertexCursor.asList();
	    }

	    /**
	     * Returns a list of all possible matches of the given query string, ordered in terms of relevance.
	     * @param q
	     * @param matchtype
	     * @param collections
	     * @return
	     * @throws ArangoException
	     */
	    public List<Match> queryMatcher(String q,StringMatchTypes matchtype,String[] collections) throws ArangoException {
	    	String query;
	    	q=q.toLowerCase().trim();
	    	String filter="";
	    	switch(matchtype) {
	    	case EXACT:
	    		filter="LOWER(v.name)=='%2$s'";
				break;
			case PARTIAL:
				filter="LIKE(v.name,'%%%2$s%%',true)";
				break;
			case PREFIX:
				filter="LIKE(v.name,'%2$s%%',true)";
				break;
			default:
				break;
	    	}
	    	
	    	if(collections==null) collections=new String[] {"taxent"};

			// this is actually a workaround so we don't use GRAPH_VERTICES when there are more than 1 collection in the filters, it's faster to do separately
	    	List<Match> res=new ArrayList<Match>();
	    	for(String collection : collections) {
		    	query=String.format("FOR v IN %3$s FILTER "+filter+" "
	    			+ "LET co=PARSE_IDENTIFIER(v._id).collection "
	    			+ "LET typematch=LIKE(v.name,'%2$s',true) ? 0 : (LIKE(v.name,'%2$s%%',true) ? 1 : 2) "	// NOTE: these numbers must correspond to the enum order StringMatchTypes
	    			+ "COLLECT c=co,r=v.rank,tm=typematch INTO gr SORT tm,r,LENGTH(gr) "
	    			+ "RETURN {rank:r,nodeType:c,matchType:tm,matches:gr[*].v.name,query:'%2$s'}"
	    			,Constants.TAXONOMICGRAPHNAME,q,collection);
		    	res.addAll(driver.executeAqlQuery(query, null, null, Match.class).asList());
	    	}
	    	return res;
/*	    	
	    	if(collections.length==1) {	// if there's only one collection, it's faster not to use GRAPH_VERTICES (as of 2.7)
		    	query=String.format("FOR v IN %3$s FILTER "+filter+" "
	    			+ "LET co=PARSE_IDENTIFIER(v._id).collection "
	    			+ "LET typematch=LIKE(v.name,'%2$s',true) ? 0 : (LIKE(v.name,'%2$s%%',true) ? 1 : 2) "	// NOTE: these numbers must correspond to the enum order StringMatchTypes
	    			+ "COLLECT c=co,r=v.rank,tm=typematch INTO gr SORT tm,r,LENGTH(gr) "
	    			+ "RETURN {rank:r,nodeType:c,matchType:tm,matches:gr[*].v.name,query:'%2$s'}"
	    			,Constants.TAXONOMICGRAPHNAME,q,collections[0]);
			} else {
				StringBuilder sb=new StringBuilder();
				sb.append("[");
				for(int i=0;i<collections.length-1;i++) {
					sb.append("'").append(collections[i]).append("',");
				}
				sb.append("'").append(collections[collections.length-1]).append("']");
				
		    	query=String.format("FOR v IN GRAPH_VERTICES('%1$s',{},{vertexCollectionRestriction:%3$s}) FILTER "+filter+" "
	    			+ "LET co=PARSE_IDENTIFIER(v._id).collection "
	    			+ "LET typematch=LIKE(v.name,'%2$s',true) ? 0 : (LIKE(v.name,'%2$s%%',true) ? 1 : 2) "	// NOTE: these numbers must correspond to the enum order StringMatchTypes
	    			+ "COLLECT c=co,r=v.rank,tm=typematch INTO gr SORT tm,r,LENGTH(gr) "
	    			+ "RETURN {rank:r,nodeType:c,matchType:tm,matches:gr[*].v.name,query:'%2$s'}"
	    			,Constants.TAXONOMICGRAPHNAME,q,sb.toString());
	    			
			}
	    	CursorResult<Match> vertexCursor=driver.executeAqlQuery(query, null, null, Match.class);
	    	return vertexCursor.asList();*/
	    }
	    
	    /**
	     * Fetches all species (or inferior rank) downstream the given match
	     * @param match A {@link Match} created by {@link queryMatcher}
	     * @return
	     * @throws ArangoException 
	     */
	    public List<SimpleTaxonResult> fetchMatchSpecies(Match match,boolean onlyLeafNodes) throws ArangoException {
	    	return speciesTextQuerySimple(match.query,match.getMatchType(),onlyLeafNodes,new String[]{match.getNodeType().toString()},match.getRank());
	    }
	    
	    /**
	     * Execute a text query on the starting nodes going upwards. Good idea, but it's actually slower!
	     * @param startingVertices
	     * @param q
	     * @param exact
	     * @return
	     * @throws ArangoException
	     */
	    @Deprecated
	    public List<SimpleTaxonResult> inverseSpeciesTextQuery(List<SimpleTaxonResult> startingVertices,String q,boolean exact) throws ArangoException {
	    	String[] handles=new String[startingVertices.size()];
	    	for(int i=0;i<startingVertices.size();i++) {
	    		handles[i]="taxent/"+startingVertices.get(i).getId();
	    	}
	    	String query="FOR o IN (FOR v IN "+EntityFactory.toJsonString(handles)+" "
				+ "LET p=GRAPH_TRAVERSAL('taxgraph',v,'outbound',{paths:true,filterVertices:'flora::testCode'})[0] LET vv=p[*].path.vertices RETURN "
				+ "{n:DOCUMENT(v),p: (FOR fi IN UNIQUE(FLATTEN(vv[*][* RETURN {n:CURRENT.name,k:CURRENT._id}])) FILTER LIKE(fi.n,'%"+q+"%',true)"
				+ " RETURN {n:fi.n,k:fi.k}) }) FILTER LENGTH(o.p)>0 RETURN {name:o.n.name,_key:o.n._key,match:o.p[*].k}";
	    	System.out.println("INVERSE QUERY\n"+query);
	    	CursorResult<SimpleTaxonResult> vertexCursor=driver.executeAqlQuery(query, null, null, SimpleTaxonResult.class);
	    	return vertexCursor.asList();
	    }
	}
	
	public final class SpecificQueries {
	    /**
	     * Gets the number of nodes in given collection.
	     * @param nodetype The collection
	     * @return
	     * @throws ArangoException
	     */
	    public int getNumberOfNodesInCollection(NodeTypes nodetype) throws ArangoException {
	    	String query="FOR v IN "+nodetype.toString()+" COLLECT WITH COUNT INTO cou RETURN cou";
	    	return driver.executeAqlQuery(query, null, null, Integer.class).getUniqueResult();
	    }
	    	
		/**
		 * Gets all species lists within a radius of a given point
		 * @param latitude The point's latitude
		 * @param longitude The point's longitude
		 * @param distance The radius
		 * @return
		 * @throws ArangoException 
		 */
		public Iterator<SpeciesList> findSpeciesListsWithin(Float latitude,Float longitude,Float distance) throws ArangoException {
	    	String query=String.format("RETURN WITHIN(%4$s,%1$f,%2$f,%3$f,'dist')",latitude,longitude,distance,NodeTypes.specieslist.toString());
	    	CursorResult<SpeciesList> vertexCursor=driver.executeAqlQuery(query, null, null, SpeciesList.class);
	    	return vertexCursor.iterator();
		}
	
		/**
		 * Gets the nearest species list to the given point, no matter how distant it is.
		 * @param latitude
		 * @param longitude
		 * @return A {@link SpeciesListVertex}
		 * @throws ArangoException
		 */
		public SpeciesList findNearestSpeciesList(Float latitude,Float longitude) throws ArangoException {
	    	String query=String.format("RETURN NEAR(%1$s, %2$f, %3$f, 1)[0]",NodeTypes.specieslist.toString(),latitude,longitude);
	    	//System.out.println(query);
	    	SpeciesListVertex vertex=driver.executeAqlQuery(query, null, null, SpeciesListVertex.class).getUniqueResult();
	    	return new SpeciesList(FloraOnGraph.this,vertex);
		}
	
		/**
		 * Gets all species found (in all species lists) within a distance from a point. Note that duplicates are removed, no matter how many occurrences each species has.
		 * @param latitude
		 * @param longitude
		 * @param distance
		 * @return
		 * @throws ArangoException
		 */
		@Deprecated
		public Iterator<SimpleTaxonResult> findTaxaWithin(Float latitude,Float longitude,int distance) throws ArangoException {
			//"FOR a IN FLATTEN(FOR sl IN WITHIN(%1$s,%2$f,%3$f,%4$d) RETURN NEIGHBORS(%1$s,OBSERVED_IN,sl,'inbound',{},{includeData:true})[*]) RETURN DISTINCT a"
			String query=String.format("FOR a IN FLATTEN(FOR sl IN WITHIN(%1$s,%2$f,%3$f,%4$d) "
				+ "RETURN NEIGHBORS(specieslist,OBSERVED_IN,sl,'inbound',{},{includeData:true})[* RETURN {name:CURRENT.name,_key:CURRENT._key}]) "
				+ "COLLECT b=a WITH COUNT INTO c SORT c DESC RETURN {name:b.name,_key:b._key,count:c}"
				,NodeTypes.specieslist.toString()
				,latitude,longitude,distance
			);
			//System.out.println(query);
	    	CursorResult<SimpleTaxonResult> vertexCursor=driver.executeAqlQuery(query, null, null, SimpleTaxonResult.class);
	    	vertexCursor.asList();
	    	return vertexCursor.iterator();
		}
	
		/**
		 * Gets all species found (in all species lists) within a distance from a point. Note that duplicates are removed, no matter how many occurrences each species has.
		 * Note that this only returns the TaxEnt nodes which are direct neighbors of the species list, independently of their taxonomic rank.
		 * @param latitude
		 * @param longitude
		 * @param distance
		 * @return A list
		 * @throws ArangoException
		 */
		public List<SimpleTaxonResult> findListTaxaWithin(Float latitude,Float longitude,int distance) throws ArangoException {
			String query=String.format("FOR sl IN WITHIN(%1$s,%2$f,%3$f,%4$d) "
					+ "FOR o IN (FOR n IN NEIGHBORS(specieslist,%5$s,sl,'inbound',{},{includeData:true}) "
					+ "RETURN {match:sl._id,name:n.name,_key:n._key}) "
					+ "COLLECT k=o._key,n=o.name INTO gr LET ma=gr[*].o.match RETURN {name:n,_key:k,match:ma,count:LENGTH(ma),reltypes:['%5$s']}"
					,NodeTypes.specieslist.toString(),latitude,longitude,distance,AllRelTypes.OBSERVED_IN.toString());
			//System.out.println(query);
	    	return driver.executeAqlQuery(query, null, null, SimpleTaxonResult.class).asList();
		}
	
		/**
		 * Gets all occurrences within a radius of given coordinates
		 * @param latitude
		 * @param longitude
		 * @param distance
		 * @return
		 * @throws ArangoException
		 */
		public Iterator<Occurrence> findOccurrencesWithin(Float latitude,Float longitude,int distance) throws ArangoException {
	    	String aqlQuery=String.format("FOR v2 IN WITHIN(%1$s,%4$f,%5$f,%6$d) "
				+ "LET nei=EDGES(%2$s,v2,'inbound') FILTER LENGTH(nei)>0 "
				+ "LET mainaut=DOCUMENT(NEIGHBORS(%1$s,%3$s,v2,'outbound',{main:true})) "
				+ "LET aut=DOCUMENT(NEIGHBORS(%1$s,%3$s,v2,'outbound',{main:false})) "
				+ "FOR n IN nei RETURN {name:DOCUMENT(n._from).name,confidence:n.confidence,weight:n.weight,phenoState:n.phenoState,wild:n.wild"
				+ ",uuid:n.uuid,dateInserted:n.dateInserted,inventoryKey:v2._key,occurrenceKey:n._key,location:v2.location,observers:APPEND(mainaut[*].name,aut[*].name)}"
				,NodeTypes.specieslist.toString(),AllRelTypes.OBSERVED_IN.toString(),AllRelTypes.OBSERVED_BY.toString()
				,latitude,longitude,distance);
	    	CursorResult<Occurrence> vertexCursor=driver.executeAqlQuery(aqlQuery, null, null, Occurrence.class);
	    	return vertexCursor.iterator();
		}
		
		/**
		 * Checks whether given species list already exists (same author, same date, coordinates very close) and returns it.
		 * @param idAuthor
		 * @param latitude
		 * @param longitude
		 * @param year
		 * @param month
		 * @param day
		 * @param radius Radius in which to search for the species list
		 * @return Null if not found, a {@link SpeciesList} if one or more results are found. In the latter case, the returned result is "randomly" selected.
		 * @throws ArangoException
		 */
		public SpeciesList findExistingSpeciesList(int idAuthor,float latitude,float longitude,Integer year,Integer month,Integer day,float radius) throws ArangoException {
			StringBuilder sb=new StringBuilder();
			sb.append("FOR sl IN WITHIN(%1$s,%2$f,%3$f,%4$f) FILTER sl.year==")
				.append(year).append(" && sl.month==")
				.append(month).append(" && sl.day==")
				.append(day)
				.append(" LET nei=GRAPH_NEIGHBORS('%6$s',sl,{direction:'outbound',neighborExamples:{idAut:%5$d},edgeExamples:{main:true},edgeCollectionRestriction:'OBSERVED_BY',includeData:true}) FILTER LENGTH(nei)>0 RETURN sl");
	
			String query=String.format(sb.toString(), NodeTypes.specieslist.toString(),latitude,longitude,radius,idAuthor,Constants.TAXONOMICGRAPHNAME.toString());
	
			SpeciesListVertex vertexCursor = null;
			try {
				vertexCursor = driver.executeAqlQuery(query, null, null, SpeciesListVertex.class).getUniqueResult();
			} catch (NonUniqueResultException e) {
				System.out.println("\nWarning: more than one species list found on "+latitude+" "+longitude+", selecting one randomly.");
				vertexCursor=driver.executeAqlQuery(query, null, null, SpeciesListVertex.class).iterator().next();
			}
			if(vertexCursor==null)
				return null;
			else
				return new SpeciesList(FloraOnGraph.this,vertexCursor);
		}
	}
		
	/**
	 * Gets all occurrences, in a simplified format, of the taxa contained in that whose name exactly matches the query.
	 * @param taxname A taxon name
	 * @return An iterator of {@link Occurrence}s. Author array is sorted so that the first author is the main observer.
	 * @throws ArangoException
	 */
    public Iterator<Occurrence> getTaxonSimpleOccurrences(String taxname) throws ArangoException {
    	String aqlQuery=String.format(
			"FOR v2 IN UNIQUE(FLATTEN(FOR v IN GRAPH_TRAVERSAL('%1$s',"
			+ "GRAPH_VERTICES('%1$s',{name:'%2$s'},{vertexCollectionRestriction:'%3$s'}),'inbound',{paths:false}) "
			+ "RETURN v[*].vertex)) LET nei=NEIGHBORS(%3$s,%4$s,v2,'outbound') FILTER LENGTH(nei)>0 "
			+ "FOR n IN nei LET mainaut=DOCUMENT(NEIGHBORS(%6$s,%5$s,n,'outbound',{main:true})) "
			+ "LET aut=DOCUMENT(NEIGHBORS(%6$s,%5$s,n,'outbound',{main:false})) "
			+ "RETURN {name:v2.name,location:DOCUMENT(n).location,observers:APPEND(mainaut[*].name,aut[*].name)}"
			,Constants.TAXONOMICGRAPHNAME,taxname,NodeTypes.taxent.toString()
			,AllRelTypes.OBSERVED_IN.toString(),AllRelTypes.OBSERVED_BY.toString(),NodeTypes.specieslist.toString());
    	CursorResult<Occurrence> vertexCursor=this.driver.executeAqlQuery(aqlQuery, null, null, Occurrence.class);
    	return vertexCursor.iterator();
    }

    /**
     * Gets all occurrences of taxa which match the query in any facet
     * @param query String to be partially matched in any facet (taxonomic, morphological, ecological, etc.)
     * @return An iterator of {@link Occurrence}s. Author array is sorted so that the first author is the main observer.
     * @throws ArangoException
     */
    public Iterator<Occurrence> getQueryOccurrences(String query,String[] collections) throws ArangoException {
    	String aqlQuery;
    	if(collections.length==1) {
    		aqlQuery=String.format(
				"FOR v2 IN UNIQUE(FLATTEN(FOR v IN GRAPH_TRAVERSAL('%1$s',"
					+ "FOR v IN %7$s FILTER LIKE(v.name,'%%%2$s%%',true) RETURN v"
					+ ",'inbound',{paths:false}) RETURN v[*].vertex)) "
					+ "LET nei=EDGES(%4$s,v2,'outbound') FILTER LENGTH(nei)>0 "
					+ "FOR n IN nei LET sl=DOCUMENT(n._to) "
					+ "LET mainaut=DOCUMENT(NEIGHBORS(%6$s,%5$s,sl,'outbound',{main:true})) "
					+ "LET aut=DOCUMENT(NEIGHBORS(%6$s,%5$s,sl,'outbound',{main:false})) "
					+ "RETURN {name:v2.name,confidence:n.confidence,weight:n.weight,phenoState:n.phenoState,wild:n.wild"
					+ ",uuid:n.uuid,dateInserted:n.dateInserted,inventory:sl._key,location:sl.location,observers:APPEND(mainaut[*].name,aut[*].name)}"
					,Constants.TAXONOMICGRAPHNAME,query,NodeTypes.taxent.toString(),AllRelTypes.OBSERVED_IN.toString()
					,AllRelTypes.OBSERVED_BY.toString(),NodeTypes.specieslist.toString(),collections[0]);
    	} else {
    		aqlQuery=String.format(
				"FOR v2 IN UNIQUE(FLATTEN(FOR v IN GRAPH_TRAVERSAL('%1$s',"
					+ "FOR v IN GRAPH_VERTICES('%1$s',{},{vertexCollectionRestriction:%7$s}) FILTER LIKE(v.name,'%%%2$s%%',true) RETURN v"
					+ ",'inbound',{paths:false}) RETURN v[*].vertex)) "
					+ "LET nei=EDGES(%4$s,v2,'outbound') FILTER LENGTH(nei)>0 "
					+ "FOR n IN nei LET sl=DOCUMENT(n._to) "
					+ "LET mainaut=DOCUMENT(NEIGHBORS(%6$s,%5$s,sl,'outbound',{main:true})) "
					+ "LET aut=DOCUMENT(NEIGHBORS(%6$s,%5$s,sl,'outbound',{main:false})) "
					+ "RETURN {name:v2.name,confidence:n.confidence,weight:n.weight,phenoState:n.phenoState,wild:n.wild"
					+ ",uuid:n.uuid,dateInserted:n.dateInserted,inventory:sl._key,location:sl.location,observers:APPEND(mainaut[*].name,aut[*].name)}"
					,Constants.TAXONOMICGRAPHNAME,query,NodeTypes.taxent.toString(),AllRelTypes.OBSERVED_IN.toString()
					,AllRelTypes.OBSERVED_BY.toString(),NodeTypes.specieslist.toString(),EntityFactory.toJsonString(collections));
    	}
    	CursorResult<Occurrence> vertexCursor=this.driver.executeAqlQuery(aqlQuery, null, null, Occurrence.class);
    	return vertexCursor.iterator();
    }

    /**
     * Gets all occurrences of taxa, in a simplified format, which match the query in any facet
     * @param query String to be partially matched in any facet (taxonomic, morphological, ecological, etc.)
     * @return An iterator of {@link Occurrence}s. Author array is sorted so that the first author is the main observer.
     * Only the taxon, coordinates and authors are filled in; other fields are left null.
     * @throws ArangoException
     */
    public Iterator<Occurrence> getQuerySimpleOccurrences(String query,String[] collections) throws ArangoException {
    	// TODO optimize query, wait for version 2.8?
    	String aqlQuery=String.format(
			"FOR v2 IN UNIQUE(FLATTEN(FOR v IN GRAPH_TRAVERSAL('%1$s',"
			+ "FOR v IN GRAPH_VERTICES('%1$s',{},{vertexCollectionRestriction:%7$s}) FILTER LIKE(v.name,'%%%2$s%%',true) RETURN v"
			//+ "FOR v IN taxent FILTER LIKE(v.name,'%%%2$s%%',true) RETURN v"
			+ ",'inbound',{paths:false}) RETURN v[*].vertex)) "
			+ "LET nei=NEIGHBORS(%3$s,%4$s,v2,'outbound') FILTER LENGTH(nei)>0 "
			+ "FOR n IN nei LET mainaut=DOCUMENT(NEIGHBORS(%6$s,%5$s,n,'outbound',{main:true})) "
			+ "LET aut=DOCUMENT(NEIGHBORS(%6$s,%5$s,n,'outbound',{main:false})) "
			+ "RETURN {name:v2.name,location:DOCUMENT(n).location,observers:APPEND(mainaut[*].name,aut[*].name)}"
			,Constants.TAXONOMICGRAPHNAME,query,NodeTypes.taxent.toString(),AllRelTypes.OBSERVED_IN.toString()
			,AllRelTypes.OBSERVED_BY.toString(),NodeTypes.specieslist.toString(),EntityFactory.toJsonString(collections));
    	CursorResult<Occurrence> vertexCursor=this.driver.executeAqlQuery(aqlQuery, null, null, Occurrence.class);
    	//System.out.println(aqlQuery);
    	return vertexCursor.iterator();
    }

    
    /**
     * Gets all occurrences from DB
     * @return
     * @throws ArangoException
     */
    public Iterator<Occurrence> getAllOccurrences() throws ArangoException {
    	String aqlQuery=String.format("FOR v2 IN %1$s "
			+ "LET nei=EDGES(%2$s,v2,'inbound') FILTER LENGTH(nei)>0 "
			+ "LET mainaut=DOCUMENT(NEIGHBORS(%1$s,%3$s,v2,'outbound',{main:true})) "
			+ "LET aut=DOCUMENT(NEIGHBORS(%1$s,%3$s,v2,'outbound',{main:false})) "
			+ "FOR n IN nei RETURN {name:DOCUMENT(n._from).name,confidence:n.confidence,weight:n.weight,phenoState:n.phenoState,wild:n.wild"
			+ ",uuid:n.uuid,dateInserted:n.dateInserted,inventory:v2._key,location:v2.location,observers:APPEND(mainaut[*].name,aut[*].name)}"
			,NodeTypes.specieslist.toString(),AllRelTypes.OBSERVED_IN.toString(),AllRelTypes.OBSERVED_BY.toString());
    	CursorResult<Occurrence> vertexCursor=this.driver.executeAqlQuery(aqlQuery, null, null, Occurrence.class);
    	System.out.println(aqlQuery);
    	return vertexCursor.iterator();
    }

	/**
	 * A class that groups methods for data uploads from CSV files.
	 * @author miguel
	 *
	 */
	public class DataUploader {
	    /**
		 * Uploads a tab-separated CSV taxonomy file.
		 * The file can have as many columns as needed, the hierarchy goes form left to right.
		 * Authority of a name goes in front of the name between braces {}
		 * Multiple files can be uploaded, as they are merged. In this case, the <u>last column</u> of the new file <b>must match</b> the <u>orphan nodes</u> in the DB
		 * <p>Optionally, the last column may be an ID (if you want to match with old IDs)</p>
	     * @param stream
	     * @param simulate
	     * @return
	     * @throws IOException
	     * @throws TaxonomyException
	     * @throws QueryException
	     */
		public Map<String,Integer> uploadTaxonomyListFromStream(InputStream stream,boolean simulate) throws IOException {
	    	Integer nnodes=0,nrels=0,nrecs=0;
	    	Reader freader;

			freader = new InputStreamReader(stream, StandardCharsets.UTF_8);
			Iterator<CSVRecord> records = CSVFormat.EXCEL.withDelimiter('\t').withQuote('"').parse(freader).iterator();
			CSVRecord record=records.next();

			int ncolumns;
			Boolean hasOldId=false;
			if(record.get(record.size()-1).equals("id")) {ncolumns=record.size()-1;hasOldId=true;} else ncolumns=record.size();
			String[] names=new String[ncolumns];
			for(int i=0;i<ncolumns;i++) names[i]=record.get(i);

			System.out.print("Reading file ");
			try {
				TaxEnt n,parentNode;
				TaxEntName parsedName;
				boolean pastspecies;	// true after the column species has passed (so to know when to append names to genus)
				while(records.hasNext()) {
					record=records.next();
					nrecs++;
					if(nrecs % 100==0) {System.out.print(".");System.out.flush();}
					if(nrecs % 1000==0) {System.out.print(nrecs);System.out.flush();}
					parentNode=null;
					n=null;
					pastspecies=false;
					for(int i=0;i<names.length;i++) {
						parsedName=new TaxEntName(record.get(i));
						if(names[i].equals("species")) pastspecies=true;
						// is it an empty cell? skip. 
						if(parsedName.isNull()) continue;
						// special cases: if species or lower rank, must prepend genus.
						if(pastspecies) parsedName.name=parentNode.getName()+" "+(names[i].equals("species") ? "" : (infraRanks.containsKey(names[i]) ? infraRanks.get(names[i]) : names[i])+" ")+parsedName.name;
						
						parsedName.rank=TaxonRanks.valueOf(names[i].toUpperCase());
						if(pastspecies && parsedName.author==null) parsedName.author=parentNode.getAuthor();
						//System.out.println(parsedname.name);
						n=dbNodeWorker.findTaxEnt(parsedName);
						
						if(n==null) {	// if node does not exist, add it.
							n=new TaxEnt(FloraOnGraph.this,parsedName,true);
							//System.out.println("ADD "+parsedName.name);System.out.flush();
							nnodes++;
						} else {	// if it exists, update its rank and authority.
							//System.out.println("EXISTS "+parsedName.name);System.out.flush();
							n.setRank(parsedName.rank.getValue());
							if(parsedName.author!=null) n.setAuthor(parsedName.author);
							n.saveToDB();
						}
						if(parentNode!=null) {	// create PART_OF relationship to previous column
							nrels+=n.setPART_OF(parentNode.baseNode);
						}
						parentNode=n;
					}
 
					if(hasOldId) {
						n.setOldId(Integer.parseInt(record.get(record.size()-1)));
						n.saveToDB();
					}
				}
			} catch (TaxonomyException | QueryException | ArangoException e) {
				e.printStackTrace();
			}
			freader.close();
			System.out.println("\nRead "+nrecs+" records; created "+nnodes+" nodes and "+nrels+" relationships");
			Map<String,Integer> out=new HashMap<String,Integer>();
			out.put("nrecs", nrecs);
			out.put("nnodes", nnodes);
			out.put("nrels", nrels);
			return out;
		}

		public Map<String,Integer> uploadTaxonomyListFromFile(String file,boolean simulate) throws IOException {
	    	File tl=new File(file);
	    	if(!tl.canRead()) throw new IOException("Cannot read input file "+file);
	    	return uploadTaxonomyListFromStream(new FileInputStream(file),simulate);
	    }
	    
	    public String uploadRecordsFromFile(String filename) throws IOException {
	    	File file=new File(filename);
	    	if(!file.canRead()) throw new IOException("Cannot read file "+filename);
	    	return uploadRecordsFromStream(new FileInputStream(file));
	    }
	    
		public String uploadRecordsFromStream(InputStream stream) throws IOException {
	    	StringBuilder out=new StringBuilder();
	    	Reader freader=null;
	    	Author autnode=null;
	    	TaxEnt taxnode;
	    	SpeciesList sln;
	    	int tmp,i,countupd=0,countnew=0,counterr=0,nrecs=0;
	    	int newsplist=0;
	    	long counter=0;
	    	Integer year,month,day;
	    	float latitude,longitude;
	    	List<Long> lineerrors=new ArrayList<Long>();
	    	Boolean abort=false;//,isupd;
	    	String[] idauts;
	    	System.out.print("Reading records");

    		try {
    			freader = new InputStreamReader(stream, StandardCharsets.UTF_8);
    			Iterable<CSVRecord> records = CSVFormat.MYSQL.parse(freader);
    			for (CSVRecord record : records) {
    				if(record.size()!=19) {
						lineerrors.add(record.getRecordNumber());
						counterr++;
						continue;	
    				}
    				nrecs++;
					if(nrecs % 100==0) {System.out.print(".");System.out.flush();}
					if(nrecs % 1000==0) {System.out.print(nrecs);System.out.flush();}

// check if authors and ident exist in graph
    				idauts=record.get(7).replace("\"", "").split(",");	// there may be several authors. The 1st is the main.
    				
    				//ni=db.findNodesByLabelAndProperty(NodeTypes.specieslist, "idrec", (int)Integer.parseInt(record.get(0)));	// check if record with same idrec exists
    				if(record.get(1).equals("\\N")) year=null; else year=Integer.parseInt(record.get(1));
    				if(record.get(2).equals("\\N")) month=null; else month=Integer.parseInt(record.get(2));
    				if(record.get(3).equals("\\N")) day=null; else day=Integer.parseInt(record.get(3));
    				latitude=Float.parseFloat(record.get(5));
    				longitude=Float.parseFloat(record.get(6));
// search for an existing species list in the same coordinates, same author and same date
    				sln=dbSpecificQueries.findExistingSpeciesList(Integer.parseInt(idauts[0]),latitude,longitude,year,month,day,3);

    				if(sln==null) {	// add new specieslist
// find 1st author
						autnode=dbNodeWorker.getAuthorById((int)Integer.parseInt(idauts[0]));
						if(autnode==null) {			// SKIP line, main observer is compulsory
							lineerrors.add(record.getRecordNumber());
    						counterr++;
    						abort=true;
    						continue;			
						} else {	// first author exists and taxon exists, create node
		    			    tmp=Integer.parseInt(record.get(8));	// precision
		    			    switch(tmp) {
		    			    case 0: tmp=1;break;
		    			    case 1: tmp=100;break;
		    			    case 2: tmp=1000;break;
		    			    case 3: tmp=10000;break;
		    			    }
		   			    	//sln.setProperty("author",(int)Integer.parseInt(idauts[0]));		// this is the main observer (it'll also be created a relationship, but this for indexing purposes)

							sln=new SpeciesList(FloraOnGraph.this,latitude,longitude,year,month,day,tmp,null,null,false);
							if(autnode!=null) sln.setObservedBy(autnode, true);
							newsplist++;
    						//isupd=false;
						}
    				} else {	// TODO: update specieslist?
    					countupd++;
    					//isupd=true;
    				}
    				
    				taxnode=dbNodeWorker.getTaxEntById((int)Integer.parseInt(record.get(4)));	// find taxon with ident, we assume there's only one!

					if(taxnode==null) {	// taxon not found! SKIP line
						lineerrors.add(record.getRecordNumber());
						System.err.println("Taxon with oldID "+(int)Integer.parseInt(record.get(4))+" not found.");
						counterr++;
						abort=true;
					}
/*if(isupd) {
	//sln.setProperty("idrec", (int)Integer.parseInt(record.get(0)));
    sln.setProperty("lat", (float)Float.parseFloat(record.get(5)));
    sln.setProperty("long", (float)Float.parseFloat(record.get(6)));
    if(year!=null) sln.setProperty("year", year); else sln.removeProperty("year");
    if(month!=null) sln.setProperty("month", month); else sln.removeProperty("month");
    if(day!=null) sln.setProperty("day", day); else sln.removeProperty("day");
}*/
		    		if(taxnode!=null) countnew+=taxnode.setObservedIn(sln
		    				,Short.parseShort(record.get(11))	// uncertainty
		    				,Short.parseShort(record.get(9))	// validated?
		    				,(int)Integer.parseInt(record.get(14)) == 1 ? PhenologicalStates.IN_FLOWER : PhenologicalStates.UNKNOWN
		    				,record.get(18).replace("\"", "")
		    				,Integer.parseInt(record.get(17))
		    				,record.get(10).equals("\\N") ? null : record.get(10).replace("\n", "").replace("\"", "")
		    				,Integer.parseInt(record.get(15))==0 ? NativeStatus.WILD : NativeStatus.NATURALIZED
		    				,record.get(12).equals("\\N") ? null : record.get(12).replace("\"", "")
						);
    			    if(idauts.length>1) {	// supplementary observers
    			    	for(i=1;i<idauts.length;i++) {
    			    		autnode=dbNodeWorker.getAuthorById((int)Integer.parseInt(idauts[i]));
    						if(autnode==null) {
    							lineerrors.add(record.getRecordNumber());
        						counterr++;
        						abort=true;
    						} else {
    							sln.setObservedBy(autnode, false);
    						}
    			    	}
    			    }
    			    counter++;
    			    if((counter % 2500)==0) {
    			    	System.out.println(counter+" records processed.");
    			    	out.append(newsplist+" species lists added; "+countupd+" updated; "+countnew+" new observations inserted; "+counterr+" warning (lines skipped).");
    			    }
    			}
			} catch (NumberFormatException e) {
				e.printStackTrace();
			} catch (ArangoException e) {
				e.printStackTrace();
				counterr++;
			} finally {
    			if(freader!=null) freader.close();
    			out.append(newsplist+" species lists added; "+countupd+" updated; "+countnew+" new observations inserted; "+counterr+" warning (lines skipped).");
			}

	    	if(abort) throw new IOException(counterr+" errors found on lines "+lineerrors.toString());
	    	return out.toString();
	    }
		
		public Map<String,Integer> uploadAuthorsFromFile(String filename) throws IOException, NumberFormatException, ArangoException {
	    	File file=new File(filename);
	    	if(!file.canRead()) throw new IOException("Cannot read file "+filename);
	    	return uploadAuthorsFromStream(new FileInputStream(file));
		}
		
	    public Map<String,Integer> uploadAuthorsFromStream(InputStream stream) throws IOException, NumberFormatException, ArangoException {
	    	Author autnode;
	    	Reader freader=null;
	    	int countnew=0,countupd=0;
	    	Iterable<CSVRecord> records=null;
	    	
			freader = new InputStreamReader(stream, StandardCharsets.UTF_8);
			records = CSVFormat.MYSQL.parse(freader);
			for (CSVRecord record : records) {
				autnode=dbNodeWorker.getAuthorById((int)Integer.parseInt(record.get(0)));
				if(autnode==null) {	// add author
					new Author(FloraOnGraph.this
						,(int)Integer.parseInt(record.get(0))
						,record.get(1).replace("\"", "")
						,!record.get(4).equals("\\N") ? record.get(4).replace("\"", "") : null
						,record.get(2).replace("\"", "")
						,!record.get(5).equals("\\N") ? record.get(5).replace("\"", "") : null
						,(int)Integer.parseInt(record.get(3)));
					countnew++;
				} else {
					// TODO update author if it exists! does it make sense here?
					countupd++;
				}
			}
			freader.close();
			Map<String,Integer> out=new HashMap<String,Integer>();
			out.put("new", countnew);
			out.put("upd", countupd);
			return out;
	    }

	    public String uploadMorphologyFromFile(String filename) throws IOException, ArangoException {
	    	File file=new File(filename);
	    	if(!file.canRead()) throw new IOException("Cannot read file "+filename);
	    	return uploadMorphologyFromStream(new FileInputStream(file));
	    }
		/**
		 * Uploads qualities as associates with taxa. First column is the taxa, the following columns the qualities.
		 * @param file
		 * @return
		 * @throws IOException
		 * @throws ArangoException 
		 */
		public String uploadMorphologyFromStream(InputStream stream) throws IOException, ArangoException {
			StringBuilder err=new StringBuilder();
	    	Reader freader;
	    	TaxEntName fullname1;
	    	TaxEnt n1;
	    	Attribute an;
	    	int nerrors=0,nnodes=0,nrels=0;
			
			freader = new InputStreamReader(stream, StandardCharsets.UTF_8);
			CSVParser csvp=CSVFormat.EXCEL.withDelimiter('\t').withQuote('"').withHeader().parse(freader);
			Iterator<CSVRecord> records =csvp.iterator();
			
			Iterator<Entry<String,Integer>> characters=csvp.getHeaderMap().entrySet().iterator();
			List<Character> colnames=new ArrayList<Character>();
			characters.next();	// skip 1st column
			while(characters.hasNext()) {
				colnames.add(new Character(FloraOnGraph.this,characters.next().getKey(),null,null));
			}
			
			CSVRecord record;
			String[] attrs;
			int count=0;
			while(records.hasNext()) {
				record=records.next();
				count++;
				if(count % 50==0) {System.out.print(".");System.out.flush();}
				if(count % 500==0) {System.out.print(count);System.out.flush();}
				try {
					fullname1=new TaxEntName(record.get("taxon"));
					n1=dbNodeWorker.findTaxEnt(fullname1);
					if(n1==null) throw new QueryException(fullname1.name+" not found.");
					for(int i=1;i<record.size();i++) {
						attrs=record.get(i).split(",");
						for(String attr:attrs) {
							attr=attr.trim();
							if(attr.length()==0) continue;
							if(attr.equalsIgnoreCase("NA")) {
								// TODO: handle when character not applicable
							} else {
								an=dbNodeWorker.findAttribute(attr);
								if(an==null) {
									an=new Attribute(FloraOnGraph.this,attr,null,null);
									an.setAttributeOfCharacter(colnames.get(i-1));
									//System.out.println("Added \""+attr+"\" of \""+colnames.get(i)+"\"");
									nnodes++;
								}
								nrels+=an.setHAS_QUALITY(n1);
							}
						}
					}
				} catch (QueryException | IllegalArgumentException | TaxonomyException | ArangoException e) {
					err.append("Error processing line "+record.getRecordNumber()+": "+e.getMessage()+"\n");
					System.err.println("Error processing line "+record.getRecordNumber()+": "+e.getMessage());
					nerrors++;
					continue;
				}
			}

			freader.close();
			if(nerrors>0)
				return nerrors+" errors found while parsing file. Nothing changed.<br/><textarea>"+err.toString()+"</textarea>";
			else
				return nnodes+" new nodes added and "+nrels+" relationships created.";
		}
	}
}
