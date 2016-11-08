package pt.floraon.entities;

import com.google.gson.JsonObject;

import pt.floraon.driver.Constants.NativeStatus;
import pt.floraon.driver.Constants.PhenologicalStates;
import pt.floraon.driver.Constants.RelTypes;
/**
 * Represents an occurrence of a taxon in a species list
 * @author miguel
 *
 */
public class OBSERVED_IN extends GeneralDBEdge {
	private Short confidence,validated,nativeStatus,phenoState;
	private String uuid,publicComment,privateComment,dateInserted;
	private Integer weight=100000;

	public OBSERVED_IN() {
		super();
	}

	public OBSERVED_IN(String from, String to) {
		super(from, to);
	}

	public OBSERVED_IN(Short confidence,Short validated,PhenologicalStates phe,String uuid,Integer weight,String pubnotes,String privnotes,NativeStatus nstate,String dateInserted) {
		this();
		this.confidence=confidence;
		this.validated=validated;
		this.phenoState=phe.getCode();
		this.uuid=uuid;
		this.weight=weight;
		this.publicComment=pubnotes;
		this.privateComment=privnotes;
		this.dateInserted=dateInserted;
		this.nativeStatus=nstate.getCode();
	}
	
	public OBSERVED_IN(Short confidence,Short validated,PhenologicalStates phe,String uuid,Integer weight,String pubnotes,String privnotes,NativeStatus nstate,String dateInserted,String from,String to) {
		this();
		this.confidence=confidence;
		this.validated=validated;
		this.phenoState=phe.getCode();
		this.uuid=uuid;
		this.weight=weight;
		this.publicComment=pubnotes;
		this.privateComment=privnotes;
		this.dateInserted=dateInserted;
		this.nativeStatus=nstate.getCode();
		this._from=from;
		this._to=to;
	}
	
	public String getUUID() {
		return this.uuid;
	}
	public String getPublicComment() {
		return this.publicComment;
	}
	public String getPrivateComment() {
		return this.privateComment;
	}
	public String getDateInserted() {
		return this.dateInserted;
	}
	public Short getConfidence() {
		return this.confidence;
	}
	public Short getValidated() {
		return this.validated;
	}
	public NativeStatus getNativeStatus() {
		return NativeStatus.fromCode(this.nativeStatus);
	}
	public PhenologicalStates getPhenoState() {
		return PhenologicalStates.getStateFromCode(this.phenoState);
	}
	public Integer getWeight() {
		return this.weight;
	}

	@Override
	public String getTypeAsString() {
		return this.getType().toString();
	}

	@Override
	public RelTypes getType() {
		return RelTypes.OBSERVED_IN;
	}

	@Override
	public JsonObject toJson() {
		return super._toJson();
	}

	@Override
	public String toJsonString() {
		return this.toJson().toString();
	}

}
