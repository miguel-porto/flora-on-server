package pt.floraon.jobs;

import java.io.OutputStream;

import com.arangodb.ArangoException;

import pt.floraon.driver.FloraOnDriver;
import pt.floraon.driver.FloraOnException;

public interface Job {
	public void run(FloraOnDriver driver, OutputStream out) throws ArangoException, FloraOnException;
}
