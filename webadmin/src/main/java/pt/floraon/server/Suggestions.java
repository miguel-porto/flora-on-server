package pt.floraon.server;

import java.io.IOException;

import javax.servlet.ServletException;

import pt.floraon.driver.FloraOnException;
import pt.floraon.driver.results.ResultProcessor;
import pt.floraon.driver.results.SimpleTaxEntResult;

public class Suggestions extends FloraOnServlet {
	private static final long serialVersionUID = 1L;

	@Override
	public void doFloraOnGet() throws ServletException, IOException, FloraOnException {
		String query=getParameterAsString("q");
		if(query==null) return;
		String lim=getParameterAsString("limit");
		Integer limit;
		try {
			limit=Integer.parseInt(lim);
		} catch (NumberFormatException e) {
			limit=null;
		}
		ResultProcessor<SimpleTaxEntResult> rp1 = new ResultProcessor<SimpleTaxEntResult>(driver.getQueryDriver().findSuggestions(query, limit));
		response.setContentType("text/html");
		response.getWriter().println(rp1.toHTMLList("suggestions"));
	}

}
