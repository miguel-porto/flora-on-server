package pt.floraon.redlistdata;

import com.arangodb.ArangoCollection;
import com.arangodb.ArangoCursor;
import com.arangodb.ArangoDBException;
import com.arangodb.ArangoDatabase;
import com.arangodb.entity.CollectionEntity;
import com.arangodb.entity.CollectionType;
import com.arangodb.model.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import pt.floraon.driver.*;
import pt.floraon.driver.interfaces.IFloraOn;
import pt.floraon.driver.interfaces.INodeKey;
import pt.floraon.driver.interfaces.IRedListDataDriver;
import pt.floraon.driver.utils.StringUtils;
import pt.floraon.ecology.entities.Habitat;
import pt.floraon.redlistdata.dataproviders.SimpleOccurrenceDataProvider;
import pt.floraon.redlistdata.dataproviders.InternalDataProvider;
import pt.floraon.redlistdata.entities.AtomicTaxonPrivilege;
import pt.floraon.redlistdata.entities.RedListDataEntity;
import pt.floraon.redlistdata.dataproviders.FloraOnDataProvider;
import pt.floraon.redlistdata.entities.RedListDataEntitySnapshot;
import pt.floraon.redlistdata.entities.RedListSettings;
import pt.floraon.taxonomy.entities.TaxEnt;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * Created by miguel on 05-11-2016.
 */
public class RedListDataArangoDBDriver extends BaseFloraOnDriver implements IRedListDataDriver {
    final protected ArangoDatabase database;
    final private List<ThreadLocal<SimpleOccurrenceDataProvider>> simpleOccurrenceDataProviders = new ArrayList<>();

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
            for(final String op : getPropertyList(properties, "occurrenceProvider")) {
                simpleOccurrenceDataProviders.add(new ThreadLocal<SimpleOccurrenceDataProvider>(){
                    @Override
                    protected SimpleOccurrenceDataProvider initialValue() {
                        try {
                            return new FloraOnDataProvider(new URL(op), driver);    // TODO use reflection
                        } catch (MalformedURLException e) {
                            return null;
                        }
                    }
                });
            }
//                simpleOccurrenceDataProviders.add(new FloraOnDataProvider(new URL(op), driver));

        simpleOccurrenceDataProviders.add(new ThreadLocal<SimpleOccurrenceDataProvider>(){
            @Override
            protected SimpleOccurrenceDataProvider initialValue() {
                return new InternalDataProvider(driver);
            }
        });
    }

    @Override
    public Map<String, RedListSettings> getRedListSettings(String territory) throws FloraOnException {
        Map<String, Object> bindVars;
        Iterator<RedListSettings> it;
        Map<String, RedListSettings> out = new HashMap<>();
        try {
            if(territory == null) {
                it = database.query(AQLRedListQueries.getString("redlistdata.9a"), null
                        , null, RedListSettings.class);
            } else {
                bindVars = new HashMap<>();
                bindVars.put("terr", territory);
                it = database.query(AQLRedListQueries.getString("redlistdata.9"), bindVars
                        , null, RedListSettings.class);
            }
        } catch (ArangoDBException e) {
            throw new DatabaseException(e.getMessage());
        }
        while(it.hasNext()) {
            RedListSettings rls = it.next();
            out.put(rls.getTerritory(), rls);
        }
        return out;
    }

    public List<SimpleOccurrenceDataProvider> getSimpleOccurrenceDataProviders() {
        List<SimpleOccurrenceDataProvider> out = new ArrayList<>();
        for(ThreadLocal<SimpleOccurrenceDataProvider> sodp : simpleOccurrenceDataProviders)
            out.add(sodp.get());
        return out;
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
    public RedListDataEntitySnapshot getRedListDataEntityAsSnapshot(String territory, INodeKey taxEntId) throws DatabaseException {
        try {
            Iterator<RedListDataEntitySnapshot> it = database.query(AQLRedListQueries.getString("redlistdata.2", territory, taxEntId), null
                    , null, RedListDataEntitySnapshot.class);
            if(it.hasNext()) return it.next();
            return null;
        } catch (ArangoDBException e) {
            throw new DatabaseException(e.getMessage());
        }
    }

    @Override
    public void saveRedListDataEntitySnapshot(String territory, RedListDataEntitySnapshot rldes) {
        String cname = "redlist_snapshots_" + territory;
        try {
            database.collection(cname).getInfo();
        } catch (ArangoDBException e) {
            database.createCollection(cname, new CollectionCreateOptions().type(CollectionType.DOCUMENT));
        }

        ArangoCollection c = database.collection(cname);
        rldes.updateDateSaved();
        rldes.setKey(null);
        rldes.setID(null);
        c.insertDocument(rldes);
    }

    @Override
    public Iterator<RedListDataEntitySnapshot> getSnapshots(String territory, INodeKey taxEntId) throws DatabaseException {
        String cname = "redlist_snapshots_" + territory;
        try {
            database.collection(cname).getInfo();
        } catch (ArangoDBException e) {
            return Collections.emptyIterator();
        }

        Map<String, Object> bindVars = new HashMap<>();
        bindVars.put("@collection", cname);
        bindVars.put("id", taxEntId.getID());

        try {
            return database.query(AQLRedListQueries.getString("redlistdata.10"), bindVars
                    , null, RedListDataEntitySnapshot.class);
        } catch (ArangoDBException e) {
            throw new DatabaseException(e.getMessage());
        }

    }

    @Override
    public Iterator<RedListDataEntitySnapshot> getSnapshotsByPublicationStatus(String territory, RedListEnums.PublicationStatus status) throws DatabaseException {
        String cname = "redlist_snapshots_" + territory;
        Map<String, Object> bindVars = new HashMap<>();
        bindVars.put("@collection", cname);
        bindVars.put("status", status.toString());

        try {
            return database.query(AQLRedListQueries.getString("redlistdata.11"), bindVars
                    , null, RedListDataEntitySnapshot.class);
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
    public Iterator<RedListDataEntity> getAllRedListData(String territory, boolean withTaxonSpecificPrivileges, String[] filterByTags) throws FloraOnException {
        String query;
        Map<String, Object> bindVars = new HashMap<>();
        bindVars.put("@collection", "redlist_" + territory);

        if(StringUtils.isArrayEmpty(filterByTags))
            query = "redlistdata.1";
        else {
            query = "redlistdata.1a";
            bindVars.put("tags", filterByTags);
        }

        if(withTaxonSpecificPrivileges) {
            Iterator<AtomicTaxonPrivilege> apIt = getTaxonPrivilegesForAllUsers(territory);
            Iterator<RedListDataEntity> rldeIt;
            try {
                rldeIt = database.query(AQLRedListQueries.getString(query), bindVars
                        , new AqlQueryOptions().ttl(8 * 60), RedListDataEntity.class);
            } catch (ArangoDBException e) {
                throw new DatabaseException(e.getMessage());
            }
            // now assign responsible authors
            return super.assignResponsibleAuthors(apIt, rldeIt);
        } else {
            try {
                return database.query(AQLRedListQueries.getString(query), bindVars
                        , new AqlQueryOptions().ttl(8 * 60), RedListDataEntity.class);
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
    public Iterator<TaxEnt> getAllRedListTaxa(String territory, String filterTag) throws FloraOnException {
        try {
            return database.query(AQLRedListQueries.getString("redlistdata.7a", territory, filterTag), null
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
                database.collection(collectionName).ensureHashIndex(Collections.singletonList("taxEntID"), new HashIndexOptions().unique(true).sparse(false));
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
//        Gson gs = new GsonBuilder().setPrettyPrinting().create();
//        System.out.println(gs.toJson(bp));

        try {
            ArangoCursor<String> c = database.query(AQLRedListQueries.getString("redlistdata.4", territory), bp, new AqlQueryOptions().count(true), String.class);
            return c.getCount();
        } catch (ArangoDBException e) {
            throw new DatabaseException(e.getMessage());
        }
//        System.out.println(a);
    }

    public int addTagToRedListDataEntities(String territory, String[] taxEntIds, String tag) throws FloraOnException {
        Map<String, Object> bp = new HashMap<>();
        bp.put("ids", taxEntIds);
        Gson gs = new GsonBuilder().setPrettyPrinting().create();
/*
        System.out.println(gs.toJson(bp));
        System.out.println(AQLRedListQueries.getString("redlistdata.8", territory, tag));
*/
        try {
            ArangoCursor<String> c = database.query(AQLRedListQueries.getString("redlistdata.8", territory, tag)
                    , bp, new AqlQueryOptions().count(true), String.class);
            return c.getCount();
        } catch (ArangoDBException e) {
            throw new DatabaseException(e.getMessage());
        }
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

    @Override
    public List<Habitat> getAllHabitats() throws DatabaseException {
        return driver.getListDriver().getAllDocumentsOfCollectionAsList(Constants.NodeTypes.habitat.toString(), Habitat.class);
    }
}
