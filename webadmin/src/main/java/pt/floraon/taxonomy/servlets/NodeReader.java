package pt.floraon.taxonomy.servlets;

import java.io.IOException;
import java.util.ListIterator;
import java.util.concurrent.atomic.AtomicReference;

import javax.servlet.ServletException;

import com.google.gson.Gson;
import pt.floraon.driver.Constants;
import pt.floraon.driver.FloraOnException;
import pt.floraon.driver.interfaces.INodeKey;
import pt.floraon.redlistdata.RedListEnums;
import pt.floraon.redlistdata.entities.RedListDataEntity;
import pt.floraon.server.FloraOnServlet;
import pt.floraon.taxonomy.entities.TaxEnt;

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
			TaxEnt te1;
			INodeKey key1 = null;
			if(key != null) {
				key1 = key;
			} else if(oldid != null) {
				te1 = driver.getNodeWorkerDriver().getTaxEntByOldId(oldid);
				if(te1 != null)
					key1 = driver.asNodeKey(te1.getID());
			}
			RedListDataEntity rlde = null;
			if(key1 != null)
				rlde = driver.getRedListData().getRedListDataEntity(driver.getDefaultRedListTerritory(), key1);
			if(key1 != null && (rlde == null || rlde.getAssessment().getReviewStatus() != RedListEnums.ReviewStatus.REVISED_PUBLISHING)) {	// this taxon has no assessment sheet
				// search for assessments in the synonyms
				for (TaxEnt te : driver.wrapTaxEnt(key1).getSynonyms()) {
					rlde = driver.getRedListData().getRedListDataEntity(driver.getDefaultRedListTerritory(), driver.asNodeKey(te.getID()));
					if (rlde != null && rlde.getAssessment().getReviewStatus() == RedListEnums.ReviewStatus.REVISED_PUBLISHING) {
						thisRequest.success(new Gson().toJsonTree(rlde));
						return;
					}
				}
				// not found, search parent taxon
				TaxEnt te = driver.wrapTaxEnt(key1).getParentTaxon();
				rlde = driver.getRedListData().getRedListDataEntity(driver.getDefaultRedListTerritory(), driver.asNodeKey(te.getID()));
				if (rlde != null && rlde.getAssessment().getReviewStatus() == RedListEnums.ReviewStatus.REVISED_PUBLISHING) {
					thisRequest.success(new Gson().toJsonTree(rlde));
					return;
				}
				thisRequest.error("No assessment found.");
			} else thisRequest.success(new Gson().toJsonTree(rlde));
			break;

		default:
			thisRequest.error("Path not found.");
		}
	}
}
