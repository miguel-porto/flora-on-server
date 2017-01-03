package pt.floraon.taxonomy;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.*;

import pt.floraon.driver.FloraOnException;
import pt.floraon.taxonomy.entities.Territory;
import pt.floraon.driver.results.TaxEntAndNativeStatusResult;
import pt.floraon.server.FloraOnServlet;

public class ChecklistAdmin extends FloraOnServlet {
	private static final long serialVersionUID = 1L;

	@Override
	public void doFloraOnGet() throws ServletException, IOException, FloraOnException {
		/*response.setContentType("text/html");
		PrintWriter out = response.getWriter();*/
		
		boolean hasTaxonInfoModule = true;

		try {
			URL rdd = new URL(request.getScheme(), request.getServerName(), request.getServerPort(), request.getContextPath() + "/api/redlistdata");
			BufferedReader in = new BufferedReader(new InputStreamReader(rdd.openStream()));
			in.close();
		} catch (FileNotFoundException e) {
			hasTaxonInfoModule = false;
		}

		String what=request.getParameter("w");
		String query=request.getParameter("q");
		if(what==null) what="main";
		request.setAttribute("what", what);
		request.setAttribute("taxoninfomodule", hasTaxonInfoModule);
		if(query!=null) request.setAttribute("query", query);

		switch(what) {		// the 'w' parameter of the URL querystring
		case "main":	// CHECKLIST
			Integer offset=getParameterAsInteger("offset",0);
			List<TaxEntAndNativeStatusResult> nsr = driver.getListDriver().getAllSpeciesOrInferior(territory == null
					, TaxEntAndNativeStatusResult.class, false, territory, getParameterAsString("filter"), offset, PAGESIZE);

			if(territory==null)
				request.setAttribute("territory", "");
			else
				request.setAttribute("territory", " existing in "+driver.getNodeWorkerDriver().getTerritoryFromShortName(territory).getName());

			List<String> checklistTerritories=new ArrayList<>();
			for(Territory tv : driver.getChecklistTerritories())
				checklistTerritories.add(tv.getShortName());

			request.setAttribute("checklist", nsr);
			request.setAttribute("checklistTerritories", checklistTerritories);
			request.setAttribute("offset", offset);
			request.setAttribute("filter", getParameterAsString("filter"));
			request.setAttribute("PAGESIZE", PAGESIZE);
			break;
			
		case "graph":
			request.getRequestDispatcher("/graph.jsp").forward(request, response);
			return;
		}
		
		request.getRequestDispatcher("/main-checklist.jsp").forward(request, response);
	}
}