package pt.floraon.server;

import pt.floraon.driver.FloraOnException;

import javax.servlet.ServletException;
import java.io.IOException;

/**
 * Created by miguel on 01-11-2016.
 */
public class MainPage extends FloraOnServlet {
    @Override
    public void doFloraOnGet() throws ServletException, IOException, FloraOnException {
        request.setAttribute("redlistterritories", driver.getRedListData().getRedListTerritories());
        request.getRequestDispatcher("/main.jsp").forward(request, response);
    }
}
