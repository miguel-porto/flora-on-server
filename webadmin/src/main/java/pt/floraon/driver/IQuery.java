package pt.floraon.driver;

import java.util.Iterator;
import java.util.List;

import com.arangodb.ArangoException;

import pt.floraon.driver.Constants.NodeTypes;
import pt.floraon.driver.Constants.StringMatchTypes;
import pt.floraon.driver.Constants.TaxonRank;
import pt.floraon.entities.SpeciesList;
import pt.floraon.queryparser.Match;
import pt.floraon.results.SimpleNameResult;
import pt.floraon.results.SimpleTaxonResult;

/**
 * Interface defining methods for high-level querying of the database.
 * @author Miguel Porto
 *
 */
public interface IQuery {
    /**
	 * Gets all species lists within a radius of a given point
	 * @param latitude The point's latitude
	 * @param longitude The point's longitude
	 * @param distance The radius
	 * @return
	 * @throws ArangoException 
	 */
	public Iterator<SpeciesList> findSpeciesListsWithin(Float latitude,Float longitude,Float distance) throws FloraOnException;
	/**
	 * Checks whether given species list already exists (same author, same date, coordinates very close) and returns it.
	 * @param idAuthor
	 * @param latitude
	 * @param longitude
	 * @param year
	 * @param month
	 * @param day
	 * @param radius Radius in which to search for the species list
	 * @return Null if not found, a {@link SpeciesList} if one or more results are found. In the latter case, the returned result is "randomly" selected.
	 * @throws ArangoException
	 */
	public SpeciesList findExistingSpeciesList(int idAuthor,float latitude,float longitude,Integer year,Integer month,Integer day,float radius) throws FloraOnException;
    /**
     * Returns a list of all possible matches of the given query string, ordered in terms of relevance.
     * @param q
     * @param matchtype
     * @param collections
     * @return
     * @throws DatabaseException 
     * @throws ArangoException
     */
	public List<Match> queryMatcher(String q,StringMatchTypes matchtype,String[] collections) throws DatabaseException;
	/**
     * Fetches all species (or inferior rank) downstream the given match
     * @param match A {@link Match} created by {@link queryMatcher}
     * @return
	 * @throws DatabaseException 
     * @throws ArangoException 
     */
	public List<SimpleTaxonResult> fetchMatchSpecies(Match match,boolean onlyLeafNodes) throws DatabaseException;
    /**
     * Execute a text query that filters nodes by their name, and returns all species (or inferior rank) downstream the filtered nodes.
     * <b>This is the main query function.</b> 
     * @param q The query as a String. It is matched as a whole to the node 'name' attribute.
     * @param matchtype Type of match desired (exact, partial or prefix).
     * @param onlyLeafNodes true to return only leaf nodes. If false, all species or inferior rank nodes are returned.
     * @param collections Node collections to be searched for matches. They must have a 'name' attribute.
     * @return A list of {@link SimpleTaxonResult}
     * @throws DatabaseException 
     * @throws ArangoException
     */
	public List<SimpleTaxonResult> speciesTextQuerySimple(String q,StringMatchTypes matchtype,boolean onlyLeafNodes,String[] collections,TaxonRank rank) throws DatabaseException;
    /**
     * Gets a list of suggested names similar to the query
     * @param query
     * @return
     * @throws ArangoException
     */
	public Iterator<SimpleNameResult> findSuggestions(String query, Integer limit) throws FloraOnException;
	/**
	 * Gets all species found (in all species lists) within a distance from a point. Note that duplicates are removed, no matter how many occurrences each species has.
	 * Note that this only returns the TaxEnt nodes which are direct neighbors of the species list, independently of their taxonomic rank.
	 * @param latitude
	 * @param longitude
	 * @param distance
	 * @return A list
	 * @throws ArangoException
	 */
	public List<SimpleTaxonResult> findListTaxaWithin(Float latitude,Float longitude,int distance) throws FloraOnException;
    /**
     * Gets the number of nodes in given collection.
     * @param nodetype The collection
     * @return
     * @throws ArangoException
     */
    public int getNumberOfNodesInCollection(NodeTypes nodetype) throws FloraOnException;

}
