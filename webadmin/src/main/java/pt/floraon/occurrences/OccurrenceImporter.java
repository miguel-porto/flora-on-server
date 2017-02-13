package pt.floraon.occurrences;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import pt.floraon.driver.*;
import pt.floraon.occurrences.entities.Inventory;
import pt.floraon.occurrences.fieldparsers.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Imports occurrences from text files.
 * Created by miguel on 08-02-2017.
 */
public class OccurrenceImporter extends BaseFloraOnDriver {
    /**
     * Holds the aliases mappings
     */
    public Map<String, FieldParser> fieldMappings = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    /**
     * Holds a map os user names to DB IDs. This will be updated as needed.
     */
    Map<String, String> userMap = new HashMap<>();

    public OccurrenceImporter(IFloraOn driver) {
        super(driver);
        fieldMappings.put("latitude", new LatitudeLongitudeParser());
        fieldMappings.put("longitude", new LatitudeLongitudeParser());
        fieldMappings.put("taxa", new TaxaParser());
        fieldMappings.put("year", new IntegerParser());
        fieldMappings.put("month", new IntegerParser());
        fieldMappings.put("day", new IntegerParser());
        fieldMappings.put("ano", new AliasFieldParser("year", fieldMappings));
        fieldMappings.put("observers", new UserListParser(userMap, driver));
        fieldMappings.put("collectors", new UserListParser(userMap, driver));
        fieldMappings.put("determiners", new UserListParser(userMap, driver));
    }

    public Map<String, Object> uploadRecordsFromFile(String filename) throws IOException, FloraOnException {
        File file=new File(filename);
        if(!file.canRead()) throw new IOException("Cannot read file "+filename);
        return uploadRecordsFromStream(new FileInputStream(file));
    }

    public Map<String,Object> uploadRecordsFromStream(InputStream stream) throws IOException, FloraOnException {
        INodeWorker nwd = driver.getNodeWorkerDriver();
        Map<String,Object> out = new HashMap<String,Object>();
        Reader freader=null;
        long countupd=0,countnew=0,counterr=0,nrecs=0;
        long newsplist=0;
        long counter=0;
        Map<Long,String> lineerrors=new HashMap<Long,String>();
        System.out.print("Reading records ");

        Gson gs = new GsonBuilder().setPrettyPrinting().create();

        Inventory occ;
//        ObjectOutputStream oost = new ObjectOutputStream(new FileOutputStream("/tmp/exp.ser"));

        try {
            freader = new InputStreamReader(stream, StandardCharsets.UTF_8);
            CSVParser records = CSVFormat.TDF.withQuote('\"').withDelimiter('\t').withHeader().parse(freader);
            Map<String, Integer> headers = records.getHeaderMap();
            Map<String, FieldParser> fieldMappers = new HashMap<>();

            // associate a parser for each table column
            for(Map.Entry<String, Integer> h : headers.entrySet()) {
                if(!fieldMappings.containsKey(h.getKey())) throw new FloraOnException(Messages.getString("error.1",h.getKey()));
                fieldMappers.put(h.getKey(), fieldMappings.get(h.getKey()));
            }

            for (CSVRecord record : records) {
                try {
                    occ = Inventory.fromCSVline(record, fieldMappers, null);
                    nrecs++;
                    if(nrecs % 100==0) {System.out.print(".");System.out.flush();}
                    if(nrecs % 1000==0) {System.out.print(nrecs);System.out.flush();}

                    System.out.println(gs.toJson(occ));

                    //nwd.createOccurrence(occ);
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

        return out;
    }
}
