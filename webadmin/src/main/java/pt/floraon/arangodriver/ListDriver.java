package pt.floraon.arangodriver;

import java.util.*;

import com.arangodb.ArangoCursor;
import com.arangodb.ArangoDBException;
import com.arangodb.ArangoDatabase;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import jline.internal.Log;
import pt.floraon.bibliography.entities.Reference;
import pt.floraon.driver.*;
import pt.floraon.driver.interfaces.IFloraOn;
import pt.floraon.driver.Constants.Facets;
import pt.floraon.driver.Constants.NodeTypes;
import pt.floraon.driver.Constants.TaxonRanks;
import pt.floraon.driver.Constants.TerritoryTypes;
import pt.floraon.driver.entities.DBEntity;
import pt.floraon.driver.interfaces.IListDriver;
import pt.floraon.driver.interfaces.INodeKey;
import pt.floraon.ecology.entities.Habitat;
import pt.floraon.taxonomy.entities.ChecklistEntry;
import pt.floraon.taxonomy.entities.TaxEnt;
import pt.floraon.taxonomy.entities.Territory;
import pt.floraon.driver.results.GraphUpdateResult;
import pt.floraon.driver.results.TaxEntAndNativeStatusResult;
import pt.floraon.driver.results.SimpleTaxEntResult;

public class ListDriver extends BaseFloraOnDriver implements IListDriver {
	private ArangoDatabase database;

	public ListDriver(IFloraOn driver) {
		super(driver);
		this.database = (ArangoDatabase) driver.getDatabase();
	}

	@Override
	public Iterator<ChecklistEntry> getCheckList() throws FloraOnException {
		String query = AQLQueries.getString("Checklist", true);
		try {
			return database.query(query, null, null, ChecklistEntry.class);
		} catch (ArangoDBException e) {
			throw new FloraOnException(e.getMessage());
		}

	}

	@Override
	public <T extends DBEntity> Iterator<T> getAllDocumentsOfCollection(String collection, Class<T> cls) throws DatabaseException {
		Map<String, Object> bindVars = new HashMap<>();
		bindVars.put("@nodetype", collection);
		try {
			return database.query(AQLQueries.getString("ListDriver.6a"), bindVars, null, cls);
		} catch (ArangoDBException e) {
			throw new DatabaseException(e.getErrorMessage());
		}
	}

	@Override
	public <T extends DBEntity> List<T> getAllDocumentsOfCollectionAsList(String collection, Class<T> cls) throws DatabaseException {
		return ((ArangoCursor<T>) getAllDocumentsOfCollection(collection, cls)).asListRemaining();
	}

	@Override
    public GraphUpdateResult getAllTerritoriesGraph(TerritoryTypes territoryType) throws FloraOnException {
    	String query;
    	if(territoryType!=null)
    		query=String.format(AQLQueries.getString("ListDriver.2"),NodeTypes.territory.toString(), territoryType.toString());
    	else
    		query=String.format(AQLQueries.getString("ListDriver.3"),NodeTypes.territory.toString());
    	String[] ids=new String[0];
    	try {
			ids=database.query(query, null, null, String.class).asListRemaining().toArray(ids);
		} catch (ArangoDBException e) {
			throw new FloraOnException(e.getMessage());
		}
    	return driver.getNodeWorkerDriver().getRelationshipsBetween(ids, new Facets[] {Facets.TAXONOMY});
    }

    @Override
	public GraphUpdateResult getGraphWholeCollection(NodeTypes nodeType, Facets[] facets) throws FloraOnException {
		Map<String, Object> bindVars = new HashMap<>();
		bindVars.put("@nodetype", nodeType.toString());

		String[] ids=new String[0];
		try {
			ids = database.query(AQLQueries.getString("ListDriver.3a"), bindVars, null, String.class).asListRemaining().toArray(ids);
		} catch (ArangoDBException e) {
			throw new FloraOnException(e.getMessage());
		}
		return driver.getNodeWorkerDriver().getRelationshipsBetween(ids, facets);
	}

	@Override
    public List<Territory> getChecklistTerritories() throws FloraOnException {
		String query = AQLQueries.getString("ListDriver.4", NodeTypes.territory.toString());
    	try {
			return database.query(query, null, null, Territory.class).asListRemaining();
		} catch (ArangoDBException e) {
			throw new FloraOnException(e.getMessage());
		}
    }

	@Override
    public List<Territory> getAllTerritories(TerritoryTypes territoryType) throws FloraOnException {
    	String query;
    	if(territoryType!=null)
    		query=String.format(AQLQueries.getString("ListDriver.5"),NodeTypes.territory.toString(), territoryType.toString()); //$NON-NLS-1$
    	else
    		query=String.format(AQLQueries.getString("ListDriver.6"),NodeTypes.territory.toString()); //$NON-NLS-1$
    	try {
			return database.query(query, null, null, Territory.class).asListRemaining();
		} catch (ArangoDBException e) {
			throw new FloraOnException(e.getMessage());
		}
    }

	@Override
	public List<TaxEnt> getAllSpeciesOrInferiorTaxEnt(Boolean onlyCurrent, boolean higherTaxa, String territory, Integer offset, Integer count) throws FloraOnException {
		String query = null;
		boolean withLimit = false;
		if(!(offset==null && count==null)) {
			if(offset==null) offset=0;
			if(count==null) count=50;
			withLimit=true;
		}
		if(territory == null) {
			return Collections.emptyList();
			// FIXME return all taxa - should know the root node?
		} else {
			Log.warn("Possibly omitting taxa from the checklist.");
			query=AQLQueries.getString("ListDriver.8"
					, territory
					, onlyCurrent ? "&& thistaxon.current" : ""
					, withLimit ? "LIMIT "+offset+","+count : ""
					, higherTaxa ? ", OUTBOUND PART_OF" : ""
			);
			System.out.println(query);
		}
		try {
			return database.query(query, null, null, TaxEnt.class).asListRemaining();
		} catch (ArangoDBException e) {
			throw new FloraOnException(e.getMessage());
		}
	}

	@Override
	public <T extends SimpleTaxEntResult> List<T> getAllSpeciesOrInferior(boolean onlyLeafNodes, Class<T> T, Boolean onlyCurrent, String territory, String filter, Integer offset, Integer count) throws FloraOnException {
		String query = null;
		boolean withLimit=false;
		if(!(offset==null && count==null)) {
			if(offset==null) offset=0;
			if(count==null) count=50;
			withLimit=true;
		}
		if(territory==null) {
			query=AQLQueries.getString("ListDriver.7"
				, onlyLeafNodes ? "&& npar==0" : ""
				, NodeTypes.taxent.toString()
				, withLimit ? "LIMIT "+offset+","+count : ""
				, onlyCurrent ? "&& thistaxon.current" : ""
				, filter == null ? "" : "FILTER LIKE(thistaxon.name, '%%" + filter + "%%', true) "
			);//onlyCurrent ? "FILTER v.current==true" : ""
		} else {
			if(onlyLeafNodes) System.out.println("Warning: possibly omitting taxa from the checklist.");
//FIXME do this!
		}
		//System.out.println(query);
		try {
			return database.query(query, null, null, T).asListRemaining();
		} catch (ArangoDBException e) {
			throw new FloraOnException(e.getMessage());
		}
	}

	@Override
	public Iterator<TaxEnt> getAllOfRank(TaxonRanks rank) throws FloraOnException {
		String query=String.format(AQLQueries.getString("ListDriver.21") //$NON-NLS-1$
			,NodeTypes.taxent.toString(),rank.getValue());
		try {
			return database.query(query, null, null, TaxEnt.class);
		} catch (ArangoDBException e) {
			throw new FloraOnException(e.getMessage());
		}
	}
	
	@Override
	public GraphUpdateResult getAllCharacters() {
		String query = AQLQueries.getString("ListDriver.22", NodeTypes.character.toString());
		try {
			return new GraphUpdateResult(database.query(query, null, null, String.class).next());
		} catch (ArangoDBException e) {
			System.err.println(e.getMessage());
			return GraphUpdateResult.emptyResult();
		}
	}

	@Override
	public Iterator<TaxEnt> getAllOrphanTaxa() throws FloraOnException {
		String query = AQLQueries.getString("ListDriver.23");
		try {
			return database.query(query, null, null, TaxEnt.class);
		} catch (ArangoDBException e) {
			throw new FloraOnException(e.getMessage());
		}
	}

	@Override
	public GraphUpdateResult getAllOrphanTaxaAsGUR() throws FloraOnException {
		String query = AQLQueries.getString("ListDriver.23");
		try {
			return toGraphUpdateResult(database.query(query, null, null, String.class));
		} catch (ArangoDBException e) {
			System.err.println(e.getMessage());
			return GraphUpdateResult.emptyResult();
		}
	}

	@Override
	public JsonObject getTaxonInfo(INodeKey key) throws FloraOnException {
		String query=AQLQueries.getString("TaxEntWrapperDriver.9", key.toString(), "");

		List<TaxEntAndNativeStatusResult> cursorResult;
		try {
			cursorResult = database.query(query, null, null, TaxEntAndNativeStatusResult.class).asListRemaining();
			if(cursorResult.size() == 0)
				throw new FloraOnException("Taxon "+key+" not found.");
		} catch (ArangoDBException e) {
			throw new FloraOnException(e.getMessage());
		}
		return this.getTaxonInfoAsJson(cursorResult).get(0).getAsJsonObject();
	}

	@Override
	public JsonArray getTaxonInfo(String taxonName, boolean onlyCurrent) throws FloraOnException {
		String query=AQLQueries.getString("ListDriver.24b", taxonName, onlyCurrent ? "&& thistaxon.current" : "");
		List<TaxEntAndNativeStatusResult> cursorResult;
		try {
			cursorResult = database.query(query, null, null, TaxEntAndNativeStatusResult.class).asListRemaining();
		} catch (ArangoDBException e) {
			throw new FloraOnException(e.getMessage());
		}
		return this.getTaxonInfoAsJson(cursorResult);
	}

	@Override
	public JsonObject getTaxonInfo(int oldId) throws FloraOnException {
		String query=AQLQueries.getString("ListDriver.24c", oldId);

		List<TaxEntAndNativeStatusResult> cursorResult;
		try {
			cursorResult = database.query(query, null, null, TaxEntAndNativeStatusResult.class).asListRemaining();
			if(cursorResult.size() == 0)
				throw new FloraOnException("Taxon with oldId "+oldId+" not found.");
		} catch (ArangoDBException e) {
			throw new FloraOnException(e.getMessage());
		}
		return this.getTaxonInfoAsJson(cursorResult).get(0).getAsJsonObject();
	}

	@Override
	public Iterator<TaxEnt> getTaxonomicErrors() throws FloraOnException {
		String query = AQLQueries.getString("error.1");
		try {
			return database.query(query, null, null, TaxEnt.class);
		} catch (ArangoDBException e) {
			throw new FloraOnException(e.getMessage());
		}
	}

	@Override
	public Iterator<TaxEnt> getChildrenTaxEnt(INodeKey parentId) throws DatabaseException {
		Map<String, Object> bindVars = new HashMap<>();
		bindVars.put("parent", parentId.toString());

		try {
			return database.query(AQLQueries.getString("ListDriver.26"), bindVars, null, TaxEnt.class);
		} catch (ArangoDBException e) {
			throw new DatabaseException(e.getMessage());
		}
	}

	@Override
	public Iterator<Habitat> getChildrenHabitats(INodeKey parentId) throws DatabaseException {
		Map<String, Object> bindVars = new HashMap<>();
		bindVars.put("parent", parentId.toString());

		try {
			return database.query(AQLQueries.getString("ListDriver.25"), bindVars, null, Habitat.class);
		} catch (ArangoDBException e) {
			throw new DatabaseException(e.getMessage());
		}

	}

	@Override
	public Iterator<Habitat> getHabitatsOfLevel(int level) throws DatabaseException {
		Map<String, Object> bindVars = new HashMap<>();
		bindVars.put("level", level);

		try {
			return database.query(AQLQueries.getString("ListDriver.27"), bindVars, null, Habitat.class);
		} catch (ArangoDBException e) {
			throw new DatabaseException(e.getMessage());
		}
	}

	@Override
	public Iterator<Reference> findReferencesWithText(String query) throws DatabaseException {
		Map<String, Object> bindVars = new HashMap<>();
		bindVars.put("query", "%" + query + "%");

		try {
			return database.query(AQLQueries.getString("ListDriver.28"), bindVars, null, Reference.class);
		} catch (ArangoDBException e) {
			throw new DatabaseException(e.getMessage());
		}
	}
}
