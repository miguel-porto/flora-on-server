package pt.floraon.taxonomy;

import jline.internal.Log;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import pt.floraon.driver.*;
import pt.floraon.driver.interfaces.IFloraOn;
import pt.floraon.driver.interfaces.INodeWorker;
import pt.floraon.driver.utils.StringUtils;
import pt.floraon.taxonomy.entities.TaxEnt;
import pt.floraon.taxonomy.entities.TaxonName;
import pt.floraon.taxonomy.entities.Territory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static pt.floraon.driver.Constants.infraRanks;

/**
 * A class to import checklists in table format
 * Created by miguel on 08-02-2017.
 */
public class TaxonomyImporter extends BaseFloraOnDriver {
    public TaxonomyImporter(IFloraOn driver) {
        super(driver);
    }

    public Map<String,Integer> uploadTaxonomyListFromStream2(InputStream stream, boolean simulate) throws IOException, FloraOnException {
        INodeWorker nwd=driver.getNodeWorkerDriver();
        int nnodes = 0, nrels = 0, nrecs = 0, nerrors = 0;
        Reader freader;

        freader = new InputStreamReader(stream, StandardCharsets.UTF_8);
        Iterator<CSVRecord> records = CSVFormat.EXCEL.withDelimiter('\t').withQuote('"').parse(freader).iterator();
        CSVRecord record = records.next();

        // read header and add territories
        Integer nRankColumns = null;
        Integer oldIdColumn = null;
        String col, name, author;
        Map<Integer, Territory> territories = new HashMap<Integer,Territory>();

        for(int i=0; i<record.size(); i++) {
            col = record.get(i);
            if(col.equals("id")) {
                if(nRankColumns == null) nRankColumns = i;
                oldIdColumn = i;
            }
            if(col.startsWith("territory:")) {	// create the territories
                if(nRankColumns == null) nRankColumns = i;
                String shortName = col.substring(col.indexOf(":") + 1);
                Territory tv = nwd.getTerritoryFromShortName(shortName);
                if(tv == null) {
                    tv = nwd.createTerritory(shortName, shortName, Constants.TerritoryTypes.COUNTRY, null, true, null);
                    System.out.println("Added territory: "+shortName);
                }
                territories.put(i, tv);
            }
        }
        if(nRankColumns == null) nRankColumns = record.size();

        // fetch rank names from header
        String[] rankNames = new String[nRankColumns];
        for(int i=0; i<nRankColumns; i++) rankNames[i] = record.get(i);

        Log.info("Reading file ");
        try {
            TaxEnt curTaxEnt, parentNode;
            TaxEnt parsedName;
            while(records.hasNext()) {
                record = records.next();
                nrecs++;
                if(nrecs % 100 == 0) {System.out.print(".");System.out.flush();}
                if(nrecs % 1000 == 0) {System.out.print(nrecs);System.out.flush();}
                parentNode = null;
                curTaxEnt = null;
                for(int i=0; i<rankNames.length; i++) {
                    if(StringUtils.isStringEmpty(record.get(i))) continue;
                    try {
                        parsedName = new TaxEnt(new TaxonName(record.get(i)));
                    } catch (TaxonomyException e) {
                        Log.error("Ignoring name: " + record.get(i));
                        nerrors++;
                        continue;
                    }

                    // only species or inferior have their ranks automatically. The others must see the header
                    if(parsedName.getRank() == null)
                        parsedName.setRank(Constants.TaxonRanks.valueOf(rankNames[i].toUpperCase()).getValue());
                    parsedName.setCurrent(true);

                    // is it a species or inferior?
                    if(parsedName.getRank().isSpeciesOrInferior()) {
                        TaxonName taxonName;
                        try {
                            taxonName = parsedName.getTaxonName();
                        } catch(TaxonomyException te) {
                            nerrors ++;
                            continue;
                        }
                        // add genus node, or fetch it, if it exists
                        TaxEnt genus = new TaxEnt(taxonName.getGenus(),
                                Constants.TaxonRanks.GENUS.getValue(), null, null);
                        genus.setCurrent(true);
                        curTaxEnt = nwd.getSingleTaxEntOrNull(genus, true);
                        if(curTaxEnt == null) { // no genus, create
                            curTaxEnt = nwd.createTaxEntFromTaxEnt(genus);
                            nnodes++;
                        }

                        // connect it to the previous column taxon
                        if (parentNode != null) {   // create PART_OF relationship to previous column
                            // if parent is higher in taxonomy than genus TODO
                            if(parentNode.getRankValue() < curTaxEnt.getRankValue()) {
                                nrels += driver.wrapNode(driver.asNodeKey(curTaxEnt.getID())).setPART_OF(driver.asNodeKey(parentNode.getID()));
                                parentNode = curTaxEnt;
                            }
                        }

                        // add species node
                        TaxEnt species = new TaxEnt(taxonName.getGenus() + " " + taxonName.getSpecificEpithet(),
                                Constants.TaxonRanks.SPECIES.getValue(), taxonName.getAuthor(0), taxonName.getSensu(),
                                taxonName.getAnnotation(), true, null, null, null);
                        curTaxEnt = nwd.getSingleTaxEntOrNull(species, true);
                        if(curTaxEnt == null) {
                            curTaxEnt = nwd.createTaxEntFromTaxEnt(species);
                            nnodes++;
                        }

                        // connect it
                        // create PART_OF relationship to previous column
                        nrels += driver.wrapNode(driver.asNodeKey(curTaxEnt.getID())).setPART_OF(driver.asNodeKey(parentNode.getID()));
                        parentNode = curTaxEnt;

                        // iterate through infraranks
                        if(parsedName.getTaxonName().getInfraRanks().size() > 0) {
                            for(int j=0; j<parsedName.getTaxonName().getInfraRanks().size(); j++) {
                                TaxEnt infra = new TaxEnt(parsedName.getTaxonName().truncateAtInfraRank(j + 1));
//                                Log.info("Iterate ", j, ": ", infra.getFullName());
                                infra.setCurrent(true);
                                curTaxEnt = nwd.getSingleTaxEntOrNull(infra, true);
                                if(curTaxEnt == null) {
                                    curTaxEnt = nwd.createTaxEntFromTaxEnt(infra);
                                    nnodes++;
                                }

                                // connect it
                                // create PART_OF relationship to previous column
                                nrels += driver.wrapNode(driver.asNodeKey(curTaxEnt.getID())).setPART_OF(driver.asNodeKey(parentNode.getID()));
                                parentNode = curTaxEnt;
                            }
                        }
//                        Log.info("Finished chain");
                    } else {
                        curTaxEnt = nwd.getSingleTaxEntOrNull(parsedName, true);

                        if (curTaxEnt == null) {    // if node does not exist, add it.
                            curTaxEnt = nwd.createTaxEntFromTaxEnt(parsedName);//   TaxEnt.newFromTaxEnt(FloraOnDriver.this,parsedName);
                            nnodes++;
                        }
                        if (parentNode != null)    // create PART_OF relationship to previous column
                            nrels += driver.wrapNode(driver.asNodeKey(curTaxEnt.getID())).setPART_OF(driver.asNodeKey(parentNode.getID()));

                        parentNode = curTaxEnt;
                    }
                }

                if(curTaxEnt == null) {
                    Log.error("Error in line: " + nrecs);
                    nerrors++;
                    continue;
                }

                if(oldIdColumn != null) { 	// FIXME: OldId
                    String oldId = record.get(oldIdColumn);
                    if(!StringUtils.isStringEmpty(oldId))
                        nwd.updateDocument(driver.asNodeKey(curTaxEnt.getID()), "oldId", (Integer)Integer.parseInt(oldId), true);
                    //curTaxEnt.setOldId(Integer.parseInt(record.get(oldIdColumn)));
                    //curTaxEnt.commit();
                }

                Constants.NativeStatus tmpNS;

                for(Map.Entry<Integer,Territory> terr : territories.entrySet()) {	// bind this taxon with the territories with the given native status
                    String ns = record.get(terr.getKey());
                    if(ns != null && !ns.equals("")) {
                        if(!ns.equalsIgnoreCase("endemic")) {
                            try {
                                tmpNS = Constants.NativeStatus.valueOf(ns.toUpperCase());
                            } catch (IllegalArgumentException e) {
                                tmpNS = Constants.NativeStatus.ERROR;
                            }
                        } else {
                            tmpNS = Constants.NativeStatus.NATIVE;
                            curTaxEnt.setWorldDistributionCompleteness(Constants.WorldNativeDistributionCompleteness.DISTRIBUTION_COMPLETE);
                            nwd.updateTaxEntNode(driver.asNodeKey(curTaxEnt.getID()), curTaxEnt, false);
                        }
//                        Log.info(curTaxEnt.getFullName() + ": " + terr.toString() + tmpNS.toVerboseString());
                        driver.wrapTaxEnt(driver.asNodeKey(curTaxEnt.getID())).setNativeStatus(
                                driver.asNodeKey(terr.getValue().getID()), tmpNS, Constants.OccurrenceStatus.PRESENT,
                                null, null, null, false);
                    }
                }
            }
        } catch (FloraOnException e) {
            e.printStackTrace();
        }
        freader.close();
        System.out.println(String.format("\nRead %d records; created %d nodes and %d relationships. %d severe errors", nrecs, nnodes, nrels, nerrors));
        Map<String,Integer> out=new HashMap<String,Integer>();
        out.put("nrecs", nrecs);
        out.put("nnodes", nnodes);
        out.put("nrels", nrels);
        out.put("nerrors", nerrors);
        return out;
    }

    /**
     * USE uploadTaxonomyListFromStream2 instead.
     * Uploads a tab-separated CSV taxonomy file.
     * The file can have as many columns as needed, the hierarchy goes form left to right.
     * Authority of a name goes in front of the name between braces {}
     * Multiple files can be uploaded, as they are merged. In this case, the <u>last column</u> of the new file
     * <b>must match</b> the <u>orphan nodes</u> in the DB
     * <p>Optionally, the last column may be an ID (if you want to match with old IDs)</p>
     * @param stream
     * @param simulate
     * @return
     * @throws IOException
     * @throws FloraOnException
     * @throws TaxonomyException
     * @throws QueryException
     */
    @Deprecated
    public Map<String,Integer> uploadTaxonomyListFromStream(InputStream stream, boolean simulate) throws IOException, FloraOnException {
        INodeWorker nwd=driver.getNodeWorkerDriver();
        int nnodes=0,nrels=0,nrecs=0;
        Reader freader;

        freader = new InputStreamReader(stream, StandardCharsets.UTF_8);
        Iterator<CSVRecord> records = CSVFormat.EXCEL.withDelimiter('\t').withQuote('"').parse(freader).iterator();
        CSVRecord record=records.next();

        Integer nRankColumns=null;
        Integer oldIdColumn=null;
        String col,name,author;
        Map<Integer,Territory> territories=new HashMap<Integer,Territory>();

        for(int i=0; i<record.size(); i++) {
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
                    tv=nwd.createTerritory(shortName, shortName, Constants.TerritoryTypes.COUNTRY, null, true, null);
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
            TaxEnt curTaxEnt, parentNode;
            TaxEnt parsedName;
            boolean pastspecies;	// true after the column species has passed (so to know when to append names to genus)
            while(records.hasNext()) {
                record = records.next();
                nrecs++;
                if(nrecs % 100==0) {System.out.print(".");System.out.flush();}
                if(nrecs % 1000==0) {System.out.print(nrecs);System.out.flush();}
                parentNode = null;
                curTaxEnt = null;
                pastspecies = false;
                for(int i=0; i<rankNames.length; i++) {
                    try {
                        parsedName=TaxEnt.parse(record.get(i));
                    } catch (DatabaseException e) {
                        // is it an empty cell? skip.
                        continue;
                    }
                    if(rankNames[i].equals("species")) pastspecies=true;
                    // special cases: if species or lower rank, must prepend genus.
                    if(pastspecies)
                        name = parentNode.getName()+" "+(rankNames[i].equals("species") ? "" : (infraRanks.containsKey(rankNames[i]) ? infraRanks.get(rankNames[i]) : rankNames[i])+" ")+parsedName.getName();
                    else
                        name=null;

                    if(pastspecies && parsedName.getAuthor()==null) author=parentNode.getAuthor(); else author=null;

                    if(name != null) parsedName.setName(name);
                    parsedName.setRank(Constants.TaxonRanks.valueOf(rankNames[i].toUpperCase()).getValue());
                    parsedName.setAuthor(author);
                    parsedName.setCurrent(true);

                    curTaxEnt = nwd.getSingleTaxEntOrNull(parsedName, true);

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
                                        , null, null, null, null, null, null)
                                , false);
                        //null, parsedName.getRank(), null, parsedName.getAuthor()!=null ? parsedName.getAuthor() : null, null);
                    }
                    if(parentNode!=null) {	// create PART_OF relationship to previous column
                        nrels+=driver.wrapNode(driver.asNodeKey(curTaxEnt.getID())).setPART_OF(driver.asNodeKey(parentNode.getID()));
                    }
                    parentNode=curTaxEnt;
                }

                if(oldIdColumn != null) { 	// FIXME: OldId
                    nwd.updateDocument(driver.asNodeKey(curTaxEnt.getID()), "oldId", (Integer)Integer.parseInt(record.get(oldIdColumn)), true);
                    //curTaxEnt.setOldId(Integer.parseInt(record.get(oldIdColumn)));
                    //curTaxEnt.commit();
                }

                Constants.NativeStatus tmpNS;

                for(Map.Entry<Integer,Territory> terr : territories.entrySet()) {	// bind this taxon with the territories with the given native status
                    String ns=record.get(terr.getKey());
                    if(ns!=null && !ns.equals("")) {
                        try {
                            tmpNS = Constants.NativeStatus.valueOf(ns.toUpperCase());
                        } catch (IllegalArgumentException e) {
                            tmpNS = Constants.NativeStatus.ERROR;
                        }
                        driver.wrapTaxEnt(driver.asNodeKey(curTaxEnt.getID())).setNativeStatus(
                                driver.asNodeKey(terr.getValue().getID()), tmpNS, Constants.OccurrenceStatus.PRESENT, null, null, null, false);
                    }
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

}
