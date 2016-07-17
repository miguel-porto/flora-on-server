package pt.floraon.results;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import pt.floraon.driver.Constants;
import pt.floraon.driver.Constants.NativeStatus;
import pt.floraon.driver.Constants.Native_Exotic;
import pt.floraon.driver.Constants.OccurrenceStatus;

public class ListOfTerritoryStatus {
	protected List<TerritoryStatus> territoryStatusList;
	
	public ListOfTerritoryStatus(List<TerritoryStatus> list) {
		this.territoryStatusList = list;
	}
	
	public class Status {
		protected NativeStatus nativeStatus;
		protected OccurrenceStatus occurrenceStatus;
		/**
		 * Status is assigned to a parent taxon. Should be read: it is not certain that it is this [sub-]taxon that exists in this territory,
		 * but if it exists, then it is with this nativeStatus.
		 */
		protected Boolean possibly;
		/**
		 * Whether this taxon has been identified with certainty or not
		 */
		protected Boolean uncertainOccurrence;
		/**
		 * Wheter it is endemic in this territory (this can be inferred from the endemism in sub-territories)
		 */
		protected Boolean endemic;
		/**
		 * The long name of the territory this Status pertains to
		 */
		protected String territoryName;
		protected Status(String nativeStatus, String occurrenceStatus, Boolean uncertainOccurrence, Boolean possibly, Boolean endemic, String name) {
			this.nativeStatus = NativeStatus.fromString(nativeStatus.toUpperCase());
			try {
				this.occurrenceStatus = occurrenceStatus==null ? null : OccurrenceStatus.valueOf(occurrenceStatus.toUpperCase());
			} catch (IllegalArgumentException e) {		// NOTE: if constant is not found, assume it is PRESENT
				this.occurrenceStatus = OccurrenceStatus.PRESENT;
			}
			this.uncertainOccurrence=uncertainOccurrence;
			this.possibly=possibly;
			this.endemic=endemic;
			this.territoryName = name;
		}
		
		public NativeStatus getNativeStatus() {
			return this.nativeStatus;
		}

		public OccurrenceStatus getOccurrenceStatus() {
			return this.occurrenceStatus;
		}
		
		public boolean getPossibly() {
			return this.possibly;
		}

		public boolean getUncertainOccurrence() {
			return this.uncertainOccurrence;
		}

		public boolean getIsEndemic() {
			return this.endemic;
		}

		public String getTerritoryName() {
			return this.territoryName;
		}
		
		public String getVerbatimNativeStatus() {
			StringBuilder sb = new StringBuilder();
			String tmp1=null, tmp2=null;
			if(this.endemic && this.nativeStatus.isNativeOrExotic() == Native_Exotic.NATIVE)
				sb.append("ENDEMIC");
			else
				sb.append(this.nativeStatus.toString());
			if(this.possibly != null && this.possibly) tmp1="if it exists";
			if(this.uncertainOccurrence != null && this.uncertainOccurrence) tmp2="uncertain";
			tmp1 = Constants.implode(", ", tmp1, tmp2);
			if(tmp1 != null) sb.append(" (").append(tmp1).append(")");
			return sb.toString();
		}
	}

	/**
	 * Computes the status of this taxon in each territory, by summarizing all the branches that connect the taxon with the territory
	 * @return
	 */
	public Map<String,Status> computeTerritoryStatus(boolean isEndemic) {
		// if isEndemic is true, then the taxon is endemic to all territories in which it is native (so, the "native" means "endemic")
		Map<String,Status> out=new HashMap<String,Status>();
		Map<String,List<TerritoryStatus>> ts=new HashMap<String,List<TerritoryStatus>>();
		TerritoryStatus tmp;
		Map<Integer,Set<String>> endemics=new HashMap<Integer,Set<String>>();// HashSet<String>();		// to count how many endemic relations it has
		List<TerritoryStatus> tStatusList;
		Iterator<TerritoryStatus> it;
		List<NativeStatus> natives=Arrays.asList(Constants.NativeStatuses);
		
		Integer minEndemicTaxLen=null;
		if(isEndemic) {		// compile the endemic relations per taxonomic depth level
			it=this.territoryStatusList.iterator();
			while(it.hasNext()) {
				tmp=it.next();
				if(NativeStatus.fromString(tmp.nativeStatus).isNativeOrExotic() == Native_Exotic.NATIVE) {
				//if(natives.contains(NativeStatus.fromString(tmp.nativeStatus))) {
				//if(tmp.nativeStatus.equals(NativeStatus.NATIVE.toString())) {
					if(minEndemicTaxLen==null || minEndemicTaxLen>tmp.taxpathlen) minEndemicTaxLen=tmp.taxpathlen;
					if(!endemics.containsKey(tmp.taxpathlen))
						endemics.put(tmp.taxpathlen, new HashSet<String>());
					endemics.get(tmp.taxpathlen).add(tmp.existsId);
				}
			}
		}
		// compile, for each unique territory, all the status that lead to it, for this taxon.
		it=this.territoryStatusList.iterator();
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
		TerritoryStatus tmp2;
		Iterator<TerritoryStatus> it1;
		for(Entry<String,List<TerritoryStatus>> e : ts.entrySet()) {	// for each unique territory
			tStatusList=e.getValue();
			nend=0;
			certain=false;
			it=tStatusList.iterator();
			while(it.hasNext()) {	// count # of endemic relations in this territory. NOTE: this assumes that there are no repeated endemic relations in the data (we could use a SET to avoid this assumption)
				tmp2=it.next();
				//if(isEndemic && tmp2.nativeStatus.equals(NativeStatus.NATIVE.toString()) && tmp2.taxpathlen==minEndemicTaxLen) nend++;
				if(isEndemic && NativeStatus.fromString(tmp2.nativeStatus).isNativeOrExotic() == Native_Exotic.NATIVE && tmp2.taxpathlen==minEndemicTaxLen) nend++;
				certain |= (tmp2.taxpathlen==0); // || tmp2.nativeStatus.equals(NativeStatus.ENDEMIC.toString());	// if higher taxon is endemic, then all children are endemic for sure
			}
// TODO: when is endemic from a territory, what should we do with sub-territories? for example juniperus navicularis
			//System.out.println(tmp1.size()+" - "+endemics.size());
			if(nend>0 && nend==endemics.get(minEndemicTaxLen).size()) {		// if all endemic relations lead to this territory, then it is endemic, no matter the status in any other territory
				Set<String> oss=new HashSet<String>();
				it1=tStatusList.iterator();
				while(it1.hasNext()) {
					tmp2=it1.next();
					if(tmp2.taxpathlen==minEndemicTaxLen && tmp2.nativeStatus.equals(NativeStatus.NATIVE.toString())) oss.add(tmp2.occurrenceStatus);
				}				
				out.put(e.getKey(), new Status(
					NativeStatus.NATIVE.toString(),
					oss.size()==1 ? oss.iterator().next() : OccurrenceStatus.PRESENT.toString(),
					false,
					!certain,
					true,
					e.getValue().get(0).territoryName)	// if the endemism is of one child only, then the occurrence status remains the same, otherwise it is undetermined
				);
			} else {	// not endemic, or endemic also from other territory that is not child of this one - that is to say, any endemic status actually means native.
				TerritoryStatus thisStatus=null;
				it1=tStatusList.iterator();
				int better;
				// Now iterate over all statuses of this territory, to find the most rigorous and direct information, respecting a priority order:
				// 3: not inferred and certain
				// 2: inferred and certain
				// 1: not inferred and uncertain
				// 0: inferred and uncertain
				// When the priority is the same, check whether the statuses are different or equal. If different, assign a general/multiple status
				while(it1.hasNext()) {
					tmp2=it1.next();
					if(thisStatus==null) thisStatus=tmp2; else {
						better=thisStatus.compareTo(tmp2);
						if(better<0)	// tmp2 is better, replace status!
							thisStatus=tmp2;
						else if(better==0) {	// tmp2 is equally good, check statuses
							if(!thisStatus.nativeStatus.equals(tmp2.nativeStatus)) thisStatus.nativeStatus=NativeStatus.EXISTING.toString();
							if(thisStatus.occurrenceStatus==null || !thisStatus.occurrenceStatus.equals(tmp2.occurrenceStatus)) thisStatus.occurrenceStatus=OccurrenceStatus.PRESENT.toString();
						}
					}
				}
				out.put(e.getKey(), new Status(
					thisStatus.nativeStatus.toString(), 
					thisStatus.occurrenceStatus,
					thisStatus.uncertainOccurrence,
					thisStatus.taxpathlen>0,
					false,
					e.getValue().get(0).territoryName )
				);
				/*
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
				else if(!anyNotInferred) {	// all statuses are inferred from a subterritory
					// count the number of different native statuses and different occurrence statuses
					Set<String> ns=new HashSet<String>();
					Set<String> os=new HashSet<String>();
					it1=tStatusList.iterator();
					tmp3=null;
					while(it1.hasNext()) {
						tmp2=it1.next();
						if(tmp2.inferred) {
							ns.add(tmp2.nativeStatus);
							if(tmp2.occurrenceStatus!=null) os.add(tmp2.occurrenceStatus);
							tmp3=tmp2;
						}
					}
					if(ns.size()==1) {
						out.put(e.getKey(), new Status(
							tmp3.nativeStatus.equals(NativeStatus.ENDEMIC.toString()) ? NativeStatus.EXISTING.toString() : tmp3.nativeStatus.toString()
							, tmp3.occurrenceStatus==null ? OccurrenceStatus.OCCURS.toString() : tmp3.occurrenceStatus.toString()
							, !certain ));
					} else
						out.put(e.getKey(), new Status(
							NativeStatus.EXISTING.toString()
							, os.size()==1 ? (tmp3.occurrenceStatus==null ? OccurrenceStatus.OCCURS.toString() : tmp3.occurrenceStatus.toString()) : OccurrenceStatus.OCCURS.toString()
							, !certain ));
				} else	// at least one status is not inferred
					out.put(e.getKey(), new Status(
						tmp3.nativeStatus.equals(NativeStatus.ENDEMIC.toString()) ? NativeStatus.NATIVE.toString() : tmp3.nativeStatus
						, tmp3.occurrenceStatus
						, !certain ));*/
			}
		}
		
		return out;
	}
}
