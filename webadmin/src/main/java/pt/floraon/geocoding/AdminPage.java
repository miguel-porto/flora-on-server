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
    public void doFloraOnGet() throws ServletException, IOException, FloraOnException {
        String what;

        request.setAttribute("what", what = getParameterAsString("w", "main"));

/*
        switch (what) {

        }
*/

        request.getRequestDispatcher("/main-admin.jsp").forward(request, response);
    }
}
