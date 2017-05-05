package pt.floraon.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;

import pt.floraon.authentication.entities.User;
import pt.floraon.driver.FloraOnException;
import pt.floraon.driver.results.ResultProcessor;
import pt.floraon.driver.results.SimpleTaxEntResult;
import pt.floraon.redlistdata.FieldValues;
import pt.floraon.redlistdata.RedListEnums;
import pt.floraon.taxonomy.entities.TaxEnt;

public class Suggestions extends FloraOnServlet {
	private static final long serialVersionUID = 1L;

	@Override
	public void doFloraOnGet() throws ServletException, IOException, FloraOnException {
		String query = getParameterAsString("q");
		if(query == null) return;
		Integer limit = getParameterAsInteger("limit", null);
		response.setContentType("text/html");
		PrintWriter pw = response.getWriter();
		if(pw == null) return;

		switch(getParameterAsString("what", "taxon")) {
			case "taxon":
				Iterator<TaxEnt> ite = driver.getQueryDriver().findTaxonSuggestions(query, limit);
				TaxEnt te;
				pw.print("<ul class=\"suggestions\">");
				while (ite.hasNext()) {
					te = ite.next();
					pw.print("<li" + (te.getCurrent() == null ? "" : (te.getCurrent() ? "" : " class=\"notcurrent\""))
							+ " data-key=\"" + te.getID() + "\"><i>" + te.getName()
							+ (te.getAnnotation() == null ? "" : " [" + te.getAnnotation() + "]")
							+ (te.getSensu() == null ? "" : " sensu " + te.getSensu()) + "</i></li>");
//							+(this.taxent.getAuthor() == null ? "" : " "+this.taxent.getAuthor())+"</li>";
				}
				pw.print("</ul>");
				pw.flush();
				break;

			case "user":
				Iterator<User> it = driver.getQueryDriver().findUserSuggestions(query, limit);
				User u;
				pw.print("<ul class=\"suggestions\">");
				while(it.hasNext()) {
					u = it.next();
					pw.print("<li data-key=\"" + u.getID() + "\">");
					pw.print(u.getName());
					pw.print("</li>");
				}
				pw.print("</ul>");
				break;

			case "threats":
				List<String> options = new ArrayList<>();
				for(RedListEnums.ThreatCategories tc : RedListEnums.ThreatCategories.values()) {
					if(FieldValues.getString(tc.getLabel()).toLowerCase().contains(query.toLowerCase()))
						options.add(FieldValues.getString(tc.getLabel()));
				}
				pw.print("<ul class=\"suggestions\">");
				Iterator<String> it1 = options.iterator();
				while(it1.hasNext()) {
					pw.print("<li>");
					pw.print(it1.next());
					pw.print("</li>");
				}
				pw.print("</ul>");
				break;
		}
		pw.close();
	}

}
