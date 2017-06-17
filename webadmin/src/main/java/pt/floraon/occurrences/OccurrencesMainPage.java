package pt.floraon.occurrences;

import org.jfree.util.Log;
import pt.floraon.authentication.entities.User;
import pt.floraon.driver.FloraOnException;
import pt.floraon.occurrences.entities.Inventory;
import pt.floraon.occurrences.entities.InventoryList;
import pt.floraon.server.FloraOnServlet;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
        Integer offset = thisRequest.getParameterAsInteger("o", null);
        Integer count = thisRequest.getParameterAsInteger("c", 200000);

        if(user.isGuest()) {
            thisRequest.response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        request.setAttribute("user", user);

        // make a map of user IDs and names
        List<User> allUsers = driver.getAdministration().getAllUsers();
        Map<String, String> userMap = new HashMap<>();
        for(User u : allUsers)
            userMap.put(u.getID(), u.getName());

        request.setAttribute("allUsers", allUsers);
        request.setAttribute("userMap", userMap);

        String what = thisRequest.getParameterAsString("w");
        if(what == null) what = "main";

        switch(what) {
            case "main":
                request.setAttribute("nroccurrences"
                        , driver.getOccurrenceDriver().getInventoriesOfMaintainerCount(driver.asNodeKey(user.getID())));
                request.setAttribute("inventories"
                        , driver.getOccurrenceDriver().getInventoriesOfMaintainer(driver.asNodeKey(user.getID()), offset, count));
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
                if(thisRequest.getParameterAsString("id") != null) {
                    request.setAttribute("inventories"
                            , driver.getOccurrenceDriver().getInventoriesByIds(new String[] {thisRequest.getParameterAsString("id")}));
                } else
                    request.setAttribute("inventories"
                            , driver.getOccurrenceDriver().getInventoriesOfMaintainer(driver.asNodeKey(user.getID()), null, null));

                Inventory i;
//                i._getIDURLEncoded()
                break;

            case "occurrenceview":
                request.setAttribute("nroccurrences"
                        , driver.getOccurrenceDriver().getOccurrencesOfMaintainerCount(driver.asNodeKey(user.getID())));
                request.setAttribute("occurrences"
                        , driver.getOccurrenceDriver().getOccurrencesOfMaintainer(driver.asNodeKey(user.getID()), offset, count));
                request.setAttribute("nproblems"
                        , driver.getOccurrenceDriver().getUnmatchedOccurrencesOfMaintainerCount(driver.asNodeKey(user.getID())));
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
                        List<String> tmp = user.getUploadedTables();
                        tmp.remove(ut);
                        driver.getNodeWorkerDriver().updateDocument(driver.asNodeKey(user.getID()), "uploadedTables", tmp);
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                request.setAttribute("filesList", filesList);
//                filesList.get(0).getQuestions().get("j").getOptions().iterator().next().getID()
                break;
        }
        request.getRequestDispatcher("/main-occurrences.jsp").forward(request, thisRequest.response);
    }
}
