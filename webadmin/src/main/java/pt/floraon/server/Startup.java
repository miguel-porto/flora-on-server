package pt.floraon.server;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.arangodb.ArangoException;

import pt.floraon.driver.FloraOnDriver;

public class Startup implements ServletContextListener {
	public void contextInitialized(ServletContextEvent sce) {
		FloraOnDriver graph;
    	try {
    		graph=new FloraOnDriver("flora");
		} catch (ArangoException e2) {
			e2.printStackTrace();
			return;
		}
		sce.getServletContext().setAttribute("graph", graph);
	}
}
