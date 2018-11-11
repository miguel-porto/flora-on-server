package pt.floraon.authentication.servlets;

import edu.emory.mathcs.backport.java.util.Collections;
import pt.floraon.authentication.Privileges;
import pt.floraon.authentication.entities.TaxonPrivileges;
import pt.floraon.authentication.entities.User;
import pt.floraon.driver.FloraOnException;
import pt.floraon.driver.annotations.PrettyName;
import pt.floraon.occurrences.entities.Inventory;
import pt.floraon.occurrences.entities.OBSERVED_IN;
import pt.floraon.occurrences.entities.SpecialFields;
import pt.floraon.server.FloraOnServlet;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;

/**
 * Created by miguel on 16-04-2017.
 */
@WebServlet("/adminpage/*")
public class AdminPage extends FloraOnServlet {
    @Override
    public void doFloraOnGet(ThisRequest thisRequest) throws ServletException, IOException, FloraOnException {
        String what;

        if(thisRequest.getUser().isGuest()) {
            thisRequest.response.setStatus(HttpServletResponse.SC_FOUND);
            thisRequest.response.setHeader("Location", "./main");
            return;
        }

        thisRequest.refreshUser();
        thisRequest.request.setAttribute("what", what = thisRequest.getParameterAsString("w", "main"));

        if(thisRequest.getUser().isAdministrator()) {
/*
            // fetch unmatched occurrences and try to match interactively
            InventoryList il = driver.getOccurrenceDriver().matchTaxEntNames(
                    driver.getOccurrenceDriver().getUnmatchedOccurrencesOfMaintainer(null)
                    , false, true);

            thisRequest.request.setAttribute("nomatchquestions", il.getQuestions());
            thisRequest.request.setAttribute("matchwarnings", il.getVerboseWarnings());
*/
/*
DEPRECATED
        Iterator<Inventory> umo = driver.getOccurrenceDriver().getUnmatchedOccurrences();
        Set<String> unmNames = new HashSet<>();
        int count = 0;
        while(umo.hasNext()) {
            unmNames.add(umo.next().getUnmatchedOccurrences().get(0).getVerbTaxon());
            count++;
        }
        thisRequest.request.setAttribute("unmatchedNames", unmNames);
        thisRequest.request.setAttribute("unmatchedNumber", count);
*/

//        driver.getOccurrenceDriver().getUnmatchedOccurrences().next().getUnmatchedOccurrences().get(0).getVerbTaxon()

/*
        switch (what) {

        }
*/
        }

        for(TaxonPrivileges tp : thisRequest.getUser().getTaxonPrivileges()) {
            if(tp.getPrivileges().contains(Privileges.DOWNLOAD_OCCURRENCES)) {
                thisRequest.request.setAttribute("showDownload", true);
                List<User> allusers = driver.getAdministration().getAllUsers(true);
                Collections.sort(allusers);
                thisRequest.request.setAttribute("allusers", allusers);
                break;
            }
        }

        Map<String, String> occurrenceFieldNames = new HashMap<>();
        Map<String, String> inventoryFieldNames = new HashMap<>();
        Map<String, String> specialFieldNames = new HashMap<>();
        Field[] fs = OBSERVED_IN.class.getDeclaredFields();
        for(Field f : fs) {
            if(f.isAnnotationPresent(PrettyName.class)) {
                PrettyName fpn = f.getAnnotation(PrettyName.class);
                occurrenceFieldNames.put(f.getName(), fpn.value());
            }
        }
        for(Field f : Inventory.class.getDeclaredFields()) {
            if(f.isAnnotationPresent(PrettyName.class)) {
                PrettyName fpn = f.getAnnotation(PrettyName.class);
                inventoryFieldNames.put(f.getName(), fpn.value());
            }
        }
        for(Field f : SpecialFields.class.getDeclaredFields()) {
            if(f.isAnnotationPresent(PrettyName.class)) {
                PrettyName fpn = f.getAnnotation(PrettyName.class);
                specialFieldNames.put(f.getName(), fpn.value());
            }
        }

        thisRequest.request.setAttribute("occurrencefields", occurrenceFieldNames);
        thisRequest.request.setAttribute("inventoryfields", inventoryFieldNames);
        thisRequest.request.setAttribute("specialfields", specialFieldNames);
        thisRequest.request.setAttribute("customflavours", thisRequest.getUser().getCustomOccurrenceFlavours());

        thisRequest.request.getRequestDispatcher("/main-admin.jsp").forward(thisRequest.request, thisRequest.response);
    }
}
