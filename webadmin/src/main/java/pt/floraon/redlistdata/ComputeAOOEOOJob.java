package pt.floraon.redlistdata;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import pt.floraon.driver.FloraOnException;
import pt.floraon.driver.IFloraOn;
import pt.floraon.driver.jobs.JobFileDownload;
import pt.floraon.geometry.PolygonTheme;
import pt.floraon.redlistdata.dataproviders.SimpleOccurrenceDataProvider;
import pt.floraon.redlistdata.entities.RedListDataEntity;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.*;

/**
 * For a given red list dataset, download a table with the taxa, AOO, EOO, etc.
 * Created by miguel on 13-03-2017.
 */
public class ComputeAOOEOOJob implements JobFileDownload {
    private String territory;
    private PolygonTheme clippingPolygon;
    private Integer minimumYear, sizeOfSquare;
    private int curSpeciesI = 0;
    private String curSpeciesName = "";
    private Set<String> filterTags;

    ComputeAOOEOOJob(String territory, PolygonTheme clippingPolygon, Integer minimumYear, Integer sizeOfSquare, Set<String> filterTags) {
        this.territory = territory;
        this.clippingPolygon = clippingPolygon;
        this.minimumYear = minimumYear;
        this.sizeOfSquare = sizeOfSquare;
        this.filterTags = filterTags;
    }

    @Override
    public void run(IFloraOn driver, OutputStream out) throws FloraOnException, IOException {
        OccurrenceProcessor op;
        Iterator<RedListDataEntity> it = driver.getRedListData().getAllRedListData(territory, false);
        CSVPrinter csvp = new CSVPrinter(new OutputStreamWriter(out), CSVFormat.TDF);

        csvp.print("TaxEnt ID");
        csvp.print("Taxon");
        csvp.print("AOO (km2)");
        csvp.print("EOO (km2)");
        csvp.print("Real EOO (km2)");
        csvp.print("Number of sites");
        csvp.println();

        while(it.hasNext()) {
            RedListDataEntity rlde = it.next();
            curSpeciesI++;
            curSpeciesName = rlde.getTaxEnt().getName();
            if(filterTags != null && Collections.disjoint(filterTags, Arrays.asList(rlde.getTags()))) continue;
            List<SimpleOccurrenceDataProvider> sodps = driver.getRedListData().getSimpleOccurrenceDataProviders();
            for (SimpleOccurrenceDataProvider edp : sodps) {
                edp.executeOccurrenceQuery(rlde.getTaxEnt());
            }
            op = new OccurrenceProcessor(sodps, null
                    , sizeOfSquare, clippingPolygon, minimumYear, null, false);

            if(op.size() == 0) {
                csvp.print(rlde.getTaxEnt().getID());
                csvp.print(rlde.getTaxEnt().getName());
                csvp.print("");
                csvp.print("");
                csvp.print("");
                csvp.print("");
                csvp.println();
            } else {
                csvp.print(rlde.getTaxEnt().getID());
                csvp.print(rlde.getTaxEnt().getName());
                csvp.print(op.getAOO());
                csvp.print(op.getEOO());
                csvp.print(op.getRealEOO());
                csvp.print(op.getNLocations());
                csvp.println();
            }
        }
        csvp.close();
    }

    @Override
    public String getState() {
        return "Processing species " + curSpeciesI + " (" + curSpeciesName + ")";
    }

    @Override
    public String getDescription() {
        return "AOO and EOO table";
    }
}
