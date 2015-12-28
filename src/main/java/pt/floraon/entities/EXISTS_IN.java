package pt.floraon.entities;

/**
 * Describes the association of one taxon to one territory.
 * It is not meant to describe the observation of a taxon in an inventory, for that we use OBSERVED_IN
 * @author miguel
 *
 */
public class EXISTS_IN extends GeneralDBEdge {
	public Boolean endemic;
	
	public EXISTS_IN(boolean endemic) {
		this.endemic=endemic;
	}
}
