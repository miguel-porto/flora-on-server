package pt.floraon.server;

import java.io.*;
import java.util.Properties;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import pt.floraon.arangodriver.FloraOnArangoDriver;
import pt.floraon.driver.FloraOnException;
import pt.floraon.driver.IFloraOn;

/**
 * Runs on webapp servlet startup
 * @author miguel
 *
 */
public class Startup implements ServletContextListener {
	//public static FloraOnInt FloraOnDriver;
	
	public void contextInitialized(ServletContextEvent sce) {
		IFloraOn FloraOnDriver;
		File dir = new File(sce.getServletContext().getRealPath("/")).getParentFile();
		Properties properties = new Properties();
		InputStream propStream;
		try {
			propStream = new FileInputStream(new File(dir.getAbsolutePath() + "/floraon.properties"));
			properties.load(propStream);
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("ERROR: "+e.getMessage());
			return;
		}

		try {
			FloraOnDriver = new FloraOnArangoDriver("flora", properties);
			FloraOnDriver.getRedListData().initializeRedListData(properties);
		} catch (FloraOnException e) {
			e.printStackTrace();
			System.err.println("ERROR: "+e.getMessage());
			return;
		}
		sce.getServletContext().setAttribute("driver", FloraOnDriver);
	}
}
