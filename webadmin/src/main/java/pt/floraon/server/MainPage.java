package pt.floraon.server;

import pt.floraon.driver.FloraOnException;

import javax.servlet.ServletException;
import java.io.IOException;

/**
 * Created by miguel on 01-11-2016.
 */
public class MainPage extends FloraOnServlet {
    @Override
    public void doFloraOnGet(ThisRequest thisRequest) throws ServletException, IOException, FloraOnException {
        thisRequest.request.setAttribute("redlistterritories", driver.getRedListData().getRedListTerritories());
        if(driver.getListDriver().getAllOrphanTaxa().hasNext())
            thisRequest.request.setAttribute("orphan", true);
        thisRequest.request.getRequestDispatcher("/main.jsp").forward(thisRequest.request, thisRequest.response);
    }
}
