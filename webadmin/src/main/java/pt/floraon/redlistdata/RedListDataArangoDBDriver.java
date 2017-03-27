package pt.floraon.redlistdata;

import com.arangodb.ArangoCursor;
import com.arangodb.ArangoDBException;
import com.arangodb.ArangoDatabase;
import com.arangodb.entity.CollectionEntity;
import com.arangodb.entity.CollectionType;
import com.arangodb.model.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import pt.floraon.driver.*;
import pt.floraon.redlistdata.dataproviders.ExternalDataProvider;
import pt.floraon.redlistdata.dataproviders.InternalDataProvider;
import pt.floraon.redlistdata.entities.AtomicTaxonPrivilege;
import pt.floraon.redlistdata.entities.RedListDataEntity;
import pt.floraon.redlistdata.dataproviders.FloraOnExternalDataProvider;
import pt.floraon.taxonomy.entities.TaxEnt;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * Created by miguel on 05-11-2016.
 */
public class RedListDataArangoDBDriver extends BaseFloraOnDriver implements IRedListDataDriver {
    final protected ArangoDatabase database;
    final private List<ExternalDataProvider> externalDataProviders = new ArrayList<>();

    public RedListDataArangoDBDriver(IFloraOn driver) {
        super(driver);
        database = (ArangoDatabase) driver.getDatabase();
    }

    @Override
    public List<String> getRedListTerritories() {
        List<String> redListTerritories = new ArrayList<>();
        // search for the red list datasets
        // each dataset is a collection with the name redlist_<territory>
        // <teritory> is the short name of the territory
        for(CollectionEntity ce : database.getCollections()) {
            if(ce.getName().startsWith("redlist_")) {
                String terr = ce.getName().substring(8);
                redListTerritories.add(terr);
            }
        }
        return redListTerritories;
    }

    @Override
    public void initializeRedListData(Properties properties) throws FloraOnException {
        try {
            for(String op : getPropertyList(properties, "occurrenceProvider")) {
                externalDataProviders.add(new FloraOnExternalDataProvider(new URL(op)));    // TODO use reflection
            }
        } catch (MalformedURLException e) {
            throw new FloraOnException(e.getMessage());
        }
        externalDataProviders.add(new InternalDataProvider(driver));
    }

    public List<ExternalDataProvider> getExternalDataProviders() {
        return externalDataProviders;
    }

    @Override
    public RedListDataEntity createRedListDataEntity(String territory, RedListDataEntity rlde) throws DatabaseException {
        try {
            return database.collection("redlist_" + territory).insertDocument(rlde, new DocumentCreateOptions().returnNew(true)).getNew();
        } catch (ArangoDBException e) {
            throw new DatabaseException(e.getMessage());
        }
    }

    @Override
    public RedListDataEntity getRedListDataEntity(String territory, INodeKey taxEntId) throws DatabaseException {
        try {
            Iterator<RedListDataEntity> it =database.query(AQLRedListQueries.getString("redlistdata.2", territory, taxEntId), null
                    , null, RedListDataEntity.class);
            if(it.hasNext()) return it.next();
            return null;
        } catch (ArangoDBException e) {
            throw new DatabaseException(e.getMessage());
        }
    }

    @Override
    public Set<String> getRedListTags(String territory) throws DatabaseException {
        Set<String> out = new HashSet<>();
        Object tmp;
        try {
            Iterator<Object> it = database.query(AQLRedListQueries.getString("redlistdata.5", territory), null
                    , null, Object.class);
            while(it.hasNext()) {
                tmp = it.next();
                if(tmp != null) out.add((String) tmp);
            }
        } catch (ArangoDBException e) {
            throw new DatabaseException(e.getMessage());
        }
        return out;
    }

    @Override
    public Iterator<AtomicTaxonPrivilege> getTaxonPrivilegesForAllUsers(String territory) throws DatabaseException {
        // fetch taxon-specific user privileges for each species / infraspecies. This is needed because
        // taxon-specific privileges may be assigned to higher taxa, so we must traverse the graph to get the species
        // or inferior.
        Iterator<AtomicTaxonPrivilege> apIt;
        try {
            apIt = database.query(AQLRedListQueries.getString("redlistdata.3", territory), null
                    , null, AtomicTaxonPrivilege.class);
        } catch (ArangoDBException e) {
            throw new DatabaseException(e.getMessage());
        }
        return apIt;
    }

    @Override
    public List<RedListDataEntity> getAllRedListData(String territory, boolean withTaxonSpecificPrivileges) throws FloraOnException {
        if(withTaxonSpecificPrivileges) {
            Iterator<AtomicTaxonPrivilege> apIt = getTaxonPrivilegesForAllUsers(territory);

            Iterator<RedListDataEntity> rldeIt;
            try {
                rldeIt = database.query(AQLRedListQueries.getString("redlistdata.1", territory), null
                        , null, RedListDataEntity.class);
            } catch (ArangoDBException e) {
                throw new DatabaseException(e.getMessage());
            }
            // now assign responsible authors
            return super.assignResponsibleAuthors(apIt, rldeIt);
        } else {
            try {
                return database.query(AQLRedListQueries.getString("redlistdata.1", territory), null
                        , null, RedListDataEntity.class).asListRemaining();
            } catch (ArangoDBException e) {
                throw new DatabaseException(e.getMessage());
            }
        }
    }

    @Override
    public Iterator<TaxEnt> getAllRedListTaxa(String territory) throws FloraOnException {
        try {
            return database.query(AQLRedListQueries.getString("redlistdata.7", territory), null
                    , null, TaxEnt.class);
        } catch (ArangoDBException e) {
            throw new DatabaseException(e.getMessage());
        }
    }

    @Override
    public void initializeRedListDataForTerritory(String territory) throws FloraOnException {
        final String collectionName = "redlist_" + territory;
        try {
            database.collection(collectionName).getInfo();
        } catch (ArangoDBException e) {
            try {
                database.createCollection(collectionName, new CollectionCreateOptions().type(CollectionType.DOCUMENT));
                database.collection(collectionName).createHashIndex(Arrays.asList("taxEntID"), new HashIndexOptions().unique(true).sparse(false));
            } catch (ArangoDBException e1) {
                throw new FloraOnException(e1.getMessage());
            }
        }

    }

    @Override
    public int updateRedListDataEntities(String territory, String[] taxEntIds, Map<String, Object> values) throws FloraOnException {
        Map<String, Object> bp = new HashMap<>();
        bp.put("ids", taxEntIds);
        bp.put("data", values);
        Gson gs = new GsonBuilder().setPrettyPrinting().create();
//        System.out.println(gs.toJson(bp));

        try {
            ArangoCursor<String> c = database.query(AQLRedListQueries.getString("redlistdata.4", territory), bp, new AqlQueryOptions().count(true), String.class);
            return c.getCount();
        } catch (ArangoDBException e) {
            throw new DatabaseException(e.getMessage());
        }
//        System.out.println(a);
    }

    @Override
    public void deleteRedListDataEntity(String territory, INodeKey taxonId) throws DatabaseException {
        String key;
        try {
            key = database.query(AQLRedListQueries.getString("redlistdata.6", territory, taxonId.getID()), null
                    , null, String.class).next();
        } catch (ArangoDBException e) {
            throw new DatabaseException(e.getMessage());
        }
        if(key == null) throw new DatabaseException("Some error occurred");
    }
}
