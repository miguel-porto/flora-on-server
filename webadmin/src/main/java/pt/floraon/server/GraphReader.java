package pt.floraon.server;

import java.io.IOException;
import java.util.ListIterator;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;

import com.google.gson.JsonObject;

import pt.floraon.driver.Constants;
import pt.floraon.driver.FloraOnException;
import pt.floraon.driver.Constants.Facets;
import pt.floraon.driver.Constants.RelTypes;
import pt.floraon.driver.Constants.TaxonRanks;
import pt.floraon.driver.Constants.TerritoryTypes;
import pt.floraon.entities.TaxEnt;
/**
 * Services for the graph manager
 * @author miguel
 *
 */
@WebServlet("/graph/*")
public class GraphReader extends FloraOnServlet {
	private static final long serialVersionUID = 1L;

	@Override
	public void doFloraOnGet() throws ServletException, IOException, FloraOnException {
		String id,query;
		StringBuilder rk=new StringBuilder();
		ListIterator<String> partIt=this.getPathIteratorAfter("graph");

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
			Integer depth=getParameterAsInteger("d",1);
			errorIfAllNull(
				response,
				id = getParameterAsString("id"),
				query = getParameterAsString("q"));

			String infacets[];
			String facets=getParameterAsString("f");
			// get the facets we want to show
			if(facets==null || facets.equals(""))
				infacets=new String[]{"taxonomy"};
			else
				infacets=facets.split(",");
			
			Facets[] fac=new Facets[infacets.length];
			for(int i=0;i<infacets.length;i++) fac[i]=Facets.valueOf(infacets[i].toUpperCase());

			if(id==null) {
				TaxEnt te=driver.getNodeWorkerDriver().getTaxEntByName(query);
				if(te==null)
					success(driver.getNodeWorkerDriver().getNeighbors(null,fac,depth).toJsonObject());
				else
					success(driver.getNodeWorkerDriver().getNeighbors(driver.asNodeKey(te.getID()),fac,depth).toJsonObject());
			} else {
				String[] ids=id.split(",");
				if(ids.length==1)
					success(driver.getNodeWorkerDriver().getNeighbors(driver.asNodeKey(ids[0]),fac,depth).toJsonObject());
				else
					success(driver.getNodeWorkerDriver().getRelationshipsBetween(ids,fac).toJsonObject());
			}
			break;
		}
	}
}
