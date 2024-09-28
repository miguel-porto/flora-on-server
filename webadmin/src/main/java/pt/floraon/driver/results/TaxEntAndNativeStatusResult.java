package pt.floraon.driver.results;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.csv.CSVPrinter;

import pt.floraon.driver.Constants;
import pt.floraon.driver.TaxonomyException;
import pt.floraon.driver.utils.StringUtils;
import pt.floraon.taxonomy.entities.TaxEnt;
import pt.floraon.taxonomy.entities.Territory;

public class TaxEntAndNativeStatusResult extends SimpleTaxEntResult implements ResultItem {
	/**
	 * Is the native distribution complete for this taxon OR for any of its synonyms?
	 * This implies that all synonyms must have been traversed to check if any of them has this field set to true.
	 */
	protected Boolean worldDistributionCompleteness;
	/**
	 * The nearest TaxEnt which has worldDistributionCompleteness == COMPLETE 
	 */
	protected TaxEnt worldDistributionCompletenessTaxEnt;
	/**
	 * The list of status in all territories that are reachable from this taxon 
	 */
	protected List<TerritoryStatus> territories;
	/**
	 * The list of inferred native status for all checklist territories
	 */
	protected transient Map<String,InferredStatus> inferredStatus;
	/**
	 * The closest accepted taxon
	 */
	protected TaxEnt acceptedTaxon;

	protected String[] relationships;

	/**
	 * All the existing direct parents that are species or inferior
	 */
	protected TaxEnt[] parents;

	public List<TerritoryStatus> getTerritoryStatus() {
		return this.territories;
	}
	
	public Boolean getWorldDistributionCompleteness() {
		return this.worldDistributionCompleteness;
	}
	
	public Map<String,InferredStatus> inferNativeStatus() {
		return new ListOfTerritoryStatus(territories).computeTerritoryStatus(this.worldDistributionCompleteness);
	}

	public InferredStatus inferNativeStatus(String territory) {
		return new ListOfTerritoryStatus(territories).computeTerritoryStatus(territory, this.worldDistributionCompleteness);
	}

	public Set<String> inferEndemismDegree() {
		if(!this.worldDistributionCompleteness) return Collections.emptySet();
		return StringUtils.getNamesSet(new ListOfTerritoryStatus(territories).computeNativeExtent(this.worldDistributionCompletenessTaxEnt.getID()));
	}
	
	public Map<String, Set<Territory>> inferRestrictedTo(Set<String> territorySet) {
		if(territorySet == null) {
			territorySet = new HashSet<String>();
			for(String t : this.taxent.getTerritoriesWithCompleteDistribution())
				territorySet.add(t);
		}
		return new ListOfTerritoryStatus(territories).computeRestrictedTo(territorySet);
	}
	
	public void inferSingleTerritoryEndemismDegree() {
		new ListOfTerritoryStatus(territories).getSingleSmallestTerritory();
	}
	/**
	 * Export as an icon table
	 * TODO: this should be in the JSP page!
	 */
	@Override
	public String toHTMLTableRow(Object obj) {
		if(this.taxent == null) return null;
		
		Map<String,InferredStatus> tStatus = this.inferNativeStatus();
		InferredStatus status;
		@SuppressWarnings("unchecked")
		List<String> allTerritories=(List<String>) obj;
		StringBuilder sb=new StringBuilder();
		try {
			sb.append("<tr data-key=\"").append(this.taxent.getID()).append("\"")
				.append(this.taxent.getCurrent()==null ? "" : (this.taxent.getCurrent() ? "" : " class=\"notcurrent\""))
				.append("><td><a href=\"checklist?w=taxdetails&id="+URLEncoder.encode(this.taxent.getID(), StandardCharsets.UTF_8.name())+"\"><i>")
				.append(this.isLeaf ==null ? "" : (this.isLeaf ? "" : "+"))
				.append(this.taxent.getNameWithAnnotationOnly(true))
				.append("</i></a></td><td>")
				.append(this.taxent.getAuthor())
				.append("</td><td>");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		boolean rare;
		if(this.territories!=null) {
			for(String terr : allTerritories) {
				if(tStatus.containsKey(terr)) {
					status=tStatus.get(terr);
					rare = (status.getAbundanceLevel() != Constants.AbundanceLevel.NOT_SPECIFIED
							&& status.getAbundanceLevel() != Constants.AbundanceLevel.VERY_COMMON &&
							status.getAbundanceLevel() != Constants.AbundanceLevel.COMMON);
					sb.append("<div class=\"territory ").append(status.endemic ? "ENDEMIC" : status.nativeStatus.toString()).append("\">");
					if(status.occurrenceStatus!=null)
						sb.append("<div class=\"occurrencestatus ").append(status.occurrenceStatus.toString() + (rare ? " RARE" : "")).append("\">").append("</div>");
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
	public String getHTMLTableHeader(Object obj) {
		return "<tr><th>Canonical name</th><th>Author</th><th>Status in territories</th></tr>";
	}

	@Override
	public void toCSVLine(CSVPrinter rec, Object obj) throws IOException {
		if(this.taxent == null) {
			rec.print("");
			return;
		}
		Map<String,InferredStatus> tStatus = this.inferNativeStatus();
		@SuppressWarnings("unchecked")
		List<String> allTerritories=(List<String>) obj;
		rec.print(this.taxent.getCurrent() ? "yes" : "no");
		rec.print(this.taxent.getID());
		rec.print(this.taxent.getOldId());
		rec.print((this.isLeaf == null ? "" : (this.isLeaf ? "" : "+")) + this.taxent.getNameWithAnnotationOnly(false));
		rec.print(this.taxent.getFullName());
		rec.print(this.taxent.getAuthor());
		rec.print(this.acceptedTaxon == null ? "" : this.acceptedTaxon.getFullName());
		rec.print(StringUtils.implode(",", this.relationships));
		// iterate through the parents and extract canonical names
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < this.parents.length; i++) {
			try {
				if(this.parents[i] != null && !this.parents[i].getTaxonName().getCanonicalName().matches(" *")) {
					sb.append(this.parents[i].getTaxonName().getCanonicalName(true));
					if(i < this.parents.length-1 && this.parents[i+1] != null && !this.parents[i+1].getTaxonName().getCanonicalName().matches(" *")) sb.append("|");
				}
			} catch (TaxonomyException e) {
				e.printStackTrace();
			}
		}
		rec.print(sb.toString());
		rec.print(StringUtils.implode(",", this.inferEndemismDegree().toArray(new String[0])));
		if(this.territories==null) return;

		for(String t : allTerritories) {
			if(tStatus.containsKey(t))
				rec.print(tStatus.get(t).getStatusSummary());
			else
				rec.print("");
		}
		rec.print(this.taxent.getComment());
	}

	@Override
	public void getCSVHeader(CSVPrinter rec, Object obj) throws IOException {
		@SuppressWarnings("unchecked")
		List<String> territories=(List<String>) obj;
		rec.print("accepted");
		rec.print("id");
		rec.print("oldId");
		rec.print("canonicalName");
		rec.print("fullName");
		rec.print("authority");
		rec.print("acceptedTaxon");
		rec.print("relationships");
		rec.print("parents");
		rec.print("endemicTo");
		for(String t : territories) {
			rec.print(t);
		}
		rec.print("comment");
		rec.println();
	}

	/*@Override
	public JsonObject toJson() {
		return null;
		JsonObject out = new JsonObject();
		if(this.taxent == null) return out;
		Gson gson = new Gson();
		Map<String,InferredStatus> tStatus = this.inferNativeStatus(null);
		out.add("taxon", gson.toJsonTree(this.taxent));
		out.add("endemismDegree", gson.toJsonTree(this.inferEndemismDegree()));
		out.add("restrictedTo", gson.toJsonTree(this.inferRestrictedTo(null)));
		JsonObject tst = new JsonObject();
		if(this.territories!=null) {
			for(Entry<String, InferredStatus> st : tStatus.entrySet()) {
				tst.add(st.getKey(), gson.toJsonTree(st.getValue()));
			}
		}
		out.add("territories", tst);
		return out;
	}*/

}
