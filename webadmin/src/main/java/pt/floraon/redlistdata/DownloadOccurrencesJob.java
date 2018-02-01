package pt.floraon.redlistdata;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import pt.floraon.driver.FloraOnException;
import pt.floraon.driver.interfaces.IFloraOn;
import pt.floraon.driver.jobs.JobFileDownload;
import pt.floraon.driver.utils.StringUtils;
import pt.floraon.geometry.PolygonTheme;
import pt.floraon.redlistdata.dataproviders.SimpleOccurrence;
import pt.floraon.redlistdata.dataproviders.SimpleOccurrenceDataProvider;
import pt.floraon.redlistdata.entities.RedListDataEntity;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.*;

public class DownloadOccurrencesJob implements JobFileDownload {
    private String territory;
    private PolygonTheme clippingPolygon;
    private Integer minimumYear;
    private int curSpeciesI = 0;
    private String curSpeciesName = "";
    private Set<String> filterTags;

    DownloadOccurrencesJob(String territory, PolygonTheme clippingPolygon, Integer minimumYear, Set<String> filterTags) {
        this.territory = territory;
        this.clippingPolygon = clippingPolygon;
        this.minimumYear = minimumYear;
        this.filterTags = filterTags;
    }

    @Override
    public void run(IFloraOn driver, OutputStream out) throws FloraOnException, IOException {
        OccurrenceProcessor op;
        Iterator<RedListDataEntity> it = driver.getRedListData().getAllRedListData(territory, false, null);
        CSVPrinter csvp = new CSVPrinter(new OutputStreamWriter(out), CSVFormat.TDF);

        csvp.print("Source");
        csvp.print("TaxEnt ID");
        csvp.print("taxon");
        csvp.print("verbTaxon");
        csvp.print("latitude");
        csvp.print("longitude");
        csvp.print("date");
        csvp.print("observers");
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

            op = OccurrenceProcessor.iterableOf(sodps, clippingPolygon, minimumYear, null, true);

            if(op.size() > 0) {
                for (SimpleOccurrence so : op) {
                    csvp.print(so.getDataSource());
                    csvp.print(rlde.getTaxEnt().getID());
                    csvp.print(rlde.getTaxEnt().getNameWithAnnotationOnly(false));
                    csvp.print(so.getOccurrence().getVerbTaxon());
                    csvp.print(so.getLatitude());
                    csvp.print(so.getLongitude());
                    csvp.print(so._getDate());
                    csvp.print(StringUtils.implode(", ", so._getObserverNames()));
                    csvp.println();
                }
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
        return "Occurrence table for all taxa";
    }
}
