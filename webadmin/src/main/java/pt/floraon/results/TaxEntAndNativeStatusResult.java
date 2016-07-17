package pt.floraon.results;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.csv.CSVPrinter;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import pt.floraon.driver.Constants.OccurrenceStatus;
import pt.floraon.driver.Constants.WorldDistributionCompleteness;
import pt.floraon.results.ListOfTerritoryStatus.Status;

/**
 * A TaxEnt result with endemismDegree and NativeStatus in all territories marked for checklist.
 * @author miguel
 *
 */
public class TaxEntAndNativeStatusResult extends SimpleTaxEntResult implements ResultItem {
	protected List<TerritoryStatus> territories;
	protected List<String> endemismDegree;
	
	@Override
	public String toHTMLTableRow(Object obj) {
		if(this.taxent == null) return null;
		Map<String,Status> tStatus = new ListOfTerritoryStatus(territories).computeTerritoryStatus(this.taxent.getWorldDistributionCompleteness()!=null && this.taxent.getWorldDistributionCompleteness()==WorldDistributionCompleteness.DISTRIBUTION_COMPLETE);
		Status status;
		@SuppressWarnings("unchecked")
		List<String> allTerritories=(List<String>) obj;
		StringBuilder sb=new StringBuilder();
		try {
			sb.append("<tr data-key=\"").append(this.taxent.getID()).append("\"")
				.append(this.taxent.getCurrent()==null ? "" : (this.taxent.getCurrent() ? "" : " class=\"notcurrent\""))
				.append("><td><a href=\"/floraon/admin?w=taxdetails&id="+URLEncoder.encode(this.taxent.getID(), StandardCharsets.UTF_8.name())+"\"><i>")
				.append(this.leaf==null ? "" : (this.leaf ? "" : "+"))
				.append(this.taxent.getNameWithAnnotationOnly())
				.append("</i></a></td><td>")
				.append(this.taxent.getAuthor())
				.append("</td><td>");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(this.territories!=null) {
			for(String terr : allTerritories) {
				if(tStatus.containsKey(terr)) {
					status=tStatus.get(terr);
					sb.append("<div class=\"territory ").append(status.endemic ? "ENDEMIC" : status.nativeStatus.toString()).append("\">");
					if(status.occurrenceStatus!=null)
						sb.append("<div class=\"occurrencestatus ").append(status.occurrenceStatus.toString()).append("\">").append("</div>");
					if(status.possibly)
						sb.append("<div class=\"occurrencestatus uncertain\"></div>");
					if(status.uncertainOccurrence!=null && status.uncertainOccurrence)
						sb.append("<div class=\"occurrencestatus UNCERTAIN_OCCURRENCE\"></div>");
					sb.append("<div class=\"legend\">").append(terr).append("</div></div>");
				} else
					sb.append("<div class=\"territory\"><div class=\"legend\">").append(terr).append("</div></div>");
			}
		}
		sb.append("</td></tr>");
		return sb.toString();
	}
	
	@Override
	public void toCSVLine(CSVPrinter rec, Object obj) throws IOException {
		if(this.taxent == null) {
			rec.print("");
			return;
		}
		Map<String,Status> tStatus = new ListOfTerritoryStatus(territories).computeTerritoryStatus(this.taxent.getWorldDistributionCompleteness()!=null && this.taxent.getWorldDistributionCompleteness()==WorldDistributionCompleteness.DISTRIBUTION_COMPLETE);
		Status tmp;
		@SuppressWarnings("unchecked")
		List<String> allTerritories=(List<String>) obj;
		rec.print(this.taxent.getID());
		rec.print((this.leaf==null ? "" : (this.leaf ? "" : "+"))+this.taxent.getNameWithAnnotationOnly());
		rec.print(this.taxent.getAuthor());
		if(this.territories==null) return;

		for(String t : allTerritories) {
			if(tStatus.containsKey(t)) {
				tmp=tStatus.get(t);
				if(tmp.occurrenceStatus!=null && tmp.occurrenceStatus!=OccurrenceStatus.PRESENT)
					rec.print((tmp.possibly ? "?" : "")+tmp.nativeStatus.toString()+" ("+tmp.occurrenceStatus.toString()+")");
				else rec.print((tmp.possibly ? "?" : "")+tmp.nativeStatus.toString());
			} else
				rec.print("");
		}
	}

	@Override
	public void getCSVHeader(CSVPrinter rec, Object obj) throws IOException {
		@SuppressWarnings("unchecked")
		List<String> territories=(List<String>) obj;
		rec.print("id");
		rec.print("canonicalName");
		rec.print("authority");
		for(String t : territories) {
			rec.print(t);
		}
		rec.println();
	}

	@Override
	public JsonObject toJson() {
		JsonObject out = new JsonObject();
		if(this.taxent == null) return out;
		Gson gson = new Gson();
		Map<String,Status> tStatus = new ListOfTerritoryStatus(territories).computeTerritoryStatus(this.taxent.getWorldDistributionCompleteness()!=null && this.taxent.getWorldDistributionCompleteness()==WorldDistributionCompleteness.DISTRIBUTION_COMPLETE);
		out.add("taxon", gson.toJsonTree(this.taxent));
		out.add("endemismDegree", gson.toJsonTree(this.endemismDegree));
		JsonObject tst = new JsonObject();
		if(this.territories!=null) {
			for(Entry<String, Status> st : tStatus.entrySet()) {
				tst.add(st.getKey(), gson.toJsonTree(st.getValue()));
			}
		}
		out.add("territories", tst);
		return out;
	}
	
	public Map<String,Status> getInferredNativeStatus() {
		return new ListOfTerritoryStatus(territories).computeTerritoryStatus(this.taxent.getWorldDistributionCompleteness()!=null && this.taxent.getWorldDistributionCompleteness()==WorldDistributionCompleteness.DISTRIBUTION_COMPLETE);
	}
}
