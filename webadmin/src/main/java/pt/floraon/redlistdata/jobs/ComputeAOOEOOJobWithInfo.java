package pt.floraon.redlistdata.jobs;

import jline.internal.Log;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import pt.floraon.authentication.entities.User;
import pt.floraon.driver.FloraOnException;
import pt.floraon.driver.interfaces.IFloraOn;
import pt.floraon.driver.interfaces.INodeKey;
import pt.floraon.driver.interfaces.OccurrenceFilter;
import pt.floraon.driver.results.InferredStatus;
import pt.floraon.redlistdata.RedListDataFilter;
import pt.floraon.redlistdata.dataproviders.SimpleOccurrenceDataProvider;
import pt.floraon.redlistdata.entities.RedListDataEntity;
import pt.floraon.redlistdata.occurrences.OccurrenceProcessor;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Map;

/**
 * This is just a short selection of fields.
 */
public class ComputeAOOEOOJobWithInfo extends ComputeAOOEOOJob {
    Map<INodeKey, Object> taxaKeyMap;

    public ComputeAOOEOOJobWithInfo(String territory, Integer sizeOfSquare, OccurrenceFilter occurrenceFilter
            , RedListDataFilter redListDataFilter, Map<INodeKey, Object> taxaKeyMap, User owner) {
        super(territory, sizeOfSquare, occurrenceFilter, redListDataFilter, owner);
        this.taxaKeyMap = taxaKeyMap;
    }

    @Override
    public void run(IFloraOn driver, OutputStream out) throws FloraOnException, IOException {
        OccurrenceProcessor op;
        CSVPrinter csvp = new CSVPrinter(new OutputStreamWriter(out, this.getCharset()), CSVFormat.TDF);

        csvp.print("TaxEnt ID");
        csvp.print("Taxon");
        csvp.print("Endemic?");
        csvp.print("Threat category");
        csvp.print("AOO (km2)");
        csvp.print("EOO (km2)");
        csvp.print("Real EOO (km2)");
        csvp.print("Number of sites");
        csvp.print("Number of occurrence records");
        csvp.print("Year last observed");
        csvp.println();

        for (Map.Entry<INodeKey, Object> entry : this.taxaKeyMap.entrySet()) {   // for each taxon in red list
            RedListDataEntity rlde = driver.getRedListData().getRedListDataEntity(this.territory, entry.getKey());
            if(rlde == null) {
                Log.warn("Taxon not found!");
                continue;
            }
            curSpeciesI++;
            curSpeciesName = rlde.getTaxEnt().getName();
            if (redListDataFilter != null && !redListDataFilter.enter(rlde)) continue;
            List<SimpleOccurrenceDataProvider> sodps = driver.getRedListData().getSimpleOccurrenceDataProviders();
            for (SimpleOccurrenceDataProvider edp : sodps) {
                edp.executeOccurrenceQuery(rlde.getTaxEnt());
            }
            op = new OccurrenceProcessor(sodps, null, sizeOfSquare, this.occurrenceFilter);
            INodeKey tKey = driver.asNodeKey(rlde.getTaxEntID());
            InferredStatus is = driver.wrapTaxEnt(tKey).getInferredNativeStatus(territory);
            boolean endemic = is != null && is.isEndemic();
            if (op.size() == 0) {
                csvp.print(rlde.getTaxEnt().getID());
                csvp.print(rlde.getTaxEnt().getName());
                csvp.print(endemic ? ("Endemic from " + territory) : "No");
                csvp.print("-");
                csvp.print("-");
                csvp.print("-");
                csvp.print("-");
                csvp.print("-");
                csvp.print("-");
                csvp.print("-");
                csvp.println();
            } else {
                csvp.print(rlde.getTaxEnt().getID());
                csvp.print(rlde.getTaxEnt().getName());
                csvp.print(endemic ? ("Endemic from " + territory) : "No");
                if (rlde.getAssessment().getFinalCategory() != null)
                    csvp.print(rlde.getAssessment().getFinalCategory().getLabel());
                else
                    csvp.print("");
                csvp.print(op.getAOO());
                csvp.print(op.getEOO());
                csvp.print(op.getRealEOO());
                csvp.print(op.getNLocations());
                csvp.print(op.size());
                csvp.print(entry.getValue().toString());
                csvp.println();
            }
        }
        csvp.close();
    }

}
