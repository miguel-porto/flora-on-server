package pt.floraon.server;

import static pt.floraon.driver.Constants.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;

import com.google.gson.JsonObject;

import pt.floraon.driver.FloraOnException;
import pt.floraon.driver.INodeKey;
import pt.floraon.entities.TaxEnt;
import pt.floraon.entities.Territory;
import pt.floraon.jobs.ChecklistDownload;
import pt.floraon.jobs.JobSubmitter;
import pt.floraon.jobs.JobRunner;
import pt.floraon.results.ChecklistEntry;
import pt.floraon.results.NamesAndTerritoriesResult;
import pt.floraon.results.ResultProcessor;
import pt.floraon.results.SimpleNameResult;
public class Lists extends FloraOnServlet {
	private static final long serialVersionUID = 1L;
	
	@Override
	public void doFloraOnGet() throws ServletException, IOException, FloraOnException {
		String htmlClass=null;
		Object opt=null;
		PrintWriter out;
		String what=request.getParameter("w");
		
		if(what==null) {
			error("Choose one of: checklist, species, speciesterritories, tree, occurrences");
			return;
		}
		String format=request.getParameter("fmt");
		if(format==null || format.trim().equals("")) format="htmltable";
		ResultProcessor<?> rpchk=null;
		switch(what) {
		case "checklist":
			if(format.equals("csv")) {
				JobRunner job=JobSubmitter.newJob(new ChecklistDownload(), "checklist.csv", driver);
				success(job.getID());
				return;
			} else {
				List<ChecklistEntry> chklst=LD.getCheckList();
				Collections.sort(chklst);
				rpchk=(ResultProcessor<ChecklistEntry>) new ResultProcessor<ChecklistEntry>(chklst.iterator());
			}
			break;
		
		case "species":
			Iterator<SimpleNameResult> species;
			species = LD.getAllSpeciesOrInferior(territory==null ? true : false, SimpleNameResult.class, false, territory, null, null, null);
			rpchk=(ResultProcessor<SimpleNameResult>) new ResultProcessor<SimpleNameResult>(species);
			break;
// get the full or partial list of names and statuses in each territory
		case "speciesterritories":
			Integer offset=getParameterAsInteger("offset",null);
			String filter=getParameterAsString("filter");
			Iterator<NamesAndTerritoriesResult> speciesterr;
			speciesterr = LD.getAllSpeciesOrInferior(territory==null ? true : false, NamesAndTerritoriesResult.class, false, territory, filter, offset, PAGESIZE);
			rpchk=(ResultProcessor<NamesAndTerritoriesResult>) new ResultProcessor<NamesAndTerritoriesResult>(speciesterr);
			List<String> opt1=new ArrayList<String>();
			for(Territory tv : driver.getChecklistTerritories())
				opt1.add(tv.getShortName());
			opt=opt1;
			break;
			
		case "tree":
			// either specify ID or RANK, not both
			INodeKey id=getParameterAsKey("id");		// the id of the taxent node of which to get the children
			String rank=getParameterAsString("rank");	// the rank of which to get all nodes
			if(id!=null & rank!=null) {
				error("You must specify id OR rank, not both.");
				return;
			}
			if(id!=null) {
				Iterator<TaxEnt> tmp=driver.wrapTaxEnt(id).getChildren();
				rpchk=(ResultProcessor<TaxEnt>) new ResultProcessor<TaxEnt>(tmp);
			}

			if(rank!=null) {
				htmlClass=rank.toUpperCase();
				Iterator<TaxEnt> res1=LD.getAllOfRank(TaxonRanks.valueOf(rank.toUpperCase()));
				rpchk=(ResultProcessor<TaxEnt>) new ResultProcessor<TaxEnt>(res1);
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
				rpchk.toHTMLTable(out, "taxonlist", opt);
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
