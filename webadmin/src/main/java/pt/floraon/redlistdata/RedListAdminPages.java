package pt.floraon.redlistdata;

import org.apache.commons.lang.ArrayUtils;
import pt.floraon.authentication.Privileges;
import pt.floraon.authentication.entities.TaxonPrivileges;
import pt.floraon.driver.FloraOnException;
import pt.floraon.geometry.PolygonTheme;
import pt.floraon.redlistdata.entities.PreviousAssessment;
import pt.floraon.taxonomy.entities.TaxEnt;
import pt.floraon.authentication.entities.User;
import pt.floraon.redlistdata.entities.RedListDataEntity;
import pt.floraon.redlistdata.entities.RedListEnums;
import pt.floraon.server.FloraOnServlet;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;
import java.util.List;

import static pt.floraon.authentication.entities.User.EDIT_ALL_FIELDS;

/**
 * Main page of red list data
 * Created by Miguel Porto on 01-11-2016.
 */
@WebServlet("/redlist/*")
public class RedListAdminPages extends FloraOnServlet {
    @Override
    public void doFloraOnGet() throws ServletException, IOException, FloraOnException {
        String what;
        TaxEnt te;
        long sizeOfSquare = 2000;
/*
Gson gs = new GsonBuilder().setPrettyPrinting().create();
System.out.println(gs.toJson(getUser()));
*/

        request.setAttribute("uuid", "sk05");

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
            userMap.put(u.getIDURLEncoded(), u.getName());
        }

        request.setAttribute("allUsers", allUsers);
        request.setAttribute("userMap", userMap);

        switch (what) {
            case "main":
//                Shows the taxon list
//                List<TaxEnt> taxEntList = driver.getListDriver().getAllSpeciesOrInferiorTaxEnt(true, true, territory, null, null);
                List<RedListDataEntity> taxEntList = driver.getRedListData().getAllRedListTaxa(territory);
//                taxEntList.get(0).getAssessment().getCategory().getLabel()
                request.setAttribute("specieslist", taxEntList);
                break;

            case "taxon":
                // TODO this should be a user configuration loaded at startup
                PolygonTheme protectedAreas = new PolygonTheme(this.getClass().getResourceAsStream("SNAC.geojson"), "SITE_NAME");
//                te = driver.getNodeWorkerDriver().getTaxEntById(getParameterAsKey("id"));
                RedListDataEntity rlde = driver.getRedListData().getRedListDataEntity(territory, getParameterAsKey("id"));
                if(rlde == null) return;
                // set privileges for this taxon
                getUser().setEffectivePrivilegesFor(driver, getParameterAsKey("id"));

                request.setAttribute("taxon", rlde.getTaxEnt());
                request.setAttribute("synonyms", driver.wrapTaxEnt(getParameterAsKey("id")).getSynonyms());
                if (rlde.getTaxEnt().getOldId() != null) {
                    foop.executeOccurrenceQuery(rlde.getTaxEnt().getOldId());
                    // TODO clipping polygon must be a user configuration
                    foop.setClippingPolygon(new PolygonTheme(this.getClass().getResourceAsStream("PT_buffer.geojson"), null));

                    OccurrenceProcessor occurrenceProcessor = new OccurrenceProcessor(
                            foop, protectedAreas, sizeOfSquare);

                    // if it is published, AOO and EOO are from the data sheet, otherwise they are computed from
                    // live occurrences
                    Double EOO = null, AOO = null;
                    if(rlde.getAssessment().getPublicationStatus() == RedListEnums.PublicationStatus.PUBLISHED) {
                        EOO = rlde.getGeographicalDistribution().getEOO();
                        AOO = rlde.getGeographicalDistribution().getAOO();
                    }
                    if(EOO == null) EOO = occurrenceProcessor.getEOO();
                    if(AOO == null) AOO = (occurrenceProcessor.getNQuads() * sizeOfSquare * sizeOfSquare) / 1000000d;
                    request.setAttribute("EOO", EOO);
                    request.setAttribute("AOO", AOO);
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
                    request.setAttribute("meanLocationArea", sum );

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

                    if(rlde.getEcology().getDescription() == null || rlde.getEcology().getDescription().trim().equals("")) {
                        if(taxonInfo.containsKey("ecology") && taxonInfo.get("ecology") != null) {
                            request.setAttribute("ecology", taxonInfo.get("ecology").toString());
                        }
                    } else {
                        request.setAttribute("ecology", rlde.getEcology().getDescription());
                    }
                    request.setAttribute("occurrences", foop);
                }
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
                request.setAttribute("assessment_Category", RedListEnums.RedListCategories.valuesNotUpDownListed());
                request.setAttribute("assessment_SubCategory", RedListEnums.CRTags.values());
                request.setAttribute("assessment_RegionalAssessment", RedListEnums.YesNoLikelyUnlikely.values());
                request.setAttribute("assessment_UpDownListing", RedListEnums.UpDownList.values());
                request.setAttribute("assessment_TextStatus", RedListEnums.TextStatus.values());
                request.setAttribute("assessment_AssessmentStatus", RedListEnums.AssessmentStatus.values());
                request.setAttribute("assessment_ReviewStatus", RedListEnums.ReviewStatus.values());
                request.setAttribute("assessment_PublicationStatus", RedListEnums.PublicationStatus.values());

                request.setAttribute("rlde", rlde);
                request.setAttribute("habitatTypes", Arrays.asList(rlde.getEcology().getHabitatTypes()));
                request.setAttribute("uses", Arrays.asList(rlde.getUsesAndTrade().getUses()));
                request.setAttribute("proposedConservationActions", Arrays.asList(rlde.getConservation().getProposedConservationActions()));
                request.setAttribute("authors", Arrays.asList(rlde.getAssessment().getAuthors()));
                request.setAttribute("evaluator", Arrays.asList(rlde.getAssessment().getEvaluator()));
                request.setAttribute("reviewer", Arrays.asList(rlde.getAssessment().getReviewer()));
                List<PreviousAssessment> prev = rlde.getAssessment().getPreviousAssessmentList();
                if(prev.size() == 0) prev = new ArrayList<>();
                for (int i = prev.size(); i < 6; i++) {
                    prev.add(new PreviousAssessment());
                }
                request.setAttribute("previousAssessments", prev);

                request.setAttribute("assessment_UpDownList", rlde.getAssessment().suggestUpDownList().getLabel());
                request.setAttribute("revisions", rlde.getRevisions());

                if(rlde.getAssessment().getPublicationStatus() == RedListEnums.PublicationStatus.PUBLISHED) {
                    // if it's published, block editing all fields
                    boolean canEdit9 = getUser().canEDIT_9_9_4();
                    getUser().revokePrivileges(EDIT_ALL_FIELDS);
                    if(canEdit9) getUser().setEDIT_9_9_4(true);
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
                for(User tmp : allusers) {
                    for (TaxonPrivileges tp : tmp.getTaxonPrivileges()) {
                        for (TaxEnt te1 : driver.getNodeWorkerDriver().getTaxEntByIds(tp.getApplicableTaxa())) {
                            taxonMap1.put(te1.getID(), te1.getFullName());
                        }
                    }
                }

                request.setAttribute("users", allusers);
                request.setAttribute("taxonMap", taxonMap1);
                request.setAttribute("redlistprivileges", Privileges.getAllPrivilegesOfType(
                        getUser().getUserType() == User.UserType.ADMINISTRATOR ? null : User.PrivilegeType.REDLISTDATA));
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
                request.setAttribute("redlistprivileges", Privileges.getAllPrivilegesOfType(
                        getUser().getUserType() == User.UserType.ADMINISTRATOR ? null : User.PrivilegeType.REDLISTDATA));
                break;
        }

        request.getRequestDispatcher("/main-redlistinfo.jsp").forward(request, response);
    }
}
