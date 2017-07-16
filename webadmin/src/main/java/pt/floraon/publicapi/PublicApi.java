package pt.floraon.publicapi;

import pt.floraon.authentication.entities.User;
import pt.floraon.driver.FloraOnException;
import pt.floraon.driver.INodeKey;
import pt.floraon.geometry.PolygonTheme;
import pt.floraon.redlistdata.OccurrenceProcessor;
import pt.floraon.redlistdata.dataproviders.SimpleOccurrenceDataProvider;
import pt.floraon.server.FloraOnServlet;
import pt.floraon.taxonomy.entities.TaxEnt;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ListIterator;

/**
 * Created by miguel on 15-07-2017.
 */
@WebServlet("/api/*")
public class PublicApi extends FloraOnServlet {
    @Override
    public void doFloraOnGet(ThisRequest thisRequest) throws ServletException, IOException, FloraOnException {
        PrintWriter wr;
        ListIterator<String> path;
        try {
            path = thisRequest.getPathIteratorAfter("api");
        } catch (FloraOnException e) {
            thisRequest.error("Missing parameters.");
            return;
        }

        User user = thisRequest.getUser();

        switch(path.next()) {
            case "svgmap":
                INodeKey key = thisRequest.getParameterAsKey("taxon");
                boolean baseMap = thisRequest.getParameterAsBoolean("basemap", false);
                Integer squareSize = thisRequest.getParameterAsInteger("size", 10000);
                Integer borderWidth = thisRequest.getParameterAsInteger("border", 2);

/*
                if(squareSize < 10000 && !user.canVIEW_OCCURRENCES()) {
                    thisRequest.response.sendError(HttpServletResponse.SC_FORBIDDEN, "No public access for this resolution.");
                    return;
                }
*/
                if(key == null) return;
                TaxEnt te2 = driver.getNodeWorkerDriver().getNode(key, TaxEnt.class);
                for(SimpleOccurrenceDataProvider edp : driver.getRedListData().getSimpleOccurrenceDataProviders()) {
                    edp.executeOccurrenceQuery(te2);
                }
                thisRequest.response.setContentType("image/svg+xml; charset=utf-8");
                thisRequest.response.setCharacterEncoding("UTF-8");
                thisRequest.response.addHeader("Access-Control-Allow-Origin", "*");

                wr = thisRequest.response.getWriter();
                PolygonTheme cP = new PolygonTheme(pt.floraon.redlistdata.OccurrenceProcessor.class.getResourceAsStream("PT_buffer.geojson"), null);
                OccurrenceProcessor op1 = new OccurrenceProcessor(
                        driver.getRedListData().getSimpleOccurrenceDataProviders(), null, squareSize
                        , cP, 1991, null, false);
                op1.exportSVG(new PrintWriter(wr), true, false, baseMap, true, borderWidth);
                wr.flush();
                break;
        }
    }
}
