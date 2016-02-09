package pt.floraon.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;

import com.google.gson.JsonObject;

import pt.floraon.driver.FloraOnException;
import pt.floraon.queryparser.YlemParser;
import pt.floraon.results.ResultProcessor;
import pt.floraon.results.SimpleTaxonResult;

public class Query extends FloraOnServlet {
	private static final long serialVersionUID = 1L;

	@Override
	public void doFloraOnGet() throws ServletException, IOException, FloraOnException {
		String query=request.getParameter("q");
		String format=request.getParameter("fmt");
		if(errorIfAnyNull(query)) return;
		if(format==null) format="json";
		Iterator<SimpleTaxonResult> it;
		ResultProcessor<SimpleTaxonResult> rp;
		
		YlemParser ylem=new YlemParser(driver,query);
		long start = System.nanoTime();
		List<SimpleTaxonResult> res=ylem.execute();
		long elapsedTime = System.nanoTime() - start;
		
		if(res==null) res=new ArrayList<SimpleTaxonResult>();
			
		JsonObject header=new JsonObject();
		header.addProperty("time", (double)elapsedTime/1000000000);
		header.addProperty("nresults", res.size());
		it=res.iterator();
		rp=new ResultProcessor<SimpleTaxonResult>(it);
		switch(format) {
		case "html":
			response.setContentType("text/html");
			rp.toHTMLTable(response.getWriter(),null);
			break;
			
		case "json":
		default:
			success(rp.toJSONElement(),header);
			return;
		}
	}
}
