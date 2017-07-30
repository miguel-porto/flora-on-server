package pt.floraon.driver.interfaces;

import pt.floraon.driver.DatabaseException;
import pt.floraon.driver.FloraOnException;
import pt.floraon.ecology.entities.Habitat;
import pt.floraon.redlistdata.entities.AtomicTaxonPrivilege;
import pt.floraon.taxonomy.entities.TaxEnt;
import pt.floraon.taxonomy.entities.Territory;
import pt.floraon.redlistdata.dataproviders.SimpleOccurrenceDataProvider;
import pt.floraon.redlistdata.entities.RedListDataEntity;

import java.util.*;

/**
 * Created by miguel on 05-11-2016.
 */
public interface IRedListDataDriver {
    /**
     * Prepares database for holding red list data and checks for which territories there is data
     */
    void initializeRedListData(Properties properties) throws FloraOnException;

    /**
     * Gets the territories for which there is red list data
     * @return
     */
    List<String> getRedListTerritories();
    /**
     * Initializes a new dataset to hold the data for the given territory. This must include all taxa existing in it,
     * along with the native status of each one in the territory
     * @param territory The short name of the {@link Territory}
     */
    void initializeRedListDataForTerritory(String territory) throws FloraOnException;

    /**
     * Note that this creates new instances of the data providers. It should be used once per thread.
     * @return
     */
    List<SimpleOccurrenceDataProvider> getSimpleOccurrenceDataProviders();

    /**
     * Stores in the DB a new red list data entity.
     * @param territory The territory shortName
     * @param rlde The {@link RedListDataEntity} object to store
     * @return
     * @throws DatabaseException
     */
    RedListDataEntity createRedListDataEntity(String territory, RedListDataEntity rlde) throws DatabaseException;

    /**
     * Updates an array of red list data entities with the specified values. Note that this function replaces the values
     * of all the fields present in the passed Map (i.e. it does not add new elements to arrays, but replaces the entire
     * array!)
     * @param territory
     * @param taxEntIds
     * @param values
     * @throws FloraOnException
     */
    int updateRedListDataEntities(String territory, String[] taxEntIds, Map<String, Object> values) throws FloraOnException;

    /**
     * Adds a tag to an array of red list data entities.
     * @param territory
     * @param taxEntIds
     * @param tag
     * @return
     * @throws FloraOnException
     */
    int addTagToRedListDataEntities(String territory, String[] taxEntIds, String tag) throws FloraOnException;

    /**
     * Deletes a data sheet and removes the taxon from the red list
     * @param territory
     * @param taxonId
     * @throws DatabaseException
     */
    void deleteRedListDataEntity(String territory, INodeKey taxonId) throws DatabaseException;

    /**
     * For all users, gets all taxon-specific privileges, disaggregated to species or inferior rank.
     * Note that in the database they may be assigned to higher taxa. This function must return species or inferior.
     * @param territory
     * @return
     * @throws DatabaseException
     */
    Iterator<AtomicTaxonPrivilege> getTaxonPrivilegesForAllUsers(String territory) throws DatabaseException;

    /**
     * Fetches all red list data for the given territory. Note this is memory intensive.
     * @param territory The territory shortName
     * @param withTaxonSpecificPrivileges True to compute who is responsible for texts, assessment, etc. for each taxon
     * @throws FloraOnException
     */
    Iterator<RedListDataEntity> getAllRedListData(String territory, boolean withTaxonSpecificPrivileges) throws FloraOnException;

    /**
     * Fetches all taxa included in the red list data for the given territory.
     * @param territory
     * @return
     * @throws FloraOnException
     */
    Iterator<TaxEnt> getAllRedListTaxa(String territory) throws FloraOnException;

    /**
     * Fetches all taxa included in the red list data for the given territory filtered by one tag.
     * @param territory
     * @return
     * @throws FloraOnException
     */
    Iterator<TaxEnt> getAllRedListTaxa(String territory, String filterTag) throws FloraOnException;

    /**
     * Gets the {@link RedListDataEntity} for the given TaxEnt and territory
     * @param territory Territory short name
     * @param taxonId TaxEnt ID
     * @return
     */
    RedListDataEntity getRedListDataEntity(String territory, INodeKey taxonId) throws DatabaseException;

    /**
     * Gets all tags in use in the red list dataset.
     * @return
     */
    Set<String> getRedListTags(String territory) throws DatabaseException;

    String buildRedListSheetCitation(RedListDataEntity rlde, Map<String, String> userMap);

    List<Habitat> getAllHabitats() throws DatabaseException;
}
