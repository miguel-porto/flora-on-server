package pt.floraon.jobs;

import pt.floraon.driver.FloraOnException;
import pt.floraon.driver.IFloraOn;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by miguel on 10-11-2016.
 */
public interface JobFileDownload {
    void run(IFloraOn driver, OutputStream out) throws FloraOnException, IOException;
    String getState();
}
