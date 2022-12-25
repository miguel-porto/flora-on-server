package pt.floraon.taxonomy.servlets;

import java.io.IOException;
import java.util.ListIterator;
import java.util.concurrent.atomic.AtomicReference;

import javax.servlet.ServletException;

import com.google.gson.Gson;
import pt.floraon.driver.Constants;
import pt.floraon.driver.FloraOnException;
import pt.floraon.driver.interfaces.INodeKey;
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
		INodeKey key;
		Integer oldid;
		ListIterator<String> partIt=thisRequest.getPathIteratorAfter("read");

		switch(partIt.next()) {
		case "getallhabitats":
			thisRequest.success(driver.getListDriver().getGraphWholeCollection(Constants.NodeTypes.habitat
					, new Constants.Facets[] {Constants.Facets.TAXONOMY}).toJsonObject());
			break;

		case "getallcharacters":
			thisRequest.success(driver.getListDriver().getAllCharacters().toJsonObject());
			break;

		case "getallterritories":
			thisRequest.success(driver.getListDriver().getAllTerritoriesGraph(null).toJsonObject());
			break;

		case "getorphan":
			thisRequest.success(driver.getListDriver().getAllOrphanTaxaAsGUR().toJsonObject());
			break;
			
		case "taxoninfo":
			String name;
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

		case "taxonredlist":
			errorIfAllNull(
					key = thisRequest.getParameterAsKey("key")
					, oldid = thisRequest.getParameterAsInteger("oldid", null)
			);
			if(key != null) {
				thisRequest.success(new Gson().toJsonTree(
						driver.getRedListData().getRedListDataEntity(driver.getDefaultRedListTerritory(), key)
				));
			} else if(oldid != null) {
				thisRequest.success(new Gson().toJsonTree(
						driver.getRedListData().getRedListDataEntity(driver.getDefaultRedListTerritory(), driver.asNodeKey(driver.getNodeWorkerDriver().getTaxEntByOldId(oldid).getID()))
				));
			}
			break;

		default:
			thisRequest.error("Path not found.");
		}
	}
}
