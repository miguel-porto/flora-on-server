package pt.floraon.taxonomy;

import java.io.IOException;
import java.util.ListIterator;

import javax.servlet.ServletException;

import pt.floraon.driver.FloraOnException;
import pt.floraon.driver.INodeKey;
import pt.floraon.server.FloraOnServlet;

/**
 * API endpoint to query node data
 * @author miguel
 *
 */
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
			
		case "taxoninfo":
			INodeKey key;
			String name;
			Integer oldid;
			errorIfAllNull(
				key = getParameterAsKey("key")
				, name = getParameterAsString("name")
				, oldid = getParameterAsInteger("oldid", null)
			);
			if(key != null)
				success(driver.getListDriver().getTaxonInfo(key));
			else if(name != null)
				success(driver.getListDriver().getTaxonInfo(name, getParameterAsBooleanNoNull("current")));
			else
				success(driver.getListDriver().getTaxonInfo(oldid));
			break;
			
		default:
			error("Path not found.");
		}
	}
}
