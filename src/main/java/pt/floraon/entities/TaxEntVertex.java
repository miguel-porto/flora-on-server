package pt.floraon.entities;

import java.io.IOException;

import org.apache.commons.csv.CSVPrinter;

import com.google.gson.internal.LinkedTreeMap;

import pt.floraon.driver.TaxonomyException;
import pt.floraon.driver.Constants.TaxonRanks;
import pt.floraon.results.ResultItem;

/**
 * Represents a taxonomic entity in the DB.
 * @author miguel
 *
 */
public class TaxEntVertex extends GeneralDBNode implements ResultItem {
	protected String name;
	protected Integer rank;
	protected String annotation;
	protected String author;
	protected Boolean current;
	protected Integer gbifKey;
	protected Boolean isSpeciesOrInf;
	protected Integer oldId;
	
	public TaxEntVertex(TaxEnt te) {
		super(te.baseNode);
		this.name=te.baseNode.name;
		this.rank=te.baseNode.rank;
		this.isSpeciesOrInf=this.rank==null ? null : this.rank>=TaxonRanks.SPECIES.getValue();
		this.annotation=te.baseNode.annotation;
		this.author=te.baseNode.author;
		this.current=te.baseNode.current;
		this.gbifKey=te.baseNode.gbifKey;
		this.oldId=te.baseNode.oldId;
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

	public TaxEntVertex(String name,Integer rank,String author,String annotation,Boolean current,Integer gbifKey) throws TaxonomyException {
		if(annotation!=null && annotation.trim().length()==0) annotation=null; 
		if(author!=null && author.trim().length()==0) author=null;
		if(name==null || name.trim().length()==0) throw new TaxonomyException("Taxon must have a name");
		this.name=name;
		this.rank=rank;
		this.isSpeciesOrInf=this.rank==null ? null : this.rank>=TaxonRanks.SPECIES.getValue();
		this.annotation=annotation;
		this.author=author;
		this.current=current;
		this.gbifKey=gbifKey;
	}
	
	public TaxEntVertex(String name,Integer rank,String author,String annotation) throws TaxonomyException {
		if(annotation!=null && annotation.trim().length()==0) annotation=null; 
		if(author!=null && author.trim().length()==0) author=null;
		if(name==null || name.trim().length()==0) throw new TaxonomyException("Taxon must have a name");
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
	 * @throws TaxonomyException 
	 */
	public TaxEntVertex(LinkedTreeMap<String,Object> doc) throws TaxonomyException {
		if(doc.get("name")==null || doc.get("name").toString().trim().length()==0) throw new TaxonomyException("Taxon must have a name");

		this.name=doc.get("name").toString();
		this.rank=((Float)Float.parseFloat(doc.get("rank").toString())).intValue();
		this.isSpeciesOrInf=this.rank==null ? null : this.rank>=TaxonRanks.SPECIES.getValue();
		this.annotation=doc.get("annotation")==null ? null : doc.get("annotation").toString();
		this.author=doc.get("author")==null ? null : doc.get("author").toString();
		this.current=Boolean.parseBoolean(doc.get("current").toString());
		this.gbifKey=doc.get("gbifKey")==null ? null : Integer.parseInt(doc.get("gbifKey").toString());
	}

	public Boolean isCurrent() {
		return this.current;
	}
	/**
	 * Gets the taxon name with authorship and annotations.
	 * @return
	 */
	public String getFullName(boolean htmlFormatted) {
		if(htmlFormatted && rank>=TaxonRanks.GENUS.getValue())
			return "<i>"+name+"</i>"+(author!=null ? " "+this.author : "")+(annotation!=null ? " ["+this.annotation+"]" : "");
		else
			return name+(author!=null ? " "+this.author : "")+(annotation!=null ? " ["+this.annotation+"]" : "");
	}
	
	public String getFullName() {
		return getFullName(false);
	}

	public TaxonRanks getRank() {
		return TaxonRanks.getRankFromValue(rank);
	}

	/**
	 * Gets the taxon canonical name.
	 * @return
	 */
	public String getName() {
		return this.name;
	}
	
	public String getAuthor() {
		return author;
	}

	public String getAnnotation() {
		return annotation;
	}

	public Integer getRankValue() {
		return rank;
	}

	@Override
	public void toCSVLine(CSVPrinter rec) throws IOException {
		rec.print(this.getFullName());
	}

	@Override
	public String toHTMLTableRow() {
		return "<tr><td>"+this.getFullName(true)+"</td></tr>";
	}

	@Override
	public String[] toStringArray() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String toHTMLListItem() {
		StringBuilder sb=new StringBuilder();
		sb.append("<li class=\"")
			.append(this.getRank().toString())
			.append(this.current ? "" : " notcurrent")
			.append("\" data-key=\"")
			.append(this._id)
			.append("\">")
			.append(this.getFullName(true))
			.append("</li>");
		return sb.toString();
	}
}
