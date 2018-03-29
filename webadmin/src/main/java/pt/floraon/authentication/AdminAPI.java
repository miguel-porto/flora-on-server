package pt.floraon.authentication;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import jline.internal.Log;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections.IteratorUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import pt.floraon.authentication.entities.TaxonPrivileges;
import pt.floraon.driver.FloraOnException;
import pt.floraon.authentication.entities.User;
import pt.floraon.driver.interfaces.INodeKey;
import pt.floraon.driver.jobs.JobRunnerTask;
import pt.floraon.driver.jobs.JobSubmitter;
import pt.floraon.geometry.PolygonTheme;
import pt.floraon.occurrences.OccurrenceImporterJob;
import pt.floraon.redlistdata.BasicOccurrenceFilter;
import pt.floraon.redlistdata.OccurrenceProcessor;
import pt.floraon.redlistdata.RedListAdminPages;
import pt.floraon.redlistdata.dataproviders.SimpleOccurrenceDataProvider;
import pt.floraon.server.FloraOnServlet;
import pt.floraon.taxonomy.entities.TaxEnt;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Part;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import static org.apache.commons.collections.IteratorUtils.toList;

/**
 * Created by miguel on 26-11-2016.
 */
@MultipartConfig
@WebServlet("/admin/*")
public class AdminAPI extends FloraOnServlet {

    @Override
    public void doFloraOnPost(ThisRequest thisRequest) throws ServletException, IOException, FloraOnException {
        ListIterator<String> path = thisRequest.getPathIteratorAfter("admin");
        User user;
        Gson gs;

        switch (path.next()) {
            case "createuser":
                thisRequest.ensurePrivilege(Privileges.MANAGE_REDLIST_USERS);

                user = readUserBean(thisRequest);
                if(user == null) break;
                char[] pass = new char[0];
                if(user.getUserName() == null)
                    user.setUserName("user_" + new RandomString(8).nextString());
                else
                    pass = generatePassword(user);

                driver.getAdministration().createUser(user);
                if(pass.length > 0)
                    thisRequest.success(new String(pass), true);
                else
                    thisRequest.success("Ok", true);

/*                gs = new GsonBuilder().setPrettyPrinting().create();
                System.out.println("CREATE BEAN:");
                System.out.println(gs.toJson(user));
*/
                break;

            case "updateuser":
                user = readUserBean(thisRequest);
                if(user == null) return;
                if(!thisRequest.getUser().getID().equals(user.getID()))
                    thisRequest.ensurePrivilege(Privileges.MANAGE_REDLIST_USERS);
                thisRequest.success(driver.getAdministration().updateUser(driver.asNodeKey(user.getID()), user).getID());
                thisRequest.refreshUser();
/*
                gs = new GsonBuilder().setPrettyPrinting().create();
                System.out.println("UPDATE BEAN:");
                System.out.println(gs.toJson(user));
*/
                break;

            case "deleteuser":
                thisRequest.ensurePrivilege(Privileges.MANAGE_REDLIST_USERS);
                driver.getNodeWorkerDriver().deleteDocument(thisRequest.getParameterAsKey("databaseId"));
                thisRequest.success("Ok");
                break;

            case "newpassword":
                thisRequest.ensurePrivilege(Privileges.MANAGE_REDLIST_USERS);
                user = readUserBean(thisRequest);
                if(user == null) break;

                if(user.getUserName() == null || user.getUserName().trim().equals(""))
                    user.setUserName("user_" + new RandomString(8).nextString());

                if(user.getUserType() == null) user.setUserType(User.UserType.REGULAR.toString());

                char[] pass1 = generatePassword(user);
                driver.getAdministration().updateUser(driver.asNodeKey(user.getID()), user);
                thisRequest.success("username: " + user.getUserName() + "\npassword: " + new String(pass1), true);
                break;

            case "setuserpolygon":
                thisRequest.ensurePrivilege(Privileges.MANAGE_REDLIST_USERS);
                Part filePart;
                InputStream fileContent = null;
                try {
                    filePart = thisRequest.request.getPart("userarea");
                    if(filePart.getSize() == 0) {
                        driver.getNodeWorkerDriver().updateDocument(thisRequest.getParameterAsKey("databaseId")
                                , "userPolygons", "");
                        thisRequest.success("Ok");
                        return;
                    }
                    System.out.println(filePart.getSize());

                    fileContent = filePart.getInputStream();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if(fileContent != null) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    IOUtils.copy(fileContent, baos);
                    byte[] bytes = baos.toByteArray();

                    ByteArrayInputStream bais = new ByteArrayInputStream(bytes);

                    PolygonTheme pt = new PolygonTheme(bais, null);
                    if(pt.size() == 0)
                        thisRequest.error("File could not be processed. Upload in GeoJson format.");
                    else {
                        bais = new ByteArrayInputStream(bytes);
                        thisRequest.success(driver.getNodeWorkerDriver().updateDocument(thisRequest.getParameterAsKey("databaseId")
                                , "userPolygons", IOUtils.toString(bais)).toJsonObject());
                    }
                }
                break;

            case "addtaxonprivileges":
                thisRequest.ensurePrivilege(Privileges.MANAGE_REDLIST_USERS);
                String[] taxa = thisRequest.request.getParameterValues("applicableTaxa");
                String[] privileges = thisRequest.request.getParameterValues("taxonPrivileges");

                if(taxa == null || taxa.length == 0) throw new FloraOnException("You must select at least one taxon.");
                if(privileges == null || privileges.length == 0) throw new FloraOnException("You must select at least one privilege.");
                if(taxa.length == 1) {
                    if(taxa[0].contains(",")) {
                        taxa = taxa[0].replaceAll("\\s", "").split(",");
                    }
                }

                Set<INodeKey> taxaKeys = new HashSet<>();
                for(String t : taxa)
                    taxaKeys.add(driver.asNodeKey(t));
/*
                gs = new GsonBuilder().setPrettyPrinting().create();
                System.out.println(gs.toJson(taxa));
                System.out.println(gs.toJson(privileges));
*/
                User u = driver.getAdministration().getUser(thisRequest.getParameterAsKey("userId"));
                u.addTaxonPrivileges(taxaKeys.toArray(new INodeKey[0]), privileges);
                thisRequest.success(driver.getAdministration().updateUser(thisRequest.getParameterAsKey("userId"), u).getID());
                break;

            case "removetaxonprivileges":
                thisRequest.ensurePrivilege(Privileges.MANAGE_REDLIST_USERS);
                thisRequest.success(driver.getAdministration().removeTaxonPrivileges(thisRequest.getParameterAsKey("userId")
                        , thisRequest.getParameterAsInt("index")).getID());
                break;

            case "removetaxonfromset":
                thisRequest.ensurePrivilege(Privileges.MANAGE_REDLIST_USERS);
                thisRequest.success(driver.getAdministration().removeTaxonFromPrivilegeSet(thisRequest.getParameterAsKey("userId")
                        , thisRequest.getParameterAsKey("taxEntId"), thisRequest.getParameterAsInt("index")).getID());
                break;

            case "updatetaxonprivileges":
                thisRequest.ensurePrivilege(Privileges.MANAGE_REDLIST_USERS);
                String[] taxa1 = thisRequest.request.getParameterValues("applicableTaxa");
                if(taxa1 == null || taxa1.length == 0) throw new FloraOnException("You must select at least one taxon.");
                User u1 = driver.getAdministration().getUser(thisRequest.getParameterAsKey("userId"));
                int ps = thisRequest.getParameterAsInt("privilegeSet");

                String[] newTaxa = (String[]) ArrayUtils.addAll(
                        u1.getTaxonPrivileges().get(ps).getApplicableTaxa()
                        , taxa1);

                u1.getTaxonPrivileges().get(ps).setApplicableTaxa(newTaxa);
                thisRequest.success(driver.getAdministration().updateUser(thisRequest.getParameterAsKey("userId"), u1).getID());
                break;

            default:
                thisRequest.error("Command not found.");
        }
    }

    private User readUserBean(ThisRequest thisRequest) throws IOException {
        User user = new User();
        HashMap<String, String[]> map = new HashMap<>();
        Enumeration names = thisRequest.request.getParameterNames();
        while (names.hasMoreElements()) {
            String name = (String) names.nextElement();
/*
            System.out.println(name);
            System.out.println(request.getParameterValues(name).toString());
*/
            map.put(name, thisRequest.request.getParameterValues(name));
        }

        try {
            BeanUtils.populate(user, map);
        } catch (InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
            if(e.getCause() != null)
                thisRequest.error(e.getCause().getMessage());
            else
                thisRequest.error("Could not populate the java bean");
            return null;
        }
        return user;
    }

    private char[] generatePassword(User u) {
        char[] pass;
        pass = new RandomString(12).nextString().toCharArray();
        u.setPassword(pass);
        return pass;
    }

    @Override
    public void doFloraOnGet(ThisRequest thisRequest) throws ServletException, IOException, FloraOnException {
        ListIterator<String> path = thisRequest.getPathIteratorAfter("admin");
        User user;
        Gson gs;

        switch (path.next()) {
            case "downloadallkml":
                PolygonTheme clip = thisRequest.getUser()._getUserPolygonsAsTheme();

                Set<String> taxaToDl = new HashSet<>();
                for(TaxonPrivileges tp : thisRequest.getUser().getTaxonPrivileges()) {
                    if(tp.getPrivileges().contains(Privileges.DOWNLOAD_OCCURRENCES))
                        taxaToDl.addAll(new HashSet<>(Arrays.asList(tp.getApplicableTaxa())));
                }
                if(taxaToDl.size() == 0)
                    throw new FloraOnException("Nothing to download.");

                List<SimpleOccurrenceDataProvider> sodps1 = driver.getRedListData().getSimpleOccurrenceDataProviders();
                Log.info("Starting query");
                for(SimpleOccurrenceDataProvider edp : sodps1)
                    edp.executeOccurrenceQuery(driver.getNodeWorkerDriver().getTaxEntByIds(taxaToDl.toArray(new String[taxaToDl.size()])));
                Log.info("End query");

                thisRequest.response.setContentType("application/vnd.google-earth.kml+xml; charset=utf-8");
                thisRequest.response.addHeader("Content-Disposition", "attachment;Filename=\"all-occurrences.kml\"");
                PrintWriter wr = thisRequest.response.getWriter();

                OccurrenceProcessor.iterableOf(sodps1, new BasicOccurrenceFilter(
                        null, null, true, (clip == null || clip.size() == 0) ? null : clip)).exportKML(wr);
                wr.flush();

                break;

            default:
                thisRequest.error("Command not found.");
        }
    }
}