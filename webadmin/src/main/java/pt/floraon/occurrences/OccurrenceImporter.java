package pt.floraon.occurrences;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import pt.floraon.driver.BaseFloraOnDriver;
import pt.floraon.driver.FloraOnException;
import pt.floraon.driver.IFloraOn;
import pt.floraon.driver.INodeWorker;
import pt.floraon.occurrences.entities.newOccurrence;
import pt.floraon.occurrences.fieldmappers.AliasFieldParser;
import pt.floraon.occurrences.fieldmappers.FieldParser;
import pt.floraon.occurrences.fieldmappers.LatitudeLongitudeParser;
import pt.floraon.occurrences.fieldmappers.TaxaParser;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Imports occurrences from text files.
 * Created by miguel on 08-02-2017.
 */
public class OccurrenceImporter extends BaseFloraOnDriver {
    /**
     * Holds the aliases mappings
     */
    public static Map<String, FieldParser> fieldMappings = new HashMap<>();

    static {
        fieldMappings.put("latitude", new LatitudeLongitudeParser());
        fieldMappings.put("longitude", new LatitudeLongitudeParser());
        fieldMappings.put("taxa", new TaxaParser());
        fieldMappings.put("lat", new AliasFieldParser("latitude", fieldMappings));
    }

    public OccurrenceImporter(IFloraOn driver) {
        super(driver);
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

        newOccurrence occ;
        try {
            freader = new InputStreamReader(stream, StandardCharsets.UTF_8);
            CSVParser records = CSVFormat.TDF.withQuote('\"').withDelimiter('\t').withHeader().parse(freader);
            Map<String, Integer> headers = records.getHeaderMap();
            Map<String, FieldParser> fieldMappers = new HashMap<>();

            for(Map.Entry<String, Integer> h : headers.entrySet()) {
                if(!fieldMappings.containsKey(h.getKey())) throw new FloraOnException("Cannot recognize field named '" + h.getKey() +"'");
                // TODO aliases
                if(FieldParser.class.isAssignableFrom(fieldMappings.get(h.getKey()).getClass()))
                    fieldMappers.put(h.getKey(), (FieldParser) fieldMappings.get(h.getKey()));
            }

            for (CSVRecord record : records) {
                try {
                    occ = newOccurrence.fromCSVline(record, fieldMappers);
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
