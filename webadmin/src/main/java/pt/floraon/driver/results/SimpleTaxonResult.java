package pt.floraon.driver.results;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.csv.CSVPrinter;

import pt.floraon.taxonomy.entities.TaxEnt;

/**
 * Represents the "taxonomic result" of a query, i.e. the simplified and aggregated information gathered from all taxa that
 * matched the query.<br/>Fields:
 * <ul>
 * <li>_id: the key of the matched species (or inferior rank)</li>
 * <li>name: the name of the matched species (or inferior rank)</li>
 * <li>match[]: the array of matching starting nodes that lead to this match</li>
 * <li>reltypes[]: the distinct types of relationships that were traversed from "match" to "name"</li>
 * <li>isLeaf: whether this entry is a isLeaf node or not</li>
 * <li>count: the number of occurrences of this species (if applicable)</li>
 * <li>notcurrentpath: if true, this result is not reachable under current taxonomy</li>
 * </ul>
 * @author Miguel Porto
 *
 */
public class SimpleTaxonResult extends SimpleTaxEntResult implements ResultItem {
	protected TaxEnt[] match;
	protected String[] reltypes;
	protected Integer count;	// number of occurrences
	protected Boolean partim=false;
	protected Boolean notcurrentpath=false;
	
	public String[] getReltypes() {
		return this.reltypes;
	}
	
	public boolean getPartim() {
		return this.partim;
	}
	
	public boolean getNotCurrentPath() {
		return this.notcurrentpath;
	}
	
	/**
	 * Check whether the path leading to this result is better than the path leading to str. This may involve several conditions, see code. 
	 * @param str SimpleTaxonResult to compare
	 * @return
	 */
	public boolean hasPriorityOver(SimpleTaxonResult str) {
		if(this.notcurrentpath && !str.notcurrentpath)	// current path always has priority, no matter how long it is
			return false;
		if(this.partim && !str.partim)					// full matches have priority
			return false;
		if(this.reltypes.length > str.reltypes.length)	// shorter paths have priority
			return false;
		/* TODO: is this needed?
		// now, if the match is of entities with the same name, choose the current
		if(this.match[0].getName().equals(str.match[0].getName()))
			if(!this.match[0].getCurrent() && str.match[0].getCurrent())
				return false;
		*/
		return true;
	}

	@Override
	@Deprecated
	public void toCSVLine(CSVPrinter rec, Object obj) throws IOException {
		// TODO obsolete!
		rec.print(this.count);
		rec.print(this.taxent.getID());
		rec.print(Arrays.toString(this.reltypes));
		rec.print((this.isLeaf ==null ? "" : (this.isLeaf ? "" : "+"))+(this.taxent.getCurrent() ? "" : "-")+this.taxent.getNameWithAnnotationOnly(false)+(this.partim ? " (partim)" : ""));
		rec.print(Arrays.toString(this.match));
	}

	@Override
	public String toHTMLTableRow(Object obj) {
		StringBuilder sb=new StringBuilder();
		StringBuilder sb1=new StringBuilder();
		for(TaxEnt s:this.match) sb1.append(s.getID()+", ");
		
		sb.append("<tr")
			.append(this.taxent.getCurrent() ? "><td>" : " class=\"notcurrent\"><td>")
			.append(this.count==null ? "" : this.count)
			.append((this.match[0].getCurrent() == null || this.match[0].getCurrent()) ? "":"non-current match ")	// FIXME handle the array!
			.append(this.notcurrentpath ? "not reachable ":"")
			.append(this.partim ? "included in ":"")
			.append("</td><td>")
			.append(this.taxent.getID())
			.append("</td><td>")
			.append(this.isLeaf ==null ? "" : (this.isLeaf ? "" : "+"))
			.append(this.taxent.getNameWithAnnotationOnly(true))
			//.append(this.partim ? " <i>partim</i>" : "")
			.append("</td><td>")
			.append(Arrays.toString(this.reltypes))
			.append("</td></tr>");
		return sb.toString();
	}

	@Override
	public String getHTMLTableHeader(Object obj) {
		return "<tr><th>Type of match</th><th>ID</th><th>Canonical name</th><th>Traversed relationship types</th></tr>";
	}

	@Override
	public String[] toStringArray() {	// TODO: should be the same fields as above
		StringBuilder sb=new StringBuilder();
		for(TaxEnt s:this.match) sb.append(s.getID()+", ");
		return new String[] {
			//this.count==null ? null : this.count.toString()
			(this.notcurrentpath ? "not reachable ":"")+(this.partim ? "included in ":"")
			,this.taxent.getID()
			,Arrays.toString(this.reltypes)
			,(this.isLeaf ==null ? "" : (this.isLeaf ? "" : "+"))+(this.taxent.getCurrent() ? "" : "-")+this.taxent.getNameWithAnnotationOnly(false)+(this.partim ? " (partim)" : "")
			,sb.toString()
		};
	}
	
	/**
	 * Intersects two result lists (keeping only common elements) but unions the matches and reltypes of each element
	 * @param l1
	 * @param l2
	 * @return
	 */
	public static List<SimpleTaxonResult> mergeSimpleTaxonResultLists(List<SimpleTaxonResult> l1,List<SimpleTaxonResult> l2) {
		if(l1==null) return l2;
		if(l2==null) return l1;
		l1.retainAll(l2);
		Set<TaxEnt> s1=new HashSet<TaxEnt>();
		Set<String> s2=new HashSet<String>();
		for(SimpleTaxonResult str:l1) {
			int io=l2.indexOf(str);
			s1.addAll(Arrays.asList(str.match));
			s1.addAll(Arrays.asList(l2.get(io).match));
			//Collections.sort(s1);
			str.match=s1.toArray(str.match);
			s1.clear();
			
			s2.addAll(Arrays.asList(str.reltypes));
			s2.addAll(Arrays.asList(l2.get(io).reltypes));
			str.reltypes=s2.toArray(str.reltypes);
			s2.clear();
		}
		return l1;
	}
}
