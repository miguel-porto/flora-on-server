package pt.floraon.occurrences;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import pt.floraon.driver.*;
import pt.floraon.morphology.entities.Attribute;
import pt.floraon.occurrences.entities.Author;
import pt.floraon.morphology.entities.Character;
import pt.floraon.taxonomy.TaxonomyImporter;
import pt.floraon.taxonomy.entities.TaxEnt;

/**
 * A bunch of methods to parse and import CSV data files of different facets
 */
public class CSVFileProcessor extends BaseFloraOnDriver {
	public CSVFileProcessor(IFloraOn driver) {
		super(driver);
	}

	public Map<String,Integer> uploadAuthorsFromFile(String filename) throws IOException, NumberFormatException, FloraOnException {
    	File file=new File(filename);
    	if(!file.canRead()) throw new IOException("Cannot read file "+filename);
    	return uploadAuthorsFromStream(new FileInputStream(file));
	}
	
    public Map<String,Integer> uploadAuthorsFromStream(InputStream stream) throws IOException, NumberFormatException, FloraOnException {
    	INodeWorker nwd=driver.getNodeWorkerDriver();
    	Author autnode;
    	Reader freader=null;
    	int countnew=0,countupd=0;
    	Iterable<CSVRecord> records=null;
    	
		freader = new InputStreamReader(stream, StandardCharsets.UTF_8);
		records = CSVFormat.MYSQL.parse(freader);
		for (CSVRecord record : records) {
			autnode=nwd.getAuthorById((int)Integer.parseInt(record.get(0)));
			if(autnode==null) {	// add author
				nwd.createAuthor(
					new Author(
						(int)Integer.parseInt(record.get(0))
						,record.get(1).replace("\"", "")
						,!record.get(4).equals("\\N") ? record.get(4).replace("\"", "") : null
						,record.get(2).replace("\"", "")
						,!record.get(5).equals("\\N") ? record.get(5).replace("\"", "") : null
						,(int)Integer.parseInt(record.get(3)))
				);
				countnew++;
			} else {
				// TODO update author if it exists! does it make sense here?
				countupd++;
			}
		}
		freader.close();
		Map<String,Integer> out=new HashMap<String,Integer>();
		out.put("new", countnew);
		out.put("upd", countupd);
		return out;
    }

    public String uploadMorphologyFromFile(String filename) throws IOException, FloraOnException {
    	File file=new File(filename);
    	if(!file.canRead()) throw new IOException("Cannot read file "+filename);
    	return uploadMorphologyFromStream(new FileInputStream(file));
    }
	/**
	 * Uploads qualities as associates with taxa. First column is the taxa, the following columns the qualities.
	 * @param stream
	 * @return
	 * @throws IOException
	 */
	public String uploadMorphologyFromStream(InputStream stream) throws IOException, FloraOnException {
		INodeWorker NWD=driver.getNodeWorkerDriver();
		StringBuilder err=new StringBuilder();
    	Reader freader;
    	TaxEnt fullname1, taxnode;
    	Attribute attribute;
    	IAttributeWrapper atW;
    	Character character;
    	String tmp;
    	int nerrors=0,nnodes=0,nrels=0;

		freader = new InputStreamReader(stream, StandardCharsets.UTF_8);
		CSVParser csvp=CSVFormat.EXCEL.withDelimiter('\t').withQuote('"').withHeader().parse(freader);
		Iterator<CSVRecord> records =csvp.iterator();
		
		Iterator<Entry<String,Integer>> characters=csvp.getHeaderMap().entrySet().iterator();
		List<Character> colnames=new ArrayList<Character>();
		// look for the characters in the DB or create new if not found
		characters.next();	// skip 1st column
		while(characters.hasNext()) {
			tmp=characters.next().getKey();
			character=NWD.getCharacterByName(tmp);
			if(character==null) {
				colnames.add(NWD.createCharacter(new Character(tmp, null, null)));
				System.out.println("Added character "+tmp);
			} else
				colnames.add(character);
		}
		
		CSVRecord record;
		String[] attrs;
		int count=0;
		while(records.hasNext()) {
			record=records.next();
			count++;
			if(count % 100==0) {System.out.print(".");System.out.flush();}
			if(count % 1000==0) {System.out.print(count);System.out.flush();}
			try {
				fullname1=TaxEnt.parse(record.get("taxon"));
				taxnode=NWD.getTaxEnt(fullname1);
				if(taxnode==null) throw new QueryException(fullname1.getFullName() +" not found.");
				for(int i=1;i<record.size();i++) {
					attrs=record.get(i).split(",");
					for(String attr:attrs) {
						attr=attr.trim();
						if(attr.length()==0) continue;
						if(attr.equalsIgnoreCase("NA")) {
							// TODO: handle when character not applicable
						} else {
							attribute=NWD.getAttributeByName(attr);
							if(attribute==null) {		// an attribute already exists with this name TODO: it should check the character so it needn't be unique in the DB!
								attribute = NWD.createAttributeFromName(attr, null, null);
								atW=driver.wrapAttribute(driver.asNodeKey(attribute.getID()));
								atW.setAttributeOfCharacter(driver.asNodeKey(colnames.get(i-1).getID()));
								//System.out.println("Added \""+attr+"\" of \""+colnames.get(i)+"\"");
								nnodes++;
							} else atW=driver.wrapAttribute(driver.asNodeKey(attribute.getID()));
							nrels+=driver.wrapTaxEnt(driver.asNodeKey(taxnode.getID())).setHAS_QUALITY(driver.asNodeKey(attribute.getID()));
						}
					}
				}
			} catch (QueryException | IllegalArgumentException | TaxonomyException e) {
				err.append("Error processing line "+record.getRecordNumber()+": "+e.getMessage()+"\n");
				System.err.println("Error processing line "+record.getRecordNumber()+": "+e.getMessage());
				nerrors++;
				continue;
			}
		}

		freader.close();
		if(nerrors>0)
			return nerrors+" errors found while parsing file. Nothing changed.<br/><textarea>"+err.toString()+"</textarea>";
		else
			return nnodes+" new nodes added and "+nrels+" relationships created.";
	}

	public TaxonomyImporter getTaxonomyImporter() {
		return new TaxonomyImporter(driver);
	}

/*
	public String uploadImagesFromStream(InputStream stream) throws FloraOnException, IOException {
		Reader freader=null;
		freader = new InputStreamReader(stream, StandardCharsets.UTF_8);
		Iterable<CSVRecord> records = CSVFormat.MYSQL.parse(freader);
		Image img;
		for(CSVRecord record : records) {
			img=new Image(FloraOnDriver.this,record);
		}
		return null;
	}
	
			public void addSpeciesLists(JsonObject sl) throws FloraOnException, ArangoException {
			JsonArray arr=new JsonArray();
			arr.add(sl);
			addSpeciesLists(arr);
		}
		public void addSpeciesLists(JsonArray sls) throws FloraOnException, ArangoException {
			for(int i=0; i<sls.size(); i++)
				new SpeciesList(FloraOnDriver.this,sls.get(i).getAsJsonObject());
		}

*/
}
