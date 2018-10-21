package pt.floraon.driver.interfaces;

import java.util.Iterator;
import java.util.List;

import pt.floraon.arangodriver.ArangoKey;
import pt.floraon.authentication.entities.User;
import pt.floraon.driver.Constants.NodeTypes;
import pt.floraon.driver.Constants.StringMatchTypes;
import pt.floraon.driver.Constants.TaxonRanks;
import pt.floraon.driver.DatabaseException;
import pt.floraon.driver.FloraOnException;
import pt.floraon.geocoding.entities.MatchedToponym;
import pt.floraon.occurrences.entities.Inventory;
import pt.floraon.occurrences.entities.Occurrence;
import pt.floraon.queryparser.Match;
import pt.floraon.driver.results.SimpleTaxonResult;
import pt.floraon.taxonomy.entities.TaxEnt;

/**
 * Interface defining methods for high-level querying of the database.
 * @author Miguel Porto
 *
 */
public interface IQuery {
	/**
	 * Gets all occurrences that are contained in the given polygon
	 * @param geoJsonPolygon
	 * @return
	 */
	Iterator<Occurrence> findOccurrencesContainedIn(String geoJsonPolygon) throws FloraOnException;
    /**
	 * Gets all inventories within a radius of a given point
	 * @param latitude The point's latitude
	 * @param longitude The point's longitude
	 * @param distance The radius
	 * @return
	 * @throws FloraOnException
	 */
	Iterator<Inventory> findInventoriesWithin(Float latitude, Float longitude, Float distance) throws FloraOnException;
	/**
	 * Checks whether given inventory already exists (same author, same date, coordinates very close) and returns it.
	 * @param idAuthor
	 * @param latitude
	 * @param longitude
	 * @param year
	 * @param month
	 * @param day
	 * @param radius Radius in which to search for the species list
	 * @return Null if not found, a {@link Inventory} if one or more results are found. In the latter case, the returned result is "randomly" selected.
	 * @throws FloraOnException
	 */
	Inventory findExistingInventory(int idAuthor, float latitude, float longitude, Integer year, Integer month, Integer day, float radius) throws FloraOnException;
    /**
     * Returns a list of all possible matches of the given query string, ordered in terms of relevance.
     * @param q
     * @param matchtype
     * @param collections
     * @return
     * @throws DatabaseException 
     */
	List<Match> queryMatcher(String q, StringMatchTypes matchtype, String[] collections) throws DatabaseException;
	/**
     * Fetches all species (or inferior rank) downstream the given match.
     * <b>This is the main high-level query function</b>
     * This function processes results from the DB so that each species appears only once.
     * @param match A {@link Match} created by queryMatcher
     * @return
	 * @throws DatabaseException 
     */
	List<SimpleTaxonResult> fetchMatchSpecies(Match match, boolean onlyLeafNodes, boolean onlyCurrent) throws DatabaseException;
    /**
     * Execute a text query that filters nodes by their name, and returns all species (or inferior rank) downstream the filtered nodes.
     * <b>This is the main low-level query function.</b>
     * It does not process the results, so it returns all possible paths that lead to each species (or inferior)
     * @param q The query as a String. It is matched as a whole to the node 'name' attribute.
     * @param matchtype Type of match desired (exact, partial or prefix).
     * @param onlyLeafNodes true to return only isLeaf nodes. If false, all species or inferior rank nodes are returned.
     * @param collections Node collections to be searched for matches. They must have a 'name' attribute.
     * @return A list of {@link SimpleTaxonResult}
     * @throws DatabaseException 
     */
	List<SimpleTaxonResult> speciesTextQuerySimple(String q, StringMatchTypes matchtype, boolean onlyLeafNodes, boolean onlyCurrent, String[] collections, TaxonRanks rank) throws DatabaseException;
	List<SimpleTaxonResult> speciesTextQuerySimple(ArangoKey node, boolean onlyLeafNodes, boolean onlyCurrent) throws DatabaseException;
    /**
     * Gets a list of suggested names similar to the query. Abbreviations should also be accepted.
     * @param query
     * @return
     * @throws FloraOnException
     */
	Iterator<TaxEnt> findTaxonSuggestions(String query, Integer limit) throws FloraOnException;

	/**
	 * Gets a list of suggested names similar to the query
	 * @param query
	 * @return
	 * @throws FloraOnException
	 */
	Iterator<User> findUserSuggestions(String query, Integer limit) throws FloraOnException;

	List<MatchedToponym> findToponymSuggestions(String query) throws FloraOnException;

	/**
	 * Gets all species found (in all species lists) within a distance from a point. Note that duplicates are removed, no matter how many occurrences each species has.
	 * Note that this only returns the TaxEnt nodes which are direct neighbors of the species list, independently of their taxonomic rank.
	 * @param latitude
	 * @param longitude
	 * @param distance
	 * @return A list
	 * @throws FloraOnException
	 */
	List<SimpleTaxonResult> findListTaxaWithin(Float latitude, Float longitude, int distance) throws FloraOnException;
    /**
     * Gets the number of nodes in given collection.
     * @param nodetype The collection
     * @return
     * @throws FloraOnException
     */
	int getNumberOfNodesInCollection(NodeTypes nodetype) throws FloraOnException;

}
