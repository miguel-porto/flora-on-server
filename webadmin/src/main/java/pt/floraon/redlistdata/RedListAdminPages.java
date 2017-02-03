package pt.floraon.redlistdata;

import org.apache.commons.lang.ArrayUtils;
import pt.floraon.authentication.Privileges;
import pt.floraon.authentication.entities.TaxonPrivileges;
import pt.floraon.driver.FloraOnException;
import pt.floraon.geometry.PolygonTheme;
import pt.floraon.redlistdata.entities.*;
import pt.floraon.taxonomy.entities.TaxEnt;
import pt.floraon.authentication.entities.User;
import pt.floraon.server.FloraOnServlet;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;
import java.util.List;

import static pt.floraon.authentication.Privileges.EDIT_ALL_FIELDS;
import static pt.floraon.driver.Constants.cleanArray;
import static pt.floraon.driver.Constants.sanitizeHtmlId;

/**
 * Main page of red list data
 * Created by Miguel Porto on 01-11-2016.
 */
@WebServlet("/redlist/*")
public class RedListAdminPages extends FloraOnServlet {
    @Override
    public void doFloraOnPost() throws ServletException, IOException, FloraOnException {
        doFloraOnGet();
    }

    @Override
    public void doFloraOnGet() throws ServletException, IOException, FloraOnException {
        String what;
        TaxEnt te;
        long sizeOfSquare = 2000;
/*
Gson gs = new GsonBuilder().setPrettyPrinting().create();
System.out.println(gs.toJson(getUser()));
*/

        request.setAttribute("uuid", "sk13");

        ListIterator<String> path;
        try {
            path = getPathIteratorAfter("redlist");
        } catch (FloraOnException e) {
            // no territory specified
            request.setAttribute("what", "addterritory");
            request.setAttribute("territories", driver.getListDriver().getAllTerritories(null));
            request.getRequestDispatcher("/main-redlistinfo.jsp").forward(request, response);
            return;
        }

        String territory = path.next();
        request.setAttribute("territory", territory);
        final ExternalDataProvider foop = driver.getRedListData().getExternalDataProviders().get(0);

        request.setAttribute("what", what = getParameterAsString("w", "main"));

        // make a map of user IDs and names
        List<User> allUsers = driver.getAdministration().getAllUsers();
        Map<String, String> userMap = new HashMap<>();
        for(User u : allUsers) {
            userMap.put(u.getID(), u.getName());
//            userMap.put(u.getIDURLEncoded(), u.getName());
        }

        request.setAttribute("allUsers", allUsers);
        request.setAttribute("userMap", userMap);

        switch (what) {
/*
 * Shows the taxon list
 */
            case "main":
//                List<TaxEnt> taxEntList = driver.getListDriver().getAllSpeciesOrInferiorTaxEnt(true, true, territory, null, null);
//                List<RedListDataEntity> taxEntList = driver.getRedListData().getAllRedListTaxa(territory, getUser().canMANAGE_REDLIST_USERS());
                getUser().resetEffectivePrivileges();
                List<RedListDataEntity> taxEntList = driver.getRedListData().getAllRedListTaxa(territory, true);
                int count1 = 0, count2 = 0, count3 = 0;
                for(RedListDataEntity rlde1 : taxEntList) {
                    if(rlde1.hasResponsibleForTexts()) count1++;
                    if(rlde1.getAssessment().getAssessmentStatus() == RedListEnums.AssessmentStatus.PRELIMINARY) count2++;
                    if(rlde1.getAssessment().getTextStatus() == RedListEnums.TextStatus.READY) count3++;
                }

//                taxEntList.get(0).getAssessment().getAssessmentStatus().isAssessed()
                Set<String> at = driver.getRedListData().getRedListTags(territory);
                Map<String, String> ate = new HashMap<>();
                for(String s : at)
                    ate.put(sanitizeHtmlId(s), s);

                request.setAttribute("allTags", ate.entrySet());
                request.setAttribute("specieslist", taxEntList);
                request.setAttribute("nrsppwithresponsible", count1);
                request.setAttribute("nrspppreliminaryassessment", count2);
                request.setAttribute("nrspptextsready", count3);
                break;

            case "taxon":
                // enums
                request.setAttribute("geographicalDistribution_DeclineDistribution", RedListEnums.DeclineDistribution.values());
                request.setAttribute("geographicalDistribution_ExtremeFluctuations", RedListEnums.ExtremeFluctuations.values());
                request.setAttribute("population_NrMatureIndividualsCategory", RedListEnums.NrMatureIndividuals.values());
                request.setAttribute("population_TypeOfEstimate", RedListEnums.TypeOfPopulationEstimate.values());
                request.setAttribute("population_PopulationDecline", RedListEnums.DeclinePopulation.values());
                request.setAttribute("population_PopulationSizeReduction", RedListEnums.PopulationSizeReduction.values());
                request.setAttribute("population_SeverelyFragmented", RedListEnums.SeverelyFragmented.values());
                request.setAttribute("population_ExtremeFluctuations", RedListEnums.YesNoNA.values());
                request.setAttribute("population_NrMatureEachSubpop", RedListEnums.NrMatureEachSubpop.values());
                request.setAttribute("population_PercentMatureOneSubpop", RedListEnums.PercentMatureOneSubpop.values());
                request.setAttribute("ecology_HabitatTypes", RedListEnums.HabitatTypes.values());
                request.setAttribute("ecology_DeclineHabitatQuality", RedListEnums.DeclineHabitatQuality.values());
                request.setAttribute("usesAndTrade_Uses", RedListEnums.Uses.values());
                request.setAttribute("usesAndTrade_Overexploitation", RedListEnums.Overexploitation.values());
                request.setAttribute("threats_DeclineNrLocations", RedListEnums.DeclineNrLocations.values());
                request.setAttribute("threats_ExtremeFluctuationsNrLocations", RedListEnums.YesNoNA.values());
                request.setAttribute("conservation_ConservationPlans", RedListEnums.YesNoNA.values());
                request.setAttribute("conservation_ExSituConservation", RedListEnums.YesNoNA.values());
                request.setAttribute("conservation_ProposedConservationActions", RedListEnums.ProposedConservationActions.values());
                request.setAttribute("conservation_ProposedStudyMeasures", RedListEnums.ProposedStudyMeasures.values());
                request.setAttribute("assessment_Category", RedListEnums.RedListCategories.valuesNotUpDownListed());
                request.setAttribute("assessment_SubCategory", RedListEnums.CRTags.values());
                request.setAttribute("assessment_RegionalAssessment", RedListEnums.YesNoLikelyUnlikely.values());
                request.setAttribute("assessment_UpDownListing", RedListEnums.UpDownList.values());
                request.setAttribute("assessment_TextStatus", RedListEnums.TextStatus.values());
                request.setAttribute("assessment_AssessmentStatus", RedListEnums.AssessmentStatus.values());
                request.setAttribute("assessment_ReviewStatus", RedListEnums.ReviewStatus.values());
                request.setAttribute("assessment_PublicationStatus", RedListEnums.PublicationStatus.values());

//                request.getRequestDispatcher("/main-redlistinfo.jsp").forward(request, response);

                // TODO this should be a user configuration loaded at startup
                PolygonTheme protectedAreas = new PolygonTheme(this.getClass().getResourceAsStream("SNAC.geojson"), "SITE_NAME");
                String[] ids = request.getParameterValues("id");
                if(ids == null || ids.length == 0) {
                    request.setAttribute("warning", "Taxon ID not provided.");
                    break;
                }
                if(ids.length == 1) {
                    RedListDataEntity rlde = driver.getRedListData().getRedListDataEntity(territory, getParameterAsKey("id"));
                    if (rlde == null) return;
                    // set privileges for this taxon
                    getUser().setEffectivePrivilegesFor(driver, getParameterAsKey("id"));

                    request.setAttribute("taxon", rlde.getTaxEnt());
                    request.setAttribute("synonyms", driver.wrapTaxEnt(getParameterAsKey("id")).getSynonyms());
                    if (rlde.getTaxEnt().getOldId() != null) {
                        foop.executeOccurrenceQuery(rlde.getTaxEnt().getOldId());
                        // TODO clipping polygon must be a user configuration
                        foop.setClippingPolygon(new PolygonTheme(this.getClass().getResourceAsStream("PT_buffer.geojson"), null));
                        foop.setMinimumYear(1991);   // TODO year should be a user configuration

                        OccurrenceProcessor occurrenceProcessor = new OccurrenceProcessor(
                                foop, protectedAreas, sizeOfSquare);

                        // if it is published, AOO and EOO are from the data sheet, otherwise they are computed from
                        // live occurrences
                        Double EOO = null, AOO = null;
                        if (rlde.getAssessment().getPublicationStatus() == RedListEnums.PublicationStatus.PUBLISHED) {
                            EOO = rlde.getGeographicalDistribution().getEOO();
                            AOO = rlde.getGeographicalDistribution().getAOO();
                        }
                        if (EOO == null) EOO = occurrenceProcessor.getEOO();
                        if (AOO == null) AOO = occurrenceProcessor.getAOO();
                        request.setAttribute("EOO", EOO);
                        request.setAttribute("AOO", AOO);
                        request.setAttribute("realEOO", occurrenceProcessor.getRealEOO());
                        request.setAttribute("squareEOO", occurrenceProcessor.getSquareEOO());
                        request.setAttribute("sizeofsquare", sizeOfSquare / 1000);
                        request.setAttribute("nquads", occurrenceProcessor.getNQuads());
                        request.setAttribute("nclusters", occurrenceProcessor.getNLocations());

                        Double[] lAreas = occurrenceProcessor.getLocationAreas();
                        double sum = 0;
                        double[] vals = ArrayUtils.toPrimitive(lAreas, -1);

                        for (int i = 0; i < lAreas.length; i++) {
                            sum += lAreas[i] / 10000;
                            vals[i] = vals[i] / 10000;
                        }
                        sum /= lAreas.length;
                        request.setAttribute("meanLocationArea", sum);

    /*
    HISTOGRAM!
                        HistogramDataset d = new HistogramDataset();
                        d.addSeries("Area", vals, 14, 0, sum * 0.5);
                        JFreeChart ch = ChartFactory.createHistogram("Area", "Sqrt Area", "N", d, PlotOrientation.VERTICAL, false, false, false);

                        XYPlot pl = (XYPlot) ch.getPlot();
                        pl.getRenderer().setSeriesPaint(0, Color.BLUE);
                        ((XYBarRenderer) pl.getRenderer()).setBarPainter(new StandardXYBarPainter());
                        ((XYBarRenderer) pl.getRenderer()).setDrawBarOutline(false);
                        ((XYBarRenderer) pl.getRenderer()).setMargin(0.25);
                        ch.setBackgroundPaint(null);

                        SVGGraphics2D g2 = new SVGGraphics2D(600, 400);
                        ch.draw(g2, new Rectangle(600,400));
                        String svgElement = g2.getSVGElement();
                        request.setAttribute("histogram", svgElement);
    */

                        StringWriter sw = new StringWriter();
                        occurrenceProcessor.exportSVG(new PrintWriter(sw), getUser().canVIEW_FULL_SHEET());
                        request.setAttribute("svgmap", sw.toString());

                        Set<String> groupAreasBy = new HashSet<>();
                        groupAreasBy.add("SITE_NAME");
                        groupAreasBy.add("TIPO");   // TODO this should be user configuration
                        request.setAttribute("occurrenceInProtectedAreas"
                                , occurrenceProcessor.getOccurrenceInProtectedAreas(groupAreasBy).entrySet());
                        request.setAttribute("locationsInPA", occurrenceProcessor.getNumberOfLocationsInsideProtectedAreas());

                        Map<String, Object> taxonInfo = foop.executeInfoQuery(rlde.getTaxEnt().getOldId());

                        if (rlde.getEcology().getDescription() == null || rlde.getEcology().getDescription().trim().equals("")) {
                            if (taxonInfo.containsKey("ecology") && taxonInfo.get("ecology") != null) {
                                request.setAttribute("ecology", taxonInfo.get("ecology").toString());
                            }
                        } else {
                            request.setAttribute("ecology", rlde.getEcology().getDescription());
                        }
                        request.setAttribute("occurrences", foop);
                    }

                    request.setAttribute("rlde", rlde);
                    // multiple selection fields
                    request.setAttribute("habitatTypes", Arrays.asList(rlde.getEcology().getHabitatTypes()));
                    request.setAttribute("uses", Arrays.asList(rlde.getUsesAndTrade().getUses()));
                    request.setAttribute("proposedConservationActions", Arrays.asList(rlde.getConservation().getProposedConservationActions()));
                    request.setAttribute("proposedStudyMeasures", Arrays.asList(rlde.getConservation().getProposedStudyMeasures()));
                    request.setAttribute("authors", Arrays.asList(cleanArray(rlde.getAssessment().getAuthors(), true)));
                    request.setAttribute("evaluator", Arrays.asList(cleanArray(rlde.getAssessment().getEvaluator(), true)));
                    request.setAttribute("reviewer", Arrays.asList(cleanArray(rlde.getAssessment().getReviewer(), true)));
                    request.setAttribute("allTags", driver.getRedListData().getRedListTags(territory));
                    request.setAttribute("tags", Arrays.asList(rlde.getTags()));
                    List<PreviousAssessment> prev = rlde.getAssessment().getPreviousAssessmentList();
                    if (prev.size() > 2) {
                        prev = new ArrayList<>();
                        prev.add(rlde.getAssessment().getPreviousAssessmentList().get(0));
                        prev.add(rlde.getAssessment().getPreviousAssessmentList().get(1));
                    }
                    if (prev.size() == 0) prev = new ArrayList<>();
                    for (int i = prev.size(); i < 2; i++) {
                        prev.add(new PreviousAssessment());
                    }
                    request.setAttribute("previousAssessments", prev);
                    request.setAttribute("assessment_UpDownList", rlde.getAssessment().suggestUpDownList().getLabel());

                    Revision c1a;
                    Map<Revision, Integer> edits = new TreeMap<>(new Revision.RevisionComparator());
                    for (Revision r : rlde.getRevisions()) {
                        c1a = r.getDayWiseRevision();
                        if (edits.get(c1a) == null)
                            edits.put(c1a, 1);
                        else
                            edits.put(c1a, edits.get(c1a) + 1);
                    }
                    //                request.setAttribute("revisions", rlde.getRevisions());
                    request.setAttribute("revisions", edits.entrySet());
                    //                edits.entrySet().iterator().next().getValue()


                    if (rlde.getAssessment().getPublicationStatus() == RedListEnums.PublicationStatus.PUBLISHED) {
                        // if it's published, block editing all fields
                        boolean canEdit9 = getUser().canEDIT_9_9_4();
                        getUser().revokePrivileges(EDIT_ALL_FIELDS);
                        if (canEdit9) getUser().setEDIT_9_9_4(true);
                    }
                } else {    // multiple IDs provided, batch update
                    getUser().resetEffectivePrivileges();
                    request.setAttribute("warning", "DataSheet.msg.warning.1");
                    request.setAttribute("allTags", driver.getRedListData().getRedListTags(territory));
                    request.setAttribute("multipletaxa", true);
                    List<TaxEnt> taxEnts = driver.getNodeWorkerDriver().getTaxEntByIds(request.getParameterValues("id"));
                    request.setAttribute("taxa", taxEnts);
                    List<PreviousAssessment> prev = new ArrayList<>();
                    for (int i = 0; i < 2; i++) {
                        prev.add(new PreviousAssessment());
                    }
                    request.setAttribute("previousAssessments", prev);
                }
                break;

            case "taxonrecords":
                if (!getUser().canVIEW_OCCURRENCES()) break;
                te = driver.getNodeWorkerDriver().getTaxEntById(getParameterAsKey("id"));
                request.setAttribute("taxon", te);
                if (te.getOldId() != null) {
                    foop.executeOccurrenceQuery(te.getOldId());
                    request.setAttribute("occurrences", foop);
                }
                break;

            case "users":
                List<User> allusers = driver.getAdministration().getAllUsers();
                Map<String, String> taxonMap1 = new HashMap<>();

                Map<String, Integer> responsibleTextCounter = new HashMap<>();
                Map<String, Integer> responsibleAssessmentCounter = new HashMap<>();
                Map<String, Integer> responsibleRevisionCounter = new HashMap<>();

                Iterator<AtomicTaxonPrivilege> tps = driver.getRedListData().getTaxonPrivilegesForAllUsers(territory);
                AtomicTaxonPrivilege atp;
                while(tps.hasNext()) {
                    atp = tps.next();
                    if(atp.isResponsibleForTexts()) {
                        if(responsibleTextCounter.get(atp.getUserId()) == null)
                            responsibleTextCounter.put(atp.getUserId(), 1);
                        else
                            responsibleTextCounter.put(atp.getUserId(), responsibleTextCounter.get(atp.getUserId()) + 1);
                    }

                    if(atp.isResponsibleForAssessment()) {
                        if(responsibleAssessmentCounter.get(atp.getUserId()) == null)
                            responsibleAssessmentCounter.put(atp.getUserId(), 1);
                        else
                            responsibleAssessmentCounter.put(atp.getUserId(), responsibleAssessmentCounter.get(atp.getUserId()) + 1);
                    }

                    if(atp.isResponsibleForRevision()) {
                        if(responsibleRevisionCounter.get(atp.getUserId()) == null)
                            responsibleRevisionCounter.put(atp.getUserId(), 1);
                        else
                            responsibleRevisionCounter.put(atp.getUserId(), responsibleRevisionCounter.get(atp.getUserId()) + 1);
                    }
                }

                // make a map with all taxon names used in privileges
                for(User tmp : allusers) {
                    for (TaxonPrivileges tp : tmp.getTaxonPrivileges()) {
                        for (TaxEnt te1 : driver.getNodeWorkerDriver().getTaxEntByIds(tp.getApplicableTaxa())) {
                            taxonMap1.put(te1.getID(), te1.getName());
                        }
                    }
                }

                request.setAttribute("users", allusers);
                request.setAttribute("taxonMap", taxonMap1);
                request.setAttribute("responsibleTextCounter", responsibleTextCounter);
                request.setAttribute("responsibleAssessmentCounter", responsibleAssessmentCounter);
                request.setAttribute("responsibleRevisionCounter", responsibleRevisionCounter);
                request.setAttribute("redlistprivileges", Privileges.getAllPrivilegesOfTypeAndScope(
                        getUser().getUserType() == User.UserType.ADMINISTRATOR ? null : Privileges.PrivilegeType.REDLISTDATA
                        , null));
                break;

            case "edituser":
                User tmp = driver.getAdministration().getUser(getParameterAsKey("user"));
                List<TaxEnt> applTax = new ArrayList<>();
/*
                for(String i : tmp.getApplicableTaxa()) {
                    applTax.add(driver.getNodeWorkerDriver().getTaxEntById(driver.asNodeKey(i)));
                }
                request.setAttribute("applicableTaxa", applTax);
*/
                Map<String, String> taxonMap = new HashMap<>();
                for(TaxonPrivileges tp : tmp.getTaxonPrivileges()) {
                    for(TaxEnt te1 : driver.getNodeWorkerDriver().getTaxEntByIds(tp.getApplicableTaxa())) {
                        taxonMap.put(te1.getID(), te1.getFullName());
                    }
                }
                request.setAttribute("taxonMap", taxonMap);
                request.setAttribute("tsprivileges", tmp.getTaxonPrivileges());
                request.setAttribute("requesteduser", tmp);
                request.setAttribute("redlistprivileges", Privileges.getAllPrivilegesOfTypeAndScope(
                        getUser().getUserType() == User.UserType.ADMINISTRATOR ? null : Privileges.PrivilegeType.REDLISTDATA
                        , null));
                request.setAttribute("redlisttaxonprivileges", Privileges.getAllPrivilegesOfTypeAndScope(
                        getUser().getUserType() == User.UserType.ADMINISTRATOR ? null : Privileges.PrivilegeType.REDLISTDATA
                        , Privileges.PrivilegeScope.PER_SPECIES));
                break;
        }

        request.getRequestDispatcher("/main-redlistinfo.jsp").forward(request, response);
    }
}
