package pt.floraon.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.arangodb.ArangoException;
import com.arangodb.CursorResult;
import com.google.gson.JsonObject;

import pt.floraon.driver.ArangoKey;
import pt.floraon.driver.FloraOnException;
import pt.floraon.entities.TaxEntVertex;
import pt.floraon.entities.TerritoryVertex;
import pt.floraon.jobs.ChecklistDownload;
import pt.floraon.jobs.JobSubmitter;
import pt.floraon.jobs.JobRunner;
import pt.floraon.results.ChecklistEntry;
import pt.floraon.results.NamesAndTerritoriesResult;
import pt.floraon.results.ResultProcessor;
import pt.floraon.results.SimpleNameResult;
import static pt.floraon.driver.Constants.*;
public class Lists extends FloraOnServlet {
	private static final long serialVersionUID = 1L;
	
	@Override
	public void doFloraOnGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, ArangoException, FloraOnException {
		String htmlClass=null,from;
		Object opt=null;
		PrintWriter out;
		String what=request.getParameter("w");
		
		if(what==null) {
			error("Choose one of: checklist, species, speciesterritories, tree");
			return;
		}
		String format=request.getParameter("fmt");
		if(format==null || format.trim().equals("")) format="htmltable";
		ResultProcessor<?> rpchk=null;
		switch(what) {
		case "checklist":
			if(format.equals("csv")) {
				JobRunner job=JobSubmitter.newJob(new ChecklistDownload(), "checklist.csv", this.graph);
				success(job.getID());
				return;
			} else {
				List<ChecklistEntry> chklst=graph.getCheckList();
				Collections.sort(chklst);
				rpchk=(ResultProcessor<ChecklistEntry>) new ResultProcessor<ChecklistEntry>(chklst.iterator());
			}
			break;
		
		case "species":
			Iterator<SimpleNameResult> species;
			species = graph.dbSpecificQueries.getAllSpeciesOrInferior(territory==null ? true : false, SimpleNameResult.class, territory, null, null);
			rpchk=(ResultProcessor<SimpleNameResult>) new ResultProcessor<SimpleNameResult>(species);
			break;

		case "speciesterritories":
			from=request.getParameter("offset");
			Iterator<NamesAndTerritoriesResult> speciesterr;
			speciesterr = graph.dbSpecificQueries.getAllSpeciesOrInferior(territory==null ? true : false, NamesAndTerritoriesResult.class, territory, from==null ? null : Integer.parseInt(from), PAGESIZE);
			rpchk=(ResultProcessor<NamesAndTerritoriesResult>) new ResultProcessor<NamesAndTerritoriesResult>(speciesterr);
			List<String> opt1=new ArrayList<String>();
			for(TerritoryVertex tv : graph.checklistTerritories)
				opt1.add(tv.getShortName());
			opt=opt1;
			break;
			
		case "tree":
			// either specify ID or RANK, not both
			String id=request.getParameter("id");		// the id of the taxent node of which to get the children
			String rank=request.getParameter("rank");	// the rank of which to get all nodes
			if(id!=null & rank!=null) {
				error("You must specify id OR rank, not both.");
				return;
			}
			if(id!=null) {
				CursorResult<TaxEntVertex> tmp=graph.dbSpecificQueries.getChildren(ArangoKey.fromString(id));
				rpchk=(ResultProcessor<TaxEntVertex>) new ResultProcessor<TaxEntVertex>(tmp.iterator());
			}

			if(rank!=null) {
				htmlClass=rank.toUpperCase();
				Iterator<TaxEntVertex> res1=graph.dbSpecificQueries.getAllOfRank(TaxonRanks.valueOf(rank.toUpperCase()));
				rpchk=(ResultProcessor<TaxEntVertex>) new ResultProcessor<TaxEntVertex>(res1);
			}
			break;
		}
		
		if(rpchk!=null) {
			JsonObject header;
			switch(format) {
			case "json":
				header=new JsonObject();
				//header.addProperty("nresults", chklst.size());
				success(rpchk.toJSONElement(),header);
				return;
			case "htmllist":
				response.setContentType("text/html");
				out=response.getWriter();
				out.println(rpchk.toHTMLList(htmlClass));
				break;
			case "html":
			case "htmltable":
				response.setContentType("text/html");
				out=response.getWriter();
				rpchk.toHTMLTable(out, opt);
				break;
			case "csv":
				response.setContentType("text/csv");
				response.setCharacterEncoding("Windows-1252");
				out=response.getWriter();
				out.println(rpchk.toCSVTable(null));
				break;
			}
		} else {
			error("Some error occurred.");
			return;
		}
	}
}
