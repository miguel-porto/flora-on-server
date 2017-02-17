package pt.floraon.driver;

import java.util.List;

import com.google.gson.JsonObject;

import pt.floraon.taxonomy.entities.EXISTS_IN;
import pt.floraon.taxonomy.entities.TaxEnt;
import pt.floraon.taxonomy.entities.Territory;
import pt.floraon.driver.Constants.Facets;
import pt.floraon.driver.Constants.TaxonRanks;
import pt.floraon.driver.Constants.TerritoryTypes;
import pt.floraon.driver.entities.*;
import pt.floraon.morphology.entities.Attribute;
import pt.floraon.morphology.entities.Character;
import pt.floraon.occurrences.entities.Author;
import pt.floraon.occurrences.entities.SpeciesList;
import pt.floraon.driver.results.GraphUpdateResult;
import pt.floraon.driver.results.NativeStatusResult;
import pt.floraon.driver.results.Occurrence;

public interface INodeWorker {
	/***********************
	 * CREATING NODES
	 ***********************/

	/**
	 * Creates a new taxon and adds it to DB.
	 *
	 * @param name
	 * @param author
	 * @param rank
	 * @param annotation
	 * @param current
	 * @return
	 * @throws FloraOnException
	 */
	TaxEnt createTaxEntFromName(String name, String author, TaxonRanks rank, String sensu, String annotation, Boolean current) throws TaxonomyException, FloraOnException;

	/**
	 * Creates a new taxon and adds it to DB.
	 *
	 * @param te
	 * @return
	 * @throws TaxonomyException
	 */
	TaxEnt createTaxEntFromTaxEnt(TaxEnt te) throws TaxonomyException, FloraOnException;

	Author createAuthor(Author author) throws FloraOnException;

	SpeciesList createSpeciesList(SpeciesList sl) throws FloraOnException;

	SpeciesList createSpeciesList(JsonObject sl) throws FloraOnException;

	Territory createTerritory(String name, String shortName, TerritoryTypes type, String theme, boolean showInChecklist, INodeKey parent) throws FloraOnException;

	/**
	 * Create a new attribute node and add to DB.
	 *
	 * @param name
	 * @param shortName
	 * @param description
	 * @return A {@link GraphUpdateResult} with the new node.
	 */
	Attribute createAttributeFromName(String name, String shortName, String description) throws FloraOnException;

	Character createCharacter(Character charNode) throws FloraOnException;

	void createOccurrence(Occurrence occ) throws FloraOnException;

	/***********************
	 * FETCHING NODES
	 ***********************/

	//public GeneralDBNode getNode(INodeKey id) throws FloraOnException;

	/**
	 * Fetches one author with the given idAut
	 *
	 * @param idaut
	 * @return Null if not found, otherwise an {@link Author}
	 */
	Author getAuthorById(int idaut);

	Territory getTerritoryFromShortName(String shortName) throws FloraOnException;

	Territory getTerritoryFromShortName(INodeKey id) throws FloraOnException;

	TaxEnt getTaxEntById(INodeKey id) throws FloraOnException;

	List<TaxEnt> getTaxEntByIds(String[] id) throws FloraOnException;

	TaxEnt getTaxEntByOldId(int oldId);

	/**
	 * Gets only one taxon node, or none, based only on taxon name. The name must not be ambiguous.
	 *
	 * @param q
	 * @return
	 * @throws QueryException
	 * @throws TaxonomyException
	 */
	TaxEnt getTaxEntByName(String q) throws FloraOnException;

	/**
	 * Gets only one node, or none, based on name and rank.
	 * NOTE: if rank is "norank", it is ignored.
	 *
	 * @param q
	 * @return
	 * @throws FloraOnException
	 */
	TaxEnt getTaxEnt(TaxEnt q) throws QueryException, FloraOnException;

	Attribute getAttributeByName(String name) throws FloraOnException;

	Character getCharacterByName(String name) throws FloraOnException;

	/**
	 * Deletes one edge or one vertex and <b>all</b> connected edges.<br/><br/>
	 * <em>WARNING!</em> This function is dangerous! It will delete all edges connected to the given vertex.
	 *
	 * @param id The document handle, can be an edge or a vertex
	 * @return An array of the deleted document handles
	 * @throws FloraOnException
	 */
	String[] deleteVertexOrEdge(INodeKey id) throws FloraOnException;

	/**
	 * Low-level function to delete one document.<br/><br/>
	 * <em>WARNING:</em> This does not check for graph consistency! An invalid graph may result.
	 *
	 * @param id
	 * @return
	 * @throws FloraOnException
	 */
	String[] deleteDocument(INodeKey id) throws FloraOnException;

	/**
	 * Deletes one node and all connected edges if the node is a isLeaf node and if it has no data associated.
	 *
	 * @param id
	 * @return
	 * @throws FloraOnException if there is data associated with the node (e.g. EXISTS_IN, HAS_QUALITY, OBSERVED_IN)
	 */
	String[] deleteLeafNode(INodeKey id) throws FloraOnException;

	/**
	 * Gets an arbitrary document and returns it as the given class.
	 * <b>It is the user's responsibility to ensure that the expected object conforms to this class!</b>
	 *
	 * @param id
	 * @param cls
	 * @return
	 * @throws FloraOnException
	 */
	<T extends DBEntity> T getNode(INodeKey id, Class<T> cls) throws FloraOnException;

	/**
	 * Deletes the SYNONYM relationship that is linking 'from' with 'to'. The deleted relationship is the one immediately connected to 'to'. Note that this might have side effects (break other synonym links) in complex chains of synonyms.
	 *
	 * @param from
	 * @param to
	 */
	GraphUpdateResult detachSynonym(INodeKey from, INodeKey to) throws FloraOnException;

	/**
	 * Low-level document updater. Adds or changes any attribute in any node.
	 *
	 * @param id The ID of the document
	 * @param key The name of the attribute to replace
	 * @param value The value to replace
	 * @return
	 * @throws FloraOnException
	 */
	GraphUpdateResult updateDocument(INodeKey id, String key, Object value) throws FloraOnException;

	/**
	 * Updates or replaces a TaxEnt node in the DB.
	 *
	 * @param node
	 * @param newTaxEnt
	 * @param replace   If true, null fields will be removed from the document; if false, null fields will keep their original values in the DB.
	 * @return
	 * @throws FloraOnException
	 */
	GraphUpdateResult updateTaxEntNode(INodeKey node, TaxEnt newTaxEnt, boolean replace) throws FloraOnException;

	GraphUpdateResult updateTerritoryNode(Territory node, String name, String shortName, TerritoryTypes type, String theme, boolean showInChecklist) throws FloraOnException;

	/**
	 * Gets the links between given nodes (in the ID array), of the given facets. Does not expand any node.
	 *
	 * @param id     An array of document handles
	 * @param facets The link facets to load
	 * @return
	 * @throws FloraOnException
	 */
	GraphUpdateResult getRelationshipsBetween(String[] id, Facets[] facets) throws FloraOnException;

	/**
	 * Gets the direct neighbors of the given vertex, off all facets.
	 *
	 * @param id The vertex's document handle
	 * @return A JSON string with an array of vertices ('nodes') and an array of edges ('links') of the form {nodes[],links:[]}
	 */
	GraphUpdateResult getNeighbors(INodeKey id, Facets[] facets, Integer depth);

	/**
	 * Gets all the native status that this taxon has, i.e., all the associations between a {@link Territory} and a {@link EXISTS_IN} relationship.
	 *
	 * @param id
	 * @return
	 * @throws FloraOnException
	 */
	List<NativeStatusResult> getAssignedNativeStatus(INodeKey id) throws FloraOnException;

	<T extends DBEntity> T getNode(INodeKey id) throws FloraOnException;

	void addUploadedTableToUser(String uploadedTableFilename, INodeKey userId) throws DatabaseException;
}
