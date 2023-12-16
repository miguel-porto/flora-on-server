package pt.floraon.server;

import pt.floraon.driver.FloraOnException;
import pt.floraon.driver.utils.StringUtils;
import pt.floraon.occurrences.Common;

import javax.servlet.ServletException;
import java.io.IOException;

/**
 * Created by miguel on 01-11-2016.
 */
public class MainPage extends FloraOnServlet {
    @Override
    public void doFloraOnGet(ThisRequest thisRequest) throws ServletException, IOException, FloraOnException {
/*
        if("map".equals(thisRequest.getParameterAsString("w"))) {
            TaxEnt tmpTe = driver.getNodeWorkerDriver().getDocument(thisRequest.getParameterAsKey("id"), TaxEnt.class);
            for (SimpleOccurrenceDataProvider edp : driver.getRedListData().getSimpleOccurrenceDataProviders())
                edp.executeOccurrenceQuery(tmpTe);
            
            PolygonTheme cP = new PolygonTheme(pt.floraon.redlistdata.occurrences.OccurrenceProcessor.class.getResourceAsStream("PT_buffer.geojson"), null);
            OccurrenceProcessor oP = new OccurrenceProcessor(
                    driver.getRedListData().getSimpleOccurrenceDataProviders(), null
                    , 10000, cP, null, null);

            thisRequest.response.setContentType("image/svg+xml");
            thisRequest.response.setCharacterEncoding("UTF-8");
            oP.exportSVG(thisRequest.response.getWriter(), true, false, false);
            thisRequest.response.getWriter().flush();
            return;
        }
*/
        String mpl = driver.getProperties().getProperty("mainPageLogo");
        thisRequest.request.setAttribute("mainPageLogo", mpl == null ? null : "images/" + mpl);
        thisRequest.request.setAttribute("mainPageFooter", driver.getProperties().getProperty("mainPageFooter"));
        String tmp = driver.getProperties().getProperty("mainMenu");
        thisRequest.request.setAttribute("mainMenu", StringUtils.isStringEmpty(tmp) ? null : tmp);
        thisRequest.request.setAttribute("favicons", driver.getProperties().getProperty("favicons"));
        thisRequest.request.setAttribute("redlistterritories", driver.getRedListData().getRedListTerritories());
        thisRequest.request.setAttribute("globalSettings", driver.getGlobalSettings());
        thisRequest.request.getRequestDispatcher("/main.jsp").forward(thisRequest.request, thisRequest.response);
    }
}
