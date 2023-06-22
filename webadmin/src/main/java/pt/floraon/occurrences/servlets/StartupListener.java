package pt.floraon.occurrences.servlets;

import com.google.common.collect.ImmutableMap;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.util.Map;

@WebListener
public class StartupListener implements ServletContextListener {
    private static final Map<String, String> PAGEROPTIONS =
            ImmutableMap.of("250", "250", "1000", "1000", "2000", "2000");

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        // The page size to display in occurrence manager
        servletContextEvent.getServletContext().setAttribute("pagerOptions", PAGEROPTIONS);
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {

    }
}
