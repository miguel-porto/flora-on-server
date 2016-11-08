package pt.floraon.results;

import java.io.IOException;

import org.apache.commons.csv.CSVPrinter;

import pt.floraon.entities.TaxEnt;
/**
 * Represents a TaxEnt result without further info.
 * @author miguel
 *
 */
public class SimpleTaxEntResult implements ResultItem,Comparable<SimpleTaxEntResult> {
	protected TaxEnt taxent;
	protected Boolean isLeaf = null;

	public Boolean isLeaf() {
		return isLeaf;
	}

	public String getTaxonId() {
		return this.taxent.getID();
	}
	
	public TaxEnt getTaxent() {
		return this.taxent;
	}
	
	@Override
	public void toCSVLine(CSVPrinter rec, Object obj) throws IOException {
		rec.print(this.taxent.getID());
		rec.print((this.isLeaf ==null ? "" : (this.isLeaf ? "" : "+"))+this.taxent.getName());
		rec.print(this.taxent.getAuthor());
	}

	@Override
	public String toHTMLTableRow(Object obj) {
		return "<tr"+(this.taxent.getCurrent()==null ? "" : (this.taxent.getCurrent() ? "" : " class=\"notcurrent\""))+"><td data-key=\""+this.taxent.getID()+"\"><i>"+(this.isLeaf ==null ? "" : (this.isLeaf ? "" : "+"))+this.taxent.getName()+"</i></td><td>"+this.taxent.getAuthor()+"</td></tr>";
	}

	@Override
	public String toHTMLListItem() {
		return "<li"+(this.taxent.getCurrent()==null ? "" : (this.taxent.getCurrent() ? "" : " class=\"notcurrent\""))+" data-key=\""+this.taxent.getID()+"\"><i>"+this.taxent.getName()+"</i>"+(this.taxent.getAuthor() == null ? "" : " "+this.taxent.getAuthor())+"</li>";
	}

	@Override
	public String[] toStringArray() {
		return new String[] {
				this.taxent.getID()
				,(this.isLeaf ==null ? "" : (this.isLeaf ? "" : "+"))+this.taxent.getName()
				,this.taxent.getAuthor()
			};
	}

	@Override
	public boolean equals(Object o) {
		return this.taxent.getID().equals(((SimpleTaxEntResult)o).taxent.getID());
	}

	@Override
	public void getCSVHeader(CSVPrinter rec, Object obj) throws IOException {
		rec.print("id");
		rec.print("canonicalName");
		rec.print("authority");
	}

	@Override
	public int compareTo(SimpleTaxEntResult arg0) {
		return this.taxent.getName().compareTo(arg0.taxent.getName());
	}

	@Override
	public String getHTMLTableHeader(Object obj) {
		return "<tr><th>ID</th><th>Canonical name</th><th>Author</th></tr>";
	}
}
