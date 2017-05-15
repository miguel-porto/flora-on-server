package pt.floraon.redlistdata;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.converters.ArrayConverter;
import org.apache.commons.beanutils.converters.IntegerConverter;
import org.apache.commons.beanutils.converters.LongConverter;
import org.apache.commons.beanutils.converters.StringConverter;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.jfree.util.Log;
import pt.floraon.driver.utils.BeanUtils;
import pt.floraon.driver.FloraOnException;
import pt.floraon.driver.jobs.JobSubmitter;
import pt.floraon.driver.results.InferredStatus;
import pt.floraon.geometry.PolygonTheme;
import pt.floraon.redlistdata.entities.RedListDataEntity;
import pt.floraon.server.FloraOnServlet;
import pt.floraon.taxonomy.entities.TaxEnt;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Part;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static pt.floraon.driver.utils.BeanUtils.fillBeanDefaults;

/**
 * Created by miguel on 01-11-2016.
 */
@MultipartConfig
@WebServlet("/redlist/api/*")
public class RedListDataApi extends FloraOnServlet {

    @Override
    public void doFloraOnGet(ThisRequest thisRequest) throws ServletException, IOException, FloraOnException {
        ListIterator<String> path = thisRequest.getPathIteratorAfter("api");
        String territory;
        RedListDataEntity rlde;
        Gson gs;
        PrintWriter pw;

        switch(path.next()) {
            case "newdataset":
                territory = thisRequest.getParameterAsString("territory");
                driver.getRedListData().initializeRedListDataForTerritory(territory);
                thisRequest.success(JobSubmitter.newJobTask(new ComputeNativeStatusJob(territory), driver).getID());
                break;

            case "updatenativestatus":
                territory = thisRequest.getParameterAsString("territory");
                thisRequest.success(JobSubmitter.newJobTask(new UpdateNativeStatusJob(territory), driver).getID());
                break;

            case "downloadtable":
                territory = thisRequest.getParameterAsString("territory");
                String[] filter = thisRequest.getParameterAsStringArray("tags");
                // TODO clipping polygon and years must be a user configuration
                PolygonTheme clippingPolygon = new PolygonTheme(this.getClass().getResourceAsStream("PT_buffer.geojson"), null);
                thisRequest.success(JobSubmitter.newJobFileDownload(
                        new ComputeAOOEOOJob(territory, clippingPolygon, 1991, 2000
                                , filter == null ? null : new HashSet<>(Arrays.asList(filter))
                        )
                        , "AOO.csv", driver).getID());
                break;

            case "addnewtaxent":
                if(!thisRequest.getUser().canCREATE_REDLIST_DATASETS()) throw new FloraOnException("You don't have privileges for this operation");
                territory = thisRequest.getParameterAsString("territory");
                if(driver.getRedListData().getRedListDataEntity(territory, thisRequest.getParameterAsKey("id")) != null)
                    throw new FloraOnException(FieldValues.getString("Error.1"));

                TaxEnt te = driver.getNodeWorkerDriver().getTaxEntById(thisRequest.getParameterAsKey("id"));
                InferredStatus is = driver.wrapTaxEnt(driver.asNodeKey(te.getID())).getInferredNativeStatus(territory);
//                System.out.println(is.getStatusSummary());
                rlde = new RedListDataEntity(te.getID(), is);
                rlde = driver.getRedListData().createRedListDataEntity(territory, rlde);
                thisRequest.success(rlde.getID());
                break;

            case "removetaxent":
                if(!thisRequest.getUser().canCREATE_REDLIST_DATASETS()) throw new FloraOnException("You don't have privileges for this operation");
                territory = thisRequest.getParameterAsString("territory");
                driver.getRedListData().deleteRedListDataEntity(territory, thisRequest.getParameterAsKey("id"));
                thisRequest.success("Ok");
                break;

            case "downloaddata":
                gs = new GsonBuilder().setPrettyPrinting().create();
                thisRequest.response.setContentType("application/json; charset=utf-8");
                thisRequest.response.setCharacterEncoding("UTF-8");
                thisRequest.response.addHeader("Content-Disposition", "attachment;Filename=\"redlistdata.json\"");
                gs.toJson(driver.getRedListData().getAllRedListData(thisRequest.getParameterAsString("territory"), false), pw = thisRequest.response.getWriter());
                pw.flush();
                break;

            case "updatedata":
                // update one or multiple data sheets with a bean
                rlde = new RedListDataEntity();
                gs = new GsonBuilder().setPrettyPrinting().create();
                String[] ids = thisRequest.request.getParameterValues("taxEntID");
                if(ids == null || ids.length == 0) {
                    Log.error("IDs not provided to update");
                    break;
                }

                HashMap<String, String[]> map = new HashMap<>();
                Enumeration names = thisRequest.request.getParameterNames();
                while (names.hasMoreElements()) {
                    String name = (String) names.nextElement();
                    map.put(name, thisRequest.request.getParameterValues(name));
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
                } catch (InvocationTargetException | IllegalAccessException e) {
                    e.printStackTrace();
                    thisRequest.error("Could not populate the java bean");
                    return;
                }

/*
                System.out.println("BEAN:");
                System.out.println(gs.toJson(rlde));
*/
                if(ids.length == 1) {
                    // TODO: must check for privileges on save!
                    // if the review status is changed from not ready to ready to publish, update date assessed.
                    RedListDataEntity old = driver.getRedListData().getRedListDataEntity(thisRequest.getParameterAsString("territory"), driver.asNodeKey(rlde.getTaxEntID()));

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
                    rlde.addRevision(thisRequest.getUser().getID());
//                    rlde = driver.getRedListData().updateRedListDataEntity(driver.asNodeKey(rlde.getID()), rlde, false);
                    rlde = driver.getNodeWorkerDriver().updateDocument(driver.asNodeKey(rlde.getID()), rlde, false, RedListDataEntity.class);
//                System.out.println("NEW DOC:");
//                System.out.println(gs.toJson(rlde));
                    thisRequest.success("Ok");
                } else {
                    // we'll not update IDs
                    rlde.setTaxEntID(null);
                    // TODO should add a new revision to every updated document
                    Map<String, Object> cb = null;
                    RedListDataEntity old = new RedListDataEntity();
                    try {
                        old = (RedListDataEntity) fillBeanDefaults(old);
                        cb = BeanUtils.diffBeans(old, rlde);
                    } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                        e.printStackTrace();
                    }

                    System.out.println("Diff:");
                    System.out.println(gs.toJson(cb));
                    driver.getRedListData().updateRedListDataEntities(thisRequest.getParameterAsString("territory"), ids, cb);
                    thisRequest.success("Ok");
                }
                break;

            case "addtag":
                System.out.println(thisRequest.getParameterAsString("territory"));
                System.out.println(thisRequest.getParameterAsString("tag"));
                System.out.println(thisRequest.getParameterAsString("taxEntID"));
                thisRequest.success("Updated " + driver.getRedListData().addTagToRedListDataEntities(thisRequest.getParameterAsString("territory")
                        , thisRequest.request.getParameterValues("taxEntID"), thisRequest.getParameterAsString("tag")) + " taxa");
                break;
        }
    }

    @Override
    public void doFloraOnPost(ThisRequest thisRequest) throws ServletException, IOException, FloraOnException {
        ListIterator<String> path = thisRequest.getPathIteratorAfter("api");
        String territory;
        RedListDataEntity rlde;
        Gson gs;

        switch(path.next()) {
            /*
             *  update from an uploaded table:
             *  col 1: TaxEnt ID
             *  col 2: JSON string with fields to update in the RedListDataEntity
             */
            case "updatefromcsv":
                territory = thisRequest.getParameterAsString("territory");
                thisRequest.getUser().resetEffectivePrivileges();
                if(!thisRequest.getUser().isAdministrator()) throw new FloraOnException(FieldValues.getString("Error.2"));
                Part filePart;
                InputStream fileContent = null;

                try {
                    filePart = thisRequest.request.getPart("updateTable");
                    System.out.println(filePart.getSize());

                    if(filePart.getSize() == 0) throw new FloraOnException("You must select a file.");
                    fileContent = filePart.getInputStream();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if(fileContent != null) {
                    Reader freader;
                    Gson gs1 = new GsonBuilder().setPrettyPrinting().create();

                    freader = new InputStreamReader(fileContent, StandardCharsets.UTF_8);
                    CSVParser records = CSVFormat.TDF.withQuote('\"').withDelimiter('\t').withHeader().parse(freader);
                    Map<String, Integer> headers = records.getHeaderMap();
                    Type mapType = new TypeToken<Map<String, Object>>() {}.getType();

                    List<String> errors = new ArrayList<>();
                    for (CSVRecord record : records) {
                        String id = record.get(0);
                        String json = record.get(1);
                        Map<String, Object> update;
                        try {
                            update = new Gson().fromJson(json, mapType);
                        } catch (JsonSyntaxException e) {
                            errors.add(id);
                            continue;
                        }
                        int n = driver.getRedListData().updateRedListDataEntities(territory, new String[] {id}, update);
                        if(n != 1) errors.add(id);
                    }

                    fileContent.close();
                    thisRequest.success(errors.size() + " errors: " + Arrays.toString(errors.toArray(new String[errors.size()])), true);
                } else
                    thisRequest.error("Could not update.");
                break;

            default:
                doFloraOnGet(thisRequest);
        }
    }
}
