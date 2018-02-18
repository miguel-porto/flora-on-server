package pt.floraon.driver.jobs;

import pt.floraon.authentication.entities.User;
import pt.floraon.driver.FloraOnException;
import pt.floraon.driver.interfaces.IFloraOn;

import java.io.IOException;

/**
 * Created by miguel on 10-11-2016.
 */
public interface JobTask extends Job {
    void run(IFloraOn driver) throws FloraOnException, IOException;
}
