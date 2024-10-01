package pt.floraon.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;

import pt.floraon.authentication.entities.User;
import pt.floraon.driver.FloraOnException;
import pt.floraon.geocoding.entities.MatchedToponym;
import pt.floraon.redlistdata.FieldValues;
import pt.floraon.redlistdata.threats.ThreatCategory;
import pt.floraon.taxonomy.entities.TaxEnt;

public class Suggestions extends FloraOnServlet {
	private static final long serialVersionUID = 1L;

	@Override
	public void doFloraOnGet(ThisRequest thisRequest) throws ServletException, IOException, FloraOnException {
		String query = thisRequest.getParameterAsString("q");
		if(query == null) return;
		Integer limit = thisRequest.getParameterAsInteger("limit", null);
		thisRequest.response.setContentType("text/html");
		PrintWriter pw = thisRequest.response.getWriter();
		if(pw == null) return;

		switch(thisRequest.getParameterAsString("what", "taxon")) {
			case "taxon":
				Iterator<TaxEnt> ite = driver.getQueryDriver().findTaxonSuggestions(query, limit, thisRequest.getUser().getShowKingdoms());
				TaxEnt te;
				pw.print("<ul class=\"suggestions\">");
				while (ite.hasNext()) {
					te = ite.next();
					pw.print("<li" + (te.getCurrent() == null ? "" : (te.getCurrent() ? "" : " class=\"notcurrent\""))
							+ " data-key=\"" + te.getID() + "\">" + te.getNameWithAnnotationOnly(false) + "</li>");
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
				for(ThreatCategory tc : driver.getThreatEnum().getEnumerationCategories().values()) {
					if(FieldValues.getString(tc.getLabel()).toLowerCase().contains(query.toLowerCase()))
						options.add(FieldValues.getString(tc.getLabel()));
				}
				pw.print("<ul class=\"suggestions\">");
				for (String option : options) {
					pw.print("<li>");
					pw.print(option);
					pw.print("</li>");
				}
				pw.print("</ul>");
				break;

			case "toponym":
				List<MatchedToponym> topo = driver.getQueryDriver().findToponymSuggestions(query);
				if(topo.size() == 0) {
					pw.print("<p>Não foram encontrados resultados para a pesquisa, ou pesquisou menos de 3 letras</p>");
				} else {
					pw.print("<table>");
					for (MatchedToponym t : topo) {
						pw.print("<tr class=\"geoelement\"><td class=\"singleselect clickable\">");
						pw.print(t.getLocality() + " (" + t.getToponymType() + ") ");
						pw.print("</td><td class=\"coordinates\" data-lat=\"" + t.getLatitude() + "\" data-lng=\""
								+ t.getLongitude() + "\" data-label=\"" + t.getLocality() + "\">");
						pw.print(t.getLatitude() + ", " + t.getLongitude());
//					pw.print(" "+t.getLevenDist());
//					pw.println();
						pw.print("</td></tr>");
					}
					pw.print("</table>");
				}
				break;
		}
		pw.flush();
	}

}
