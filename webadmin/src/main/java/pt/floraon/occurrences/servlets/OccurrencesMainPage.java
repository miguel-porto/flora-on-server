package pt.floraon.occurrences.servlets;

import jline.internal.Log;
import pt.floraon.authentication.entities.User;
import pt.floraon.driver.FloraOnException;
import pt.floraon.driver.interfaces.INodeKey;
import pt.floraon.driver.jobs.JobRunner;
import pt.floraon.driver.jobs.JobSubmitter;
import pt.floraon.driver.utils.StringUtils;
import pt.floraon.occurrences.Common;
import pt.floraon.occurrences.OccurrenceConstants;
import pt.floraon.occurrences.OccurrenceImporterJob;
import pt.floraon.occurrences.entities.Inventory;
import pt.floraon.occurrences.entities.InventoryList;
import pt.floraon.occurrences.entities.Occurrence;
import pt.floraon.occurrences.fields.flavours.IOccurrenceFlavour;
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
        INodeKey maintainer;
        int tmp;
        HttpSession session = request.getSession(false);
        if(user.isAdministrator() && !thisRequest.isOptionSet("allusers"))
            thisRequest.setOption("allusers", true);

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

/*
        Enumeration<String> attrs = session.getAttributeNames();
        while(attrs.hasMoreElements()) {
            String attr = attrs.nextElement();
            if(attr.startsWith("option-view-")) {
                if((Boolean) session.getAttribute(attr)) {
                    String howMany = attr.split("-")[2];
                    if(howMany.equals("all"))
                        count = 10000000;
                    else
                        count = Integer.parseInt(howMany);
                }
            }
        }
*/
        String howMany;
        if((howMany = (String) thisRequest.getOption("view")) != null)
            count = Integer.parseInt(howMany);
/*
        if(session.getAttribute("option-view250") != null && (Boolean) session.getAttribute("option-view250")) count = 250;
        if(session.getAttribute("option-view1000") != null && (Boolean) session.getAttribute("option-view1000")) count = 1000;
        if(session.getAttribute("option-view5000") != null && (Boolean) session.getAttribute("option-view5000")) count = 5000;
        if(session.getAttribute("option-viewall") != null && (Boolean) session.getAttribute("option-viewall")) count = 10000000;
*/

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
            case "main":    // this is the inventory summary view
                INodeKey tu1;
                if(thisRequest.isOptionTrue("allusers"))
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

                // Flavours
                // the flavour for new inventories
                request.setAttribute("flavourfields", OccurrenceConstants.occurrenceManagerFlavours.get("inventory"));
                // the flavour for the inventory summary
//                request.setAttribute("summaryfields", OccurrenceConstants.occurrenceManagerFlavours.get("inventorySummary"));

                // the flavour for the inventory summary
                Map<String, IOccurrenceFlavour> flv2 = user.getEffectiveOccurrenceFlavours(User.FlavourFilter.ONLY_INVENTORY);
                request.setAttribute("flavourList", user.getEffectiveOccurrenceFlavourNames(flv2));
                String flavour2 = (String) thisRequest.getOption("flavour");
                if(!flv2.containsKey(flavour2))
                    thisRequest.setOption("flavour", flavour2 = "inventory");

                request.setAttribute("summaryfields", flv2.get(flavour2));
                break;

            case "openinventory":
                if(!StringUtils.isStringEmpty(thisRequest.getParameterAsString("id"))) {
                    request.setAttribute("inventories"
                            , driver.getOccurrenceDriver().getInventoriesByIds(new String[] {thisRequest.getParameterAsString("id")}));
                } else
                    request.setAttribute("inventories"
                            , driver.getOccurrenceDriver().getInventoriesOfMaintainer(driver.asNodeKey(user.getID()), null, null));

                // Flavours
                // Get the list of flavours
                Map<String, IOccurrenceFlavour> flv = user.getEffectiveOccurrenceFlavours(User.FlavourFilter.ONLY_INVENTORY);
                request.setAttribute("flavourList", user.getEffectiveOccurrenceFlavourNames(flv));

                // get the list of fields for the selected flavour
                String flavour = (String) thisRequest.getOption("flavour");
                if(StringUtils.isStringEmpty(flavour) || !flv.containsKey(flavour))
                    thisRequest.setOption("flavour", flavour = "inventory");

                request.setAttribute("flavourfields", flv.get(flavour));
                break;

            case "fixissues":
                if(thisRequest.isOptionTrue("allusers"))
                    maintainer = null;
                else
                    maintainer = driver.asNodeKey(user.getID());
                Iterator<Inventory> itInv = driver.getOccurrenceDriver().getUnmatchedOccurrencesOfMaintainer(maintainer);
                InventoryList il = driver.getOccurrenceDriver().matchTaxEntNames(itInv, false, true);

                request.setAttribute("nomatchquestions", il.getQuestions());
                request.setAttribute("matchwarnings", il.getVerboseWarnings());
                request.setAttribute("nomatches", il.getVerboseErrors());
                request.setAttribute("parseerrors", il.getParseErrors());
                break;

            case "occurrenceview":  // The main view of occurrences, with many flavours
                if(thisRequest.isOptionTrue("allusers"))
                    maintainer = null;
                else
                    maintainer = driver.asNodeKey(user.getID());

                tmp = driver.getOccurrenceDriver().getOccurrencesOfMaintainerCount(maintainer);

                if (filter == null) {
                    request.setAttribute("nroccurrences", count > tmp ? tmp : count);
                    request.setAttribute("nrtotaloccurrences", tmp);
                    request.setAttribute("occurrences"
                            , driver.getOccurrenceDriver().getOccurrencesOfMaintainer(maintainer,false,(page - 1) * count, count));
                } else {
                    session.setAttribute("filter", filter);
                    request.setAttribute("nroccurrences", count);   // TODO this should be the number after filtering, but we don't know it by now
                    request.setAttribute("nrtotaloccurrences", tmp);
                    Map<String, String> parsedFilter = new HashMap<>();
                    try {
                        parsedFilter = driver.getOccurrenceDriver().parseFilterExpression(filter);
                        request.setAttribute("occurrences"
                                , driver.getOccurrenceDriver().findOccurrencesByFilter(parsedFilter, maintainer, (page - 1) * count, count));
                    } catch(FloraOnException e) {
                        request.setAttribute("warning", "O filtro não foi compreendido: " + e.getMessage());
                    }


                    if(maintainer == null && parsedFilter.containsKey("NA")) {
                        List<SimpleOccurrenceDataProvider> sodps = driver.getRedListData().getSimpleOccurrenceDataProviders();
                        for (SimpleOccurrenceDataProvider edp : sodps) {
                            if (edp.canQueryText()) {
                                // TODO: only works for one provider
                                edp.executeOccurrenceTextQuery(parsedFilter.get("NA"));
                                request.setAttribute("externaloccurrences", edp.iterator());
                                break;
                            }
                        }
                    }
                }

                if(maintainer != null)  // if it is all occurrences, we skip this check cause it takes a few seconds
                    request.setAttribute("nproblems", driver.getOccurrenceDriver().getUnmatchedOccurrencesOfMaintainerCount(maintainer));
                else
                    request.setAttribute("nproblems", 1);

                request.setAttribute("historicalYear", 1991);   // TODO this should be user configuration

                // Flavours
                // Get the list of flavours
                Map<String, IOccurrenceFlavour> flv1 = user.getEffectiveOccurrenceFlavours(User.FlavourFilter.ONLY_OCCURRENCE);
                request.setAttribute("flavourList", user.getEffectiveOccurrenceFlavourNames(flv1));

                // get the list of fields for the selected flavour
                String flavour1 = (String) thisRequest.getOption("flavour");
                if(StringUtils.isStringEmpty(flavour1) || !flv1.containsKey(flavour1))
                    thisRequest.setOption("flavour", flavour1 = "simple");

                request.setAttribute("flavourfields", flv1.get(flavour1));
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
//                System.out.println("SET " + "option-" + thisRequest.getParameterAsString("n")+ " to "+thisRequest.getParameterAsBooleanNoNull("v"));
                String optionName = thisRequest.getParameterAsString("n");
                switch(thisRequest.getParameterAsString("t", "boolean")) {
                    case "boolean":
                        thisRequest.setOption(optionName, thisRequest.getParameterAsBooleanNoNull("v"));
                        break;

                    case "radio":
/*
                        // NOTE: format is option-prefix-name don't use -
                        String prefix = "option-" + optionName.split("-")[0];
                        Enumeration<String> attributeNames = session.getAttributeNames();
                        while(attributeNames.hasMoreElements()) {
                            String opt = attributeNames.nextElement();
                            if(opt.startsWith(prefix))
                                session.setAttribute(opt, false);
                        }
*/
                        thisRequest.setOption(optionName, thisRequest.getParameterAsString("v"));
                        break;
                }
/*
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
*/
                thisRequest.success("Set");
                return;

            case "downloadoccurrencetable":
                thisRequest.response.setContentType("text/csv; charset=utf-8");
                thisRequest.response.addHeader("Content-Disposition", "attachment;Filename=\"occurrences.csv\"");
                INodeKey u;
                if(thisRequest.isOptionTrue("allusers"))
                    u = null;
                else
                    u = driver.asNodeKey(user.getID());

                Iterator<Occurrence> it1;
                if(filter == null) {
                    it1 = driver.getOccurrenceDriver().getOccurrencesOfMaintainer(u, true,null, null);
                } else {
                    Map<String, String> parsedFilter3;
                    try {
                        parsedFilter3 = driver.getOccurrenceDriver().parseFilterExpression(filter);
                        it1 = driver.getOccurrenceDriver().findOccurrencesByFilter(parsedFilter3, u, null, null);
                    } catch (FloraOnException e) {
                        thisRequest.error("O filtro não foi compreendido: " + e.getMessage());
                        return;
                    }
                }

                Common.exportOccurrencesToCSV(it1, thisRequest.response.getWriter(), driver);
                return;
        }
        request.getRequestDispatcher("/main-occurrences.jsp").forward(request, thisRequest.response);

    }
}
