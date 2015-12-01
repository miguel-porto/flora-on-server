package pt.floraon.entities;

import pt.floraon.server.TaxonomyException;
import pt.floraon.server.Constants.TaxonRanks;

public class TaxEntName {
	public String name=null,author=null,comment=null,genus=null;
	public TaxonRanks rank=null;
	public Long id=null;
	private Boolean isNull=false;
	
	public TaxEntName() {
	}

	public TaxEntName(String name,String author,TaxonRanks rank) {
		if(name==null && author==null) this.isNull=true;
		this.name=name;
		this.author=author;
		this.rank=rank;
	}
	
	/**
	 * Basic parsing of taxon names from uploaded tables.
	 * Converts plain text to TaxEntName class.
	 * @param name
	 * @return
	 * @throws TaxonomyException 
	 */
	public TaxEntName(String name) throws TaxonomyException {
		// TODO process annotations (in [square brackets])
		name=name.replaceAll(" +", " ").trim();
		if(name.equals("")) {
			this.isNull=true;
			return;
		}
		// extract the authority between braces (I don't use regex cause it's too simple)
		int a=name.indexOf('{');
		int b=name.indexOf('}');
		if(a>-1 && b>-1) {
			if(b>a+1) {
				this.author=name.substring(a+1, b-0).trim();
				this.name=name.substring(0,a).trim();
			} else {
				this.name=name.substring(0,a).trim();
			}
		} else {
			this.name=name;
		}
	}

	public Boolean isNull() {
		return this.isNull;
	}
	public TaxEntName(GeneralDBNode n) {
		// TODO: create name from node
/*		this.name=n.getProperty("name").toString();
		this.rank=TaxonRanks.valueOf(n.getProperty("rank").toString().toUpperCase());
		TaxNode tn=new TaxNode(n);
		if(n.hasProperty("author")) this.author=n.getProperty("author").toString();
		if(tn.isSpeciesOrInferior())
			try {
				this.genus=new SpeciesNode(tn).getGenus();
			} catch (TaxonomyException e) {
				this.genus="<ERROR: "+e.getMessage()+">";
			}

		if(n.getDegree(TaxonomyRelTypes.HYBRID_OF, Direction.OUTGOING)>0) {
			// it's an hybrid! get all its parents (it's recursive)
			List<String> parents=new ArrayList<String>();
			for(Relationship r:n.getRelationships(TaxonomyRelTypes.HYBRID_OF, Direction.OUTGOING)) {
				parents.add(new TaxEntName(r.getEndNode()).toString(false));
			}
			StringBuilder sb=new StringBuilder();
			for(int i=0;i<parents.size();i++)
				sb.append(parents.get(i)+(i<parents.size()-1 ? " x " : ""));
			this.comment=sb.toString();
		} else
			if(n.hasProperty("comment")) this.comment=n.getProperty("comment").toString();
		this.id=n.getId();*/
	}
	
	public TaxEntName(String name,String rank,String author) {
		this.name=name;
		this.rank=TaxonRanks.valueOf(rank.toUpperCase());
		this.author=author;
	}

	public String toString() {
		return this.name+(this.author!=null ? " "+this.author : "")+(this.comment!=null ? " ["+this.comment+"]" : "");
	}

	public String toString(boolean authorship) {
		if(authorship) return this.toString();
		return this.name+(this.comment!=null ? " ["+this.comment+"]" : "");
	}
}