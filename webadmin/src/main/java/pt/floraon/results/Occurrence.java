package pt.floraon.results;

import java.io.IOException;

import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import com.arangodb.ArangoException;

import pt.floraon.driver.FloraOnDriver;
import pt.floraon.driver.FloraOnException;
import pt.floraon.driver.Constants.NativeStatus;
import pt.floraon.driver.Constants.PhenologicalStates;
import pt.floraon.entities.Author;
import pt.floraon.entities.SpeciesList;
import pt.floraon.entities.TaxEnt;

/**
 * Represents the occurrence of one taxon. Note that this is a class which has no representation in the DB. It is constructed by extracting info from a {@link SpeciesList},
 * the {@link Author}s and a {@link TaxEnt}.  
 * @author miguel
 *
 */
public class Occurrence implements ResultItem {
	/**
	 * NOTE: the field names must be exactly equal to the corresponding fields in the classes OBSERVED_IN and SpeciesList
	 * They don't need to be all here, though, just the relevant ones.
	 */
	protected String uuid,comment,dateInserted;			// from OBSERVED_IN
	protected Short confidence,validated,nativeStatus,phenoState;	// from OBSERVED_IN
	protected Integer weight;										// from OBSERVED_IN
	protected Float[] location={null,null};							// from SpeciesList
	protected Integer year,month,day,precision/*,area*/;			// from SpeciesList
	protected String pubNotes,privNotes,habitat;					// from SpeciesList

	protected String name,inventoryKey;
	protected Integer idEnt;
	protected String[] observers;	// this first of the array is the main observer
	protected Integer[] idauts;
	
	/**
	 * Creates a new occurrence from a record of a CSV imported file, Flora-On style.  This does not add anything to DB.
	 * @param record
	 * @return
	 * @throws FloraOnException
	 */
	public static Occurrence fromCSVline(CSVRecord record) throws FloraOnException {
		String tmp;
		if(record.size()!=19) throw new FloraOnException("Record #"+record.getRecordNumber()+" did not have 19 fields.");
		Occurrence occ=new Occurrence();

		String[] idautsstr=record.get(7).replace("\"", "").split(",");	// there may be several authors. The 1st is the main.
		occ.idauts=new Integer[idautsstr.length];
		for(int i=0;i<idautsstr.length;i++) occ.idauts[i]=Integer.parseInt(idautsstr[i]); 
		
		if(!(tmp=record.get(1).replace("\"", "")).equals("\\N")) occ.year=Integer.parseInt(tmp);
		if(!(tmp=record.get(2).replace("\"", "")).equals("\\N")) occ.month=Integer.parseInt(tmp);
		if(!(tmp=record.get(3).replace("\"", "")).equals("\\N")) occ.day=Integer.parseInt(tmp);
		occ.idEnt=Integer.parseInt(record.get(4));
		occ.location[0]=Float.parseFloat(record.get(5));
		occ.location[1]=Float.parseFloat(record.get(6));
		occ.precision=Integer.parseInt(record.get(8));
		switch(occ.precision) {
	    case 0: occ.precision=1;break;
	    case 1: occ.precision=100;break;
	    case 2: occ.precision=1000;break;
	    case 3: occ.precision=10000;break;
	    }
		occ.confidence = Short.parseShort(record.get(11));
		occ.validated = Short.parseShort(record.get(9));
		occ.phenoState = (int)Integer.parseInt(record.get(14)) == 1 ? PhenologicalStates.FLOWER.getCode() : PhenologicalStates.UNKNOWN.getCode();
		occ.uuid = record.get(18).replace("\"", "");
		occ.weight = Integer.parseInt(record.get(17));
		occ.comment = (tmp = record.get(10).replace("\"", "")).equals("\\N") ? null : tmp.replace("\n", "");
		occ.nativeStatus = Integer.parseInt(record.get(15))==0 ? NativeStatus.WILD.getCode() : NativeStatus.UNCERTAIN_SPONTANEITY.getCode();
		occ.dateInserted = (tmp = record.get(12).replace("\"", "")).equals("\\N") ? null : tmp;
// FIXME: column 13!		
		return occ;
	}
	
	/**
	 * Writes this occurrence to DB. This may involve creating a new SpeciesList and/or creating a new OBSERVED_IN relation.
	 * Does not add anything if an very similar occurrence already exists.
	 * @param graph
	 * @return
	 * @throws ArangoException 
	 * @throws FloraOnException 
	 */
	public void commit(FloraOnDriver graph) throws ArangoException, FloraOnException {
		// search for an existing species list in the same coordinates, same author and same date
		SpeciesList sl = graph.dbSpecificQueries.findExistingSpeciesList(idauts[0],location[0],location[1],year,month,day,3);
		Author autnode;
		if(sl == null) {	// add new specieslist
			autnode = graph.dbNodeWorker.getAuthorById(idauts[0]);		//find 1st author (main)
			if(autnode == null)
				throw new FloraOnException("Cannot find main author with idAut="+idauts[0]);
			else {	// first author exists and taxon exists, create node
				sl = new SpeciesList(graph,location[0],location[1],year,month,day,precision,null,null,false,null,null);
				sl.setObservedBy(autnode, true);
			}
			
			// add supplementary observers
			for(int i=1;i<idauts.length;i++) {
				autnode = graph.dbNodeWorker.getAuthorById(idauts[i]);		//find 1st author (main)
				if(autnode == null)			// SKIP line, main observer is compulsory
					throw new FloraOnException("Cannot find author with idAut="+idauts[i]);
				else
					sl.setObservedBy(autnode, false);
			}
		}
		
		TaxEnt taxnode = graph.dbNodeWorker.getTaxEntById(idEnt);	// find taxon with ident, we assume there's only one!

		if(taxnode == null)	// taxon not found! SKIP line
			throw new FloraOnException("Taxon with oldID "+idEnt+" not found.");
		
		this.name=taxnode.baseNode.getName();
		taxnode.setObservedIn(sl, confidence, validated, PhenologicalStates.getStateFromCode(phenoState), uuid, weight, comment, NativeStatus.getStateFromCode(nativeStatus), dateInserted);
	}
	
	@Override
	public void toCSVLine(CSVPrinter rec, Object obj) throws IOException {
		StringBuffer sb1=new StringBuffer();
		rec.print(this.inventoryKey);
		rec.print(this.year);
		rec.print(this.month);
		rec.print(this.day);
		rec.print(this.name);
		rec.print(this.location[0]);
		rec.print(this.location[1]);
		for(String s:this.observers) sb1.append(s+", ");
		rec.print(sb1.toString());
		rec.print(this.precision);
		rec.print(this.validated);
		rec.print(this.comment);
		rec.print(this.confidence);
		rec.print(this.dateInserted);
		rec.print(PhenologicalStates.getStateFromCode(this.phenoState));
		rec.print(NativeStatus.getStateFromCode(this.nativeStatus));
		rec.print(this.weight);
		rec.print(this.uuid);
	}
	
	@Override
	public String toHTMLTableRow(Object obj) {
		StringBuilder sb=new StringBuilder();
		sb.append("<tr><td>"+this.inventoryKey+"</td><td>"+this.name+"</td><td>"+this.location[0]+"</td><td>"+this.location[1]+"</td><td>"+NativeStatus.getStateFromCode(this.nativeStatus)+"</td><td>"+PhenologicalStates.getStateFromCode(this.phenoState)+"</td><td>"+this.dateInserted+"</td><td>"+this.confidence+"</td><td>"+this.validated+"</td><td>"+this.uuid+"</td><td>");
		for(String s:this.observers)
			sb.append(s+", ");
		sb.append("</td></tr>");
		return sb.toString();
	}

	@Override
	public String toHTMLListItem() {
		return "<li>"+this.location[0]+", "+this.location[1]+"</li>";
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
