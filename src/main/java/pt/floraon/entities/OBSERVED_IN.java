package pt.floraon.entities;

import pt.floraon.server.Constants.NativeStatus;
import pt.floraon.server.Constants.PhenologicalStates;
/**
 * Represents an occurrence of a taxon in a species list
 * @author miguel
 *
 */
public class OBSERVED_IN extends GeneralDBEdge {
	protected Short confidence,validated,nativeStatus,phenoState;
	protected String uuid,comment,dateInserted;
	protected Integer weight=100000;
	
	public OBSERVED_IN(Short unc,Short validated,PhenologicalStates phe,String uuid,Integer weight,String pubnotes,NativeStatus nstate,String dateInserted) {
		this.confidence=unc;
		this.validated=validated;
		this.phenoState=phe.getCode();
		this.uuid=uuid;
		this.weight=weight;
		this.comment=pubnotes;
		this.dateInserted=dateInserted;
		this.nativeStatus=nstate.getCode();
	}
	
	public OBSERVED_IN(Short unc,Short validated,PhenologicalStates phe,String uuid,Integer weight,String pubnotes,NativeStatus nstate,String dateInserted,String from,String to) {
		this.confidence=unc;
		this.validated=validated;
		this.phenoState=phe.getCode();
		this.uuid=uuid;
		this.weight=weight;
		this.comment=pubnotes;
		this.dateInserted=dateInserted;
		this.nativeStatus=nstate.getCode();
		this._from=from;
		this._to=to;
	}
}
