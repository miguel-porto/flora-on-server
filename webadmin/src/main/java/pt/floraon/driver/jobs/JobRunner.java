package pt.floraon.driver.jobs;

import pt.floraon.authentication.entities.User;
import pt.floraon.driver.FloraOnException;

import java.util.Date;

/**
 * Created by miguel on 10-11-2016.
 */
public interface JobRunner extends Runnable {
    String getID();
    Boolean isReady() throws FloraOnException;
    Boolean hasError();
    String getErrorMessage();
    String getState() throws FloraOnException;
    String getDescription();
    User getOwner();
    boolean isFileDownload();
    Job getJob();
    String getDateSubmitted();
}
