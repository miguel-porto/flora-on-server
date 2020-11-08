package pt.floraon.occurrences.arangodb;

import com.arangodb.ArangoCursor;
import com.arangodb.ArangoDBException;
import com.arangodb.ArangoDatabase;
import com.arangodb.model.AqlQueryOptions;
import com.google.gson.Gson;
import jline.internal.Log;
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
import pt.floraon.occurrences.fields.parsers.DateParser;
import pt.floraon.taxonomy.entities.TaxEnt;

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
                    , bindVars, null, Occurrence.class);
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
    public Iterator<Occurrence> getOccurrencesOfObserver(INodeKey authorId, Integer offset, Integer count) throws DatabaseException {
        if(authorId == null) return getOccurrencesOfMaintainer(null, false, offset, count);
        Map<String, Object> bindVars = new HashMap<>();
        bindVars.put("observer", authorId.toString());
        bindVars.put("off", offset == null ? 0 : offset);
        bindVars.put("cou", count == null ? 999999 : count);

        try {
            return database.query(
                    AQLOccurrenceQueries.getString("occurrencequery.2"), bindVars, null, Occurrence.class);
        } catch (ArangoDBException e) {
            throw new DatabaseException(e.getMessage());
        }
    }

    @Override
    public Iterator<Occurrence> getOccurrencesOfObserverWithinDates(INodeKey authorId, Date from, Date to, Integer offset, Integer count) throws DatabaseException {
        if(authorId == null) return getOccurrencesOfMaintainer(null, false, offset, count);
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
    public Iterator<Occurrence> getOccurrencesOfMaintainer(INodeKey authorId, boolean returnObserverNames, Integer offset, Integer count) throws DatabaseException {
        if(offset == null) offset = 0;
        if(count == null) count = 999999;
        String query = authorId == null ?
                (returnObserverNames ? AQLOccurrenceQueries.getString("occurrencequery.4.nouser.observernames", null, offset, count)
                    : AQLOccurrenceQueries.getString("occurrencequery.4.nouser", null, offset, count))
                : (returnObserverNames ? AQLOccurrenceQueries.getString("occurrencequery.4.observernames", authorId.getID(), offset, count)
                    : AQLOccurrenceQueries.getString("occurrencequery.4", authorId.getID(), offset, count));
        try {
            return database.query(query, null, null, Occurrence.class);
        } catch (ArangoDBException e) {
            throw new DatabaseException(e.getMessage());
        }
    }

    @Override
    public int getOccurrencesOfMaintainerCount(INodeKey authorId) throws DatabaseException {
        String query = authorId == null ?
                AQLOccurrenceQueries.getString("occurrencequery.4b.nouser")
                : AQLOccurrenceQueries.getString("occurrencequery.4b", authorId.getID());
        try {
            return database.query(query, null, null, Integer.class).next();
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
        String query = authorId == null ?
                AQLOccurrenceQueries.getString("occurrencequery.4a.nouser", null, offset, count)
                : AQLOccurrenceQueries.getString("occurrencequery.4a", authorId.getID(), offset, count);

        try {
            return database.query(query, null, null, Inventory.class);
        } catch (ArangoDBException e) {
            throw new DatabaseException(e.getMessage());
        }
    }

    @Override
    public int getInventoriesOfMaintainerCount(INodeKey authorId) throws DatabaseException {
        String query = authorId == null ? AQLOccurrenceQueries.getString("occurrencequery.4a.nouser.count")
                : AQLOccurrenceQueries.getString("occurrencequery.4a.count", authorId.getID());
        try {
            return database.query(query, null, null, Integer.class).next();
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
                TaxEnt te = driver.getNodeWorkerDriver().createTaxEntFromTaxEnt(TaxEnt.parse(change.getKey()));
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

    @Override
    public Iterator<Inventory> findInventoriesByFilter(Map<String, String> filter, INodeKey userId, Integer offset, Integer count) throws FloraOnException {
        String textFilter = filter.get("NA");
        Map<String, Object> bindVars = new HashMap<>();
        if(offset == null) offset = 0;
        if(count == null) count = 999999;
        if(!StringUtils.isStringEmpty(textFilter)) bindVars.put("query", "%" + textFilter + "%");
        bindVars.put("offset", offset);
        bindVars.put("count", count);
        String inventoryFilter = "";
        String occurrenceFilter = "";

        if(userId != null) {
            bindVars.put("user", new String[] {userId.toString()});
            inventoryFilter += AQLOccurrenceQueries.getString("filter.maintainer") + " ";
        }

        inventoryFilter += processInventoryFilters(filter, bindVars);

        String[] occurrenceFilters = processOccurrenceFilters(filter, bindVars);
        inventoryFilter += occurrenceFilters[0];
        occurrenceFilter += occurrenceFilters[1];

        try {
            return database.query(
                    AQLOccurrenceQueries.getString(!StringUtils.isStringEmpty(textFilter) ?
                            "occurrencequery.9.withtextfilter" : "occurrencequery.9.withouttextfilter", inventoryFilter, occurrenceFilter)
                    , bindVars, null, Inventory.class);
        } catch (ArangoDBException e) {
            throw new DatabaseException(e.getMessage());
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
        }

        // precision filter
        if(filter.containsKey("prec")) {
            if(filter.get("prec").toUpperCase().equals("NA")) {
                inventoryFilter.append(AQLOccurrenceQueries.getString("filter.nullprecision")).append(" ");
            } else {
                bindVars.put("precision", new Precision(filter.get("prec")));
                inventoryFilter.append(AQLOccurrenceQueries.getString("filter.precision")).append(" ");
            }
        }

        // inventory ID filter
        if(filter.containsKey("iid")) {
            bindVars.put("inventoryId", filter.get("iid"));
            inventoryFilter.append(AQLOccurrenceQueries.getString("filter.inventoryId")).append(" ");
        }

        // user filter, checks if user is mentioned in observers, collectors, dets or maintainer
        if(filter.containsKey("uid")) {
            bindVars.put("userId", filter.get("uid"));
            inventoryFilter.append(AQLOccurrenceQueries.getString("filter.userId")).append(" ");
        }

        // verbLocality filter
        if(filter.containsKey("local")) {
            if(filter.get("local").toUpperCase().equals("NA")) {
                inventoryFilter.append(AQLOccurrenceQueries.getString("filter.nullverblocality")).append(" ");
            } else {
                bindVars.put("verbLocality", filter.get("local").replaceAll("\\*", "%"));
                inventoryFilter.append(AQLOccurrenceQueries.getString("filter.verblocality")).append(" ");
            }
        }

        // credits filter
        if(filter.containsKey("proj")) {
            if(filter.get("proj").toUpperCase().equals("NA")) {
                inventoryFilter.append(AQLOccurrenceQueries.getString("filter.nullcredits")).append(" ");
            } else {
                bindVars.put("credits", filter.get("proj").replaceAll("\\*", "%"));
                inventoryFilter.append(AQLOccurrenceQueries.getString("filter.credits")).append(" ");
            }
        }

        // habitat filter
        if(filter.containsKey("hab")) {
            if(filter.get("hab").toUpperCase().equals("NA")) {
                inventoryFilter.append(AQLOccurrenceQueries.getString("filter.nullhabitat")).append(" ");
            } else {
                bindVars.put("habitat", filter.get("hab").replaceAll("\\*", "%"));
                inventoryFilter.append(AQLOccurrenceQueries.getString("filter.habitat")).append(" ");
            }
        }

        // code filter
        if(filter.containsKey("code")) {
            if(filter.get("code").toUpperCase().equals("NA")) {
                inventoryFilter.append(AQLOccurrenceQueries.getString("filter.nullcode")).append(" ");
            } else {
                bindVars.put("code", filter.get("code").replaceAll("\\*", "%"));
                inventoryFilter.append(AQLOccurrenceQueries.getString("filter.code")).append(" ");
            }
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
        }

        // maintainer filter
        if(filter.containsKey("maint")) {
            Iterator<User> it = this.driver.getAdministration().findUserByName(filter.get("maint").replaceAll("\\*", "%"));
            List<String> uid = new ArrayList<>();
            while(it.hasNext())
                uid.add(it.next().getID());
            bindVars.put("user", uid.toArray(new String[0]));
            inventoryFilter.append(AQLOccurrenceQueries.getString("filter.maintainer")).append(" ");
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
        }

        // taxon filter
        if(filter.containsKey("tax")) {
            if(filter.get("tax").toUpperCase().equals("NA")) {
                occurrenceFilter.append(AQLOccurrenceQueries.getString("filter.nulltaxon")).append(" ");
            } else {
                bindVars.put("taxon", filter.get("tax").replaceAll("\\*", "%"));
                occurrenceFilter.append(AQLOccurrenceQueries.getString("filter.taxon")).append(" ");
            }
        }

        // tag filter
        if(filter.containsKey("tag")) {
            if(filter.get("tag").toUpperCase().equals("NA")) {
                occurrenceFilter.append(AQLOccurrenceQueries.getString("filter.nulltag")).append(" ");
            } else {
                bindVars.put("tag", filter.get("tag").replaceAll("\\*", "%"));
                occurrenceFilter.append(AQLOccurrenceQueries.getString("filter.tag")).append(" ");
            }
        }

        // gpsCode filter
        if(filter.containsKey("gps")) {
            if(filter.get("gps").toUpperCase().equals("NA")) {
                occurrenceFilter.append(AQLOccurrenceQueries.getString("filter.nullGpsCode")).append(" ");
            } else {
                bindVars.put("gpscode", filter.get("gps").replaceAll("\\*", "%"));
                occurrenceFilter.append(AQLOccurrenceQueries.getString("filter.gpsCode")).append(" ");
            }
        }

        // privateComment filter
        if(filter.containsKey("priv")) {
            if(filter.get("priv").toUpperCase().equals("NA")) {
                occurrenceFilter.append(AQLOccurrenceQueries.getString("filter.nullPrivateComment")).append(" ");
            } else {
                bindVars.put("privateComment", filter.get("priv").replaceAll("\\*", "%"));
                occurrenceFilter.append(AQLOccurrenceQueries.getString("filter.privateComment")).append(" ");
            }
        }

        // accession filter
        if(filter.containsKey("acc")) {
            if(filter.get("acc").toUpperCase().equals("NA")) {
                occurrenceFilter.append(AQLOccurrenceQueries.getString("filter.nullAccession")).append(" ");
            } else {
                bindVars.put("accession", filter.get("acc").replaceAll("\\*", "%"));
                occurrenceFilter.append(AQLOccurrenceQueries.getString("filter.accession")).append(" ");
            }
        }

        return new String[] {inventoryFilter.toString(), occurrenceFilter.toString()};
    }


    @Override
    public Iterator<Occurrence> findOccurrencesByFilter(Map<String, String> filter, INodeKey userId, Integer offset, Integer count) throws FloraOnException {
        String textFilter = filter.get("NA");
//        System.out.println(new Gson().toJson(filter));

        Map<String, Object> bindVars = new HashMap<>();
        if(offset == null) offset = 0;
        if(count == null) count = 999999;
        if(!StringUtils.isStringEmpty(textFilter)) bindVars.put("query", "%" + textFilter + "%");
        bindVars.put("offset", offset);
        bindVars.put("count", count);

        String inventoryFilter = "";
        String occurrenceFilter = "";

        if(userId != null) {
            bindVars.put("user", new String[] {userId.toString()});
            inventoryFilter += AQLOccurrenceQueries.getString("filter.maintainer") + " ";
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

        // TODO must fetch observer and maintainer names!
        try {
            return database.query(
                    AQLOccurrenceQueries.getString(!StringUtils.isStringEmpty(textFilter) ?
                            "occurrencequery.8.withtextfilter" : "occurrencequery.8.withouttextfilter", inventoryFilter, occurrenceFilter)
                    , bindVars, null, Occurrence.class);
        } catch (ArangoDBException e) {
            throw new DatabaseException(e.getMessage());
        }
    }

    @Override
    public int findOccurrencesByFilterCount(String filter, INodeKey userId) throws FloraOnException {
        Map<String, Object> bindVars = new HashMap<>();
        bindVars.put("query", "%" + filter + "%");
        if(userId != null)
            bindVars.put("user", userId.toString());

        try {
            return database.query(
                    AQLOccurrenceQueries.getString(userId == null ? "occurrencequery.8a.count" : "occurrencequery.8.count")
                    , bindVars, null, Integer.class).next();
        } catch (ArangoDBException e) {
            throw new DatabaseException(e.getMessage());
        }
    }
}