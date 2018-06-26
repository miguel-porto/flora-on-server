package pt.floraon.bibliography;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import pt.floraon.bibliography.entities.Reference;
import pt.floraon.driver.Constants;
import pt.floraon.driver.FloraOnException;
import pt.floraon.driver.datatypes.SafeHTMLString;
import pt.floraon.driver.interfaces.INodeKey;
import pt.floraon.driver.utils.BeanUtils;
import pt.floraon.redlistdata.entities.RedListDataEntity;
import pt.floraon.server.FloraOnServlet;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.*;

@MultipartConfig
@WebServlet("/references/*")
public class ReferenceServlet extends FloraOnServlet {

    @Override
    public void doFloraOnGet(ThisRequest thisRequest) throws ServletException, IOException, FloraOnException {
        ListIterator<String> path;
        String what = thisRequest.getParameterAsString("w");
/*
        try {
            path = thisRequest.getPathIteratorAfter("references");
            what = path.next();
        } catch (FloraOnException e) {
            what = "main";
        }
*/
        if(what == null || "".equals(what)) what = "main";

        Iterator<Reference> refs;
        switch(what) {
            case "main":
                if(thisRequest.getParameterAsString("query") == null)
                    refs = driver.getListDriver().getAllDocumentsOfCollection(Constants.NodeTypes.reference.toString(), Reference.class);
                else
                    refs = driver.getListDriver().findReferencesWithText(thisRequest.getParameterAsString("query"));
                thisRequest.request.setAttribute("references", refs);
                thisRequest.request.setCharacterEncoding(StandardCharsets.UTF_8.toString());
                break;

            case "edit":
                thisRequest.request.setAttribute("editref", driver.getNodeWorkerDriver().getDocument(
                        driver.asNodeKey(thisRequest.getParameterAsString("id")), Reference.class));
                break;
        }

        thisRequest.request.getRequestDispatcher("references.jsp").include(thisRequest.request, thisRequest.response);
    }

    @Override
    public void doFloraOnPost(ThisRequest thisRequest) throws ServletException, IOException, FloraOnException {
        INodeKey id;
        String[] ids;

        String what = thisRequest.getParameterAsString("what");
        if(what == null) return;
        switch (what) {
            case "addreference":
                Reference newRef = new Reference();
                try {
                    BeanUtils.createBeanUtilsNullSafeHTML().populate(newRef, thisRequest.request.getParameterMap());
                } catch (InvocationTargetException | IllegalAccessException e) {
                    e.printStackTrace();
                    thisRequest.error("Could not create reference. Did you format author list properly? Surnames must be followed by acronyms, e.g. Doe J., Almeida G.T.");
                    return;
                }
/*
                Gson gs = new GsonBuilder().setPrettyPrinting().create();
                System.out.println(gs.toJson(newRef));
*/

                driver.getNodeWorkerDriver().createDocument(newRef);

                thisRequest.success("Ok");
                break;

            case "deletereference":
                ids = thisRequest.getParameterAsString("ids").split(",");
                for(String id1 : ids)
                    driver.getNodeWorkerDriver().deleteDocument(driver.asNodeKey(id1));
                thisRequest.success("Ok");
                break;

            case "mergereferences":
                ids = thisRequest.getParameterAsString("ids").split(",");
                id = thisRequest.getParameterAsKey("target");
                System.out.println(Arrays.toString(ids));
                System.out.println("TAR: "+id.toString());

                BibliographyCompiler<RedListDataEntity, SafeHTMLString> bc = new BibliographyCompiler<>(
                        driver.getRedListData().getAllRedListData("lu", false, null), SafeHTMLString.class, driver);
                // TODO territory above must come from request
                Iterator<RedListDataEntity> it = bc.replaceCitations(ids, id.toString());
                RedListDataEntity tmpdoc;
                Gson gs7 = new GsonBuilder().setPrettyPrinting().create();
                while(it.hasNext()) {
                    tmpdoc = it.next();
                    if(tmpdoc._getFlag()) {
                        driver.getNodeWorkerDriver().updateDocument(driver.asNodeKey(tmpdoc.getID()), tmpdoc, false, RedListDataEntity.class);
                        System.out.println("REPLACED IN: " + tmpdoc.getTaxEnt().getFullName());
//                        System.out.println(gs7.toJson(tmpdoc));
                    }
                }
                // TODO HERE check here
                for(String eachId : ids)
                    driver.getNodeWorkerDriver().deleteDocument(driver.asNodeKey(eachId));

                thisRequest.success("Ok");
                break;

            case "updatereference":
                Reference updref = new Reference();
                Gson gs = new GsonBuilder().setPrettyPrinting().create();
                id = thisRequest.getParameterAsKey("id");

                HashMap<String, String[]> map = new HashMap<>();
                Enumeration names = thisRequest.request.getParameterNames();
                while (names.hasMoreElements()) {
                    String name = (String) names.nextElement();
                    map.put(name, thisRequest.request.getParameterValues(name));
                }

                try {
                    BeanUtils.createBeanUtilsNullSafeHTML().populate(updref, map);
                } catch (InvocationTargetException | IllegalAccessException e) {
                    e.printStackTrace();
                    thisRequest.error("Could not populate the java bean");
                    return;
                }
                System.out.println("BEAN:");
                System.out.println(gs.toJson(updref));

                driver.getNodeWorkerDriver().updateDocument(id, updref, true, Reference.class);
                thisRequest.success("Ok");
                break;
        }
    }
}
