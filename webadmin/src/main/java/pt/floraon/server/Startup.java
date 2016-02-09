package pt.floraon.server;

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
		try {
			FloraOnDriver = new FloraOnArangoDriver("flora");
		} catch (FloraOnException e) {
			e.printStackTrace();
		}
		sce.getServletContext().setAttribute("driver", FloraOnDriver);
	}
}
