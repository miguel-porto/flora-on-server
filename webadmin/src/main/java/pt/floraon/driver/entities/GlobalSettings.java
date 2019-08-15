package pt.floraon.driver.entities;

import pt.floraon.driver.Constants;

/**
 * Global settings for this installation
 */
public class GlobalSettings extends GeneralDBNode {
    private boolean closedForAdminTasks;

    public boolean isClosedForAdminTasks() {
        return closedForAdminTasks;
    }

    public void setClosedForAdminTasks(boolean closedForAdminTasks) {
        this.closedForAdminTasks = closedForAdminTasks;
    }

    @Override
    public Constants.NodeTypes getType() {
        return Constants.NodeTypes.global_settings;
    }

    @Override
    public String getTypeAsString() {
        return this.getType().toString();
    }
}
