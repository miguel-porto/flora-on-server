package pt.floraon.taxonomy;

import java.io.IOException;

import javax.servlet.ServletException;

import pt.floraon.driver.Constants;
import pt.floraon.driver.FloraOnException;
import pt.floraon.driver.INodeKey;
import pt.floraon.taxonomy.entities.TaxEnt;
import pt.floraon.server.FloraOnServlet;

public class TaxDetails extends FloraOnServlet {

	private static final long serialVersionUID = 1L;

	@Override
	public void doFloraOnGet() throws ServletException, IOException, FloraOnException {
		INodeKey id=getParameterAsKey("id");
		TaxEnt taxent = driver.getNodeWorkerDriver().getTaxEntById(id);
		
		request.setAttribute("taxent", taxent);
		//request.setAttribute("taxentWrapper", driver.wrapTaxEnt(driver.asNodeKey(taxent.getID())));
		request.setAttribute("taxentWrapper", driver.wrapTaxEnt(id));
		request.setAttribute("assignedNativeStatus", driver.getNodeWorkerDriver().getAssignedNativeStatus(id));
		/*if(taxent.getRank().getValue() > Constants.TaxonRanks.FAMILY.getValue())
			request.setAttribute("inferredNativeStatus", driver.wrapTaxEnt(id).getInferredNativeStatus(null).entrySet());*/
		request.setAttribute("inferredNativeStatus", driver.wrapTaxEnt(id).getInferredNativeStatus().entrySet());
		request.setAttribute("restrictedTo", driver.wrapTaxEnt(id).getRestrictedTo(Constants.getIDsList(driver.getListDriver().getChecklistTerritories())));
		request.setAttribute("TaxonRanks", Constants.TaxonRanks.values());
		request.setAttribute("territories", driver.getListDriver().getAllTerritories(null));
		request.setAttribute("occurrenceStatus", Constants.OccurrenceStatus.values());
		request.setAttribute("abundanceLevel", Constants.AbundanceLevel.values());
		request.setAttribute("nativeStatus", Constants.NativeStatus.values());
		request.setAttribute("introducedStatus", Constants.PlantIntroducedStatus.values());
		request.setAttribute("naturalizationDegree", Constants.PlantNaturalizationDegree.values());
		
		response.setContentType("text/html");
		request.getRequestDispatcher("/taxdetails.jsp").include(request, response);
	}

}
