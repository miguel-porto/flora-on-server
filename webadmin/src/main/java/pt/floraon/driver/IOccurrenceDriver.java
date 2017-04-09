package pt.floraon.driver;

import pt.floraon.occurrences.entities.Inventory;
import pt.floraon.occurrences.entities.InventoryList;

import java.util.Iterator;

/**
 * Created by miguel on 24-03-2017.
 */
public interface IOccurrenceDriver {
    void createInventory(Inventory inventory);

    /**
     * Gets all the occurrences of the given taxon and all its subtaxa. Multi-species inventories are decomposed in
     * single-species, which are output one at a time.
     * @param taxEntId
     * @return
     * @throws DatabaseException
     */
    Iterator<Inventory> getOccurrencesOfTaxon(INodeKey taxEntId) throws DatabaseException;

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
     * observer). Inventories as disaggregated into individual occurrences. NOTE: in case of empty inventories, one
     * (empty) occurrence is returned, to allow the user to populate the empty inventory with taxa.
     * @param authorId
     * @param offset
     * @param count
     * @return
     * @throws DatabaseException
     */
    Iterator<Inventory> getOccurrencesOfObserver(INodeKey authorId, Integer offset, Integer count) throws DatabaseException;

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
     * are set in the passedobject, and errors are stored.
     * @param inventories
     * @throws FloraOnException
     */
    void matchTaxEntNames(InventoryList inventories) throws FloraOnException;

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
}
