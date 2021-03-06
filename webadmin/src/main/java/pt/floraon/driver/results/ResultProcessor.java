package pt.floraon.driver.results;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.gson.Gson;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

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
	@Deprecated
	public void toHTMLTable(PrintWriter output,String cls, Object obj) {
		output.print("<table class=\""+cls+"\">");
		getHTMLTableRows(output, obj);
    	output.print("</table>");
    	output.flush();
	}
	
	/**
	 * Outputs the result as the HTML table rows (without the TABLE tag)
	 * @param output
	 * @param obj
	 */
	public void getHTMLTableRows(PrintWriter output, Object obj) {
		T tmp;
		boolean header=false;
		while (this.results.hasNext()) {
			tmp=this.results.next();
			if(!header) {
				output.print(tmp.getHTMLTableHeader(obj));
				header=true;
			}
			output.print(tmp.toHTMLTableRow(obj));
		}
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
    	return new Gson().toJson(out); // EntityFactory.toJsonString(out);
    }

    public JsonElement toJSONElement() {
    	List<T> out=new ArrayList<T>();
		while(this.results.hasNext()) {
			out.add(this.results.next());
		}
    	return new Gson().toJsonTree(out); // EntityFactory.toJsonElement(out, false);
    }

	public String toDWC() {
		// TODO export DWC string
		return null;
	}
}
