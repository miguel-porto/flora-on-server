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
import pt.floraon.driver.Constants;
import pt.floraon.driver.FloraOnException;
import pt.floraon.driver.interfaces.IFloraOn;
import pt.floraon.driver.interfaces.INodeWorker;
import pt.floraon.driver.jobs.JobTask;
import pt.floraon.driver.utils.BeanUtils;
import pt.floraon.driver.utils.StringUtils;
import pt.floraon.occurrences.entities.Inventory;
import pt.floraon.occurrences.entities.InventoryList;
import pt.floraon.occurrences.entities.OBSERVED_IN;
import pt.floraon.occurrences.fields.parsers.*;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Processes an occurrence record table or KML, to insert into database.
 * Created by miguel on 15-02-2017.
 */
public class OccurrenceImporterJob implements JobTask {
    private InputStream stream;
    private User user;
    private boolean mainObserver;
    private boolean createTaxa;
    private long nrRecordsRead = 0, nrMatchedInventories = 0;
    private OccurrenceParser occurrenceParser;
    private String format;
    private User owner;
    private InventoryList invList;

    public OccurrenceImporterJob(InputStream stream, IFloraOn driver, User user, String format, boolean mainObserver, Boolean createUsers, boolean createTaxa, User owner) {
        this.stream = stream;
        this.user = user;
        this.format = format == null ? "csv" : format;
        this.mainObserver = mainObserver;
        this.createTaxa = createTaxa;
        this.owner = owner;
        occurrenceParser = new OccurrenceParser(driver);

        occurrenceParser.registerParser("observers", new UserListParser(occurrenceParser.getUserMap(), driver, createUsers));
        occurrenceParser.registerParser("collectors", new UserListParser(occurrenceParser.getUserMap(), driver, createUsers));
        occurrenceParser.registerParser("dets", new UserListParser(occurrenceParser.getUserMap(), driver, createUsers));
    }

    /**
     * @return Who ordered this job
     */
    public User getOwner() {
        return this.owner;
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
                    inv.getUnmatchedOccurrences().add(new OBSERVED_IN(true));
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
        Map<Long,String> lineerrors = new LinkedHashMap<>();
        invList = new InventoryList();
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
                            recordValues.put(col.replaceAll("\t", "").toLowerCase().trim(), record.get(col).replaceAll("\t", ""));

                        occurrenceParser.parseFields(recordValues, inv);
                    } catch (FloraOnException | IllegalArgumentException e) {
                        Log.warn(e.getMessage());
                        lineerrors.put(record.getRecordNumber(), e.getMessage());
                    }
                    nrRecordsRead++;
                    if (nrRecordsRead % 100 == 0) {
                        System.out.print(".");
                        System.out.flush();
                    }
                    if (nrRecordsRead % 1000 == 0) {
                        System.out.print(nrRecordsRead);
                        System.out.flush();
                    }

                    // this groups inventories by coordinates, location and observers
                    invMap.put(inv, inv);
                }
                freader.close();

                // now let's sweep out the species from all inventory groups and aggregate
                for (Map.Entry<Inventory, Collection<Inventory>> entry : invMap.asMap().entrySet()) {
                    // grab an array of Inventory of these Inventories
                    // note that the field 'code', if present, rules over all the others
                    Collection<Inventory> tmpInventories = entry.getValue();
//            List<Inventory> tmp = new ArrayList<>();
//            for(Inventory i : entr.getValue()) {
//                tmp.add(i.getInventoryData());
//            }

                    // merge all the Inventory into one
                    Inventory merged = null;
                    try {
                        // we ignore the field that holds the occurrences
//                        System.out.println(tmpInventories.iterator().next().toJson().toString());
                        merged = BeanUtils.mergeBeans(Inventory.class, Collections.singletonList("unmatchedOccurrences"), "code"
                                , tmpInventories.toArray(new Inventory[0]));
//                        System.out.println("MERG" + merged.toJson().toString());
                    } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | InstantiationException e) {
                        e.printStackTrace();
                        throw new FloraOnException(e.getMessage());
                    } catch (FloraOnException e) {
                        Log.info(e.getMessage());
                    }

                    if(merged == null) {    // could not merge, go on without merging
                        // insert each occurrence in a separate inventory
                        for(Inventory i : entry.getValue()) {
                            if (mainObserver) {
                                Set<String> obs = new LinkedHashSet<>();
                                obs.add(user.getID());
                                obs.addAll(Arrays.asList(i.getObservers()));
                                i.setObservers(obs.toArray(new String[0]));
                            }
                            i.setMaintainer(user.getID());
                            invList.add(i);
                        }
                    } else {
//            System.out.println("MERGE OF "+tmp.size()+" BEANS:");
//            System.out.println(gs.toJson(merged));

                        // assemble all species found in these inventories into the merged one
                        List<OBSERVED_IN> occurrences = new ArrayList<>();
                        for (Inventory inventory : entry.getValue()) {
                            // gather all coordinates of the pre-merged inventory and copy them to the occurrences, if empty.
                            // because the inventory coordinates will be lost.
/*  TODO: IS THIS NEEDED? Perhaps when we have a decent geo index?
                            for(OBSERVED_IN oi1 : inventory.getUnmatchedOccurrences()) {
                                if(oi1.getObservationLatitude() == null)
                                    oi1.setObservationLatitude(inventory.getLatitude());
                                if(oi1.getObservationLongitude() == null)
                                    oi1.setObservationLongitude(inventory.getLongitude());
                            }
*/
                            occurrences.addAll(inventory.getUnmatchedOccurrences());
                        }
                        if (occurrences.size() == 0) occurrences.add(new OBSERVED_IN(true));
/*
                        Set<String> coords = new HashSet<>();
                        for(OBSERVED_IN oi1 : occurrences) {
                            System.out.println(oi1.getObservationLatitude());
                            System.out.println(oi1.getObservationLongitude());
                            if (oi1.getObservationLatitude() != null && oi1.getObservationLongitude() != null) {
                                System.out.println(oi1._getObservationCoordinates());
                                coords.add(oi1._getObservationCoordinates());
                            }
                        }
                        if(coords.size() == 1) {
                            // all the coordinates are the same, so we can remove them all and keep only the inventory's
                            ListIterator<OBSERVED_IN> li = occurrences.listIterator();
                            while(li.hasNext()) {
                                OBSERVED_IN oi2 = li.next();
                                oi2.setObservationLatitude(null);
                                oi2.setObservationLongitude(null);
                                li.set(oi2);
                            }
                        }
*/
                        // Set inventory coordinates to the average of the merged occurrences if the inventory coordinates
                        // are null
                        if(Constants.isNullOrNoData(merged.getLatitude()) || Constants.isNullOrNoData(merged.getLongitude())) {
                            float averageLat = 0, averageLong = 0;
                            int count = 0;
                            for (OBSERVED_IN oi : occurrences) {
                                if (!Constants.isNullOrNoData(oi.getObservationLatitude()) && !Constants.isNullOrNoData(oi.getObservationLongitude())) {
                                    averageLat += oi.getObservationLatitude();
                                    averageLong += oi.getObservationLongitude();
                                    count++;
                                }
                            }
                            if (count > 1) {
                                averageLat = averageLat / count;
                                averageLong = averageLong / count;

                                merged.setLatitude(averageLat);
                                merged.setLongitude(averageLong);
                            }
                        }

                        merged.setUnmatchedOccurrences(occurrences);

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

        for(Inventory inv : invList) {
            driver.getOccurrenceDriver().matchTaxEntNames(inv, createTaxa, false, invList);
            nrMatchedInventories++;
        }

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
        if(nrMatchedInventories > 0)
            return "All records processed. Matching taxon names: " + nrMatchedInventories + " of " + invList.size() + " inventories processed.";
        else
            return nrRecordsRead + " records read.";
    }

    @Override
    public String getDescription() {
        return "Occurrence importer";
    }
}
