package pt.floraon.server;

import java.io.IOException;
import java.util.ListIterator;

import javax.servlet.ServletException;

import com.arangodb.ArangoException;

import pt.floraon.driver.FloraOnException;

public class NodeReader extends FloraOnServlet {
	private static final long serialVersionUID = 1L;

	@Override
	public void doFloraOnGet()
			throws ServletException, IOException, ArangoException, FloraOnException {
		
		ListIterator<String> partIt=this.getPathIterator(request);
		while(!partIt.next().equals("read"));
		
		switch(partIt.next()) {
		case "getallcharacters":
			success(graph.dbNodeWorker.getAllCharacters().toJsonObject());
			break;

		case "getallterritories":
			success(graph.dbGeneralQueries.getAllTerritoriesGraph(null).toJsonObject());
			break;
		}
	}
}
