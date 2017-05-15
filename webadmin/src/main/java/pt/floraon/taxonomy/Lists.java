package pt.floraon.taxonomy;

import static pt.floraon.driver.Constants.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import pt.floraon.driver.FloraOnException;
import pt.floraon.driver.INodeKey;
import pt.floraon.taxonomy.entities.TaxEnt;
import pt.floraon.taxonomy.entities.Territory;
import pt.floraon.driver.jobs.ChecklistDownloadJob;
import pt.floraon.driver.jobs.JobSubmitter;
import pt.floraon.driver.jobs.JobRunnerFileDownload;
import pt.floraon.driver.results.TaxEntAndNativeStatusResult;
import pt.floraon.driver.results.ResultProcessor;
import pt.floraon.server.FloraOnServlet;

public class Lists extends FloraOnServlet {
	private static final long serialVersionUID = 1L;
	
	@Override
	public void doFloraOnGet(ThisRequest thisRequest) throws ServletException, IOException, FloraOnException {
		String htmlClass=null;
		Object opt=null;
		PrintWriter out;
		String what=thisRequest.getParameterAsString("w");
		
		if(what==null) {
			thisRequest.error("Choose one of: checklist, species, speciesterritories, tree, occurrences");
			return;
		}
		String format=thisRequest.getParameterAsString("fmt");
		if(format==null || format.trim().equals("")) format="htmltable";
		ResultProcessor<?> rpchk=null;
		switch(what) {
		case "species":
			thisRequest.success(new Gson().toJsonTree(LD.getAllSpeciesOrInferiorTaxEnt(true, false, thisRequest.getParameterAsString("territory"), null, null)));
			return;

		case "checklist":
			if(format.equals("csv")) {
				JobRunnerFileDownload job=JobSubmitter.newJobFileDownload(new ChecklistDownloadJob(), "checklist.csv", driver);
				thisRequest.success(job.getID());
			} else {
				thisRequest.error("Not implemented");
//				List<ChecklistEntry> chklst=LD.getCheckList();
//				Collections.sort(chklst);
//				rpchk = new ResultProcessor<ChecklistEntry>(chklst.iterator());
			}
			return;

// get the full or partial list of names and statuses in each territory
		case "speciesterritories":
			Integer offset=thisRequest.getParameterAsInteger("offset",null);
			String filter=thisRequest.getParameterAsString("filter");
			Iterator<TaxEntAndNativeStatusResult> speciesterr;
			speciesterr = LD.getAllSpeciesOrInferior(territory == null, TaxEntAndNativeStatusResult.class, false, territory, filter, offset, PAGESIZE).iterator();
			rpchk = new ResultProcessor<TaxEntAndNativeStatusResult>(speciesterr);
			List<String> opt1=new ArrayList<String>();
			for(Territory tv : driver.getChecklistTerritories())
				opt1.add(tv.getShortName());
			opt=opt1;
			break;
			
		case "tree":
			// either specify ID or RANK, not both
			INodeKey id=thisRequest.getParameterAsKey("id");		// the id of the taxent node of which to get the children
			String rank=thisRequest.getParameterAsString("rank");	// the rank of which to get all nodes
			if(id!=null && rank!=null) {
				thisRequest.error("You must specify id OR rank, not both.");
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
				thisRequest.success(rpchk.toJSONElement(),header);
				return;
			case "htmllist":
				thisRequest.response.setContentType("text/html");
				out = thisRequest.response.getWriter();
				out.println(rpchk.toHTMLList(htmlClass));
				out.flush();
				break;
			case "html":
			case "htmltable":
				thisRequest.error("This is deprecated");
				return;
/*
				response.setContentType("text/html");
				out=response.getWriter();
				rpchk.toHTMLTable(out, "taxonlist", opt);
				break;
*/
			case "csv":
				thisRequest.response.setContentType("text/csv");
				thisRequest.response.setCharacterEncoding("Windows-1252");
				out=thisRequest.response.getWriter();
				out.println(rpchk.toCSVTable(null));
				out.flush();
				break;
			}
		} else {
			thisRequest.error("Some error occurred.");
			return;
		}
	}
}
