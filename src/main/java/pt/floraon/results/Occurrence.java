package pt.floraon.results;

import pt.floraon.entities.Author;
import pt.floraon.entities.SpeciesList;
import pt.floraon.entities.TaxEnt;
import pt.floraon.server.Constants.NativeStatus;
import pt.floraon.server.Constants.PhenologicalStates;

/**
 * Represents the occurrence of one taxon. Note that this is a class which has no representation in the DB. It is constructed by extracting info from a {@link SpeciesList},
 * the {@link Author}s and a {@link TaxEnt}.  
 * @author miguel
 *
 */
public class Occurrence implements ResultItem {
	protected String name,dateInserted,uuid,inventoryKey,occurrenceKey;
	protected Short wild,confidence,phenoState,validated;
	protected Integer weight;
	protected Float[] location={null,null};
	protected String[] observers;
	
	@Override
	public String toCSVLine() {
		StringBuilder sb=new StringBuilder();
		sb.append(this.inventoryKey+"\t"+this.name+"\t"+this.location[0]+"\t"+this.location[1]+"\t"+NativeStatus.getStateFromCode(this.wild)+"\t"+PhenologicalStates.getStateFromCode(this.phenoState)+"\t"+this.dateInserted+"\t"+this.confidence+"\t"+this.validated+"\t"+this.uuid+"\t");
		for(String s:this.observers)
			sb.append(s+", ");
		 return sb.toString();
	}
	
	@Override
	public String toHTMLLine() {
		StringBuilder sb=new StringBuilder();
		sb.append("<tr><td>"+this.inventoryKey+"</td><td>"+this.name+"</td><td>"+this.location[0]+"</td><td>"+this.location[1]+"</td><td>"+NativeStatus.getStateFromCode(this.wild)+"</td><td>"+PhenologicalStates.getStateFromCode(this.phenoState)+"</td><td>"+this.dateInserted+"</td><td>"+this.confidence+"</td><td>"+this.validated+"</td><td>"+this.uuid+"</td><td>");
		for(String s:this.observers)
			sb.append(s+", ");
		sb.append("</td></tr>");
		return sb.toString();
	}

	@Override
	public String[] toStringArray() {
		// TODO Auto-generated method stub
		return null;
	}
}
