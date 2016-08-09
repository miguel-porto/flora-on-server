package pt.floraon.results;

import java.util.Collections;
import java.util.List;

import pt.floraon.driver.Constants;
import pt.floraon.driver.Constants.NativeStatus;
import pt.floraon.driver.Constants.OccurrenceStatus;
import pt.floraon.driver.Constants.WorldNativeDistributionCompleteness;
import pt.floraon.entities.EXISTS_IN;
import pt.floraon.entities.Territory;

/**
 * Represents a triplet {@link TaxEnt}-{@link EXISTS_IN}-{@link Territory} and the summary of the edge types
 * that have been traversed, and in which direction.
 * @author miguel
 *
 */
public class TerritoryStatus implements Comparable<TerritoryStatus> {
	protected Integer depth;
	protected EXISTS_IN existsIn;
	protected Territory territory;
	/**
	 * The list of vertex names traversed, starting in the TaxEnt name
	 */
	protected List<String> vertices;
	/**
	 * The list of edge types traversed in this path. Must correspond exactly to the names of edge classes in {@link pt.floraon.entities}
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

	@Override
	public int compareTo(TerritoryStatus o) {
		Integer priorityObj =
			(Collections.frequency(o.edges, Constants.RelTypes.PART_OF.toString()) > 0 ? 0 : 2)
			+ (o.edges.contains(Constants.RelTypes.BELONGS_TO.toString()) ? 0 : 1);

		Integer priorityThis =
			(Collections.frequency(this.edges, Constants.RelTypes.PART_OF.toString()) > 0 ? 0 : 2)
			+ (this.edges.contains(Constants.RelTypes.BELONGS_TO.toString()) ? 0 : 1);
		
		return priorityThis.compareTo(priorityObj);
	}
	
	
	public TerritoryStatus merge(TerritoryStatus o) { // TODO: introducedStatus and Naturalizationdegree
		int better = this.compareTo(o);
		if(better < 0)	// o is better, replace status!
			return o;
		else if(better==0) {	// tmp2 is equally good, check statuses
			if(!this.existsIn.getNativeStatus().equals(o.existsIn.getNativeStatus())) this.existsIn.setNativeStatus(NativeStatus.MULTIPLE_STATUS);
			if(this.existsIn.getOccurrenceStatus() == null || !this.existsIn.getOccurrenceStatus().equals(o.existsIn.getOccurrenceStatus())) this.existsIn.setOccurrenceStatus(OccurrenceStatus.PRESENT);
			return this;
		} else return this;
	}
}
