package pt.floraon.results;

import java.io.IOException;
import java.io.PrintWriter;
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

	public String toCSVTable(Object obj) {
		StringBuffer sb=new StringBuffer();
		CSVPrinter out;
		T record;
		boolean header=false;
		try {
			out = new CSVPrinter(sb,CSVFormat.DEFAULT.withQuote('\"'));
			
			while (this.results.hasNext()) {
				record=this.results.next();
				if(!header) {
					record.getCSVHeader(out, obj);
					header=true;
				}
				record.toCSVLine(out, obj);
				out.println();
			}
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return sb.toString();
	}

	@Deprecated
	public String toHTMLTable() {
		StringBuilder sb=new StringBuilder(); 
		T tmp;
		sb.append("<table>");
    	while (this.results.hasNext()) {
    		tmp=this.results.next();
    		sb.append(tmp.toHTMLTableRow(null));
    	}
    	sb.append("</table>");
    	return sb.toString();
	}

	/**
	 * Outputs the results as an HTML table
	 * @param output
	 * @param obj Optional data object needed by some implementations of {@link ResultItem} 
	 */
	public void toHTMLTable(PrintWriter output,Object obj) {
		T tmp;
		output.print("<table class=\"taxonlist\">");
    	while (this.results.hasNext()) {
    		tmp=this.results.next();
    		output.print(tmp.toHTMLTableRow(obj));
    	}
    	output.print("</table>");
    	output.flush();
	}

	/**
	 * Outputs these results as an HTML UL tag, optionally with a class.
	 * @param cssClass
	 * @return
	 */
	public String toHTMLList(String cssClass) {
		StringBuilder sb=new StringBuilder(); 
		T tmp;
		if(cssClass==null)
			sb.append("<ul>");
		else
			sb.append("<ul class=\"").append(cssClass).append("\">");
		while (this.results.hasNext()) {
			tmp=this.results.next();
			sb.append(tmp.toHTMLListItem());
		}
		sb.append("</ul>");
		return sb.toString();
	}

	public String toHTMLList() {
		return toHTMLList(null);
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
