package pt.floraon.redlistdata;

import com.arangodb.ArangoDatabase;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.beanutils.BeanUtils;
import pt.floraon.driver.FloraOnException;
import pt.floraon.entities.TaxEnt;
import pt.floraon.jobs.JobSubmitter;
import pt.floraon.redlistdata.entities.RedListDataEntity;
import pt.floraon.results.ListOfTerritoryStatus;
import pt.floraon.server.FloraOnServlet;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;

/**
 * Created by miguel on 01-11-2016.
 */
@MultipartConfig
@WebServlet("/redlist/api/*")
public class RedListDataApi extends FloraOnServlet {

    @Override
    public void doFloraOnGet() throws ServletException, IOException, FloraOnException {
        ListIterator<String> path = getPathIteratorAfter("api");
        switch(path.next()) {
            case "newdataset":
                String territory = getParameterAsString("territory");
                driver.getRedListData().initializeRedListDataForTerritory(territory);
                success(JobSubmitter.newJobTask(new ComputeNativeStatusJob(), territory, driver).getID());
/*
                List<TaxEnt> taxEntList = driver.getListDriver().getAllSpeciesOrInferiorTaxEnt(true, true, "lu", null, null);

                for(TaxEnt te1 : taxEntList) {
                    ListOfTerritoryStatus.InferredStatus is = driver.wrapTaxEnt(driver.asNodeKey(te1.getID())).getInferredNativeStatus("lu");
                    System.out.println(te1.getName() +": "+ is.getNativeStatus());
                }
*/

                break;

            case "updatedata":
                RedListDataEntity rlde = new RedListDataEntity();
                HashMap<String, String[]> map = new HashMap<>();
                Enumeration names = request.getParameterNames();
                while (names.hasMoreElements()) {
                    String name = (String) names.nextElement();
                    map.put(name, request.getParameterValues(name));
                }
                try {
                    BeanUtils.populate(rlde, map);
                } catch (InvocationTargetException | IllegalAccessException e) {
                    e.printStackTrace();
                    error("Could not populate the java bean");
                    return;
                }
                Gson gs = new GsonBuilder().setPrettyPrinting().create();
//                System.out.println("BEAN:");
//                System.out.println(gs.toJson(rlde));
                rlde = driver.getRedListData().updateRedListDataEntity(getParameterAsString("territory"), driver.asNodeKey(rlde.getID()), rlde, false);
//                System.out.println("NEW DOC:");
//                System.out.println(gs.toJson(rlde));
                success("Ok");
                break;
        }
    }

    @Override
    public void doFloraOnPost() throws ServletException, IOException, FloraOnException {
        doFloraOnGet();
    }
}
