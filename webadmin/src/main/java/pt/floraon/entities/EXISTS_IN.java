package pt.floraon.entities;

import pt.floraon.driver.Constants.AbundanceLevel;
import pt.floraon.driver.Constants.NativeStatus;
import pt.floraon.driver.Constants.OccurrenceStatus;
import pt.floraon.driver.Constants.PlantIntroducedStatus;
import pt.floraon.driver.Constants.PlantNaturalizationDegree;
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
	protected AbundanceLevel abundanceLevel;
	protected PlantIntroducedStatus introducedStatus;
	protected PlantNaturalizationDegree naturalizationDegree;
	/**
	 * If true, it is uncertain the identity of the taxon in this territory, but, shall the identification be correct, then it occurs with the reported NativeStatus and OccurrenceStatus.
	 */
	protected Boolean uncertainOccurrenceStatus;
	
	public EXISTS_IN(NativeStatus nativeStatus) {
		this.nativeStatus=nativeStatus;
		this.occurrenceStatus=OccurrenceStatus.PRESENT;
	}

	public EXISTS_IN(NativeStatus nativeStatus, OccurrenceStatus occurrenceStatus, AbundanceLevel abundanceLevel, PlantIntroducedStatus introducedStatus, PlantNaturalizationDegree naturalizationDegree) {
		this.nativeStatus = nativeStatus;
		this.occurrenceStatus = occurrenceStatus;
		this.abundanceLevel = abundanceLevel;
		if(introducedStatus != null && this.nativeStatus.getNativeOrExotic() != introducedStatus.getNativeOrExotic()) {
			this.introducedStatus = null;
			this.naturalizationDegree = null;
		} else {
			this.introducedStatus = introducedStatus;
			this.naturalizationDegree = naturalizationDegree;
		}
	}

	public EXISTS_IN(NativeStatus nativeStatus, OccurrenceStatus occurrenceStatus, AbundanceLevel abundanceLevel, PlantIntroducedStatus introducedStatus, PlantNaturalizationDegree naturalizationDegree, Boolean uncertain, String from, String to) {
		this(nativeStatus, occurrenceStatus, abundanceLevel, introducedStatus, naturalizationDegree);
		this.uncertainOccurrenceStatus=uncertain;
		this._from=from;
		this._to=to;
	}

	/*	GETTERS	*/
	public NativeStatus getNativeStatus() {
		return this.nativeStatus==null ? NativeStatus.ERROR : this.nativeStatus;
	}
	
	public AbundanceLevel getAbundanceLevel() {
		return this.abundanceLevel==null ? AbundanceLevel.NOT_SPECIFIED : this.abundanceLevel;
	}
	
	public OccurrenceStatus getOccurrenceStatus() {
		return this.occurrenceStatus == null ? OccurrenceStatus.PRESENT : this.occurrenceStatus;	// assume it is present, if no information is given, or if an error has occurred (value not found in enum)
	}
	
	public PlantIntroducedStatus getIntroducedStatus() {
		if(this.introducedStatus != null && this.nativeStatus.getNativeOrExotic() != this.introducedStatus.getNativeOrExotic()) return PlantIntroducedStatus.NOT_APPLICABLE;
		if(this.introducedStatus == null) return PlantIntroducedStatus.NOT_SPECIFIED;
		return this.introducedStatus;
	}
	
	public PlantNaturalizationDegree getNaturalizationDegree() {
		if(this.nativeStatus.isNative()) return PlantNaturalizationDegree.NOT_APPLICABLE;
		if(this.naturalizationDegree == null) return PlantNaturalizationDegree.NOT_SPECIFIED;
		return this.naturalizationDegree;
	}

	/*	SETTERS	*/
	public void setNativeStatus(NativeStatus ns) {
		this.nativeStatus = ns;
	}
	
	public void setOccurrenceStatus(OccurrenceStatus os) {
		this.occurrenceStatus = os;
	}

	public void setAbundanceLevel(AbundanceLevel al) {
		this.abundanceLevel = al;
	}

	public void setIntroducedStatus(PlantIntroducedStatus is) {
		this.introducedStatus = is;
	}

	public void setNaturalizationDegree(PlantNaturalizationDegree nd) {
		this.naturalizationDegree = nd;
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

	/**
	 * Merges this territory with the given one, i.e., adjusts the Status fields of this territory
	 * having in account the values of the two territories.
	 * @param o
	 */
	public void mergeWith(EXISTS_IN o, boolean propagateStatus) {
		if(propagateStatus) {
			this.setOccurrenceStatus(this.getOccurrenceStatus().merge(o.getOccurrenceStatus()));
			this.setAbundanceLevel(this.getAbundanceLevel().merge(o.getAbundanceLevel()));
			this.setIntroducedStatus(this.getIntroducedStatus().merge(o.getIntroducedStatus()));
			this.setNaturalizationDegree(this.getNaturalizationDegree().merge(o.getNaturalizationDegree()));
		} else {	// don't propagate status, set them to general defaults
			if(this.getOccurrenceStatus().isPresent())
				this.setOccurrenceStatus(OccurrenceStatus.PRESENT);
			this.setIntroducedStatus(PlantIntroducedStatus.NOT_SPECIFIED);
			this.setNaturalizationDegree(PlantNaturalizationDegree.NOT_SPECIFIED);
			this.setAbundanceLevel(AbundanceLevel.NOT_SPECIFIED);
		}
		
		if(this.getNativeStatus() != o.getNativeStatus()) {
			if(this.getNativeStatus().isNative() != o.getNativeStatus().isNative())
				this.setNativeStatus(NativeStatus.MULTIPLE_STATUS);
			else {
				if(this.getNativeStatus().isNative())
					this.setNativeStatus(NativeStatus.MULTIPLE_NATIVE_STATUS);
				else
					this.setNativeStatus(NativeStatus.MULTIPLE_EXOTIC_STATUS);
			}
		}
	}
}
