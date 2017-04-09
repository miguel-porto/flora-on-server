package pt.floraon.occurrences;

import org.jfree.util.Log;
import pt.floraon.authentication.entities.User;
import pt.floraon.driver.FloraOnException;
import pt.floraon.occurrences.entities.Inventory;
import pt.floraon.occurrences.entities.InventoryList;
import pt.floraon.server.FloraOnServlet;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

/**
 * Created by miguel on 05-02-2017.
 */
@WebServlet("/occurrences/*")
public class OccurrencesMainPage extends FloraOnServlet {
    @Override
    public void doFloraOnGet() throws ServletException, IOException, FloraOnException {
        List<InventoryList> filesList = new ArrayList<>();
        User user = getUser();
        if(user.isGuest()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
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

        String what = getParameterAsString("w");
        if(what == null) what = "main";

        switch(what) {
            case "main":
                request.setAttribute("inventories"
                        , driver.getOccurrenceDriver().getInventoriesOfObserver(driver.asNodeKey(user.getID()), null, null));

                Inventory i;
//                i.getObservers()
                break;

            case "occurrenceview":
                request.setAttribute("occurrences"
                        , driver.getOccurrenceDriver().getOccurrencesOfObserver(driver.asNodeKey(user.getID()), null, null));
                break;

            case "uploads":
                user = refreshUser();
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
                break;
        }
        request.getRequestDispatcher("/main-occurrences.jsp").forward(request, response);
    }
}
