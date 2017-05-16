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
import pt.floraon.driver.utils.StringUtils;
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
    private boolean mainObserver;
    private boolean createTaxa;
    private long nrecs=0;
    private long newsplist=0;
    private long counter=0;
    private OccurrenceParser occurrenceParser;
    private String format;


    public OccurrenceImporterJob(InputStream stream, IFloraOn driver, User user, String format, boolean mainObserver, Boolean createUsers, boolean createTaxa) {
        this.stream = stream;
        this.user = user;
        this.format = format == null ? "csv" : format;
        this.mainObserver = mainObserver;
        this.createTaxa = createTaxa;
        occurrenceParser = new OccurrenceParser(driver);

        occurrenceParser.registerParser("observers", new UserListParser(occurrenceParser.getUserMap(), driver, createUsers));
        occurrenceParser.registerParser("collectors", new UserListParser(occurrenceParser.getUserMap(), driver, createUsers));
        occurrenceParser.registerParser("determiners", new UserListParser(occurrenceParser.getUserMap(), driver, createUsers));
    }

    /**
     * Recursively process a KML and spit out all placemarks in all folders
     * @param feature
     * @param output
     */
    private void processKMLFeature(List<Feature> feature, List<Inventory> output) {
        for (int i = 0; i < feature.size(); i++) {
            if(Folder.class.isAssignableFrom(feature.get(i).getClass())) {
                Folder folder = (Folder) feature.get(i);
                processKMLFeature(folder.getFeature(), output);
                continue;
            }
            if(Placemark.class.isAssignableFrom(feature.get(i).getClass())) {
                Placemark pm = (Placemark) feature.get(i);
                if(Point.class.isAssignableFrom(pm.getGeometry().getClass())) {
                    Point p = (Point) pm.getGeometry();
                    Inventory inv = new Inventory();
                    inv.setLatitude((float) p.getCoordinates().get(0).getLatitude());
                    inv.setLongitude((float) p.getCoordinates().get(0).getLongitude());
                    inv.setCode(pm.getName());
                    inv.setPubNotes(pm.getDescription());
                    if (mainObserver)
                        inv.setObservers(new String[] {user.getID()});
                    inv.setMaintainer(user.getID());
                    inv.getUnmatchedOccurrences().add(new newOBSERVED_IN(true));
//                    System.out.println(pm.getName()+": "+ p.getCoordinates().get(0).getObservationLatitude()+", "+p.getCoordinates().get(0).getObservationLongitude());
                    output.add(inv);
                } else
                    Log.warn("Skipped non-point placemark in KML");
            }
        }
    }

    @Override
    public void run(IFloraOn driver) throws FloraOnException, IOException {
        INodeWorker nwd = driver.getNodeWorkerDriver();
        Reader freader;
        Map<Long,String> lineerrors = new HashMap<>();
        InventoryList invList = new InventoryList();
        System.out.print("Reading records ");

        Gson gs = new GsonBuilder().setPrettyPrinting().create();

        switch(this.format) {
            case "kml":
                Kml kml = Kml.unmarshal(stream);
                Document document = (Document) kml.getFeature();
                processKMLFeature(document.getFeature(), invList);
                break;

            default:
//                freader = new InputStreamReader(stream, StandardCharsets.UTF_8);
//                CSVParser records = CSVFormat.TDF.withQuote('\"').withDelimiter('\t').withHeader().parse(freader);
                freader = new InputStreamReader(stream, StandardCharsets.ISO_8859_1);
                CSVParser records = CSVFormat.EXCEL.withDelimiter('\t').withHeader().parse(freader);

                Map<String, Integer> headers = records.getHeaderMap();

                try {
                    occurrenceParser.checkFieldNames(headers.keySet());
                } catch (FloraOnException e) {
                    lineerrors.put(0L, e.getMessage());
                }

                Inventory inv;
                Multimap<Inventory, Inventory> invMap = ArrayListMultimap.create();

                for (CSVRecord record : records) {
                    inv = new Inventory();
                    Map<String, String> recordValues = new HashMap<>();
                    try {
                        for (String col : headers.keySet())
                            recordValues.put(col, record.get(col));

                        occurrenceParser.parseFields(recordValues, inv);
                    } catch (FloraOnException | IllegalArgumentException e) {
                        Log.warn(e.getMessage());
                        lineerrors.put(record.getRecordNumber(), e.getMessage());
                    }
                    nrecs++;
                    if (nrecs % 100 == 0) {
                        System.out.print(".");
                        System.out.flush();
                    }
                    if (nrecs % 1000 == 0) {
                        System.out.print(nrecs);
                        System.out.flush();
                    }

                    // this groups inventories by coordinates, location and observers
                    invMap.put(inv, inv);
                    counter++;
                    if ((counter % 2500) == 0) {
                        System.out.println(counter + " records processed.");
                    }
                }
                freader.close();

                // now let's sweep out the species from all inventory groups and aggregate
                for (Map.Entry<Inventory, Collection<Inventory>> entr : invMap.asMap().entrySet()) {
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
                        merged = BeanUtils.mergeBeans(Inventory.class, Arrays.asList("unmatchedOccurrences")
                                , tmp.toArray(new Inventory[tmp.size()]));
                    } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | InstantiationException e) {
                        e.printStackTrace();
                        throw new FloraOnException(e.getMessage());
                    } catch (FloraOnException e) {
                        lineerrors.put(0L, e.getMessage());
                    }

                    if(merged == null) {    // could not merge, go on without merging
                        for(Inventory i : entr.getValue()) {
                            if (mainObserver) {
                                Set<String> obs = new LinkedHashSet<>();
                                obs.add(user.getID());
                                obs.addAll(Arrays.asList(i.getObservers()));
                                i.setObservers(obs.toArray(new String[obs.size()]));
                            }
                            i.setMaintainer(user.getID());
                            invList.add(i);
                        }
                    } else {
//            System.out.println("MERGE OF "+tmp.size()+" BEANS:");
//            System.out.println(gs.toJson(merged));

                        // assemble all species found in these inventories into the merged one
                        List<newOBSERVED_IN> occ = new ArrayList<>();
                        for (Inventory inventory : entr.getValue()) {
                            occ.addAll(inventory.getUnmatchedOccurrences());
                        }
                        if (occ.size() == 0) occ.add(new newOBSERVED_IN(true));
                        merged.setUnmatchedOccurrences(occ);

                        if (mainObserver) {
                            Set<String> obs = new LinkedHashSet<>();
                            obs.add(user.getID());
                            obs.addAll(Arrays.asList(merged.getObservers()));
                            merged.setObservers(obs.toArray(new String[obs.size()]));
                        }
                        merged.setMaintainer(user.getID());
                        invList.add(merged);
                    }
                }
                break;
        }
        Log.info("Matching taxon names");
        driver.getOccurrenceDriver().matchTaxEntNames(invList, createTaxa);

        File temp = File.createTempFile("uploadedtable-",".ser", new File("/tmp"));
        ObjectOutputStream oost = new ObjectOutputStream(new FileOutputStream(temp));
        invList.setFileName(temp.getName());
        invList.setUploadDate();
        Set<String> err = invList.getVerboseErrors();
        for(Map.Entry<Long, String> e : lineerrors.entrySet()) {
            err.add(String.format("L%d: %s", e.getKey(), e.getValue()));
        }

        if(invList.getNoMatches().size() > 0) {
            String msg = createTaxa ? Messages.getString("error.9") : Messages.getString("error.8");
            String taxa = "<i>" + StringUtils.implode("</i>, <i>", invList.getNoMatches().toArray(new String[0])) + "</i>";

            if (createTaxa)
                invList.getVerboseWarnings().add(String.format("%s: %s", msg, taxa));
            else
                err.add(String.format("%s: %s", msg, taxa));
        }

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
