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
	public void doFloraOnGet(ThisRequest thisRequest) throws ServletException, IOException, FloraOnException {
		ListIterator<String> partIt=thisRequest.getPathIteratorAfter("read");

		switch(partIt.next()) {
		case "getallcharacters":
			thisRequest.success(driver.getListDriver().getAllCharacters().toJsonObject());
			break;

		case "getallterritories":
			thisRequest.success(driver.getListDriver().getAllTerritoriesGraph(null).toJsonObject());
			break;
			
		case "taxoninfo":
			INodeKey key;
			String name;
			Integer oldid;
			errorIfAllNull(
				key = thisRequest.getParameterAsKey("key")
				, name = thisRequest.getParameterAsString("name")
				, oldid = thisRequest.getParameterAsInteger("oldid", null)
			);
			if(key != null)
				thisRequest.success(driver.getListDriver().getTaxonInfo(key));
			else if(name != null)
				thisRequest.success(driver.getListDriver().getTaxonInfo(name, thisRequest.getParameterAsBooleanNoNull("current")));
			else
				thisRequest.success(driver.getListDriver().getTaxonInfo(oldid));
			break;
			
		default:
			thisRequest.error("Path not found.");
		}
	}
}
