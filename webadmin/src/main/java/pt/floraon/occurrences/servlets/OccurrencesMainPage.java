package pt.floraon.occurrences.servlets;

import jline.internal.Log;
import pt.floraon.authentication.entities.User;
import pt.floraon.driver.FloraOnException;
import pt.floraon.driver.datatypes.NumericInterval;
import pt.floraon.driver.interfaces.INodeKey;
import pt.floraon.driver.jobs.JobRunner;
import pt.floraon.driver.jobs.JobSubmitter;
import pt.floraon.driver.utils.StringUtils;
import pt.floraon.geometry.Polygon;
import pt.floraon.geometry.PolygonTheme;
import pt.floraon.occurrences.Common;
import pt.floraon.occurrences.OBSERVED_IN_summary;
import pt.floraon.occurrences.OccurrenceConstants;
import pt.floraon.occurrences.OccurrenceImporterJob;
import pt.floraon.occurrences.entities.Inventory;
import pt.floraon.occurrences.entities.InventoryList;
import pt.floraon.occurrences.entities.Occurrence;
import pt.floraon.occurrences.fields.flavours.IOccurrenceFlavour;
import pt.floraon.occurrences.dataproviders.iNaturalistImporterJob;
import pt.floraon.redlistdata.dataproviders.SimpleOccurrenceDataProvider;
import pt.floraon.server.FloraOnServlet;
import pt.floraon.taxonomy.entities.TaxEnt;
import pt.floraon.taxonomy.entities.Territory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.Serializable;
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
        int occPerPage = 250;    // how many occurrences or inventories per page
        String filter, order, baseFilter;
        Map<String, String> parsedFilter;
        INodeKey maintainer;
        boolean viewAsObserver;
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

        // fetch base filter from selection option of saved filters
        baseFilter = (String) thisRequest.getOption("baseFilter");
        if(baseFilter != null) request.setAttribute("baseFilter", baseFilter);

        // fetch filter from querystring or session
        filter = thisRequest.getParameterAsString("filter");

        if(filter == null) {
            filter = (String) session.getAttribute("filter");
        } else if(filter.equals("")) {
            session.removeAttribute("filter");
            filter = null;
        }

        parsedFilter = driver.getOccurrenceDriver().parseFilterExpression(filter);

        if(parsedFilter.size() > 0)
            session.setAttribute("filter", filter);

        Map<String, String> parsedBaseFilter = driver.getOccurrenceDriver().parseFilterExpression(baseFilter);
        parsedBaseFilter.putAll(parsedFilter);
        parsedFilter = parsedBaseFilter;

        if(parsedFilter.containsKey("lat") && parsedFilter.containsKey("long")) {
            // draw queried rectangle on map
            NumericInterval latRange = new NumericInterval(parsedFilter.get("lat"));
            NumericInterval longRange = new NumericInterval(parsedFilter.get("long"));
            if(latRange.getMinValue() != null && latRange.getMaxValue() != null
                && longRange.getMinValue() != null && longRange.getMaxValue() != null) {
                request.setAttribute("queriedRectangleMinLat", latRange.getMinValue());
                request.setAttribute("queriedRectangleMaxLat", latRange.getMaxValue());
                request.setAttribute("queriedRectangleMinLong", longRange.getMinValue());
                request.setAttribute("queriedRectangleMaxLong", longRange.getMaxValue());
            }
        }

        if(parsedFilter.containsKey("wkt")) {
            PolygonTheme pt;
            try {
                pt = new PolygonTheme(parsedFilter.get("wkt"));
                Polygon poly = pt.iterator().next().getValue();
                String coo = poly.toCoordinatesArrayLatLong();
                request.setAttribute("queriedPolygon", "[" + coo + "]");
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }

        // fetch order from querystring or session
        order = thisRequest.getParameterAsString("order");
        if(order == null) {
            order = (String) session.getAttribute("occurrenceOrder");
        } else if(order.equals("")) {
            session.removeAttribute("occurrenceOrder");
            order = null;
        }

        Map<String, String> tmpSOF = new HashMap<>();
        for(Map.Entry<String, String> ent : user.getSavedOccurrenceFilters().entrySet()) {
            tmpSOF.put(ent.getValue(), ent.getKey());
        }
        request.setAttribute("savedFilters", tmpSOF);

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
            occPerPage = Integer.parseInt(howMany);
/*
        if(session.getAttribute("option-view250") != null && (Boolean) session.getAttribute("option-view250")) count = 250;
        if(session.getAttribute("option-view1000") != null && (Boolean) session.getAttribute("option-view1000")) count = 1000;
        if(session.getAttribute("option-view5000") != null && (Boolean) session.getAttribute("option-view5000")) count = 5000;
        if(session.getAttribute("option-viewall") != null && (Boolean) session.getAttribute("option-viewall")) count = 10000000;
*/

        request.setAttribute("user", user);
        request.setAttribute("occperpage", occPerPage);
        request.setAttribute("p", page);

        // make a map of user IDs and names
        List<User> allUsers = driver.getAdministration().getAllUsers(false);
        Map<String, String> userMap = new HashMap<>();
        for(User u : allUsers)
            userMap.put(u.getID(), u.getName());
        request.setAttribute("allUsers", allUsers);
        request.setAttribute("userMap", userMap);

        Map<String, String> taxonomicRanks = new HashMap<>();
        taxonomicRanks.put("family", "Family");
        taxonomicRanks.put("genus", "Genus");
        request.setAttribute("taxonomicRanks", taxonomicRanks);

        Map<String, String> territories = new HashMap<>();
        for(Territory t : driver.getChecklistTerritories())
            if(t.getShowInChecklist()) territories.put(t.getShortName(), t.getShortName());
        request.setAttribute("checklistTerritories", territories);
        request.setAttribute("defaultTerritory", driver.getDefaultRedListTerritory());

        String what = thisRequest.getParameterAsString("w");
        if(StringUtils.isStringEmpty(what)) what = thisRequest.isOptionTrue("advancedview") ? "occurrenceview" : "main";

        switch(what) {
            case "main":    // this is the inventory summary view
                viewAsObserver = thisRequest.isOptionTrue("viewAsObserver");
                INodeKey tu1;
                if(thisRequest.isOptionTrue("allusers") && user.canMODIFY_OCCURRENCES())
                    tu1 = null;
                else
                    tu1 = driver.asNodeKey(user.getID());

                tmp = driver.getOccurrenceDriver().getInventoriesOfUserCount(tu1, viewAsObserver);

                session.setAttribute("occurrenceOrder", order);
                boolean orderDirection = false;
                if(order != null && order.endsWith("_d")) {
                    orderDirection = true;
                    order = order.substring(0, order.length() - 2);
                }

                if (parsedFilter.size() == 0) {
                    request.setAttribute("nroccurrences", Math.min(occPerPage, tmp));
                    request.setAttribute("nrtotaloccurrences", tmp);
                    request.setAttribute("inventories"
                            , driver.getOccurrenceDriver().getInventoriesOfUser(tu1, viewAsObserver, new AbstractMap.SimpleEntry<String, Boolean>(order, orderDirection), (page - 1) * occPerPage, occPerPage));
                } else {
                    request.setAttribute("nroccurrences", occPerPage);
                    request.setAttribute("nrtotaloccurrences", tmp);
                    try {
                        request.setAttribute("inventories"
                                , driver.getOccurrenceDriver().findInventoriesByFilter(parsedFilter, new AbstractMap.SimpleEntry<String, Boolean>(order, orderDirection), tu1, viewAsObserver, (page - 1) * occPerPage, occPerPage));
                    } catch(FloraOnException e) {
                        request.setAttribute("warning", "O filtro não foi compreendido: " + e.getMessage());
                    }
                }

                // Flavours
                // the flavour for new inventories
                request.setAttribute("flavourfields", OccurrenceConstants.occurrenceManagerFlavours.get(thisRequest.isOptionTrue("advancedview") ? "inventory" : "simple"));
                // the flavour for the inventory summary
//                request.setAttribute("summaryfields", OccurrenceConstants.occurrenceManagerFlavours.get("inventorySummary"));

                // the flavour for the inventory summary
                Map<String, IOccurrenceFlavour> flv2 = user.getEffectiveOccurrenceFlavours(User.FlavourFilter.ONLY_INVENTORY);
                request.setAttribute("flavourList", user.getEffectiveOccurrenceFlavourNames(flv2));
                String flavour2 = (String) thisRequest.getOption("flavour");
                if(!flv2.containsKey(flavour2))
                    thisRequest.setOption("flavour", flavour2 = thisRequest.isOptionTrue("advancedview") ? "inventory" : "simple");

                request.setAttribute("summaryfields", flv2.get(flavour2));
                break;

            case "openinventory":
                if(!StringUtils.isStringEmpty(thisRequest.getParameterAsString("id"))) {
                    request.setAttribute("inventories"
                            , driver.getOccurrenceDriver().getInventoriesByIds(new String[] {thisRequest.getParameterAsString("id")}));
                } else
                    request.setAttribute("inventories"
                            , driver.getOccurrenceDriver().getInventoriesOfMaintainer(driver.asNodeKey(user.getID()), null, null, null));

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
                if(thisRequest.isOptionTrue("allusers") && user.canMODIFY_OCCURRENCES())
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

            case "fetchOccurrenceRows": // TODO: occurrence table as AJAX
                if(thisRequest.isOptionTrue("allusers") && user.canMODIFY_OCCURRENCES())
                    maintainer = null;
                else
                    maintainer = driver.asNodeKey(user.getID());

                tmp = driver.getOccurrenceDriver().getOccurrencesOfMaintainerCount(maintainer);

                if (parsedFilter.size() == 0) {
                    request.setAttribute("occurrences"
                            , driver.getOccurrenceDriver().getOccurrencesOfMaintainer(maintainer, null,false,(page - 1) * occPerPage, occPerPage));
                } else {
                    try {
                        request.setAttribute("occurrences"
                                , driver.getOccurrenceDriver().findOccurrencesByFilter(parsedFilter, null, maintainer, false, (page - 1) * occPerPage, occPerPage));
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

                // Flavours
                // Get the list of flavours
                Map<String, IOccurrenceFlavour> flv3 = user.getEffectiveOccurrenceFlavours(User.FlavourFilter.ONLY_OCCURRENCE);

                // get the list of fields for the selected flavour
                String flavour3 = (String) thisRequest.getOption("flavour");
                if(StringUtils.isStringEmpty(flavour3) || !flv3.containsKey(flavour3))
                    thisRequest.setOption("flavour", flavour3 = "simple");

                request.setAttribute("flavourfields", flv3.get(flavour3));
                request.getRequestDispatcher("/fragments/occurrences/frag-occurrenceTableRows.jsp").forward(request, thisRequest.response);
                return;

            case "occurrenceview":  // The main view of occurrences, with many flavours
                viewAsObserver = thisRequest.isOptionTrue("viewAsObserver");
                if(thisRequest.isOptionTrue("allusers") && user.canMODIFY_OCCURRENCES())
                    maintainer = null;
                else
                    maintainer = driver.asNodeKey(user.getID());

                tmp = driver.getOccurrenceDriver().getOccurrencesOfUserCount(maintainer, viewAsObserver);

                session.setAttribute("occurrenceOrder", order);
                boolean orderDirection1 = false;
                if(order != null && order.endsWith("_d")) {
                    orderDirection1 = true;
                    order = order.substring(0, order.length() - 2);
                }
                if (parsedFilter.size() == 0) {
                    request.setAttribute("nroccurrences", Math.min(occPerPage, tmp));
                    request.setAttribute("nrtotaloccurrences", tmp);
                    request.setAttribute("occurrences"
                            , driver.getOccurrenceDriver().getOccurrencesOfUser(maintainer, viewAsObserver,
                                    new AbstractMap.SimpleEntry<String, Boolean>(order, orderDirection1),
                                        false,(page - 1) * occPerPage, occPerPage));
                } else {
                    request.setAttribute("nroccurrences", occPerPage);
                    request.setAttribute("nrtotaloccurrences", tmp);
                    try {
                        request.setAttribute("occurrences"
                                , driver.getOccurrenceDriver().findOccurrencesByFilter(parsedFilter, new AbstractMap.SimpleEntry<String, Boolean>(order, orderDirection1), maintainer, viewAsObserver, (page - 1) * occPerPage, occPerPage));
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
                            && (OccurrenceImporterJob.class.isAssignableFrom(JobSubmitter.getJob(job).getJob().getClass())
                            || iNaturalistImporterJob.class.isAssignableFrom(JobSubmitter.getJob(job).getJob().getClass())))
                        pending.add(JobSubmitter.getJob(job));
                }

                request.setAttribute("pendingFiles", pending);
//                filesList.get(0).getQuestions().get("j").getOptions().iterator().next().getID()
                break;

            case "setoption":
//                System.out.println("SET " + "option-" + thisRequest.getParameterAsString("n")+ " to "+thisRequest.getParameterAsBooleanNoNull("v"));
                String optionName = thisRequest.getParameterAsString("n");
                boolean persistent = thisRequest.getParameterAsBoolean("persistent", false);
                Serializable value;
                switch(thisRequest.getParameterAsString("t", "boolean")) {
                    case "boolean":
                        value = thisRequest.getParameterAsBooleanNoNull("v");
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
                        if("null".equals(thisRequest.getParameterAsString("v")))
                            value = null;
                        else
                            value = thisRequest.getParameterAsString("v");
                        break;

                    default:
                        value = null;
                }
                thisRequest.setOption(optionName, value);
                if(persistent) {
                    Map<String, Object> persistentOptions = user.getOptions();
                    persistentOptions.put(optionName, value);
                    driver.getAdministration().updateUser(driver.asNodeKey(user.getID()), user);
                    thisRequest.refreshUser();
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
            case "downloadspeciestable":
            case "downloadinventorytable":
                thisRequest.response.setContentType("text/csv; charset=Windows-1252");
                thisRequest.response.addHeader("Content-Disposition", "attachment;Filename=\"" + (what.equals("downloadspeciestable") ? "species-list" : "occurrences") + ".csv\"");
                thisRequest.response.setCharacterEncoding("Windows-1252");
                INodeKey u;
                if(thisRequest.isOptionTrue("allusers") && user.canMODIFY_OCCURRENCES())
                    u = null;
                else
                    u = driver.asNodeKey(user.getID());

                Iterator<? extends Inventory> it1;
                if(what.equals("downloadinventorytable")) {
                    if (parsedFilter.size() == 0) {
                        it1 = driver.getOccurrenceDriver().getInventoriesOfMaintainer(u, null, null, null);
                    } else {
                        try {
                            it1 = driver.getOccurrenceDriver().findInventoriesByFilter(parsedFilter, null, u, false, null, null);
                        } catch (FloraOnException e) {
                            thisRequest.error("O filtro não foi compreendido: " + e.getMessage());
                            return;
                        }
                    }
                } else {
                    if (parsedFilter.size() == 0) {
                        it1 = driver.getOccurrenceDriver().getOccurrencesOfMaintainer(u, null, true, null, null);
                    } else {
                        try {
                            it1 = driver.getOccurrenceDriver().findOccurrencesByFilter(parsedFilter, null, u, false, null, null);
                        } catch (FloraOnException e) {
                            thisRequest.error("O filtro não foi compreendido: " + e.getMessage());
                            return;
                        }
                    }
                }
                switch(what) {
                    case "downloadspeciestable":
                        Map<TaxEnt, OBSERVED_IN_summary> speciesList = driver.getOccurrenceDriver().getTaxonListFromOccurrences((Iterator<Occurrence>) it1, true);
                        Common.exportTaxonListToCSV(speciesList.entrySet().iterator(), thisRequest.response.getWriter(), driver);
                        break;
                    case "downloadoccurrencetable":
                        Common.exportOccurrencesToCSV((Iterator<Occurrence>) it1, thisRequest.response.getWriter(), thisRequest.isOptionTrue("advancedview") ? Common.allOutputFields : Common.simplifiedOutputFields, driver);
                        break;
                    case "downloadinventorytable":
                        Common.exportInventoriesToCSV((Iterator<Inventory>) it1, thisRequest.response.getWriter(), driver);
                        break;
                }
                return;

        }
        request.getRequestDispatcher("/main-occurrences.jsp").forward(request, thisRequest.response);

    }
}
