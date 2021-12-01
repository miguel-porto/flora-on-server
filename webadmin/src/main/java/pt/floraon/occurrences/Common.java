package pt.floraon.occurrences;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.openhtmltopdf.swing.NaiveUserAgent;
import jline.internal.Log;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang.mutable.MutableInt;
import pt.floraon.authentication.entities.User;
import pt.floraon.driver.Constants;
import pt.floraon.driver.FloraOnException;
import pt.floraon.driver.interfaces.IFloraOn;
import pt.floraon.driver.interfaces.ITaxEntWrapper;
import pt.floraon.driver.results.InferredStatus;
import pt.floraon.driver.utils.StringUtils;
import pt.floraon.geometry.CoordinateConversion;
import pt.floraon.geometry.Point2D;
import pt.floraon.geometry.UTMCoordinate;
import pt.floraon.geometry.gridmaps.Square;
import pt.floraon.occurrences.entities.*;
import pt.floraon.redlistdata.entities.RedListDataEntity;
import pt.floraon.taxonomy.entities.TaxEnt;
import pt.floraon.taxonomy.entities.TaxEntMatch;

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Created by miguel on 23-03-2017.
 */
public final class Common {
    public static InventoryList readInventoryListFromFile(String fileName) throws IOException, ClassNotFoundException {
        ObjectInputStream oist;
        InventoryList invList;

        File f = new File("/tmp/" + fileName);
        if (f.canRead()) {
            Log.info("Read " + f.getName());
            oist = new ObjectInputStream(new FileInputStream(f));
            invList = (InventoryList) oist.readObject();
            Log.info(invList.size());
            oist.close();
            return invList;
        } else throw new IOException("File not found.");
    }

    public static int levenshteinDistance(String lhs, String rhs) {
        if (rhs == null || lhs == null) return 1000;
        // dashes and spaces mean the same in locality names
/*
        rhs = rhs.replace('-', ' ');
        lhs = lhs.replace('-', ' ');
*/

        int len0 = lhs.length() + 1;
        int len1 = rhs.length() + 1;

        // the array of distances
        int[] cost = new int[len0];
        int[] newcost = new int[len0];

        // initial cost of skipping prefix in String s0
        for (int i = 0; i < len0; i++) cost[i] = i;

        // dynamically computing the array of distances

        // transformation cost for each letter in s1
        for (int j = 1; j < len1; j++) {
            // initial cost of skipping prefix in String s1
            newcost[0] = j;

            // transformation cost for each letter in s0
            for (int i = 1; i < len0; i++) {
                // matching current letters in both strings
                int match = (lhs.charAt(i - 1) == rhs.charAt(j - 1)) ? 0 : 1;

                // computing cost for each transformation
                int cost_replace = cost[i - 1] + match;
                int cost_insert = cost[i] + 1;
                int cost_delete = newcost[i - 1] + 1;

                // keep minimum cost
                newcost[i] = Math.min(Math.min(cost_insert, cost_delete), cost_replace);
            }

            // swap cost/newcost arrays
            int[] swap = cost;
            cost = newcost;
            newcost = swap;
        }

        // the distance is the cost for transforming all letters in both strings
        return cost[len0 - 1];
    }


    public static void exportInventoriesToPDF(Iterator<Inventory> inventoryIterator, String baseUri, OutputStream stream) throws UnsupportedEncodingException {
        List<String> ids = new ArrayList<>();
        while (inventoryIterator.hasNext())
            ids.add(inventoryIterator.next().getID());

        if (ids.size() == 0) return;

        String idsString = StringUtils.implode(",", ids.toArray(new String[0]));

        PdfRendererBuilder builder = new PdfRendererBuilder();
        final NaiveUserAgent.DefaultUriResolver defaultUriResolver = new NaiveUserAgent.DefaultUriResolver();

        builder.useUriResolver(defaultUriResolver);
        builder.withUri(baseUri + URLEncoder.encode(idsString, StandardCharsets.UTF_8.toString()));
        builder.toStream(stream);
        try {
            builder.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void exportTaxonListToCSV(Iterator<Map.Entry<TaxEnt, OBSERVED_IN_summary>> speciesIterator, Writer stream, IFloraOn driver)
            throws IOException, FloraOnException {
        CSVPrinter csv = new CSVPrinter(stream, CSVFormat.EXCEL);
        exportTaxonListHeaderToCSV(csv);

        while (speciesIterator.hasNext()) {
            Map.Entry<TaxEnt, OBSERVED_IN_summary> ent = speciesIterator.next();
            RedListDataEntity rlde = null;
            ITaxEntWrapper tew = null;
            String family = null;
            TaxEnt accepted;
            if(ent.getKey() != null) {
                try {
                    rlde = driver.getRedListData().getRedListDataEntity(driver.getDefaultRedListTerritory(), driver.asNodeKey(ent.getKey().getID()));
                    tew = driver.wrapTaxEnt(driver.asNodeKey(ent.getKey().getID()));
                    Iterator<TaxEnt> itHigher = driver.getQueryDriver().getHigherTaxonomy(new String[]{ent.getKey().getID()}, Constants.TaxonRanks.FAMILY);
                    if (itHigher.hasNext()) {
                        TaxEnt tmp = itHigher.next();
                        family = tmp == null ? null : tmp.getName();
                    }

                } catch (FloraOnException e) { }
                Iterator<TaxEntMatch> tem = driver.getQueryDriver().getFirstAcceptedTaxonContaining(new String[]{ent.getKey().getID()});
                accepted = tem.hasNext() ? tem.next().getMatchedTaxEnt() : null;
            } else accepted = null;

            exportTaxonToCSV(csv, accepted, rlde, tew, family, ent.getValue());
        }
        csv.close();
    }

    /**
     * The main function for exporting any occurrence set to stream, as CSV.
     * @param occurrenceIterator
     * @param stream
     * @param driver
     * @throws IOException
     * @throws FloraOnException
     */
    public static void exportOccurrencesToCSV(Iterator<Occurrence> occurrenceIterator, Writer stream, IFloraOn driver)
            throws IOException, FloraOnException {
        CSVPrinter csv = new CSVPrinter(stream, CSVFormat.EXCEL);
        ITaxEntWrapper tew;
        Map<String, InferredStatus> inferredStatusMap = new HashMap<>();
        Map<String, String[]> endemismDegreeMap = new HashMap<>();
        Map<String, TaxEnt[]> higherTaxonomyMap = new HashMap<>();
        InferredStatus inferredStatus;
        String[] endemismDegree;
        TaxEnt[] higherTaxonomy;

        exportOccurrenceHeaderToCSV(csv);
        Map<String, TaxEnt> acceptedTaxa = new HashMap<>();
        Map<String, String> userMap = new HashMap<>();
        TaxEnt tmp = null;
        if(driver != null) {
            // build the user map
            List<User> allUsers = driver.getAdministration().getAllUsers(false);
            for (User u : allUsers)
                userMap.put(u.getID(), u.getName());
        }

        while (occurrenceIterator.hasNext()) {
            Occurrence i2 = occurrenceIterator.next();
            // if driver is provided, fetch endemism degree and accepted taxon
            if(driver != null) {
                String taxEntId = i2.getOccurrence().getTaxEntMatch();
                // fill in accepted taxa
                if (acceptedTaxa.containsKey(taxEntId))
                    tmp = acceptedTaxa.get(i2.getOccurrence().getTaxEntMatch());
                else {
                    Iterator<TaxEntMatch> tem =
                            driver.getQueryDriver().getFirstAcceptedTaxonContaining(new String[] {i2.getOccurrence().getTaxEntMatch()});
                    tmp = tem.hasNext() ? tem.next().getMatchedTaxEnt() : null;
                    if(tmp != null)
                        acceptedTaxa.put(i2.getOccurrence().getTaxEntMatch(), tmp);
                }
                if(inferredStatusMap.containsKey(taxEntId)) {
                    inferredStatus = inferredStatusMap.get(taxEntId);
                    endemismDegree = endemismDegreeMap.get(taxEntId);
                    higherTaxonomy = higherTaxonomyMap.get(taxEntId);
                } else {
                    try {
                        tew = driver.wrapTaxEnt(driver.asNodeKey(taxEntId));
                        inferredStatus = tew.getInferredNativeStatus("lu");   // TODO user config
                        inferredStatusMap.put(taxEntId, inferredStatus);
                        endemismDegree = tew.getEndemismDegree();
                        endemismDegreeMap.put(taxEntId, endemismDegree);
                        higherTaxonomy = driver.getQueryDriver().getHigherTaxonomy(new String[] {taxEntId}).next();
                        higherTaxonomyMap.put(taxEntId, higherTaxonomy);
                    } catch (FloraOnException e) {
                        inferredStatus = null;
                        endemismDegree = null;
                        higherTaxonomy = null;
                    }
                }
            } else {
                inferredStatus = null;
                endemismDegree = null;
                higherTaxonomy = null;
            }
            exportOccurrenceToCSV(i2, csv, tmp, userMap, null, inferredStatus, endemismDegree, higherTaxonomy);
        }
        csv.close();
    }

    /**
     * The main function for exporting inventory summary table as CSV
     * @param inventoryIterator
     * @param stream
     * @param driver
     * @throws IOException
     * @throws FloraOnException
     */
    public static void exportInventoriesToCSV(Iterator<Inventory> inventoryIterator, Writer stream, IFloraOn driver)
            throws IOException, FloraOnException {
        CSVPrinter csv = new CSVPrinter(stream, CSVFormat.EXCEL);
        exportInventoryHeaderToCSV(csv);
        Map<String, TaxEnt> acceptedTaxa = new HashMap<>();
        Map<String, String> userMap = new HashMap<>();
        if(driver != null) {
            // build the user map
            List<User> allUsers = driver.getAdministration().getAllUsers(false);
            for (User u : allUsers)
                userMap.put(u.getID(), u.getName());
        }

        while (inventoryIterator.hasNext()) {
            Inventory i2 = inventoryIterator.next();
            exportInventoryToCSV(i2, csv, userMap);
        }
        csv.close();
    }

    public static void exportOccurrenceHeaderToCSV(CSVPrinter csv) throws IOException {
        final List<String> fields = new ArrayList<>(Arrays.asList("source", "taxa", "taxaCanonical", "taxonFull", "acceptedTaxon", "verbTaxa", "higherTaxonomy", "confidence", "excludeReason"
                , "phenoState", "naturalization", "date", "observers", "collectors", "latitude", "longitude", "utmZone", "utmX", "utmY"
                , "precision", "mgrs", "WKT", "locality", "verbLocality", "code", "abundance", "method", "cover", "photo", "collected"
                , "specificThreats", "habitat", "comment", "privateComment", "inventoryComment", "year", "month", "day", "dateInserted", "uuid", "accession"
                , "credits", "maintainer", "maintainerName", "redListCategory", "URL", "Endemic to", "Native status in Lu"));
        csv.printRecord(fields);
    }

    public static void exportInventoryHeaderToCSV(CSVPrinter csv) throws IOException {
        final List<String> fields = new ArrayList<>(Arrays.asList("source", "taxa"
                , "date", "observers", "collectors", "latitude", "longitude", "utmZone", "utmX", "utmY"
                , "precision", "mgrs", "WKT", "locality", "verbLocality", "code"
                , "threats", "habitat", "privNotes", "inventoryComment", "year", "month", "day"
                , "credits", "maintainer", "maintainerName"));
        csv.printRecord(fields);
    }

    public static void exportTaxonListHeaderToCSV(CSVPrinter csv) throws IOException {
        final List<String> fields = new ArrayList<>(Arrays.asList("Used names", "Accepted Taxon", "Family", "Nr. of occurrences", "Max confidence"
                , "Red List Category", "Legal Protection", "Endemic to", "Native status in Lu"));
        csv.printRecord(fields);
    }

    public static void exportOccurrenceToCSV(Occurrence occurrence, CSVPrinter csv, TaxEnt acceptedTaxEnt,
                                             Map<String, String> userMap, RedListDataEntity rlde, InferredStatus ist,
                                             String[] endemismDegree, TaxEnt[] higherTaxonomy)
            throws IOException, FloraOnException {
        // TODO use field annotations
        OBSERVED_IN oi = occurrence.getOccurrence();
        UTMCoordinate utm = occurrence.hasCoordinate()
                ? CoordinateConversion.LatLonToUtmWGS84(occurrence._getLatitude(), occurrence._getLongitude(), 0)
                : null;
//                    TaxEnt te = oi.getTaxEnt();
        String MGRS, WKT;
        try {
            MGRS = CoordinateConversion.LatLongToMGRS(occurrence._getLatitude(), occurrence._getLongitude(), 1000);
        } catch (IllegalArgumentException e) {
            MGRS = "<invalid>";
        }
        if (utm != null) {
            Point2D tmp1 = new Point2D(utm.getX(), utm.getY());
            WKT = new Square(tmp1, 10000).toWKT();
        } else WKT = "<invalid>";

        StringBuilder higherTaxonomyString = new StringBuilder();
        if(higherTaxonomy != null && higherTaxonomy.length > 1) {
            for(int i=higherTaxonomy.length - 1; i>=1; i--)
                higherTaxonomyString.append(">").append(higherTaxonomy[i].getName());
        } else higherTaxonomyString.append(" ");

        csv.printRecord(
                occurrence.getSource()
                , oi.getTaxEnt() == null ? "" : oi.getTaxEnt().getFullName()
                , oi.getTaxEnt() == null ? "" : oi.getTaxEnt().getCanonicalName().toString()
                , oi.getTaxEnt() == null ? "" : oi.getTaxEnt().getNameWithAnnotationOnly(false)
                , acceptedTaxEnt == null ? "" : acceptedTaxEnt.getNameWithAnnotationOnly(false)
                , oi.getVerbTaxon()
                , higherTaxonomyString.substring(1).toString()
                , oi.getConfidence()
                , oi.getPresenceStatus()
                , oi.getPhenoState()
                , oi.getNaturalization()
                , occurrence._getDateYMD()
//                , StringUtils.implode(", ", occurrence._getObserverNames())
                , userMap == null || StringUtils.isArrayEmpty(occurrence.getObservers()) ? StringUtils.implode(", ", occurrence._getObserverNames()) : StringUtils.implode(", ", userMap, occurrence.getObservers())
                , StringUtils.implode(", ", userMap, occurrence.getCollectors())
                , occurrence._getLatitude(), occurrence._getLongitude()
                , utm == null ? "" : ((Integer) utm.getXZone()).toString() + utm.getYZone()
                , utm == null ? "" : utm.getX()
                , utm == null ? "" : utm.getY()
                , occurrence.getPrecision()
                , MGRS, WKT
                , occurrence.getLocality()
                , occurrence.getVerbLocality(), occurrence.getCode()
                , oi.getAbundance(), oi.getTypeOfEstimate(), oi.getCover(), oi.getHasPhoto(), oi.getHasSpecimen()
                , oi.getSpecificThreats(), occurrence.getHabitat(), oi.getComment(), oi.getPrivateComment(), occurrence.getPubNotes()
                , occurrence.getYear(), occurrence.getMonth(), occurrence.getDay()
                , oi.getDateInserted() == null ? "" : Constants.dateFormatYMDHM.get().format(oi.getDateInserted())
                , oi.getUuid().toString()
                , oi.getAccession()
                , occurrence.getCredits()
                , occurrence.getMaintainer()
                , occurrence.getMaintainer() == null || userMap == null ? "" : (userMap.get(occurrence.getMaintainer()) == null ? "" : occurrence._getMaintainerName())
                , (rlde == null || rlde.getAssessment().getFinalCategory() == null) ? "" : rlde.getAssessment().getFinalCategory().getLabel()
                , occurrence.getOccurrence().getUri()
                , endemismDegree == null ? "" : StringUtils.implode(" ", endemismDegree)
                , ist == null ? "" : (ist.getNativeStatus() + (ist.isEndemic() ? " (ENDEMIC)" : ""))
//                , occurrence._getMaintainerName()
        );
    }

    public static void exportInventoryToCSV(Inventory inventory, CSVPrinter csv, Map<String, String> userMap) throws IOException {
        Float lat, lng = null;
        UTMCoordinate utm = (lat = inventory._getLatitude()) != null && (lng = inventory._getLongitude()) != null
                ? CoordinateConversion.LatLonToUtmWGS84(lat, lng, 0)
                : null;
        String MGRS, WKT;
        try {
            MGRS = CoordinateConversion.LatLongToMGRS(lat, lng, 1000);
        } catch (IllegalArgumentException e) {
            MGRS = "<invalid>";
        }
        if (utm != null) {
            Point2D tmp1 = new Point2D(utm.getX(), utm.getY());
            WKT = new Square(tmp1, 10000).toWKT();
        } else WKT = "<invalid>";

        csv.printRecord(
                inventory.getSource()
                , inventory._getSampleTaxa(200, false, true)
                , inventory._getDateYMD()
                , userMap == null || StringUtils.isArrayEmpty(inventory.getObservers()) ? StringUtils.implode(", ", inventory._getObserverNames()) : StringUtils.implode(", ", userMap, inventory.getObservers())
                , StringUtils.implode(", ", userMap, inventory.getCollectors())
                , lat, lng
                , utm == null ? "" : ((Integer) utm.getXZone()).toString() + utm.getYZone()
                , utm == null ? "" : utm.getX()
                , utm == null ? "" : utm.getY()
                , inventory.getPrecision()
                , MGRS, WKT
                , inventory.getLocality()
                , inventory.getVerbLocality(), inventory.getCode()
                , inventory.getThreats(), inventory.getHabitat(), inventory.getPrivNotes(), inventory.getPubNotes()
                , inventory.getYear(), inventory.getMonth(), inventory.getDay()
                , inventory.getCredits()
                , inventory.getMaintainer()
                , inventory.getMaintainer() == null || userMap == null ? "" : (userMap.get(inventory.getMaintainer()) == null ? "" : inventory._getMaintainerName())
        );
    }

    public static void exportTaxonToCSV(CSVPrinter csv, TaxEnt acceptedTaxEnt, RedListDataEntity rlde, ITaxEntWrapper tew, String family, OBSERVED_IN_summary number) throws IOException, FloraOnException {
        InferredStatus ist;
        csv.printRecord(
//                taxon == null ? "" : taxon.getFullName()
                number.getNames()
                , acceptedTaxEnt == null ? "" : acceptedTaxEnt.getFullName()
                , family
                , number.count
                , number.maxConfidence
                , (rlde == null || rlde.getAssessment().getFinalCategory() == null) ? "" : rlde.getAssessment().getFinalCategory().getLabel()
                , (rlde == null || rlde.getConservation() == null) ? "" : StringUtils.implode(", ", rlde.getConservation().getLegalProtection())
                , tew == null ? "" : StringUtils.implode(" ", tew.getEndemismDegree())
                , tew == null ? "" : (ist = tew.getInferredNativeStatus("lu")) == null ? "" : (ist.getNativeStatus() + (ist.isEndemic() ? " (ENDEMIC)" : ""))
        );
    }
}