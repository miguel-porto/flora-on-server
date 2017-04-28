package pt.floraon.occurrences.arangodb;

import com.arangodb.ArangoCursor;
import com.arangodb.ArangoDBException;
import com.arangodb.ArangoDatabase;
import com.arangodb.model.AqlQueryOptions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.collections.iterators.EmptyIterator;
import pt.floraon.authentication.entities.User;
import pt.floraon.driver.*;
import pt.floraon.driver.utils.BeanUtils;
import pt.floraon.driver.utils.StringUtils;
import pt.floraon.occurrences.entities.Inventory;
import pt.floraon.occurrences.entities.newOBSERVED_IN;
import pt.floraon.redlistdata.AQLRedListQueries;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * Created by miguel on 24-03-2017.
 */
public class OccurrenceArangoDriver extends GOccurrenceDriver implements IOccurrenceDriver {
    private ArangoDatabase database;

    public OccurrenceArangoDriver(IFloraOn driver) {
        super(driver);
        this.database = (ArangoDatabase) driver.getDatabase();
    }

    @Override
    public void createInventory(Inventory inventory) {
        database.collection(Constants.NodeTypes.inventory.toString()).insertDocument(inventory);
    }

    @Override
    public Iterator<Inventory> getOccurrencesOfTaxon(INodeKey taxEntId) throws DatabaseException {
        // TODO: this should return the OBSERVED_IN graph links, not the unmatched
        // FIXME must traverse to infrataxa!!!
        try {
            return database.query(
                    AQLOccurrenceQueries.getString("occurrencequery.1", taxEntId.getID())
                    , null, null, Inventory.class);
        } catch (ArangoDBException e) {
            throw new DatabaseException(e.getMessage());
        }
    }

    @Override
    public Iterator<Inventory> getOccurrencesByUuid(INodeKey authorId, String[] uuid) throws DatabaseException {
        // TODO: this should return the OBSERVED_IN graph links, not the unmatched
        if(uuid == null || uuid.length == 0) return Collections.emptyIterator();
        try {
            return database.query(
                    AQLOccurrenceQueries.getString("occurrencequery.2b", authorId.getID()
                            , "[\"" + StringUtils.implode("\",\"", uuid) + "\"]")
                    , null, null, Inventory.class);
        } catch (ArangoDBException e) {
            throw new DatabaseException(e.getMessage());
        }
    }


    @Override
    public Iterator<Inventory> getOccurrencesOfObserver(INodeKey authorId, Integer offset, Integer count) throws DatabaseException {
        if(offset == null) offset = 0;
        if(count == null) count = 999999;
        try {
            return database.query(
                    AQLOccurrenceQueries.getString("occurrencequery.2", authorId.getID(), offset, count)
                    , null, null, Inventory.class);
        } catch (ArangoDBException e) {
            throw new DatabaseException(e.getMessage());
        }
    }

    @Override
    public Iterator<Inventory> getOccurrencesOfMaintainer(INodeKey authorId, Integer offset, Integer count) throws DatabaseException {
        if(offset == null) offset = 0;
        if(count == null) count = 999999;
        try {
            return database.query(
                    AQLOccurrenceQueries.getString("occurrencequery.4", authorId.getID(), offset, count)
                    , null, null, Inventory.class);
        } catch (ArangoDBException e) {
            throw new DatabaseException(e.getMessage());
        }
    }

    @Override
    public Iterator<Inventory> getInventoriesOfObserver(INodeKey authorId, Integer offset, Integer count) throws DatabaseException {
        if(offset == null) offset = 0;
        if(count == null) count = 999999;
        try {
            return database.query(
                    AQLOccurrenceQueries.getString("occurrencequery.2a", authorId.getID(), offset, count)
                    , null, null, Inventory.class);
        } catch (ArangoDBException e) {
            throw new DatabaseException(e.getMessage());
        }
    }

    @Override
    public Iterator<Inventory> getInventoriesOfMaintainer(INodeKey authorId, Integer offset, Integer count) throws DatabaseException {
        if(offset == null) offset = 0;
        if(count == null) count = 999999;
        try {
            return database.query(
                    AQLOccurrenceQueries.getString("occurrencequery.4a", authorId.getID(), offset, count)
                    , null, null, Inventory.class);
        } catch (ArangoDBException e) {
            throw new DatabaseException(e.getMessage());
        }
    }

    @Override
    public Iterator<Inventory> getInventoriesByIds(String[] inventoryIds) throws DatabaseException {
        Map<String, Object> bp = new HashMap<>();
        bp.put("ids", inventoryIds);
//        Gson gs = new GsonBuilder().setPrettyPrinting().create();
        ArangoCursor<Inventory> c;
        try {
             c = database.query(AQLOccurrenceQueries.getString("occurrencequery.5")
                    , bp, new AqlQueryOptions().count(true), Inventory.class);
        } catch (ArangoDBException e) {
            throw new DatabaseException(e.getMessage());
        }
        return c;
    }

    @Override
    public boolean discardUploadedTable(INodeKey authorId, String filename) throws FloraOnException {
        User user = driver.getAdministration().getUser(authorId);
        List<String> tmp = user.getUploadedTables();
        if(!tmp.contains(filename)) return false;
        tmp.remove(filename);
        driver.getNodeWorkerDriver().updateDocument(authorId, "uploadedTables", tmp);
        return true;
    }

    @Override
    public int deleteInventoriesOrOccurrences(String[] inventoryId, String[] uuid) throws FloraOnException {
        int count = 0;
        for (int i = 0; i < inventoryId.length; i++) {
            try {
                if(StringUtils.isArrayEmpty(uuid) || uuid[i].trim().equals("")) {
//                    driver.getNodeWorkerDriver().deleteDocument(driver.asNodeKey(inventoryId[i]));  // FIXME: check for connected links
                    driver.getNodeWorkerDriver().deleteVertexOrEdge(driver.asNodeKey(inventoryId[i]));
                } else {
                    Inventory inv = database.query(
                            AQLOccurrenceQueries.getString("occurrencequery.3", inventoryId[i], uuid[i])
                            , null, null, Inventory.class).next();
                    // if the inventory became empty, delete the inventory
                    if(inv._getOccurrences().size() == 0)
                        driver.getNodeWorkerDriver().deleteDocument(driver.asNodeKey(inventoryId[i]));
                }
            } catch (ArangoDBException e) {
                e.printStackTrace();
                continue;
            }
            count++;
        }
        return count;
    }

    @Override
    public Inventory updateInventory(Inventory inv) throws FloraOnException {
/*
        if(inv._getOccurrences().size() == 0)
            return driver.getNodeWorkerDriver().updateDocument(driver.asNodeKey(inv.getID()), inv, false, Inventory.class);
        else {
*/

            Inventory tmp = driver.getNodeWorkerDriver().getNode(driver.asNodeKey(inv.getID()), Inventory.class);
            Map<UUID, newOBSERVED_IN> origMap = new HashMap<>();
            Set<UUID> alreadUpdated = new HashSet<>();
            for(newOBSERVED_IN occ : tmp._getOccurrences())
                origMap.put(occ.getUuid(), occ);

            Map<UUID, newOBSERVED_IN> updMap = new HashMap<>(origMap);

            for(newOBSERVED_IN occ : inv._getOccurrences()) {
                if(origMap.containsKey(occ.getUuid())) {
                    if(alreadUpdated.contains(occ.getUuid())) { // this occurrence was already updated, so add new and copy
                        UUID newUuid = UUID.randomUUID();
                        newOBSERVED_IN newOcc;
                        try {
                            newOcc = BeanUtils.updateBean(newOBSERVED_IN.class, null, origMap.get(occ.getUuid()), occ);
                            newOcc.setUuid(newUuid);
                        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                            e.printStackTrace();
                            throw new FloraOnException(e.getMessage());
                        }
                        updMap.put(newUuid, newOcc);
                    } else {
                        try {
                            updMap.put(occ.getUuid()
                                    , BeanUtils.updateBean(newOBSERVED_IN.class, null, origMap.get(occ.getUuid()), occ));
                        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                            e.printStackTrace();
                            throw new FloraOnException(e.getMessage());
                        }
                    }
                } else
                    updMap.put(occ.getUuid(), occ);
                alreadUpdated.add(occ.getUuid());
            }

            inv.setUnmatchedOccurrences(new ArrayList<>(updMap.values()));
            return driver.getNodeWorkerDriver().updateDocument(driver.asNodeKey(inv.getID()), inv, false, Inventory.class);
       // }
    }
}