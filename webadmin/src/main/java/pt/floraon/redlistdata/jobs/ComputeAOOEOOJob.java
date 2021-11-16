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
import pt.floraon.driver.utils.StringUtils;
import pt.floraon.ecology.entities.Habitat;
import pt.floraon.redlistdata.FieldValues;
import pt.floraon.redlistdata.RedListDataFilter;
import pt.floraon.redlistdata.RedListEnums;
import pt.floraon.redlistdata.threats.Threat;
import pt.floraon.redlistdata.dataproviders.SimpleOccurrenceDataProvider;
import pt.floraon.redlistdata.entities.RedListDataEntity;
import pt.floraon.redlistdata.occurrences.OccurrenceProcessor;
import pt.floraon.taxonomy.entities.TaxEnt;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
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
    private boolean recalculate;
    Map<String, String> userMap;
    protected RedListDataFilter redListDataFilter;

    public ComputeAOOEOOJob(String territory, Integer sizeOfSquare, OccurrenceFilter occurrenceFilter
            , RedListDataFilter redListDataFilter) {
        this(territory, sizeOfSquare, occurrenceFilter, redListDataFilter, true, null);
    }

    /**
     *
     * @param territory
     * @param sizeOfSquare
     * @param occurrenceFilter
     * @param redListDataFilter
     * @param recalculate Recompute AOO and EOO?
     */
    public ComputeAOOEOOJob(String territory, Integer sizeOfSquare, OccurrenceFilter occurrenceFilter
            , RedListDataFilter redListDataFilter, boolean recalculate, IFloraOn driver) {
        this.sizeOfSquare = sizeOfSquare;
        this.occurrenceFilter = occurrenceFilter;
        this.redListDataFilter = redListDataFilter;
        this.territory = territory;
        this.recalculate = recalculate;

        if(driver != null) {
            List<User> allUsers = null;
            userMap = new HashMap<>();
            try {
                allUsers = driver.getAdministration().getAllUsers(false);
                for (User u : allUsers) {
                    userMap.put(u.getID(), u.getName());
                }
            } catch (FloraOnException e) {
                e.printStackTrace();
            }
        }

    }
/*
    public ComputeAOOEOOJob(String territory, Integer sizeOfSquare, OccurrenceFilter occurrenceFilter
            , RedListDataFilter redListDataFilter, Iterator<RedListDataEntity> redListDataEntityIterator) {
        this(territory, sizeOfSquare, occurrenceFilter, redListDataFilter);
        this.itRLDE = redListDataEntityIterator;
    }


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
        return Charset.forName("windows-1252");
//        return StandardCharsets.UTF_8;
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
        csvp.print("Fully qualified taxon");
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
        csvp.print("Authors");
        csvp.print("Assessors");
        csvp.print("Reviewers");
        csvp.print("Collaborators");
        csvp.print("Decline AOO EOO");
        csvp.print("Decline nr individuals");
        csvp.print("Decline habitat");
        csvp.print("Decline locations");
        csvp.print("Number of individuals (cat)");
        csvp.print("Number of individuals (exact)");
        csvp.print("Severely fragmented");
        csvp.print("Extreme fluctuations");
        csvp.print("Number of locations");
        csvp.print("7.2 Conservation plans");
        csvp.print("7.2 Conservation plans justification");
        csvp.print("7.3 Ex-situ");
        csvp.print("7.3 Ex-situ justification");
        csvp.print("Uses");
        csvp.print("Habitats");

/*
        csvp.print("Threats");
        csvp.print("Conservation measures");
        csvp.print("Proposed studies");
*/
        for(Threat t : driver.getThreatEnum().values())
            csvp.print("Threat: " + FieldValues.getString(t.getLabel()));
        for(RedListEnums.ProposedConservationActions t : RedListEnums.ProposedConservationActions.values())
            csvp.print("Action: " + FieldValues.getString(t.getLabel()));
        for(RedListEnums.ProposedStudyMeasures t : RedListEnums.ProposedStudyMeasures.values())
            csvp.print("Study: " + FieldValues.getString(t.getLabel()));

        csvp.print("URL");
        csvp.println();

        while(it.hasNext()) {   // for each taxon in red list
            RedListDataEntity rlde = it.next();
            curSpeciesI++;
            curSpeciesName = rlde.getTaxEnt().getName();
            if(redListDataFilter != null && !redListDataFilter.enter(rlde)) continue;

            INodeKey tKey = driver.asNodeKey(rlde.getTaxEntID());
            String endemicFrom = StringUtils.implode(", ", driver.wrapTaxEnt(tKey).getEndemismDegree());
//            InferredStatus is = driver.wrapTaxEnt(tKey).getInferredNativeStatus(territory);
//            boolean endemic = is != null && is.isEndemic();
            TaxEnt f = driver.wrapTaxEnt(tKey).getParentOfRank(Constants.TaxonRanks.FAMILY);
            String family = f == null ? "" : f.getName();

            csvp.print(rlde.getTaxEnt().getID());
            csvp.print(family);
            csvp.print(rlde.getTaxEnt().getName());
            csvp.print(rlde.getTaxEnt().getFullName(false));
            csvp.print(StringUtils.implode(", ", rlde.getTags()));
//            csvp.print(endemic ? ("Endemic from " + territory) : "No");

            csvp.print(StringUtils.isStringEmpty(endemicFrom) ? "No info" : ("Endemic from " + endemicFrom));
            if(rlde.getAssessment().getFinalCategory() != null) {
                csvp.print(rlde.getAssessment().getFinalCategory().getLabel());
                csvp.print(rlde.getAssessment()._getCriteriaAsString());
            } else {
                csvp.print("");
                csvp.print("");
            }

            csvp.print(StringUtils.implode(", ", rlde.getConservation().getLegalProtection()));

            if(this.recalculate) {
                List<SimpleOccurrenceDataProvider> sodps = driver.getRedListData().getSimpleOccurrenceDataProviders();
                for (SimpleOccurrenceDataProvider edp : sodps) {
                    edp.executeOccurrenceQuery(rlde.getTaxEnt());
                }
                op = new OccurrenceProcessor(sodps, null, sizeOfSquare, this.occurrenceFilter);

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
            } else {
                csvp.print(rlde.getGeographicalDistribution().getAOO());
                csvp.print(rlde.getGeographicalDistribution().getEOO());
                csvp.print("-");
                csvp.print("-");
                csvp.print("-");
            }

            // Authors, collaborators, etc.
            csvp.print(StringUtils.implode(", ", userMap, rlde.getAssessment().getAuthors()));
            csvp.print(StringUtils.implode(", ", userMap, rlde.getAssessment().getEvaluator()));
            csvp.print(StringUtils.implode(", ", userMap, rlde.getAssessment().getReviewer()));
            csvp.print(rlde.getAssessment().getCollaborators());

            csvp.print(rlde.getGeographicalDistribution().getDeclineDistribution() == null ? "" : rlde.getGeographicalDistribution().getDeclineDistribution().getLabel());
            csvp.print(rlde.getPopulation().getPopulationDecline() == null ? "" : rlde.getPopulation().getPopulationDecline().getLabel());
            csvp.print(rlde.getEcology().getDeclineHabitatQuality() == null ? "" : rlde.getEcology().getDeclineHabitatQuality().getLabel());
            csvp.print(rlde.getThreats().getDeclineNrLocations() == null ? "" : FieldValues.getString(rlde.getThreats().getDeclineNrLocations().getLabel()));

            csvp.print(rlde.getPopulation().getNrMatureIndividualsCategory() == null ? "" : rlde.getPopulation().getNrMatureIndividualsCategory().getLabel());
            csvp.print(rlde.getPopulation().getNrMatureIndividualsExact());
            csvp.print(rlde.getPopulation().getSeverelyFragmented().getLabel());
            csvp.print(rlde.getPopulation().getExtremeFluctuations().getLabel());
            csvp.print(rlde.getThreats().getNumberOfLocations());

            csvp.print(rlde.getConservation().getConservationPlans().getLabel());
            csvp.print(rlde.getConservation().getConservationPlansJustification().toCleanString());
            csvp.print(rlde.getConservation().getExSituConservation().getLabel());
            csvp.print(rlde.getConservation().getExSituConservationJustification().toCleanString());

            csvp.print(StringUtils.implode("; ", rlde.getUsesAndTrade().getUses()));
            Iterator<Habitat> itHab = driver.getNodeWorkerDriver().getDocuments(new HashSet<>(Arrays.asList(rlde.getEcology().getHabitatTypes())), Habitat.class);
            ArrayList<String> habs = new ArrayList<>();
            while(itHab.hasNext()) {
                habs.add(itHab.next().getFullName());
            }

            csvp.print(StringUtils.implode("; ", habs.toArray(new String[0])));

/*
            csvp.print(StringUtils.implode("; ", "pt.floraon.redlistdata.fieldValues", rlde.getThreats().getThreats()));
            csvp.print(StringUtils.implode("; ", "pt.floraon.redlistdata.fieldValues", rlde.getConservation().getProposedConservationActions()));
            csvp.print(StringUtils.implode("; ", "pt.floraon.redlistdata.fieldValues", rlde.getConservation().getProposedStudyMeasures()));
*/

            // Columns for multiple selection fields
            List<Threat> thr = Arrays.asList(rlde.getThreats().getThreats());
            for(Threat t : driver.getThreatEnum().values())
                csvp.print(thr.contains(t) ? "x" : "");

            List<RedListEnums.ProposedConservationActions> cns = Arrays.asList(rlde.getConservation().getProposedConservationActions());
            for(RedListEnums.ProposedConservationActions t : RedListEnums.ProposedConservationActions.values())
                csvp.print(cns.contains(t) ? "x" : "");

            List<RedListEnums.ProposedStudyMeasures> std = Arrays.asList(rlde.getConservation().getProposedStudyMeasures());
            for(RedListEnums.ProposedStudyMeasures t : RedListEnums.ProposedStudyMeasures.values())
                csvp.print(std.contains(t) ? "x" : "");


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
