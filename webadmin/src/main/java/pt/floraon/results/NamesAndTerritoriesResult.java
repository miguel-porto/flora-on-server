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
		protected Status(String ns, String os, Boolean unc) {
			this.nativeStatus = NativeStatus.fromString(ns.toUpperCase());
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
		Set<String> endemics=new HashSet<String>();		// to count how many endemic relations it has
		List<TerritoryStatus> tmp1;
		Iterator<TerritoryStatus> it;
		
		it=territories.iterator();
		while(it.hasNext()) {
			tmp=it.next();
			if(tmp.nativeStatus.equals(NativeStatus.ENDEMIC.toString())) endemics.add(tmp.existsId);
			if(!tmp.inferred)		// all the status which are not inferred, are directly added to the output
				out.put(tmp.territory, new Status(tmp.nativeStatus, tmp.occurrenceStatus, tmp.uncertain));
			else {			// others are compiled for each unique territory.
				if(!ts.containsKey(tmp.territory))
					ts.put(tmp.territory, tmp1=new ArrayList<TerritoryStatus>());
				else
					tmp1=ts.get(tmp.territory);
				tmp1.add(tmp);
			}
		}
		
		int nend;
		boolean certain;
		TerritoryStatus tmp2;
		for(Entry<String,List<TerritoryStatus>> e : ts.entrySet()) {	// for each unique territory
			tmp1=e.getValue();
			nend=0;
			certain=false;
			it=tmp1.iterator();
			while(it.hasNext()) {	// count # of endemic relations in this territory. NOTE: this assumes that there are no repeated endemic relations in the data (we could use a SET to avoid this assumption)
				tmp2=it.next();
				if(tmp2.nativeStatus.equals(NativeStatus.ENDEMIC.toString())) nend++;
				certain|=(!tmp2.uncertain);
				
			}
			//System.out.println(tmp1.size()+" - "+endemics.size());
			if(nend>0 && nend==endemics.size()) {		// if all endemic relations lead to this territory, then it is endemic, no matter the status in any other territory
				out.put(e.getKey(), new Status(NativeStatus.ENDEMIC.toString()
					, tmp1.size()==1 ? tmp1.get(0).occurrenceStatus : OccurrenceStatus.OCCURS.toString(), !certain)	// if the endemism is of one child only, then the occurrence status remains the same, otherwise it is undetermined
					);
			} else {	// not endemic, or endemic also from other territory that is not child of this one
				out.put(e.getKey(), new Status(NativeStatus.EXISTING.toString(), OccurrenceStatus.OCCURS.toString(), !certain ));
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
			sb.append("<tr data-key=\"").append(this._id).append("\"")
				.append(this.current==null ? "" : (this.current ? "" : " class=\"notcurrent\""))
				.append("><td><a href=\"/floraon/admin?w=taxdetails&id="+URLEncoder.encode(this._id, StandardCharsets.UTF_8.name())+"\"><i>")
				.append(this.leaf==null ? "" : (this.leaf ? "" : "+"))
				.append(this.name)
				.append("</i></a></td><td>")
				.append(this.author)
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
						sb.append("<div class=\"").append(tmp.occurrenceStatus.toString()).append("\">").append("</div>");
					if(tmp.uncertain)
						sb.append("<div class=\"uncertain\"></div>");
					sb.append(t).append("</div>");
				} else
					sb.append("<div class=\"territory\">").append(t).append("</div>");
			}
		}
		sb.append("</td></tr>");
		System.out.println("Output "+this.name);
		return sb.toString();
	}
	
	@Override
	public void toCSVLine(CSVPrinter rec, Object obj) throws IOException {
		Map<String,Status> tStatus=computeTerritoryStatus();
		Status tmp;
		@SuppressWarnings("unchecked")
		List<String> allTerritories=(List<String>) obj;
		rec.print(this._id);
		rec.print((this.leaf==null ? "" : (this.leaf ? "" : "+"))+this.name);
		rec.print(this.author);
		if(this.territories==null) return;

		for(String t : allTerritories) {
			if(tStatus.containsKey(t)) {
				tmp=tStatus.get(t);
				if(tmp.occurrenceStatus!=null && tmp.occurrenceStatus!=OccurrenceStatus.OCCURS)
					rec.print(tmp.nativeStatus.toString()+"; "+tmp.occurrenceStatus.toString());
				else rec.print(tmp.nativeStatus.toString());
				/*if(tmp.uncertain)		TODO: what if uncertain?
					sb.append("<div class=\"uncertain\"></div>");*/
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
