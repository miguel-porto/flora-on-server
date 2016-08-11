package pt.floraon.driver;

import java.util.List;

import pt.floraon.entities.Territory;

public interface FloraOn {
	public Object getArangoDriver();
	public INodeWorker getNodeWorkerDriver();
	public IQuery getQueryDriver();
	public IListDriver getListDriver();
	public CSVFileProcessor getCSVFileProcessor();
	public INodeWrapper wrapNode(INodeKey node) throws FloraOnException;
	public ITaxEntWrapper wrapTaxEnt(INodeKey node) throws FloraOnException;
	public IAttributeWrapper wrapAttribute(INodeKey node) throws FloraOnException;
	public ISpeciesListWrapper wrapSpeciesList(INodeKey node) throws FloraOnException;
	public List<Territory> getChecklistTerritories();
	public INodeKey asNodeKey(String id) throws FloraOnException;
}
