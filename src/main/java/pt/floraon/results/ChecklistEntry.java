package pt.floraon.results;

/**
 * Represents an output line in the downloaded checklist
 * @author miguel
 *
 */
public class ChecklistEntry implements ResultItem, Comparable<ChecklistEntry> {
	public String taxon=null,canonicalName=null,genus=null,family=null,order=null;

	@Override
	public int compareTo(ChecklistEntry arg0) {
		int co=this.order.compareTo(arg0.order);
		if(co==0) {
			int cf=this.family.compareTo(arg0.family);
			if(cf==0) {
				return this.canonicalName.compareTo(arg0.canonicalName);
			} else return cf;
		} else return co;
	}
	@Override
	public String toCSVLine() {
		return this.taxon+"\t"+this.canonicalName+"\t"+this.genus+"\t"+this.family+"\t"+this.order;
	}

	@Override
	public String toHTMLLine() {
		return "<tr><td>"+this.taxon+"</td><td>"+this.canonicalName+"</td><td>"+this.genus+"</td><td>"+this.family+"</td><td>"+this.order+"</td></tr>";
	}
}
