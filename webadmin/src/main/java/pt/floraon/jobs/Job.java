package pt.floraon.jobs;

import java.io.IOException;
import java.io.OutputStream;

import pt.floraon.driver.FloraOnException;
import pt.floraon.driver.IFloraOn;

public interface Job {
	public void run(IFloraOn driver, OutputStream out) throws FloraOnException, IOException;
}
