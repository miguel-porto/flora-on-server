package pt.floraon.driver;

import static pt.floraon.driver.Constants.*;

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
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.internal.LinkedTreeMap;

import pt.floraon.driver.Constants.TaxonRanks;
import pt.floraon.entities.Attribute;
import pt.floraon.entities.AttributeVertex;
import pt.floraon.entities.Author;
import pt.floraon.entities.AuthorVertex;
import pt.floraon.entities.Character;
import pt.floraon.entities.GeneralDBNode;
import pt.floraon.entities.GeneralDBNodeImpl;
import pt.floraon.entities.GeneralNodeWrapperImpl;
import pt.floraon.entities.Image;
import pt.floraon.entities.SYNONYM;
import pt.floraon.entities.SpeciesList;
import pt.floraon.entities.SpeciesListVertex;
import pt.floraon.entities.TaxEnt;
import pt.floraon.entities.TaxEntVertex;
import pt.floraon.entities.Territory;
import pt.floraon.entities.TerritoryVertex;
import pt.floraon.queryparser.Match;
import pt.floraon.results.ChecklistEntry;
import pt.floraon.results.GraphUpdateResult;
import pt.floraon.results.NamesAndTerritoriesResult;
import pt.floraon.results.NativeStatusResult;
import pt.floraon.results.Occurrence;
import pt.floraon.results.SimpleNameResult;
import pt.floraon.results.SimpleTaxonResult;

public class FloraOnDriver {
	public ArangoDriver driver;
	public final GeneralQueries dbGeneralQueries;
	public final NodeWorker dbNodeWorker;
	public final DataUploader dbDataUploader;
	public final SpecificQueries dbSpecificQueries;
	public final List<TerritoryVertex> territories;
	
	public FloraOnDriver(String dbname) throws ArangoException {
        ArangoConfigure configure = new ArangoConfigure();
        configure.init();
        configure.setDefaultDatabase("flora");
        this.dbGeneralQueries=new FloraOnDriver.GeneralQueries();
        this.dbNodeWorker=new FloraOnDriver.NodeWorker();
        this.dbDataUploader=new FloraOnDriver.DataUploader();
        this.dbSpecificQueries=new FloraOnDriver.SpecificQueries();
        
        driver = new ArangoDriver(configure);
/*
        driver.createAqlFunction("flora::hybridVisitor", "function (config, result, vertex, path, connections) {"
    		+ "result.push({vertex});"
    		+ "}");*/
        /*
        driver.createAqlFunction("flora::testCode", "function (config, vertex, path) {"
    		+ "if(!vertex.name) return ['exclude','prune'];"
    		+ "}");*/
                
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
        this.territories=dbGeneralQueries.getAllTerritories(true).asList();
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
		for(RelTypes nt:RelTypes.values()) {
			driver.createCollection(nt.toString(),co);
		}

		List<EdgeDefinitionEntity> edgeDefinitions = new ArrayList<EdgeDefinitionEntity>();

		// taxonomic relations
		EdgeDefinitionEntity edgeDefinition = new EdgeDefinitionEntity();
		// define the edgeCollection to store the edges
		edgeDefinition.setCollection(RelTypes.PART_OF.toString());
		// define a set of collections where an edge is going out...
		List<String> from = new ArrayList<String>();
		// and add one or more collections
		from.add(NodeTypes.taxent.toString());
		from.add(NodeTypes.territory.toString());
		edgeDefinition.setFrom(from);
		 // repeat this for the collections where an edge is going into  
		List<String> to = new ArrayList<String>();
		to.add(NodeTypes.taxent.toString());
		from.add(NodeTypes.territory.toString());
		edgeDefinition.setTo(to);
		edgeDefinitions.add(edgeDefinition);

		edgeDefinition = new EdgeDefinitionEntity();
		edgeDefinition.setCollection(RelTypes.HYBRID_OF.toString());
		from = new ArrayList<String>();
		from.add(NodeTypes.taxent.toString());
		edgeDefinition.setFrom(from);
		to = new ArrayList<String>();
		to.add(NodeTypes.taxent.toString());
		edgeDefinition.setTo(to);
		edgeDefinitions.add(edgeDefinition);

		edgeDefinition = new EdgeDefinitionEntity();
		edgeDefinition.setCollection(RelTypes.SYNONYM.toString());
		from = new ArrayList<String>();
		from.add(NodeTypes.taxent.toString());
		edgeDefinition.setFrom(from);
		to = new ArrayList<String>();
		to.add(NodeTypes.taxent.toString());
		edgeDefinition.setTo(to);
		edgeDefinitions.add(edgeDefinition);

		// species list subgraph
		edgeDefinition = new EdgeDefinitionEntity();
		edgeDefinition.setCollection(RelTypes.OBSERVED_IN.toString());
		from = new ArrayList<String>();
		from.add(NodeTypes.taxent.toString());
		edgeDefinition.setFrom(from);
		to = new ArrayList<String>();
		to.add(NodeTypes.specieslist.toString());
		edgeDefinition.setTo(to);
		edgeDefinitions.add(edgeDefinition);

		edgeDefinition = new EdgeDefinitionEntity();
		edgeDefinition.setCollection(RelTypes.OBSERVED_BY.toString());
		from = new ArrayList<String>();
		from.add(NodeTypes.specieslist.toString());
		edgeDefinition.setFrom(from);
		to = new ArrayList<String>();
		to.add(NodeTypes.author.toString());
		edgeDefinition.setTo(to);
		edgeDefinitions.add(edgeDefinition);

		// attributes <- taxent
		edgeDefinition = new EdgeDefinitionEntity();
		edgeDefinition.setCollection(RelTypes.HAS_QUALITY.toString());
		from = new ArrayList<String>();
		from.add(NodeTypes.taxent.toString());
		edgeDefinition.setFrom(from);
		to = new ArrayList<String>();
		to.add(NodeTypes.attribute.toString());
		edgeDefinition.setTo(to);
		edgeDefinitions.add(edgeDefinition);

		// characters <- attributes
		edgeDefinition = new EdgeDefinitionEntity();
		edgeDefinition.setCollection(RelTypes.ATTRIBUTE_OF.toString());
		from = new ArrayList<String>();
		from.add(NodeTypes.attribute.toString());
		edgeDefinition.setFrom(from);
		to = new ArrayList<String>();
		to.add(NodeTypes.character.toString());
		edgeDefinition.setTo(to);
		edgeDefinitions.add(edgeDefinition);

		// territory <- taxent
		edgeDefinition = new EdgeDefinitionEntity();
		edgeDefinition.setCollection(RelTypes.EXISTS_IN.toString());
		from = new ArrayList<String>();
		from.add(NodeTypes.taxent.toString());
		edgeDefinition.setFrom(from);
		to = new ArrayList<String>();
		to.add(NodeTypes.territory.toString());
		edgeDefinition.setTo(to);
		edgeDefinitions.add(edgeDefinition);

		driver.createGraph(Constants.TAXONOMICGRAPHNAME, edgeDefinitions, null, true);
		driver.createGeoIndex(NodeTypes.specieslist.toString(), false, "location");
		driver.createHashIndex("author", true, "idAut");
		driver.createHashIndex("taxent", true, true, "oldId");
		driver.createHashIndex("taxent", false, true, "rank");
		driver.createHashIndex("taxent", false, false, "isSpeciesOrInf");
	}
	
	/**
	 * Gets the complete list of taxa in the DB
	 * @return
	 */
	public List<ChecklistEntry> getCheckList() {
		// TODO the query is very slow!
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
			+ "FOR v IN taxent FILTER v.isSpeciesOrInf==true && v.current==true && LENGTH(FOR e IN PART_OF FILTER e._to==v._id RETURN e)==0 RETURN v"	// leaf nodes
			+ ",'outbound',{paths:false,filterVertices:[%3$s],vertexFilterMethod:['exclude']}) COLLECT a=v[*].vertex RETURN a"
   			, Constants.TAXONOMICGRAPHNAME,RelTypes.PART_OF.toString(),Constants.CHECKLISTFIELDS);

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
							chk.taxon=te.baseNode.getFullName();
							chk.canonicalName=te.baseNode.getName();
						}
					}
					switch(te.baseNode.getRank()) {
					case GENUS:
						chk.genus=te.baseNode.getName();
						break;
					case FAMILY:
						chk.family=te.baseNode.getName();
						break;
					case ORDER:
						chk.order=te.baseNode.getName();
						break;
					default:
						break;
					}
				}
				chklst.add(chk);
			}
		} catch (ArangoException | FloraOnException e) {
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
				return new Author(FloraOnDriver.this,vertexCursor);
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
				return new TaxEnt(FloraOnDriver.this,vertexCursor);
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
	     * @return A {@link GraphUpdateResult} with the new node.
	     * @throws ArangoException
	     * @throws TaxonomyException 
	     */
		@Deprecated
	    public GraphUpdateResult createTaxEntNode(String name,String author,TaxonRanks rank,String annotation,Boolean current) throws ArangoException, TaxonomyException {
	    	return GraphUpdateResult.fromHandle(
    			FloraOnDriver.this, TaxEnt.newFromName(FloraOnDriver.this,name,author,rank,annotation,current).getID()
			);
	    }
	    
	    /**
	     * Creates a new territory and immediately add it to DB.
	     * @param name
	     * @param shortName
	     * @return
	     * @throws ArangoException
	     * @throws FloraOnException 
	     */
	    public GraphUpdateResult createTerritory(String name,String shortName, ArangoKey parentId) throws ArangoException, FloraOnException {
	    	TerritoryVertex tv=null;
	    	if(parentId!=null) tv=getNode(parentId,TerritoryVertex.class);
	    	
	    	return GraphUpdateResult.fromHandle(
	    		FloraOnDriver.this, Territory.newFromName(FloraOnDriver.this, name, shortName, tv).getID()
			);
	    }

	    public GraphUpdateResult createTerritory(String name,String shortName) throws ArangoException, FloraOnException {
	    	return createTerritory(name, shortName, null);
	    }

	    /**
	     * Creates a new taxonomic node bond to the given parent node. Ensures that this new node is taxonomically valid.
	     * This means that it must be of an inferior rank of its parent, and its name, in case it is below genus, must be fully qualified (i.e. not the epithets only)
	     * @param parent
	     * @param name
	     * @param author
	     * @param rank
	     * @param annotation
	     * @param current
	     * @throws FloraOnException
	     * @throws ArangoException
	     */
	    public ArangoKey createTaxEntChild(ArangoKey parent,String name,String author,TaxonRanks rank,String annotation,Boolean current) throws FloraOnException, ArangoException {
	    	TaxEnt par=this.getTaxEnt(parent);
	    	TaxEnt child=new TaxEnt(name, author, rank.getValue(), annotation, current);
	    	child.canBeChildOf(par.baseNode);
	    	//if(par.baseNode.getRankValue() >= rank.getValue()) throw new TaxonomyException("Rank must be lower than parent rank");
	    	
	    	child=TaxEnt.newFromTaxEnt(FloraOnDriver.this, child);
	    	child.setPART_OF(par.baseNode);
	    	return child.getArangoKey();
	    }

	    /**
	     * Create a new attribute node and add to DB.
	     * @param name
	     * @param shortName
	     * @param description
	     * @return A {@link GraphUpdateResult} with the new node.
	     * @throws ArangoException
	     */
	    public GraphUpdateResult createAttributeNode(String name,String shortName,String description) throws ArangoException {
	    	return GraphUpdateResult.fromHandle(
    			FloraOnDriver.this, new Attribute(FloraOnDriver.this,name,shortName,description).getID()
			);
	    }

	    /**
	     * Create a new character node and add to DB.
	     * @param name
	     * @param shortName
	     * @param description
	     * @return A {@link GraphUpdateResult} with the new node.
	     * @throws ArangoException
	     */
	    public GraphUpdateResult createCharacterNode(String name,String shortName,String description) throws ArangoException {
	    	return GraphUpdateResult.fromHandle(
    			FloraOnDriver.this, new Character(FloraOnDriver.this,name,shortName,description).getID()
			);
	    }

	    public GraphUpdateResult updateDocument(String handle,String key,Object value) throws ArangoException {
	    	HashMap<String, Object> newHashMap = new HashMap<String, Object>();
	    	newHashMap.put(key, value);
	    	driver.updateDocument(handle, newHashMap);
	    	return GraphUpdateResult.fromHandle(FloraOnDriver.this,handle);
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
	    	return findTaxEnt(TaxEnt.parse(q).baseNode);
	    }
	    
	    /**
		 * Gets only one node, or none, based on name and rank.
		 * NOTE: if rank is "norank", it is ignored.
	     * @param q
	     * @return
	     * @throws QueryException
	     * @throws ArangoException 
	     */
		public TaxEnt findTaxEnt(TaxEntVertex q) throws QueryException, ArangoException {
	// NOTE: this AQL query is slower than the code below!	
			/*String query=String.format("FOR v IN %1$s FILTER LOWER(v.name)=='%2$s' RETURN v",NodeTypes.taxent.toString(),q.name.trim().toLowerCase());
			TaxEntVertex tev=this.driver.executeAqlQuery(query, null, null, TaxEntVertex.class).getUniqueResult();
			if(tev==null)
				return null;
			else
				return new TaxEnt(FloraOnGraph.this,tev);*/
			
	    	if(q.getName()==null || q.getName().equals("")) throw new QueryException("Invalid blank name.");
	    	TaxEnt n;
	    	try {
	    		VertexCursor<TaxEntVertex> vc=driver.graphGetVertexCursor(Constants.TAXONOMICGRAPHNAME, TaxEntVertex.class, new TaxEntVertex(q.getName(),null,null,null,null,null), null, null);
	    		VertexEntity<TaxEntVertex> ve1=vc.getUniqueResult();

	    		if(ve1==null)	// node doesn't exist
	    			return null;
	    		else
	    			n=new TaxEnt(FloraOnDriver.this,ve1);

	    		if(q.getRankValue()==null || q.getRankValue().equals(TaxonRanks.NORANK.getValue()) || n.baseNode.getRankValue()==null) return n; else {
					if(!n.baseNode.getRankValue().equals(q.getRankValue())) return null; else return n;
				}	    		
	    	} catch (NonUniqueResultException e) {	// multiple nodes with this name. Search the one of the right rank
	    		VertexCursor<TaxEntVertex> vc=null;
				try {
					vc = driver.graphGetVertexCursor(Constants.TAXONOMICGRAPHNAME, TaxEntVertex.class, new TaxEntVertex(q.getName(),null,null,null), null, null);
				} catch (TaxonomyException e1) {
					// just go on, empty query
				}
				if(q.getRankValue()==null || q.getRankValue().equals(TaxonRanks.NORANK.getValue())) throw new QueryException("More than one node with name "+q.getName()+". You must disambiguate.");

				Iterator<VertexEntity<TaxEntVertex>> ns=vc.iterator();
				n=null;
				TaxEnt n1;
				while(ns.hasNext()) {
					//n1=ns.next().getEntity();
					n1=new TaxEnt(FloraOnDriver.this,ns.next());
					if(n1.baseNode.getRankValue().equals(q.getRankValue()) || n1.baseNode.getRankValue().equals(TaxonRanks.NORANK.getValue())) {
						if(n!=null) throw new QueryException("More than one node with name "+q.getName()+" and rank "+q.getRank().toString()); else n=n1;
					}
				}
				return n;
	    	} catch (TaxonomyException e) {
	    		return null;
			}
		}

		public String[] deleteTaxEntNode(TaxEntVertex nodename) throws QueryException, ArangoException {
	    	TaxEnt te=findTaxEnt(nodename);
	    	if(te!=null) return deleteNode(ArangoKey.fromString(te.getID()));
	    	return new String[0];
	    }
		
		/**
		 * Deletes the SYNONYM relationship that is linking 'from' with 'to'. The deleted relationship is the one immediately connected to 'to'. Note that this might have side effects (break other synonym links) in complex chains of synonyms.
		 * @param from
		 * @param to
		 * @throws ArangoException 
		 */
		public GraphUpdateResult detachSynonym(ArangoKey from,ArangoKey to) throws ArangoException {
			String query=String.format("LET e=FLATTEN("
				+ "FOR v in TRAVERSAL(%1$s,%2$s,'%3$s','any',{paths:true,filterVertices:[{_id:'%4$s'}],vertexFilterMethod:'exclude'}) RETURN v.path.edges) "
				+ "REMOVE e[LENGTH(e)-1] IN SYNONYM RETURN OLD"
				,NodeTypes.taxent.toString(),RelTypes.SYNONYM.toString(),from.toString(),to.toString());
			SYNONYM deleted=driver.executeAqlQuery(query, null, null, SYNONYM.class).getUniqueResult();
			return GraphUpdateResult.fromHandle(FloraOnDriver.this, deleted.getID());
		}
		
		public GraphUpdateResult updateTaxEntNode(TaxEnt node,String name,TaxonRanks rank,Boolean current,String author,String annotation) throws ArangoException, FloraOnException {
			node.setAnnotation(annotation);
			node.setName(name);
			if(rank!=null) node.setRank(rank.getValue());
			if(current!=null) node.setCurrent(current);
			node.setAuthor(author);
			node.commit();
			return GraphUpdateResult.fromHandle(FloraOnDriver.this, node.getID());
		}

		public GraphUpdateResult updateTerritoryNode(Territory node,String name,String shortName) throws ArangoException, FloraOnException {
			node.setName(name);
			node.setShortName(shortName);
			node.commit();
			return GraphUpdateResult.fromHandle(FloraOnDriver.this, node.getID());
		}

		/**
		 * Deletes one node and all connected edges
		 * @param id The document handle
		 * @return An array of the deleted document handles
		 * @throws ArangoException
		 */
		public String[] deleteNode(ArangoKey id) throws ArangoException {
			List<String> deleted=new ArrayList<String>();
			String tmp;
			String query=String.format("FOR e IN GRAPH_EDGES('%1$s','%2$s') RETURN e"
				,Constants.TAXONOMICGRAPHNAME,id);

			Iterator<String> vertexCursor=driver.executeAqlQuery(query, null, null, String.class).iterator();
			while(vertexCursor.hasNext()) {
				tmp=vertexCursor.next();
				driver.deleteDocument(tmp);
				deleted.add(tmp);
			}
			driver.deleteDocument(id.toString());
			deleted.add(id.toString());
			return deleted.toArray(new String[0]);
		}

		/**
		 * Deletes one node and all connected edges if the node is a leaf node.
		 * @param id
		 * @return
		 * @throws ArangoException
		 * @throws FloraOnException 
		 */
		public String[] deleteLeafNode(ArangoKey id) throws ArangoException, FloraOnException {
			List<String> deleted=new ArrayList<String>();
			String tmp;
			// TODO check for attributes!
			String query=String.format("FOR e IN GRAPH_EDGES('%1$s','%2$s',{direction:'inbound'}) COLLECT WITH COUNT INTO cou RETURN cou"
				,Constants.TAXONOMICGRAPHNAME,id);
			if(driver.executeAqlQuery(query, null, null, Integer.class).getUniqueResult()!=0) throw new FloraOnException("Node has children, inward synonyms or is parent of an hybrid");
			
			query=String.format("FOR e IN GRAPH_EDGES('%1$s','%2$s') RETURN e"
				,Constants.TAXONOMICGRAPHNAME,id);
			Iterator<String> vertexCursor=driver.executeAqlQuery(query, null, null, String.class).iterator();
			while(vertexCursor.hasNext()) {
				tmp=vertexCursor.next();
				driver.deleteDocument(tmp);
				deleted.add(tmp);
			}
			driver.deleteDocument(id.toString());
			deleted.add(id.toString());
			return deleted.toArray(new String[0]);
		}

	    public Attribute findAttribute(String name) throws ArangoException {
	    	String query="FOR v IN attribute FILTER v.name=='"+name+"' RETURN v";
			AttributeVertex vertexCursor=driver.executeAqlQuery(query, null, null, AttributeVertex.class).getUniqueResult();
			if(vertexCursor==null)
				return null;
			else
				return new Attribute(FloraOnDriver.this,vertexCursor);
	    }

		/**
		 * Gets the direct neighbors of the given vertex, off all facets.
		 * @param id The vertex's document handle
		 * @return A JSON string with an array of vertices ('nodes') and an array of edges ('links') of the form {nodes[],links:[]}
		 * @throws ArangoException
		 */
		public GraphUpdateResult getNeighbors(String id, Facets[] facets) {
			if(id==null) return GraphUpdateResult.emptyResult();
			RelTypes[] art=RelTypes.getRelTypesOfFacets(facets);
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
		
		/**
		 * Gets all morphological characters
		 * @return
		 */
		public GraphUpdateResult getAllCharacters() {
			String query=String.format("RETURN {nodes:(FOR v IN %1$s "
				+ "RETURN MERGE(v,{type:PARSE_IDENTIFIER(v._id).collection}))"
				+ ",links:[]}"
				,NodeTypes.character.toString()
			);
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

		public GraphUpdateResult getAllNodesOfType(NodeTypes nodeType) {
			String query=String.format("RETURN {nodes:(FOR v IN %1$s "
					+ "RETURN MERGE(v,{type:PARSE_IDENTIFIER(v._id).collection}))"
					+ ",links:[]}"
					,nodeType.toString()
				);
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
		
		public GeneralDBNode getNode(ArangoKey id) throws ArangoException {
			return driver.getDocument(id.toString(), GeneralDBNode.class).getEntity();
		}

		public <T extends GeneralDBNode> T getNode(ArangoKey id, Class<T> cls) throws ArangoException {
			return driver.getDocument(id.toString(), cls).getEntity();
		}

		public TaxEntVertex getTaxEntVertex(ArangoKey id) throws ArangoException {
			return driver.getDocument(id.toString(), TaxEntVertex.class).getEntity();
		}

		public TaxEnt getTaxEnt(ArangoKey id) throws ArangoException {
			return new TaxEnt(FloraOnDriver.this,getTaxEntVertex(id));
		}

		public TerritoryVertex getTerritoryFromShortName(String shortName) throws ArangoException {
			String query=String.format("FOR v IN %2$s FILTER v.shortName=='%1$s' RETURN v"
				, shortName, NodeTypes.territory.toString());
			return driver.executeAqlQuery(query, null, null, TerritoryVertex.class).getUniqueResult();
		}
		
		public GeneralNodeWrapperImpl getNodeWrapper(String id) {
			try {
				return new GeneralNodeWrapperImpl(FloraOnDriver.this,driver.getDocument(id, GeneralDBNodeImpl.class).getEntity());
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
			RelTypes[] art=RelTypes.getRelTypesOfFacets(facets);
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
					+ "RETURN {source:v,name:v2.vertex.name,annotation:v2.vertex.annotation,_id:v2.vertex._id,leaf:nedg==0,edges: (FOR ed IN v2.path.edges RETURN PARSE_IDENTIFIER(ed._id).collection)})) "
					+ "COLLECT k=o._id,n=(o.annotation==null ? o.name : CONCAT(o.name,' [',o.annotation,']')),l=o.leaf INTO gr RETURN {name:n, _id:k, leaf:l, match:UNIQUE(gr[*].o.source), reltypes:UNIQUE(FLATTEN(gr[*].o.edges))}"
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
				// TODO may this option should be removed? we don't want queries with ambiguous results (from matches of different collections)
				StringBuilder sb=new StringBuilder();
				sb.append("[");
				for(int i=0;i<collections.length-1;i++) {
					sb.append("'").append(collections[i]).append("',");
				}
				sb.append("'").append(collections[collections.length-1]).append("']");

				query=String.format("LET base=(FOR v IN GRAPH_VERTICES('%1$s',{},{vertexCollectionRestriction:%3$s}) FILTER "+filter+" RETURN v._id) "
					+ "FOR o IN FLATTEN(FOR v IN base FOR v1 IN GRAPH_TRAVERSAL('%1$s',v,'inbound',{paths:true,filterVertices:[{isSpeciesOrInf:true}],vertexFilterMethod:['exclude']}) "
					+ "RETURN FLATTEN(FOR v2 IN v1[*] LET nedg=LENGTH(FOR e IN PART_OF FILTER e._to==v2.vertex._id RETURN e)"+leaf+" "
					+ "RETURN {source:v,name:v2.vertex.name,annotation:v2.vertex.annotation,_id:v2.vertex._id,leaf:nedg==0,edges: (FOR ed IN v2.path.edges RETURN PARSE_IDENTIFIER(ed._id).collection)})) "
					+ "COLLECT k=o._id,n=(o.annotation==null ? o.name : CONCAT(o.name,' [',o.annotation,']')),l=o.leaf INTO gr RETURN {name:n,_id:k,leaf:l,match:UNIQUE(gr[*].o.source),reltypes:UNIQUE(FLATTEN(gr[*].o.edges))}"
					,Constants.TAXONOMICGRAPHNAME,q,sb.toString());
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
	    
	    public List<String> validateTaxonomy() {
	    	/* TODO:
	    	 * - in a synonym chain, only one node can be current
	    	 * - PART_OF links always connect nodes of the same type
	    	 */
	    	List<String> out=new ArrayList<String>();
	    	return out;
	    }
	    /**
	     * Execute a text query on the starting nodes going upwards. Good idea, but it's actually slower!
	     * @param startingVertices
	     * @param q
	     * @param exact
	     * @return
	     * @throws ArangoException
	     */
/*	    @Deprecated
	    public List<SimpleTaxonResult> inverseSpeciesTextQuery(List<SimpleTaxonResult> startingVertices,String q,boolean exact) throws ArangoException {
	    	String[] handles=new String[startingVertices.size()];
	    	for(int i=0;i<startingVertices.size();i++) {
	    		//handles[i]="taxent/"+startingVertices.get(i).getId();
	    	}
	    	String query="FOR o IN (FOR v IN "+EntityFactory.toJsonString(handles)+" "
				+ "LET p=GRAPH_TRAVERSAL('taxgraph',v,'outbound',{paths:true,filterVertices:'flora::testCode'})[0] LET vv=p[*].path.vertices RETURN "
				+ "{n:DOCUMENT(v),p: (FOR fi IN UNIQUE(FLATTEN(vv[*][* RETURN {n:CURRENT.name,k:CURRENT._id}])) FILTER LIKE(fi.n,'%"+q+"%',true)"
				+ " RETURN {n:fi.n,k:fi.k}) }) FILTER LENGTH(o.p)>0 RETURN {name:o.n.name,_key:o.n._key,match:o.p[*].k}";
	    	System.out.println("INVERSE QUERY\n"+query);
	    	CursorResult<SimpleTaxonResult> vertexCursor=driver.executeAqlQuery(query, null, null, SimpleTaxonResult.class);
	    	return vertexCursor.asList();
	    }*/
	    
	    /**
	     * Gets all territories.
	     * @return
	     * @throws ArangoException
	     */
	    public CursorResult<TerritoryVertex> getAllTerritories(boolean onlyLeafNodes) throws ArangoException {
	    	String query;
	    	if(onlyLeafNodes)
	    		query=String.format("FOR v IN %1$s FILTER LENGTH(FOR e IN PART_OF FILTER e._to==v._id RETURN e)==0 SORT v.name RETURN v",NodeTypes.territory.toString());
	    	else
	    		query=String.format("FOR v IN %1$s SORT v.name RETURN v",NodeTypes.territory.toString());
	    	return driver.executeAqlQuery(query, null, null, TerritoryVertex.class);
	    }

	    /**
	     * Gets a list of suggested names similar to the query
	     * @param query
	     * @return
	     * @throws ArangoException
	     */
	    public CursorResult<SimpleNameResult> findSuggestions(String query, Integer limit) throws ArangoException {
	    	String limitQ;
	    	if(limit!=null) limitQ=" LIMIT "+limit; else limitQ="";
	    	String _query=String.format("FOR v IN taxent FILTER LIKE(v.name,'%1$s%%',true) SORT v.rank DESC"+limitQ+" RETURN v",query);
	    	return driver.executeAqlQuery(_query, null, null, SimpleNameResult.class);
	    	// TODO levenshtein, etc.
	    }
	}
	
	public final class SpecificQueries {
		/**
		 * Gets all the native status that this taxon has, i.e., all the associations between a {@link TerritoryVertex} and a {@link EXISTS_IN} relationship.
		 * @param id
		 * @return
		 * @throws ArangoException
		 */
		public CursorResult<NativeStatusResult> getTaxonNativeStatus(ArangoKey id) throws ArangoException {
			String query=String.format("LET terr=TRAVERSAL(taxent, EXISTS_IN, '%1$s', 'outbound', {maxDepth:1,paths:true}) "
				+ "FOR v IN SLICE(terr,1) RETURN {territory: v.vertex, nativeStatus:v.path.edges[0]}", id.toString());
			return driver.executeAqlQuery(query, null, null, NativeStatusResult.class);
		}
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
		 * Gets the immediate children of the given node
		 * @param id
		 * @return
		 * @throws ArangoException 
		 */
		public CursorResult<TaxEntVertex> getChildren(ArangoKey id) throws ArangoException {
			String query=String.format("FOR v IN NEIGHBORS(%1$s, %2$s, '%3$s', 'inbound') LET v1=DOCUMENT(v) SORT v1.name RETURN v1"
				,NodeTypes.taxent.toString(),RelTypes.PART_OF.toString(),id.toString());
		    return driver.executeAqlQuery(query, null, null, TaxEntVertex.class);
		}
		
		/**
		 * Gets all the taxent nodes of the given rank
		 * @param rank
		 * @return
		 * @throws ArangoException
		 */
		public Iterator<TaxEntVertex> getAllOfRank(TaxonRanks rank) throws ArangoException {
			String query=String.format("FOR v IN %1$s FILTER v.rank==%2$d SORT v.name RETURN v"
					,NodeTypes.taxent.toString(),rank.getValue());
	    	CursorResult<TaxEntVertex> vertexCursor=driver.executeAqlQuery(query, null, null, TaxEntVertex.class);
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
	    	return new SpeciesList(FloraOnDriver.this,vertex);
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
				+ "RETURN NEIGHBORS(specieslist,OBSERVED_IN,sl,'inbound',{},{includeData:true})[* RETURN {name:CURRENT.name,_id:CURRENT._id}]) "
				+ "COLLECT b=a WITH COUNT INTO c SORT c DESC RETURN {name:b.name,_id:b._id,count:c}"
				,NodeTypes.specieslist.toString()
				,latitude,longitude,distance
			);
			//System.out.println(query);
	    	CursorResult<SimpleTaxonResult> vertexCursor=driver.executeAqlQuery(query, null, null, SimpleTaxonResult.class);
	    	vertexCursor.asList();
	    	return vertexCursor.iterator();
		}
	
		/**
		 * Gets all species or inferior ranks, optionally filtered by those that exist in the given territory.
		 * Note that when onlyLeafNodes is true and territory is not null, some taxa may be omitted from the list,
		 * namely those which have inferior taxa but are bond to a territory (and not the inferior taxa). 
		 * @param onlyLeafNodes true to return only the terminal nodes.
		 * @return An Iterator of any class that extends SimpleNameResult
		 * @territory The territory to filter taxa, or null if no filter is wanted.
		 * @throws ArangoException
		 */
		public <T extends SimpleNameResult> Iterator<T> getAllSpeciesOrInferior(boolean onlyLeafNodes, Class<T> T, String territory) throws ArangoException {
			String query;
			if(territory==null) {
				query=String.format("FOR v IN %2$s "
					+ "LET npar=LENGTH(FOR e IN PART_OF FILTER e._to==v._id RETURN e) FILTER v.isSpeciesOrInf==true "
					+ "%1$s LET terr=TRAVERSAL(%2$s, EXISTS_IN, v, 'outbound', {maxDepth:1,paths:true}) SORT v.name RETURN {_id:v._id,name:v.name,author:v.author,leaf:npar==0, current:v.current"
					+ ", territories:(LET d=SLICE(terr,1) RETURN ZIP(d[*].vertex.shortName, d[*].path.edges[0].nativeStatus))[0]}"	//DOCUMENT(terr)[*].shortName
					, onlyLeafNodes ? "&& npar==0" : "", NodeTypes.taxent.toString());
			} else {
				if(onlyLeafNodes) System.out.println("Warning: possibly omitting taxa from the checklist.");
				query=String.format(
					"FOR t IN territory FILTER t.shortName=='%3$s' FOR v IN (FOR v1 IN NEIGHBORS(territory, EXISTS_IN, t, 'inbound') RETURN DOCUMENT(v1)) "
					+ "LET npar=LENGTH(FOR e IN PART_OF FILTER e._to==v._id RETURN e) "
					+ "%1$s LET terr=TRAVERSAL(%2$s, EXISTS_IN, v, 'outbound', {maxDepth:1,paths:true}) SORT v.name RETURN {_id:v._id,name:v.name,author:v.author,leaf:npar==0, current:v.current"
					+ ", territories:(LET d=SLICE(terr,1) RETURN ZIP(d[*].vertex.shortName, d[*].path.edges[0].nativeStatus))[0]}"	//DOCUMENT(terr)[*].shortName
					, onlyLeafNodes ? " FILTER npar==0" : "", NodeTypes.taxent.toString(), territory);
/*					"FOR t IN territory FILTER t.shortName=='%3$s' FOR v IN FLATTEN(FOR v1 IN GRAPH_TRAVERSAL('taxgraph', t, 'inbound', {filterVertices: [{isSpeciesOrInf: true}], vertexFilterMethod:'exclude'}) RETURN v1[*].vertex) "
					+ "LET npar=LENGTH(FOR e IN PART_OF FILTER e._to==v._id RETURN e) "
					+ "%1$s LET terr=TRAVERSAL(%2$s, EXISTS_IN, v, 'outbound', {maxDepth:1,paths:true}) SORT v.name RETURN {_id:v._id,name:v.name,author:v.author,leaf:npar==0, current:v.current"
					+ ", territories:(LET d=SLICE(terr,1) RETURN ZIP(d[*].vertex.shortName, d[*].path.edges[0].nativeStatus))[0]}"	//DOCUMENT(terr)[*].shortName
					, onlyLeafNodes ? " FILTER npar==0" : "", NodeTypes.taxent.toString(), territory);*/
			}
			//System.out.println(query);
	    	CursorResult<T> vertexCursor=driver.executeAqlQuery(query, null, null, T);
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
					+ "RETURN {match:sl._id,name:n.name,_id:n._id}) "
					+ "COLLECT k=o._id,n=o.name INTO gr LET ma=gr[*].o.match RETURN {name:n,_id:k,match:ma,count:LENGTH(ma),reltypes:['%5$s']}"
					,NodeTypes.specieslist.toString(),latitude,longitude,distance,RelTypes.OBSERVED_IN.toString());
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
				,NodeTypes.specieslist.toString(),RelTypes.OBSERVED_IN.toString(),RelTypes.OBSERVED_BY.toString()
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
				return new SpeciesList(FloraOnDriver.this,vertexCursor);
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
			,RelTypes.OBSERVED_IN.toString(),RelTypes.OBSERVED_BY.toString(),NodeTypes.specieslist.toString());
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
					,Constants.TAXONOMICGRAPHNAME,query,NodeTypes.taxent.toString(),RelTypes.OBSERVED_IN.toString()
					,RelTypes.OBSERVED_BY.toString(),NodeTypes.specieslist.toString(),collections[0]);
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
					,Constants.TAXONOMICGRAPHNAME,query,NodeTypes.taxent.toString(),RelTypes.OBSERVED_IN.toString()
					,RelTypes.OBSERVED_BY.toString(),NodeTypes.specieslist.toString(),EntityFactory.toJsonString(collections));
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
			,Constants.TAXONOMICGRAPHNAME,query,NodeTypes.taxent.toString(),RelTypes.OBSERVED_IN.toString()
			,RelTypes.OBSERVED_BY.toString(),NodeTypes.specieslist.toString(),EntityFactory.toJsonString(collections));
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
    	String aqlQuery="FOR oi IN OBSERVED_IN LET sl=(FOR sl IN specieslist FILTER oi._to==sl._id RETURN sl)[0] RETURN MERGE("
    			+ "sl,oi,(FOR tx IN taxent FILTER oi._from==tx._id RETURN tx)[0]"
    			+ ",{observers:(FOR ob IN OBSERVED_BY FILTER ob._from==sl._id RETURN (FOR au IN author FILTER au._id==ob._to SORT ob.main RETURN au.name)[0])}"
    			+ ",{inventoryKey:sl._key})";

/*
    	String aqlQuery=String.format("FOR v2 IN %1$s "
			+ "LET nei=EDGES(%2$s,v2,'inbound') FILTER LENGTH(nei)>0 "
			+ "LET mainaut=DOCUMENT(NEIGHBORS(%1$s,%3$s,v2,'outbound',{main:true})) "
			+ "LET aut=DOCUMENT(NEIGHBORS(%1$s,%3$s,v2,'outbound',{main:false})) "
			+ "FOR n IN nei RETURN {name:DOCUMENT(n._from).name,confidence:n.confidence,weight:n.weight,phenoState:n.phenoState,wild:n.wild"
			+ ",uuid:n.uuid,dateInserted:n.dateInserted,inventory:v2._key,location:v2.location,observers:APPEND(mainaut[*].name,aut[*].name)}"
			,NodeTypes.specieslist.toString(),AllRelTypes.OBSERVED_IN.toString(),AllRelTypes.OBSERVED_BY.toString());*/
    	CursorResult<Occurrence> vertexCursor=this.driver.executeAqlQuery(aqlQuery, null, null, Occurrence.class);
    	//System.out.println(aqlQuery);
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
	     * @throws FloraOnException 
	     * @throws ArangoException 
	     * @throws TaxonomyException
	     * @throws QueryException
	     */
		public Map<String,Integer> uploadTaxonomyListFromStream(InputStream stream,boolean simulate) throws IOException, ArangoException, FloraOnException {
	    	Integer nnodes=0,nrels=0,nrecs=0;
	    	Reader freader;

			freader = new InputStreamReader(stream, StandardCharsets.UTF_8);
			Iterator<CSVRecord> records = CSVFormat.EXCEL.withDelimiter('\t').withQuote('"').parse(freader).iterator();
			CSVRecord record=records.next();

			Integer nRankColumns=null;
			Integer oldIdColumn=null;
			String col;
			Map<Integer,Territory> territories=new HashMap<Integer,Territory>();
			
			for(int i=0;i<record.size();i++) {
				col=record.get(i);
				if(col.equals("id")) {
					if(nRankColumns==null) nRankColumns=i;
					oldIdColumn=i;
				}
				if(col.startsWith("territory:")) {	// create the territories
					if(nRankColumns==null) nRankColumns=i;
					String shortName=col.substring(col.indexOf(":")+1);
					Territory tv = Territory.newFromName(FloraOnDriver.this, shortName, shortName, (ArangoKey)null);
					System.out.println("Added territory: "+shortName);
					territories.put(i, tv);
				}
			}
			if(nRankColumns==null) nRankColumns=record.size();
			
			String[] rankNames=new String[nRankColumns];
			for(int i=0;i<nRankColumns;i++) rankNames[i]=record.get(i);

			System.out.print("Reading file ");
			try {
				TaxEnt curTaxEnt,parentNode;
				TaxEnt parsedName;
				boolean pastspecies;	// true after the column species has passed (so to know when to append names to genus)
				while(records.hasNext()) {
					record=records.next();
					nrecs++;
					if(nrecs % 100==0) {System.out.print(".");System.out.flush();}
					if(nrecs % 1000==0) {System.out.print(nrecs);System.out.flush();}
					parentNode=null;
					curTaxEnt=null;
					pastspecies=false;
					for(int i=0;i<rankNames.length;i++) {
						try {
							parsedName=TaxEnt.parse(record.get(i));
						} catch (TaxonomyException e) {
							// is it an empty cell? skip. 
							continue;
						}
						if(rankNames[i].equals("species")) pastspecies=true;
						parsedName.setCurrent(true);
						// special cases: if species or lower rank, must prepend genus.
						if(pastspecies) parsedName.setName(parentNode.baseNode.getName()+" "+(rankNames[i].equals("species") ? "" : (infraRanks.containsKey(rankNames[i]) ? infraRanks.get(rankNames[i]) : rankNames[i])+" ")+parsedName.baseNode.getName());
						
						parsedName.setRank(TaxonRanks.valueOf(rankNames[i].toUpperCase()).getValue());
						if(pastspecies && parsedName.baseNode.getAuthor()==null) parsedName.setAuthor(parentNode.baseNode.getAuthor());
						//System.out.println(parsedname.name);
						curTaxEnt=dbNodeWorker.findTaxEnt(parsedName.baseNode);
						
						if(curTaxEnt==null) {	// if node does not exist, add it.
							curTaxEnt=TaxEnt.newFromTaxEnt(FloraOnDriver.this,parsedName);
							//System.out.println("ADD "+parsedName.name);System.out.flush();
							nnodes++;
						} else {	// if it exists, update its rank and authority.
							//System.out.println("EXISTS "+parsedName.baseNode.getFullName());System.out.flush();
							curTaxEnt.setRank(parsedName.baseNode.getRankValue());
							if(parsedName.baseNode.getAuthor()!=null) curTaxEnt.setAuthor(parsedName.baseNode.getAuthor());
							curTaxEnt.commit();
						}
						if(parentNode!=null) {	// create PART_OF relationship to previous column
							nrels+=curTaxEnt.setPART_OF(parentNode.baseNode);
						}
						parentNode=curTaxEnt;
					}
 
					if(oldIdColumn != null) {
						curTaxEnt.setOldId(Integer.parseInt(record.get(oldIdColumn)));
						curTaxEnt.commit();
					}
					
					for(Entry<Integer,Territory> terr : territories.entrySet()) {	// bind this taxon with the territories with the given native status
						String ns=record.get(terr.getKey());
						if(ns!=null && !ns.equals(""))
							terr.getValue().setTaxEntNativeStatus(curTaxEnt.getArangoKey(), NativeStatus.valueOf(ns.toUpperCase()));
					}
				}
			} catch (FloraOnException | ArangoException e) {
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

		public Map<String,Integer> uploadTaxonomyListFromFile(String file,boolean simulate) throws IOException, ArangoException, FloraOnException {
	    	File tl=new File(file);
	    	if(!tl.canRead()) throw new IOException("Cannot read input file "+file);
	    	return uploadTaxonomyListFromStream(new FileInputStream(file),simulate);
	    }
	    
		public String uploadImagesFromStream(InputStream stream) throws FloraOnException, IOException {
			Reader freader=null;
			freader = new InputStreamReader(stream, StandardCharsets.UTF_8);
			Iterable<CSVRecord> records = CSVFormat.MYSQL.parse(freader);
			Image img;
			for(CSVRecord record : records) {
				img=new Image(FloraOnDriver.this,record);
			}
			return null;
		}
		
	    public String uploadRecordsFromFile(String filename) throws IOException, FloraOnException {
	    	File file=new File(filename);
	    	if(!file.canRead()) throw new IOException("Cannot read file "+filename);
	    	return uploadRecordsFromStream(new FileInputStream(file));
	    }
	    
		public String uploadRecordsFromStream(InputStream stream) throws FloraOnException, IOException {
	    	StringBuilder out=new StringBuilder();
	    	Reader freader=null;
	    	int countupd=0,countnew=0,counterr=0,nrecs=0;
	    	int newsplist=0;
	    	long counter=0;
	    	List<String[]> lineerrors=new ArrayList<String[]>();
	    	System.out.print("Reading records ");

	    	Occurrence occ;
    		try {
    			freader = new InputStreamReader(stream, StandardCharsets.UTF_8);
    			Iterable<CSVRecord> records = CSVFormat.MYSQL.parse(freader);
    			for (CSVRecord record : records) {
    				try {
    					occ=Occurrence.fromCSVline(record);
    					nrecs++;
    					if(nrecs % 100==0) {System.out.print(".");System.out.flush();}
    					if(nrecs % 1000==0) {System.out.print(nrecs);System.out.flush();}
    					
    					occ.commit(FloraOnDriver.this);
    				} catch(FloraOnException e) {
						lineerrors.add(new String[] {((Long)record.getRecordNumber()).toString(),e.getMessage()});
						counterr++;
						continue;
    				}
    				
    			    counter++;
    			    if((counter % 2500)==0) {
    			    	System.out.println(counter+" records processed.");
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

	    	//if(abort) throw new FloraOnException(counterr+" errors found on lines "+lineerrors.toString());
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
					new Author(FloraOnDriver.this
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
	    	TaxEnt fullname1;
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
				colnames.add(new Character(FloraOnDriver.this,characters.next().getKey(),null,null));
			}
			
			CSVRecord record;
			String[] attrs;
			int count=0;
			while(records.hasNext()) {
				record=records.next();
				count++;
				if(count % 100==0) {System.out.print(".");System.out.flush();}
				if(count % 1000==0) {System.out.print(count);System.out.flush();}
				try {
					fullname1=TaxEnt.parse(record.get("taxon"));
					n1=dbNodeWorker.findTaxEnt(fullname1.baseNode);
					if(n1==null) throw new QueryException(fullname1.baseNode.getFullName() +" not found.");
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
									an=new Attribute(FloraOnDriver.this,attr,null,null);
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
		
		public void addSpeciesLists(JsonObject sl) throws FloraOnException, ArangoException {
			JsonArray arr=new JsonArray();
			arr.add(sl);
			addSpeciesLists(arr);
		}
		public void addSpeciesLists(JsonArray sls) throws FloraOnException, ArangoException {
			for(int i=0; i<sls.size(); i++)
				new SpeciesList(FloraOnDriver.this,sls.get(i).getAsJsonObject());
		}
	}
}
