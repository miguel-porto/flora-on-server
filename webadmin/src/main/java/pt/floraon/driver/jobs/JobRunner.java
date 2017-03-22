package pt.floraon.driver.jobs;

import pt.floraon.driver.FloraOnException;

import java.util.Date;

/**
 * Created by miguel on 10-11-2016.
 */
public interface JobRunner extends Runnable {
    String getID();
    Boolean isReady() throws FloraOnException;
    String getState() throws FloraOnException;
    String getDescription();
    boolean isFileDownload();
    String getDateSubmitted();
}
