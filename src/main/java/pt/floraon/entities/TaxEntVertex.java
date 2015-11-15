package pt.floraon.entities;

import com.google.gson.internal.LinkedTreeMap;

import pt.floraon.server.Constants;
import pt.floraon.server.Constants.TaxonRanks;

/**
 * Represents a taxonomic entity in the DB.
 * @author miguel
 *
 */
public class TaxEntVertex extends GeneralDBNode {
	protected String name;
	protected Integer rank;
	protected String annotation;
	protected String author;
	protected Boolean current;
	protected Integer gbifKey;
	protected Boolean isSpeciesOrInf;
	protected Integer oldId;
	
	public TaxEntVertex(TaxEnt te) {
		super(te);
		this.name=te.name;
		this.rank=te.rank;
		this.isSpeciesOrInf=this.rank==null ? null : this.rank>=TaxonRanks.SPECIES.getValue();
		this.annotation=te.annotation;
		this.author=te.author;
		this.current=te.current;
		this.gbifKey=te.gbifKey;
		this.oldId=te.oldId;
	}

	public TaxEntVertex(TaxEntVertex te) {
		super(te);
		this.name=te.name;
		this.rank=te.rank;
		this.isSpeciesOrInf=this.rank==null ? null : this.rank>=TaxonRanks.SPECIES.getValue();
		this.annotation=te.annotation;
		this.author=te.author;
		this.current=te.current;
		this.gbifKey=te.gbifKey;
		this.oldId=te.oldId;
	}

	public TaxEntVertex(String name,Integer rank,String author,String annotation,Boolean current,Integer gbifKey) {
		this.name=name;
		this.rank=rank;
		this.isSpeciesOrInf=this.rank==null ? null : this.rank>=TaxonRanks.SPECIES.getValue();
		this.annotation=annotation;
		this.author=author;
		this.current=current;
		this.gbifKey=gbifKey;
	}
	
	public TaxEntVertex(String name,Integer rank,String author,String annotation) {
		this.name=name;
		this.rank=rank;
		this.isSpeciesOrInf=this.rank==null ? null : this.rank>=TaxonRanks.SPECIES.getValue();
		this.annotation=annotation;
		this.author=author;
		this.current=null;
		this.gbifKey=null;
	}
	/**
	 * Constructor for a JSON document
	 * @param doc JSON document, as returned by Arango driver
	 */
	public TaxEntVertex(LinkedTreeMap<String,Object> doc) {
		this.name=doc.get("name").toString();
		this.rank=((Float)Float.parseFloat(doc.get("rank").toString())).intValue();
		this.isSpeciesOrInf=this.rank==null ? null : this.rank>=TaxonRanks.SPECIES.getValue();
		this.annotation=doc.get("annotation")==null ? null : doc.get("annotation").toString();
		this.author=doc.get("author")==null ? null : doc.get("author").toString();
		this.current=Boolean.parseBoolean(doc.get("current").toString());
		this.gbifKey=doc.get("gbifKey")==null ? null : Integer.parseInt(doc.get("gbifKey").toString());
	}
}
