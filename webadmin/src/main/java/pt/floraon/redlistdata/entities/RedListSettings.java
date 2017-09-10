package pt.floraon.redlistdata.entities;

import pt.floraon.driver.Constants;
import pt.floraon.driver.entities.GeneralDBNode;

/**
 * The user settings for a given red list dataset.
 * TODO: here should go the protected areas and clipping polygon geojson!
 */
public class RedListSettings extends GeneralDBNode {
    private String territory;
    private boolean editionLocked = false;

    public String getTerritory() {
        return territory;
    }

    public void setTerritory(String territory) {
        this.territory = territory;
    }

    public boolean isEditionLocked() {
        return editionLocked;
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
