package pt.floraon.driver.interfaces;

import pt.floraon.driver.DatabaseException;
import pt.floraon.driver.FloraOnException;
import pt.floraon.occurrences.TaxonomicChange;
import pt.floraon.occurrences.entities.Inventory;
import pt.floraon.occurrences.entities.InventoryList;
import pt.floraon.occurrences.entities.Occurrence;

import java.util.AbstractMap;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by miguel on 24-03-2017.
 */
public interface IOccurrenceDriver {
    /**
     * Inserts a new Inventory in the database.
     * @param inventory
     */
    void createInventory(Inventory inventory);

    /**
     * Gets all the occurrences of the given taxon, all its subtaxa and all higher taxa that correspond to this one, e.g.
     * of a family which has only one species.
     * Multi-species inventories are decomposed in single-species, which are output one at a time.
     * @param taxEntId
     * @return
     * @throws DatabaseException
     */
    Iterator<Occurrence> getOccurrencesOfTaxon(INodeKey taxEntId) throws DatabaseException;

    /**
     * Gets all occurrences that don't match any taxon
     * @return
     * @throws DatabaseException
     */
    Iterator<Inventory> getUnmatchedOccurrences() throws DatabaseException;

    /**
     * Gets all occurrences that have no match in the taxonomy (this includes null matches or matches of taxa that do
     * not exist). If userId is specified, get occurrences only for this maintainer.
     * @param userId
     * @return
     * @throws DatabaseException
     */
    Iterator<Inventory> getUnmatchedOccurrencesOfMaintainer(INodeKey userId) throws DatabaseException;

    /**
     * Gets the number of occurrences of this maintainer that have no match in the taxonomy (this includes null matches
     * or matches of taxa that do not exist).
     * @param userId
     * @return
     * @throws DatabaseException
     */
    int getUnmatchedOccurrencesOfMaintainerCount(INodeKey userId) throws DatabaseException;

    /**
     * Gets an array of occurrences by UUID.
     * @param authorId
     * @param uuid
     * @return
     * @throws DatabaseException
     */
    Iterator<Inventory> getOccurrencesByUuid(INodeKey authorId, String[] uuid) throws DatabaseException;

    /**
     * Gets all or part of the occurrences where the given observer has participated (either as the main or secondary
     * observer), within specified dates. Inventories as disaggregated into individual occurrences. NOTE: in case of
     * empty inventories, one (empty) occurrence is returned, to allow the user to populate the empty inventory with taxa.
     * @param authorId
     * @param from
     * @param to
     * @param offset
     * @param count
     * @return
     * @throws DatabaseException
     */
    Iterator<Occurrence> getOccurrencesOfObserverWithinDates(INodeKey authorId, Date from, Date to, Integer offset, Integer count) throws DatabaseException;

    /**
     * Gets all or part of the occurrences where the given observer has participated (either as the main or secondary
     * observer). Inventories as disaggregated into individual occurrences. NOTE: in case of empty inventories, one
     * (empty) occurrence is returned, to allow the user to populate the empty inventory with taxa.
     * @param authorId Set to NULL to return all
     * @param offset
     * @param count
     * @return
     * @throws DatabaseException
     */
    Iterator<Occurrence> getOccurrencesOfObserver(INodeKey authorId, AbstractMap.SimpleEntry<String, Boolean> orderField, boolean returnObserverNames, Integer offset, Integer count) throws DatabaseException;

    /**
     * Gets all or part of the occurrences maintained by the given user. Inventories as disaggregated into individual
     * occurrences. NOTE: in case of empty inventories, one (empty) occurrence is returned, to allow the user to
     * populate the empty inventory with taxa.
     * @param authorId Set to NULL to return all occurrences
     * @param offset
     * @param count
     * @return
     * @throws DatabaseException
     */
    Iterator<Occurrence> getOccurrencesOfMaintainer(INodeKey authorId, AbstractMap.SimpleEntry<String, Boolean> orderField, boolean returnObserverNames, Integer offset, Integer count) throws DatabaseException;

    /**
     * Gets all or part of the occurrences either maintained by, or where the given user is listed as observer.
     * Inventories as disaggregated into individual occurrences.
     * NOTE: in case of empty inventories, one (empty) occurrence is returned, to allow the user to
     * populate the empty inventory with taxa.
     * @param authorId Set to NULL to return all occurrences
     * @param offset
     * @param count
     * @return
     * @throws DatabaseException
     */
    Iterator<Occurrence> getOccurrencesOfUser(INodeKey authorId, boolean asObserver, AbstractMap.SimpleEntry<String, Boolean> orderField, boolean returnObserverNames, Integer offset, Integer count) throws DatabaseException;

    /**
     * Gets the number of occurrences of the maintainer (inventories are disaggregated into single occurrences).
     * @param authorId
     * @return
     * @throws DatabaseException
     */
    int getOccurrencesOfMaintainerCount(INodeKey authorId) throws DatabaseException;

    int getOccurrencesOfUserCount(INodeKey authorId, boolean asObserver) throws DatabaseException;

    /**
     * Gets all or part of the occurrences where the given observer has participated (either as the main or secondary
     * observer), aggregated in Inventories.
     * @param authorId
     * @param offset
     * @param count
     * @return
     * @throws DatabaseException
     */
    Iterator<Inventory> getInventoriesOfObserver(INodeKey authorId, Integer offset, Integer count) throws DatabaseException;

    /**
     * Gets all or part of the occurrences maintained by the given user, aggregated in Inventories.
     * @param authorId
     * @param offset
     * @param count
     * @return
     * @throws DatabaseException
     */
    Iterator<Inventory> getInventoriesOfMaintainer(INodeKey authorId, AbstractMap.SimpleEntry<String, Boolean> orderField, Integer offset, Integer count) throws DatabaseException;

    Iterator<Inventory> findInventoriesByFilter(Map<String, String> filter, AbstractMap.SimpleEntry<String, Boolean> orderField, INodeKey userId, boolean asObserver, Integer offset, Integer count) throws FloraOnException;

    Iterator<Inventory> getInventoriesOfUser(INodeKey authorId, boolean asObserver, AbstractMap.SimpleEntry<String, Boolean> orderField, Integer offset, Integer count) throws DatabaseException;

    int getInventoriesOfUserCount(INodeKey authorId, boolean asObserver) throws DatabaseException;

    int getInventoriesOfMaintainerCount(INodeKey authorId) throws DatabaseException;

    Iterator<Inventory> getInventoriesByIds(String[] inventoryIds) throws DatabaseException;

    /**
     * Deletes an uploaded occurrence table from temporary storage. Note that these tables are not guaranteed to remain
     * in temporary storage, they may get deleted upon server restart.
     * @param authorId
     * @param filename
     * @return
     * @throws FloraOnException
     */
    boolean discardUploadedTable(INodeKey authorId, String filename) throws FloraOnException;

    /**
     * Matches all the taxon names observed in a list of inventories, to the taxonomic database. The IDs of the TaxEnt
     * are set in the passed object, and errors are stored.
     * @param inventories
     * @throws FloraOnException
     */
    void matchTaxEntNames(InventoryList inventories, boolean createNew, boolean doMatch) throws FloraOnException;

    /**
     * Matches all the taxon names observed in an inventory, to the taxonomic database.
     * @param inventory
     * @param createNew
     * @param inventories
     * @param doMatch True to automatically match taxa that have same name & author, modifying the DB
     * @throws FloraOnException
     */
    void matchTaxEntNames(Inventory inventory, boolean createNew, boolean doMatch, InventoryList inventories) throws FloraOnException;

    InventoryList matchTaxEntNames(Iterator<Inventory> inventories, boolean createNew, boolean doMatch) throws FloraOnException;

    /**
     * Deletes occurrences by their UUID or inventories, if UUIDs are not provided. If UUIDs are provided, the ID of the
     * inventory must also be provided in a parallel array. If the inventory becomes empty after deleting the
     * occurrences, it is deleted also.
     * @param inventoryId
     * @param uuid
     * @throws DatabaseException
     */
    int deleteInventoriesOrOccurrences(String[] inventoryId, String[] uuid) throws FloraOnException;

    /**
     * Updates one inventory with the fields present in the passed Inventory. The occurrence array is merged by their
     * UUID. Same UUIDs are replaced, others are added. No occurrences are removed.
     * @param inv
     * @return
     * @throws FloraOnException
     */
    Inventory updateInventory(Inventory inv) throws FloraOnException;

    /**
     * Replaces the taxon match of the given occurrences
     * @param changes
     * @throws FloraOnException
     */
    void replaceTaxEntMatch(Map<String, TaxonomicChange> changes) throws FloraOnException;

    /**
     * Fetches all occurrences that match the given provider filter, or only those of a specific maintainer.
     * @param filter The filter is searched in taxon name, GPS code, Locality and verbLocality
     * @param userId
     * @param offset
     * @param count
     * @return
     * @throws FloraOnException
     */
    Iterator<Occurrence> findOccurrencesByFilter(Map<String, String> filter, AbstractMap.SimpleEntry<String, Boolean> orderField, INodeKey userId, boolean asObserver, Integer offset, Integer count) throws FloraOnException;

    <T> int findAnyByFilterCount(Class<T> type, Map<String, String> filter, INodeKey userId, boolean asObserver) throws FloraOnException;

    /**
     * Applies the same occurrence filter as findOccurrencesByFilter but returns only the count
     * @param filter
     * @param userId
     * @return
     * @throws FloraOnException
     */
    int findOccurrencesByFilterCount(Map<String, String> filter, INodeKey userId, boolean asObserver) throws FloraOnException;

    /**
     * Parses a compound occurrence filter into a Map.
     * @param filterText
     * @return
     */
    Map<String, String> parseFilterExpression(String filterText);

    /**
     * Finds all occurrences and iterate applying a custom in-house filter
     * @param filter
     * @return
     * @throws DatabaseException
     */
    Iterator<Occurrence> getFilteredOccurrences(OccurrenceFilter filter) throws DatabaseException;
}
