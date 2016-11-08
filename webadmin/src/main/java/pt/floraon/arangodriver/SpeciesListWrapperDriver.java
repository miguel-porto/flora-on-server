package pt.floraon.arangodriver;

import com.arangodb.ArangoDBException;

import pt.floraon.driver.DatabaseException;
import pt.floraon.driver.FloraOnException;
import pt.floraon.driver.IFloraOn;
import pt.floraon.driver.INodeKey;
import pt.floraon.driver.ISpeciesListWrapper;
import pt.floraon.entities.Author;

import java.util.NoSuchElementException;

public class SpeciesListWrapperDriver extends NodeWrapperDriver implements ISpeciesListWrapper {
	public SpeciesListWrapperDriver(IFloraOn driver, INodeKey node) throws FloraOnException {
		super(driver, node);
	}

	@Override
	public int setObservedBy(int idaut,Boolean isMainAuthor) throws FloraOnException {
		String query=String.format(AQLQueries.getString("SpeciesListWrapperDriver.0"),node.getID(),idaut,isMainAuthor);
		try {
			return database.query(query,null,null,Integer.class).next();
		} catch (ArangoDBException | NoSuchElementException e) {
			throw new DatabaseException(e.getMessage());
		}
	}

	@Override
	public int setObservedBy(Author aut, Boolean isMainAuthor) throws FloraOnException {
// TODO if it is main observer, can only be one!
		String query=String.format(AQLQueries.getString("SpeciesListWrapperDriver.1"),node.getID(),aut.getIdAut(),isMainAuthor);
		try {
			return database.query(query,null,null,Integer.class).next();
		} catch (ArangoDBException | NoSuchElementException e) {
			throw new DatabaseException(e.getMessage());
		}
	}

}
