package pt.floraon.results;

public class TerritoryStatus implements Comparable<TerritoryStatus> {
	protected String existsId, nativeStatus, occurrenceStatus, territory;
	//protected NativeStatus ;
	protected Boolean uncertainOccurrence;
	protected Boolean inferred;		// true if this status is inferred from the status of a child territory
	//protected Boolean possibly;	// true if this status is assigned to a higher-level taxonomic node
	protected Integer taxpathlen;	// the number of uphill taxonomic PART_OFs that were climbed

	@Override
	public int compareTo(TerritoryStatus o) {
		Integer priorityObj=(o.taxpathlen>0 ? 0 : 2) + (o.inferred ? 0 : 1);
		Integer prioritythis=(this.taxpathlen>0 ? 0 : 2) + (this.inferred ? 0 : 1);
		return prioritythis.compareTo(priorityObj);
	}
}
