package pt.floraon.server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.arangodb.ArangoException;

import pt.floraon.driver.ArangoKey;
import pt.floraon.driver.Constants;
import pt.floraon.driver.FloraOnException;
import pt.floraon.entities.TaxEnt;
import pt.floraon.results.NativeStatusResult;
import pt.floraon.results.ResultProcessor;

public class TaxDetails extends FloraOnServlet {

	private static final long serialVersionUID = 1L;

	@Override
	public void doFloraOnGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, ArangoException, FloraOnException {
		String id=request.getParameter("id");
		TaxEnt tev=graph.dbNodeWorker.getTaxEnt(ArangoKey.fromString(id));
		ResultProcessor<NativeStatusResult> rpnsr=new ResultProcessor<NativeStatusResult>(graph.dbSpecificQueries.getTaxonNativeStatus(tev.getArangoKey()).iterator());
		
		ByteArrayOutputStream baos=new ByteArrayOutputStream();
		rpnsr.toHTMLTable(new PrintWriter(baos), null);
		baos.close();

		request.setAttribute("taxent", tev);
		request.setAttribute("nativeStatusTable", baos.toString());
		request.setAttribute("TaxonRanks", Constants.TaxonRanks.values());
		request.setAttribute("territories", graph.dbGeneralQueries.getAllTerritories(null).iterator());
		
		response.setContentType("text/html");
		request.getRequestDispatcher("/taxdetails.jsp").include(request, response);
	}

}
