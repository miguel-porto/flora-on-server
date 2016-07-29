package pt.floraon.results;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.csv.CSVPrinter;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import pt.floraon.driver.Constants;
import pt.floraon.driver.Constants.WorldNativeDistributionCompleteness;
import pt.floraon.results.ListOfTerritoryStatus.InferredStatus;

public class TaxEntAndNativeStatusResult extends SimpleTaxEntResult implements ResultItem {
	protected List<TerritoryStatus> territories;

	public Map<String,InferredStatus> inferNativeStatus(String territory) {
		if(territory == null)
			return new ListOfTerritoryStatus(territories).computeTerritoryStatus(this.taxent.getWorldDistributionCompleteness()!=null && this.taxent.getWorldDistributionCompleteness()==WorldNativeDistributionCompleteness.DISTRIBUTION_COMPLETE);
		else
			return new ListOfTerritoryStatus(territories).computeTerritoryStatus(territory, this.taxent.getWorldDistributionCompleteness()!=null && this.taxent.getWorldDistributionCompleteness()==WorldNativeDistributionCompleteness.DISTRIBUTION_COMPLETE);
	}
	
	public Set<String> inferEndemismDegree() {
		if(this.taxent.getWorldDistributionCompleteness() != Constants.WorldNativeDistributionCompleteness.DISTRIBUTION_COMPLETE) return Collections.emptySet();
		return new ListOfTerritoryStatus(territories).computeEndemismDegreeName();
	}
	
	/**
	 * Export as an icon table
	 */
	@Override
	public String toHTMLTableRow(Object obj) {
		if(this.taxent == null) return null;
		
		Map<String,InferredStatus> tStatus = this.inferNativeStatus(null);
		InferredStatus status;
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
		Map<String,InferredStatus> tStatus = this.inferNativeStatus(null);
		@SuppressWarnings("unchecked")
		List<String> allTerritories=(List<String>) obj;
		rec.print(this.taxent.getID());
		rec.print((this.leaf==null ? "" : (this.leaf ? "" : "+"))+this.taxent.getNameWithAnnotationOnly());
		rec.print(this.taxent.getAuthor());
		if(this.territories==null) return;

		for(String t : allTerritories) {
			if(tStatus.containsKey(t))
				rec.print(tStatus.get(t).getVerbatimNativeStatus());
			else
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
		Map<String,InferredStatus> tStatus = this.inferNativeStatus(null);
		out.add("taxon", gson.toJsonTree(this.taxent));
		out.add("endemismDegree", gson.toJsonTree(this.inferEndemismDegree()));
		JsonObject tst = new JsonObject();
		if(this.territories!=null) {
			for(Entry<String, InferredStatus> st : tStatus.entrySet()) {
				tst.add(st.getKey(), gson.toJsonTree(st.getValue()));
			}
		}
		out.add("territories", tst);
		return out;
	}

}
