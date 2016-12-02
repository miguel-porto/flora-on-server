package pt.floraon.redlistdata;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jline.internal.Log;
import org.apache.commons.math3.ml.clustering.*;
import org.apache.commons.math3.ml.clustering.Cluster;
import org.apache.commons.math3.ml.clustering.DBSCANClusterer;
import pt.floraon.driver.FloraOnException;
import pt.floraon.entities.TaxEnt;
import pt.floraon.entities.User;
import pt.floraon.redlistdata.entities.RedListDataEntity;
import pt.floraon.redlistdata.entities.RedListEnums;
import pt.floraon.server.FloraOnServlet;
import pt.floraon.utmlatlong.GrahamScan;
import pt.floraon.utmlatlong.Point2D;
import pt.floraon.utmlatlong.UTMCoordinate;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.util.*;

/**
 * Main page of red list data
 * Created by Miguel Porto on 01-11-2016.
 */
@WebServlet("/redlist/*")
public class RedListAdmin extends FloraOnServlet {
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

        final OccurrenceProvider foop = driver.getRedListData().getOccurrenceProviders().get(0);

        request.setAttribute("what", what = getParameterAsString("w", "main"));
        switch (what) {
            case "main":
//                List<TaxEnt> taxEntList = driver.getListDriver().getAllSpeciesOrInferiorTaxEnt(true, true, territory, null, null);
                List<RedListDataEntity> taxEntList = driver.getRedListData().getAllRedListTaxa(territory);
//                taxEntList.get(0).getInferredStatus().getStatusSummary()
                request.setAttribute("specieslist", taxEntList);
                break;

            case "svgmap":
                response.getWriter().println("AAINCLUE<svg></svg>");
                return;
//                break;

            case "taxon":
                te = driver.getNodeWorkerDriver().getTaxEntById(getParameterAsKey("id"));
                RedListDataEntity rlde = driver.getRedListData().getRedListDataEntity(territory, getParameterAsKey("id"));
                request.setAttribute("taxon", te);
                request.setAttribute("synonyms", driver.wrapTaxEnt(getParameterAsKey("id")).getSynonyms());
                if (te.getOldId() != null) {
                    try {
                        foop.executeOccurrenceQuery(te.getOldId());
                    } catch (URISyntaxException e) {
                        throw new FloraOnException(e.getMessage());
                    }

                    OccurrenceMap map = new OccurrenceMap(foop, sizeOfSquare);

                    request.setAttribute("EOO", map.getEOO());

                    request.setAttribute("AOO", (map.getNQuads() * sizeOfSquare * sizeOfSquare) / 1000000);
                    request.setAttribute("sizeofsquare", sizeOfSquare / 1000);
                    request.setAttribute("nquads", map.getNQuads());

                    request.setAttribute("nclusters", map.getNLocations());
                    StringWriter sw = new StringWriter();
                    map.exportSVG(new PrintWriter(sw));
                    request.setAttribute("svgmap", sw.toString());

                    request.setAttribute("occurrenceInProtectedAreas", map.getOccurrenceInProtectedAreas());
                    request.setAttribute("locationsInPA", map.getNumberOfLocationsInsideProtectedAreas());

/*
                    Gson gs = new GsonBuilder().setPrettyPrinting().create();
                    System.out.println(gs.toJson(rlde));
*/

                    if (rlde != null) request.setAttribute("rlde", rlde);
                    request.setAttribute("territory", territory);

                    // enums
                    request.setAttribute("geographicalDistribution_DeclineDistribution", RedListEnums.DeclineDistribution.values());
                    request.setAttribute("population_NrMatureIndividualsCategory", RedListEnums.NrMatureIndividuals.values());
                    request.setAttribute("population_TypeOfEstimate", RedListEnums.TypeOfPopulationEstimate.values());
                    request.setAttribute("population_PopulationDecline", RedListEnums.DeclinePopulation.values());
                    request.setAttribute("population_SeverelyFragmented", RedListEnums.SeverelyFragmented.values());
                    request.setAttribute("population_ExtremeFluctuations", RedListEnums.YesNoNA.values());
                    request.setAttribute("ecology_HabitatTypes", RedListEnums.HabitatTypes.values());
                    request.setAttribute("ecology_GenerationLength", RedListEnums.GenerationLength.values());
                    request.setAttribute("usesAndTrade_Uses", RedListEnums.Uses.values());
                    request.setAttribute("usesAndTrade_Overexploitation", RedListEnums.Overexploitation.values());
                    request.setAttribute("conservation_ConservationPlans", RedListEnums.YesNoNA.values());
                    request.setAttribute("conservation_ExSituConservation", RedListEnums.YesNoNA.values());
                    request.setAttribute("conservation_ProposedConservationActions", RedListEnums.ProposedConservationActions.values());
                    request.setAttribute("assessment_Category", RedListEnums.RedListCategories.values());
                    request.setAttribute("assessment_AssessmentStatus", RedListEnums.AssessmentStatus.values());

                    request.setAttribute("habitatTypes", Arrays.asList(rlde.getEcology().getHabitatTypes()));
                    request.setAttribute("uses", Arrays.asList(rlde.getUsesAndTrade().getUses()));
                    request.setAttribute("proposedConservationActions", Arrays.asList(rlde.getConservation().getProposedConservationActions()));
                    request.setAttribute("authors", Arrays.asList(rlde.getAssessment().getAuthors()));
                    request.setAttribute("evaluator", Arrays.asList(rlde.getAssessment().getEvaluator()));
                    request.setAttribute("reviewer", Arrays.asList(rlde.getAssessment().getReviewer()));

                    // make a map of user IDs and names
                    List<User> allUsers = driver.getAdministration().getAllUsers();
                    Map<String, String> userMap = new HashMap<>();
                    for(User u : allUsers)
                        userMap.put(u.getID(), u.getName());

                    request.setAttribute("allUsers", allUsers);
                    request.setAttribute("userMap", userMap);

                    request.setAttribute("occurrences", foop);
                }
                break;

            case "taxonrecords":
                if (!getUser().canVIEW_OCCURRENCES()) break;

                te = driver.getNodeWorkerDriver().getTaxEntById(getParameterAsKey("id"));
                request.setAttribute("taxon", te);
                if (te.getOldId() != null) {
                    try {
                        foop.executeOccurrenceQuery(te.getOldId());
                    } catch (URISyntaxException e) {
                        throw new FloraOnException(e.getMessage());
                    }
                    request.setAttribute("occurrences", foop);
                }
                break;

            case "users":
                request.setAttribute("users", driver.getAdministration().getAllUsers());
                request.setAttribute("redlistprivileges", User.getAllPrivilegesOfType(
                        getUser().getUserType() == User.UserType.ADMINISTRATOR ? null : User.PrivilegeType.REDLISTDATA));
                break;

            case "edituser":
                request.setAttribute("requesteduser", driver.getAdministration().getUser(getParameterAsKey("user")));
                request.setAttribute("redlistprivileges", User.getAllPrivilegesOfType(
                        getUser().getUserType() == User.UserType.ADMINISTRATOR ? null : User.PrivilegeType.REDLISTDATA));
                break;
        }

        request.getRequestDispatcher("/main-redlistinfo.jsp").forward(request, response);
    }
}
