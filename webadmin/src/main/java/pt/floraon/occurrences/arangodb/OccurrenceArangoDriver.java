package pt.floraon.occurrences.arangodb;

import com.arangodb.ArangoCursor;
import com.arangodb.ArangoDB;
import com.arangodb.ArangoDBException;
import com.arangodb.ArangoDatabase;
import com.arangodb.model.AqlQueryOptions;
import com.arangodb.util.ArangoSerializer;
import com.google.gson.Gson;
import jline.internal.Log;
import org.apache.commons.lang.mutable.MutableInt;
import pt.floraon.authentication.entities.User;
import pt.floraon.driver.*;
import pt.floraon.driver.datatypes.IntegerInterval;
import pt.floraon.driver.datatypes.NumericInterval;
import pt.floraon.driver.interfaces.IFloraOn;
import pt.floraon.driver.interfaces.INodeKey;
import pt.floraon.driver.interfaces.IOccurrenceDriver;
import pt.floraon.driver.utils.BeanUtils;
import pt.floraon.driver.utils.StringUtils;
import pt.floraon.geometry.LatLongCoordinate;
import pt.floraon.geometry.Precision;
import pt.floraon.occurrences.OccurrenceConstants;
import pt.floraon.occurrences.TaxonomicChange;
import pt.floraon.occurrences.entities.Inventory;
import pt.floraon.occurrences.entities.OBSERVED_IN;
import pt.floraon.occurrences.entities.Occurrence;
import pt.floraon.occurrences.entities.TaxEntObservation;
import pt.floraon.occurrences.fields.FieldReflection;
import pt.floraon.occurrences.fields.parsers.DateParser;
import pt.floraon.taxonomy.entities.TaxEnt;
import pt.floraon.taxonomy.entities.TaxEntMatch;
import pt.floraon.taxonomy.entities.TaxonName;

import java.lang.reflect.InvocationTargetException;
import java.text.DateFormat;
import java.util.*;
import java.util.List;

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
        if(inventory.shouldGetCoordinatesFromObservation()) {
            LatLongCoordinate llc = inventory._getUniqueCoordinate(false);
            inventory.setLatitude(llc.getLatitude());
            inventory.setLongitude(llc.getLongitude());
            for (OBSERVED_IN oi : inventory._getTaxa()) {
                oi.setObservationLatitude(null);
                oi.setObservationLongitude(null);
            }
        }
        database.collection(Constants.NodeTypes.inventory.toString()).insertDocument(inventory);
    }

    @Override
    public Iterator<Occurrence> getOccurrencesOfTaxon(INodeKey taxEntId) throws DatabaseException {
        // TODO: see occurrencequery.1 for the TODO
        Map<String, Object> bindVars = new HashMap<>();
        bindVars.put("id", taxEntId.toString());

        try {
            return database.query(
                    AQLOccurrenceQueries.getString("occurrencequery.1")
                    , bindVars, new AqlQueryOptions().rules(Collections.singleton("-splice-subqueries")), Occurrence.class);
        } catch (ArangoDBException e) {
            throw new DatabaseException(e.getMessage());
        }
    }

    @Override
    public Iterator<Inventory> getUnmatchedOccurrences() throws DatabaseException {
        try {
            return database.query(
                    AQLOccurrenceQueries.getString("occurrencequery.6")
                    , null, null, Inventory.class);
        } catch (ArangoDBException e) {
            throw new DatabaseException(e.getMessage());
        }
    }

    @Override
    public int getUnmatchedOccurrencesOfMaintainerCount(INodeKey userId) throws DatabaseException {
        String query = userId == null ? "occurrencequery.6a.nouser.count" : "occurrencequery.6a.count";
        try {
            return database.query(
                    AQLOccurrenceQueries.getString(query, userId == null ? null : userId.toString())
                    , null, null, Integer.class).next();
        } catch (ArangoDBException e) {
            throw new DatabaseException(e.getMessage());
        }
    }

    @Override
    public Iterator<Inventory> getUnmatchedOccurrencesOfMaintainer(INodeKey userId) throws DatabaseException {
        String query = (userId == null ? "occurrencequery.6b" : "occurrencequery.6a");
        try {
            return database.query(
                    AQLOccurrenceQueries.getString(query, userId == null ? null : userId.toString())
                    , null, new AqlQueryOptions().ttl(120), Inventory.class);
        } catch (ArangoDBException e) {
            throw new DatabaseException(e.getMessage());
        }
    }

    @Override
    public Iterator<Inventory> getOccurrencesByUuid(INodeKey authorId, String[] uuid) throws DatabaseException {
        // TODO: this should return the OBSERVED_IN graph links, not the unmatched
        if(uuid == null || uuid.length == 0) return Collections.emptyIterator();
        Map<String, Object> bindVars = new HashMap<>();
        bindVars.put("uuid", uuid);
        bindVars.put("user", authorId.getID());
        try {
            return database.query(
                    AQLOccurrenceQueries.getString("occurrencequery.2ba")
                    , bindVars, null, Inventory.class);
        } catch (ArangoDBException e) {
            throw new DatabaseException(e.getMessage());
        }
    }

    @Override
    public Inventory getOccurrenceByUuid(String uuid) throws DatabaseException {
        if(StringUtils.isStringEmpty(uuid)) return null;
        Map<String, Object> bindVars = new HashMap<>();
        bindVars.put("uuid", uuid);
        try {
            Iterator<Inventory> it = database.query(
                    AQLOccurrenceQueries.getString("occurrencequery.2b")    // TODO: optimize with index
                    , bindVars, null, Inventory.class);
            if(it.hasNext()) return it.next(); else return null;
        } catch (ArangoDBException e) {
            throw new DatabaseException(e.getMessage());
        }
    }

    @Override
    public Iterator<Occurrence> getOccurrencesOfObserverWithinDates(INodeKey authorId, Date from, Date to, Integer offset, Integer count) throws DatabaseException {
        if(authorId == null) return getOccurrencesOfMaintainer(null, null, false, offset, count);
        DateFormat df = Constants.dateFormatYMD.get();
        Map<String, Object> bindVars = new HashMap<>();
        bindVars.put("observer", authorId.toString());
        bindVars.put("off", offset == null ? 0 : offset);
        bindVars.put("cou", count == null ? 999999 : count);
        bindVars.put("from", df.format(from));
        bindVars.put("to", df.format(to));

        try {
            return database.query(
                    AQLOccurrenceQueries.getString("occurrencequery.2.date")
                    , bindVars, null, Occurrence.class);
        } catch (ArangoDBException e) {
            throw new DatabaseException(e.getMessage());
        }
    }

    @Override
    public Iterator<Occurrence> getOccurrencesOfObserver(INodeKey authorId, AbstractMap.SimpleEntry<String, Boolean> orderField, boolean returnObserverNames, Integer offset, Integer count) throws DatabaseException {
        return getOccurrencesOfUser(authorId, true, orderField, returnObserverNames, offset, count);
    }

    @Override
    public Iterator<Occurrence> getOccurrencesOfUser(INodeKey authorId, boolean asObserver, AbstractMap.SimpleEntry<String, Boolean> orderField, boolean returnObserverNames, Integer offset, Integer count) throws DatabaseException {
        Map<String, Object> bindVars = new HashMap<>();
        if(authorId != null) bindVars.put("user", authorId.toString());
        bindVars.put("off", offset == null ? 0 : offset);
        bindVars.put("cou", count == null ? 999999 : count);

        String filter = authorId == null ? "" : (asObserver ? "FILTER i.observers ANY == @user" : "FILTER i.maintainer == @user");
        String sortExpression = buildSortOrderExpression(orderField);
        String query = returnObserverNames ? AQLOccurrenceQueries.getString("occurrencequery.4.observernames", sortExpression, filter)
                : AQLOccurrenceQueries.getString("occurrencequery.4", sortExpression, filter);
        try {
            return database.query(query, bindVars, null, Occurrence.class);
        } catch (ArangoDBException e) {
            throw new DatabaseException(e.getMessage());
        }
    }

    @Override
    public Iterator<Occurrence> getOccurrencesOfMaintainer(INodeKey authorId, AbstractMap.SimpleEntry<String, Boolean> orderField, boolean returnObserverNames, Integer offset, Integer count) throws DatabaseException {
        return getOccurrencesOfUser(authorId, false, orderField, returnObserverNames, offset, count);
    }

    @Override
    public int getOccurrencesOfUserCount(INodeKey authorId, boolean asObserver) throws DatabaseException {
        Map<String, Object> bindVars = new HashMap<>();
        String filter = authorId == null ? "" : (asObserver ? "FILTER i.observers ANY == @user" : "FILTER i.maintainer == @user");
        if(authorId != null) {
            bindVars.put("user", authorId.getID());
        }

        String query = AQLOccurrenceQueries.getString("occurrencequery.4b", filter);
        try {
            return database.query(query, bindVars, null, Integer.class).next();
        } catch (ArangoDBException e) {
            throw new DatabaseException(e.getMessage());
        }
    }

    @Override
    public int getOccurrencesOfMaintainerCount(INodeKey authorId) throws DatabaseException {
        return getOccurrencesOfUserCount(authorId, false);
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
    public Iterator<Inventory> getInventoriesOfMaintainer(INodeKey authorId, AbstractMap.SimpleEntry<String, Boolean> orderField, Integer offset, Integer count) throws DatabaseException {
        return getInventoriesOfUser(authorId, false, orderField, offset, count);
    }

    @Override
    public Iterator<Inventory> getInventoriesOfUser(INodeKey authorId, boolean asObserver, AbstractMap.SimpleEntry<String, Boolean> orderField, Integer offset, Integer count) throws DatabaseException {
        Map<String, Object> bindVars = new HashMap<>();
        if(offset == null) offset = 0;
        if(count == null) count = 999999;
        bindVars.put("offset", offset);
        bindVars.put("count", count);
        String filter = authorId == null ? "" : (asObserver ? "FILTER i.observers ANY == @user" : "FILTER i.maintainer == @user");
        if(authorId != null) {
            bindVars.put("user", authorId.getID());
        }

        String sortExpression = buildSortOrderExpression(orderField);
        String query = AQLOccurrenceQueries.getString("occurrencequery.4a", sortExpression, filter);
Log.info(query);
        try {
            return database.query(query, bindVars, null, Inventory.class);
        } catch (ArangoDBException e) {
            throw new DatabaseException(e.getMessage());
        }
    }

    @Override
    public int getInventoriesOfUserCount(INodeKey authorId, boolean asObserver) throws DatabaseException {
        Map<String, Object> bindVars = new HashMap<>();
        String filter = authorId == null ? "" : (asObserver ? "FILTER i.observers ANY == @user" : "FILTER i.maintainer == @user");
        if(authorId != null) bindVars.put("user", authorId.getID());

        String query = AQLOccurrenceQueries.getString("occurrencequery.4a.count", filter);

        try {
            return database.query(query, bindVars, null, Integer.class).next();
        } catch (ArangoDBException e) {
            throw new DatabaseException(e.getMessage());
        }
    }

    @Override
    public int getInventoriesOfMaintainerCount(INodeKey authorId) throws DatabaseException {
        return getInventoriesOfUserCount(authorId, false);
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
    public Inventory updateOccurrence(String uuid, Inventory inv) throws FloraOnException {
        if(inv._getTaxa().length > 1) throw new FloraOnException("Inventory to update has more than one occurrence");
        Map<String, Object> bindVars = new HashMap<>();
        bindVars.put("uuid", uuid);
        // we have to serialize manually so we can opt out from serializing null values
        bindVars.put("json", ((ArangoDB) driver.getDatabaseDriver()).util().serialize(inv, new ArangoSerializer.Options().serializeNullValues(false)));
        String query = AQLOccurrenceQueries.getString("occurrencequery.8");
//Log.info(new Gson().toJson(inv));
        try {
/*
            Log.info(query);
            Log.info(new Gson().toJson(bindVars));
*/
            return database.query(query, bindVars, null, Inventory.class).next();
        } catch (ArangoDBException e) {
            throw new DatabaseException(e.getMessage());
        }
    }

    @Override
    public Inventory updateInventory(Inventory newinv) throws FloraOnException {
/*
        if(inv._getOccurrences().size() == 0)
            return driver.getNodeWorkerDriver().updateDocument(driver.asNodeKey(inv.getID()), inv, false, Inventory.class);
        else {
*/
// TODO HERE changed coords
            Inventory original = driver.getNodeWorkerDriver().getDocument(driver.asNodeKey(newinv.getID()), Inventory.class);
            Map<UUID, OBSERVED_IN> origMap = new HashMap<>();
            Set<UUID> alreadUpdated = new HashSet<>();
            for(OBSERVED_IN occ : original._getOccurrences())
                origMap.put(occ.getUuid(), occ);

            Map<UUID, OBSERVED_IN> updMap = new HashMap<>(origMap);

            for(OBSERVED_IN eachNewOcc : newinv._getOccurrences()) {
                if(origMap.containsKey(eachNewOcc.getUuid())) {    // the original inventory contains this occurrence
                    if(alreadUpdated.contains(eachNewOcc.getUuid())) { // this occurrence was already updated, so add new and copy
                        UUID newUuid = UUID.randomUUID();
                        OBSERVED_IN newOcc;
                        eachNewOcc.setDateUpdated(new Date());
                        eachNewOcc.setDateInserted(null);
                        try {
                            newOcc = BeanUtils.updateBean(OBSERVED_IN.class, null, origMap.get(eachNewOcc.getUuid()), eachNewOcc);
                            newOcc.setUuid(newUuid);
                        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                            e.printStackTrace();
                            throw new FloraOnException(e.getMessage());
                        }
                        updMap.put(newUuid, newOcc);
                    } else {    // update existing occurrence
                        try {
                            OBSERVED_IN tmpOriginalOccurrence = origMap.get(eachNewOcc.getUuid());
//                            System.out.println(original._getLatitude()+", "+original._getLongitude()+", "+tmpori.getObservationLatitude()+", "+tmpori.getObservationLongitude()+", "+eachNewOcc.getObservationLatitude()+", "+eachNewOcc.getObservationLongitude());
                            if(tmpOriginalOccurrence.getObservationLatitude() == null || tmpOriginalOccurrence.getObservationLongitude() == null) {
                                if(original._getLatitude() != null && original._getLongitude() != null
                                        && eachNewOcc.getObservationLatitude() != null && eachNewOcc.getObservationLongitude() != null) {
                                    eachNewOcc.setCoordinatesChanged(true);
                                }
                            } else {
                                if (eachNewOcc.getObservationLatitude() != null && eachNewOcc.getObservationLongitude() != null)
                                    eachNewOcc.setCoordinatesChanged(true);
                            }
                            eachNewOcc.setDateUpdated(new Date());
                            // this occurrence is being updated, so leave the dateInserted intact. Only update de dateUpdated.
                            eachNewOcc.setDateInserted(null);

                            updMap.put(eachNewOcc.getUuid()
                                    , BeanUtils.updateBean(OBSERVED_IN.class, null, tmpOriginalOccurrence, eachNewOcc));
                        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                            e.printStackTrace();
                            throw new FloraOnException(e.getMessage());
                        }
                    }
                } else  // this occurrence is new
                    updMap.put(eachNewOcc.getUuid(), eachNewOcc);
                alreadUpdated.add(eachNewOcc.getUuid());
            }

            newinv.setUnmatchedOccurrences(new ArrayList<>(updMap.values()));
//            System.out.println(new Gson().toJson(newinv));
            // if the occurrences share the same coordinate, then move it to the inventory and clean the occurrences'
            if(newinv.shouldGetCoordinatesFromObservation()) {
                LatLongCoordinate llc = newinv._getUniqueCoordinate(false);
                if(Constants.isNullOrNoData(original.getLatitude()) || Constants.isNullOrNoData(original.getLongitude())
                        || new LatLongCoordinate(original.getLatitude(), original.getLongitude()).equals(llc)) {
                    Log.info("Move coord to inv");
                    newinv.setLatitude(llc.getLatitude());
                    newinv.setLongitude(llc.getLongitude());
                    for (OBSERVED_IN oi : newinv._getTaxa()) {
                        oi.setObservationLatitude(null);
                        oi.setObservationLongitude(null);
                    }
                }
            }

            return driver.getNodeWorkerDriver().updateDocument(driver.asNodeKey(newinv.getID()), newinv, false, Inventory.class);
       // }
    }

    @Override
    public void replaceTaxEntMatch(Map<String, TaxonomicChange> changes) throws FloraOnException {
//        Gson gs = new GsonBuilder().setPrettyPrinting().create();
//        System.out.println(gs.toJson(changes));
        Map<String, Object> bindVars;
        String tmp;

        for(Map.Entry<String, TaxonomicChange> change : changes.entrySet()) {
            bindVars = new HashMap<>();
            if(change.getValue().getUserId() != null) bindVars.put("user", change.getValue().getUserId());
            bindVars.put("uuids", change.getValue().getUuids());
            if(change.getValue().getTargetTaxEntId().equals("NM"))
                tmp = "";
            else if(change.getValue().getTargetTaxEntId().equals("NA")) {
                //TaxEnt te = driver.getNodeWorkerDriver().createTaxEntFromTaxEnt(TaxEnt.parse(change.getKey()));
                TaxEnt te;
                try {   // try to parse species or inferior
                    te = driver.getNodeWorkerDriver().createTaxEntFromTaxEnt(new TaxEnt(new TaxonName(change.getKey())));
                } catch (TaxonomyException tex) {   // if not, fall back to old parser
                    te = driver.getNodeWorkerDriver().createTaxEntFromTaxEnt(TaxEnt.parse(change.getKey()));
                }
                tmp = te.getID();
            } else
                tmp = change.getValue().getTargetTaxEntId();
            bindVars.put("replace", tmp);

            try {
                database.query(
                        AQLOccurrenceQueries.getString(change.getValue().getUserId() == null ? "occurrencequery.7a" : "occurrencequery.7")
                        , bindVars, null, null);
            } catch (ArangoDBException e) {
                e.printStackTrace();
                throw new DatabaseException(e.getMessage());
            }
        }
    }

    private String processInventoryFilters(Map<String, String> filter, Map<String, Object> bindVars) throws FloraOnException {
        StringBuilder inventoryFilter = new StringBuilder();
        if(filter.containsKey("date")) {
            if(filter.get("date").toUpperCase().equals("NA"))
                inventoryFilter.append(AQLOccurrenceQueries.getString("filter.nulldate")).append(" ");
            else {
                Integer[] date;
                try {
                    date = DateParser.parseDate(filter.get("date"));
                } catch (IllegalArgumentException e) {
                    throw new FloraOnException(e.getMessage());
                }
                Log.info("DATE: "+ new Gson().toJson(date));
                if(date.length == 5) {  // single date
                    if (!Constants.isNullOrNoData(date[0])) {
                        inventoryFilter.append(AQLOccurrenceQueries.getString("filter.day")).append(" ");
                        bindVars.put("day", date[0]);
                    }

                    if (!Constants.isNullOrNoData(date[1])) {
                        inventoryFilter.append(AQLOccurrenceQueries.getString("filter.month")).append(" ");
                        bindVars.put("month", date[1]);
                    }

                    if (!Constants.isNullOrNoData(date[2])) {
                        inventoryFilter.append(AQLOccurrenceQueries.getString("filter.year")).append(" ");
                        bindVars.put("year", date[2]);
                    }
                }

                if(date.length == 6) {  // date range
                    if(Constants.isNullOrNoData(date[0]) && Constants.isNullOrNoData(date[2]))
                        throw new FloraOnException("Date ranges must be defined in relation to a precise day, month or year. Disjoint intervals are not supported.");
                    if(Constants.isNullOrNoData(date[3]) && Constants.isNullOrNoData(date[5]))
                        throw new FloraOnException("Date ranges must be defined in relation to a precise day, month or year. Disjoint intervals are not supported.");
                    String fromDate = Inventory.formatDateYMD(date[0], date[1], date[2], null, null, "0");
                    String toDate = Inventory.formatDateYMD(date[3], date[4], date[5], null, null, "9");
                    inventoryFilter.append(AQLOccurrenceQueries.getString("filter.daterange")).append(" ");
                    bindVars.put("fromDate", fromDate);
                    bindVars.put("toDate", toDate);
                }
            }
            filter.remove("date");
        }

        // precision filter
        if(filter.containsKey("prec")) {
            if(filter.get("prec").toUpperCase().equals("NA")) {
                inventoryFilter.append(AQLOccurrenceQueries.getString("filter.nullprecision")).append(" ");
            } else {
                bindVars.put("precision", new Precision(filter.get("prec")));
                inventoryFilter.append(AQLOccurrenceQueries.getString("filter.precision")).append(" ");
            }
            filter.remove("prec");
        }

        // inventory ID filter
        if(filter.containsKey("iid")) {
            bindVars.put("inventoryId", filter.get("iid"));
            inventoryFilter.append(AQLOccurrenceQueries.getString("filter.inventoryId")).append(" ");
            filter.remove("iid");
        }

        // user filter, checks if user is mentioned in observers, collectors, dets or maintainer
        if(filter.containsKey("uid")) {
            bindVars.put("userId", filter.get("uid"));
            inventoryFilter.append(AQLOccurrenceQueries.getString("filter.userId")).append(" ");
            filter.remove("uid");
        }

        // verbLocality filter
        if(filter.containsKey("local")) {
            if(filter.get("local").toUpperCase().equals("NA")) {
                inventoryFilter.append(AQLOccurrenceQueries.getString("filter.nullverblocality")).append(" ");
            } else {
                bindVars.put("verbLocality", filter.get("local").replaceAll("\\*", "%"));
                inventoryFilter.append(AQLOccurrenceQueries.getString("filter.verblocality")).append(" ");
            }
            filter.remove("local");
        }

        // new record filter
        if(filter.containsKey("new")) {
            if(filter.get("new").equalsIgnoreCase("NA") || filter.get("new").equalsIgnoreCase("no") || filter.get("new").equalsIgnoreCase("0"))
                inventoryFilter.append(AQLOccurrenceQueries.getString("filter.notNew")).append(" ");
            else
                inventoryFilter.append(AQLOccurrenceQueries.getString("filter.new")).append(" ");
            filter.remove("new");
        }

        // credits filter
        if(filter.containsKey("proj")) {
            if(filter.get("proj").toUpperCase().equals("NA")) {
                inventoryFilter.append(AQLOccurrenceQueries.getString("filter.nullcredits")).append(" ");
            } else {
                bindVars.put("credits", filter.get("proj").replaceAll("\\*", "%"));
                inventoryFilter.append(AQLOccurrenceQueries.getString("filter.credits")).append(" ");
            }
            filter.remove("proj");
        }

        // source filter
        if(filter.containsKey("source")) {
            if(filter.get("source").equalsIgnoreCase("NA")) {
                inventoryFilter.append(AQLOccurrenceQueries.getString("filter.nullsource")).append(" ");
            } else {
                bindVars.put("source", filter.get("source").replaceAll("\\*", "%"));
                inventoryFilter.append(AQLOccurrenceQueries.getString("filter.source")).append(" ");
            }
            filter.remove("source");
        }

        // habitat filter
        if(filter.containsKey("hab")) {
            if(filter.get("hab").toUpperCase().equals("NA")) {
                inventoryFilter.append(AQLOccurrenceQueries.getString("filter.nullhabitat")).append(" ");
            } else {
                bindVars.put("habitat", filter.get("hab").replaceAll("\\*", "%"));
                inventoryFilter.append(AQLOccurrenceQueries.getString("filter.habitat")).append(" ");
            }
            filter.remove("hab");
        }

        // code filter
        if(filter.containsKey("code")) {
            if(filter.get("code").toUpperCase().equals("NA")) {
                inventoryFilter.append(AQLOccurrenceQueries.getString("filter.nullcode")).append(" ");
            } else {
                bindVars.put("code", filter.get("code").replaceAll("\\*", "%"));
                inventoryFilter.append(AQLOccurrenceQueries.getString("filter.code")).append(" ");
            }
            filter.remove("code");
        }

        // observers filter
        if(filter.containsKey("obs")) {
            if(filter.get("obs").toUpperCase().equals("NA")) {
                inventoryFilter.append(AQLOccurrenceQueries.getString("filter.nullObserver")).append(" ");
            } else {
                Iterator<User> it = this.driver.getAdministration().findUserByName(filter.get("obs").replaceAll("\\*", "%"));
                List<String> uid = new ArrayList<>();
                while(it.hasNext())
                    uid.add(it.next().getID());
                bindVars.put("observer", uid.toArray(new String[0]));
                inventoryFilter.append(AQLOccurrenceQueries.getString("filter.observer")).append(" ");
            }
            filter.remove("obs");
        }

        // collectors filter
        if(filter.containsKey("coll")) {
            if(filter.get("coll").toUpperCase().equals("NA")) {
                inventoryFilter.append(AQLOccurrenceQueries.getString("filter.nullCollector")).append(" ");
            } else {
                Iterator<User> it = this.driver.getAdministration().findUserByName(filter.get("coll").replaceAll("\\*", "%"));
                List<String> uid = new ArrayList<>();
                while(it.hasNext())
                    uid.add(it.next().getID());
                bindVars.put("collector", uid.toArray(new String[0]));
                inventoryFilter.append(AQLOccurrenceQueries.getString("filter.collector")).append(" ");
            }
            filter.remove("coll");
        }

        // maintainer filter
        if(filter.containsKey("maint")) {
            Iterator<User> it = this.driver.getAdministration().findUserByName(filter.get("maint").replaceAll("\\*", "%"));
            List<String> uid = new ArrayList<>();
            while(it.hasNext())
                uid.add(it.next().getID());
            bindVars.put("user", uid.toArray(new String[0]));
            inventoryFilter.append(AQLOccurrenceQueries.getString("filter.maintainer")).append(" ");
            filter.remove("maint");
        }

        // number of taxa filter
        if(filter.containsKey("nsp")) {
            if(filter.get("nsp").toUpperCase().equals("NA")) {
                inventoryFilter.append(AQLOccurrenceQueries.getString("filter.numberOfSpeciesZero")).append(" ");
            } else {
                IntegerInterval range = new IntegerInterval(filter.get("nsp"));
                if((range.getValue() != null && range.getValue() == 0) || (range.getMaxValue() != null && range.getMaxValue() == 0))
                    inventoryFilter.append(AQLOccurrenceQueries.getString("filter.numberOfSpeciesZero")).append(" ");
                else {
                    if (range.getValue() == null) {  // is an interval
                        bindVars.put("minnsp", range.getMinValue() == null ? -9999 : range.getMinValue());
                        bindVars.put("maxnsp", range.getMaxValue() == null ? 9999 : range.getMaxValue());
                    } else {    // is an exact number
                        bindVars.put("minnsp", range.getValue());
                        bindVars.put("maxnsp", range.getValue());
                    }

                    inventoryFilter.append(AQLOccurrenceQueries.getString("filter.numberOfSpecies")).append(" ");
                }
            }
            filter.remove("nsp");
        }
        return inventoryFilter.toString();
    }

    /**
     * Construct the AQL query filters for the occurrences
     * @param filter
     * @param bindVars
     * @return
     * @throws FloraOnException
     */
    private String[] processOccurrenceFilters(Map<String, String> filter, Map<String, Object> bindVars) throws FloraOnException {
        StringBuilder inventoryFilter = new StringBuilder();
        StringBuilder occurrenceFilter = new StringBuilder();

        // excludeReason filter
        if(filter.containsKey("excl")) {
            if(filter.get("excl").equalsIgnoreCase("NA")) {
                inventoryFilter.append(AQLOccurrenceQueries.getString("filter.nullExcludeReason")).append(" ");
                occurrenceFilter.append(AQLOccurrenceQueries.getString("filter.nullExcludeReason.2")).append(" ");
            } else {
                try {
                    bindVars.put("excludeReason"
                            , OccurrenceConstants.PresenceStatus.getValueFromAcronym(filter.get("excl")).toString());
                } catch (IllegalArgumentException e) {
                    throw new FloraOnException(e.getMessage());
                }
                inventoryFilter.append(AQLOccurrenceQueries.getString("filter.excludeReason")).append(" ");
                occurrenceFilter.append(AQLOccurrenceQueries.getString("filter.excludeReason.2")).append(" ");
            }
            filter.remove("excl");
        }

        // count filter
        if(filter.containsKey("detected")) {
            if(filter.get("detected").equalsIgnoreCase("NA") || filter.get("detected").equalsIgnoreCase("no") || filter.get("detected").equalsIgnoreCase("0"))
                occurrenceFilter.append(AQLOccurrenceQueries.getString("filter.notDetected")).append(" ");
            else
                occurrenceFilter.append(AQLOccurrenceQueries.getString("filter.detected")).append(" ");
            filter.remove("detected");
        }

        // hasPhoto filter
        if(filter.containsKey("photo")) {
            if(filter.get("photo").equalsIgnoreCase("NA") || filter.get("photo").equalsIgnoreCase("no") || filter.get("photo").equalsIgnoreCase("0"))
                occurrenceFilter.append(AQLOccurrenceQueries.getString("filter.notHasPhoto")).append(" ");
            else
                occurrenceFilter.append(AQLOccurrenceQueries.getString("filter.hasPhoto")).append(" ");
            filter.remove("photo");
        }

        if(filter.containsKey("dateinserted")) {
            if(filter.get("dateinserted").equalsIgnoreCase("NA"))
                occurrenceFilter.append(AQLOccurrenceQueries.getString("filter.nulldateinserted")).append(" ");
            else {
                Integer[] dateInserted;
                try {
                    dateInserted = DateParser.parseDate(filter.get("dateinserted"));
                } catch (IllegalArgumentException e) {
                    throw new FloraOnException(e.getMessage());
                }
                Log.info("DATEINSERTED: "+ new Gson().toJson(dateInserted));
                Calendar c = Calendar.getInstance();
                if(dateInserted.length == 5) {  // single date
                    if (!Constants.isNullOrNoData(dateInserted[0]) && !Constants.isNullOrNoData(dateInserted[1]) && !Constants.isNullOrNoData(dateInserted[2])) {
                        occurrenceFilter.append(AQLOccurrenceQueries.getString("filter.dateInsertedRange")).append(" ");

                        c.set(Calendar.YEAR, dateInserted[2]);
                        c.set(Calendar.MONTH, dateInserted[1] - 1);
                        c.set(Calendar.DAY_OF_MONTH, dateInserted[0]);
                        c.set(Calendar.HOUR_OF_DAY, 0);
                        c.set(Calendar.MINUTE, 0);
                        bindVars.put("fromDateInserted", c.getTime());
                        c.set(Calendar.HOUR_OF_DAY, 23);
                        c.set(Calendar.MINUTE, 59);
                        bindVars.put("toDateInserted", c.getTime());
                    } else
                        throw new FloraOnException("Invalid exact date");
                }

                if(dateInserted.length == 6) {  // date range
                    if (Constants.isNullOrNoData(dateInserted[0]) || Constants.isNullOrNoData(dateInserted[1]) || Constants.isNullOrNoData(dateInserted[2])
                        || Constants.isNullOrNoData(dateInserted[3]) || Constants.isNullOrNoData(dateInserted[4]) || Constants.isNullOrNoData(dateInserted[5]))
                        throw new FloraOnException("Date inserted ranges must be defined in relation to precise dates.");

                    occurrenceFilter.append(AQLOccurrenceQueries.getString("filter.dateInsertedRange")).append(" ");

                    c.set(Calendar.YEAR, dateInserted[2]);
                    c.set(Calendar.MONTH, dateInserted[1] - 1);
                    c.set(Calendar.DAY_OF_MONTH, dateInserted[0]);
                    c.set(Calendar.HOUR_OF_DAY, 0);
                    c.set(Calendar.MINUTE, 0);
                    bindVars.put("fromDateInserted", c.getTime());
                    c.set(Calendar.YEAR, dateInserted[5]);
                    c.set(Calendar.MONTH, dateInserted[4] - 1);
                    c.set(Calendar.DAY_OF_MONTH, dateInserted[3]);
                    c.set(Calendar.HOUR_OF_DAY, 23);
                    c.set(Calendar.MINUTE, 59);
                    bindVars.put("toDateInserted", c.getTime());
                }
            }

            filter.remove("dateinserted");
        }

        // confidence filter
        if(filter.containsKey("conf")) {
            if(filter.get("conf").toUpperCase().equals("NA")) {
                inventoryFilter.append(AQLOccurrenceQueries.getString("filter.nullconfidence")).append(" ");
                occurrenceFilter.append(AQLOccurrenceQueries.getString("filter.nullconfidence.2")).append(" ");
            } else {
                try {
                    bindVars.put("confidence"
                            , OccurrenceConstants.ConfidenceInIdentifiction.getValueFromAcronym(filter.get("conf")).toString());
                } catch (IllegalArgumentException e) {
                    throw new FloraOnException(e.getMessage());
                }
                inventoryFilter.append(AQLOccurrenceQueries.getString("filter.confidence")).append(" ");
                occurrenceFilter.append(AQLOccurrenceQueries.getString("filter.confidence.2")).append(" ");
            }
            filter.remove("conf");
        }

        // phenology filter
        if(filter.containsKey("phen")) {
            if(filter.get("phen").toUpperCase().equals("NA")) {
                inventoryFilter.append(AQLOccurrenceQueries.getString("filter.nullphenology")).append(" ");
                occurrenceFilter.append(AQLOccurrenceQueries.getString("filter.nullphenology.2")).append(" ");
            } else {
                try {
                    bindVars.put("phenoState"
                            , Constants.PhenologicalStates.getValueFromAcronym(filter.get("phen")).toString());
                } catch (IllegalArgumentException e) {
                    throw new FloraOnException(e.getMessage());
                }
                inventoryFilter.append(AQLOccurrenceQueries.getString("filter.phenology")).append(" ");
                occurrenceFilter.append(AQLOccurrenceQueries.getString("filter.phenology.2")).append(" ");
            }
            filter.remove("phen");
        }

        // inventory latitude filter
        if(filter.containsKey("ilat")) {
            if(filter.get("ilat").toUpperCase().equals("NA")) {
                inventoryFilter.append(AQLOccurrenceQueries.getString("filter.nulllatitude")).append(" ");
            } else {
                NumericInterval range = new NumericInterval(filter.get("ilat"));
                if(range.getValue() == null) {  // is an interval
                    bindVars.put("minlat", range.getMinValue() == null ? true : range.getMinValue());
                    bindVars.put("maxlat", range.getMaxValue() == null ? "" : range.getMaxValue());
                } else {    // is an exact number
                    bindVars.put("minlat", range.getValue() - 0.0001);
                    bindVars.put("maxlat", range.getValue() + 0.0001);
                }

                inventoryFilter.append(AQLOccurrenceQueries.getString("filter.ilatitude")).append(" ");
            }
            filter.remove("ilat");
        }

        // inventory longitude filter
        if(filter.containsKey("ilong")) {
            if(filter.get("ilong").toUpperCase().equals("NA")) {
                inventoryFilter.append(AQLOccurrenceQueries.getString("filter.nulllongitude")).append(" ");
            } else {
                NumericInterval range = new NumericInterval(filter.get("ilong"));
                if(range.getValue() == null) {  // is an interval
                    bindVars.put("minlng", range.getMinValue() == null ? true : range.getMinValue());
                    bindVars.put("maxlng", range.getMaxValue() == null ? "" : range.getMaxValue());
                } else {    // is an exact number
                    bindVars.put("minlng", range.getValue() - 0.0001);
                    bindVars.put("maxlng", range.getValue() + 0.0001);
                }

                inventoryFilter.append(AQLOccurrenceQueries.getString("filter.ilongitude")).append(" ");
            }
            filter.remove("ilong");
        }

        // latitude filter
        if(filter.containsKey("lat")) {
            if(filter.get("lat").toUpperCase().equals("NA")) {
                inventoryFilter.append(AQLOccurrenceQueries.getString("filter.nulllatitude")).append(" ");
                occurrenceFilter.append(AQLOccurrenceQueries.getString("filter.nulllatitude.2")).append(" ");
            } else {
                NumericInterval range = new NumericInterval(filter.get("lat"));
                if(range.getValue() == null) {  // is an interval
                    bindVars.put("minlat", range.getMinValue() == null ? -9999f : range.getMinValue());
                    bindVars.put("maxlat", range.getMaxValue() == null ? 9999f : range.getMaxValue());
                } else {    // is an exact number
                    bindVars.put("minlat", range.getValue() - 0.0001);
                    bindVars.put("maxlat", range.getValue() + 0.0001);
                }

                inventoryFilter.append(AQLOccurrenceQueries.getString("filter.latitude")).append(" ");
                occurrenceFilter.append(AQLOccurrenceQueries.getString("filter.latitude.2")).append(" ");
            }
            filter.remove("lat");
        }

        // longitude filter
        if(filter.containsKey("long")) {
            if(filter.get("long").toUpperCase().equals("NA")) {
                inventoryFilter.append(AQLOccurrenceQueries.getString("filter.nulllongitude")).append(" ");
                occurrenceFilter.append(AQLOccurrenceQueries.getString("filter.nulllongitude.2")).append(" ");
            } else {
                NumericInterval range = new NumericInterval(filter.get("long"));
                if(range.getValue() == null) {  // is an interval
                    bindVars.put("minlng", range.getMinValue() == null ? -9999f : range.getMinValue());
                    bindVars.put("maxlng", range.getMaxValue() == null ? 9999f : range.getMaxValue());
                } else {    // is an exact number
                    bindVars.put("minlng", range.getValue() - 0.0001);
                    bindVars.put("maxlng", range.getValue() + 0.0001);
                }

                inventoryFilter.append(AQLOccurrenceQueries.getString("filter.longitude")).append(" ");
                occurrenceFilter.append(AQLOccurrenceQueries.getString("filter.longitude.2")).append(" ");
            }
            filter.remove("long");
        }

        // tag filter
        if(filter.containsKey("tag")) {
            if(filter.get("tag").toUpperCase().equals("NA")) {
                occurrenceFilter.append(AQLOccurrenceQueries.getString("filter.nulltag")).append(" ");
            } else {
                bindVars.put("tag", filter.get("tag").replaceAll("\\*", "%"));
                occurrenceFilter.append(AQLOccurrenceQueries.getString("filter.tag")).append(" ");
            }
            filter.remove("tag");
        }

        // gpsCode filter
        if(filter.containsKey("gps")) {
            if(filter.get("gps").toUpperCase().equals("NA")) {
                occurrenceFilter.append(AQLOccurrenceQueries.getString("filter.nullGpsCode")).append(" ");
            } else {
                bindVars.put("gpscode", filter.get("gps").replaceAll("\\*", "%"));
                occurrenceFilter.append(AQLOccurrenceQueries.getString("filter.gpsCode")).append(" ");
            }
            filter.remove("gps");
        }

        // privateComment filter (both of inventory and of occurrence)
        if(filter.containsKey("priv")) {
            if(filter.get("priv").toUpperCase().equals("NA")) {
                occurrenceFilter.append(AQLOccurrenceQueries.getString("filter.nullPrivateComment")).append(" ");
            } else {
                bindVars.put("privateComment", filter.get("priv").replaceAll("\\*", "%"));
                occurrenceFilter.append(AQLOccurrenceQueries.getString("filter.privateComment")).append(" ");
            }
            filter.remove("priv");
        }

        // public comment filter (both of inventory and of occurrence)
        if(filter.containsKey("pub")) {
            if(filter.get("pub").toUpperCase().equals("NA")) {
                occurrenceFilter.append(AQLOccurrenceQueries.getString("filter.nullPublicComment")).append(" ");
            } else {
                bindVars.put("publicComment", filter.get("pub").replaceAll("\\*", "%"));
                occurrenceFilter.append(AQLOccurrenceQueries.getString("filter.publicComment")).append(" ");
            }
            filter.remove("pub");
        }

        // accession filter
        if(filter.containsKey("acc")) {
            if(filter.get("acc").toUpperCase().equals("NA")) {
                occurrenceFilter.append(AQLOccurrenceQueries.getString("filter.nullAccession")).append(" ");
            } else {
                bindVars.put("accession", filter.get("acc").replaceAll("\\*", "%"));
                occurrenceFilter.append(AQLOccurrenceQueries.getString("filter.accession")).append(" ");
            }
            filter.remove("acc");
        }

        // taxon filter
        if(filter.containsKey("tax")) {
            if(filter.get("tax").toUpperCase().equals("NA")) {
                occurrenceFilter.append(AQLOccurrenceQueries.getString("filter.nulltaxon")).append(" ");
            } else {
                bindVars.put("taxon", filter.get("tax").replaceAll("\\*", "%"));
                occurrenceFilter.append(AQLOccurrenceQueries.getString("filter.taxon")).append(" ");
            }
            filter.remove("tax");
        }

        if(filter.size() > 0) {
            throw new FloraOnException("These filters were not understood: " + StringUtils.implode(", ", filter.keySet().toArray(new String[0])));
        }
        return new String[] {inventoryFilter.toString(), occurrenceFilter.toString()};
    }

    public <T> Iterator<T> findAnyByFilter(Class<T> type, Map<String, String> filter, AbstractMap.SimpleEntry<String, Boolean> orderField, INodeKey userId, boolean asObserver, Integer offset, Integer count) throws FloraOnException {
        String textFilter = filter.get("NA");
        filter.remove("NA");
        Map<String, Object> bindVars = new HashMap<>();
        if(offset == null) offset = 0;
        if(count == null) count = 999999;
        if(!StringUtils.isStringEmpty(textFilter)) bindVars.put("query", "%" + textFilter + "%");
        bindVars.put("offset", offset);
        bindVars.put("count", count);

        String query;
        String inventoryFilter = "";
        String occurrenceFilter = "";
        String preliminaryFilter = "";
        String sortExpression = this.buildSortOrderExpression(orderField);
// TODO: filters by phenoState, etc. are not using indexes! Must go to preliminary filters: filter 'FLOWER' IN i.unmatchedOccurrences[*].phenoState
        if(userId != null) {
            bindVars.put("preliminary", userId.toString());
            preliminaryFilter += asObserver ? "FILTER i.observers ANY == @preliminary" : "FILTER i.maintainer == @preliminary";
        }
        inventoryFilter += processInventoryFilters(filter, bindVars);

        String[] occurrenceFilters = processOccurrenceFilters(filter, bindVars);
        inventoryFilter += occurrenceFilters[0];
        occurrenceFilter += occurrenceFilters[1];
/*
        Log.info(AQLOccurrenceQueries.getString(!StringUtils.isStringEmpty(textFilter) ?
                "occurrencequery.8.withtextfilter" : "occurrencequery.8.withouttextfilter", inventoryFilter, occurrenceFilter));
        Log.info(new Gson().toJson(bindVars));
*/
        if(type.equals(Inventory.class)) {
            query = AQLOccurrenceQueries.getString(!StringUtils.isStringEmpty(textFilter) ?
                    "occurrencequery.9.withtextfilter" : "occurrencequery.9.withouttextfilter", inventoryFilter, occurrenceFilter, sortExpression, preliminaryFilter);

        } else if(type.equals(Occurrence.class)) {
            query = AQLOccurrenceQueries.getString(!StringUtils.isStringEmpty(textFilter) ?
                    "occurrencequery.8.withtextfilter" : "occurrencequery.8.withouttextfilter", inventoryFilter, occurrenceFilter, sortExpression, preliminaryFilter);
        } else throw new FloraOnException("Unknown class");

        try {
            return database.query(query, bindVars, null, type);
        } catch (ArangoDBException e) {
            throw new DatabaseException(e.getMessage());
        }
    }

    @Override
    public Iterator<Inventory> findInventoriesByFilter(Map<String, String> filter, AbstractMap.SimpleEntry<String, Boolean> orderField, INodeKey userId, boolean asObserver, Integer offset, Integer count) throws FloraOnException {
        return findAnyByFilter(Inventory.class, filter, orderField, userId, asObserver, offset, count);
    }

    @Override
    public Iterator<Occurrence> findOccurrencesByFilter(Map<String, String> filter, AbstractMap.SimpleEntry<String, Boolean> orderField, INodeKey userId, boolean asObserver, Integer offset, Integer count) throws FloraOnException {
        return findAnyByFilter(Occurrence.class, filter, orderField, userId, asObserver, offset, count);
    }

    @Override
    public <T> int findAnyByFilterCount(Class<T> type, Map<String, String> filter, INodeKey userId, boolean asObserver) throws FloraOnException {
        Map<String, Object> bindVars = new HashMap<>();
        String query;
        String inventoryFilter = "";
        String occurrenceFilter = "";
        String preliminaryFilter = "";

        if(userId != null) {
            bindVars.put("preliminary", userId.toString());
            preliminaryFilter += asObserver ? "FILTER i.observers ANY == @preliminary" : "FILTER i.maintainer == @preliminary";
        }
        inventoryFilter += processInventoryFilters(filter, bindVars);

        String[] occurrenceFilters = processOccurrenceFilters(filter, bindVars);
        inventoryFilter += occurrenceFilters[0];
        occurrenceFilter += occurrenceFilters[1];

        if(type.equals(Inventory.class)) {
            query = AQLOccurrenceQueries.getString("occurrencequery.9.count", inventoryFilter, occurrenceFilter, preliminaryFilter);
        } else if(type.equals(Occurrence.class)) {
            query = AQLOccurrenceQueries.getString("occurrencequery.8.count", inventoryFilter, occurrenceFilter, preliminaryFilter);
        } else throw new FloraOnException("Unknown class");

        try {
            return database.query(query, bindVars, null, Integer.class).next();
        } catch (ArangoDBException e) {
            throw new DatabaseException(e.getMessage());
        }
    }

    @Override
    public int findOccurrencesByFilterCount(Map<String, String> filter, INodeKey userId, boolean asObserver) throws FloraOnException {
        return findAnyByFilterCount(Occurrence.class, filter, userId, asObserver);
/*
        Map<String, Object> bindVars = new HashMap<>();
        String inventoryFilter = "";
        String occurrenceFilter = "";
        String preliminaryFilter = "";

        if(userId != null) {
            bindVars.put("user", new String[] {userId.toString()});
            inventoryFilter += AQLOccurrenceQueries.getString("filter.maintainer") + " ";
            bindVars.put("preliminary", userId.toString());
            preliminaryFilter += asObserver ? "FILTER i.observers ANY == @preliminary" : "FILTER i.maintainer == @preliminary";
        }

        inventoryFilter += processInventoryFilters(filter, bindVars);

        String[] occurrenceFilters = processOccurrenceFilters(filter, bindVars);
        inventoryFilter += occurrenceFilters[0];
        occurrenceFilter += occurrenceFilters[1];

        // TODO must fetch observer and maintainer names!
        try {
            return database.query(
                    AQLOccurrenceQueries.getString("occurrencequery.8.count", inventoryFilter, occurrenceFilter, preliminaryFilter)
                    , bindVars, null, Integer.class).next();
        } catch (ArangoDBException e) {
            throw new DatabaseException(e.getMessage());
        }
*/
    }

    @Override
    public Map<TaxEntObservation, MutableInt> getTaxonListFromOccurrences(Iterator<Occurrence> occurrences, boolean aggregateByAccepted) throws FloraOnException {
        Map<TaxEntObservation, MutableInt> speciesList = new HashMap<>();
        while(occurrences.hasNext()) {
            Occurrence oc = occurrences.next();
            if(oc.getOccurrence().getConfidence() != null
                    && oc.getOccurrence().getConfidence() != OccurrenceConstants.ConfidenceInIdentifiction.CERTAIN
                    && oc.getOccurrence().getConfidence() != OccurrenceConstants.ConfidenceInIdentifiction.NULL)
                continue;
            TaxEntObservation te = new TaxEntObservation(oc.getOccurrence());
            if(speciesList.containsKey(te))
                speciesList.get(te).increment();
            else
                speciesList.put(te, new MutableInt(1));
        }
        if(aggregateByAccepted) {
            Map<TaxEntObservation, MutableInt> tmp = new HashMap<>();
            for(Map.Entry<TaxEntObservation, MutableInt> ent : speciesList.entrySet()) {
                if(ent.getKey().occurrence.getTaxEntMatch() != null) {
                    Iterator<TaxEntMatch> tem = driver.getQueryDriver().getFirstAcceptedTaxonContaining(new String[]{ent.getKey().occurrence.getTaxEntMatch()});
                    if(tem.hasNext()) {
                        TaxEntObservation accepted = new TaxEntObservation(tem.next().getMatchedTaxEnt(), OccurrenceConstants.ConfidenceInIdentifiction.CERTAIN);
                        if (tmp.containsKey(accepted))
                            tmp.get(accepted).add(ent.getValue());
                        else
                            tmp.put(accepted, new MutableInt(ent.getValue()));
                    }
                }
            }
            speciesList = tmp;
        }

        return speciesList;
    }
}