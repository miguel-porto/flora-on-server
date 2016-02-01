package pt.floraon.server;

import java.io.IOException;
import java.util.ListIterator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.arangodb.ArangoException;

import pt.floraon.driver.FloraOnException;

public class NodeReader extends FloraOnServlet {
	private static final long serialVersionUID = 1L;

	@Override
	public void doFloraOnGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException, ArangoException, FloraOnException {
		String part;
		
		ListIterator<String> partIt=this.getPathIterator(request);
		while(!(part=partIt.next()).equals("read"));
		part=partIt.next();
		
		switch(part) {
		case "getallcharacters":
			//response.getWriter().print(graph.dbNodeWorker.getAllCharacters().toString());
			success(graph.dbNodeWorker.getAllCharacters().toJsonObject());
			break;

		case "getallterritories":
			success(graph.dbGeneralQueries.getAllTerritoriesGraph(null).toJsonObject());
			break;
		}
	}
}
