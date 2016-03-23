package pt.floraon.results;

public class TerritoryStatus {
	protected String existsId, nativeStatus, occurrenceStatus, territory;
	protected Boolean inferred;		// true if this status is inferred from the status of a child territory
	protected Boolean uncertain;	// true if this status is assigned to a higher-level taxonomic node
	protected Integer taxpathlen;	// the number of uphill taxonomic PART_OFs that were climbed
}
