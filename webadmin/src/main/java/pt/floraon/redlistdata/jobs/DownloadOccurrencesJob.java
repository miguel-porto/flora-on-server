package pt.floraon.redlistdata.jobs;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import pt.floraon.authentication.entities.User;
import pt.floraon.driver.FloraOnException;
import pt.floraon.driver.interfaces.IFloraOn;
import pt.floraon.driver.interfaces.ITaxEntWrapper;
import pt.floraon.driver.interfaces.OccurrenceFilter;
import pt.floraon.driver.jobs.JobFileDownload;
import pt.floraon.driver.results.InferredStatus;
import pt.floraon.geometry.PolygonTheme;
import pt.floraon.occurrences.Common;
import pt.floraon.occurrences.entities.Occurrence;
import pt.floraon.redlistdata.RedListDataFilter;
import pt.floraon.redlistdata.RedListDataFilterFactory;
import pt.floraon.redlistdata.dataproviders.SimpleOccurrenceDataProvider;
import pt.floraon.redlistdata.entities.RedListDataEntity;
import pt.floraon.redlistdata.occurrences.BasicOccurrenceFilter;
import pt.floraon.redlistdata.occurrences.OccurrenceProcessor;
import pt.floraon.taxonomy.entities.TaxEnt;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Downloads all occurrences from all providers of all species in the given red list (or optionally filtered)
 */
public class DownloadOccurrencesJob implements JobFileDownload {
    private String territory;
    private int curSpeciesI = 0;
    private Object flag;
    private String curSpeciesName = "";
    private OccurrenceFilter occurrenceFilter;
    private RedListDataFilter redListDataFilter;
    private Iterator<TaxEnt> taxEntIterator;

    public DownloadOccurrencesJob(String territory, RedListDataFilter redListDataFilter, OccurrenceFilter occurrenceFilter) {
        this(territory, redListDataFilter, occurrenceFilter, false);
    }

    public DownloadOccurrencesJob(String territory, RedListDataFilter redListDataFilter, OccurrenceFilter occurrenceFilter, Object flag) {
        this.territory = territory;
        this.occurrenceFilter = occurrenceFilter;
        this.redListDataFilter = redListDataFilter;
        this.flag = flag;
    }

    /**
     * Include only given taxa
     * @param taxEntIterator
     * @param occurrenceFilter
     */
    public DownloadOccurrencesJob(Iterator<TaxEnt> taxEntIterator, OccurrenceFilter occurrenceFilter) {
        this.occurrenceFilter = occurrenceFilter;
        // this is to prevent the database cursor timeout
        List<TaxEnt> te = new ArrayList<>();
        while(taxEntIterator.hasNext())
            te.add(taxEntIterator.next());
        this.taxEntIterator = te.iterator();
    }

    @Override
    public Charset getCharset() {
        return StandardCharsets.UTF_8;
    }

    @Override
    public void run(IFloraOn driver, OutputStream out) throws FloraOnException, IOException {
        Map<String, String> userMap = new HashMap<>();
        CSVPrinter csvp = new CSVPrinter(new OutputStreamWriter(out), CSVFormat.TDF);
        Common.exportOccurrenceHeaderToCSV(csvp);
        List<User> allUsers = driver.getAdministration().getAllUsers(false);
        for (User u : allUsers)
            userMap.put(u.getID(), u.getName());

        if(this.taxEntIterator == null) {
            Iterator<RedListDataEntity> it = driver.getRedListData().getAllRedListData(territory, false, null);
            while (it.hasNext()) {
                RedListDataEntity rlde = it.next();
                if (redListDataFilter != null && !this.redListDataFilter.enter(rlde)) continue;
                TaxEnt te = rlde.getTaxEnt();
                querySpecies(driver, te, rlde, userMap, csvp);
            }
        } else {
            while(taxEntIterator.hasNext()) {
                querySpecies(driver, taxEntIterator.next(), null, userMap, csvp);
            }
        }

        csvp.close();
    }

    private void querySpecies(IFloraOn driver, TaxEnt taxEnt, RedListDataEntity rlde, Map<String, String> userMap, CSVPrinter printer) throws IOException, FloraOnException {
        OccurrenceProcessor op;
        ITaxEntWrapper tew;
        Map<String, InferredStatus> inferredStatusMap = new HashMap<>();
        Map<String, String[]> endemismDegreeMap = new HashMap<>();
        Map<String, TaxEnt[]> higherTaxonomyMap = new HashMap<>();
        InferredStatus inferredStatus;
        String[] endemismDegree;
        TaxEnt[] higherTaxonomy;

        curSpeciesI++;
        curSpeciesName = taxEnt.getName();
        List<SimpleOccurrenceDataProvider> sodps = driver.getRedListData().getSimpleOccurrenceDataProviders();
        for (SimpleOccurrenceDataProvider edp : sodps)
            edp.executeOccurrenceQuery(taxEnt, flag);

        op = OccurrenceProcessor.iterableOf(sodps, occurrenceFilter);
//        op = new OccurrenceProcessor(sodps, null, 10000, occurrenceFilter);

        if (op.size() > 0) {
            for (Occurrence so : op) {
                // here we override the matched taxent because we want occurrences to be organized by red list sheets.
                so.getOccurrence().setTaxEnt(taxEnt);
                String taxEntId = taxEnt.getID();
                if(inferredStatusMap.containsKey(taxEnt.getID())) {
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
                Common.exportOccurrenceToCSV(so, printer, null, userMap, rlde, inferredStatus, endemismDegree, higherTaxonomy);
            }
        }
    }

    @Override
    public String getState() {
        return "Processing species " + curSpeciesI + " (" + curSpeciesName + ")";
    }

    @Override
    public String getDescription() {
        return "Occurrence table for taxa (optionally filtered)";
    }

    @Override
    public User getOwner() {
        return null;
    }
}
