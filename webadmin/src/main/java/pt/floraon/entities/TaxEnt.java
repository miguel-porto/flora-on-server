package pt.floraon.entities;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.apache.commons.csv.CSVPrinter;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.internal.LinkedTreeMap;

import pt.floraon.driver.TaxonomyException;
import pt.floraon.driver.Constants.TaxonRanks;
import pt.floraon.driver.Constants.WorldDistributionCompleteness;
import pt.floraon.results.ResultItem;

/**
 * Represents a taxonomic entity in the DB.
 * @author miguel
 *
 */
public class TaxEnt extends GeneralDBNode implements ResultItem {
	protected String name;
	protected Integer rank;
	/**
	 * Some character that distinguishes these populations from the parent taxon
	 */
	protected String annotation;
	protected String sensu;		// TODO: this should come from a reference list
	protected String author;
	protected String in;
	protected Boolean current;
	protected Integer gbifKey;
	protected Boolean isSpeciesOrInf;
	protected Integer oldId;
	protected WorldDistributionCompleteness worldDistributionCompleteness;
	
	public TaxEnt(TaxEnt te) {
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

	/*public TaxEnt(String name,Integer rank,String author,String annotation,Boolean current,Integer gbifKey) throws TaxonomyException {
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
	}*/

	public TaxEnt(String name,Integer rank,String author,String sensu,String annotation,Boolean current,Integer gbifKey, WorldDistributionCompleteness worldDistributionCompleteness) throws TaxonomyException {
		if(name!=null && name.trim().length()==0) throw new TaxonomyException("Taxon must have a name");
		
		if(name!=null) this.name=name.trim();
		this.rank=rank;
		this.isSpeciesOrInf=this.rank==null ? null : this.rank>=TaxonRanks.SPECIES.getValue();
		
		if(author!=null && author.trim().length()==0) this.author=null;
		else if(author!=null) this.author=author.trim();

		if(annotation!=null && annotation.trim().length()==0) this.annotation=null;
		else if(annotation!=null) this.annotation=annotation.trim();

		if(sensu!=null && sensu.trim().length()==0) this.sensu=null;
		else if(sensu!=null) this.sensu=sensu.trim();

		this.current=current;
		this.gbifKey=gbifKey;
		this.worldDistributionCompleteness=worldDistributionCompleteness;
	}

	public TaxEnt(String name,Integer rank,String author,String annotation) throws TaxonomyException {
		this(name, rank, author, null, annotation, null, null, null);
	}

	/**
	 * Updates this TaxEntVertex. Pass null to leave as it is.
	 * TODO: empty string to remove
	 * @param name
	 * @param rank
	 * @param author
	 * @param annotation
	 * @param current
	 * @throws TaxonomyException
	 */
	public void update(String name,Integer rank,String author,String annotation,Boolean current) throws TaxonomyException {
		if(name!=null && name.trim().length()==0) throw new TaxonomyException("Taxon must have a name");
		
		if(name!=null) this.name=name;
		if(rank!=null) this.rank=rank;
		this.isSpeciesOrInf=this.rank==null ? null : this.rank>=TaxonRanks.SPECIES.getValue();
		
		if(author!=null && author.trim().length()==0) this.author=null;
		else if(author!=null) this.author=author;

		if(annotation!=null && annotation.trim().length()==0) this.annotation=null;
		else if(annotation!=null) this.annotation=annotation;

		if(current!=null) this.current=current;
	}

	/**
	 * Creates a TaxEntVertex from a compound string of the form name {authorship}
	 * @param name
	 * @return
	 * @throws TaxonomyException
	 */
	public static TaxEnt parse(String name) throws TaxonomyException {
		name=name.replaceAll(" +", " ").trim();
		if(name.equals("")) {
			throw new TaxonomyException("Taxon must have a name");
		}
		// extract the authority between braces (I don't use regex cause it's too simple)
		int a=name.indexOf('{');
		int b=name.indexOf('}');
		String author=null,name1;
		if(a>-1 && b>-1) {
			if(b>a+1) {
				author=name.substring(a+1, b-0).trim();
				name1=name.substring(0,a).trim();
			} else {
				name1=name.substring(0,a).trim();
			}
		} else name1=name;
		return new TaxEnt(name1, null, author, null);
	}

	/**
	 * Constructor for a JSON document
	 * @param doc JSON document, as returned by Arango driver
	 * @throws TaxonomyException 
	 */
	public TaxEnt(LinkedTreeMap<String,Object> doc) throws TaxonomyException {
		if(doc.get("name")==null || doc.get("name").toString().trim().length()==0) throw new TaxonomyException("Taxon must have a name");

		this.name=doc.get("name").toString();
		this.rank=((Float)Float.parseFloat(doc.get("rank").toString())).intValue();
		this.isSpeciesOrInf=this.rank==null ? null : this.rank>=TaxonRanks.SPECIES.getValue();
		this.annotation=doc.get("annotation")==null ? null : doc.get("annotation").toString();
		this.author=doc.get("author")==null ? null : doc.get("author").toString();
		this.current=Boolean.parseBoolean(doc.get("current").toString());
		this.gbifKey=doc.get("gbifKey")==null ? null : Integer.parseInt(doc.get("gbifKey").toString());
	}

	public Boolean isSpecies() {
		return this.getRankValue()==TaxonRanks.SPECIES.getValue();
	}
	
	public Boolean isSpeciesOrInferior() {
		return this.getRankValue()>=TaxonRanks.SPECIES.getValue();
	}

	/**
	 * Tests whether this taxon can be inserted as a child of given taxon, following the rules of nomenclature.
	 * @param taxon
	 * @return
	 * @throws TaxonomyException 
	 */
	public void canBeChildOf(TaxEnt taxon) throws TaxonomyException {
		if(taxon==null) return;
		if(current && rank <= taxon.getRankValue()) throw new TaxonomyException("Rank must be lower than parent rank, unless it is set as not current");
		if(this.isSpeciesOrInferior()) {
			if(current && getName().toLowerCase().indexOf(taxon.getName().toLowerCase()+" ") != 0) throw new TaxonomyException("Name must include all superior taxa up to genus");
			// TODO: more tests for name validity
		}
	}

	/**
	 * Gets the taxon name with authorship and annotations.
	 * @return
	 */
	public String getFullName(boolean htmlFormatted) {
		if(htmlFormatted && rank>=TaxonRanks.GENUS.getValue())
			return "<i>"+name+"</i>"+(author!=null ? " "+this.author : "")
				+ (sensu!=null ? " <i>sensu</i> "+sensu : "")
				+ (annotation!=null ? " ["+this.annotation+"]" : "");
		else
			return name+(author!=null ? " "+this.author : "")
				+ (sensu!=null ? " sensu "+sensu : "")
				+ (annotation!=null ? " ["+this.annotation+"]" : "");
	}
	
	public String getFullName() {
		return getFullName(false);
	}

	public WorldDistributionCompleteness getWorldDistributionCompleteness() {
		return this.worldDistributionCompleteness == null ? WorldDistributionCompleteness.NOT_KNOWN : this.worldDistributionCompleteness;
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

	public String getNameWithAnnotationOnly() {
		return this.name
			+ (sensu!=null ? " <i>sensu</i> "+this.sensu : "")
			+ (annotation!=null ? " ["+this.annotation+"]" : "");
	}

	public String getURLEncodedName() throws UnsupportedEncodingException {
		return URLEncoder.encode(this.name, StandardCharsets.UTF_8.name());
	}
	
	public String getAuthor() {
		return author;
	}
	
	public Integer getOldId() {
		return this.oldId;
	}

	public String getAnnotation() {
		return annotation;
	}

	public String getSensu() {
		return sensu;
	}

	public Integer getRankValue() {
		return rank;
	}
	
	public Boolean getCurrent() {
		return this.current;
	}
	
	@Override
	public void toCSVLine(CSVPrinter rec, Object obj) throws IOException {
		rec.print(this.getFullName());
	}

	@Override
	public String toHTMLTableRow(Object obj) {
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

	@Override
	public void getCSVHeader(CSVPrinter rec, Object obj) throws IOException {
		rec.print("scientificName");
	}

	@Override
	public String getHTMLTableHeader(Object obj) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public String toString() {
		StringBuilder sb=new StringBuilder();
		sb.append("Name: ")
			.append(name+"; ")
			.append("Rank: ").append(rank.toString()).append("; ")
			.append("Annot: ").append(annotation).append("; ")
			.append("Sensu: ").append(sensu).append("; ")
			.append("Author: ").append(author).append("; ")
			.append("in: ").append(in).append("; ")
			.append("current: ").append(current).append("; ")
			.append("isSpeciesOrInf: ").append(isSpeciesOrInf).append("; ")
			.append("oldId: ").append(oldId).append("; ")
			.append("worldDistributionCompleteness: ").append(worldDistributionCompleteness).append("; ");
		return sb.toString();
	}

	@Override
	public JsonElement toJson() {
		return new Gson().toJsonTree(this);
	}
}
