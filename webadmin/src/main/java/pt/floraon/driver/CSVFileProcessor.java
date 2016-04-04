package pt.floraon.driver;

import static pt.floraon.driver.Constants.infraRanks;

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

import pt.floraon.driver.Constants.NativeStatus;
import pt.floraon.driver.Constants.OccurrenceStatus;
import pt.floraon.driver.Constants.TaxonRanks;
import pt.floraon.driver.Constants.TerritoryTypes;
import pt.floraon.entities.Attribute;
import pt.floraon.entities.Author;
import pt.floraon.entities.Character;
import pt.floraon.entities.TaxEnt;
import pt.floraon.entities.Territory;
import pt.floraon.results.Occurrence;

public class CSVFileProcessor extends BaseFloraOnDriver {
	public CSVFileProcessor(FloraOn driver) {
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
    
    public Map<String,Object> uploadRecordsFromFile(String filename) throws IOException, FloraOnException {
    	File file=new File(filename);
    	if(!file.canRead()) throw new IOException("Cannot read file "+filename);
    	return uploadRecordsFromStream(new FileInputStream(file));
    }
    
	public Map<String,Object> uploadRecordsFromStream(InputStream stream) throws FloraOnException, IOException {
		INodeWorker nwd=driver.getNodeWorkerDriver();
		Map<String,Object> out=new HashMap<String,Object>();
    	Reader freader=null;
    	long countupd=0,countnew=0,counterr=0,nrecs=0;
    	long newsplist=0;
    	long counter=0;
    	Map<Long,String> lineerrors=new HashMap<Long,String>();
    	System.out.print("Reading records ");

    	Occurrence occ;
		try {
			freader = new InputStreamReader(stream, StandardCharsets.UTF_8);
			Iterable<CSVRecord> records = CSVFormat.MYSQL.parse(freader);
			for (CSVRecord record : records) {
				try {
					occ=Occurrence.fromCSVline(record);
					nrecs++;
					if(nrecs % 100==0) {System.out.print(".");System.out.flush();}
					if(nrecs % 1000==0) {System.out.print(nrecs);System.out.flush();}
					
					nwd.createOccurrence(occ);
					//occ.commit(driver);
				} catch(FloraOnException e) {
					lineerrors.put(record.getRecordNumber(), e.getMessage());
					counterr++;
					continue;
				}
				
			    counter++;
			    if((counter % 2500)==0) {
			    	System.out.println(counter+" records processed.");
			    }
			}
		} catch (NumberFormatException e) {
			counterr++;
			e.printStackTrace();
		} finally {
			if(freader!=null) freader.close();
			out.put("speciesListsAdded", newsplist);
			out.put("speciesListsUpdated", countupd);
			out.put("newObservationsInserted", countnew);
			out.put("linesSkipped", counterr);
			out.put("linesProcessed", counter);
			out.put("errors", lineerrors);
		}

    	//if(abort) throw new FloraOnException(counterr+" errors found on lines "+lineerrors.toString());
    	return out;
    }

    public String uploadMorphologyFromFile(String filename) throws IOException, FloraOnException {
    	File file=new File(filename);
    	if(!file.canRead()) throw new IOException("Cannot read file "+filename);
    	return uploadMorphologyFromStream(new FileInputStream(file));
    }
	/**
	 * Uploads qualities as associates with taxa. First column is the taxa, the following columns the qualities.
	 * @param file
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
	
    /**
	 * Uploads a tab-separated CSV taxonomy file.
	 * The file can have as many columns as needed, the hierarchy goes form left to right.
	 * Authority of a name goes in front of the name between braces {}
	 * Multiple files can be uploaded, as they are merged. In this case, the <u>last column</u> of the new file <b>must match</b> the <u>orphan nodes</u> in the DB
	 * <p>Optionally, the last column may be an ID (if you want to match with old IDs)</p>
     * @param stream
     * @param simulate
     * @return
     * @throws IOException
     * @throws FloraOnException 
     * @throws ArangoException 
     * @throws TaxonomyException
     * @throws QueryException
     */
	public Map<String,Integer> uploadTaxonomyListFromStream(InputStream stream,boolean simulate) throws IOException, FloraOnException {
		INodeWorker nwd=driver.getNodeWorkerDriver();
    	Integer nnodes=0,nrels=0,nrecs=0;
    	Reader freader;

		freader = new InputStreamReader(stream, StandardCharsets.UTF_8);
		Iterator<CSVRecord> records = CSVFormat.EXCEL.withDelimiter('\t').withQuote('"').parse(freader).iterator();
		CSVRecord record=records.next();

		Integer nRankColumns=null;
		Integer oldIdColumn=null;
		String col,name,author;
		Map<Integer,Territory> territories=new HashMap<Integer,Territory>();
		
		for(int i=0;i<record.size();i++) {
			col=record.get(i);
			if(col.equals("id")) {
				if(nRankColumns==null) nRankColumns=i;
				oldIdColumn=i;
			}
			if(col.startsWith("territory:")) {	// create the territories
				if(nRankColumns==null) nRankColumns=i;
				String shortName=col.substring(col.indexOf(":")+1);
				Territory tv=nwd.getTerritoryFromShortName(shortName);
				if(tv==null) {
					tv=nwd.createTerritory(shortName, shortName, TerritoryTypes.COUNTRY, null, true, null);
					System.out.println("Added territory: "+shortName);
				}
				territories.put(i, tv);
			}
		}
		if(nRankColumns==null) nRankColumns=record.size();
		
		String[] rankNames=new String[nRankColumns];
		for(int i=0;i<nRankColumns;i++) rankNames[i]=record.get(i);

		System.out.print("Reading file ");
		try {
			TaxEnt curTaxEnt,parentNode;
			TaxEnt parsedName;
			boolean pastspecies;	// true after the column species has passed (so to know when to append names to genus)
			while(records.hasNext()) {
				record=records.next();
				nrecs++;
				if(nrecs % 100==0) {System.out.print(".");System.out.flush();}
				if(nrecs % 1000==0) {System.out.print(nrecs);System.out.flush();}
				parentNode=null;
				curTaxEnt=null;
				pastspecies=false;
				for(int i=0;i<rankNames.length;i++) {
					try {
						parsedName=TaxEnt.parse(record.get(i));
					} catch (TaxonomyException e) {
						// is it an empty cell? skip. 
						continue;
					}
					if(rankNames[i].equals("species")) pastspecies=true;
					// special cases: if species or lower rank, must prepend genus.
					if(pastspecies)
						name=parentNode.getName()+" "+(rankNames[i].equals("species") ? "" : (infraRanks.containsKey(rankNames[i]) ? infraRanks.get(rankNames[i]) : rankNames[i])+" ")+parsedName.getName();
					else
						name=null;
					
					if(pastspecies && parsedName.getAuthor()==null) author=parentNode.getAuthor(); else author=null;

					parsedName.update(
						name
						, TaxonRanks.valueOf(rankNames[i].toUpperCase()).getValue()
						, author
						, null
						, true);
					
					curTaxEnt=nwd.getTaxEnt(parsedName);
					
					if(curTaxEnt==null) {	// if node does not exist, add it.
						curTaxEnt=nwd.createTaxEntFromTaxEnt(parsedName);//   TaxEnt.newFromTaxEnt(FloraOnDriver.this,parsedName);
						//System.out.println("ADD "+parsedName.getName());System.out.flush();
						nnodes++;
					} else {	// if it exists, update its rank and authority. WHY this?!
						//System.out.println("EXISTS "+parsedName.getFullName());System.out.flush();
						nwd.updateTaxEntNode(
							driver.asNodeKey(curTaxEnt.getID())
							, new TaxEnt(
								null
								, parsedName.getRankValue()
								, parsedName.getAuthor()
								, null, null, null, null)
							, false);
								//null, parsedName.getRank(), null, parsedName.getAuthor()!=null ? parsedName.getAuthor() : null, null);
					}
					if(parentNode!=null) {	// create PART_OF relationship to previous column
						nrels+=driver.wrapNode(driver.asNodeKey(curTaxEnt.getID())).setPART_OF(driver.asNodeKey(parentNode.getID()));
					}
					parentNode=curTaxEnt;
				}

				if(oldIdColumn != null) { 	// FIXME: OldId
					nwd.updateDocument(driver.asNodeKey(curTaxEnt.getID()), "oldId", (Integer)Integer.parseInt(record.get(oldIdColumn)));
					//curTaxEnt.setOldId(Integer.parseInt(record.get(oldIdColumn)));
					//curTaxEnt.commit();
				}
				
				for(Entry<Integer,Territory> terr : territories.entrySet()) {	// bind this taxon with the territories with the given native status
					String ns=record.get(terr.getKey());
					if(ns!=null && !ns.equals(""))
						driver.wrapTaxEnt(driver.asNodeKey(curTaxEnt.getID())).setNativeStatus(
							driver.asNodeKey(terr.getValue().getID()), NativeStatus.fromString(ns.toUpperCase()), OccurrenceStatus.PRESENT, false);
				}
			}
		} catch (FloraOnException e) {
			e.printStackTrace();
		}
		freader.close();
		System.out.println("\nRead "+nrecs+" records; created "+nnodes+" nodes and "+nrels+" relationships");
		Map<String,Integer> out=new HashMap<String,Integer>();
		out.put("nrecs", nrecs);
		out.put("nnodes", nnodes);
		out.put("nrels", nrels);
		return out;
	}

	public Map<String,Integer> uploadTaxonomyListFromFile(String file,boolean simulate) throws IOException, FloraOnException {
    	File tl=new File(file);
    	if(!tl.canRead()) throw new IOException("Cannot read input file "+file);
    	return uploadTaxonomyListFromStream(new FileInputStream(file),simulate);
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
