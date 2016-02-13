package pt.floraon.results;

import java.io.IOException;

import org.apache.commons.csv.CSVPrinter;

import pt.floraon.entities.EXISTS_IN;
import pt.floraon.entities.Territory;

public class NativeStatusResult implements ResultItem {
	protected Territory territory;
	protected EXISTS_IN nativeStatus;

	@Override
	public void toCSVLine(CSVPrinter rec, Object obj) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String toHTMLTableRow(Object obj) {
		return "<tr><td>"+this.territory.getName()+"</td><td class=\""+this.nativeStatus.getNativeStatus().toString()+"\">"+this.nativeStatus.getNativeStatus().toString()+", "+this.nativeStatus.getOccurrenceStatus().toString()+"</td></tr>";
	}

	@Override
	public String toHTMLListItem() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] toStringArray() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void getCSVHeader(CSVPrinter rec, Object obj) throws IOException {
		// TODO Auto-generated method stub
	}

}
