package pt.floraon.redlistdata.entities;

import pt.floraon.driver.Constants;
import pt.floraon.driver.entities.GeneralDBNode;
import pt.floraon.driver.interfaces.INodeKey;
import pt.floraon.taxonomy.entities.TaxEnt;

import java.util.*;

/**
 * The user settings for a given red list dataset.
 * TODO: here should go the protected areas and clipping polygon geojson!
 */
public class RedListSettings extends GeneralDBNode {
    private String territory;
    private boolean editionLocked = false;
    private Set<String> tagsEditionLocked = new HashSet<>();
    private Integer historicalThreshold, editionsLastNDays;
    private Set<String> unlockedSheets = new HashSet<>();

    public Integer getHistoricalThreshold() {
        return historicalThreshold == null ? 1990 : historicalThreshold;
    }

    public Integer getEditionsLastNDays() {
        return editionsLastNDays == null ? 20 : editionsLastNDays;
    }

    public String getTerritory() {
        return territory;
    }

    public Set<String> getUnlockedSheets() {
        return unlockedSheets;
    }

    public Set<String> getLockedTags() {
        return tagsEditionLocked;
    }

    public void lockEditionForTag(String tag) {
        this.tagsEditionLocked.add(tag);
    }

    public void unlockEditionForTaxon(String taxEntId) {
        this.unlockedSheets.add(taxEntId);
    }

    public void removeUnlockEditionException(String taxEntId) {
        this.unlockedSheets.remove(taxEntId);
    }

    public void setTerritory(String territory) {
        this.territory = territory;
    }

    public boolean isEditionGloballyLocked() {
        return editionLocked;
    }

    public boolean isEditionLocked(String taxEntId, String[] tagsOfThisTaxon) {
        return (editionLocked || !Collections.disjoint(Arrays.asList(tagsOfThisTaxon), tagsEditionLocked));
//                && !this.unlockedSheets.contains(taxEntId);
    }

    public boolean isEditionLocked(RedListDataEntity rlde) {
        return isEditionLocked(rlde.getTaxEntID(), rlde.getTags());
    }

    public boolean isSheetUnlocked(String taxEntId) {
        return this.unlockedSheets.contains(taxEntId);
    }

    public void setEditionLocked(boolean editionLocked) {
        this.editionLocked = editionLocked;
    }

    @Override
    public Constants.NodeTypes getType() {
        return Constants.NodeTypes.redlist_settings;
    }

    @Override
    public String getTypeAsString() {
        return this.getType().toString();
    }
}
