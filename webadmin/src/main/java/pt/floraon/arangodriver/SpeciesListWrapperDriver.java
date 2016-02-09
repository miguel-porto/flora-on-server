package pt.floraon.arangodriver;

import com.arangodb.ArangoException;

import pt.floraon.driver.DatabaseException;
import pt.floraon.driver.FloraOnException;
import pt.floraon.driver.FloraOn;
import pt.floraon.driver.INodeKey;
import pt.floraon.driver.ISpeciesListWrapper;
import pt.floraon.entities.Author;

public class SpeciesListWrapperDriver extends NodeWrapperDriver implements ISpeciesListWrapper {
	public SpeciesListWrapperDriver(FloraOn driver, INodeKey node) {
		super(driver, node);
	}

	@Override
	public int setObservedBy(int idaut,Boolean isMainAuthor) throws FloraOnException {
		String query=String.format("FOR au IN author FILTER au.idAut==%2$d UPSERT {_from:'%1$s',_to:au._id} INSERT {_from:'%1$s',_to:au._id,main:%3$b} UPDATE {} IN OBSERVED_BY RETURN OLD ? 0 : 1",node.getID(),idaut,isMainAuthor);
		try {
			return dbDriver.executeAqlQuery(query,null,null,Integer.class).getUniqueResult();
		} catch (ArangoException e) {
			throw new DatabaseException(e.getErrorMessage());
		}
	}

	@Override
	public int setObservedBy(Author aut, Boolean isMainAuthor) throws FloraOnException {
// TODO if it is main observer, can only be one!
		String query=String.format("FOR au IN author FILTER au.idAut==%2$d UPSERT {_from:'%1$s',_to:au._id} INSERT {_from:'%1$s',_to:au._id,main:%3$b} UPDATE {} IN OBSERVED_BY RETURN OLD ? 0 : 1",node.getID(),aut.getIdAut(),isMainAuthor);
		try {
			return dbDriver.executeAqlQuery(query,null,null,Integer.class).getUniqueResult();
		} catch (ArangoException e) {
			throw new DatabaseException(e.getErrorMessage());
		}
	}

}
