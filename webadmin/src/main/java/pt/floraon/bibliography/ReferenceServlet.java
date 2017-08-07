package pt.floraon.bibliography;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import pt.floraon.bibliography.entities.Reference;
import pt.floraon.driver.Constants;
import pt.floraon.driver.FloraOnException;
import pt.floraon.driver.interfaces.INodeKey;
import pt.floraon.driver.utils.BeanUtils;
import pt.floraon.server.FloraOnServlet;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.ListIterator;

@MultipartConfig
@WebServlet("/references/*")
public class ReferenceServlet extends FloraOnServlet {

    @Override
    public void doFloraOnGet(ThisRequest thisRequest) throws ServletException, IOException, FloraOnException {
        ListIterator<String> path;
        String what;
        try {
            path = thisRequest.getPathIteratorAfter("references");
            what = path.next();
        } catch (FloraOnException e) {
            what = "main";
        }
        if("".equals(what)) what = "main";

        switch(what) {
            case "main":
                Iterator<Reference> refs = driver.getListDriver().getAllDocumentsOfCollection(Constants.NodeTypes.reference.toString(), Reference.class);
                thisRequest.request.setAttribute("references", refs);
                thisRequest.request.setCharacterEncoding(StandardCharsets.UTF_8.toString());
                break;
        }
        thisRequest.request.getRequestDispatcher("references.jsp").include(thisRequest.request, thisRequest.response);
    }

    @Override
    public void doFloraOnPost(ThisRequest thisRequest) throws ServletException, IOException, FloraOnException {
        String what = thisRequest.getParameterAsString("what");
        if(what == null) return;
        switch (what) {
            case "addreference":
                Reference newRef = new Reference();

                try {
                    BeanUtils.createBeanUtilsNullSafeHTML().populate(newRef, thisRequest.request.getParameterMap());
                } catch (InvocationTargetException | IllegalAccessException e) {
                    e.printStackTrace();
                    thisRequest.error("Could not create reference. Did you format author list properly? e.g. Doe J., Almeida G.T.");
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
                INodeKey id = thisRequest.getParameterAsKey("id");
                System.out.println(id.toString());
                driver.getNodeWorkerDriver().deleteDocument(id);
                thisRequest.success("Ok");
                break;
        }
    }
}
