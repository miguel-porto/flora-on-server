package pt.floraon.driver.results;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import pt.floraon.driver.Constants;
import pt.floraon.driver.Constants.NativeStatus;
import pt.floraon.taxonomy.entities.Territory;
/**
 * A wrapper class for a List<TerritoryStatus> which adds methods for inferring the NativeStatus in all territories.
 * @author miguel
 *
 */
public class ListOfTerritoryStatus {
	protected List<TerritoryStatus> territoryStatusList;
	
	public ListOfTerritoryStatus(List<TerritoryStatus> list) {
		this.territoryStatusList = list;
	}

	/**
	 * Computes the status of this taxon in each territory marked for checklist, by summarizing all the branches that connect the taxon with the territory
	 * @return
	 */
	public Map<String,InferredStatus> computeTerritoryStatus(Boolean worldDistributionComplete) {
		Set<Territory> endemismDegree = this.computeNativeExtent(null);

		// compile the territories marked for checklist
		Set<String> checklistTerritories = new HashSet<String>();
		for(TerritoryStatus ts : this.territoryStatusList) {
			if(ts.territory.getShowInChecklist())
				checklistTerritories.add(ts.territory.getShortName());
		}
		
		return computeTerritoryStatus(checklistTerritories, worldDistributionComplete, endemismDegree);
	}

	/**
	 * Computes the status of this taxon for the given territory, by summarizing all the branches that connect the taxon with the territory
	 * @param territory The shortName of the {@link Territory}
	 * @param worldDistributionComplete
	 * @return
	 */
	public InferredStatus computeTerritoryStatus(String territory, boolean worldDistributionComplete) {
		Set<Territory> endemismDegree = this.computeNativeExtent(null);
		Set<String> thisTerritory = new HashSet<String>();
		thisTerritory.add(territory);
		Map<String, InferredStatus> tmp = computeTerritoryStatus(thisTerritory, worldDistributionComplete, endemismDegree);
		if(tmp.isEmpty())
			return null;	// null status, does not exist
		else
			return tmp.get(territory);
	}

	private Map<String,InferredStatus> computeTerritoryStatus(Set<String> territories, boolean worldDistributionComplete, Set<Territory> endemismDegree) {
		// Now iterate over all statuses of this territory, to find the most rigorous and direct information, respecting a priority order:
		// 3: not inferred and certain
		// 2: inferred from subterritory and certain
		// 1: not inferred and uncertain
		// 0: inferred and uncertain
		// When the priority is the same, check whether the statuses are different or equal. If different, assign a general/multiple status
		
		Map<String,InferredStatus> out=new HashMap<String,InferredStatus>();
		TerritoryStatus thisStatus, tmp;
		boolean isEndemic;
		Iterator<TerritoryStatus> it;
		
		for(String territory : territories) {
			thisStatus = null;
			// first check if it is endemic to this territory or not
			if(worldDistributionComplete)
				isEndemic = isEndemicToTerritory(endemismDegree, territory);
			else
				isEndemic = false;

			it = this.territoryStatusList.iterator();
			while(it.hasNext()) {	// look for a path that leads to this territory
				tmp=it.next();
				if(!tmp.territory.getShortName().equals(territory)) continue;
				//System.out.println(tmp.territory.getName()+" "+tmp.completeDistributionUpstream);
				if(thisStatus == null)
					thisStatus = tmp.merge(tmp, tmp.completeDistributionUpstream != null
						|| !tmp.isInferredFromChildTerritory());	// is distribution complete within this territory? Or is it a direct assignment? If so, we propagate OccurrenceStatus etc.
				else
					thisStatus = thisStatus.merge(tmp, tmp.completeDistributionUpstream != null
						|| !tmp.isInferredFromChildTerritory());	// is distribution complete within this territory? Or is it a direct assignment? If so, we propagate OccurrenceStatus etc.
			}

			if(thisStatus != null && thisStatus.existsIn.getNativeStatus() != NativeStatus.NULL)
				out.put(territory, new InferredStatus(thisStatus, isEndemic));
		}
		return out;
	}

	/**
	 * Checks if this TaxEnt is endemic to the given territory
	 * @param endemicTerritories
	 * @param territoryShortName
	 * @return
	 */
	private boolean isEndemicToTerritory(Set<Territory> endemicTerritories, String territoryShortName) {
		TerritoryStatus tmp;
		Iterator<TerritoryStatus> it;
		Boolean isEndemic = null, chk;
		// check if all the endemic territories are in the path of this territory
//		List<String> eDegs = this.computeEndemismDegreeName();
//		System.out.println("****\nChecking: "+territory);
//		System.out.println("Endemic to: "+OccurrenceConstants.implode(", ", eDegs.toArray(new String[0])));
		for(Territory terr : endemicTerritories) {
			it = this.territoryStatusList.iterator();
//			System.out.println("  Checking endemic terr: "+terr.getName());
			chk = false;
			while(it.hasNext()) {
				tmp = it.next();
				if(!tmp.territory.getShortName().equals(territoryShortName)) continue;
				// for each territory path that leads to the inquired territory
//				System.out.println(tmp.territory.getName()+": "+OccurrenceConstants.implode(", ", tmp.vertices.toArray(new String[0])));
				if(tmp.vertices.contains(terr.getID())) {
					// yes this endemic terr is on the path of the inquired territory
					chk = true;
					break;
				}
			}
			// it is only endemic to the inquired territory if all the endemic territories are in the path of the inquired territory:
			if(isEndemic == null) isEndemic = true;
			isEndemic &= chk;
			if(!isEndemic) break;
		}
		return isEndemic == null ? false : isEndemic;
	}

	public String getSingleSmallestTerritory() {
		List<List<String>> paths = new ArrayList<List<String>>();
		for(TerritoryStatus ts : this.territoryStatusList)
			if(ts.existsIn.getNativeStatus().isNative()) paths.add(ts.vertices);

		/*System.out.println("before:");
		for(List<String> s : paths) {
			for(String s1 : s)
				System.out.println(s1+"; ");
			System.out.println();
		}*/
		
		// compile all distinct paths that terminate in a root node
		for(TerritoryStatus ts1 : this.territoryStatusList) {
			if(!ts1.existsIn.getNativeStatus().isNative()) continue;
			for(TerritoryStatus ts2 : this.territoryStatusList) {
				if(ts1 == ts2 || !ts1.existsIn.getNativeStatus().isNative()) continue;
				if(ts1.vertices.size() > ts2.vertices.size()) {
					if(ts1.vertices.subList(0, ts2.vertices.size()).containsAll(ts2.vertices)) paths.remove(ts2.vertices);
				} else {
					if(ts2.vertices.subList(0, ts1.vertices.size()).containsAll(ts1.vertices)) paths.remove(ts1.vertices);
				}
			}
		}
		System.out.println("After:");
		for(List<String> s : paths) {
			for(String s1 : s)
				System.out.println(s1+"; ");
			System.out.println();
		}
		
		// now find all unique vertices of the rooted paths above
		Set<String> common = new HashSet<String>();
		Set<String> tmp;
		Set<String> candidates = new HashSet<String>();
		for(List<String> p : paths) common.addAll(p);
		// for each vertex, check whether the paths where it appears cover all outbound possibilities (i.e. all territories with direct native status assigned)
		for(String s : common) {
			tmp = new HashSet<String>();
			for(List<String> p : paths) {
				if(p.contains(s)) tmp.add(p.get(1));	// FIXME FIXME we cannot assume the 1st territory is in index 1!!!
			}
			if(tmp.size() == 3) candidates.add(s);	//nº assignments directos 
		}
		
	System.out.println("Candidates:");
	for(String s : candidates) {
		System.out.println(s);
	}
		// now check, from the common vertices, which one is the lowest (i.e. the samllest territory)
		int i, mini=10000;
		String out = null;
		
		for(String s : candidates) {
			// we can take any path here, since they are common to all paths
			i = paths.get(0).indexOf(s);
			if(i > 0 && i < mini) {
				mini = i;
				out = s;
			}
		}
		System.out.println("RESUTL: "+out);
		return out;
	}
	/**
	 * Gets the smallest geographical area that comprises all native occurrences of this TaxEnt.
	 * This is usually an array of territories, as it does not perform aggregation.
	 * Note that this is only meaningful if the worldNativeDistribution is complete.
	 * @param directFrom NULL will return the area through all possible taxonomic paths (navigating through SYNONYM and PART_OFs.
	 * Otherwise, the ID of the TaxEnt from which only the direct EXISTS_IN assignments will be considered.
	 * @return
	 */
	public Set<Territory> computeNativeExtent(String directFrom) {
		Set<Territory> out = new HashSet<Territory>();
		Set<String> terr = new HashSet<String>();
		Map<String,Integer> nativeExistsIn = new HashMap<String,Integer>();
		// compile all native NativeStatus
		for(TerritoryStatus ts : this.territoryStatusList) {
			// exclude non-native statuses
			if(!ts.existsIn.getNativeStatus().isNative()) continue;
			// exclude indirect assignments
			if(directFrom != null && !ts.existsIn.getFrom().equals(directFrom)) continue;
			// compile the unique EXISTS_IN edges going out from this TaxEnt and the respective taxonomic depth
			// each EXISTS_IN is a different territory route in the graph
			//nativeExistsIn.put(ts.existsIn.getID(), ts.edges.indexOf(OccurrenceConstants.RelTypes.EXISTS_IN.toString()));
			// the taxonomic depth is the number of PART_OF relations
			nativeExistsIn.put(ts.existsIn.getID(), Collections.frequency(ts.edges, Constants.RelTypes.PART_OF.toString()));
			// compile the base territories, i.e. those which have a native status directly assigned
			if(!ts.isInferredFromChildTerritory()) terr.add(ts.territory.getID());		// only add direct territory assignments
		}
		if(nativeExistsIn.size() == 0) return Collections.emptySet();	// it has no NATIVE status
		int minExistsInDepth = Collections.min(nativeExistsIn.values());
		//System.out.println("Size "+nativeExistsIn.size()+" min "+minExistsInDepth);
		
		Set<String> exclude = new HashSet<String>();
		// check, for each territory where the taxon is native, if it is contained in any of the others 
		for(Entry<String, Integer> ei : nativeExistsIn.entrySet()) {	// for each EXISTS_IN route
			if(!ei.getValue().equals(minExistsInDepth)) continue;	// the best inference is the minimum depth of EXISTS_IN
			
			//System.out.println("EI: "+ei.getKey());
			// for each EXISTS_IN route
			for(TerritoryStatus ts : this.territoryStatusList) {
				//if(!ts.existsIn.getID().equals(ei.getKey()) || ts.vertices.size() <= 2) continue;
				if(!ts.existsIn.getID().equals(ei.getKey()) || !ts.isInferredFromChildTerritory()) continue;
				// this is one EXISTS_IN route and we only want to test upstream territories (above the base territory, so there's a BELONGS_TO link)
				//System.out.println(OccurrenceConstants.implode(", ", ts.vertices.subList(2, ts.vertices.size()).toArray(new String[0]) ));
				//System.out.println(OccurrenceConstants.implode(", ", terr.toArray(new String[0])));
				//if(!Collections.disjoint(ts.vertices.subList(minExistsInDepth + 2, ts.vertices.size()), terr)) exclude.add(ts.existsIn.getID());
				// extract vertices after EXISTS_IN (so, territories)
				if(!Collections.disjoint(ts.getTerritoryPath(), terr)) exclude.add(ts.existsIn.getID());
			}
		}
		/*String[] ex=exclude.toArray(new String[exclude.size()]);
		System.out.println("Exclu: "+ OccurrenceConstants.implode(", ", ex));*/
		int min;
		TerritoryStatus mini;
		for(Entry<String, Integer> ei : nativeExistsIn.entrySet()) {	// for each EXISTS_IN route
			if(exclude.contains(ei.getKey())) continue;
			
			// for each EXISTS_IN route fetch the nearest territory
			min = 1000;
			mini = null;
			for(TerritoryStatus ts : this.territoryStatusList) {
				if(!ts.existsIn.getID().equals(ei.getKey())) continue;
				if(ts.edges.size() < min) {
					min = ts.edges.size();
					mini = ts;
				}
			}
			if(mini != null) out.add(mini.territory);
		}
		return out;
	}
	
	/**
	 * Within each given territory, compute to which subterritories the taxon is restricted to
	 * @param territories The IDs of the territories
	 * @return
	 */
	public Map<String, Set<Territory>> computeRestrictedTo(Set<String> territories) {
		Map<String, Set<Territory>> out = new HashMap<String, Set<Territory>>();
		Map<String, Set<Territory>> tmp;
		Iterator<String> tmpIt;
		String terrName;
		for(String terr : territories) {
			tmp = computeRestrictedTo(terr);
			tmpIt = tmp.keySet().iterator();
			if(tmpIt.hasNext()) {
				terrName = tmpIt.next();
				if(tmp.get(terrName).size() > 0) out.put(terrName, tmp.get(terrName));
			}
		}
		return out;
	}

	/**
	 * Within the given territory, compute to which subterritories the taxon is restricted to
	 * @param thisTerr The IDs of the territories
	 * @return
	 */
	public Map<String, Set<Territory>> computeRestrictedTo(String thisTerr) {
		Map<String, Set<Territory>> out = new HashMap<String, Set<Territory>>();
		Set<String> terr = new HashSet<String>();
		Map<String,Integer> allExistsIn = new HashMap<String,Integer>();
		String terrName = null;
		// compile all NativeStatus
		for(TerritoryStatus ts : this.territoryStatusList) {
			// compile the unique EXISTS_IN edges going out from this TaxEnt and the respective taxonomic depth
			// each EXISTS_IN is a different territory route in the graph
			// the taxonomic depth is the number of PART_OF relations
			if(ts.territory.getID().equals(thisTerr)) terrName = ts.territory.getShortName();
			if(ts.completeDistributionUpstream == null || ts.isInferredFromChildTaxEnt()) continue;
			if(!ts.territory.getID().equals(thisTerr) && !ts.vertices.contains(thisTerr)) continue;
			
			allExistsIn.put(ts.existsIn.getID(), Collections.frequency(ts.edges, Constants.RelTypes.PART_OF.toString()));
			// compile the base territories, i.e. those which have a native status directly assigned
			if(!ts.isInferredFromChildTerritory()) terr.add(ts.territory.getID());		// only add direct territory assignments
		}
		if(allExistsIn.size() == 0) return Collections.emptyMap();	// it has no status
		int minExistsInDepth = Collections.min(allExistsIn.values());
		
		Set<String> exclude = new HashSet<String>();
		// check, for each territory where the taxon is native, if it is contained in any of the others
		// TODO this does not work in SYNONYM chains, e.g. Centaure langei rothmaleriana!
		for(Entry<String, Integer> ei : allExistsIn.entrySet()) {	// for each EXISTS_IN route
			if(!ei.getValue().equals(minExistsInDepth)) continue;	// the best inference is the minimum depth of EXISTS_IN
			
			//System.out.println("EI: "+ei.getKey());
			// for each EXISTS_IN route
			for(TerritoryStatus ts : this.territoryStatusList) {
				if(!ts.existsIn.getID().equals(ei.getKey()) || !ts.isInferredFromChildTerritory()) continue;
				// this is one EXISTS_IN route and we only want to test upstream territories (above the base territory, so there's a BELONGS_TO link)
				// extract vertices after EXISTS_IN (so, territories)
				if(!Collections.disjoint(ts.getTerritoryPath(), terr)) exclude.add(ts.existsIn.getID());
			}
		}
		/*String[] ex=exclude.toArray(new String[exclude.size()]);
		System.out.println("Exclu: "+ OccurrenceConstants.implode(", ", ex));*/
		int min;
		TerritoryStatus mini;
		Set<Territory> tmp;
		for(Entry<String, Integer> ei : allExistsIn.entrySet()) {	// for each EXISTS_IN route
			if(exclude.contains(ei.getKey())) continue;
			
			// for each EXISTS_IN route fetch the nearest territory
			min = 1000;
			mini = null;
			for(TerritoryStatus ts : this.territoryStatusList) {
				if(!ts.existsIn.getID().equals(ei.getKey())
					|| ts.vertices.contains(thisTerr.toString())) continue;
				if(ts.edges.size() < min) {
					min = ts.edges.size();
					mini = ts;
				}
			}
			if(mini != null) {
				if(out.containsKey(terrName))
					out.get(terrName).add(mini.territory);
				else {
					tmp = new HashSet<Territory>();
					tmp.add(mini.territory);
					out.put(terrName, tmp);
				}
			}
		}
		
		return out;
	}
}