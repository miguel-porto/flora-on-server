package pt.floraon.server;

import java.io.IOException;
import java.util.ListIterator;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.arangodb.ArangoException;

import pt.floraon.driver.ArangoKey;
import pt.floraon.driver.FloraOnException;
import pt.floraon.driver.Constants.NativeStatus;
import pt.floraon.entities.Territory;

@MultipartConfig
public class Territories extends FloraOnServlet {
	private static final long serialVersionUID = 1L;

	@Override
	public void doFloraOnPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, ArangoException, FloraOnException {
		ListIterator<String> part=this.getPathIterator(request);
		while(!part.next().equals("territories"));
		String from, to, query;
		
		if(!part.hasNext()) {
			error("Choose one of: set");
			return;
		}
		switch(part.next()) {
		case "set":
			from=getParameter(request,"taxon");		// the taxon id
			to=getParameter(request,"territory");
			query=getParameter(request,"status");
			if(errorIfAnyNull(response, from, to, query)) return;
			Territory terr=new Territory(graph, graph.dbNodeWorker.getTerritoryFromShortName(to));
			NativeStatus nst=null;
			if(!query.toUpperCase().equals("NULL")) nst=NativeStatus.valueOf(query.toUpperCase());
			terr.setTaxEntNativeStatus(ArangoKey.fromString(from), nst);
			success( nst==null ? "NULL" : nst.toString().toUpperCase());
			break;
			
		default:
			error("Command not found");
			break;
		}

	}
}
