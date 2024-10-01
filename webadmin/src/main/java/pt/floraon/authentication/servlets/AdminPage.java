package pt.floraon.authentication.servlets;

import edu.emory.mathcs.backport.java.util.Collections;
import pt.floraon.authentication.Privileges;
import pt.floraon.authentication.entities.TaxonPrivileges;
import pt.floraon.authentication.entities.User;
import pt.floraon.driver.FloraOnException;
import pt.floraon.driver.annotations.InventoryField;
import pt.floraon.driver.annotations.PrettyName;
import pt.floraon.driver.annotations.ReadOnly;
import pt.floraon.driver.annotations.SpecialField;
import pt.floraon.occurrences.entities.Inventory;
import pt.floraon.occurrences.entities.OBSERVED_IN;
import pt.floraon.occurrences.entities.SpecialFields;
import pt.floraon.occurrences.fields.flavours.GeneralOccurrenceFlavour;
import pt.floraon.redlistdata.entities.AtomicTaxonPrivilege;
import pt.floraon.server.FloraOnServlet;
import pt.floraon.taxonomy.entities.TaxEnt;

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

        if (thisRequest.getUser().isGuest()) {
            thisRequest.response.setStatus(HttpServletResponse.SC_FOUND);
            thisRequest.response.setHeader("Location", "./main");
            return;
        }

        thisRequest.refreshUser();
        thisRequest.request.setAttribute("what", what = thisRequest.getParameterAsString("w", "main"));

        if (thisRequest.getUser().isAdministrator()) {
            if(driver.getListDriver().getAllOrphanTaxa().hasNext())
                thisRequest.request.setAttribute("orphan", true);

            thisRequest.request.setAttribute("errors", driver.getListDriver().getTaxonomicErrors());
            thisRequest.request.setAttribute("globalSettings", driver.getGlobalSettings());

            Set<User> logins = (Set<User>) this.getServletContext().getAttribute("logins");

            if (logins == null)
                logins = new HashSet<>();
            thisRequest.request.setAttribute("logins", logins);

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

        }

        switch (what) {
            case "main":
                // Show occurrence download button?
                for (TaxonPrivileges tp : thisRequest.getUser().getTaxonPrivileges()) {
                    if (tp.getPrivileges().contains(Privileges.DOWNLOAD_OCCURRENCES)) {
                        thisRequest.request.setAttribute("showDownload", true);
                        List<User> allusers = driver.getAdministration().getAllUsers(true);
                        Collections.sort(allusers);
                        thisRequest.request.setAttribute("allusers", allusers);
                        break;
                    }
                }

                // Get inventory and occurrence fields to populate the custom views table
                Map<String, String[]> occurrenceFieldNames = new TreeMap<>();
                Map<String, String[]> inventoryFieldNames = new TreeMap<>();
                Field[] fs = OBSERVED_IN.class.getDeclaredFields();
                for (Field f : fs) {
                    if (f.isAnnotationPresent(PrettyName.class)) {
                        PrettyName fpn = f.getAnnotation(PrettyName.class);
                        boolean readOnly = f.isAnnotationPresent(ReadOnly.class);
                        occurrenceFieldNames.put(f.getName(), new String[]{fpn.value(), fpn.description(), readOnly ? "RO" : ""});
                    }
                }
                for (Field f : Inventory.class.getDeclaredFields()) {
                    if (f.isAnnotationPresent(PrettyName.class)) {
                        PrettyName fpn = f.getAnnotation(PrettyName.class);
                        boolean readOnly = f.isAnnotationPresent(ReadOnly.class);
                        inventoryFieldNames.put(f.getName(), new String[]{fpn.value(), fpn.description(), readOnly ? "RO" : ""});
                    }
                }
                for (Field f : SpecialFields.class.getDeclaredFields()) {
                    if (f.isAnnotationPresent(PrettyName.class)) {
                        if (f.isAnnotationPresent(SpecialField.class) && f.getAnnotation(SpecialField.class).hideFromCustomFlavour())
                            continue;
                        PrettyName fpn = f.getAnnotation(PrettyName.class);
                        boolean readOnly = f.isAnnotationPresent(ReadOnly.class);
                        if (f.isAnnotationPresent(InventoryField.class))
                            inventoryFieldNames.put(f.getName(), new String[]{fpn.value(), fpn.description(), readOnly ? "RO" : ""});
                        else
                            occurrenceFieldNames.put(f.getName(), new String[]{fpn.value(), fpn.description(), readOnly ? "RO" : ""});
                    }
                }

                thisRequest.request.setAttribute("occurrencefields", occurrenceFieldNames);
                thisRequest.request.setAttribute("inventoryfields", inventoryFieldNames);
//        thisRequest.request.setAttribute("specialfields", specialFieldNames);
                thisRequest.request.setAttribute("customflavours", thisRequest.getUser().getCustomOccurrenceFlavours());
                thisRequest.request.setAttribute("showKingdoms", Arrays.asList(thisRequest.getUser().getShowKingdoms()));
                thisRequest.request.setAttribute("fieldData", new GeneralOccurrenceFlavour() {  // a dummy OF for exposing field attributes only
                    @Override
                    public String[] getFields() {
                        return new String[0];
                    }

                    @Override
                    public boolean showInOccurrenceView() {
                        return false;
                    }

                    @Override
                    public boolean showInInventoryView() {
                        return false;
                    }

                    @Override
                    public String getName() {
                        return null;
                    }

                    @Override
                    public boolean containsCoordinates() {
                        return false;
                    }

                    @Override
                    public boolean containsInventoryFields() {
                        return false;
                    }
                });
                thisRequest.request.getRequestDispatcher("/main-admin.jsp").forward(thisRequest.request, thisRequest.response);
                break;

            case "users":
                thisRequest.ensureAdministrator();
                List<User> allusers1 = driver.getAdministration().getAllUsers(false);
                Collections.sort(allusers1);
                thisRequest.request.setAttribute("allusers", allusers1);
//                allusers.get(0).getUserName()
                Map<String, Integer> nrOccurrences = new HashMap<>();
                for(User u : allusers1) {
                    nrOccurrences.put(u.getID(), driver.getAdministration().getNumberOfOccurrencesOfUser(driver.asNodeKey(u.getID())));
                }
                thisRequest.request.setAttribute("nrOccurrencesMap", nrOccurrences);
                thisRequest.request.getRequestDispatcher("/user-mng.jsp").forward(thisRequest.request, thisRequest.response);
                break;

            case "edituser":
                thisRequest.ensureAdministrator();
                User tmp = driver.getAdministration().getUser(thisRequest.getParameterAsKey("user"));
                thisRequest.request.setAttribute("requesteduser", tmp);
                List<Privileges> privileges = Privileges.getAllPrivilegesOfTypeAndScope(
                        Privileges.PrivilegeType.GLOBAL, Privileges.PrivilegeScope.GLOBAL);
                privileges.addAll(Privileges.getAllPrivilegesOfTypeAndScope(
                        Privileges.PrivilegeType.CHECKLIST, Privileges.PrivilegeScope.GLOBAL));
                thisRequest.request.setAttribute("redlistprivileges", privileges);
                thisRequest.request.setAttribute("context", "global");
                thisRequest.request.getRequestDispatcher("/user-mng.jsp").forward(thisRequest.request, thisRequest.response);
                break;
        }

    }
}
