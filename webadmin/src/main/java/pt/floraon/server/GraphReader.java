package pt.floraon.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import pt.floraon.driver.Constants;
import pt.floraon.driver.FloraOnException;
import pt.floraon.driver.Constants.Facets;
import pt.floraon.driver.Constants.RelTypes;
import pt.floraon.driver.Constants.TaxonRanks;
import pt.floraon.driver.Constants.TerritoryTypes;
import pt.floraon.driver.results.GraphUpdateResult;
import pt.floraon.taxonomy.entities.TaxEnt;
/**
 * Services for the graph manager
 * @author miguel
 *
 */
@WebServlet("/graph/*")
public class GraphReader extends FloraOnServlet {
	private static final long serialVersionUID = 1L;

	@Override
	public void doFloraOnGet(ThisRequest thisRequest) throws ServletException, IOException, FloraOnException {
		String id,query;
		StringBuilder rk=new StringBuilder();
		ListIterator<String> partIt=thisRequest.getPathIteratorAfter("graph");
		PrintWriter pw;

		switch(partIt.next()) {
		case "reference":
			if(!partIt.hasNext()) {
				thisRequest.error("Choose one of: all, ranks, territorytypes");
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
				thisRequest.success(jobj);
				break;

			case "ranks":
				rk.append("<select name=\"taxonrank\">");
				for(TaxonRanks e : Constants.TaxonRanks.values()) {
					rk.append("<option value=\""+e.getValue().toString()+"\">"+e.getName()+"</option>");
				}
				rk.append("</select>");
				pw = thisRequest.response.getWriter();
				pw.print(rk.toString());
				pw.flush();
 				break;
				
			case "territorytypes":
				rk.append("<select name=\"territorytype\"><option value=\"NOT_SET\">undefined</option>");
				for(TerritoryTypes e : Constants.TerritoryTypes.values()) {
					rk.append("<option value=\""+e.toString()+"\">"+e.toString()+"</option>");
				}
				rk.append("</select>");
				pw = thisRequest.response.getWriter();
				pw.print(rk.toString());
				pw.flush();
				break;
			}
			break;

		case "getneighbors":
			Integer depth=thisRequest.getParameterAsInteger("d",1);
			errorIfAllNull(
					thisRequest.response,
				id = thisRequest.getParameterAsString("id"),
				query = thisRequest.getParameterAsString("q"));

			String infacets[];
			String facets=thisRequest.getParameterAsString("f");
			// get the facets we want to show
			if(facets==null || facets.equals(""))
				infacets=new String[]{"taxonomy"};
			else
				infacets=facets.split(",");
			
			Facets[] fac=new Facets[infacets.length];
			for(int i=0;i<infacets.length;i++) fac[i]=Facets.valueOf(infacets[i].toUpperCase());

			if(id==null) {
//				System.out.println(TaxEnt.parse(query).toJson().toString());
//				TaxEnt te=driver.getNodeWorkerDriver().getSingleTaxEntOrNull(TaxEnt.parse(query));
				List<TaxEnt> te = driver.getNodeWorkerDriver().getTaxEnt(TaxEnt.parse(query), null);
				if(te == null || te.size() == 0)
					thisRequest.success(driver.getNodeWorkerDriver().getNeighbors(null,fac,depth).toJsonObject());
				else {
					Iterator<TaxEnt> itte = te.iterator();
					GraphUpdateResult out = driver.getNodeWorkerDriver().getNeighbors(driver.asNodeKey(itte.next().getID()), fac, depth);
					while(itte.hasNext()) {
						out.mergeWith(driver.getNodeWorkerDriver().getNeighbors(driver.asNodeKey(itte.next().getID()), fac, depth));
					}
					thisRequest.success(out.toJsonObject());
				}
			} else {
				String[] ids=id.split(",");
				if(ids.length==1)
					thisRequest.success(driver.getNodeWorkerDriver().getNeighbors(driver.asNodeKey(ids[0]),fac,depth).toJsonObject());
				else
					thisRequest.success(driver.getNodeWorkerDriver().getRelationshipsBetween(ids,fac).toJsonObject());
			}
			break;
		}
	}
}
