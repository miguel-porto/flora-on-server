package pt.floraon.server;

import java.io.*;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import com.google.common.collect.ImmutableMap;
import pt.floraon.arangodriver.FloraOnArangoDriver;
import pt.floraon.driver.FloraOnException;
import pt.floraon.driver.interfaces.IFloraOn;

/**
 * Runs on webapp servlet startup
 * @author miguel
 *
 */
@WebListener
public class Startup implements ServletContextListener {
	// The page size to display in occurrence manager
	private static final Map<String, String> PAGEROPTIONS =
			ImmutableMap.of("250", "250", "1000", "1000", "5000", "5000", "10000000", "all");

	//public static FloraOnInt FloraOnDriver;

	public void contextInitialized(ServletContextEvent event) {
		IFloraOn FloraOnDriver;
        ServletContext servletContext = event.getServletContext();

        servletContext.setAttribute("pagerOptions", PAGEROPTIONS);

		File dir = new File(servletContext.getRealPath("/")).getParentFile();
		Properties properties = new Properties();
		InputStream propStream;

		Locale.setDefault(Locale.forLanguageTag("pt"));

		try {
			propStream = new FileInputStream(new File(dir.getAbsolutePath() + "/floraon.properties"));
			properties.load(propStream);
		} catch (Throwable e) {
			e.printStackTrace();
			System.err.println("ERROR: "+e.getMessage());
			FloraOnDriver = new FloraOnArangoDriver(e.getMessage());
            servletContext.setAttribute("driver", FloraOnDriver);
			return;
		}

		try {
			FloraOnDriver = new FloraOnArangoDriver(properties);
			FloraOnDriver.getRedListData().initializeRedListData(properties);
		} catch (Throwable e) {
			e.printStackTrace();
			System.err.println("ERROR: "+e.getMessage());
			FloraOnDriver = new FloraOnArangoDriver(e.getMessage());
		}
        servletContext.setAttribute("driver", FloraOnDriver);
	}

	@Override
	public void contextDestroyed(ServletContextEvent servletContextEvent) {

	}
}
