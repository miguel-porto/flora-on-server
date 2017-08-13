package pt.floraon.driver.interfaces;

import java.util.Iterator;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import pt.floraon.bibliography.entities.Reference;
import pt.floraon.driver.Constants;
import pt.floraon.driver.DatabaseException;
import pt.floraon.driver.FloraOnException;
import pt.floraon.driver.entities.DBEntity;
import pt.floraon.ecology.entities.Habitat;
import pt.floraon.taxonomy.entities.ChecklistEntry;
import pt.floraon.taxonomy.entities.EXISTS_IN;
import pt.floraon.driver.Constants.TaxonRanks;
import pt.floraon.driver.Constants.TerritoryTypes;
import pt.floraon.taxonomy.entities.TaxEnt;
import pt.floraon.taxonomy.entities.Territory;
import pt.floraon.driver.results.GraphUpdateResult;
import pt.floraon.driver.results.SimpleTaxEntResult;

public interface IListDriver {
	/**
	 * Gets the complete list of taxa in the DB
	 * @return
	 */
	Iterator<ChecklistEntry> getCheckList() throws FloraOnException;

    GraphUpdateResult getGraphWholeCollection(Constants.NodeTypes nodeType, Constants.Facets[] facets) throws FloraOnException;

    /**
     * Gets the territories that should be listed in the checklist
     * @return
     * @throws FloraOnException
     */
    List<Territory> getChecklistTerritories() throws FloraOnException;

	/**
	 * Fetches all species or inferior taxa that exist in the given {@link Territory} (given by shortName) or all if null.
	 * Note that this only returns taxa directly assigned to the territory with a {@link EXISTS_IN} link.
	 * @param onlyCurrent Return only current TaxEnt?
	 * @param higherTaxa Return also higher taxa (up to species) or only the directly assigned to a territory?
	 * @param territory Short name of the territory. Can be <code>null</code>.
	 * @param offset Params for the LIMIT clause
	 * @param count Params for the LIMIT clause
	 * @return A List<TaxEnt>.
	 * @throws FloraOnException
	 */
	List<TaxEnt> getAllSpeciesOrInferiorTaxEnt(Boolean onlyCurrent, boolean higherTaxa, String territory, Integer offset, Integer count) throws FloraOnException;
	/**
	 * Gets all species or inferior ranks, optionally filtered by those that exist in the given territory.
	 * Note that when onlyLeafNodes is true and territory is not null, some taxa may be omitted from the list,
	 * namely those which have inferior taxa but are bond to a territory (and not the inferior taxa). 
	 * @param onlyLeafNodes true to return only the terminal nodes.
	 * @param territory The territory to filter taxa, or null if no filter is wanted.
	 * @return An Iterator of any class that extends SimpleNameResult
	 * @throws FloraOnException
	 */
	<T extends SimpleTaxEntResult> List<T> getAllSpeciesOrInferior(boolean onlyLeafNodes, Class<T> T, Boolean onlyCurrent, String territory, String filter, Integer offset, Integer count) throws FloraOnException;
	/**
	 * Gets all the taxent nodes of the given rank
	 * @param rank
	 * @return
	 * @throws FloraOnException
	 */
	Iterator<TaxEnt> getAllOfRank(TaxonRanks rank) throws FloraOnException;
	/**
     * Gets all territories.
     * @return
     * @throws FloraOnException
     */
	List<Territory> getAllTerritories(TerritoryTypes territoryType) throws FloraOnException;

    /*
        public List<ChecklistEntry> getCheckList() {
            // TODO the query is very slow!
            List<ChecklistEntry> chklst=new ArrayList<ChecklistEntry>();
            @SuppressWarnings("rawtypes")
            ArangoCursor<List> vertexCursor;
            GraphVerticesOptions gvo=new GraphVerticesOptions();
            List<String> vcr=new ArrayList<String>();
            vcr.add("taxent"); //$NON-NLS-1$
            gvo.setVertexCollectionRestriction(vcr);
            String query=String.format(
                AQLQueries.getString("ListDriver.1")
                   , OccurrenceConstants.TAXONOMICGRAPHNAME, OccurrenceConstants.RelTypes.PART_OF.toString(),OccurrenceConstants.CHECKLISTFIELDS);

            try {
                // traverse all isLeaf nodes outwards
                vertexCursor = database.query(query, null, null, List.class);

                ChecklistEntry chk;
                while (vertexCursor.hasNext()) {
                    @SuppressWarnings("unchecked")
                    List<LinkedTreeMap<String,Object>> entry1 = vertexCursor.next();
                    chk=new ChecklistEntry();
                    for(LinkedTreeMap<String,Object> tev:entry1) {
                        TaxEnt te=new TaxEnt(tev);
                        if(te.isSpeciesOrInferior()) {
                            if(chk.canonicalName==null) {
                                chk.taxon=te.getFullName();
                                chk.canonicalName=te.getName();
                            }
                        }
                        switch(te.getRank()) {
                        case GENUS:
                            chk.genus=te.getName();
                            break;
                        case FAMILY:
                            chk.family=te.getName();
                            break;
                        case ORDER:
                            chk.order=te.getName();
                            break;
                        default:
                            break;
                        }
                    }
                    chklst.add(chk);
                }
            } catch (ArangoDBException | FloraOnException e) {
                e.printStackTrace();
            }
            return chklst;
        }
    */
    <T extends DBEntity> Iterator<T> getAllDocumentsOfCollection(String collection, Class<T> cls) throws DatabaseException;

	<T extends DBEntity> List<T> getAllDocumentsOfCollectionAsList(String collection, Class<T> cls) throws DatabaseException;

	/**
     * Gets all territories and all the PART_OF relations between them
     * @param territoryType
     * @return
     * @throws FloraOnException
     */
	GraphUpdateResult getAllTerritoriesGraph(TerritoryTypes territoryType) throws FloraOnException;
	/**
	 * Gets all morphological characters
	 * @return
	 */
	GraphUpdateResult getAllCharacters();

	/**
	 * Gets all orphan taxa: taxa which have nothing more than inbound PART_OF
	 * @return
	 */
	Iterator<TaxEnt> getAllOrphanTaxa() throws FloraOnException;

	GraphUpdateResult getAllOrphanTaxaAsGUR() throws FloraOnException;

	/**
	 * Gets information about one given taxon.
	 * @param key
	 * @return
	 * @throws FloraOnException 
	 */
	JsonObject getTaxonInfo(INodeKey key) throws FloraOnException;
	/**
	 * Gets information about one given taxon.
	 * @param taxonName
	 * @return
	 * @throws FloraOnException 
	 */
	JsonArray getTaxonInfo(String taxonName, boolean onlyCurrent) throws FloraOnException;
	/**
	 * Gets information about one given taxon.
	 * @param oldId
	 * @return
	 * @throws FloraOnException
	 */
	JsonObject getTaxonInfo(int oldId) throws FloraOnException;

	/**
	 * Gets likely taxonomic errors in the graph.
	 * TODO: parameter to indicate what type of error
	 * @return
	 * @throws FloraOnException
	 */
	Iterator<TaxEnt> getTaxonomicErrors() throws FloraOnException;

	/**
	 * Gets the immediate children of the given TaxEnt node
	 * @return
	 * @throws FloraOnException
	 */
	Iterator<TaxEnt> getChildrenTaxEnt(INodeKey parentId) throws FloraOnException;

	Iterator<Habitat> getChildrenHabitats(INodeKey parentId) throws DatabaseException;

	Iterator<Habitat> getHabitatsOfLevel(int level) throws DatabaseException;

    Iterator<Reference> findReferencesWithText(String query) throws DatabaseException;
}
