package pt.floraon.authentication;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.apache.commons.beanutils.BeanUtils;
import pt.floraon.driver.FloraOnException;
import pt.floraon.authentication.entities.User;
import pt.floraon.server.FloraOnServlet;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
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
                char[] pass = new char[0];
                user = readUserBean();
                if(user == null) break;
                if(user.getUserName() == null) {
                    user.setUserName("user_" + new RandomString(8).nextString());
                } else {    // we only set password if a username has been provided
                    pass = new RandomString(12).nextString().toCharArray();
                    user.setPassword(pass);
                }
                driver.getAdministration().createUser(user);
                JsonObject jo = new JsonObject();
                if(pass.length > 0) {
                    jo.addProperty("text", new String(pass));
                    jo.addProperty("alert", true);
                } else jo.addProperty("text", "Ok");
                success(jo);
                gs = new GsonBuilder().setPrettyPrinting().create();
/*
                System.out.println("CREATE BEAN:");
                System.out.println(gs.toJson(user));
*/
                break;

            case "updateuser":
                user = readUserBean();
                success(driver.getAdministration().updateUser(driver.asNodeKey(user.getID()), user).getID());
                gs = new GsonBuilder().setPrettyPrinting().create();
                System.out.println("UPDATE BEAN:");
                System.out.println(gs.toJson(user));
                break;

            case "deleteuser":
                driver.getNodeWorkerDriver().deleteDocument(getParameterAsKey("databaseId"));
                success("Ok");
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
            System.out.println(name);
            System.out.println(request.getParameterValues(name).toString());
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

    @Override
    public void doFloraOnGet() throws ServletException, IOException, FloraOnException {
        error("Expecting POST.");
    }
}