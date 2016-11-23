package pt.floraon.redlistdata;

import pt.floraon.driver.FloraOnException;
import pt.floraon.entities.TaxEnt;
import pt.floraon.redlistdata.entities.RedListDataEntity;
import pt.floraon.redlistdata.entities.RedListEnums;
import pt.floraon.server.FloraOnServlet;
import pt.floraon.utmlatlong.GrahamScan;
import pt.floraon.utmlatlong.Point2D;
import pt.floraon.utmlatlong.UTMCoordinate;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import java.io.IOException;
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
        if(request.getSession().getAttribute("user") == null) {
            request.getRequestDispatcher("/main-redlistinfo.jsp").forward(request, response);
            return;
        }
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

            case "taxon":
                te = driver.getNodeWorkerDriver().getTaxEntById(getParameterAsKey("id"));
                RedListDataEntity rlde = driver.getRedListData().getRedListDataEntity(territory, getParameterAsKey("id"));
                request.setAttribute("taxon", te);
                request.setAttribute("synonyms", driver.wrapTaxEnt(getParameterAsKey("id")).getSynonyms());
                if(te.getOldId() != null) {
                    try {
                        foop.executeOccurrenceQuery(te.getOldId());
                    } catch (URISyntaxException e) {
                        throw new FloraOnException(e.getMessage());
                    }
                    List<UTMCoordinate> utmCoords = new ArrayList<>(foop.size());
                    List<Point2D> points = new ArrayList<>();
                    UTMCoordinate tmp;
                    Set<String> utmZones = new HashSet<>();
                    for (OccurrenceProvider.SimpleOccurrence so : foop) {
                        utmCoords.add(tmp = so.getUTMCoordinates());
                        points.add(new Point2D(tmp));
                        utmZones.add(((Integer) tmp.getXZone()).toString() + java.lang.Character.toString(tmp.getYZone()));
                    }
                    if(foop.size() >= 3) {
                        // compute convex hull
                        // first convert to UTM
                        // TODO use a projection without zones
                        if (utmZones.size() > 1)
                            request.setAttribute("warning", "EOO computation is inaccurate for data " +
                                    "points spreading more than one UTM zone.");

                        Stack<Point2D> hull = (Stack<Point2D>) new GrahamScan(points.toArray(new Point2D[0])).hull();
                        hull.add(hull.get(0));
                        double sum = 0.0;
                        for (int i = 0; i < hull.size() - 1; i++) {
                            sum = sum + (hull.get(i).x() * hull.get(i + 1).y()) - (hull.get(i).y() * hull.get(i + 1).x());
                        }
                        sum = 0.5 * sum;
/*
${occ.getUTMCoordinates().getXZone()}${occ.getUTMCoordinates().getYZone()} ${occ.getUTMCoordinates().getX()} ${occ.getUTMCoordinates().getY()}<br/>*/
/*
                    for(Point2D p : hull) {
                        System.out.print(p.x());
                        System.out.print(", ");
                        System.out.println(p.y());
                    }
*/
                        request.setAttribute("EOO", sum / 1000000);
                    } else
                        request.setAttribute("EOO", null);

                    // now calculate the number of UTM squares occupied
                    Long qx, qy;
                    Set<String> quads = new HashSet<>();
                    for (UTMCoordinate u : utmCoords) {
                        qx = (long) Math.floor(u.getX() / sizeOfSquare);
                        qy = (long) Math.floor(u.getY() / sizeOfSquare);
                        quads.add(qx + "," + qy);
                    }
                    request.setAttribute("AOO", (quads.size() * sizeOfSquare * sizeOfSquare) / 1000000);
                    request.setAttribute("sizeofsquare", sizeOfSquare / 1000);
                    request.setAttribute("nquads", quads.size());
/*
                    Gson gs = new GsonBuilder().setPrettyPrinting().create();
                    System.out.println(gs.toJson(rlde));
*/

                    if(rlde != null) request.setAttribute("rlde", rlde);
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

                    request.setAttribute("habitatTypes", Arrays.asList(rlde.getEcology().getHabitatTypes()));
                    request.setAttribute("uses", Arrays.asList(rlde.getUsesAndTrade().getUses()));
                    request.setAttribute("proposedConservationActions", Arrays.asList(rlde.getConservation().getProposedConservationActions()));

                    request.setAttribute("occurrences", foop);
                }
                break;

            case "taxonrecords":
                if(request.getSession().getAttribute("user") == null) break;

                te = driver.getNodeWorkerDriver().getTaxEntById(getParameterAsKey("id"));
                request.setAttribute("taxon", te);
                if(te.getOldId() != null) {
                    try {
                        foop.executeOccurrenceQuery(te.getOldId());
                    } catch (URISyntaxException e) {
                        throw new FloraOnException(e.getMessage());
                    }
                    request.setAttribute("occurrences", foop);
                }
                break;
        }

        request.getRequestDispatcher("/main-redlistinfo.jsp").forward(request, response);
    }
}
