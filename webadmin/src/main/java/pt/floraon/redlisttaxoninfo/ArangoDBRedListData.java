package pt.floraon.redlisttaxoninfo;

import com.arangodb.ArangoDB;
import com.arangodb.ArangoDatabase;
import com.arangodb.entity.CollectionEntity;
import pt.floraon.driver.BaseFloraOnDriver;
import pt.floraon.driver.FloraOnException;
import pt.floraon.driver.IFloraOn;
import pt.floraon.driver.IRedListData;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Created by miguel on 05-11-2016.
 */
public class ArangoDBRedListData extends BaseFloraOnDriver implements IRedListData {
    final protected ArangoDB dbDriver;
    final protected ArangoDatabase database;
    final protected List<OccurrenceProvider> occurrenceProviders = new ArrayList<>();

    public ArangoDBRedListData(IFloraOn driver) {
        super(driver);
        dbDriver = (ArangoDB) driver.getDatabaseDriver();
        database = (ArangoDatabase) driver.getDatabase();
    }

    @Override
    public String[] initializeRedListData(Properties properties) throws FloraOnException {
        database.getCollections();

        try {
            occurrenceProviders.add(new FloraOnOccurrenceProvider(
                    new URL(properties.getProperty("floraon.occurrence.URL"))
            ));
        } catch (MalformedURLException e) {
            throw new FloraOnException(e.getMessage());
        }

        return new String[0];
    }

    @Override
    public List<OccurrenceProvider> getOccurrenceProviders() {
        return occurrenceProviders;
    }

    @Override
    public void initializeRedListDataForTerritory(String territory) {

    }
}
