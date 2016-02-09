package pt.floraon.server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;

import pt.floraon.driver.Constants;
import pt.floraon.driver.FloraOnException;
import pt.floraon.driver.INodeKey;
import pt.floraon.entities.TaxEnt;
import pt.floraon.results.NativeStatusResult;
import pt.floraon.results.ResultProcessor;

public class TaxDetails extends FloraOnServlet {

	private static final long serialVersionUID = 1L;

	@Override
	public void doFloraOnGet() throws ServletException, IOException, FloraOnException {
		INodeKey id=getParameterAsKey("id");
		TaxEnt taxent= driver.getNodeWorkerDriver().getTaxEntById(id);
		ResultProcessor<NativeStatusResult> rpnsr=new ResultProcessor<NativeStatusResult>(driver.getNodeWorkerDriver().getTaxonNativeStatus(id));
		
		ByteArrayOutputStream baos=new ByteArrayOutputStream();
		rpnsr.toHTMLTable(new PrintWriter(baos), null);
		baos.close();

		request.setAttribute("taxent", taxent);
		request.setAttribute("taxentWrapper", driver.wrapTaxEnt(driver.asNodeKey(taxent.getID())));
		request.setAttribute("nativeStatusTable", baos.toString());
		request.setAttribute("TaxonRanks", Constants.TaxonRanks.values());
		request.setAttribute("territories", driver.getListDriver().getAllTerritories(null));
		
		response.setContentType("text/html");
		request.getRequestDispatcher("/taxdetails.jsp").include(request, response);
	}

}
