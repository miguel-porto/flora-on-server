package pt.floraon.occurrences;

import org.jfree.util.Log;
import pt.floraon.authentication.entities.User;
import pt.floraon.driver.FloraOnException;
import pt.floraon.occurrences.entities.Inventory;
import pt.floraon.redlistdata.ExternalDataProvider;
import pt.floraon.server.FloraOnServlet;
import pt.floraon.taxonomy.entities.TaxEnt;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.*;

/**
 * Created by miguel on 05-02-2017.
 */
@WebServlet("/occurrences/*")
public class MainPage extends FloraOnServlet {
    @Override
    public void doFloraOnGet() throws ServletException, IOException, FloraOnException {
        ObjectInputStream oist;
        List<Inventory> invList;
        List<List<Inventory>> filesList = new ArrayList<>();
        User user = getUser();
        request.setAttribute("user", user);

        String what = getParameterAsString("w");

        if(what == null) what = "main";

        switch(what) {
            case "uploads":
                if(!user.isGuest()) {
                    refreshUser();
                    List<String> uts = new ArrayList<>();
                    uts.addAll(user.getUploadedTables());   // clone
                    for(String ut : uts) {
                        File f = new File("/tmp/" + ut);
                        if(f.canRead()) {
                            Log.info("Read " + f.getName());
                            oist = new ObjectInputStream(new FileInputStream(f));
                            try {
                                invList = (List<Inventory>) oist.readObject();
                                Log.info(invList.size());
                                filesList.add(invList);
                            } catch (ClassNotFoundException e) {
                                e.printStackTrace();
                                oist.close();
                                continue;
                            }
                            oist.close();
                        } else {    // table doesn't exist any more
                            Log.info("Removing reference to uploaded table" + ut);
                            List<String> tmp = user.getUploadedTables();
                            tmp.remove(ut);
                            driver.getNodeWorkerDriver().updateDocument(driver.asNodeKey(user.getID()), "uploadedTables", tmp);
                        }
                    }
                    request.setAttribute("filesList", filesList);
                }
                break;
        }
/*
Gson gs = new GsonBuilder().setPrettyPrinting().create();
System.out.println(gs.toJson(getUser()));
*/
/*
        ListIterator<String> path;
        try {
            path = getPathIteratorAfter("occurrences");
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
*/

//        request.setAttribute("what", what = getParameterAsString("w", "main"));
        request.getRequestDispatcher("/main-occurrences.jsp").forward(request, response);

    }
}
