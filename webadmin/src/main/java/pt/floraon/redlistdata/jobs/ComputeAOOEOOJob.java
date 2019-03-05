package pt.floraon.redlistdata.jobs;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import pt.floraon.authentication.entities.User;
import pt.floraon.driver.Constants;
import pt.floraon.driver.FloraOnException;
import pt.floraon.driver.interfaces.IFloraOn;
import pt.floraon.driver.interfaces.INodeKey;
import pt.floraon.driver.interfaces.OccurrenceFilter;
import pt.floraon.driver.jobs.JobFileDownload;
import pt.floraon.driver.results.InferredStatus;
import pt.floraon.driver.utils.StringUtils;
import pt.floraon.geometry.PolygonTheme;
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
 * For a given red list dataset, download a table with the taxa, AOO, EOO, etc.
 * Created by miguel on 13-03-2017.
 */
public class ComputeAOOEOOJob implements JobFileDownload {
    protected String territory;
//    private PolygonTheme clippingPolygon;
//    private Integer minimumYear;
    protected Integer sizeOfSquare;
    protected int curSpeciesI = 0;
    protected String curSpeciesName = "";
    protected OccurrenceFilter occurrenceFilter;
    private Iterator<RedListDataEntity> itRLDE;
    protected RedListDataFilter redListDataFilter;

    public ComputeAOOEOOJob(String territory, Integer sizeOfSquare, OccurrenceFilter occurrenceFilter
            , RedListDataFilter redListDataFilter) {
        this.sizeOfSquare = sizeOfSquare;
        this.occurrenceFilter = occurrenceFilter;
        this.redListDataFilter = redListDataFilter;
        this.territory = territory;
    }

    public ComputeAOOEOOJob(String territory, Integer sizeOfSquare, OccurrenceFilter occurrenceFilter
            , RedListDataFilter redListDataFilter, Iterator<RedListDataEntity> redListDataEntityIterator) {
        this(territory, sizeOfSquare, occurrenceFilter, redListDataFilter);
        this.itRLDE = redListDataEntityIterator;
    }

/*
    ComputeAOOEOOJob(String territory, PolygonTheme clippingPolygon, Integer minimumYear, Integer sizeOfSquare, Set<String> filterTags) {
        this.territory = territory;
        this.clippingPolygon = clippingPolygon;
        this.minimumYear = minimumYear;
        this.sizeOfSquare = sizeOfSquare;
        this.filterTags = filterTags;
    }

    ComputeAOOEOOJob(String territory, Iterator<RedListDataEntity> redListDataEntityIterator, PolygonTheme clippingPolygon, Integer minimumYear, Integer sizeOfSquare, Set<String> filterTags) {
        this.territory = territory;
        this.clippingPolygon = clippingPolygon;
        this.minimumYear = minimumYear;
        this.sizeOfSquare = sizeOfSquare;
        this.filterTags = filterTags;
        this.itRLDE = redListDataEntityIterator;
    }
*/

    @Override
    public Charset getCharset() {
        return Charset.forName("Windows-1252");
    }

    @Override
    public void run(IFloraOn driver, OutputStream out) throws FloraOnException, IOException {
        OccurrenceProcessor op;
        Iterator<RedListDataEntity> it =
                itRLDE == null ?
                        driver.getRedListData().getAllRedListData(territory, false, null)
                        : itRLDE;
        CSVPrinter csvp = new CSVPrinter(new OutputStreamWriter(out, this.getCharset()), CSVFormat.TDF);
        csvp.print("TaxEnt ID");
        csvp.print("Family");
        csvp.print("Taxon");
        csvp.print("Tags");
        csvp.print("Endemic?");
        csvp.print("Threat category");
        csvp.print("Criteria");
        csvp.print("Protection");
        csvp.print("AOO (km2)");
        csvp.print("EOO (km2)");
        csvp.print("Real EOO (km2)");
        csvp.print("Number of sites");
        csvp.print("Number of occurrence records");
        csvp.print("URL");
        csvp.println();

        while(it.hasNext()) {   // for each taxon in red list
            RedListDataEntity rlde = it.next();
            curSpeciesI++;
            curSpeciesName = rlde.getTaxEnt().getName();
            if(redListDataFilter != null && !redListDataFilter.enter(rlde)) continue;

            List<SimpleOccurrenceDataProvider> sodps = driver.getRedListData().getSimpleOccurrenceDataProviders();
            for (SimpleOccurrenceDataProvider edp : sodps) {
                edp.executeOccurrenceQuery(rlde.getTaxEnt());
            }
            op = new OccurrenceProcessor(sodps, null, sizeOfSquare, this.occurrenceFilter);

            INodeKey tKey = driver.asNodeKey(rlde.getTaxEntID());
            String endemicFrom = StringUtils.implode(", ", driver.wrapTaxEnt(tKey).getEndemismDegree());
//            InferredStatus is = driver.wrapTaxEnt(tKey).getInferredNativeStatus(territory);
//            boolean endemic = is != null && is.isEndemic();
            TaxEnt f = driver.wrapTaxEnt(tKey).getParentOfRank(Constants.TaxonRanks.FAMILY);
            String family = f == null ? "" : f.getName();

            csvp.print(rlde.getTaxEnt().getID());
            csvp.print(family);
            csvp.print(rlde.getTaxEnt().getName());
            csvp.print(StringUtils.implode(", ", rlde.getTags()));
//            csvp.print(endemic ? ("Endemic from " + territory) : "No");
            csvp.print(endemicFrom == null ? "No info" : ("Endemic from " + endemicFrom));
            if(rlde.getAssessment().getAdjustedCategory() != null) {
                csvp.print(rlde.getAssessment().getAdjustedCategory().getLabel());
                csvp.print(rlde.getAssessment()._getCriteriaAsString());
            } else {
                csvp.print("");
                csvp.print("");
            }

            csvp.print(StringUtils.implode(", ", rlde.getConservation().getLegalProtection()));

            if(op.size() == 0) {
                csvp.print("-");
                csvp.print("-");
                csvp.print("-");
                csvp.print("-");
                csvp.print("-");
            } else {
                csvp.print(op.getAOO());
                csvp.print(op.getEOO());
                csvp.print(op.getRealEOO());
                csvp.print(op.getNLocations());
                csvp.print(op.size());
            }
            csvp.print("https://lvf.flora-on.pt/redlist/" + territory + "?w=taxon&id=" + rlde.getTaxEnt()._getIDURLEncoded());
            csvp.println();
        }
        csvp.close();
    }

    @Override
    public String getState() {
        return "Processing species " + curSpeciesI + " (" + curSpeciesName + ")";
    }

    @Override
    public String getDescription() {
        return "Table of taxa with AOO, EOO, etc.";
    }

    @Override
    public User getOwner() {
        return null;
    }
}
