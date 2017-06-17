package pt.floraon.driver;

import java.util.Iterator;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import pt.floraon.taxonomy.entities.ChecklistEntry;
import pt.floraon.taxonomy.entities.EXISTS_IN;
import pt.floraon.driver.Constants.TaxonRanks;
import pt.floraon.driver.Constants.TerritoryTypes;
import pt.floraon.taxonomy.entities.TaxEnt;
import pt.floraon.taxonomy.entities.Territory;
import pt.floraon.driver.results.GraphUpdateResult;
import pt.floraon.driver.results.SimpleTaxEntResult;

public interface IListDriver {
	/**
	 * Gets the complete list of taxa in the DB
	 * @return
	 */
	Iterator<ChecklistEntry> getCheckList() throws FloraOnException;
    /**
     * Gets the territories that should be listed in the checklist
     * @return
     * @throws FloraOnException
     */
    List<Territory> getChecklistTerritories() throws FloraOnException;

	/**
	 * Fetches all species or inferior taxa that exist in the given {@link Territory} (given by shortName) or all if null.
	 * Note that this only returns taxa directly assigned to the territory with a {@link EXISTS_IN} link.
	 * @param onlyCurrent Return only current TaxEnt?
	 * @param higherTaxa Return also higher taxa (up to species) or only the directly assigned to a territory?
	 * @param territory Short name of the territory. Can be <code>null</code>.
	 * @param offset Params for the LIMIT clause
	 * @param count Params for the LIMIT clause
	 * @return A List<TaxEnt>.
	 * @throws FloraOnException
	 */
	List<TaxEnt> getAllSpeciesOrInferiorTaxEnt(Boolean onlyCurrent, boolean higherTaxa, String territory, Integer offset, Integer count) throws FloraOnException;
	/**
	 * Gets all species or inferior ranks, optionally filtered by those that exist in the given territory.
	 * Note that when onlyLeafNodes is true and territory is not null, some taxa may be omitted from the list,
	 * namely those which have inferior taxa but are bond to a territory (and not the inferior taxa). 
	 * @param onlyLeafNodes true to return only the terminal nodes.
	 * @param territory The territory to filter taxa, or null if no filter is wanted.
	 * @return An Iterator of any class that extends SimpleNameResult
	 * @throws FloraOnException
	 */
	<T extends SimpleTaxEntResult> List<T> getAllSpeciesOrInferior(boolean onlyLeafNodes, Class<T> T, Boolean onlyCurrent, String territory, String filter, Integer offset, Integer count) throws FloraOnException;
	/**
	 * Gets all the taxent nodes of the given rank
	 * @param rank
	 * @return
	 * @throws FloraOnException
	 */
	Iterator<TaxEnt> getAllOfRank(TaxonRanks rank) throws FloraOnException;
	/**
     * Gets all territories.
     * @return
     * @throws FloraOnException
     */
	List<Territory> getAllTerritories(TerritoryTypes territoryType) throws FloraOnException;
    /**
     * Gets all territories and all the PART_OF relations between them
     * @param territoryType
     * @return
     * @throws FloraOnException
     */
	GraphUpdateResult getAllTerritoriesGraph(TerritoryTypes territoryType) throws FloraOnException;
	/**
	 * Gets all morphological characters
	 * @return
	 */
	GraphUpdateResult getAllCharacters();

	/**
	 * Gets all orphan taxa: taxa which have nothing more than inbound PART_OF
	 * @return
	 */
	Iterator<TaxEnt> getAllOrphanTaxa() throws FloraOnException;

	GraphUpdateResult getAllOrphanTaxaAsGUR() throws FloraOnException;

	/**
	 * Gets information about one given taxon.
	 * @param key
	 * @return
	 * @throws FloraOnException 
	 */
	JsonObject getTaxonInfo(INodeKey key) throws FloraOnException;
	/**
	 * Gets information about one given taxon.
	 * @param taxonName
	 * @return
	 * @throws FloraOnException 
	 */
	JsonArray getTaxonInfo(String taxonName, boolean onlyCurrent) throws FloraOnException;
	/**
	 * Gets information about one given taxon.
	 * @param oldId
	 * @return
	 * @throws FloraOnException
	 */
	JsonObject getTaxonInfo(int oldId) throws FloraOnException;

	/**
	 * Gets likely taxonomic errors in the graph.
	 * TODO: parameter to indicate what type of error
	 * @return
	 * @throws FloraOnException
	 */
	Iterator<TaxEnt> getTaxonomicErrors() throws FloraOnException;
}
