package pt.floraon.results;

import java.io.IOException;

import org.apache.commons.csv.CSVPrinter;

public interface ResultItem {
	public void toCSVLine(CSVPrinter rec) throws IOException;
	public String toHTMLLine();
	public String[] toStringArray();
}
