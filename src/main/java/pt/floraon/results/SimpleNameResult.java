package pt.floraon.results;

import java.io.IOException;

import org.apache.commons.csv.CSVPrinter;

import com.google.gson.internal.LinkedTreeMap;

public class SimpleNameResult implements ResultItem {
	protected String name;		// taxon canonical name
	protected String _id,author;
	protected Boolean leaf=null;
	protected Boolean current=null;
	protected LinkedTreeMap<String,String> territories;

	@Override
	public void toCSVLine(CSVPrinter rec) throws IOException {
		rec.print(this._id);
		rec.print((this.leaf==null ? "" : (this.leaf ? "" : "+"))+this.name);
		rec.print(this.author);
	}

	@Override
	public String toHTMLTableRow(Object obj) {
		return "<tr"+(this.current==null ? "" : (this.current ? "" : " class=\"notcurrent\""))+"><td data-key=\""+this._id+"\"><i>"+(this.leaf==null ? "" : (this.leaf ? "" : "+"))+this.name+"</i></td><td>"+this.author+"</td></tr>";
	}

	@Override
	public String toHTMLListItem() {
		return "<li"+(this.current==null ? "" : (this.current ? "" : " class=\"notcurrent\""))+" data-key=\""+this._id+"\"><i>"+this.name+"</i>"+(this.author == null ? "" : " "+this.author)+"</li>";
	}

	@Override
	public String[] toStringArray() {
		return new String[] {
				this._id
				,(this.leaf==null ? "" : (this.leaf ? "" : "+"))+this.name
				,this.author
			};
	}

	@Override
	public boolean equals(Object o) {
		return this._id.equals(((SimpleNameResult)o)._id);
	}
}
