package pt.floraon.driver.jobs;

import pt.floraon.authentication.entities.User;

public interface Job {
    String getState();
    String getDescription();
    User getOwner();
}
