package pt.floraon.driver.results;

import java.util.Collections;
import java.util.List;

import pt.floraon.driver.Constants;
import pt.floraon.driver.Constants.WorldNativeDistributionCompleteness;
import pt.floraon.taxonomy.entities.EXISTS_IN;
import pt.floraon.taxonomy.entities.Territory;

/**
 * Represents a triplet {@link TaxEnt}-{@link EXISTS_IN}-{@link Territory} and the summary of the edge types
 * that have been traversed, and in which direction.
 * @author miguel
 *
 */
public class TerritoryStatus implements Comparable<TerritoryStatus> {
	protected Integer depth;
	protected EXISTS_IN existsIn;
	/**
	 * The territory that this status refers to
	 */
	protected Territory territory;
	/**
	 * The list of vertex IDs traversed, starting in the TaxEnt name
	 */
	protected List<String> vertices;
	/**
	 * The list of edge types traversed in this path. Must correspond exactly to the names of edge classes in {@link pt.floraon.driver.entities}
	 */
	protected List<String> edges;
	/**
	 * The list of directions in which each edge was traversed. Corresponds 1:1 to the edges list.
	 */
	protected List<String> direction;
	/**
	 * The list of worldDistributionCompleteness of all traversed vertices (obviously null for non-TaxEnt vertices)
	 */
	protected List<WorldNativeDistributionCompleteness> worldDistributionCompleteness;
	/**
	 * The ID of the upstream territory, if any, for which distribution is complete for this taxent
	 */
	protected String completeDistributionUpstream;

	/**
	 * Is this status in this territory inferred from the status in a subterritory?
	 * @return
	 */
	public boolean isInferredFromChildTerritory() {
		return this.edges.contains(Constants.RelTypes.BELONGS_TO.toString());
	}
	
	/**
	 * Is this status in this territory inferred from the status of a subtaxon?
	 * @return
	 */
	public boolean isInferredFromChildTaxEnt() {
		return this.edges.contains(Constants.RelTypes.PART_OF.toString());
	}
	
	/**
	 * Extracts the vertices of the territory portion of the path, namely the territory IDs after the EXISTS_IN.
	 * @return
	 */
	public List<String> getTerritoryPath() {
		return this.vertices.subList(this.edges.indexOf(Constants.RelTypes.EXISTS_IN.toString()) + 2, this.vertices.size());
	}
	
	@Override
	public int compareTo(TerritoryStatus o) {
		Integer priorityObj =
			(Collections.frequency(o.edges, Constants.RelTypes.PART_OF.toString()) > 0 ? 0 : 2)
			+ (o.isInferredFromChildTerritory() ? 0 : 1);		// we don't prioritize on the number of BELONGS_TO. We just prioritize direct links over indirect links.

		Integer priorityThis =
			(Collections.frequency(this.edges, Constants.RelTypes.PART_OF.toString()) > 0 ? 0 : 2)
			+ (this.isInferredFromChildTerritory() ? 0 : 1);
		
		return priorityThis.compareTo(priorityObj);
	}
	
	
	public TerritoryStatus merge(TerritoryStatus o, boolean propagateStatus) {
		int better = this.compareTo(o);
		if(better < 0)	// o is better, replace status!
			return o;
		else if(better==0)	// tmp2 is equally good (for example, status assigned to multiple subterritories), so merge statuses
			this.existsIn.mergeWith(o.existsIn, propagateStatus);
		
		if(this.isInferredFromChildTerritory()) {		// if the status is from a subterritory, we have some exceptions:
			if(this.completeDistributionUpstream == null)		// if the distribution is not complete, it is not possible to infer NativeStatus
				this.existsIn.setNativeStatus(Constants.NativeStatus.EXISTS);
			if(!this.existsIn.getOccurrenceStatus().isPresent() && !propagateStatus) this.existsIn.setNativeStatus(Constants.NativeStatus.NULL);
		}
		return this;
	}
}
