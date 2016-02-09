package pt.floraon.driver;

import java.util.Iterator;
import java.util.List;

import pt.floraon.driver.Constants.TaxonRanks;
import pt.floraon.driver.Constants.TerritoryTypes;
import pt.floraon.entities.TaxEnt;
import pt.floraon.entities.Territory;
import pt.floraon.results.ChecklistEntry;
import pt.floraon.results.GraphUpdateResult;
import pt.floraon.results.SimpleNameResult;

public interface IListDriver {
	public List<ChecklistEntry> getCheckList();
    /**
     * Gets the territories that should be listed in the checklist
     * @return
     * @throws ArangoException
     */
    public Iterator<Territory> getChecklistTerritories() throws FloraOnException;
	/**
	 * Gets all species or inferior ranks, optionally filtered by those that exist in the given territory.
	 * Note that when onlyLeafNodes is true and territory is not null, some taxa may be omitted from the list,
	 * namely those which have inferior taxa but are bond to a territory (and not the inferior taxa). 
	 * @param onlyLeafNodes true to return only the terminal nodes.
	 * @return An Iterator of any class that extends SimpleNameResult
	 * @territory The territory to filter taxa, or null if no filter is wanted.
	 * @throws ArangoException
	 */
    public <T extends SimpleNameResult> Iterator<T> getAllSpeciesOrInferior(boolean onlyLeafNodes, Class<T> T, String territory, Integer offset, Integer count) throws FloraOnException;
	/**
	 * Gets all the taxent nodes of the given rank
	 * @param rank
	 * @return
	 * @throws ArangoException
	 */
	public Iterator<TaxEnt> getAllOfRank(TaxonRanks rank) throws FloraOnException;
	/**
     * Gets all territories.
     * @return
     * @throws ArangoException
     */
	public Iterator<Territory> getAllTerritories(TerritoryTypes territoryType) throws FloraOnException;
    /**
     * Gets all territories and all the PART_OF relations between them
     * @param territoryType
     * @return
     * @throws FloraOnException
     */
    public GraphUpdateResult getAllTerritoriesGraph(TerritoryTypes territoryType) throws FloraOnException;
	/**
	 * Gets all morphological characters
	 * @return
	 */
	public GraphUpdateResult getAllCharacters();
}
