package pt.floraon.driver;

import java.util.Iterator;

import com.arangodb.ArangoException;
import com.google.gson.JsonObject;

import pt.floraon.driver.Constants.Facets;
import pt.floraon.driver.Constants.TaxonRanks;
import pt.floraon.driver.Constants.TerritoryTypes;
import pt.floraon.entities.Attribute;
import pt.floraon.entities.Author;
import pt.floraon.entities.Character;
import pt.floraon.entities.GeneralDBNode;
import pt.floraon.entities.SpeciesList;
import pt.floraon.entities.TaxEnt;
import pt.floraon.entities.Territory;
import pt.floraon.results.GraphUpdateResult;
import pt.floraon.results.NativeStatusResult;
import pt.floraon.results.Occurrence;

public interface INodeWorker {
	/***********************
	 * CREATING NODES
	 ***********************/
	
	/**
	 * Creates a new taxon and adds it to DB.
	 * @param name
	 * @param author
	 * @param rank
	 * @param annotation
	 * @param current
	 * @return
	 * @throws FloraOnException
	 */
	public TaxEnt createTaxEntFromName(String name,String author,TaxonRanks rank, String sensu, String annotation,Boolean current) throws TaxonomyException, FloraOnException;
    /**
	 * Creates a new taxon and adds it to DB.
	 * @param driver
	 * @param te
	 * @return
	 * @throws TaxonomyException
	 * @throws ArangoException
	 */
	public TaxEnt createTaxEntFromTaxEnt(TaxEnt te) throws TaxonomyException, FloraOnException;
	public Author createAuthor(Author author) throws FloraOnException;
	public SpeciesList createSpeciesList(SpeciesList sl) throws FloraOnException;
	public SpeciesList createSpeciesList(JsonObject sl) throws FloraOnException;
	public Territory createTerritory(String name, String shortName, TerritoryTypes type, String theme, boolean showInChecklist, INodeKey parent) throws FloraOnException;
    /**
     * Create a new attribute node and add to DB.
     * @param name
     * @param shortName
     * @param description
     * @return A {@link GraphUpdateResult} with the new node.
     * @throws ArangoException
     */
    public Attribute createAttributeFromName(String name,String shortName,String description) throws FloraOnException;
    public Character createCharacter(Character charNode) throws FloraOnException;

	public void createOccurrence(Occurrence occ) throws FloraOnException;

	/***********************
	 * FETCHING NODES
	 ***********************/

	public GeneralDBNode getNode(INodeKey id) throws FloraOnException;
	public Author getAuthorById(int idaut);
    public Territory getTerritoryFromShortName(String shortName) throws FloraOnException;
    public TaxEnt getTaxEntById(INodeKey id) throws FloraOnException;
    public TaxEnt getTaxEntByOldId(int oldId);
    /**
     * Gets only one taxon node, or none, based only on taxon name. The name must not be ambiguous.
     * @param q
     * @return
     * @throws QueryException
     * @throws ArangoException
     * @throws TaxonomyException
     */
	public TaxEnt getTaxEntByName(String q) throws FloraOnException;
	public TaxEnt getTaxEnt(TaxEnt q) throws QueryException, FloraOnException;
	public Attribute getAttributeByName(String name) throws FloraOnException;
	public Character getCharacterByName(String name) throws FloraOnException;

	/**
	 * Deletes one edge or one vertex and <b>all</b> connected edges.<br/><br/>
	 * <em>WARNING!</em> This function is dangerous! It will delete all edges connected to the given vertex.
	 * @param id The document handle, can be an edge or a vertex
	 * @return An array of the deleted document handles
	 * @throws FloraOnException
	 */
	public String[] deleteVertexOrEdge(INodeKey id) throws FloraOnException;
	/**
	 * Low-level function to delete one document.<br/><br/>
	 * <em>WARNING:</em> This does not check for graph consistency! An invalid graph may result.
	 * @param id
	 * @return
	 * @throws FloraOnException
	 */
	public String[] deleteDocument(INodeKey id) throws FloraOnException;
	/**
	 * Deletes one node and all connected edges if the node is a leaf node and if it has no data associated.
	 * @param id
	 * @return
	 * @throws FloraOnException if there is data associated with the node (e.g. EXISTS_IN, HAS_QUALITY, OBSERVED_IN)
	 */
	public String[] deleteLeafNode(INodeKey id) throws FloraOnException;
	public <T extends GeneralDBNode> T getNode(INodeKey id, Class<T> cls) throws FloraOnException;
	/**
	 * Deletes the SYNONYM relationship that is linking 'from' with 'to'. The deleted relationship is the one immediately connected to 'to'. Note that this might have side effects (break other synonym links) in complex chains of synonyms.
	 * @param from
	 * @param to
	 * @throws ArangoException 
	 */
	public GraphUpdateResult detachSynonym(INodeKey from,INodeKey to) throws FloraOnException;
	/**
	 * Low-level document updater. Adds or changes any attribute in any node.
	 * @param id
	 * @param key
	 * @param value
	 * @return
	 * @throws FloraOnException
	 */
    public GraphUpdateResult updateDocument(INodeKey id,String key,Object value) throws FloraOnException;
    /**
     * Updates or replaces a TaxEnt node in the DB. 
     * @param node
     * @param newTaxEnt
     * @param replace If true, null fields will be removed from the document; if false, null fields will keep their original values in the DB. 
     * @return
     * @throws FloraOnException
     */
    public GraphUpdateResult updateTaxEntNode(INodeKey node,TaxEnt newTaxEnt, boolean replace) throws FloraOnException;
    public GraphUpdateResult updateTerritoryNode(Territory node,String name,String shortName, TerritoryTypes type, String theme, boolean showInChecklist) throws FloraOnException;
	/**
	 * Gets the links between given nodes (in the ID array), of the given facets. Does not expand any node.
	 * @param id An array of document handles
	 * @param facets The link facets to load
	 * @return
	 * @throws ArangoException
	 */
    public GraphUpdateResult getRelationshipsBetween(String[] id, Facets[] facets) throws FloraOnException;

	/**
	 * Gets the direct neighbors of the given vertex, off all facets.
	 * @param id The vertex's document handle
	 * @return A JSON string with an array of vertices ('nodes') and an array of edges ('links') of the form {nodes[],links:[]}
	 * @throws ArangoException
	 */
	public GraphUpdateResult getNeighbors(INodeKey id, Facets[] facets, Integer depth);
	/**
	 * Gets all the native status that this taxon has, i.e., all the associations between a {@link Territory} and a {@link EXISTS_IN} relationship.
	 * @param id
	 * @return
	 * @throws ArangoException
	 */
	public Iterator<NativeStatusResult> getAssignedNativeStatus(INodeKey id) throws FloraOnException;
}
