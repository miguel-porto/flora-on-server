package pt.floraon.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;

import javax.servlet.ServletException;

import pt.floraon.authentication.entities.User;
import pt.floraon.driver.FloraOnException;
import pt.floraon.driver.results.ResultProcessor;
import pt.floraon.driver.results.SimpleTaxEntResult;
import pt.floraon.taxonomy.entities.TaxEnt;

public class Suggestions extends FloraOnServlet {
	private static final long serialVersionUID = 1L;

	@Override
	public void doFloraOnGet() throws ServletException, IOException, FloraOnException {
		String query = getParameterAsString("q");
		if(query == null) return;
		Integer limit = getParameterAsInteger("limit", null);
		PrintWriter pw;

		switch(getParameterAsString("what", "taxon")) {
			case "taxon":
				Iterator<TaxEnt> ite = driver.getQueryDriver().findTaxonSuggestions(query, limit);
				TaxEnt te;
				response.setContentType("text/html");
				pw = response.getWriter();
				pw.print("<ul class=\"suggestions\">");
				while(ite.hasNext()) {
					te = ite.next();
					pw.print("<li" + (te.getCurrent() == null ? "" : (te.getCurrent() ? "" : " class=\"notcurrent\""))
							+ " data-key=\"" + te.getID() + "\"><i>" + te.getName()+"</i></li>");
//							+(this.taxent.getAuthor() == null ? "" : " "+this.taxent.getAuthor())+"</li>";
				}
				pw.print("</ul>");
				pw.flush();
				break;

			case "user":
				Iterator<User> it = driver.getQueryDriver().findUserSuggestions(query, limit);
				User u;
				response.setContentType("text/html");
				PrintWriter resp = response.getWriter();
				resp.print("<ul class=\"suggestions\">");
				while(it.hasNext()) {
					u = it.next();
					resp.print("<li data-key=\"" + u.getID() + "\">");
					resp.print(u.getName());
					resp.print("</li>");
				}
				resp.print("</ul>");
				break;
		}

	}

}
