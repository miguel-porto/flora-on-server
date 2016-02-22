package pt.floraon.results;

import java.io.IOException;

import org.apache.commons.csv.CSVPrinter;

import pt.floraon.entities.TaxEnt;

public class SimpleNameResult implements ResultItem,Comparable<SimpleNameResult> {
	protected TaxEnt taxent;
	/*protected String name;		// taxon canonical name
	protected String _id,author;
	protected Boolean current=null;*/
	protected Boolean leaf=null;
	//protected LinkedTreeMap<String,String> territories;

	public String getTaxonId() {
		return this.taxent.getID();
	}
	
	@Override
	public void toCSVLine(CSVPrinter rec, Object obj) throws IOException {
		rec.print(this.taxent.getID());
		rec.print((this.leaf==null ? "" : (this.leaf ? "" : "+"))+this.taxent.getName());
		rec.print(this.taxent.getAuthor());
	}

	@Override
	public String toHTMLTableRow(Object obj) {
		return "<tr"+(this.taxent.getCurrent()==null ? "" : (this.taxent.getCurrent() ? "" : " class=\"notcurrent\""))+"><td data-key=\""+this.taxent.getID()+"\"><i>"+(this.leaf==null ? "" : (this.leaf ? "" : "+"))+this.taxent.getName()+"</i></td><td>"+this.taxent.getAuthor()+"</td></tr>";
	}

	@Override
	public String toHTMLListItem() {
		return "<li"+(this.taxent.getCurrent()==null ? "" : (this.taxent.getCurrent() ? "" : " class=\"notcurrent\""))+" data-key=\""+this.taxent.getID()+"\"><i>"+this.taxent.getName()+"</i>"+(this.taxent.getAuthor() == null ? "" : " "+this.taxent.getAuthor())+"</li>";
	}

	@Override
	public String[] toStringArray() {
		return new String[] {
				this.taxent.getID()
				,(this.leaf==null ? "" : (this.leaf ? "" : "+"))+this.taxent.getName()
				,this.taxent.getAuthor()
			};
	}

	@Override
	public boolean equals(Object o) {
		return this.taxent.getID().equals(((SimpleNameResult)o).taxent.getID());
	}

	@Override
	public void getCSVHeader(CSVPrinter rec, Object obj) throws IOException {
		rec.print("id");
		rec.print("canonicalName");
		rec.print("authority");
	}

	@Override
	public int compareTo(SimpleNameResult arg0) {
		return this.taxent.getName().compareTo(arg0.taxent.getName());
	}
}
