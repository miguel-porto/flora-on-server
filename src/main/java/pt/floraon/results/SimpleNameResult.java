package pt.floraon.results;

import java.io.IOException;

import org.apache.commons.csv.CSVPrinter;

public class SimpleNameResult implements ResultItem {
	protected String name;		// taxon canonical name
	protected String _key,author;
	protected Boolean leaf=null;

	@Override
	public void toCSVLine(CSVPrinter rec) throws IOException {
		rec.print(this._key);
		rec.print((this.leaf==null ? "" : (this.leaf ? "" : "+"))+this.name);
		rec.print(this.author);
	}

	@Override
	public String toHTMLLine() {
		return "<tr><td data-key=\""+this._key+"\"><i>"+(this.leaf==null ? "" : (this.leaf ? "" : "+"))+this.name+"</i></td><td>"+this.author+"</td></tr>";
	}

	@Override
	public String[] toStringArray() {
		return new String[] {
				this._key
				,(this.leaf==null ? "" : (this.leaf ? "" : "+"))+this.name
				,this.author
			};
	}

	@Override
	public boolean equals(Object o) {
		return this._key.equals(((SimpleTaxonResult)o)._key);
	}
}
