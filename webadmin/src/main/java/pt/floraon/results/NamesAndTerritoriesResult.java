package pt.floraon.results;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.csv.CSVPrinter;

import pt.floraon.driver.Constants.NativeStatus;
import pt.floraon.driver.Constants.OccurrenceStatus;

public class NamesAndTerritoriesResult extends SimpleNameResult implements ResultItem {
	protected List<TerritoryStatus> territories;
	 
	private class Status {
		protected NativeStatus nativeStatus;
		protected OccurrenceStatus occurrenceStatus;
		protected Boolean uncertain;
		protected Status(String nativeStatus, String os, Boolean unc) {
			this.nativeStatus = NativeStatus.fromString(nativeStatus.toUpperCase());
			this.occurrenceStatus = os==null ? null : OccurrenceStatus.valueOf(os.toUpperCase());
			this.uncertain=unc;
		}
	}
	/**
	 * Computes the status of this taxon in each territory, by summarizing all the branches that connect the taxon with the territory
	 * @return
	 */
	private Map<String,Status> computeTerritoryStatus() {
		Map<String,Status> out=new HashMap<String,Status>();
		Map<String,List<TerritoryStatus>> ts=new HashMap<String,List<TerritoryStatus>>();
		TerritoryStatus tmp;
		Map<Integer,Set<String>> endemics=new HashMap<Integer,Set<String>>();// HashSet<String>();		// to count how many endemic relations it has
		List<TerritoryStatus> tStatusList;
		Iterator<TerritoryStatus> it;
		
		Integer minEndemicTaxLen=null;
		it=territories.iterator();
		while(it.hasNext()) {
			tmp=it.next();
			if(tmp.nativeStatus.equals(NativeStatus.ENDEMIC.toString())) {		// compile the endemic relations per depth level
				if(minEndemicTaxLen==null || minEndemicTaxLen>tmp.taxpathlen) minEndemicTaxLen=tmp.taxpathlen;
				if(!endemics.containsKey(tmp.taxpathlen))
					endemics.put(tmp.taxpathlen, new HashSet<String>());
				endemics.get(tmp.taxpathlen).add(tmp.existsId);
			}
		}
		// compile, for each unique territory, all the status that lead to it, for this taxon.
		it=territories.iterator();
		while(it.hasNext()) {
			tmp=it.next();
			if(!ts.containsKey(tmp.territory))
				ts.put(tmp.territory, tStatusList=new ArrayList<TerritoryStatus>());
			else
				tStatusList=ts.get(tmp.territory);
			tStatusList.add(tmp);
		}
		
		int nend;
		boolean certain;
		Boolean anyNotInferred;
		TerritoryStatus tmp2,tmp3;
		Iterator<TerritoryStatus> it1;
		for(Entry<String,List<TerritoryStatus>> e : ts.entrySet()) {	// for each unique territory
			tStatusList=e.getValue();
			nend=0;
			certain=false;
			it=tStatusList.iterator();
			while(it.hasNext()) {	// count # of endemic relations in this territory. NOTE: this assumes that there are no repeated endemic relations in the data (we could use a SET to avoid this assumption)
				tmp2=it.next();
				if(tmp2.nativeStatus.equals(NativeStatus.ENDEMIC.toString()) && tmp2.taxpathlen==minEndemicTaxLen) nend++;
				certain|=(!tmp2.uncertain); // || tmp2.nativeStatus.equals(NativeStatus.ENDEMIC.toString());	// if higher taxon is endemic, then all children are endemic for sure
			}
// TODO: when is endemic from a territory, what should we do with sub-territories?
			//System.out.println(tmp1.size()+" - "+endemics.size());
			if(nend>0 && nend==endemics.get(minEndemicTaxLen).size()) {		// if all endemic relations lead to this territory, then it is endemic, no matter the status in any other territory
				Set<String> oss=new HashSet<String>();
				it1=tStatusList.iterator();
				while(it1.hasNext()) {
					tmp2=it1.next();
					if(tmp2.taxpathlen==minEndemicTaxLen && tmp2.nativeStatus.equals(NativeStatus.ENDEMIC.toString())) oss.add(tmp2.occurrenceStatus);
				}				
				out.put(e.getKey(), new Status(NativeStatus.ENDEMIC.toString()
					, oss.size()==1 ? oss.iterator().next() : OccurrenceStatus.OCCURS.toString(), !certain)	// if the endemism is of one child only, then the occurrence status remains the same, otherwise it is undetermined
					);
			} else {	// not endemic, or endemic also from other territory that is not child of this one
				//out.put(e.getKey(), new Status(NativeStatus.EXISTING.toString(), OccurrenceStatus.OCCURS.toString(), !certain ));
				// search for a not inferred relation
				anyNotInferred=false;
				it1=tStatusList.iterator();
				tmp2=null;
				tmp3=null;
				int nnicert=0;
				while(it1.hasNext()) {
					tmp2=it1.next();
					if(!tmp2.inferred) {
						if(!tmp2.uncertain && nnicert>0) {
							anyNotInferred=null;
							break;
						}
						anyNotInferred=true;
						if(!tmp2.uncertain) nnicert++;
						tmp3=tmp2;
					}
				}
				if(anyNotInferred==null)	// there is more than one assigned native status to this territory!
					out.put(e.getKey(), new Status(NativeStatus.ERROR.toString(), OccurrenceStatus.OCCURS.toString(), !certain ));
				else if(!anyNotInferred) {	// all statuses are inferred, check if there are different statuses
					Set<String> ns=new HashSet<String>();
					it1=tStatusList.iterator();
					tmp3=null;
					while(it1.hasNext()) {
						tmp2=it1.next();
						if(tmp2.inferred) {
							ns.add(tmp2.nativeStatus);
							tmp3=tmp2;
						}
					}
					if(ns.size()==1) {
						out.put(e.getKey(), new Status(tmp3.nativeStatus.toString().equals("ENDEMIC") ? NativeStatus.EXISTING.toString() : tmp3.nativeStatus.toString(), tmp3.occurrenceStatus==null ? OccurrenceStatus.OCCURS.toString() : tmp3.occurrenceStatus.toString(), !certain ));
					} else
						out.put(e.getKey(), new Status(NativeStatus.EXISTING.toString(), OccurrenceStatus.OCCURS.toString(), !certain ));
				} else	// at least one status is not inferred
					out.put(e.getKey(), new Status(tmp3.nativeStatus.toString().equals("ENDEMIC") ? NativeStatus.NATIVE.toString() : tmp3.nativeStatus, tmp3.occurrenceStatus, !certain ));
			}
			
		}
		
		return out;
	}
	
	@Override
	public String toHTMLTableRow(Object obj) {
		Map<String,Status> tStatus=computeTerritoryStatus();
		Status tmp;
		@SuppressWarnings("unchecked")
		List<String> allTerritories=(List<String>) obj;
		StringBuilder sb=new StringBuilder();
		try {
			sb.append("<tr data-key=\"").append(this.taxent.getID()).append("\"")
				.append(this.taxent.getCurrent()==null ? "" : (this.taxent.getCurrent() ? "" : " class=\"notcurrent\""))
				.append("><td><a href=\"/floraon/admin?w=taxdetails&id="+URLEncoder.encode(this.taxent.getID(), StandardCharsets.UTF_8.name())+"\"><i>")
				.append(this.leaf==null ? "" : (this.leaf ? "" : "+"))
				.append(this.taxent.getNameWithAnnotation())
				.append("</i></a></td><td>")
				.append(this.taxent.getAuthor())
				.append("</td><td>");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(this.territories!=null) {
			for(String t : allTerritories) {
				if(tStatus.containsKey(t)) {
					tmp=tStatus.get(t);
					sb.append("<div class=\"territory ").append(tmp.nativeStatus.toString()).append("\">");
					if(tmp.occurrenceStatus!=null)
						sb.append("<div class=\"occurrencestatus ").append(tmp.occurrenceStatus.toString()).append("\">").append("</div>");
					if(tmp.uncertain)
						sb.append("<div class=\"occurrencestatus uncertain\"></div>");
					sb.append("<div class=\"legend\">").append(t).append("</div></div>");
				} else
					sb.append("<div class=\"territory\"><div class=\"legend\">").append(t).append("</div></div>");
			}
		}
		sb.append("</td></tr>");
		return sb.toString();
	}
	
	@Override
	public void toCSVLine(CSVPrinter rec, Object obj) throws IOException {
		Map<String,Status> tStatus=computeTerritoryStatus();
		Status tmp;
		@SuppressWarnings("unchecked")
		List<String> allTerritories=(List<String>) obj;
		rec.print(this.taxent.getID());
		rec.print((this.leaf==null ? "" : (this.leaf ? "" : "+"))+this.taxent.getNameWithAnnotation());
		rec.print(this.taxent.getAuthor());
		if(this.territories==null) return;

		for(String t : allTerritories) {
			if(tStatus.containsKey(t)) {
				tmp=tStatus.get(t);
				if(tmp.occurrenceStatus!=null && tmp.occurrenceStatus!=OccurrenceStatus.OCCURS)
					rec.print((tmp.uncertain ? "?" : "")+tmp.nativeStatus.toString()+" ("+tmp.occurrenceStatus.toString()+")");
				else rec.print((tmp.uncertain ? "?" : "")+tmp.nativeStatus.toString());
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

}
