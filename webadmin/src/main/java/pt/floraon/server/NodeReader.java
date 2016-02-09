package pt.floraon.server;

import java.io.IOException;
import java.util.ListIterator;

import javax.servlet.ServletException;

import pt.floraon.driver.FloraOnException;

public class NodeReader extends FloraOnServlet {
	private static final long serialVersionUID = 1L;

	@Override
	public void doFloraOnGet() throws ServletException, IOException, FloraOnException {
		ListIterator<String> partIt=this.getPathIteratorAfter("read");
		
		switch(partIt.next()) {
		case "getallcharacters":
			success(driver.getListDriver().getAllCharacters().toJsonObject());
			break;

		case "getallterritories":
			success(driver.getListDriver().getAllTerritoriesGraph(null).toJsonObject());
			break;
		}
	}
}
