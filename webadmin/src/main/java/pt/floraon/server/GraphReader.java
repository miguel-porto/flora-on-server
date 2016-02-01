package pt.floraon.server;

import java.io.IOException;
import java.util.ListIterator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.arangodb.ArangoException;
import com.google.gson.JsonObject;

import pt.floraon.driver.Constants;
import pt.floraon.driver.FloraOnException;
import pt.floraon.driver.Constants.Facets;
import pt.floraon.driver.Constants.RelTypes;
import pt.floraon.driver.Constants.TaxonRanks;
import pt.floraon.driver.Constants.TerritoryTypes;
import pt.floraon.entities.TaxEnt;

public class GraphReader extends FloraOnServlet {
	private static final long serialVersionUID = 1L;

	@Override
	public void doFloraOnGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, ArangoException, FloraOnException {
		String id,query;
		StringBuilder rk=new StringBuilder();

		ListIterator<String> partIt=this.getPathIterator(request);	

		while(!partIt.next().equals("graph"));
		
		switch(partIt.next()) {
		case "reference":
			if(!partIt.hasNext()) {
				error("Choose one of: all, ranks, territorytypes");
				return;
			}
			
			switch(partIt.next()) {
			case "all":
				JsonObject jobj=new JsonObject();
				JsonObject ranks=new JsonObject();	// a map to convert rank numbers to names
				for(TaxonRanks e : Constants.TaxonRanks.values()) {
					ranks.addProperty(e.getValue().toString(), e.toString());
				}
				jobj.add("rankmap", ranks);
				
				ranks=new JsonObject();
				for(RelTypes art:Constants.RelTypes.values()) {
					ranks.addProperty(art.toString(), art.getFacet().toString());
				}
				jobj.add("facets", ranks);
				success(jobj);
				break;

			case "ranks":
				rk.append("<select name=\"taxonrank\">");
				for(TaxonRanks e : Constants.TaxonRanks.values()) {
					rk.append("<option value=\""+e.getValue().toString()+"\">"+e.getName()+"</option>");
				}
				rk.append("</select>");
				response.getWriter().print(rk.toString());
				break;
				
			case "territorytypes":
				rk.append("<select name=\"territorytype\"><option value=\"NOT_SET\">undefined</option>");
				for(TerritoryTypes e : Constants.TerritoryTypes.values()) {
					rk.append("<option value=\""+e.toString()+"\">"+e.toString()+"</option>");
				}
				rk.append("</select>");
				response.getWriter().print(rk.toString());
				break;
			}
			break;

		case "getneighbors":
			id=request.getParameter("id");
			query=request.getParameter("q");
			if(errorIfAllNull(response, id, query)) return;

			String infacets[];
			String facets=request.getParameter("f");
			// get the facets we want to show
			if(facets==null || facets.equals(""))
				infacets=new String[]{"taxonomy"};
			else
				infacets=facets.split(",");
			
			Facets[] fac=new Facets[infacets.length];
			for(int i=0;i<infacets.length;i++) fac[i]=Facets.valueOf(infacets[i].toUpperCase());

			if(id==null) {
				TaxEnt te=graph.dbNodeWorker.findTaxEnt(query);
				if(te==null)
					success(graph.dbNodeWorker.getNeighbors(null,fac).toJsonObject());
				else
					success(graph.dbNodeWorker.getNeighbors(te.getID(),fac).toJsonObject());
			} else {
				String[] ids=id.split(",");
				if(ids.length==1)
					success(graph.dbNodeWorker.getNeighbors(ids[0],fac).toJsonObject());
				else
					success(graph.dbNodeWorker.getRelationshipsBetween(ids,fac).toJsonObject());
			}
			break;
		}
	}
}
