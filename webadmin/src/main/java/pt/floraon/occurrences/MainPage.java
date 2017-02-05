package pt.floraon.occurrences;

import pt.floraon.authentication.entities.User;
import pt.floraon.driver.FloraOnException;
import pt.floraon.redlistdata.ExternalDataProvider;
import pt.floraon.server.FloraOnServlet;
import pt.floraon.taxonomy.entities.TaxEnt;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

/**
 * Created by miguel on 05-02-2017.
 */
@WebServlet("/occurrences/*")
public class MainPage extends FloraOnServlet {
    @Override
    public void doFloraOnGet() throws ServletException, IOException, FloraOnException {
        String what;
        TaxEnt te;
/*
Gson gs = new GsonBuilder().setPrettyPrinting().create();
System.out.println(gs.toJson(getUser()));
*/
/*
        ListIterator<String> path;
        try {
            path = getPathIteratorAfter("occurrences");
        } catch (FloraOnException e) {
            // no territory specified
            request.setAttribute("what", "addterritory");
            request.setAttribute("territories", driver.getListDriver().getAllTerritories(null));
            request.getRequestDispatcher("/main-redlistinfo.jsp").forward(request, response);
            return;
        }

        String territory = path.next();
        request.setAttribute("territory", territory);
        final ExternalDataProvider foop = driver.getRedListData().getExternalDataProviders().get(0);
*/

//        request.setAttribute("what", what = getParameterAsString("w", "main"));
        request.getRequestDispatcher("/main-occurrences.jsp").forward(request, response);

    }
}
