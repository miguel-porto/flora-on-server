package pt.floraon.arangodriver;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.arangodb.ArangoDriver;
import com.arangodb.ArangoException;
import com.arangodb.NonUniqueResultException;

import pt.floraon.driver.DatabaseException;
import pt.floraon.driver.FloraOnException;
import pt.floraon.driver.FloraOn;
import pt.floraon.driver.GTaxEntWrapper;
import pt.floraon.driver.INodeKey;
import pt.floraon.driver.ITaxEntWrapper;
import pt.floraon.driver.TaxonomyException;
import static pt.floraon.driver.Constants.*;
import pt.floraon.driver.Constants.NativeStatus;
import pt.floraon.driver.Constants.OccurrenceStatus;
import pt.floraon.driver.Constants.PhenologicalStates;
import pt.floraon.driver.Constants.RelTypes;
import pt.floraon.entities.EXISTS_IN;
import pt.floraon.entities.OBSERVED_IN;
import pt.floraon.entities.TaxEnt;
import pt.floraon.entities.Territory;
import pt.floraon.results.ListOfTerritoryStatus.InferredStatus;
import pt.floraon.results.TaxEntAndNativeStatusResult;

/**
 * A node wrapper for TaxEnt-specific operations. A TaxEnt must be provided to work on.
 * @author miguel
 *
 */
public class TaxEntWrapperDriver extends GTaxEntWrapper implements ITaxEntWrapper {
	protected ArangoDriver dbDriver;
	public TaxEntWrapperDriver(FloraOn driver, INodeKey tev) throws FloraOnException {
		super(driver, tev);
		this.dbDriver=(ArangoDriver) driver.getArangoDriver();
	}

	public Boolean isHybrid() {
		// TODO hybrids!
		return null;
	}
	
	@Override
	public boolean isLeafNode() throws FloraOnException {
		String query=String.format(AQLQueries.getString("TaxEntWrapperDriver.0"), thisNode);
		try {
			return dbDriver.executeAqlQuery(query,null,null,Integer.class).getUniqueResult()==0;
		} catch (ArangoException e) {
			throw new DatabaseException(e.getErrorMessage());
		}
	}

	@Override
	public Iterator<TaxEnt> getSynonyms() throws FloraOnException {
		String query=String.format(//"FOR u IN UNIQUE(FOR v IN TRAVERSAL(%1$s, %2$s, '%3$s', 'inbound',{paths:false}) FILTER v.vertex._id!='%3$s' RETURN v.vertex) RETURN u"
			AQLQueries.getString("TaxEntWrapperDriver.1")
			,RelTypes.SYNONYM.toString(),thisNode
		);
		try {
			return dbDriver.executeAqlQuery(query,null,null,TaxEnt.class).iterator();
		} catch (ArangoException e) {
			throw new DatabaseException(e.getErrorMessage());
		}
	}

	@Override
	public TaxEnt getParentTaxon() throws TaxonomyException, DatabaseException {
		String query = AQLQueries.getString("TaxEntWrapperDriver.2", thisNode.toString());
			
		TaxEnt out;
		try {
			out=dbDriver.executeAqlQuery(query,null,null,TaxEnt.class).getUniqueResult();
		} catch (NonUniqueResultException e) {
			throw new TaxonomyException("The taxon "+thisNode.toString()+" has more than one current parent taxon. This must be fixed.");	// TODO: what about hybrids?
		} catch (ArangoException e) {
			throw new DatabaseException(e.getErrorMessage());
		}
		//if(out==null) throw new TaxonomyException("The taxon "+node.toString()+" has no parent taxon. This must be fixed.");
		return out;
	}

	@Override
	public String[] getEndemismDegree() throws FloraOnException {
		String query=AQLQueries.getString("TaxEntWrapperDriver.9", thisNode.toString(), "");
		TaxEntAndNativeStatusResult list;
		
		try {
			list =  dbDriver.executeAqlQuery(query,null,null,TaxEntAndNativeStatusResult.class).getUniqueResult();
		} catch (ArangoException e) {
			throw new DatabaseException(e.getErrorMessage());
		}
		Set<String> tmp = list.inferEndemismDegree();
		String[] out = tmp.toArray(new String[tmp.size()]);
		return out;
	}

	@Override
	public Map<String,InferredStatus> getInferredNativeStatus(String territory) throws FloraOnException {
		String query=AQLQueries.getString("TaxEntWrapperDriver.9"
			, thisNode.toString()
			, territory == null ? "" : AQLQueries.getString("TaxEntWrapperDriver.9a", territory));
		TaxEntAndNativeStatusResult listOfStatus;
		try {
			listOfStatus =  dbDriver.executeAqlQuery(query,null,null,TaxEntAndNativeStatusResult.class).getUniqueResult();
		} catch (ArangoException e) {
			throw new DatabaseException(e.getErrorMessage());
		}
		return listOfStatus.inferNativeStatus(territory);		
	}

	public List<TaxEnt> getHybridAncestry() {
		// TODO get parent nodes
		return new ArrayList<TaxEnt>();
	}

	@Override
	public int setObservedIn(INodeKey slist,Short doubt,Short validated,PhenologicalStates state,String uuid,Integer weight,String pubnotes,String privnotes,NativeStatus nstate,String dateInserted) throws FloraOnException {
		OBSERVED_IN a=new OBSERVED_IN(doubt,validated,state,uuid,weight,pubnotes,privnotes,nstate,dateInserted,thisNode.toString(),slist.getID().toString());
		String query=String.format(
			AQLQueries.getString("TaxEntWrapperDriver.4")
			,thisNode, slist.getID(), a.toJSONString());
		try {
			return dbDriver.executeAqlQuery(query,null,null,Integer.class).getUniqueResult();
		} catch (ArangoException e) {
			throw new DatabaseException(e.getErrorMessage());
		}
	}

	@Override
	public Iterator<TaxEnt> getChildren() throws FloraOnException {
		String query=String.format(
			AQLQueries.getString("TaxEntWrapperDriver.5")
			,RelTypes.PART_OF.toString(),thisNode,RelTypes.HYBRID_OF.toString());
	    try {
			return dbDriver.executeAqlQuery(query, null, null, TaxEnt.class).iterator();
		} catch (ArangoException e) {
			throw new DatabaseException(e.getErrorMessage());
		}
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
				,eIn.toJSONString());
		}

		try {
			return dbDriver.executeAqlQuery(query,null,null,Integer.class).getUniqueResult();
		} catch (ArangoException e) {
			throw new DatabaseException(e.getErrorMessage());
		}
	}

	@Override
	public Iterator<TaxEnt> getIncludedTaxa() throws FloraOnException {
		String query = AQLQueries.getString("TaxEntWrapperDriver.8", RelTypes.PART_OF.toString(), thisNode);
		try {
			return dbDriver.executeAqlQuery(query,null,null,TaxEnt.class).iterator();
		} catch (ArangoException e) {
			throw new DatabaseException(e.getErrorMessage());
		}
	}

	@Override
	public List<Territory> getTerritoryNamesWithCompleteDistribution() throws DatabaseException {
		String query = AQLQueries.getString("TaxEntWrapperDriver.10", thisNode);
				
		try {
			return dbDriver.executeAqlQuery(query,null,null,Territory.class).asList();
		} catch (ArangoException e) {
			throw new DatabaseException(e.getErrorMessage());
		}
	}

	@Override
	public int setTerritoryWithCompleteDistribution(INodeKey id) throws DatabaseException {
		String query = AQLQueries.getString("TaxEntWrapperDriver.11", thisNode, id);
		
		try {
			return dbDriver.executeAqlQuery(query,null,null,Integer.class).getUniqueResult();
		} catch (ArangoException e) {
			throw new DatabaseException(e.getErrorMessage());
		}
	}

	@Override
	public int unsetTerritoryWithCompleteDistribution(INodeKey id) throws DatabaseException {
		String query = AQLQueries.getString("TaxEntWrapperDriver.12", thisNode, id);
		
		try {
			return dbDriver.executeAqlQuery(query,null,null,Integer.class).getUniqueResult();
		} catch (ArangoException e) {
			throw new DatabaseException(e.getErrorMessage());
		}
	}
}
