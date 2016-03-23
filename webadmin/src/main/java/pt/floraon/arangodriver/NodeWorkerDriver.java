package pt.floraon.arangodriver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.arangodb.ArangoDriver;
import com.arangodb.ArangoException;
import com.arangodb.NonUniqueResultException;
import com.arangodb.VertexCursor;
import com.arangodb.entity.EntityFactory;
import com.arangodb.entity.marker.VertexEntity;

import pt.floraon.driver.Constants;
import pt.floraon.driver.DatabaseException;
import pt.floraon.driver.FloraOnException;
import pt.floraon.driver.FloraOn;
import pt.floraon.driver.GNodeWorker;
import pt.floraon.driver.INodeKey;
import pt.floraon.driver.INodeWorker;
import pt.floraon.driver.QueryException;
import pt.floraon.driver.TaxonomyException;
import pt.floraon.driver.Constants.Facets;
import pt.floraon.driver.Constants.NodeTypes;
import pt.floraon.driver.Constants.RelTypes;
import pt.floraon.driver.Constants.TaxonRanks;
import pt.floraon.driver.Constants.TerritoryTypes;
import pt.floraon.entities.Attribute;
import pt.floraon.entities.Author;
import pt.floraon.entities.Character;
import pt.floraon.entities.GeneralDBNode;
import pt.floraon.entities.SYNONYM;
import pt.floraon.entities.SpeciesList;
import pt.floraon.entities.TaxEnt;
import pt.floraon.entities.Territory;
import pt.floraon.results.GraphUpdateResult;
import pt.floraon.results.NativeStatusResult;

public class NodeWorkerDriver extends GNodeWorker implements INodeWorker {
	protected ArangoDriver dbDriver;
	
	public NodeWorkerDriver(FloraOn driver) {
		super(driver);
		this.dbDriver=(ArangoDriver) driver.getArangoDriver();
	}
	
	@Override
	public TaxEnt createTaxEntFromName(String name,String author,TaxonRanks rank,String sensu, String annotation,Boolean current) throws TaxonomyException, FloraOnException {
		TaxEnt out=new TaxEnt(name, rank == null ? null : rank.getValue(), author, sensu, annotation, current, null);
		try {
			VertexEntity<TaxEnt> ve=dbDriver.graphCreateVertex(Constants.TAXONOMICGRAPHNAME, NodeTypes.taxent.toString(), out, false);
			out=ve.getEntity();
			out.setID(ve.getDocumentHandle());
			out.setKey(ve.getDocumentKey());
			return out;
		} catch (ArangoException e) {
			throw new DatabaseException(e.getErrorMessage());
		}
	}
	
	@Override
	public Territory createTerritory(String name, String shortName, TerritoryTypes type, String theme, boolean showInChecklist, INodeKey parent) throws FloraOnException {
		Territory out=new Territory(name, shortName, type, theme, showInChecklist);
		try {
			VertexEntity<Territory> ve=dbDriver.graphCreateVertex(Constants.TAXONOMICGRAPHNAME, NodeTypes.territory.toString(), out, false);
			out = ve.getEntity();
			out.setID(ve.getDocumentHandle());
			out.setKey(ve.getDocumentKey());
			if(parent!=null) driver.wrapNode(driver.asNodeKey(out.getID())).setPART_OF(parent);
		} catch (ArangoException e) {
			throw new DatabaseException(e.getErrorMessage());
		}
		
		return out;
	}

	@Override
	public TaxEnt getTaxEntById(INodeKey id) throws FloraOnException {
		try {
			return dbDriver.getDocument(id.toString(), TaxEnt.class).getEntity();
		} catch (ArangoException e) {
			throw new DatabaseException(e.getErrorMessage());
		}
	}

	/**
	 * Fetches one author with the given idAut
	 * @param idaut
	 * @return Null if not found, otherwise an {@link Author}
	 */
	@Override
	public Author getAuthorById(int idaut) {
		String query="RETURN GRAPH_VERTICES('taxgraph',{idAut:"+idaut+"})[0]";
		Author vertexCursor=null;
		try {
			vertexCursor = dbDriver.executeAqlQuery(query, null, null, Author.class).getUniqueResult();
		} catch (ArangoException e) {
			return null;
		}
		if(vertexCursor==null)
			return null;
		else
			return vertexCursor;
	}

	@Override
	public Author createAuthor(Author author) throws FloraOnException {
		try {
			return dbDriver.graphCreateVertex(Constants.TAXONOMICGRAPHNAME, NodeTypes.author.toString(), author, false).getEntity();
		} catch (ArangoException e) {
			throw new DatabaseException(e.getErrorMessage());
		}
	}
	
	@Override
	public SpeciesList createSpeciesList(SpeciesList sl) throws FloraOnException {
		try {
			return dbDriver.graphCreateVertex(Constants.TAXONOMICGRAPHNAME, NodeTypes.specieslist.toString(), sl, false).getEntity();
		} catch (ArangoException e) {
			throw new DatabaseException(e.getErrorMessage());
		}
	}
	
	/**
	 * Fetches one {@link TaxEnt} with the given idEnt
	 * @param oldId Legacy ID (when importing from other DB)
	 * @return
	 */
	@Override
	public TaxEnt getTaxEntByOldId(int oldId) {
		String query="FOR v IN taxent FILTER v.oldId=="+oldId+" RETURN v";
		TaxEnt vertexCursor=null;
		try {
			vertexCursor = dbDriver.executeAqlQuery(query, null, null, TaxEnt.class).getUniqueResult();
		} catch (ArangoException e) {
			System.err.println("More than one taxon with this ID?!");
			return null;
		}
		if(vertexCursor==null)
			return null;
		else
			return vertexCursor;
	}
	
	@Override
	public String[] deleteNode(INodeKey id) throws FloraOnException {
		List<String> deleted=new ArrayList<String>();
		String tmp;
		String query=String.format("FOR e IN GRAPH_EDGES('%1$s','%2$s') RETURN e"
			,Constants.TAXONOMICGRAPHNAME,id);

		try {
			Iterator<String> vertexCursor=dbDriver.executeAqlQuery(query, null, null, String.class).iterator();
			while(vertexCursor.hasNext()) {
				tmp=vertexCursor.next();
				dbDriver.deleteDocument(tmp);
				deleted.add(tmp);
			}
			dbDriver.deleteDocument(id.toString());
			deleted.add(id.toString());
		} catch (ArangoException e) {
			throw new DatabaseException(e.getErrorMessage());
		}
		return deleted.toArray(new String[0]);
	}
	
	@Override
	public String[] deleteLeafNode(INodeKey id) throws FloraOnException {
		List<String> deleted=new ArrayList<String>();
		String tmp;
		// TODO check for attributes upon delete!
		String query=String.format("FOR e IN GRAPH_EDGES('%1$s','%2$s',{direction:'inbound'}) COLLECT WITH COUNT INTO cou RETURN cou"
			,Constants.TAXONOMICGRAPHNAME,id);
		
		try {
			if(dbDriver.executeAqlQuery(query, null, null, Integer.class).getUniqueResult()!=0) throw new FloraOnException("Node has children, inward synonyms or is parent of an hybrid");
		
			query=String.format("FOR e IN GRAPH_EDGES('%1$s','%2$s') RETURN e"
				,Constants.TAXONOMICGRAPHNAME,id);
			Iterator<String> vertexCursor=dbDriver.executeAqlQuery(query, null, null, String.class).iterator();
			while(vertexCursor.hasNext()) {
				tmp=vertexCursor.next();
				dbDriver.deleteDocument(tmp);
				deleted.add(tmp);
			}
			dbDriver.deleteDocument(id.toString());
			deleted.add(id.toString());
		} catch (ArangoException e) {
			throw new DatabaseException(e.getErrorMessage());
		}
		return deleted.toArray(new String[0]);
	}

	@Override
	public GeneralDBNode getNode(INodeKey id) throws FloraOnException {
		try {
			return dbDriver.getDocument(id.toString(), GeneralDBNode.class).getEntity();
		} catch (ArangoException e) {
			throw new DatabaseException(e.getErrorMessage());
		}
	}

	@Override
	public <T extends GeneralDBNode> T getNode(INodeKey id, Class<T> cls) throws FloraOnException {
		try {
			return dbDriver.getDocument(id.toString(), cls).getEntity();
		} catch (ArangoException e) {
			throw new DatabaseException(e.getErrorMessage());
		}
	}
	
	@Override
	public GraphUpdateResult detachSynonym(INodeKey from,INodeKey to) throws FloraOnException {
		String query=String.format("LET e=FLATTEN("
			+ "FOR v in TRAVERSAL(%1$s,%2$s,'%3$s','any',{paths:true,filterVertices:[{_id:'%4$s'}],vertexFilterMethod:'exclude'}) RETURN v.path.edges) "
			//+ "LET e1=e[LENGTH(e)-1] LET rem=[e1, (FOR e2 IN SYNONYM FILTER e2._to==e1._from && e2._from==e1._to RETURN e2)[0]]"
			//+ "FOR r IN rem REMOVE r IN SYNONYM RETURN OLD"
			+ "LET e1=e[LENGTH(e)-1] "
			+ "REMOVE e1 IN SYNONYM RETURN OLD"
			,NodeTypes.taxent.toString(),RelTypes.SYNONYM.toString(),from.toString(),to.toString());
		//System.out.println(query);
		List<SYNONYM> deleted;
		try {
			deleted = dbDriver.executeAqlQuery(query, null, null, SYNONYM.class).asList();
		} catch (ArangoException e) {
			throw new DatabaseException(e.getErrorMessage());
		}
		if(deleted.size()==1)
			return new GraphUpdateResult(driver, deleted.get(0).getID());
		else
			return new GraphUpdateResult(driver, new String[] {deleted.get(0).getID().toString(), deleted.get(1).getID().toString()});
	}

	@Override
    public GraphUpdateResult updateDocument(INodeKey id,String key,Object value) throws FloraOnException {
    	HashMap<String, Object> newHashMap = new HashMap<String, Object>();
    	newHashMap.put(key, value);
    	try {
			dbDriver.updateDocument(id.toString(), newHashMap);
		} catch (ArangoException e) {
			throw new DatabaseException(e.getErrorMessage());
		}
    	return new GraphUpdateResult(driver,id);
    }

	@Override
	public GraphUpdateResult updateTaxEntNode(INodeKey node,TaxEnt newTaxEnt, boolean replace) throws FloraOnException {
		if(replace && newTaxEnt.getRank()==null) throw new TaxonomyException("Taxon must have a rank");
    	try {
			dbDriver.graphUpdateVertex(Constants.TAXONOMICGRAPHNAME, NodeTypes.taxent.toString(), node.getDBKey(), newTaxEnt, !replace);
		} catch (ArangoException e) {
			throw new DatabaseException(e.getErrorMessage());
		}
		return new GraphUpdateResult(driver, node.getID());
	}
/*    public GraphUpdateResult updateTaxEntNode(TaxEnt node,String name,TaxonRanks rank,Boolean current,String author,String annotation) throws FloraOnException {
    	node.update(name, rank.getValue(), author, annotation, current);
    	try {
			dbDriver.graphUpdateVertex(Constants.TAXONOMICGRAPHNAME, NodeTypes.taxent.toString(), driver.asNodeKey(node.getID()).getDBKey(), node, false);
		} catch (ArangoException e) {
			throw new DatabaseException(e.getErrorMessage());
		}
		return new GraphUpdateResult(driver, node.getID());
	}*/

	@Override
    public GraphUpdateResult updateTerritoryNode(Territory node,String name,String shortName, TerritoryTypes type, String theme, boolean showInChecklist) throws FloraOnException {
		//Territory node = new Territory(FloraOnDriver.this, dbNodeWorker.getNode(id, TerritoryVertex.class));
    	node.update(name, shortName, type, theme, showInChecklist);
    	try {
    		dbDriver.updateDocument(node.getID(), node, false);
			//dbDriver.graphUpdateVertex(Constants.TAXONOMICGRAPHNAME, NodeTypes.territory.toString(), driver.asNodeKey(node.getID()).getDBKey(), node, false);
		} catch (ArangoException e) {
			throw new DatabaseException(e.getErrorMessage());
		}
		return new GraphUpdateResult(driver, node.getID());
	}

	@Override
	public GraphUpdateResult getRelationshipsBetween(String[] id, Facets[] facets) {
		RelTypes[] art=RelTypes.getRelTypesOfFacets(facets);
    	String rt=Arrays.toString(art);
    	rt=rt.substring(1, rt.length()-1);

		String query=String.format("RETURN {nodes:(FOR n IN %2$s "
			+ "LET v=DOCUMENT(n) RETURN MERGE(v,{type:PARSE_IDENTIFIER(v._id).collection}))"//{id:v._id,r:v.rank,t:PARSE_IDENTIFIER(v._id).collection,n:v.name,c:v.current})"
			+ ",links:("
			//+ "FOR n IN GRAPH_EDGES('%1$s',%2$s,{edgeCollectionRestriction:%3$s}) "
			//+ "LET d=DOCUMENT(n) FILTER d._from IN %2$s && d._to IN %2$s"
			//+ "RETURN MERGE(d,{type:PARSE_IDENTIFIER(d).collection}))}"
			+ "FOR start IN %2$s FOR v,e IN 1..1 ANY start %3$s FILTER e._from IN %2$s && e._to IN %2$s"
			+ "RETURN MERGE(e,{type:PARSE_IDENTIFIER(e).collection}))}"
			,Constants.TAXONOMICGRAPHNAME,EntityFactory.toJsonString(id),rt
			//,Constants.TAXONOMICGRAPHNAME,EntityFactory.toJsonString(id),EntityFactory.toJsonString(art)
		);
		String res=null;
		System.out.println(query);
		try {
			res = dbDriver.executeAqlQueryJSON(query, null, null);
		} catch (ArangoException e) {
			e.printStackTrace();
		}
		// NOTE: server responses are always an array, but here we always have one element, so we remove the []
		return (res==null || res.equals("[]")) ? GraphUpdateResult.emptyResult() : new GraphUpdateResult(res.substring(1, res.length()-1));
	}

	@Override
	public GraphUpdateResult getNeighbors(INodeKey id, Facets[] facets, Integer depth) {
		if(id==null) return GraphUpdateResult.emptyResult();
		RelTypes[] art=RelTypes.getRelTypesOfFacets(facets);
		String artconc=Arrays.toString(art);
		artconc=artconc.substring(1, artconc.length()-1);
		/*String query=String.format("RETURN {nodes:(FOR n IN APPEND(['%2$s'],GRAPH_NEIGHBORS('%1$s','%2$s',{edgeCollectionRestriction:%3$s})) "
			+ "LET v=DOCUMENT(n) RETURN MERGE(v,{type:PARSE_IDENTIFIER(v._id).collection}))"//{id:v._id,r:v.rank,t:PARSE_IDENTIFIER(v._id).collection,n:v.name,c:v.current})"
			+ ",links:(FOR n IN GRAPH_EDGES('%1$s','%2$s',{edgeCollectionRestriction:%3$s}) "
			+ "LET d=DOCUMENT(n) RETURN MERGE(d,{type:PARSE_IDENTIFIER(d).collection}))}"	//source:d._from,target:d._to,
			,Constants.TAXONOMICGRAPHNAME,id.toString(),EntityFactory.toJsonString(art)
		);*/
		String query=String.format("RETURN {nodes: " + 
		"    APPEND( " + 
		"        [MERGE(DOCUMENT('%1$s'),{type:PARSE_IDENTIFIER('%1$s').collection})] " + 
		"        ,(FOR v IN 1..%3$d ANY '%1$s' %2$s RETURN DISTINCT MERGE(v,{type:PARSE_IDENTIFIER(v._id).collection})) " + 
		"    ) " + 
		"    ,links:(FOR v,e IN 1..%3$d ANY '%1$s' %2$s RETURN DISTINCT MERGE(e,{type:PARSE_IDENTIFIER(e._id).collection})) " + 
		"} ",id.toString(), artconc, depth);
		//System.out.println(query);
		
/*		String query=String.format("FOR p IN GRAPH_TRAVERSAL('%1$s','%2$s','any',{paths:true,maxDepth:1}) "
			+ "RETURN {nodes:(FOR v IN p[*].vertex RETURN {id:v._id,r:v.rank,t:PARSE_IDENTIFIER(v._id).collection,n:v.name,c:v.current})"
			+ ",links:(FOR e IN FLATTEN(FOR ed IN p[*].path.edges RETURN ed) COLLECT a=e._id LET d=DOCUMENT(a) LET ty=PARSE_IDENTIFIER(d).collection "
			+ "FILTER ty=='PART_OF'"
			+ "RETURN {id:d._id,source:d._from,target:d._to,current:d.current,type:ty})}",Constants.TAXONOMICGRAPHNAME,id);*/
		//System.out.println(query);//System.out.println(res);
		String res;
		try {
			res = dbDriver.executeAqlQueryJSON(query, null, null);
		} catch (ArangoException e) {
			System.err.println(e.getErrorMessage());
			return GraphUpdateResult.emptyResult();
		}
		// NOTE: server responses are always an array, but here we always have one element, so we remove the []
		return (res==null || res.equals("[]")) ? GraphUpdateResult.emptyResult() : new GraphUpdateResult(res.substring(1, res.length()-1));
	}

	@Override
	public Territory getTerritoryFromShortName(String shortName) throws FloraOnException {
		String query=String.format("FOR v IN %2$s FILTER v.shortName=='%1$s' RETURN v"
			, shortName, NodeTypes.territory.toString());
		try {
			return dbDriver.executeAqlQuery(query, null, null, Territory.class).getUniqueResult();
		} catch (ArangoException e) {
			throw new DatabaseException(e.getErrorMessage());
		}
	}

	
    /**
	 * Gets only one node, or none, based on name and rank.
	 * NOTE: if rank is "norank", it is ignored.
     * @param q
     * @return
     * @throws QueryException
     * @throws ArangoException 
     */
	@Override
	public TaxEnt getTaxEnt(TaxEnt q) throws QueryException, FloraOnException {
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
    		String query=String.format("FOR t IN taxent FILTER t.name=='%1$s' RETURN t",q.getName());
    		n=dbDriver.executeAqlQuery(query, null, null, TaxEnt.class).getUniqueResult();
    		if(n==null)	// node doesn't exist
    			return null;

    		if(q.getRankValue()==null || q.getRankValue().equals(TaxonRanks.NORANK.getValue()) || n.getRankValue()==null) return n; else {
				if(!n.getRankValue().equals(q.getRankValue())) return null; else return n;
			}	    		
    	} catch (NonUniqueResultException e) {	// multiple nodes with this name. Search the one of the right rank
    		VertexCursor<TaxEnt> vc=null;
			try {
				vc = dbDriver.graphGetVertexCursor(Constants.TAXONOMICGRAPHNAME, TaxEnt.class, new TaxEnt(q.getName(),null,null,null), null, null);
			} catch (TaxonomyException e1) {
				// just go on, empty query
			} catch (ArangoException e1) {
				throw new DatabaseException(e1.getMessage());
			}
			if(q.getRankValue()==null || q.getRankValue().equals(TaxonRanks.NORANK.getValue())) throw new QueryException("More than one node with name "+q.getName()+". You must disambiguate.");

			Iterator<VertexEntity<TaxEnt>> ns=vc.iterator();
			n=null;
			TaxEnt n1;
			while(ns.hasNext()) {
				//n1=ns.next().getEntity();
				n1=new TaxEnt(ns.next().getEntity());
				if(n1.getRankValue().equals(q.getRankValue()) || n1.getRankValue().equals(TaxonRanks.NORANK.getValue())) {
					if(n!=null) throw new QueryException("More than one node with name "+q.getName()+" and rank "+q.getRank().toString()); else n=n1;
				}
			}
			return n;
    	} catch (ArangoException e) {
			throw new DatabaseException(e.getMessage());
		}
	}

	@Override
	public Iterator<NativeStatusResult> getTaxonNativeStatus(INodeKey id) throws FloraOnException {
/*		String query=String.format("LET terr=TRAVERSAL(taxent, EXISTS_IN, '%1$s', 'outbound', {maxDepth:1,paths:true}) "
			+ "FOR v IN SLICE(terr,1) RETURN {territory: v.vertex, nativeStatus:v.path.edges[0]}", id.toString());*/
		String query=String.format("FOR v,e IN 1..100 OUTBOUND '%1$s' EXISTS_IN RETURN {territory: v, nativeStatus:e}", id.toString());
		try {
			return dbDriver.executeAqlQuery(query, null, null, NativeStatusResult.class).iterator();
		} catch (ArangoException e) {
			throw new DatabaseException(e.getMessage());
		}
	}
	
	@Override
    public Attribute getAttributeByName(String name) throws FloraOnException {
		// TODO same function but to search only within one character
    	String query=String.format("FOR v IN attribute FILTER v.name=='%1$s' RETURN v",name);
		try {
			return dbDriver.executeAqlQuery(query, null, null, Attribute.class).getUniqueResult();
		} catch (ArangoException e) {
			throw new DatabaseException(e.getMessage());
		}
    }

	@Override
	public Attribute createAttributeFromName(String name, String shortName, String description) throws FloraOnException {
		Attribute out=new Attribute(name, shortName, description);
		try {
			VertexEntity<Attribute> ve=dbDriver.graphCreateVertex(Constants.TAXONOMICGRAPHNAME, NodeTypes.attribute.toString(), out, false);
			out=ve.getEntity();
			out.setID(ve.getDocumentHandle());
			out.setKey(ve.getDocumentKey());
			return out;
		} catch (ArangoException e) {
			throw new DatabaseException(e.getErrorMessage());
		}
	}

	@Override
	public Character createCharacter(Character charNode) throws FloraOnException {
		try {
			VertexEntity<Character> ve=dbDriver.graphCreateVertex(Constants.TAXONOMICGRAPHNAME, NodeTypes.character.toString(), charNode, false);
			charNode=ve.getEntity();
			charNode.setID(ve.getDocumentHandle());
			charNode.setKey(ve.getDocumentKey());
			return charNode;
		} catch (ArangoException e) {
			throw new DatabaseException(e.getErrorMessage());
		}
	}

	@Override
	public Character getCharacterByName(String name) throws FloraOnException {
    	String query=String.format("FOR v IN %2$s FILTER v.name=='%1$s' RETURN v",name,NodeTypes.character.toString());
		try {
			return dbDriver.executeAqlQuery(query, null, null, Character.class).getUniqueResult();
		} catch (ArangoException e) {
			throw new DatabaseException(e.getMessage());
		}
	}
}
