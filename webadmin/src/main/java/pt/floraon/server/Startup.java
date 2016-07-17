package pt.floraon.server;

import java.io.File;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import pt.floraon.arangodriver.FloraOnArangoDriver;
import pt.floraon.driver.FloraOnException;
import pt.floraon.driver.FloraOn;

/**
 * Runs on webapp servlet startup
 * @author miguel
 *
 */
public class Startup implements ServletContextListener {
	//public static FloraOnInt FloraOnDriver;
	
	public void contextInitialized(ServletContextEvent sce) {
		FloraOn FloraOnDriver=null;
		File dir = new File(sce.getServletContext().getRealPath("/")).getParentFile();
		try {
			FloraOnDriver = new FloraOnArangoDriver("flora", dir.getAbsolutePath());
		} catch (FloraOnException e) {
			e.printStackTrace();
			System.err.println("ERROR: "+e.getMessage());
			return;
		}
		sce.getServletContext().setAttribute("driver", FloraOnDriver);
	}
}
