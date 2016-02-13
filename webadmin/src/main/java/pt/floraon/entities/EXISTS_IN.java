package pt.floraon.entities;

import pt.floraon.driver.Constants.NativeStatus;
import pt.floraon.driver.Constants.OccurrenceStatus;
import pt.floraon.driver.Constants.RelTypes;

/**
 * Describes the association of one taxon to one {@link Territory}.
 * It is not meant to describe the observation of a taxon in an inventory, for that we use {@link OBSERVED_IN}
 * @author miguel
 *
 */
public class EXISTS_IN extends GeneralDBEdge {
	protected NativeStatus nativeStatus;
	protected OccurrenceStatus occurrenceStatus;
	
	public EXISTS_IN(NativeStatus nativeStatus) {
		this.nativeStatus=nativeStatus;
		this.occurrenceStatus=OccurrenceStatus.OCCURS;
	}

	public EXISTS_IN(NativeStatus nativeStatus, OccurrenceStatus occurrenceStatus) {
		this.nativeStatus=nativeStatus;
		this.occurrenceStatus=occurrenceStatus;
	}

	public EXISTS_IN(NativeStatus nativeStatus, OccurrenceStatus occurrenceStatus, String from, String to) {
		this.nativeStatus=nativeStatus;
		this.occurrenceStatus=occurrenceStatus;
		this._from=from;
		this._to=to;
	}
	
	public NativeStatus getNativeStatus() {
		return this.nativeStatus==null ? NativeStatus.ERROR : this.nativeStatus;
	}
	
	public OccurrenceStatus getOccurrenceStatus() {
		return this.occurrenceStatus==null ? OccurrenceStatus.OCCURS : this.occurrenceStatus;
	}
	
	@Override
	public RelTypes getType() {
		return RelTypes.EXISTS_IN;
	}

}
