package pt.floraon.results;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import com.arangodb.entity.EntityFactory;
import com.google.gson.JsonElement;

import dnl.utils.text.table.TextTable;
/**
 * Utility class to handle query responses
 * @author miguel
 *
 * @param <T> A class implementing {@link ResultItem}
 */
public class ResultProcessor<T extends ResultItem> {
	private Iterator<T> results;
	
	public ResultProcessor(Iterator<T> results) {
		this.results=results;
	}
	 /*private final Class<T> type;

	public ResultProcessor(Class<T> type) {
		this.type = type;
	}*/

	public String toCSVTable() {
		StringBuffer sb=new StringBuffer();
		CSVPrinter out;
		try {
			out = new CSVPrinter(sb,CSVFormat.DEFAULT.withQuote('\"'));
			while (this.results.hasNext()) {
				this.results.next().toCSVLine(out);
				out.println();
			}
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return sb.toString();
	}

	public String toHTMLTable() {
		StringBuilder sb=new StringBuilder(); 
		T tmp;
		sb.append("<table>");
    	while (this.results.hasNext()) {
    		tmp=this.results.next();
    		sb.append(tmp.toHTMLLine());
    	}
    	sb.append("</table>");
    	return sb.toString();
	}
	
	public TextTable toPrettyTable() {
		List<String[]> tmp=new ArrayList<String[]>();
    	while (this.results.hasNext()) {
    		tmp.add(this.results.next().toStringArray());
    	}
    	return new TextTable(new String[] {"Count","Key","RelTypes","Name","Matches"},tmp.toArray(new String[0][0]));
	}

    public String toJSONString() {
    	List<T> out=new ArrayList<T>();
		while(this.results.hasNext()) {
			out.add(this.results.next());
		}
    	return EntityFactory.toJsonString(out);
    }

    public JsonElement toJSONElement() {
    	List<T> out=new ArrayList<T>();
		while(this.results.hasNext()) {
			out.add(this.results.next());
		}
    	return EntityFactory.toJsonElement(out, false);
    }

	public String toDWC() {
		// TODO export DWC string
		return null;
	}
}
