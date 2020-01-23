package pt.floraon.taxonomy.servlets;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import javax.servlet.*;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import com.google.gson.Gson;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import pt.floraon.driver.FloraOnException;
import pt.floraon.driver.jobs.JobSubmitter;
import pt.floraon.occurrences.CSVFileProcessor;
import pt.floraon.redlistdata.FieldValues;
import pt.floraon.redlistdata.entities.RedListDataEntity;
import pt.floraon.redlistdata.jobs.SearchAndReplaceDryJob;
import pt.floraon.redlistdata.jobs.SearchAndReplaceJob;
import pt.floraon.taxonomy.TaxonomyImporter;
import pt.floraon.taxonomy.entities.Territory;
import pt.floraon.driver.results.TaxEntAndNativeStatusResult;
import pt.floraon.server.FloraOnServlet;

@MultipartConfig
public class ChecklistAdmin extends FloraOnServlet {
	private static final long serialVersionUID = 1L;

	@Override
	public void doFloraOnGet(ThisRequest thisRequest) throws ServletException, IOException, FloraOnException {
		/*response.setContentType("text/html");
		PrintWriter out = response.getWriter();*/
		final HttpServletRequest request = thisRequest.request;
		String territory = null;
/*
		boolean hasTaxonInfoModule = true;

		try {
			URL rdd = new URL(request.getScheme(), request.getServerName(), request.getServerPort(), request.getContextPath() + "/api/redlistdata");
			BufferedReader in = new BufferedReader(new InputStreamReader(rdd.openStream()));
			in.close();
		} catch (FileNotFoundException e) {
			hasTaxonInfoModule = false;
		}
*/

		String what=request.getParameter("w");
		String query=request.getParameter("q");
		if(what==null) what="main";
		request.setAttribute("what", what);
//		request.setAttribute("taxoninfomodule", hasTaxonInfoModule);
		if(query!=null) request.setAttribute("query", query);

		switch(what) {		// the 'w' parameter of the URL querystring
		case "main":	// CHECKLIST
			Integer offset=thisRequest.getParameterAsInteger("offset", 0);
			List<TaxEntAndNativeStatusResult> nsr = driver.getListDriver().getAllSpeciesOrInferior(territory == null
					, TaxEntAndNativeStatusResult.class, false, territory, thisRequest.getParameterAsString("filter"), offset, PAGESIZE);

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
			request.setAttribute("filter", thisRequest.getParameterAsString("filter"));
			request.setAttribute("PAGESIZE", PAGESIZE);
			break;
			
		case "graph":
			request.getRequestDispatcher("/graph.jsp").forward(request, thisRequest.response);
			return;
		}
		
		request.getRequestDispatcher("/main-checklist.jsp").forward(request, thisRequest.response);
	}

	public void doFloraOnPost(ThisRequest thisRequest) throws ServletException, IOException, FloraOnException {
		Part filePart;
		InputStream fileContent = null;

		String what = thisRequest.getParameterAsString("w");
		switch(what) {
			case "uploadChecklist":
				try {
					filePart = thisRequest.request.getPart("checklistTable");
					System.out.println("File size: " + filePart.getSize());

					if(filePart.getSize() == 0) throw new FloraOnException("You must select a file.");
					fileContent = filePart.getInputStream();
				} catch (IOException e) {
					e.printStackTrace();
					throw new FloraOnException("Some error.");
				}

				if(fileContent != null) {
					driver.getCSVFileProcessor().getTaxonomyImporter().uploadTaxonomyListFromStream2(fileContent, true);
					thisRequest.request.getRequestDispatcher("/main-checklist.jsp").forward(thisRequest.request, thisRequest.response);
				}
				break;
		}
	}
}