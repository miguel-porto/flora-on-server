package pt.floraon.redlisttaxoninfo;

import pt.floraon.driver.FloraOnException;
import pt.floraon.server.FloraOnServlet;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import java.io.IOException;

/**
 * Created by miguel on 01-11-2016.
 */
@WebServlet("/api/redlisttaxoninfo/*")
public class TaxonInfoApi extends FloraOnServlet {
    @Override
    public void doFloraOnGet() throws ServletException, IOException, FloraOnException {
        success("Im");
    }
}
