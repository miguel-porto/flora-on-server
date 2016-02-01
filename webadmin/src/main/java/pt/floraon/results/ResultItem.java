package pt.floraon.results;

import java.io.IOException;

import org.apache.commons.csv.CSVPrinter;

/**
 * Defines a result item of a query, which can be output in different formats.
 * @author miguel
 *
 */
public interface ResultItem {
	public void toCSVLine(CSVPrinter rec, Object obj) throws IOException;
	public void getCSVHeader(CSVPrinter rec, Object obj) throws IOException;
	public String toHTMLTableRow(Object obj);	// optional data object needed by some implementations
	public String toHTMLListItem();
	public String[] toStringArray();
}
