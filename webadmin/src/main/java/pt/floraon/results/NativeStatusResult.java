package pt.floraon.results;

import java.io.IOException;

import org.apache.commons.csv.CSVPrinter;

import pt.floraon.driver.Constants.NativeStatus;
import pt.floraon.entities.EXISTS_IN;
import pt.floraon.entities.Territory;

/**
 * Represents one association between a territory and a taxon (NOTE: the taxon is not referred in this class)
 * @author miguel
 *
 */
public class NativeStatusResult implements ResultItem {
	protected Territory territory;
	protected EXISTS_IN nativeStatus;

	@Override
	public void toCSVLine(CSVPrinter rec, Object obj) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String toHTMLTableRow(Object obj) {
		if(this.nativeStatus.getNativeStatus()==null)
			return "<tr><td>"+this.territory.getName()+"</td><td class=\""+NativeStatus.ERROR.toString()+"\">"+NativeStatus.ERROR.toString()+"</td><td>"+this.nativeStatus.getOccurrenceStatus().toString()+(this.nativeStatus.isUncertainOccurrenceStatus() ? " (uncertain)" : "")+"</td><td>"+this.nativeStatus.getAbundanceLevel().toString()+"</td></tr>";
		else
			return "<tr><td>"+this.territory.getName()+"</td><td class=\""+this.nativeStatus.getNativeStatus().toString()+"\">"+this.nativeStatus.getNativeStatus().toString()+"</td><td>"+this.nativeStatus.getOccurrenceStatus().toString()+(this.nativeStatus.isUncertainOccurrenceStatus() ? " (uncertain)" : "")+"</td><td>"+this.nativeStatus.getAbundanceLevel().toString()+"</td></tr>";
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

	@Override
	public String getHTMLTableHeader(Object obj) {
		return "<tr><th>Territory</th><th>Native Status</th><th>Occurrence Status</th><th>Abundance Level</th></tr>";
	}

}
