package pt.floraon.arangodriver;

import java.util.*;

import com.arangodb.ArangoCursor;
import com.arangodb.ArangoDB;
import com.arangodb.ArangoDBException;
import com.arangodb.ArangoDatabase;

import com.arangodb.model.AqlQueryOptions;
import pt.floraon.driver.*;
import pt.floraon.driver.interfaces.IFloraOn;
import pt.floraon.driver.interfaces.INodeKey;
import pt.floraon.driver.interfaces.ITaxEntWrapper;

import static pt.floraon.driver.Constants.*;
import pt.floraon.driver.Constants.NativeStatus;
import pt.floraon.driver.Constants.OccurrenceStatus;
import pt.floraon.driver.Constants.RelTypes;
import pt.floraon.taxonomy.entities.EXISTS_IN;
import pt.floraon.taxonomy.entities.TaxEnt;
import pt.floraon.taxonomy.entities.Territory;
import pt.floraon.driver.results.TaxEntAndNativeStatusResult;

/**
 * A node wrapper for TaxEnt-specific operations. A TaxEnt must be provided to work on.
 * @author miguel
 *
 */
public class TaxEntWrapperDriver extends GTaxEntWrapper implements ITaxEntWrapper {
	protected ArangoDB dbDriver;
	protected ArangoDatabase database;

	public TaxEntWrapperDriver(IFloraOn driver, INodeKey tev) throws FloraOnException {
		super(driver, tev);
		this.dbDriver = (ArangoDB) driver.getDatabaseDriver();
		this.database = (ArangoDatabase) driver.getDatabase();
	}

	@Override
	public Boolean isHybrid() {
		// TODO hybrids!
		return null;
	}
	
	@Override
	public boolean isLeafNode() throws FloraOnException {
		String query=String.format(AQLQueries.getString("TaxEntWrapperDriver.0"), thisNode);
		try {
			return database.query(query,null,null,Integer.class).next() == 0;
		} catch (ArangoDBException e) {
			throw new DatabaseException(e.getMessage());
		}
	}

	@Override
	public List<TaxEnt> getSynonyms() throws FloraOnException {
		String query = AQLQueries.getString("TaxEntWrapperDriver.1", thisNode.getID());

		try {
			return database.query(query,null,null,TaxEnt.class).asListRemaining();
		} catch (ArangoDBException e) {
			throw new DatabaseException(e.getMessage());
		}
	}

	@Override
	public TaxEnt getParentTaxon() throws TaxonomyException, DatabaseException {
		String query = AQLQueries.getString("TaxEntWrapperDriver.2", thisNode.toString());
			
		ArangoCursor<TaxEnt> cur;
		TaxEnt out;

		try {
			cur = database.query(query,null,null,TaxEnt.class);
			if(!cur.hasNext()) return null;
			out = cur.next();
			if(cur.hasNext())
				throw new TaxonomyException("The taxon "+thisNode.toString()+" has more than one current parent taxon.");	// TODO: what about hybrids?
		} catch (ArangoDBException e) {
			throw new DatabaseException(e.getMessage());
		}
		return out;
	}

	@Override
	public TaxEntAndNativeStatusResult getNativeStatusList(String territory) throws DatabaseException {
		String query=AQLQueries.getString("TaxEntWrapperDriver.9"
				, thisNode.toString()
				, territory == null ? "" : AQLQueries.getString("TaxEntWrapperDriver.9a", territory));
		TaxEntAndNativeStatusResult listOfStatus;

		try {
			listOfStatus = database.query(query,null,null,TaxEntAndNativeStatusResult.class).next();
		} catch (ArangoDBException | NoSuchElementException e) {
			throw new DatabaseException(e.getMessage());
		}
		return listOfStatus;
	}

	@Override
	public boolean isInfrataxonOf(INodeKey id) throws DatabaseException {
		if(id.getID().equals(thisNode.getID())) return true;
		String query = AQLQueries.getString("TaxEntWrapperDriver.13",
				thisNode.getID(), id.getID());
//		System.out.println(query);
		try {
			ArangoCursor<TaxEnt> r = database.query(query,null, new AqlQueryOptions(), TaxEnt.class);
			return r.hasNext();
		} catch (ArangoDBException | NoSuchElementException e) {
			throw new DatabaseException(e.getMessage());
		}
	}

	public List<TaxEnt> getHybridAncestry() {
		// TODO get parent nodes
		return new ArrayList<TaxEnt>();
	}

	@Override
	public int setNativeStatus(INodeKey territory, NativeStatus status, OccurrenceStatus occurrenceStatus, AbundanceLevel abundanceLevel, PlantIntroducedStatus introducedStatus, PlantNaturalizationDegree naturalizationDegree, Boolean uncertainOccurrenceStatus) throws FloraOnException {
		String query;
		if(status == null) {	// remove the EXISTS_IN link, if it exists
			query=String.format(
				AQLQueries.getString("TaxEntWrapperDriver.6")
				,thisNode.toString()
				,territory.toString());
		} else {				// create or update the EXISTS_IN link
			EXISTS_IN eIn=new EXISTS_IN(status, occurrenceStatus, abundanceLevel, introducedStatus, naturalizationDegree, uncertainOccurrenceStatus, thisNode.toString(), territory.toString());
			query=String.format(
				AQLQueries.getString("TaxEntWrapperDriver.7")
				,thisNode.toString()
				,territory.toString()
				,eIn.toJson().toString());
		}

		try {
			return database.query(query,null,null,Integer.class).next();
		} catch (ArangoDBException | NoSuchElementException e) {
			throw new DatabaseException(e.getMessage());
		}
	}

	@Override
	public List<TaxEnt> getIncludedTaxa() throws FloraOnException {
		String query = AQLQueries.getString("TaxEntWrapperDriver.8", thisNode);
		try {
			return database.query(query,null,null,TaxEnt.class).asListRemaining();
		} catch (ArangoDBException e) {
			throw new DatabaseException(e.getMessage());
		}
	}

	@Override
	public List<TaxEnt> getFormerlyIncludedIn() throws FloraOnException {
		String query = AQLQueries.getString("TaxEntWrapperDriver.8a", thisNode);
		try {
			return database.query(query,null,null,TaxEnt.class).asListRemaining();
		} catch (ArangoDBException e) {
			throw new DatabaseException(e.getMessage());
		}
	}

	@Override
	public List<Territory> getTerritoriesWithCompleteDistribution() throws DatabaseException {
		String query = AQLQueries.getString("TaxEntWrapperDriver.10", thisNode);
				
		try {
			return database.query(query,null,null,Territory.class).asListRemaining();
		} catch (ArangoDBException e) {
			throw new DatabaseException(e.getMessage());
		}
	}

	@Override
	public int setTerritoryWithCompleteDistribution(INodeKey id) throws DatabaseException {
		String query = AQLQueries.getString("TaxEntWrapperDriver.11", thisNode, id);
		
		try {
			return database.query(query,null,null,Integer.class).next();
		} catch (ArangoDBException | NoSuchElementException e) {
			throw new DatabaseException(e.getMessage());
		}
	}

	@Override
	public int unsetTerritoryWithCompleteDistribution(INodeKey id) throws DatabaseException {
		String query = AQLQueries.getString("TaxEntWrapperDriver.12", thisNode, id);
		
		try {
			return database.query(query,null,null,Integer.class).next();
		} catch (ArangoDBException | NoSuchElementException e) {
			throw new DatabaseException(e.getMessage());
		}
	}

	@Override
	public Iterator<TaxEnt> getInfrataxa(int level) throws DatabaseException {
		String query = AQLQueries.getString("TaxEntWrapperDriver.14", thisNode.getID(), level);

		try {
			return database.query(query,null,null,TaxEnt.class);
		} catch (ArangoDBException | NoSuchElementException e) {
			throw new DatabaseException(e.getMessage());
		}
	}

	@Override
	public TaxEnt getParentOfRank(TaxonRanks rank) throws DatabaseException {
		Map<String, Object> bindVars = new HashMap<>();
		bindVars.put("id", thisNode.getID());
		bindVars.put("rank", rank.getValue());

		try {
			Iterator<TaxEnt> it = database.query(AQLQueries.getString("TaxEntWrapperDriver.15"), bindVars,null, TaxEnt.class);
			if(it.hasNext()) return it.next();
			return null;
		} catch (ArangoDBException | NoSuchElementException e) {
			throw new DatabaseException(e.getMessage());
		}
	}
}
