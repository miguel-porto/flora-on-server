package pt.floraon.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.arangodb.ArangoException;

import pt.floraon.driver.FloraOnException;
import pt.floraon.results.ResultProcessor;
import pt.floraon.results.SimpleNameResult;

public class Suggestions extends FloraOnServlet {
	private static final long serialVersionUID = 1L;

	@Override
	public void doFloraOnGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, ArangoException, FloraOnException {
		String query=getParameter(request,"q");
		if(query==null) return;
		String lim=getParameter(request, "limit");
		Integer limit;
		try {
			limit=Integer.parseInt(lim);
		} catch (NumberFormatException e) {
			limit=null;
		}
		ResultProcessor<SimpleNameResult> rp1 = new ResultProcessor<SimpleNameResult>(graph.dbGeneralQueries.findSuggestions(query, limit).iterator());
		response.setContentType("text/html");
		response.getWriter().println(rp1.toHTMLList("suggestions"));
	}

}
