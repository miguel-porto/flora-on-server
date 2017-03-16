package pt.floraon.occurrences;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jline.internal.Log;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import pt.floraon.authentication.entities.User;
import pt.floraon.driver.FloraOnException;
import pt.floraon.driver.IFloraOn;
import pt.floraon.driver.INodeWorker;
import pt.floraon.driver.jobs.JobTask;
import pt.floraon.driver.utils.BeanUtils;
import pt.floraon.occurrences.entities.Inventory;
import pt.floraon.occurrences.entities.InventoryData;
import pt.floraon.occurrences.entities.InventoryList;
import pt.floraon.occurrences.entities.newOBSERVED_IN;
import pt.floraon.occurrences.fieldparsers.*;
import pt.floraon.taxonomy.entities.TaxEnt;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by miguel on 15-02-2017.
 */
public class OccurrenceImporterJob implements JobTask {
    private InputStream stream;
    private User user;
    private long countupd=0,countnew=0,counterr=0,nrecs=0;
    private long newsplist=0;
    private long counter=0;

    /**
     * Holds the aliases mappings
     */
    public Map<String, FieldParser> fieldMappings = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    /**
     * Holds a map os user names to DB IDs. This will be updated as needed.
     */
    Map<String, String> userMap = new HashMap<>();

    public OccurrenceImporterJob(InputStream stream, IFloraOn driver, User user) {
        this.stream = stream;
        this.user = user;
        fieldMappings.put("latitude", new LatitudeLongitudeParser());
        fieldMappings.put("longitude", new LatitudeLongitudeParser());
        fieldMappings.put("taxa", new TaxaParser());
        fieldMappings.put("year", new IntegerParser());
        fieldMappings.put("month", new IntegerParser());
        fieldMappings.put("day", new IntegerParser());
        fieldMappings.put("code", new PlainTextParser());
        fieldMappings.put("ano", new AliasFieldParser("year", fieldMappings));
        fieldMappings.put("código", new AliasFieldParser("code", fieldMappings));
        fieldMappings.put("observers", new UserListParser(userMap, driver));
        fieldMappings.put("collectors", new UserListParser(userMap, driver));
        fieldMappings.put("determiners", new UserListParser(userMap, driver));
    }

    @Override
    public void run(IFloraOn driver) throws FloraOnException, IOException {
        INodeWorker nwd = driver.getNodeWorkerDriver();
        Reader freader;
        Map<Long,String> lineerrors=new HashMap<Long,String>();
        System.out.print("Reading records ");

        Gson gs = new GsonBuilder().setPrettyPrinting().create();

        freader = new InputStreamReader(stream, StandardCharsets.UTF_8);
        CSVParser records = CSVFormat.TDF.withQuote('\"').withDelimiter('\t').withHeader().parse(freader);
        Map<String, Integer> headers = records.getHeaderMap();

        for(Map.Entry<String, Integer> h : headers.entrySet()) {
            if(!fieldMappings.containsKey(h.getKey())) throw new FloraOnException(Messages.getString("error.1", h.getKey()));
        }

        Inventory inv;
        Multimap<Inventory, Inventory> invMap = ArrayListMultimap.create();

        for (CSVRecord record : records) {
            inv = new Inventory();
            try {
                for(String col : headers.keySet()) {
                    fieldMappings.get(col).parseValue(record.get(col), col, inv);
                }
                nrecs++;
                if(nrecs % 100==0) {System.out.print(".");System.out.flush();}
                if(nrecs % 1000==0) {System.out.print(nrecs);System.out.flush();}

                // this groups inventories by coordinates, location and observers
                invMap.put(inv, inv);

                //nwd.createOccurrence(occ);
            } catch(FloraOnException | IllegalArgumentException e) {
                lineerrors.put(record.getRecordNumber(), e.getMessage());
                counterr++;
                continue;
            }
            counter++;
            if((counter % 2500)==0) {
                System.out.println(counter+" records processed.");
            }
        }
        freader.close();

        // now let's sweep out the species from all inventory groups and aggregate
        InventoryList invList = new InventoryList();

        for(Map.Entry<Inventory, Collection<Inventory>> entr : invMap.asMap().entrySet()) {
            // grab an array of InventoryData of these Inventories
            List<InventoryData> tmp = new ArrayList<>();
            for(Inventory i : entr.getValue()) {
                tmp.add(i.getInventoryData());
            }

            // merge all the InventoryData into one
            InventoryData merged = null;
            try {
                merged = BeanUtils.mergeBeans(InventoryData.class, tmp.toArray(new InventoryData[tmp.size()]));
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | InstantiationException e) {
                e.printStackTrace();
            } catch (FloraOnException e) {
                throw new FloraOnException(Messages.getString("error.3"));
            }

//            System.out.println("MERGE OF "+tmp.size()+" BEANS:");
//            System.out.println(gs.toJson(merged));

            inv = new Inventory();
            inv.setInventoryData(merged);
            // assemble all species found in these inventories into the merged one
            inv.setObservedIn(new ArrayList<newOBSERVED_IN>());
            for (Inventory inventory : entr.getValue()) {
                inv.getObservedIn().addAll(inventory.getObservedIn());
            }
            invList.add(inv);
        }

//        System.out.println(gs.toJson(invList));
        Log.info("Matching taxon names");

        for(Inventory i : invList) {
            for(newOBSERVED_IN oi : i.getObservedIn()) {
                TaxEnt te, matched;
                try {
                    te = TaxEnt.parse(oi.getVerbTaxon());
                } catch (FloraOnException e) {
                    invList.addParseError(oi);
                    continue;
                }
                Log.info("    Verbose name: "+ oi.getVerbTaxon());
                Log.info("    Parsed name: "+ te.getFullName(false));
                matched = nwd.getTaxEnt(te);
                if(matched == null) {
                    Log.warn("    No match");
                    invList.addNoMatch(oi);
                } else
                    Log.info("    Matched name: "+ matched.getFullName(false), " -- ", matched.getID());
            }
        }

        File temp = File.createTempFile("uploadedtable-",".ser", new File("/tmp"));
        ObjectOutputStream oost = new ObjectOutputStream(new FileOutputStream(temp));
        oost.writeObject(invList);
        oost.close();
        nwd.addUploadedTableToUser(temp.getName(), driver.asNodeKey(user.getID()));
    }

    @Override
    public String getState() {
        return "Nº records processed: " + nrecs;
    }
}
