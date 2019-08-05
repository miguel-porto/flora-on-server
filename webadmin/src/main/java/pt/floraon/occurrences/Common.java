package pt.floraon.occurrences;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.openhtmltopdf.swing.NaiveUserAgent;
import jline.internal.Log;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import pt.floraon.driver.Constants;
import pt.floraon.driver.FloraOnException;
import pt.floraon.driver.interfaces.IFloraOn;
import pt.floraon.driver.utils.StringUtils;
import pt.floraon.geometry.CoordinateConversion;
import pt.floraon.occurrences.entities.*;
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

    public static void exportOccurrencesToCSV(Iterator<Occurrence> occurrenceIterator, Writer stream, IFloraOn driver)
            throws IOException, FloraOnException {
        CSVPrinter csv = new CSVPrinter(stream, CSVFormat.EXCEL);
//                csv.printRecord("gpsCode", "verbLocality", "latitude", "longitude", "mgrs", "date", "taxa", "comment", "privateNote");
        exportOccurrenceHeaderToCSV(csv);
        Map<String, TaxEnt> acceptedTaxa = new HashMap<>();
        TaxEnt tmp = null;
        while (occurrenceIterator.hasNext()) {
            Occurrence i2 = occurrenceIterator.next();
            if(driver != null) {
                if (acceptedTaxa.containsKey(i2.getOccurrence().getTaxEntMatch()))
                    tmp = acceptedTaxa.get(i2.getOccurrence().getTaxEntMatch());
                else {
                    Iterator<TaxEntMatch> tem =
                            driver.getQueryDriver().getFirstAcceptedTaxonContaining(new String[] {i2.getOccurrence().getTaxEntMatch()});
                    tmp = tem.hasNext() ? tem.next().getMatchedTaxEnt() : null;
                    if(tmp != null)
                        acceptedTaxa.put(i2.getOccurrence().getTaxEntMatch(), tmp);
                }
            }
            exportOccurrenceToCSV(i2, csv, tmp);
        }
        csv.close();
    }

    public static void exportOccurrenceHeaderToCSV(CSVPrinter csv) throws IOException {
        csv.printRecord("source", "taxa", "taxaCanonical", "acceptedTaxon", "verbTaxa", "confidence", "excludeReason", "phenoState", "date", "observers", "latitude", "longitude"
                , "precision", "mgrs", "verbLocality", "code", "abundance", "method", "cover", "photo", "collected"
                , "specificThreats", "comment", "privateComment", "year", "month", "day", "dateInserted");
    }

    public static void exportOccurrenceToCSV(Occurrence occurrence, CSVPrinter csv, TaxEnt acceptedTaxEnt) throws IOException {
        OBSERVED_IN oi = occurrence.getOccurrence();
//                    TaxEnt te = oi.getTaxEnt();
// TODO use field annotations
        csv.printRecord(
                occurrence.getDataSource()
                , oi.getTaxEnt() == null ? "" : oi.getTaxEnt().getNameWithAnnotationOnly(false)
                , oi.getTaxEnt() == null ? "" : oi.getTaxEnt().getName()
                , acceptedTaxEnt == null ? "" : acceptedTaxEnt.getNameWithAnnotationOnly(false)
                , oi.getVerbTaxon()
                , oi.getConfidence()
                , oi.getPresenceStatus()
                , oi.getPhenoState()
                , occurrence._getDateYMD()
                , StringUtils.implode(", ", occurrence._getObserverNames())
                , occurrence._getLatitude(), occurrence._getLongitude(), occurrence.getPrecision()
                , CoordinateConversion.LatLongToMGRS(occurrence._getLatitude(), occurrence._getLongitude(), 1000)
                , occurrence.getVerbLocality(), occurrence.getCode()
                , oi.getAbundance(), oi.getTypeOfEstimate(), oi.getCover(), oi.getHasPhoto(), oi.getHasSpecimen()
                , oi.getSpecificThreats(), oi.getComment(), oi.getPrivateComment(), occurrence.getYear(), occurrence.getMonth(), occurrence.getDay()
                , oi.getDateInserted() == null ? "" : Constants.dateFormatYMDHM.get().format(oi.getDateInserted())
        );

    }
}