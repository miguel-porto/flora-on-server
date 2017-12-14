package pt.floraon.redlistdata.entities;

import pt.floraon.driver.Constants;
import pt.floraon.driver.entities.GeneralDBNode;
import pt.floraon.driver.interfaces.INodeKey;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The user settings for a given red list dataset.
 * TODO: here should go the protected areas and clipping polygon geojson!
 */
public class RedListSettings extends GeneralDBNode {
    private String territory;
    private boolean editionLocked = false;
    private Integer historicalThreshold;
    private Set<String> unlockedSheets = new HashSet<>();

    public Integer getHistoricalThreshold() {
        return historicalThreshold == null ? 1990 : historicalThreshold;
    }

    public void setHistoricalThreshold(Integer historicalThreshold) {
        this.historicalThreshold = historicalThreshold;
    }

    public String getTerritory() {
        return territory;
    }

    public Set<String> getUnlockedSheets() {
        return unlockedSheets;
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

    public boolean isEditionLocked() {
        return editionLocked;
    }

    public boolean isEditionLocked(String taxEntId) {
        return editionLocked && !this.unlockedSheets.contains(taxEntId);
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
