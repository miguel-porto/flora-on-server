package pt.floraon.entities;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Represents the result of a taxon query, i.e. the simplified and aggregated information gathered from the occurrences upon a query.
 * @author miguel
 *
 */
public class SimpleTaxonResult implements ResultItem {
	protected String name;		// taxon canonical name
	protected String _key;
	protected String[] match;
	protected Boolean leaf=null;
	protected Integer count;	// number of occurrences

	@Override
	public String toCSVLine() {
		return this.count+"\t"+this._key+"\t"+(this.leaf==null ? "" : (this.leaf ? "" : "+"))+this.name+"\t\tMatches: "+Arrays.toString(this.match);
	}

	@Override
	public String toHTMLLine() {
		return "<tr><td>"+this.count+"</td><td>"+this._key+"</td><td>"+(this.leaf==null ? "" : (this.leaf ? "" : "+"))+this.name+"</td><td>"+Arrays.toString(this.match)+"</td></tr>";
	}

	@Override
	public boolean equals(Object o) {
		return this._key.equals(((SimpleTaxonResult)o)._key);
	}
	
	public String getId() {
		return this._key;
	}

	/**
	 * Intersects two result lists (keeping only common elements) but unions the matches of each element
	 * @param l1
	 * @param l2
	 * @return
	 */
	public static List<SimpleTaxonResult> mergeResultLists(List<SimpleTaxonResult> l1,List<SimpleTaxonResult> l2) {
		if(l1==null) return l2;
		if(l2==null) return l1;
		l1.retainAll(l2);
		Set<String> s1=new HashSet<String>();
		for(SimpleTaxonResult str:l1) {
			int io=l2.indexOf(str);
			s1.addAll(Arrays.asList(str.match));
			s1.addAll(Arrays.asList(l2.get(io).match));
			str.match=s1.toArray(str.match);
			s1.clear();
		}
		return l1;
	}
}
