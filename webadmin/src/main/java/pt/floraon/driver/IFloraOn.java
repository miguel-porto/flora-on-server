package pt.floraon.driver;

import java.util.List;

import pt.floraon.entities.Territory;

/**
 * The constructor of a IFloraOn must either initialize a fresh new database, or check if there's one already functional.
 */
public interface IFloraOn {
	Object getDatabaseDriver();
	Object getDatabase();
	INodeWorker getNodeWorkerDriver();
	IQuery getQueryDriver();
	IListDriver getListDriver();
	CSVFileProcessor getCSVFileProcessor();
	IRedListData getRedListData();
	INodeWrapper wrapNode(INodeKey node) throws FloraOnException;
	ITaxEntWrapper wrapTaxEnt(INodeKey node) throws FloraOnException;
	IAttributeWrapper wrapAttribute(INodeKey node) throws FloraOnException;
	ISpeciesListWrapper wrapSpeciesList(INodeKey node) throws FloraOnException;
	List<Territory> getChecklistTerritories();
	INodeKey asNodeKey(String id) throws FloraOnException;
}
