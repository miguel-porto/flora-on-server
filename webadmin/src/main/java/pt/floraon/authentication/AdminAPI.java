package pt.floraon.authentication;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import pt.floraon.driver.FloraOnException;
import pt.floraon.authentication.entities.User;
import pt.floraon.driver.jobs.JobRunnerTask;
import pt.floraon.driver.jobs.JobSubmitter;
import pt.floraon.geometry.PolygonTheme;
import pt.floraon.occurrences.OccurrenceImporterJob;
import pt.floraon.server.FloraOnServlet;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Part;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.ListIterator;

/**
 * Created by miguel on 26-11-2016.
 */
@MultipartConfig
@WebServlet("/admin/*")
public class AdminAPI extends FloraOnServlet {

    @Override
    public void doFloraOnPost() throws ServletException, IOException, FloraOnException {
        ListIterator<String> path = getPathIteratorAfter("admin");
        User user;
        Gson gs;
        if(!getUser().canMANAGE_REDLIST_USERS()) {
            error("You don't have privileges to do this action.");
            return;
        }

        switch (path.next()) {
            case "createuser":
                user = readUserBean();
                if(user == null) break;
                char[] pass = new char[0];
                if(user.getUserName() == null)
                    user.setUserName("user_" + new RandomString(8).nextString());
                else
                    pass = generatePassword(user);

                driver.getAdministration().createUser(user);
                if(pass.length > 0)
                    success(new String(pass), true);
                else
                    success("Ok", true);

/*                gs = new GsonBuilder().setPrettyPrinting().create();
                System.out.println("CREATE BEAN:");
                System.out.println(gs.toJson(user));
*/
                break;

            case "updateuser":
                user = readUserBean();
                success(driver.getAdministration().updateUser(driver.asNodeKey(user.getID()), user).getID());
/*
                gs = new GsonBuilder().setPrettyPrinting().create();
                System.out.println("UPDATE BEAN:");
                System.out.println(gs.toJson(user));
*/
                break;

            case "deleteuser":
                driver.getNodeWorkerDriver().deleteDocument(getParameterAsKey("databaseId"));
                success("Ok");
                break;

            case "newpassword":
                user = readUserBean();
                if(user == null) break;

                if(user.getUserName() == null || user.getUserName().trim().equals(""))
                    user.setUserName("user_" + new RandomString(8).nextString());

                if(user.getUserType() == null) user.setUserType(User.UserType.REGULAR.toString());

                char[] pass1 = generatePassword(user);
                driver.getAdministration().updateUser(driver.asNodeKey(user.getID()), user);
                success("username: " + user.getUserName() + "\npassword: " + new String(pass1), true);
                break;

            case "setuserpolygon":
                Part filePart;
                InputStream fileContent = null;
                try {
                    filePart = request.getPart("userarea");
                    if(filePart.getSize() == 0) {
                        driver.getNodeWorkerDriver().updateDocument(getParameterAsKey("databaseId")
                                , "userPolygons", "");
                        success("Ok");
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
                        error("File could not be processed. Upload in GeoJson format.");
                    else {
                        bais = new ByteArrayInputStream(bytes);
                        success(driver.getNodeWorkerDriver().updateDocument(getParameterAsKey("databaseId")
                                , "userPolygons", IOUtils.toString(bais)).toJsonObject());
                    }
                }
                break;

            case "addtaxonprivileges":
                String[] taxa = request.getParameterValues("applicableTaxa");
                String[] privileges = request.getParameterValues("taxonPrivileges");

                if(taxa == null || taxa.length == 0) throw new FloraOnException("You must select at least one taxon.");
/*
                gs = new GsonBuilder().setPrettyPrinting().create();
                System.out.println(gs.toJson(taxa));
                System.out.println(gs.toJson(privileges));
*/
                User u = driver.getAdministration().getUser(getParameterAsKey("userId"));
                u.addTaxonPrivileges(taxa, privileges);
                success(driver.getAdministration().updateUser(getParameterAsKey("userId"), u).getID());
                break;

            case "removetaxonprivileges":
                success(driver.getAdministration().removeTaxonPrivileges(getParameterAsKey("userId")
                        , getParameterAsInt("index")).getID());
                break;

            case "updatetaxonprivileges":
                String[] taxa1 = request.getParameterValues("applicableTaxa");
                if(taxa1 == null || taxa1.length == 0) throw new FloraOnException("You must select at least one taxon.");
                User u1 = driver.getAdministration().getUser(getParameterAsKey("userId"));
                int ps = getParameterAsInt("privilegeSet");

                String[] newTaxa = (String[]) ArrayUtils.addAll(
                        u1.getTaxonPrivileges().get(ps).getApplicableTaxa()
                        , taxa1);

                u1.getTaxonPrivileges().get(ps).setApplicableTaxa(newTaxa);
                success(driver.getAdministration().updateUser(getParameterAsKey("userId"), u1).getID());
                break;

            default:
                error("Command not found.");
        }
    }

    private User readUserBean() throws IOException {
        User user = new User();
        HashMap<String, String[]> map = new HashMap<>();
        Enumeration names = request.getParameterNames();
        while (names.hasMoreElements()) {
            String name = (String) names.nextElement();
/*
            System.out.println(name);
            System.out.println(request.getParameterValues(name).toString());
*/
            map.put(name, request.getParameterValues(name));
        }

        try {
            BeanUtils.populate(user, map);
        } catch (InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
            if(e.getCause() != null)
                error(e.getCause().getMessage());
            else
                error("Could not populate the java bean");
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
    public void doFloraOnGet() throws ServletException, IOException, FloraOnException {
        error("Expecting POST.");
    }
}