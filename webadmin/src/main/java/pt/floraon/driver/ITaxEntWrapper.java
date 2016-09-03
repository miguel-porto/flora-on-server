package pt.floraon.driver;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.arangodb.ArangoException;

import pt.floraon.driver.Constants.AbundanceLevel;
import pt.floraon.driver.Constants.NativeStatus;
import pt.floraon.driver.Constants.OccurrenceStatus;
import pt.floraon.driver.Constants.PhenologicalStates;
import pt.floraon.driver.Constants.PlantIntroducedStatus;
import pt.floraon.driver.Constants.PlantNaturalizationDegree;
import pt.floraon.driver.Constants.TaxonRanks;
import pt.floraon.entities.GeneralDBEdge;
import pt.floraon.entities.TaxEnt;
import pt.floraon.entities.Territory;
import pt.floraon.results.ListOfTerritoryStatus.InferredStatus;
import pt.floraon.results.TaxEntAndNativeStatusResult;

public interface ITaxEntWrapper {
	public int createRelationshipTo(INodeKey parent, GeneralDBEdge edge) throws FloraOnException;
	public Boolean isHybrid();
	public boolean isLeafNode() throws FloraOnException;
	/**
	 * Gets the chain of synonyms associated with this taxon (excluding self). Note that only true SYNONYMs are returned (no PART_OF). See {@link getIncludedTaxa}
	 * @return
	 * @throws FloraOnException 
	 */
	public Iterator<TaxEnt> getSynonyms() throws FloraOnException;
	/**
	 * Gets the taxa which are PART_OF this taxon and are not current.
	 * @return
	 * @throws FloraOnException 
	 */
	public Iterator<TaxEnt> getIncludedTaxa() throws FloraOnException;
	/**
	 * Gets the current taxonomic parent of this taxon.
	 * @return
	 * @throws TaxonomyException if there is more than one current parent (this is a taxonomic violation)
	 */
	public TaxEnt getParentTaxon() throws FloraOnException;
	/**
	 * Gets and array of territories which altogether comprise the endemic range of the taxon.
	 * @return
	 * @throws ArangoException 
	 */
	public String[] getEndemismDegree() throws FloraOnException;
	/**
	 * Gets the parents of an hybrid
	 * @return
	 */
	public List<TaxEnt> getHybridAncestry();
	/**
	 * Sets this taxon as a synonym of given taxon. Automatically sets this taxon to not current.
	 * @param tev
	 * @throws FloraOnException 
	 */
	public void setSynonymOf(INodeKey tev) throws FloraOnException;
	public int setObservedIn(INodeKey slist,Short doubt,Short validated,PhenologicalStates state,String uuid,Integer weight,String pubnotes,String privnotes,NativeStatus nstate,String dateInserted) throws FloraOnException;
	/**
	 * Associates this taxon with an {@link Attribute}.
	 * @param parent The attribute
	 * @return 0 if already existing relationship, 1 if created new
	 * @throws IOException
	 * @throws ArangoException
	 */
	public int setHAS_QUALITY(INodeKey parent) throws FloraOnException;

	/**
     * Creates a new taxonomic node bond to the given parent node. Ensures that this new node is taxonomically valid.
     * This means that it must be of an inferior rank of its parent, and its name, in case it is below genus, must be fully qualified (i.e. not the epithets only)
     * @param parent
     * @param name
     * @param author
     * @param rank
     * @param annotation
     * @param current
     * @throws FloraOnException
     * @throws ArangoException
     */
    public INodeKey createTaxEntChild(String name,String author,TaxonRanks rank,String sensu, String annotation,Boolean current) throws FloraOnException;
	/**
	 * Gets the immediate children of the given TaxEnt node
	 * @param id
	 * @return
	 * @throws ArangoException 
	 */
	public Iterator<TaxEnt> getChildren() throws FloraOnException;
	/**
	 * Infers the NativeStatus of this TaxEnt for all territories marked for checklist or for the given territory (as shortName)
	 * @return
	 * @throws FloraOnException
	 */
	Map<String, InferredStatus> getInferredNativeStatus(String territory) throws FloraOnException;
	/**
	 * Sets the native status of this TaxEnt in the given territory
	 * @param territory
	 * @param status
	 * @param occurrenceStatus
	 * @param abundanceLevel
	 * @param introducedStatus
	 * @param naturalizationDegree
	 * @param uncertainOccurrenceStatus
	 * @return
	 * @throws FloraOnException
	 */
	public int setNativeStatus(INodeKey territory, NativeStatus status, OccurrenceStatus occurrenceStatus,
			AbundanceLevel abundanceLevel, PlantIntroducedStatus introducedStatus,
			PlantNaturalizationDegree naturalizationDegree, Boolean uncertainOccurrenceStatus) throws FloraOnException;
	
	/**
	 * Gets an array of territory names where the distribution of the TaxEnt is complete.
	 * @return
	 * @throws DatabaseException 
	 */
	public List<Territory> getTerritoriesWithCompleteDistribution() throws DatabaseException;
	
	/**
	 * Adds one territory to the list of those with complete distribution
	 * @param id
	 * @return
	 * @throws DatabaseException 
	 */
	public int setTerritoryWithCompleteDistribution(INodeKey id) throws DatabaseException;
	/**
	 * Removes one territory from the list of those with complete distribution
	 * @param id
	 * @return
	 * @throws DatabaseException 
	 */
	public int unsetTerritoryWithCompleteDistribution(INodeKey id) throws DatabaseException;
	/**
	 * Within the given territories, returns the subterriotories to which the taxon is restricted.
	 * Of course, territories for which there is no complete distribution will return null results.  
	 * @param territory
	 * @return
	 * @throws FloraOnException
	 */
	Map<String, Set<Territory>> getRestrictedTo(List<String> territory) throws FloraOnException;
	/**
	 * Returns the list of all native status departing from this TaxEnt 
	 * @return
	 * @throws DatabaseException
	 */
	public TaxEntAndNativeStatusResult getNativeStatusList(String territory) throws DatabaseException;
	void getSingleTerritoryEndemism() throws FloraOnException;
}
