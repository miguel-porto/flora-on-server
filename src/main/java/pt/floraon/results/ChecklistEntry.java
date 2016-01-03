package pt.floraon.results;

import java.io.IOException;

import org.apache.commons.csv.CSVPrinter;

/**
 * Represents an output line in the downloaded checklist
 * @author miguel
 *
 */
public class ChecklistEntry implements ResultItem, Comparable<ChecklistEntry> {
	public String taxon=null,canonicalName=null,genus=null,family=null,order=null;

	@Override
	public int compareTo(ChecklistEntry arg0) {
		int co,cf;
		if(this.order!=null && arg0.order!=null) co=this.order.compareTo(arg0.order); else co=0;
		if(co==0) {
			if(this.family!=null && arg0.family!=null) cf=this.family.compareTo(arg0.family); else cf=0;
			if(cf==0) {
				if(this.canonicalName!=null && arg0.canonicalName!=null)
					return this.canonicalName.compareTo(arg0.canonicalName);
				else return 0;
			} else return cf;
		} else return co;
	}
	@Override
	public void toCSVLine(CSVPrinter rec) throws IOException {
		rec.print(this.taxon);
		rec.print(this.canonicalName);
		rec.print(this.genus);
		rec.print(this.family);
		rec.print(this.order);
	}

	@Override
	public String toHTMLTableRow(Object obj) {
		return "<tr><td>"+this.taxon+"</td><td>"+this.canonicalName+"</td><td>"+this.genus+"</td><td>"+this.family+"</td><td>"+this.order+"</td></tr>";
	}

	@Override
	public String toHTMLListItem() {
		return "<li>"+this.taxon+"</li>";
	}
	
	@Override
	public String[] toStringArray() {
		// TODO Auto-generated method stub
		return null;
	}
}
