package pt.floraon.arangodriver;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

import com.arangodb.ArangoCursor;
import com.arangodb.ArangoDB;
import com.arangodb.ArangoDBException;
import com.arangodb.ArangoDatabase;
import com.arangodb.entity.DocumentCreateEntity;

import com.arangodb.entity.DocumentUpdateEntity;
import com.arangodb.entity.VertexEntity;
import com.arangodb.model.DocumentCreateOptions;
import com.arangodb.model.DocumentUpdateOptions;
import com.arangodb.model.VertexCreateOptions;
import com.arangodb.velocypack.VPackSlice;
import com.google.gson.Gson;
import org.apache.commons.lang.mutable.MutableBoolean;
import pt.floraon.driver.*;
import pt.floraon.driver.Constants.DocumentType;
import pt.floraon.driver.Constants.Facets;
import pt.floraon.driver.Constants.NodeTypes;
import pt.floraon.driver.Constants.RelTypes;
import pt.floraon.driver.Constants.TaxonRanks;
import pt.floraon.driver.Constants.TerritoryTypes;
import pt.floraon.driver.entities.GeneralDBNode;
import pt.floraon.driver.interfaces.IFloraOn;
import pt.floraon.driver.interfaces.INodeKey;
import pt.floraon.driver.interfaces.INodeWorker;
import pt.floraon.driver.utils.StringUtils;
import pt.floraon.ecology.entities.Habitat;
import pt.floraon.geocoding.entities.Toponym;
import pt.floraon.morphology.entities.Attribute;
import pt.floraon.morphology.entities.Character;
import pt.floraon.driver.entities.DBEntity;
import pt.floraon.taxonomy.entities.SYNONYM;
import pt.floraon.taxonomy.entities.TaxEnt;
import pt.floraon.taxonomy.entities.Territory;
import pt.floraon.driver.results.GraphUpdateResult;
import pt.floraon.driver.results.NativeStatusResult;

public class NodeWorkerDriver extends GNodeWorker implements INodeWorker {
	protected ArangoDB dbDriver;
	protected ArangoDatabase database;

	public NodeWorkerDriver(IFloraOn driver) {
		super(driver);
		this.dbDriver = (ArangoDB) driver.getDatabaseDriver();
		this.database = (ArangoDatabase) driver.getDatabase();
	}
	
	@Override
	public TaxEnt createTaxEntFromName(String name,String author,TaxonRanks rank,String sensu, String annotation,Boolean current) throws FloraOnException {
		TaxEnt out=new TaxEnt(name, rank == null ? null : rank.getValue(), author, sensu, annotation, current, null, null, null);
		try {
			//VertexEntity<TaxEnt> ve=dbDriver.graphCreateVertex(OccurrenceConstants.TAXONOMICGRAPHNAME, NodeTypes.taxent.toString(), out, false);
			DocumentCreateEntity<TaxEnt> ve = database.collection(NodeTypes.taxent.toString())
					.insertDocument(out, new DocumentCreateOptions().returnNew(true));
			out = ve.getNew();
			out.setID(ve.getId());
			out.setKey(ve.getKey());
			return out;
		} catch (ArangoDBException e) {
			throw new DatabaseException(e.getMessage());
		}
	}
	
	@Override
	@Deprecated
	public Territory createTerritory(String name, String shortName, TerritoryTypes type, String theme, boolean showInChecklist, INodeKey parent) throws FloraOnException {
		Territory out = new Territory(name, shortName, type, theme, showInChecklist);
		try {
//			VertexEntity<Territory> ve=dbDriver.graphCreateVertex(OccurrenceConstants.TAXONOMICGRAPHNAME, NodeTypes.territory.toString(), out, false);
			VertexEntity ve = database.graph(Constants.TAXONOMICGRAPHNAME).vertexCollection(NodeTypes.territory.toString())
					.insertVertex(out, new VertexCreateOptions());
			out.setID(ve.getId());
			out.setKey(ve.getKey());
			if(parent!=null) driver.wrapNode(driver.asNodeKey(out.getID())).setPART_OF(parent);
		} catch (ArangoDBException e) {
			throw new DatabaseException(e.getMessage());
		}
		
		return out;
	}

	@Override
	public <T extends GeneralDBNode> T createNode(Class<T> cls, T node) throws FloraOnException {
		try {
			VertexEntity ve = database.graph(Constants.TAXONOMICGRAPHNAME).vertexCollection(node.getTypeAsString())
					.insertVertex(node);
			node.setID(ve.getId());
			node.setKey(ve.getKey());
		} catch (ArangoDBException e) {
			throw new DatabaseException(e.getMessage());
		}
		return node;
	}

	@Override
	public <T extends GeneralDBNode> T createDocument(T document) throws FloraOnException {
		try {
			return database.collection(document.getTypeAsString()).insertDocument(document, new DocumentCreateOptions().returnNew(true)).getNew();
		} catch (ArangoDBException e) {
			throw new DatabaseException(e.getMessage());
		}
	}

	@Override
	public TaxEnt getTaxEntById(INodeKey id) throws FloraOnException {
		if(id == null) throw new FloraOnException("No ID provided");
		try {
			return database.getDocument(id.toString(), TaxEnt.class);
		} catch (ArangoDBException e) {
			throw new DatabaseException(e.getMessage());
		}
	}

	@Override
	public Habitat getHabitatById(INodeKey id) throws FloraOnException {
		if(id == null) throw new FloraOnException("No ID provided");
		try {
			return database.getDocument(id.toString(), Habitat.class);
		} catch (ArangoDBException e) {
			throw new DatabaseException(e.getMessage());
		}
	}

	@Override
	public Iterator<TaxEnt> getTaxEntByIds(String[] id) throws FloraOnException {
		Map<String, Object> bp = new HashMap<>();
		bp.put("ids", id);
		try {
			return database.query(AQLQueries.getString("NodeWorkerDriver.13"), bp, null, TaxEnt.class);
		} catch (ArangoDBException e) {
			return Collections.emptyIterator();
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
		List<TaxEnt> out;
		try {
			out = database.query(query, null, null, TaxEnt.class).asListRemaining();
			if(out.size() != 1) return null;
			return out.get(0);
		} catch (ArangoDBException e) {
			System.err.println("More than one taxon with this ID?!");
			return null;
		}
	}
	
	@Override
	public String[] deleteVertexOrEdge(INodeKey id) throws FloraOnException {
		List<String> deleted=new ArrayList<String>();
		try {
			switch(id.getDocumentType()) {
			case EDGE:
				database.graph(Constants.TAXONOMICGRAPHNAME).edgeCollection(id.getCollection()).deleteEdge(id.getDBKey());
				break;
			
			case VERTEX:
				// we do it manually cause we want the IDs of everything that was deleted
				String query = AQLQueries.getString("NodeWorkerDriver.2", id.getID(), StringUtils.implode(",", RelTypes.values()));
				ArangoKey tmp;
				ArangoCursor<String> toDelete = database.query(query, null, null, String.class);
				if(toDelete.getWarnings().size() > 0)
					throw new DatabaseException(toDelete.getWarnings().iterator().next().getMessage());
				
				while(toDelete.hasNext()) {
					tmp = (ArangoKey) driver.asNodeKey(toDelete.next());
					database.collection(tmp.getCollection()).deleteDocument(tmp.getDBKey());
					deleted.add(tmp.getID());
				}
				database.collection(id.getCollection()).deleteDocument(id.getDBKey());
				break;

			case NONE:
				throw new DatabaseException("Given key is not an edge nor a vertex.");
			}
			deleted.add(id.getID());
		} catch (ArangoDBException e) {
			throw new DatabaseException(e.getMessage());
		}
		return deleted.toArray(new String[0]);
	}

	@Override
	public String[] deleteDocument(INodeKey id) throws FloraOnException {
		List<String> deleted=new ArrayList<String>();
		try {
			database.collection(id.getCollection()).deleteDocument(id.getDBKey());
			deleted.add(id.toString());
		} catch (ArangoDBException e) {
			throw new DatabaseException(e.getMessage());
		}
		return deleted.toArray(new String[0]);
	}

	@Override
	public String[] deleteLeafNode(INodeKey id) throws FloraOnException {
		List<String> deleted = new ArrayList<>();
		ArangoKey tmp;
		String query = AQLQueries.getString("NodeWorkerDriver.8", id.toString());

		try {
			Iterator<String> toDelete = database.query(query,null,null,String.class);
			while(toDelete.hasNext()) {
				tmp = (ArangoKey) driver.asNodeKey(toDelete.next());
				database.collection(tmp.getCollection()).deleteDocument(tmp.getDBKey());
				deleted.add(tmp.getID());
			}
		} catch (ArangoDBException e) {
			throw new DatabaseException(e.getMessage());
		}
		if(deleted.size() == 0) throw new FloraOnException("Node has children, data, or is parent of an hybrid");
		return deleted.toArray(new String[0]);
	}
/*
	public String[] deleteLeafNode(INodeKey id) throws FloraOnException {
		List<String> deleted=new ArrayList<String>();
		String tmp;
		// TODO check for attributes upon delete!
		String query=String.format("FOR e IN GRAPH_EDGES('%1$s','%2$s',{direction:'inbound'}) COLLECT WITH COUNT INTO cou RETURN cou"
			,OccurrenceConstants.TAXONOMICGRAPHNAME,id);
		
		try {
			if(dbDriver.executeAqlQuery(query, null, null, Integer.class).getUniqueResult()!=0) throw new FloraOnException("Node has children, inward synonyms or is parent of an hybrid");
		
			query=String.format("FOR e IN GRAPH_EDGES('%1$s','%2$s') RETURN e"
				,OccurrenceConstants.TAXONOMICGRAPHNAME,id);
			Iterator<String> vertexCursor=dbDriver.executeAqlQuery(query, null, null, String.class).iterator();
			while(vertexCursor.hasNext()) {
				tmp=vertexCursor.next();
				dbDriver.deleteDocument(tmp);
				deleted.add(tmp);
			}
			dbDriver.deleteDocument(id.toString());
			deleted.add(id.toString());
		} catch (ArangoDBException e) {
			throw new DatabaseException(e.getMessage());
		}
		return deleted.toArray(new String[0]);
	}
*/
/*	@Override
	public GeneralDBNode getDocument(INodeKey id) throws FloraOnException {
		try {
			return dbDriver.getDocument(id.toString(), GeneralDBNode.class).getEntity();
		} catch (ArangoDBException e) {
			throw new DatabaseException(e.getMessage());
		}
	}*/

	@Override
	public <T extends DBEntity> T getDocument(INodeKey id, Class<T> cls) throws FloraOnException {
		try {
			return database.getDocument(id.getID(), cls);
		} catch (ArangoDBException e) {
			throw new DatabaseException(e.getMessage());
		}
	}

	@Override
	public <T extends DBEntity> Iterator<T> getDocuments(Set<String> ids, Class<T> cls) throws FloraOnException {
		Map<String, Object> bindVars = new HashMap<>();
		bindVars.put("ids", ids);
		try {
			return database.query(AQLQueries.getString("NodeWorkerDriver.15"), bindVars, null, cls);
		} catch (ArangoDBException | NoSuchElementException e) {
			e.printStackTrace();
			throw new DatabaseException(e.getMessage());
		}
	}

	@Override
	public <T extends DBEntity> T getDocument(INodeKey id) throws FloraOnException {
		NodeTypes nt;
		RelTypes rt;
		if(id.getDocumentType() == DocumentType.VERTEX) {
			nt = NodeTypes.valueOf(id.getCollection());
			try { // FIXME HERE HERE
				return (T) database.getDocument(id.toString(), nt.getNodeClass());
			} catch (ArangoDBException | SecurityException | IllegalArgumentException e) {
				throw new DatabaseException(e.getMessage());
			}
		} else {
			rt = RelTypes.valueOf(id.getCollection());
			System.out.println(rt);
			try {
				return (T) database.getDocument(id.toString(), rt.getEdgeClass());
			} catch (ArangoDBException | SecurityException | IllegalArgumentException e) {
				throw new DatabaseException(e.getMessage());
			}
		}
	}

	@Override
	public void addUploadedTableToUser(String uploadedTableFilename, INodeKey userId) throws DatabaseException {
		try {
			database.query(AQLQueries.getString("NodeWorkerDriver.14", userId.getID(), uploadedTableFilename), null
			, null, null);
		} catch (ArangoDBException e) {
			throw new DatabaseException(e.getMessage());
		}
	}

	@Override
	public GraphUpdateResult detachSynonym(INodeKey from,INodeKey to) throws FloraOnException {
		String query = AQLQueries.getString("NodeWorkerDriver.1", from.toString(), to.toString());
		List<SYNONYM> deleted;
		try {
			deleted = database.query(query, null, null, SYNONYM.class).asListRemaining();
		} catch (ArangoDBException e) {
			throw new DatabaseException(e.getMessage());
		}
		if(deleted.size()==1)
			return new GraphUpdateResult(driver, deleted.get(0).getID());
		else
			return new GraphUpdateResult(driver, new String[] {deleted.get(0).getID(), deleted.get(1).getID()});
	}

	@Override
    public GraphUpdateResult updateDocument(INodeKey id, String key, Object value) throws FloraOnException {
    	HashMap<String, Object> newHashMap = new HashMap<String, Object>();
    	newHashMap.put(key, value);
    	try {
			database.collection(id.getCollection()).updateDocument(id.getDBKey(), newHashMap);
		} catch (ArangoDBException e) {
			throw new DatabaseException(e.getMessage());
		}
    	return new GraphUpdateResult(driver, id);
    }

	@Override
	public GraphUpdateResult updateTaxEntNode(INodeKey node, TaxEnt newTaxEnt, boolean replace) throws FloraOnException {
		if(replace && newTaxEnt.getRank() == null) throw new TaxonomyException("Taxon must have a rank");

		VPackSlice value = dbDriver.util().serialize(newTaxEnt, replace);

    	try {
			database.collection(node.getCollection())
					.updateDocument(node.getDBKey(), value, new DocumentUpdateOptions().keepNull(false));
		} catch (ArangoDBException e) {
			throw new DatabaseException(e.getMessage());
		}
		return new GraphUpdateResult(driver, node.getID());
	}

	@Override
    public GraphUpdateResult updateTerritoryNode(Territory node,String name,String shortName, TerritoryTypes type, String theme, boolean showInChecklist) throws FloraOnException {
		//Territory node = new Territory(FloraOnDriver.this, dbNodeWorker.getDocument(id, TerritoryVertex.class));
    	node.update(name, shortName, type, theme, showInChecklist);
    	try {
			database.collection(driver.asNodeKey(node.getID()).getCollection()).updateDocument(node.getKey(), node, new DocumentUpdateOptions().keepNull(false));
			//dbDriver.graphUpdateVertex(OccurrenceConstants.TAXONOMICGRAPHNAME, NodeTypes.territory.toString(), driver.asNodeKey(node.getID()).getDBKey(), node, false);
		} catch (ArangoDBException e) {
			throw new DatabaseException(e.getMessage());
		}
		return new GraphUpdateResult(driver, node.getID());
	}

	@Override
	public <T extends DBEntity> T updateDocument(INodeKey id, T newEntity, boolean replaceNull, Class<T> tClass) throws FloraOnException {
		DocumentUpdateEntity<T> out;
		try {
			out = database.collection(id.getCollection()).updateDocument(id.getDBKey(), newEntity
					, new DocumentUpdateOptions().serializeNull(replaceNull).keepNull(false).returnNew(true).waitForSync(true));
		} catch (ArangoDBException e) {
			throw new DatabaseException(e.getMessage());
		}
		return out.getNew();
	}

	@Override
	public GraphUpdateResult getRelationshipsBetween(String[] id, Facets[] facets) {
		RelTypes[] art = RelTypes.getRelTypesOfFacets(facets);
    	String rt = Arrays.toString(art);
		rt = rt.substring(1, rt.length()-1);

		Map<String, Object> bindVars = new HashMap<>();
		bindVars.put("nodes", id);

		String query = AQLQueries.getString("NodeWorkerDriver.9", rt);
		try {
			return new GraphUpdateResult(database.query(query, bindVars, null, String.class).next());
		} catch (ArangoDBException | NoSuchElementException e) {
			e.printStackTrace();
			return GraphUpdateResult.emptyResult();
		}
	}

	@Override
	public GraphUpdateResult getNeighbors(INodeKey id, Facets[] facets, Integer depth) {
		if(id==null) return GraphUpdateResult.emptyResult();
		RelTypes[] art=RelTypes.getRelTypesOfFacets(facets);
		String artconc=Arrays.toString(art);
		artconc=artconc.substring(1, artconc.length()-1);
		String query = AQLQueries.getString("NodeWorkerDriver.6", id.toString(), artconc, depth);
		List<String> res;
		try {
			res = database.query(query, null, null, String.class).asListRemaining();
		} catch (ArangoDBException e) {
			System.err.println(e.getMessage());
			return GraphUpdateResult.emptyResult();
		}
		return (res.size() == 0) ? GraphUpdateResult.emptyResult() : new GraphUpdateResult(this.driver, res);
	}

	@Override
	public Territory getTerritoryFromShortName(String shortName) throws FloraOnException {
		String query = AQLQueries.getString("NodeWorkerDriver.10", shortName);
		try {
			return database.query(query, null, null, Territory.class).next();
		} catch (ArangoDBException e) {
			throw new DatabaseException(e.getMessage());
		} catch (NoSuchElementException e) {
			return null;
		}
	}

	@Override
	public Territory getTerritoryFromShortName(INodeKey id) throws FloraOnException {
		String query=AQLQueries.getString("NodeWorkerDriver.5", id);
		try {
			return database.query(query, null, null, Territory.class).next();
		} catch (ArangoDBException e) {
			throw new DatabaseException(e.getMessage());
		}
	}

	@Override
	public List<TaxEnt> getTaxEnt(TaxEnt q, MutableBoolean askQuestion) throws FloraOnException {
		// TODO when imported name has not subsp., doesn't work, e.g. cistus ladanifer sulcatus
    	if(q.getName() == null || q.getName().equals("")) throw new QueryException("Invalid blank name.");
		ArangoCursor<TaxEnt> cursor;

		String query = AQLQueries.getString("NodeWorkerDriver.12", q.getName().substring(0
				, q.getName().length() > 2 ? 3 : q.getName().length()));
// FIXME: when no rank is specified!
		try {
			cursor = database.query(query, null, null, TaxEnt.class);
			if(!cursor.hasNext())	// node does not exist
				return Collections.emptyList();
    	} catch (ArangoDBException e) {
			throw new DatabaseException(e.getMessage());
		}
		return matchTaxEntToTaxEntList(q, cursor, askQuestion);
	}

	@Override
	public TaxEnt getSingleTaxEntOrNull(TaxEnt q) throws FloraOnException {
		List<TaxEnt> nodes = getTaxEnt(q, null);

		switch(nodes.size()) {
			case 0:
				return null;
			case 1:
				return nodes.get(0);
			default:
				throw new QueryException(Messages.getString("error.4", q.getName()));
		}
	}

	@Override
	public List<NativeStatusResult> getAssignedNativeStatus(INodeKey id) throws FloraOnException {
		String query=AQLQueries.getString("NodeWorkerDriver.3", id.toString());
		try {
			return database.query(query, null, null, NativeStatusResult.class).asListRemaining();
		} catch (ArangoDBException e) {
			throw new DatabaseException(e.getMessage());
		}
	}
	
	@Override
    public Attribute getAttributeByName(String name) throws FloraOnException {
		// TODO same function but to search only within one character
    	String query = AQLQueries.getString("NodeWorkerDriver.4", name);
		try {
			return database.query(query, null, null, Attribute.class).next();
		} catch (ArangoDBException | NoSuchElementException e) {
			throw new DatabaseException(e.getMessage());
		}
    }

	@Override
	public Attribute createAttributeFromName(String name, String shortName, String description) throws FloraOnException {
		Attribute out=new Attribute(name, shortName, description);
		try {
			VertexEntity ve = database.graph(Constants.TAXONOMICGRAPHNAME).vertexCollection(NodeTypes.attribute.toString())
					.insertVertex(out);
			out.setID(ve.getId());
			out.setKey(ve.getKey());
			return out;
		} catch (ArangoDBException e) {
			throw new DatabaseException(e.getMessage());
		}
	}

	@Override
	public Character createCharacter(Character charNode) throws FloraOnException {
		try {
			VertexEntity ve = database.graph(Constants.TAXONOMICGRAPHNAME).vertexCollection(NodeTypes.character.toString())
					.insertVertex(charNode);
			charNode.setID(ve.getId());
			charNode.setKey(ve.getKey());
			return charNode;
		} catch (ArangoDBException e) {
			throw new DatabaseException(e.getMessage());
		}
	}

	@Override
	public Character getCharacterByName(String name) throws FloraOnException {
    	String query = AQLQueries.getString("NodeWorkerDriver.11", name);
		try {
			return database.query(query, null, null, Character.class).next();
		} catch (ArangoDBException e) {
			throw new DatabaseException(e.getMessage());
		}
	}

	@Override
	public <T extends GeneralDBNode> void createDocuments(List<T> documents) throws FloraOnException {
		if(documents.size() == 0) return;
		String type = documents.get(0).getTypeAsString();
		try {
			database.collection(type).insertDocuments(documents);
		} catch (ArangoDBException e) {
			throw new DatabaseException(e.getMessage());
		}
	}
}
