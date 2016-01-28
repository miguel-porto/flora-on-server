package pt.floraon.server;
import java.io.*;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

import javax.servlet.*;
import javax.servlet.http.*;

import com.arangodb.ArangoException;

import pt.floraon.driver.ArangoKey;
import pt.floraon.driver.FloraOnDriver;
import pt.floraon.driver.FloraOnException;
import pt.floraon.driver.TaxonomyException;
import pt.floraon.entities.TaxEnt;
import pt.floraon.entities.TaxEntVertex;
import pt.floraon.entities.TerritoryVertex;
import pt.floraon.results.NativeStatusResult;
import pt.floraon.results.ResultProcessor;

public class WebAdmin extends FloraOnServlet {
	private static final long serialVersionUID = 1L;
	
	@Override
	public void doFloraOnGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, ArangoException, FloraOnException {
		/*response.setContentType("text/html");
		PrintWriter out = response.getWriter();*/
		String what=request.getParameter("w"),from;
		String query=request.getParameter("q");
		request.setAttribute("what", what);
		if(query!=null) request.setAttribute("query", query);
		if(what==null) {
			//out.print("<div id=\"main\"><h2>This is the Flora-On taxonomy manager</h2><p>To edit the checklist, choose <a href=\"?w=families\">Family tree</a> on the left.</p></div>");
			request.getRequestDispatcher("/main.jsp").forward(request, response);
			return;
		}
		switch(what) {		// the 'w' parameter of the URL querystring
		case "main":	// CHECKLIST
			if( (from=request.getParameter("offset"))==null ) from="0";
			Integer offset=Integer.parseInt(from);
			if(territory==null)
				request.setAttribute("territory", "");
			else
				request.setAttribute("territory", " existing in "+graph.dbNodeWorker.getTerritoryFromShortName(territory).getName());
			request.setAttribute("offset", offset);
			request.setAttribute("PAGESIZE", PAGESIZE);
			break;
			
		case "graph":
			request.getRequestDispatcher("/graph.jsp").forward(request, response);
			return;
		}
		
		request.getRequestDispatcher("/main.jsp").forward(request, response);
	}
}