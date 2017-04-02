package pt.floraon.occurrences;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.micromata.opengis.kml.v_2_2_0.*;
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
import pt.floraon.occurrences.entities.InventoryList;
import pt.floraon.occurrences.entities.newOBSERVED_IN;
import pt.floraon.occurrences.fieldparsers.*;
import pt.floraon.taxonomy.entities.TaxEnt;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Created by miguel on 15-02-2017.
 */
public class OccurrenceImporterJob implements JobTask {
    private InputStream stream;
    private User user;
    private long countupd=0,countnew=0,counterr=0,nrecs=0;
    private long newsplist=0;
    private long counter=0;
    private OccurrenceParser occurrenceParser;
    private String format;


    public OccurrenceImporterJob(InputStream stream, IFloraOn driver, User user, String format) {
        this.stream = stream;
        this.user = user;
        this.format = format == null ? "csv" : format;
        occurrenceParser = new OccurrenceParser(driver);
    }

    @Override
    public void run(IFloraOn driver) throws FloraOnException, IOException {
        INodeWorker nwd = driver.getNodeWorkerDriver();
        Reader freader;
        Map<Long,String> lineerrors=new HashMap<Long,String>();
        InventoryList invList = new InventoryList();
        System.out.print("Reading records ");

        Gson gs = new GsonBuilder().setPrettyPrinting().create();

        switch(this.format) {
            case "kml":
                Kml kml = Kml.unmarshal(stream);
                Document document = (Document) kml.getFeature();
                for (int i = 0; i < document.getFeature().size(); i++) {
                    Placemark pm = (Placemark) document.getFeature().get(i);
                    Point p = (Point) pm.getGeometry();
                    Inventory inv = new Inventory();
                    inv.setLatitude((float) p.getCoordinates().get(0).getLatitude());
                    inv.setLongitude((float) p.getCoordinates().get(0).getLongitude());
                    inv.setCode(pm.getName());
                    inv.setPubNotes(pm.getDescription());
                    inv.setMaintainer(user.getID());
                    inv.getUnmatchedOccurrences().add(new newOBSERVED_IN());

                    invList.add(inv);
//                    System.out.println(pm.getName()+": "+ p.getCoordinates().get(0).getLatitude()+", "+p.getCoordinates().get(0).getLongitude());
                }
                break;

            default:
                freader = new InputStreamReader(stream, StandardCharsets.UTF_8);
                CSVParser records = CSVFormat.TDF.withQuote('\"').withDelimiter('\t').withHeader().parse(freader);
                Map<String, Integer> headers = records.getHeaderMap();

                occurrenceParser.checkFieldNames(headers.keySet());

                Inventory inv;
                Multimap<Inventory, Inventory> invMap = ArrayListMultimap.create();

                for (CSVRecord record : records) {
                    inv = new Inventory();
                    Map<String, String> recordValues = new HashMap<>();
                    try {
                        for(String col : headers.keySet())
                            recordValues.put(col, record.get(col));

                        occurrenceParser.parseFields(recordValues, inv);
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
                for(Map.Entry<Inventory, Collection<Inventory>> entr : invMap.asMap().entrySet()) {
                    // grab an array of Inventory of these Inventories
                    Collection<Inventory> tmp = entr.getValue();
//            List<Inventory> tmp = new ArrayList<>();
//            for(Inventory i : entr.getValue()) {
//                tmp.add(i.getInventoryData());
//            }

                    // merge all the Inventory into one
                    Inventory merged = null;
                    try {
                        // we ignore the field that holds the occurrences
                        merged = BeanUtils.mergeBeans(Inventory.class, Arrays.asList("unmatchedOccurrences"), tmp.toArray(new Inventory[tmp.size()]));
                    } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | InstantiationException e) {
                        e.printStackTrace();
                        throw new FloraOnException(e.getMessage());
                    } catch (FloraOnException e) {
                        throw new FloraOnException(Messages.getString("error.3"));
                    }

//            System.out.println("MERGE OF "+tmp.size()+" BEANS:");
//            System.out.println(gs.toJson(merged));

                    // assemble all species found in these inventories into the merged one
                    List<newOBSERVED_IN> occ = new ArrayList<>();
                    for (Inventory inventory : entr.getValue()) {
                        occ.addAll(inventory.getUnmatchedOccurrences());
                    }
                    if(occ.size() == 0) occ.add(new newOBSERVED_IN());
                    merged.setUnmatchedOccurrences(occ);
                    merged.setMaintainer(user.getID());
                    invList.add(merged);
/*
            inv = new Inventory();
            inv.setInventoryData(merged);
            // assemble all species found in these inventories into the merged one
            inv.setObservedIn(new ArrayList<newOBSERVED_IN>());
            for (Inventory inventory : entr.getValue()) {
                inv.getObservedIn().addAll(inventory.getObservedIn());
            }
            invList.add(inv);
*/
                }
                break;
        }

//        System.out.println(gs.toJson(invList));
        Log.info("Matching taxon names");
        driver.getOccurrenceDriver().matchTaxEntNames(invList);

/*
        for(Inventory i : invList) {
            for(newOBSERVED_IN oi : i.getUnmatchedOccurrences()) {
                TaxEnt te, matched;
                Log.info("Verbose name: "+ oi.getVerbTaxon());
                try {
                    te = TaxEnt.parse(oi.getVerbTaxon());
                } catch (FloraOnException e) {
                    invList.addParseError(oi);
                    continue;
                }
                Log.info("    Parsed name: "+ te.getFullName(false));
                matched = nwd.getTaxEnt(te);
                if(matched == null) {
                    Log.warn("    No match");
                    invList.addNoMatch(oi);
                } else {
                    Log.info("    Matched name: " + matched.getFullName(false), " -- ", matched.getID());
                    oi.setTaxEntMatch(matched.getID());
                }
            }
        }
*/

        File temp = File.createTempFile("uploadedtable-",".ser", new File("/tmp"));
        ObjectOutputStream oost = new ObjectOutputStream(new FileOutputStream(temp));
        invList.setFileName(temp.getName());
        invList.setUploadDate();
        oost.writeObject(invList);
        oost.close();
        nwd.addUploadedTableToUser(temp.getName(), driver.asNodeKey(user.getID()));
    }

    @Override
    public String getState() {
        return "Nº records processed: " + nrecs;
    }

    @Override
    public String getDescription() {
        return "Occurrence importer";
    }
}
