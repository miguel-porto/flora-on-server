package pt.floraon.redlistdata;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.converters.ArrayConverter;
import org.apache.commons.beanutils.converters.IntegerConverter;
import org.apache.commons.beanutils.converters.LongConverter;
import org.jfree.util.Log;
import pt.floraon.driver.Constants;
import pt.floraon.driver.FloraOnException;
import pt.floraon.driver.jobs.JobSubmitter;
import pt.floraon.redlistdata.entities.RedListDataEntity;
import pt.floraon.server.FloraOnServlet;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.Map;

import static pt.floraon.driver.Constants.fillBeanDefaults;

/**
 * Created by miguel on 01-11-2016.
 */
@MultipartConfig
@WebServlet("/redlist/api/*")
public class RedListDataApi extends FloraOnServlet {

    @Override
    public void doFloraOnGet() throws ServletException, IOException, FloraOnException {
        ListIterator<String> path = getPathIteratorAfter("api");
        String territory;
        Gson gs;

        switch(path.next()) {
            case "newdataset":
                territory = getParameterAsString("territory");
                driver.getRedListData().initializeRedListDataForTerritory(territory);
                success(JobSubmitter.newJobTask(new ComputeNativeStatusJob(), territory, driver).getID());
                break;

            case "updatenativestatus":
                territory = getParameterAsString("territory");
                success(JobSubmitter.newJobTask(new UpdateNativeStatusJob(), territory, driver).getID());
                break;

            case "downloaddata":
                gs = new GsonBuilder().setPrettyPrinting().create();
                response.setContentType("application/json; charset=utf-8");
                response.setCharacterEncoding("UTF-8");
                response.addHeader("Content-Disposition", "attachment;Filename=\"redlistdata.json\"");
                gs.toJson(driver.getRedListData().getAllRedListTaxa(getParameterAsString("territory"), false), response.getWriter());
                break;

            case "updatedata":
                RedListDataEntity rlde = new RedListDataEntity();
                gs = new GsonBuilder().setPrettyPrinting().create();
                String[] ids = request.getParameterValues("taxEntID");
                if(ids == null || ids.length == 0) {
                    Log.error("IDs not provided to update");
                    break;
                }

                HashMap<String, String[]> map = new HashMap<>();
                Enumeration names = request.getParameterNames();
                while (names.hasMoreElements()) {
                    String name = (String) names.nextElement();
                    map.put(name, request.getParameterValues(name));
                }

                IntegerConverter iconverter = new IntegerConverter(null);
                LongConverter longConverter = new LongConverter(null);
                ArrayConverter arrayConverter = new ArrayConverter(Integer[].class, iconverter);
                BeanUtilsBean beanUtilsBean = new BeanUtilsBean();
                beanUtilsBean.getConvertUtils().register(iconverter, Integer.class);
                beanUtilsBean.getConvertUtils().register(longConverter, Long.class);
                beanUtilsBean.getConvertUtils().register(arrayConverter, Integer[].class);

                try {
                    beanUtilsBean.populate(rlde, map);
//                    BeanUtils.populate(rlde, map);
                } catch (InvocationTargetException | IllegalAccessException e) {
                    e.printStackTrace();
                    error("Could not populate the java bean");
                    return;
                }

/*
                System.out.println("BEAN:");
                System.out.println(gs.toJson(rlde));
*/
                if(ids.length == 1) {
                    // TODO: must check for privileges on save!
                    // if the review status is changed from not ready to ready to publish, update date assessed.
                    RedListDataEntity old = driver.getRedListData().getRedListDataEntity(getParameterAsString("territory"), driver.asNodeKey(rlde.getTaxEntID()));

                    if (rlde.getAssessment().getReviewStatus() != RedListEnums.ReviewStatus.REVISED_PUBLISHING) {
                        rlde.updateDateAssessed();
                    } else {
                        if (old.getAssessment().getReviewStatus() != RedListEnums.ReviewStatus.REVISED_PUBLISHING
                                && rlde.getAssessment().getReviewStatus() == RedListEnums.ReviewStatus.REVISED_PUBLISHING)
                            rlde.updateDateAssessed();
                    }
                    // if it was published now, update date published
                    if (rlde.getAssessment().getPublicationStatus() == RedListEnums.PublicationStatus.PUBLISHED
                            && old.getAssessment().getPublicationStatus() != RedListEnums.PublicationStatus.PUBLISHED) {
                        rlde.updateDatePublished();
                    }
                    rlde.setRevisions(old.getRevisions());
                    rlde.addRevision(getUser().getID());
                    rlde = driver.getRedListData().updateRedListDataEntity(getParameterAsString("territory"), driver.asNodeKey(rlde.getID()), rlde, false);
//                System.out.println("NEW DOC:");
//                System.out.println(gs.toJson(rlde));
                    success("Ok");
                } else {
                    // we'll not update IDs
                    rlde.setTaxEntID(null);
                    // TODO should add a new revision to every updated document
                    Map<String, Object> cb = null;
                    RedListDataEntity old = new RedListDataEntity();
                    try {
                        old = (RedListDataEntity) fillBeanDefaults(old);
                        cb = Constants.diffBeans(old, rlde);
                    } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                        e.printStackTrace();
                    }

//                    System.out.println("Diff:");
//                    System.out.println(gs.toJson(cb));
                    driver.getRedListData().updateRedListDataEntities(getParameterAsString("territory"), ids, cb);
                    success("Ok");
                }
                break;
        }
    }

    @Override
    public void doFloraOnPost() throws ServletException, IOException, FloraOnException {
        doFloraOnGet();
    }
}
