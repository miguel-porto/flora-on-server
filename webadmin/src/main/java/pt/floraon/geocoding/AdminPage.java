package pt.floraon.geocoding;

import pt.floraon.driver.FloraOnException;
import pt.floraon.server.FloraOnServlet;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import java.io.IOException;

/**
 * Created by miguel on 16-04-2017.
 */
@WebServlet("/adminpage/*")
public class AdminPage extends FloraOnServlet {
    @Override
    public void doFloraOnGet(ThisRequest thisRequest) throws ServletException, IOException, FloraOnException {
        String what;

        thisRequest.request.setAttribute("what", what = thisRequest.getParameterAsString("w", "main"));

/*
        switch (what) {

        }
*/

        thisRequest.request.getRequestDispatcher("/main-admin.jsp").forward(thisRequest.request, thisRequest.response);
    }
}
