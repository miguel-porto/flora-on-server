package pt.floraon.results;

import java.io.IOException;

import org.apache.commons.csv.CSVPrinter;

import com.google.gson.JsonElement;

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
		StringBuilder sb = new StringBuilder();
		sb.append("<tr><td>").append(this.territory.getName()).append("</td><td class=\"")
			.append(this.nativeStatus.getNativeStatus().toString()).append("\">").append(this.nativeStatus.getNativeStatus().toString()).append("</td><td>")
			.append(this.nativeStatus.getOccurrenceStatus().toString()).append((this.nativeStatus.isUncertainOccurrenceStatus() ? " (uncertain)" : "")).append("</td><td>")
			.append(this.nativeStatus.getAbundanceLevel().toString()).append("</td><td>")
			.append(this.nativeStatus.getIntroducedStatus().toString()).append("</td><td>")
			.append(this.nativeStatus.getNaturalizationDegree().toString()).append("</td></tr>");
		return sb.toString();
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
		return "<tr><th>Territory</th><th>Native Status</th><th>Occurrence Status</th><th>Abundance Level</th><th>Introduced Status</th><th>Naturalization Degree</th></tr>";
	}

	@Override
	public JsonElement toJson() {
		// TODO Auto-generated method stub
		return null;
	}

}
