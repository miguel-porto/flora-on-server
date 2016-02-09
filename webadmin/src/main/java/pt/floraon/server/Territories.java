package pt.floraon.server;

import java.io.IOException;
import java.util.ListIterator;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;

import pt.floraon.driver.FloraOnException;
import pt.floraon.driver.INodeKey;
import pt.floraon.driver.Constants.NativeStatus;
import pt.floraon.entities.Territory;

@MultipartConfig
public class Territories extends FloraOnServlet {
	private static final long serialVersionUID = 1L;

	@Override
	public void doFloraOnPost() throws ServletException, IOException, FloraOnException {
		if(!isAuthenticated()) {
			error("You must login to do this operation!");
			return;
		}

		ListIterator<String> part=this.getPathIteratorAfter("territories");
		String to, query;

		if(!part.hasNext()) {
			error("Choose one of: set");
			return;
		}
		switch(part.next()) {
		case "set":
			INodeKey from=getParameterAsKey("taxon");		// the taxon id
			to=getParameterAsString("territory");
			query=getParameterAsString("status");
			if(errorIfAnyNull(response, from, to, query)) return;
			Territory terr=NWD.getTerritoryFromShortName(to);
			//Territory terr=new Territory(graph, graph.dbNodeWorker.getTerritoryFromShortName(to));
			NativeStatus nst=null;
			if(!query.toUpperCase().equals("NULL")) nst=NativeStatus.valueOf(query.toUpperCase());
			driver.wrapTaxEnt(from).setNativeStatus(driver.asNodeKey(terr.getID()), nst);
			//terr.setTaxEntNativeStatus(ArangoKey.fromString(from), nst);
			success( nst==null ? "NULL" : nst.toString().toUpperCase());
			break;
			
		default:
			error("Command not found");
			break;
		}

	}
}
