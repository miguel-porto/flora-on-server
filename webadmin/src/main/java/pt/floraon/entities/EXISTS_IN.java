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
	/**
	 * If true, it is uncertain the identity of the taxon in this territory, but, shall the identification be correct, then it occurs with the reported NativeStatus and OccurrenceStatus.
	 */
	protected Boolean uncertainOccurrenceStatus;
	
	public EXISTS_IN(NativeStatus nativeStatus) {
		this.nativeStatus=nativeStatus;
		this.occurrenceStatus=OccurrenceStatus.PRESENT;
	}

	public EXISTS_IN(NativeStatus nativeStatus, OccurrenceStatus occurrenceStatus) {
		this.nativeStatus=nativeStatus;
		this.occurrenceStatus=occurrenceStatus;
	}

	public EXISTS_IN(NativeStatus nativeStatus, OccurrenceStatus occurrenceStatus, Boolean uncertain, String from, String to) {
		this.nativeStatus=nativeStatus;
		this.occurrenceStatus=occurrenceStatus;
		this.uncertainOccurrenceStatus=uncertain;
		this._from=from;
		this._to=to;
	}
	
	public NativeStatus getNativeStatus() {
		return this.nativeStatus==null ? NativeStatus.ERROR : this.nativeStatus;
	}
	
	public OccurrenceStatus getOccurrenceStatus() {
		if(this.occurrenceStatus == OccurrenceStatus.RARE
			|| this.occurrenceStatus == OccurrenceStatus.COMMON
			|| this.occurrenceStatus == OccurrenceStatus.OCCURS
			|| this.occurrenceStatus == OccurrenceStatus.UNCERTAIN_OCCURRENCE) return OccurrenceStatus.ERROR;
		return this.occurrenceStatus==null ? OccurrenceStatus.PRESENT : this.occurrenceStatus;	// assume it is present, if no information is given, or if an error has occurred (value not found in enum)
	}
	
	/**
	 * Is the OccurrenceStatus uncetain in terms of taxonomy (i.e. is the taxon possibly misidentified)? 
	 * @return
	 */
	public boolean isUncertainOccurrenceStatus() {
		return this.uncertainOccurrenceStatus==null ? false : this.uncertainOccurrenceStatus;
	}
	
	@Override
	public RelTypes getType() {
		return RelTypes.EXISTS_IN;
	}

}
