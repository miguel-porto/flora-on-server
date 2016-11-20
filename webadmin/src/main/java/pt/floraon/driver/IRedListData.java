package pt.floraon.driver;

import pt.floraon.redlistdata.OccurrenceProvider;
import pt.floraon.redlistdata.entities.RedListDataEntity;

import java.util.List;
import java.util.Properties;

/**
 * Created by miguel on 05-11-2016.
 */
public interface IRedListData {
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
     * @param territory The short name of the {@link pt.floraon.entities.Territory}
     */
    void initializeRedListDataForTerritory(String territory) throws FloraOnException;

    List<OccurrenceProvider> getOccurrenceProviders();

    /**
     * Stores in the DB a new red list data entity.
     * @param territory The territory shortName
     * @param rlde The {@link RedListDataEntity} object to store
     * @return
     * @throws DatabaseException
     */
    RedListDataEntity createRedListDataEntity(String territory, RedListDataEntity rlde) throws DatabaseException;

    /**
     * Updates a red list data entity with the fields of the passed object.
     * @param territory
     * @param id The ID of the red list data entity to update.
     * @param rlde The object containing the new values.
     * @param replace false to ignore null values. true to remove fields that are null.
     * @throws DatabaseException
     */
    void updateRedListDataEntity(String territory, INodeKey id, RedListDataEntity rlde, boolean replace) throws DatabaseException;

    /**
     * Fetches all red list data for the given territory
     * @param territory The territory shortName
     * @return
     * @throws FloraOnException
     */
    List<RedListDataEntity> getAllRedListTaxa(String territory) throws FloraOnException;

    /**
     * Gets the {@link RedListDataEntity} for the given TaxEnt and territory
     * @param territory Territory short name
     * @param id TaxEnt ID
     * @return
     */
    RedListDataEntity getRedListDataEntity(String territory, INodeKey id) throws DatabaseException;
}
