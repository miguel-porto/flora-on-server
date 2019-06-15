package pt.floraon.redlistdata.jobs;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import pt.floraon.authentication.entities.User;
import pt.floraon.driver.FloraOnException;
import pt.floraon.driver.interfaces.IFloraOn;
import pt.floraon.driver.interfaces.OccurrenceFilter;
import pt.floraon.driver.jobs.JobFileDownload;
import pt.floraon.driver.utils.StringUtils;
import pt.floraon.geometry.PolygonTheme;
import pt.floraon.occurrences.entities.Occurrence;
import pt.floraon.redlistdata.BasicRedListDataFilter;
import pt.floraon.redlistdata.RedListDataFilter;
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
 * Downloads all occurrences from all providers of all species in the given red list (or optionally filtered by tags)
 */
public class DownloadOccurrencesJob implements JobFileDownload {
    private String territory;
    private int curSpeciesI = 0;
    private String curSpeciesName = "";
    private OccurrenceFilter occurrenceFilter;
    private RedListDataFilter redListDataFilter;
    private Iterator<TaxEnt> taxEntIterator;

    public DownloadOccurrencesJob(String territory, PolygonTheme clippingPolygon, Integer minimumYear, Set<String> filterTags) {
        this.territory = territory;
        this.occurrenceFilter = new BasicOccurrenceFilter(minimumYear, null, true, clippingPolygon);
        this.redListDataFilter = new BasicRedListDataFilter(filterTags);
    }

    public DownloadOccurrencesJob(String territory, RedListDataFilter redListDataFilter, OccurrenceFilter occurrenceFilter) {
        this.territory = territory;
        this.occurrenceFilter = occurrenceFilter;
        this.redListDataFilter = redListDataFilter;
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
        CSVPrinter csvp = new CSVPrinter(new OutputStreamWriter(out), CSVFormat.TDF);

        csvp.print("Source");
        csvp.print("TaxEnt ID");
        csvp.print("taxon");
        csvp.print("verbTaxon");
        csvp.print("latitude");
        csvp.print("longitude");
        csvp.print("date");
        csvp.print("observers");
        csvp.print("precision");
        csvp.print("confidence");
        csvp.print("code");
        csvp.print("local");
        csvp.print("pubNotes");
        csvp.println();
        if(this.taxEntIterator == null) {
            Iterator<RedListDataEntity> it = driver.getRedListData().getAllRedListData(territory, false, null);
            while (it.hasNext()) {
                RedListDataEntity rlde = it.next();
                if (redListDataFilter != null && !this.redListDataFilter.enter(rlde)) continue;
                TaxEnt te = rlde.getTaxEnt();
                querySpecies(driver, te, csvp);
            }
        } else {
            while(taxEntIterator.hasNext()) {
                querySpecies(driver, taxEntIterator.next(), csvp);
            }
        }

        csvp.close();
    }

    private void querySpecies(IFloraOn driver, TaxEnt taxEnt, CSVPrinter printer) throws IOException, FloraOnException {
        OccurrenceProcessor op;
        curSpeciesI++;
        curSpeciesName = taxEnt.getName();
        List<SimpleOccurrenceDataProvider> sodps = driver.getRedListData().getSimpleOccurrenceDataProviders();
        for (SimpleOccurrenceDataProvider edp : sodps)
            edp.executeOccurrenceQuery(taxEnt);

        op = OccurrenceProcessor.iterableOf(sodps, occurrenceFilter);

        if (op.size() > 0) {
            for (Occurrence so : op) {
                printer.printRecord(
                        so.getDataSource(), taxEnt.getID()
                        , taxEnt.getNameWithAnnotationOnly(false)
                        , so.getOccurrence().getVerbTaxon(), so._getLatitude(), so._getLongitude(), so._getDate()
                        , StringUtils.implode(", ", so._getObserverNames())
                        , so.getPrecision(), so.getOccurrence().getConfidence()
                        , so.getCode(), so.getLocality()
                        , so.getPubNotes()
                );
            }
        }

    }

    @Override
    public String getState() {
        return "Processing species " + curSpeciesI + " (" + curSpeciesName + ")";
    }

    @Override
    public String getDescription() {
        return "Occurrence table for all taxa";
    }

    @Override
    public User getOwner() {
        return null;
    }
}
