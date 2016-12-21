package pt.floraon.redlistdata;

import org.apache.commons.lang.ArrayUtils;
import pt.floraon.driver.FloraOnException;
import pt.floraon.geometry.PolygonTheme;
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
        for(User u : allUsers)
            userMap.put(u.getID(), u.getName());

        request.setAttribute("allUsers", allUsers);
        request.setAttribute("userMap", userMap);

        switch (what) {
            case "main":
//                List<TaxEnt> taxEntList = driver.getListDriver().getAllSpeciesOrInferiorTaxEnt(true, true, territory, null, null);
                List<RedListDataEntity> taxEntList = driver.getRedListData().getAllRedListTaxa(territory);
//                taxEntList.get(0).getAssessment().getCategory().getLabel()
                request.setAttribute("specieslist", taxEntList);
                break;

            case "taxon":
                // TODO this should be a user configuration loaded at startup
                PolygonTheme protectedAreas = new PolygonTheme(this.getClass().getResourceAsStream("SNAC.geojson"), "SITE_NAME");
                te = driver.getNodeWorkerDriver().getTaxEntById(getParameterAsKey("id"));
                RedListDataEntity rlde = driver.getRedListData().getRedListDataEntity(territory, getParameterAsKey("id"));
                System.out.println("VO: "+getUser().canVIEW_OCCURRENCES());
                // set privileges for this taxon
                getUser().setEffectivePrivilegesFor(driver, getParameterAsKey("id"));
                System.out.println("VO1: "+getUser().canVIEW_OCCURRENCES());

                request.setAttribute("taxon", te);
                request.setAttribute("synonyms", driver.wrapTaxEnt(getParameterAsKey("id")).getSynonyms());
                if (te.getOldId() != null) {
                    foop.executeOccurrenceQuery(te.getOldId());
                    // TODO clipping polygon must be a user configuration
                    foop.setClippingPolygon(new PolygonTheme(this.getClass().getResourceAsStream("PT_buffer.geojson"), null));

                    OccurrenceProcessor occurrenceProcessor = new OccurrenceProcessor(
                            foop, protectedAreas, sizeOfSquare);

                    request.setAttribute("EOO", occurrenceProcessor.getEOO());

                    request.setAttribute("AOO", (occurrenceProcessor.getNQuads() * sizeOfSquare * sizeOfSquare) / 1000000);
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
                    occurrenceProcessor.exportSVG(new PrintWriter(sw), getUser().canVIEW_OCCURRENCES());
                    request.setAttribute("svgmap", sw.toString());

                    Set<String> groupAreasBy = new HashSet<>();
                    groupAreasBy.add("SITE_NAME");
                    groupAreasBy.add("TIPO");   // TODO this should be user configuration
                    request.setAttribute("occurrenceInProtectedAreas"
                            , occurrenceProcessor.getOccurrenceInProtectedAreas(groupAreasBy).entrySet());
                    request.setAttribute("locationsInPA", occurrenceProcessor.getNumberOfLocationsInsideProtectedAreas());

                    Map<String, Object> taxonInfo = foop.executeInfoQuery(te.getOldId());

                    if (rlde != null) {
                        request.setAttribute("rlde", rlde);
                        request.setAttribute("habitatTypes", Arrays.asList(rlde.getEcology().getHabitatTypes()));
                        request.setAttribute("uses", Arrays.asList(rlde.getUsesAndTrade().getUses()));
                        request.setAttribute("proposedConservationActions", Arrays.asList(rlde.getConservation().getProposedConservationActions()));
                        request.setAttribute("authors", Arrays.asList(rlde.getAssessment().getAuthors()));
                        request.setAttribute("evaluator", Arrays.asList(rlde.getAssessment().getEvaluator()));
                        request.setAttribute("reviewer", Arrays.asList(rlde.getAssessment().getReviewer()));

                        if(rlde.getEcology().getDescription() == null || rlde.getEcology().getDescription().trim().equals("")) {
                            if(taxonInfo.containsKey("ecology") && taxonInfo.get("ecology") != null) {
                                request.setAttribute("ecology", taxonInfo.get("ecology").toString());
                            }
                        } else {
                            request.setAttribute("ecology", rlde.getEcology().getDescription());
                        }
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
                request.setAttribute("assessment_Category", RedListEnums.RedListCategories.values());
                request.setAttribute("assessment_SubCategory", RedListEnums.CRTags.values());
                request.setAttribute("assessment_AssessmentStatus", RedListEnums.AssessmentStatus.values());
//rlde.getAssessment().getSubCategory()

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
                request.setAttribute("users", driver.getAdministration().getAllUsers());
                request.setAttribute("redlistprivileges", User.getAllPrivilegesOfType(
                        getUser().getUserType() == User.UserType.ADMINISTRATOR ? null : User.PrivilegeType.REDLISTDATA));
                break;

            case "edituser":
                User tmp = driver.getAdministration().getUser(getParameterAsKey("user"));
                List<TaxEnt> applTax = new ArrayList<>();
                for(String i : tmp.getApplicableTaxa()) {
                    applTax.add(driver.getNodeWorkerDriver().getTaxEntById(driver.asNodeKey(i)));
                }
                request.setAttribute("requesteduser", tmp);
                request.setAttribute("applicableTaxa", applTax);
                request.setAttribute("redlistprivileges", User.getAllPrivilegesOfType(
                        getUser().getUserType() == User.UserType.ADMINISTRATOR ? null : User.PrivilegeType.REDLISTDATA));
                break;
        }

        request.getRequestDispatcher("/main-redlistinfo.jsp").forward(request, response);
    }
}
