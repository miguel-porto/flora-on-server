package pt.floraon.taxonomy.servlets;

import static pt.floraon.driver.Constants.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import pt.floraon.driver.FloraOnException;
import pt.floraon.driver.interfaces.INodeKey;
import pt.floraon.ecology.entities.Habitat;
import pt.floraon.redlistdata.entities.RedListDataEntity;
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
		String territory = null;
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
			INodeKey id;
			try {
				id = thisRequest.getParameterAsKey("id");        // the id of the taxent node of which to get the children
			} catch (FloraOnException e) {
				id = null;
			}
			String rank = thisRequest.getParameterAsString("rank");	// the rank of which to get all nodes
			String type = thisRequest.getParameterAsString("type");	// taxent or habitat
			if(id != null && rank != null) {
				thisRequest.error("You must specify id OR rank, not both.");
				return;
			}
			if(type == null && id != null)
				type = id.getCollection();

			if(type == null) {
				thisRequest.error("You must specify id or type.");
				return;
			}

			switch(type) {
				case "taxent":
					Iterator<TaxEnt> ite = null;
					if(id != null)
						ite = LD.getChildrenTaxEnt(id);

					if(rank != null)
						ite = LD.getAllOfRank(TaxonRanks.valueOf(rank.toUpperCase()));

					thisRequest.request.setAttribute("taxents", ite);
					thisRequest.request.getRequestDispatcher("/fragments/frag-taxentli.jsp").include(thisRequest.request,
							thisRequest.response);
					return;

				case "habitat":
					Iterator<Habitat> ith;
					Integer level = thisRequest.getParameterAsInteger("level", null);
					thisRequest.request.setAttribute("maxlevel", thisRequest.getParameterAsInteger("maxlevel", null));
					thisRequest.request.setAttribute("hideafterlevel", thisRequest.getParameterAsInteger("hideafterlevel", null));
					INodeKey taxEntId = thisRequest.getParameterAsKey("taxent");
					if(thisRequest.request.getAttribute("territory") == null || thisRequest.request.getAttribute("habitatTypesIds") == null) {
						territory = thisRequest.getParameterAsString("territory");
						RedListDataEntity rlde = driver.getRedListData().getRedListDataEntity(territory, taxEntId);
						if(rlde == null) return;
						thisRequest.request.setAttribute("habitatTypesIds", Arrays.asList(rlde.getEcology().getHabitatTypes()));
					}


//					System.out.printf("Level %d; ID: %s\n", level == null ? 0 : level, id == null ? "NULL" : id);

					if(id == null) {
						if(level == null)
							ith = LD.getAllDocumentsOfCollection(NodeTypes.habitat.toString(), Habitat.class);
						else
							ith = LD.getHabitatsOfLevel(level);
					} else
						ith = LD.getChildrenHabitats(id);

					thisRequest.request.setAttribute("habitats", ith);
					thisRequest.request.getRequestDispatcher("/fragments/frag-habitatli.jsp").include(thisRequest.request,
							thisRequest.response);
					return;
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
