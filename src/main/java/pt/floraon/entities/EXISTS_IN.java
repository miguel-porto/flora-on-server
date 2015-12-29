package pt.floraon.entities;

import pt.floraon.driver.Constants.NativeStatus;

/**
 * Describes the association of one taxon to one {@link TerritoryVertex}.
 * It is not meant to describe the observation of a taxon in an inventory, for that we use {@link OBSERVED_IN}
 * @author miguel
 *
 */
public class EXISTS_IN extends GeneralDBEdge {
	public NativeStatus nativeStatus;
	
	public EXISTS_IN(NativeStatus nativeStatus) {
		this.nativeStatus=nativeStatus;
	}
}
