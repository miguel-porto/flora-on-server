package pt.floraon.driver.results;

import java.io.IOException;

import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.jsoup.Jsoup;

import pt.floraon.driver.FloraOnException;
import pt.floraon.driver.Constants.NativeStatus;
import pt.floraon.driver.Constants.PhenologicalStates;
import pt.floraon.occurrences.entities.OBSERVED_IN;
import pt.floraon.occurrences.entities.SpeciesList;

/**
 * Represents the occurrence of one taxon. Note that this is a class which has no representation in the DB.
 * It is constructed by extracting info from a {@link SpeciesList},
 * the {@link Author}s and a {@link TaxEnt}.  
 * @author miguel
 *
 */
public class Occurrence implements ResultItem {
	protected OBSERVED_IN observation;
	protected SpeciesList speciesList;
	protected String name,inventoryKey;
	protected Integer idEnt;
	protected String[] observers;	// this first of the array is the main observer
	protected Integer[] idauts;
	
	public void setName(String name) {
		this.name=name;
	}
	public Integer getIdEnt() {
		return this.idEnt;
	}
	public Integer[] getIdAuts() {
		return this.idauts;
	}
	public OBSERVED_IN getObservation() {
		return this.observation;
	}
	public SpeciesList getSpeciesList() {
		return this.speciesList;
	}
	/**
	 * Creates a new occurrence from a record of a CSV imported file, Flora-On style.  This does not add anything to DB.
	 * @param record
	 * @return
	 * @throws FloraOnException
	 */
	public static Occurrence fromCSVline(CSVRecord record) throws FloraOnException {
		String tmp;
		if(record.size()!=20) throw new FloraOnException("Record #"+record.getRecordNumber()+" did not have 20 fields.");
		Occurrence occ=new Occurrence();

		String[] idautsstr=record.get(7).replace("\"", "").split(",");	// there may be several authors. The 1st is the main.
		occ.idauts=new Integer[idautsstr.length];
		for(int i=0;i<idautsstr.length;i++) occ.idauts[i]=Integer.parseInt(idautsstr[i]); 
		
		Integer precision=Integer.parseInt(record.get(8));
		switch(precision) {
	    case 0: precision=1;break;
	    case 1: precision=100;break;
	    case 2: precision=1000;break;
	    case 3: precision=10000;break;
	    }
		
		occ.idEnt=Integer.parseInt(record.get(4));
		occ.speciesList = new SpeciesList(
			Float.parseFloat(record.get(5))
			,Float.parseFloat(record.get(6))
			,(tmp=record.get(1).replace("\"", "")).equals("\\N") ? null : Integer.parseInt(tmp)
			,(tmp=record.get(2).replace("\"", "")).equals("\\N") ? null : Integer.parseInt(tmp)
			,(tmp=record.get(3).replace("\"", "")).equals("\\N") ? null : Integer.parseInt(tmp)
			,precision
			,null,null,false,null,null
		);
		/*if(!(tmp=record.get(1).replace("\"", "")).equals("\\N")) occ.year=Integer.parseInt(tmp);
		if(!(tmp=record.get(2).replace("\"", "")).equals("\\N")) occ.month=Integer.parseInt(tmp);
		if(!(tmp=record.get(3).replace("\"", "")).equals("\\N")) occ.day=Integer.parseInt(tmp);
		occ.location[0]=Float.parseFloat(record.get(5));
		occ.location[1]=Float.parseFloat(record.get(6));
		occ.precision=Integer.parseInt(record.get(8));
		switch(occ.precision) {
	    case 0: occ.precision=1;break;
	    case 1: occ.precision=100;break;
	    case 2: occ.precision=1000;break;
	    case 3: occ.precision=10000;break;
	    }*/
		
		occ.observation = new OBSERVED_IN(
			Short.parseShort(record.get(11))
			, Short.parseShort(record.get(9))
			, (int)Integer.parseInt(record.get(14)) == 1 ? PhenologicalStates.FLOWER : PhenologicalStates.UNKNOWN
			, record.get(18).replace("\"", "")
			, Integer.parseInt(record.get(17))
			, (tmp = record.get(10).replace("\"", "")).equals("\\N") ? null : Jsoup.parse(tmp.replace("\n", "")).text()
			, (tmp = record.get(13).replace("\"", "")).equals("\\N") ? null : Jsoup.parse(tmp.replace("\n", "")).text()
			, Integer.parseInt(record.get(15))==0 ? NativeStatus.NATIVE : NativeStatus.DOUBTFULLY_NATIVE
			, (tmp = record.get(12).replace("\"", "")).equals("\\N") ? null : tmp);
		
		/*occ.confidence = Short.parseShort(record.get(11));
		occ.validated = Short.parseShort(record.get(9));
		occ.phenoState = (int)Integer.parseInt(record.get(14)) == 1 ? PhenologicalStates.FLOWER.getCode() : PhenologicalStates.UNKNOWN.getCode();
		occ.uuid = record.get(18).replace("\"", "");
		occ.weight = Integer.parseInt(record.get(17));
		occ.publicComment = (tmp = record.get(10).replace("\"", "")).equals("\\N") ? null : Jsoup.parse(tmp.replace("\n", "")).text();
		occ.privateComment = (tmp = record.get(13).replace("\"", "")).equals("\\N") ? null : Jsoup.parse(tmp.replace("\n", "")).text();
		occ.nativeStatus = Integer.parseInt(record.get(15))==0 ? NativeStatus.WILD.getCode() : NativeStatus.UNCERTAIN.getCode();
		occ.dateInserted = (tmp = record.get(12).replace("\"", "")).equals("\\N") ? null : tmp;*/
		return occ;
	}
		
	@Override
	public void toCSVLine(CSVPrinter rec, Object obj) throws IOException {
		StringBuffer sb1=new StringBuffer();
		rec.print(this.inventoryKey);
		rec.print(this.speciesList.getYear());
		rec.print(this.speciesList.getMonth());
		rec.print(this.speciesList.getDay());
		rec.print(this.name);
		rec.print(this.speciesList.getLocation()[0]);
		rec.print(this.speciesList.getLocation()[1]);
		for(String s:this.observers) sb1.append(s+", ");
		rec.print(sb1.toString());
		rec.print(this.speciesList.getPrecision());
		rec.print(this.observation.getValidated());
		rec.print(this.observation.getPublicComment());
		rec.print(this.observation.getConfidence());
		rec.print(this.observation.getDateInserted());
		rec.print(this.observation.getPhenoState());
		rec.print(this.observation.getNativeStatus());
		rec.print(this.observation.getWeight());
		rec.print(this.observation.getUUID());
	}
	
	@Override
	public String toHTMLTableRow(Object obj) {
		StringBuilder sb=new StringBuilder();
		sb.append("<tr><td>"+this.inventoryKey+"</td><td>"+this.name+"</td><td>"+this.speciesList.getLocation()[0]+"</td><td>"+this.speciesList.getLocation()[1]+"</td><td>"+this.observation.getNativeStatus()+"</td><td>"+this.observation.getPhenoState()+"</td><td>"+this.observation.getDateInserted()+"</td><td>"+this.observation.getConfidence()+"</td><td>"+this.observation.getValidated()+"</td><td>"+this.observation.getUUID()+"</td><td>");
		for(String s:this.observers)
			sb.append(s+", ");
		sb.append("</td></tr>");
		return sb.toString();
	}

	@Override
	public String toHTMLListItem() {
		return "<li>"+this.speciesList.getLocation()[0]+", "+this.speciesList.getLocation()[1]+"</li>";
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
		// TODO Auto-generated method stub
		return null;
	}
}
