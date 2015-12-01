package pt.floraon.results;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.csv.CSVPrinter;

/**
 * Represents the "taxonomic result" of a query, i.e. the simplified and aggregated information gathered from all taxa that
 * matched the query.<br/>Fields:
 * <ul>
 * <li>_key: the key of the matched species (or inferior rank)</li>
 * <li>name: the name of the matched species (or inferior rank)</li>
 * <li>match[]: the array of matching starting nodes that lead to this match</li>
 * <li>reltypes[]: the distinct types of relationships that were traversed from "match" to "name"</li>
 * <li>leaf: whether this entry is a leaf node or not</li>
 * <li>count: the number of occurrences of this species (if applicable)</li>
 * </ul>
 * @author Miguel Porto
 *
 */
public class SimpleTaxonResult implements ResultItem {
	protected String name;		// taxon canonical name
	protected String _key;
	protected String[] match;
	protected String[] reltypes;
	protected Boolean leaf=null;
	protected Integer count;	// number of occurrences

	@Override
	public void toCSVLine(CSVPrinter rec) throws IOException {
		rec.print(this.count);
		rec.print(this._key);
		rec.print(Arrays.toString(this.reltypes));
		rec.print(this._key);
		rec.print((this.leaf==null ? "" : (this.leaf ? "" : "+"))+this.name);
		rec.print(Arrays.toString(this.match));
	}

	@Override
	public String toHTMLLine() {
		return "<tr><td>"+this.count+"</td><td>"+this._key+"</td><td>"+(this.leaf==null ? "" : (this.leaf ? "" : "+"))+this.name+"</td><td>"+Arrays.toString(this.reltypes)+"</td></tr>";
	}

	@Override
	public String[] toStringArray() {
		return new String[] {
			this.count==null ? null : this.count.toString()
			,this._key
			,Arrays.toString(this.reltypes)
			,(this.leaf==null ? "" : (this.leaf ? "" : "+"))+this.name
			,Arrays.toString(this.match)
		};
	}
	
	@Override
	public boolean equals(Object o) {
		return this._key.equals(((SimpleTaxonResult)o)._key);
	}
	
	public String getId() {
		return this._key;
	}

	/**
	 * Intersects two result lists (keeping only common elements) but unions the matches and reltypes of each element
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
			
			s1.addAll(Arrays.asList(str.reltypes));
			s1.addAll(Arrays.asList(l2.get(io).reltypes));
			str.reltypes=s1.toArray(str.reltypes);
			s1.clear();
		}
		return l1;
	}
}
