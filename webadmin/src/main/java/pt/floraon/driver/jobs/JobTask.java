package pt.floraon.driver.jobs;

import pt.floraon.driver.FloraOnException;
import pt.floraon.driver.IFloraOn;

import java.io.IOException;

/**
 * Created by miguel on 10-11-2016.
 */
public interface JobTask {
    void run(IFloraOn driver, Object options) throws FloraOnException, IOException;
    String getState();
}
