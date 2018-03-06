package pt.floraon.occurrences;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.jfree.util.Log;
import pt.floraon.authentication.entities.User;
import pt.floraon.driver.FloraOnException;
import pt.floraon.driver.interfaces.INodeKey;
import pt.floraon.driver.jobs.JobRunner;
import pt.floraon.driver.jobs.JobSubmitter;
import pt.floraon.driver.utils.StringUtils;
import pt.floraon.geometry.CoordinateConversion;
import pt.floraon.occurrences.entities.Inventory;
import pt.floraon.occurrences.entities.InventoryList;
import pt.floraon.occurrences.entities.OBSERVED_IN;
import pt.floraon.redlistdata.dataproviders.SimpleOccurrenceDataProvider;
import pt.floraon.server.FloraOnServlet;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.*;

/**
 * Created by miguel on 05-02-2017.
 */
@WebServlet("/occurrences/*")
public class OccurrencesMainPage extends FloraOnServlet {
    @Override
    public void doFloraOnGet(ThisRequest thisRequest) throws ServletException, IOException, FloraOnException {
        final HttpServletRequest request = thisRequest.request;
        List<InventoryList> filesList = new ArrayList<>();
        User user = thisRequest.getUser();
        Integer page = thisRequest.getParameterAsInteger("p", null);
        Integer count = 250;    // how many occurrences or inventories per page
        String filter;
        int tmp;
        HttpSession session = request.getSession(false);

        if(user.isGuest()) {
            thisRequest.response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        // fetch page from querystring or session
        if(page == null) {
            page = (Integer) session.getAttribute("page");
            if(page == null)
                page = 1;
        } else
            session.setAttribute("page", page);

        // fetch filter from querystring or session
        filter = thisRequest.getParameterAsString("filter");
        if(filter == null) {
            filter = (String) session.getAttribute("filter");
        } else if(filter.equals("")) {
            session.removeAttribute("filter");
            filter = null;
        }

        if(session.getAttribute("option-view250") != null && (Boolean) session.getAttribute("option-view250")) count = 250;
        if(session.getAttribute("option-view1000") != null && (Boolean) session.getAttribute("option-view1000")) count = 1000;
        if(session.getAttribute("option-view5000") != null && (Boolean) session.getAttribute("option-view5000")) count = 5000;
        if(session.getAttribute("option-viewall") != null && (Boolean) session.getAttribute("option-viewall")) count = 10000000;

        request.setAttribute("user", user);
        request.setAttribute("occperpage", count);
        request.setAttribute("p", page);

        // make a map of user IDs and names
        List<User> allUsers = driver.getAdministration().getAllUsers(false);
        Map<String, String> userMap = new HashMap<>();
        for(User u : allUsers)
            userMap.put(u.getID(), u.getName());
        request.setAttribute("allUsers", allUsers);
        request.setAttribute("userMap", userMap);

        String what = thisRequest.getParameterAsString("w");
        if(what == null) what = "main";

        switch(what) {
            case "main":
                INodeKey tu1;
                if(session.getAttribute("option-allusers") != null && (Boolean) session.getAttribute("option-allusers"))
                    tu1 = null;
                else
                    tu1 = driver.asNodeKey(user.getID());

                tmp = driver.getOccurrenceDriver().getInventoriesOfMaintainerCount(tu1);
                if (filter == null) {
                    request.setAttribute("nroccurrences", count > tmp ? tmp : count);
                    request.setAttribute("nrtotaloccurrences", tmp);
                    request.setAttribute("inventories"
                            , driver.getOccurrenceDriver().getInventoriesOfMaintainer(tu1, (page - 1) * count, count));
                } else {
                    session.setAttribute("filter", filter);
                    request.setAttribute("nroccurrences", count);
                    request.setAttribute("nrtotaloccurrences", tmp);
                    Map<String, String> parsedFilter;
                    try {
                        parsedFilter = driver.getOccurrenceDriver().parseFilterExpression(filter);
                        request.setAttribute("inventories"
                                , driver.getOccurrenceDriver().findInventoriesByFilter(parsedFilter, tu1, (page - 1) * count, count));
                    } catch(FloraOnException e) {
                        request.setAttribute("warning", "O filtro não foi compreendido: " + e.getMessage());
                    }

                }
                break;

            case "fixissues":
                InventoryList il = driver.getOccurrenceDriver().matchTaxEntNames(
                        driver.getOccurrenceDriver().getUnmatchedOccurrencesOfMaintainer(driver.asNodeKey(user.getID()))
                        , false, true);
                request.setAttribute("nomatchquestions", il.getQuestions());
                request.setAttribute("matchwarnings", il.getVerboseWarnings());
                request.setAttribute("nomatches", il.getVerboseErrors());
                request.setAttribute("parseerrors", il.getParseErrors());
                break;

            case "openinventory":
                if(!StringUtils.isStringEmpty(thisRequest.getParameterAsString("id"))) {
                    request.setAttribute("inventories"
                            , driver.getOccurrenceDriver().getInventoriesByIds(new String[] {thisRequest.getParameterAsString("id")}));
                } else
                    request.setAttribute("inventories"
                            , driver.getOccurrenceDriver().getInventoriesOfMaintainer(driver.asNodeKey(user.getID()), null, null));

                break;

            case "occurrenceview":
                INodeKey tu;
                if(session.getAttribute("option-allusers") != null && (Boolean) session.getAttribute("option-allusers"))
                    tu = null;
                else
                    tu = driver.asNodeKey(user.getID());

                tmp = driver.getOccurrenceDriver().getOccurrencesOfMaintainerCount(tu);

                if (filter == null) {
                    request.setAttribute("nroccurrences", count > tmp ? tmp : count);
                    request.setAttribute("nrtotaloccurrences", tmp);
                    request.setAttribute("occurrences"
                            , driver.getOccurrenceDriver().getOccurrencesOfMaintainer(tu,false,(page - 1) * count, count));
                } else {
                    session.setAttribute("filter", filter);
                    request.setAttribute("nroccurrences", count);   // TODO this should be the number after filtering, but we don't know it by now
                    request.setAttribute("nrtotaloccurrences", tmp);
                    Map<String, String> parsedFilter = new HashMap<>();
                    try {
                        parsedFilter = driver.getOccurrenceDriver().parseFilterExpression(filter);
                        request.setAttribute("occurrences"
                                , driver.getOccurrenceDriver().findOccurrencesByFilter(parsedFilter, tu, (page - 1) * count, count));
                    } catch(FloraOnException e) {
                        request.setAttribute("warning", "O filtro não foi compreendido: " + e.getMessage());
                    }


                    if(tu == null && parsedFilter.containsKey("NA")) {
                        List<SimpleOccurrenceDataProvider> sodps = driver.getRedListData().getSimpleOccurrenceDataProviders();
                        for (SimpleOccurrenceDataProvider edp : sodps) {
                            if (edp.canQueryText()) {
                                // TODO: only works for one provider
                                edp.executeOccurrenceTextQuery(parsedFilter.get("NA"));
                                request.setAttribute("externaloccurrences", edp.iterator());
                            }
                        }
                    }
                }

                if(tu != null)  // if it is all occurrences, we skip this check cause it takes a few seconds
                    request.setAttribute("nproblems", driver.getOccurrenceDriver().getUnmatchedOccurrencesOfMaintainerCount(tu));
                request.setAttribute("historicalYear", 1991);   // TODO this should be user configuration
                break;

            case "uploads":
                user = thisRequest.refreshUser();
                List<String> uts = new ArrayList<>();
                uts.addAll(user.getUploadedTables());   // clone
                for(String ut : uts) {
                    try {
                        filesList.add(Common.readInventoryListFromFile(ut));
                    } catch (IOException e) {
                        // table doesn't exist any more
                        Log.info("Removing reference to uploaded table" + ut);
                        List<String> tmp1 = user.getUploadedTables();
                        tmp1.remove(ut);
                        driver.getNodeWorkerDriver().updateDocument(driver.asNodeKey(user.getID()), "uploadedTables", tmp1);
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                request.setAttribute("filesList", filesList);

                List<JobRunner> pending = new ArrayList<>();
                for(String job : JobSubmitter.getJobList()) {
//                    jline.internal.Log.info(job + JobSubmitter.getJob(job).getOwner().getName());
                    if(user.equals(JobSubmitter.getJob(job).getOwner())
                            && OccurrenceImporterJob.class.isAssignableFrom(JobSubmitter.getJob(job).getJob().getClass()))
                        pending.add(JobSubmitter.getJob(job));
                }

                request.setAttribute("pendingFiles", pending);
//                filesList.get(0).getQuestions().get("j").getOptions().iterator().next().getID()
                break;

            case "setoption":
//                    System.out.println("SET " + "option-" + thisRequest.getParameterAsString("n")+ " to "+thisRequest.getParameterAsBooleanNoNull("v"));
                String optionName = thisRequest.getParameterAsString("n");
                if(optionName.startsWith("view")) {
                    session.setAttribute("option-view250", false);
                    session.setAttribute("option-view1000", false);
                    session.setAttribute("option-view5000", false);
                    session.setAttribute("option-viewall", false);
                    session.setAttribute("option-" + optionName, true);
                } else {
                    session.setAttribute("option-" + optionName
                            , thisRequest.getParameterAsBooleanNoNull("v"));
                }
                thisRequest.success("Set");
                break;

            case "downloadoccurrencetable":
                thisRequest.response.setContentType("text/csv; charset=utf-8");
                thisRequest.response.addHeader("Content-Disposition", "attachment;Filename=\"occurrences.csv\"");
                INodeKey u;
                if(session.getAttribute("option-allusers") != null && (Boolean) session.getAttribute("option-allusers"))
                    u = null;
                else
                    u = driver.asNodeKey(user.getID());

                CSVPrinter csv = new CSVPrinter(thisRequest.response.getWriter(), CSVFormat.EXCEL);
                Iterator<Inventory> it1 = driver.getOccurrenceDriver().getOccurrencesOfMaintainer(u, true,null, null);
//                csv.printRecord("gpsCode", "verbLocality", "latitude", "longitude", "mgrs", "date", "taxa", "comment", "privateNote");
                csv.printRecord("date", "observers", "latitude", "longitude", "mgrs", "verbLocality", "precision"
                        , "gpsCode", "taxa", "confidence", "phenoState", "abundance", "method", "photo", "collected"
                        , "specificThreats", "comment", "privateNote", "year", "month", "day");
                while(it1.hasNext()) {
                    Inventory i2 = it1.next();
                    OBSERVED_IN oi = i2._getTaxa()[0];
//                    TaxEnt te = oi.getTaxEnt();

                    csv.printRecord(i2._getDateYMD(), pt.floraon.driver.utils.StringUtils.implode(", ", i2._getObserverNames()), i2._getLatitude(), i2._getLongitude()
                            , CoordinateConversion.LatLongToMGRS(i2._getLatitude(), i2._getLongitude(), 1000)
                            , i2.getVerbLocality(), i2.getPrecision(), i2.getCode(), oi.getVerbTaxon(), oi.getConfidence()
                            , oi.getPhenoState(), oi.getAbundance(), oi.getTypeOfEstimate(), oi.getHasPhoto(), oi.getHasSpecimen()
                            , oi.getSpecificThreats(), oi.getComment(), oi.getPrivateComment(), i2.getYear(), i2.getMonth(), i2.getDay());

                }
                csv.close();
                return;
        }
        request.getRequestDispatcher("/main-occurrences.jsp").forward(request, thisRequest.response);

    }
}
