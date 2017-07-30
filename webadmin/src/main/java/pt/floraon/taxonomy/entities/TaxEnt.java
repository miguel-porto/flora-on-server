package pt.floraon.taxonomy.entities;

import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.csv.CSVPrinter;

import com.google.gson.JsonObject;
import com.google.gson.internal.LinkedTreeMap;

import pt.floraon.driver.FloraOnException;
import pt.floraon.driver.Messages;
import pt.floraon.driver.TaxonomyException;
import pt.floraon.driver.Constants.NodeTypes;
import pt.floraon.driver.Constants.TaxonRanks;
import pt.floraon.driver.Constants.WorldNativeDistributionCompleteness;
import pt.floraon.driver.DatabaseException;
import pt.floraon.driver.entities.NamedDBNode;
import pt.floraon.driver.results.ResultItem;

/**
 * Represents a taxonomic entity.
 * TODO: convert to java bean
 * @author miguel
 *
 */
public class TaxEnt extends NamedDBNode implements ResultItem, Serializable {
	/**
	 * Matches a taxon name of the form:
	 * Genus species rank infrataxa Author [annotation] sensu somework
	 */
	private transient static Pattern taxonName = Pattern.compile("^ *(?<subrank>subgen.? +)?(?<genus>[a-zçA-Z]+)(?: +(?<species>[a-zç-]+))?" +
			"((?: +\\{?(?<author1> *[A-ZÁÉÍÓÚ(][^\\[\\]{}]+?)?}?)?" +
			"(?<infras>(?: +((subsp)|(ssp)|(var)|(f)|(forma))\\.? +[a-z-]+)+))?" +
			"(?: +\\{?(?<author> *[A-ZÁÉÍÓÚ(][^\\[\\]{}]+?)?}?)?(?:(?:(?: +\\[(?<annot1>[\\w çãõáàâéêíóôú]+)])?" +
			"(?: +sensu +(?<sensu1>[^\\[\\]]+))?)|(?:(?: +sensu +(?<sensu2>[^\\[\\]]+))?(?: +\\[(?<annot2>[\\w çãõáàâéêíóôú]+)])?)) *$");

	protected Integer rank;
	/**
	 * Some character that distinguishes these populations from the parent taxon
	 */
	protected String annotation;
	/**
	 * The reference of which the interpretation of this name follows
	 */
	protected String sensu;		// TODO: this should come from a reference list
	protected String author;
	protected String in;
	/**
	 * Whether this taxon is included in the checklist or not
	 */
	protected Boolean current;
	protected Integer gbifKey;
	protected Boolean isSpeciesOrInf;
	protected Integer oldId;
	/**
	 * Comments about what this taxon is, e.g. this taxon
	 */
	protected String comment;
	/**
	 * This tells whether the world <b>native</b> distribution is complete, regardless of incompleteness in the exotic distribution.
	 */
	protected WorldNativeDistributionCompleteness worldDistributionCompleteness = WorldNativeDistributionCompleteness.NOT_KNOWN;
	/**
	 * These are the IDs of the largest territories for which the distribution of the taxon is complete (native and exotic)
	 */
	protected String[] territoriesWithCompleteDistribution;
	/**
	 * Is it a isLeaf node? This field is computed from the graph when a TaxEnt is returned.
	 */
	protected transient Boolean isLeaf;
	/**
	 * Just a temporary storage for the parsed name
	 */
	private transient CanonicalName canonicalName;

	public TaxEnt() {
		super();
	}

	public TaxEnt(TaxEnt te) throws DatabaseException {
		super(te);
		this.rank=te.rank;
		this.isSpeciesOrInf=this.rank==null ? null : this.rank>=TaxonRanks.SPECIES.getValue();
		this.annotation=te.annotation;
		this.author=te.author;
		this.current=te.current;
		this.gbifKey=te.gbifKey;
		this.oldId=te.oldId;
	}

	public TaxEnt(String name, Integer rank, String author, String sensu, String annotation, Boolean current, Integer gbifKey, WorldNativeDistributionCompleteness worldDistributionCompleteness, Integer oldId) throws DatabaseException {
		super(name);
		this.rank = rank;
		this.isSpeciesOrInf = this.rank==null ? null : this.rank>=TaxonRanks.SPECIES.getValue();
		
		/*if(author!=null && author.trim().length()==0) this.author=null;
		else if(author!=null) this.author = author.trim();*/
		this.author = author != null ? author.trim() : null;

		/*if(annotation!=null && annotation.trim().length()==0) this.annotation=null;
		else if(annotation!=null) this.annotation=annotation.trim();*/
		this.annotation = annotation != null ? annotation.trim() : null;

		/*if(sensu!=null && sensu.trim().length()==0) this.sensu=null;
		else if(sensu!=null) this.sensu=sensu.trim();*/
		this.sensu = sensu != null ? sensu.trim() : null;

		this.current = current;
		this.gbifKey = gbifKey;
		this.oldId = oldId;
		this.worldDistributionCompleteness = worldDistributionCompleteness;
	}

	public TaxEnt(String name,Integer rank,String author,String annotation) throws DatabaseException {
		this(name, rank, author, null, annotation, null, null, null, null);
	}

	/**
	 * Gets the parsed canonical name
	 * @return
	 */
	public CanonicalName getCanonicalName() {
		return this.canonicalName == null ? (this.canonicalName = new CanonicalName(this.getName())) : this.canonicalName;
	}

	/**
	 * Creates a TaxEnt from a compound string of the form
	 * Genus species rank infrataxa Author [annotation] sensu somework
	 * @param name
	 * @return
	 * @throws TaxonomyException
	 */
	public static TaxEnt parse(String name) throws FloraOnException {
		if(name == null) throw new DatabaseException(Messages.getString("error.3"));
		name = name.replaceAll(" +", " ").trim();
		if(name.equals(""))
			throw new DatabaseException(Messages.getString("error.3"));

		Matcher m = taxonName.matcher(name);
		TaxEnt out = new TaxEnt();

		if(m.find()) {
			StringBuilder sb = new StringBuilder(m.group("genus"));
			if (m.group("species") != null) sb.append(" ").append(m.group("species"));
			String r = m.group("infras");
			if (r != null) {
				sb.append(" ").append(r);
			}
			// this strips out any other words than the name itself
			CanonicalName cn = new CanonicalName(sb.toString());
/*
			String r = m.group("rank");
			if(r != null) {
				r = r + (r.endsWith(".") ? "" : ".");
				sb.append(" ").append(r);
			}
			if(m.group("infra") != null) sb.append(" ").append(m.group("infra"));
*/
			out.setName(cn.toString());

			TaxonRanks tr;
			if(m.group("subrank") != null) {
				tr = TaxonRanks.getRankFromShortname(m.group("subrank").trim());
			} else {
				tr = cn.getTaxonRank();
			}
			if (tr != null)	out.setRank(tr.getValue());
			if (m.group("author") != null && m.group("author1") == null)
				out.setAuthor(m.group("author"));
			else if (m.group("author") == null && m.group("author1") != null)
				out.setAuthor(m.group("author1"));
			else if (m.group("author") != null && m.group("author1") != null)
				out.setAuthor(m.group("author"));
			out.setAnnotation((m.group("annot1") == null ? m.group("annot2") : m.group("annot1")));
			out.setSensu((m.group("sensu1") == null ? m.group("sensu2") : m.group("sensu1")));
		} else throw new FloraOnException(Messages.getString("error.2", name));
		return out;
/*
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
		return new TaxEnt(name1, null, author, null);*/
	}

	/**
	 * Constructor for a JSON document
	 * @param doc JSON document, as returned by Arango driver
	 * @throws TaxonomyException 
	 */
	public TaxEnt(LinkedTreeMap<String,Object> doc) throws DatabaseException {
		super(doc.get("name"));

		this.name=doc.get("name").toString();
		this.rank=((Float)Float.parseFloat(doc.get("rank").toString())).intValue();
		this.isSpeciesOrInf=this.rank==null ? null : this.rank>=TaxonRanks.SPECIES.getValue();
		this.annotation=doc.get("annotation")==null ? null : doc.get("annotation").toString();
		this.author=doc.get("author")==null ? null : doc.get("author").toString();
		this.current=Boolean.parseBoolean(doc.get("current").toString());
		this.gbifKey=doc.get("gbifKey")==null ? null : Integer.parseInt(doc.get("gbifKey").toString());
	}

	public Boolean isSpecies() {
		return this.getRankValue().equals(TaxonRanks.SPECIES.getValue());
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
		if(getCurrent() && rank <= taxon.getRankValue()) throw new TaxonomyException("Rank must be lower than parent rank, unless it is set as not current");
		if(this.isSpeciesOrInferior()) {
			if(getCurrent() && getName().toLowerCase().indexOf(taxon.getName().toLowerCase()+" ") != 0) throw new TaxonomyException("Name must include all superior taxa up to genus");
			// TODO: more tests for name validity
		}
	}

	/**
	 * Gets the taxon name with authorship and annotations.
	 * @return
	 */
	public String getFullName(boolean htmlFormatted) {
		if(htmlFormatted && getRankValue() >= TaxonRanks.GENUS.getValue())	// genus or inferior
			return (this.getRankValue().equals(TaxonRanks.SUBGENUS.getValue()) ? "subgen. " : "")
				+ "<i>"+this.getName()+"</i>"+(this.getAuthor() != null ? " "+this.getAuthor() : "")
				+ (this.getSensu() != null ? " <i>sensu</i> "+this.getSensu() : "")
				+ (this.getAnnotation() != null ? " ["+this.getAnnotation()+"]" : "");
		else
			return this.getName() + (this.getAuthor() != null ? " "+this.getAuthor() : "")
				+ (this.getSensu() != null ? " sensu "+this.getSensu() : "")
				+ (this.getAnnotation() != null ? " ["+this.getAnnotation()+"]" : "");
	}

	public String getFullName() {
		return getFullName(false);
	}

	public WorldNativeDistributionCompleteness getWorldDistributionCompleteness() {
		return this.worldDistributionCompleteness == null ? WorldNativeDistributionCompleteness.NOT_KNOWN : this.worldDistributionCompleteness;
	}
	
	public String[] getTerritoriesWithCompleteDistribution() {
		return this.territoriesWithCompleteDistribution == null ? new String[0] : this.territoriesWithCompleteDistribution;
	}
	
	public TaxonRanks getRank() {
		return TaxonRanks.getRankFromValue(rank);
	}

	public String getNameWithAnnotationOnly(boolean htmlFormatted) {
		return (this.getRankValue().equals(TaxonRanks.SUBGENUS.getValue()) ? "subgen. " : "")
			+ this.getName()
			+ (this.getSensu() != null ? (htmlFormatted ? " <i>sensu</i> " : " sensu ") + this.getSensu() : "")
			+ (this.getAnnotation() != null ? " ["+this.getAnnotation()+"]" : "");
	}

	public String getURLEncodedName() throws UnsupportedEncodingException {
		return URLEncoder.encode(this.name, StandardCharsets.UTF_8.name());
	}
	
	public String getAuthor() {
		return "".equals(this.author) ? null : this.author;
	}
	
	public Integer getOldId() {
		return this.oldId;
	}

	public String getAnnotation() {
		return "".equals(this.annotation) ? null : this.annotation;
	}

	public String getSensu() {
		return "".equals(this.sensu) ? null : this.sensu;
	}

	public Integer getRankValue() {
		return rank == null ? TaxonRanks.NORANK.getValue() : rank;
	}
	
	public Boolean getCurrent() {
		return this.current == null ? false : this.current;
	}

	public String getComment() {
		return comment == null ? "" : comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public void setAnnotation(String annotation) {
		this.annotation = annotation;
	}

	public void setSensu(String sensu) {
		this.sensu = sensu;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public void setCurrent(Boolean current) {
		this.current = current;
	}

	public void setOldId(Integer oldId) {
		this.oldId = oldId;
	}

	public void setRank(Integer rank) {
		this.rank = rank;
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
	public String getTypeAsString() {
		return this.getType().toString();
	}
	
	@Override
	public NodeTypes getType() {
		return NodeTypes.taxent;
	}
	
	@Override
	public JsonObject toJson() {
		JsonObject out = super._toJson();
		return out;
	}

	@Override
	public String toJsonString() {
		return this.toJson().toString();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		TaxEnt taxEnt = (TaxEnt) o;

		return this.getID().equals(taxEnt.getID());
	}

	@Override
	public int hashCode() {
		return this.getID().hashCode();
	}
}
