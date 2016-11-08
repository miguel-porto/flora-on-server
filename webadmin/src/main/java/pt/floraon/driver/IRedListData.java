package pt.floraon.driver;

import pt.floraon.redlisttaxoninfo.OccurrenceProvider;

import java.net.MalformedURLException;
import java.util.List;
import java.util.Properties;

/**
 * Created by miguel on 05-11-2016.
 */
public interface IRedListData {
    /**
     * Prepares database for holding red list data
     * @return An array of territory short names for which red list data exists.
     */
    String[] initializeRedListData(Properties properties) throws FloraOnException;

    /**
     * Initializes a new dataset to hold the data for the given territory. This must include all taxa existing in it,
     * along with the native status of each one in the territory
     * @param territory The short name of the {@link pt.floraon.entities.Territory}
     */
    void initializeRedListDataForTerritory(String territory);

    List<OccurrenceProvider> getOccurrenceProviders();
}
