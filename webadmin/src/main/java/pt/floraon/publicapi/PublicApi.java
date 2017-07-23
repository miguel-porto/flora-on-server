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
import java.util.Calendar;
import java.util.List;
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
                Integer squareSize = thisRequest.getParameterAsInteger("size", 10000);
                Integer borderWidth = thisRequest.getParameterAsInteger("border", 2);
                boolean viewAll = "all".equals(thisRequest.getParameterAsString("view"));

                if(squareSize < 10000 && !user.canVIEW_OCCURRENCES()) {
                    thisRequest.response.sendError(HttpServletResponse.SC_FORBIDDEN, "No public access for this precision.");
                    return;
                }

                if(key == null) return;
                TaxEnt te2 = driver.getNodeWorkerDriver().getNode(key, TaxEnt.class);
                List<SimpleOccurrenceDataProvider> sodps = driver.getRedListData().getSimpleOccurrenceDataProviders();
                for(SimpleOccurrenceDataProvider edp : sodps)
                    edp.executeOccurrenceQuery(te2);

                thisRequest.response.setContentType("image/svg+xml; charset=utf-8");
                thisRequest.response.setCharacterEncoding("UTF-8");
                thisRequest.setCacheHeaders(60 * 10);
//                thisRequest.response.addHeader("Access-Control-Allow-Origin", "*");

                wr = thisRequest.response.getWriter();
                PolygonTheme cP = new PolygonTheme(pt.floraon.redlistdata.OccurrenceProcessor.class.getResourceAsStream("PT_buffer.geojson"), null);
                OccurrenceProcessor op1 = new OccurrenceProcessor(
                        sodps, null, squareSize
                        , cP, viewAll ? null : 1991, null, viewAll);
                op1.exportSVG(new PrintWriter(wr), true, false
                        , thisRequest.getParameterAsBoolean("basemap", false)
                        , true
                        , borderWidth
                        , thisRequest.getParameterAsBoolean("shadow", true));
                wr.flush();
                break;
        }
    }
}
