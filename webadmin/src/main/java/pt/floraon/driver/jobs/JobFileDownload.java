package pt.floraon.driver.jobs;

import pt.floraon.driver.FloraOnException;
import pt.floraon.driver.interfaces.IFloraOn;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by miguel on 10-11-2016.
 */
public interface JobFileDownload extends Job {
    void run(IFloraOn driver, OutputStream out) throws FloraOnException, IOException;
}
