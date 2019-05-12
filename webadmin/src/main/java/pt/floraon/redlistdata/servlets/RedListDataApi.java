package pt.floraon.redlistdata.servlets;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.openhtmltopdf.svgsupport.BatikSVGDrawer;
import com.openhtmltopdf.swing.NaiveUserAgent;
import jline.internal.Log;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.mutable.MutableInt;
import pt.floraon.driver.Constants;
import pt.floraon.driver.interfaces.INodeKey;
import pt.floraon.driver.interfaces.IOccurrenceReportDriver;
import pt.floraon.driver.utils.BeanUtils;
import pt.floraon.driver.FloraOnException;
import pt.floraon.driver.jobs.JobSubmitter;
import pt.floraon.driver.results.InferredStatus;
import pt.floraon.driver.utils.StringUtils;
import pt.floraon.geometry.Point2D;
import pt.floraon.geometry.Polygon;
import pt.floraon.geometry.PolygonTheme;
import pt.floraon.driver.datatypes.Rectangle;
import pt.floraon.occurrences.Common;
import pt.floraon.occurrences.StatisticPerTaxon;
import pt.floraon.occurrences.arangodb.OccurrenceReportArangoDriver;
import pt.floraon.occurrences.entities.Inventory;
import pt.floraon.occurrences.fields.parsers.DateParser;
import pt.floraon.occurrences.entities.Occurrence;
import pt.floraon.redlistdata.*;
import pt.floraon.redlistdata.jobs.*;
import pt.floraon.redlistdata.dataproviders.SimpleOccurrenceDataProvider;
import pt.floraon.redlistdata.entities.RedListDataEntity;
import pt.floraon.redlistdata.entities.RedListDataEntitySnapshot;
import pt.floraon.redlistdata.entities.RedListSettings;
import pt.floraon.redlistdata.occurrences.BasicOccurrenceFilter;
import pt.floraon.redlistdata.occurrences.OccurrenceProcessor;
import pt.floraon.server.FloraOnServlet;
import pt.floraon.taxonomy.entities.TaxEnt;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Part;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.util.*;

import static pt.floraon.driver.utils.BeanUtils.fillBeanDefaults;

/**
 * Execute some tasks pertaining to the red list sheets
 * Created by miguel on 01-11-2016.
 * TODO: move to the territory context in path!
 */
@MultipartConfig
@WebServlet("/redlist/api/*")
public class RedListDataApi extends FloraOnServlet {

    private void saveSnapshot(String territory, INodeKey id, String versionTag, String savedByUser) throws FloraOnException, IOException {
        RedListDataEntitySnapshot rldes;
        // read sheet data
        rldes = driver.getRedListData().getRedListDataEntityAsSnapshot(territory, id);

        // query occurrences
        PolygonTheme clippingPolygon2 = new PolygonTheme(this.getClass().getResourceAsStream("PT_buffer.geojson"), null);
        List<SimpleOccurrenceDataProvider> sodps = driver.getRedListData().getSimpleOccurrenceDataProviders();

        for(SimpleOccurrenceDataProvider edp : sodps)
            edp.executeOccurrenceQuery(rldes.getTaxEnt());

        OccurrenceProcessor op = OccurrenceProcessor.iterableOf(sodps, new BasicOccurrenceFilter(clippingPolygon2));

        // populate sheet data with a copy of the occurrences
        Iterator<Occurrence> it6 = op.iterator();
        List<Occurrence> ol = new ArrayList<>();
        while(it6.hasNext())
            ol.add(it6.next());
        rldes.setOccurrences(ol);

        rldes.setVersionTag(versionTag);
        rldes.setSavedByUser(savedByUser);
        driver.getRedListData().saveRedListDataEntitySnapshot(territory, rldes);
    }

    @Override
    public void doFloraOnGet(ThisRequest thisRequest) throws ServletException, IOException, FloraOnException {
        ListIterator<String> path = thisRequest.getPathIteratorAfter("api");
        String territory;
        RedListDataEntity rlde;
        Gson gs;
        PrintWriter pw;
        String what;
        RedListSettings rls;

        switch(what = path.next()) {
            case "newdataset":
                if(thisRequest.getUser().isGuest()) {
                    thisRequest.error("You're not logged in.");
                    break;
                }
                territory = thisRequest.getParameterAsString("territory");
                driver.getRedListData().initializeRedListDataForTerritory(territory);
                thisRequest.success(JobSubmitter.newJobTask(new CreateRedListSheetsJob(territory), driver).getID());
                break;

            case "snapshot":    // archives a new snapshot of one sheet, including a copy of the occurrence records
                saveSnapshot(thisRequest.getParameterAsString("territory"), thisRequest.getParameterAsKey("id")
                    , thisRequest.getParameterAsString("versiontag"), thisRequest.getUser().getID());

                thisRequest.success("Ok");
//                gs = new GsonBuilder().setPrettyPrinting().create();
//                System.out.println(gs.toJson(rldes));
                break;

            case "deletesnapshot":
                driver.getNodeWorkerDriver().deleteDocument(thisRequest.getParameterAsKey("id"));
                thisRequest.success("Ok");
                break;

            case "updatenativestatus":
                if(thisRequest.getUser().isGuest()) {
                    thisRequest.error("You're not logged in.");
                    break;
                }
                territory = thisRequest.getParameterAsString("territory");
                thisRequest.success(JobSubmitter.newJobTask(new UpdateNativeStatusJob(territory), driver).getID());
                break;

            case "downloadalloccurrences":
                territory = thisRequest.getParameterAsString("territory");
                String[] filter1 = thisRequest.getParameterAsStringArray("tags");
                // TODO clipping polygon and years must be a user configuration
                PolygonTheme clippingPolygon1 = new PolygonTheme(this.getClass().getResourceAsStream("PT_buffer.geojson"), null);
                rls = driver.getRedListSettings(territory);
                thisRequest.success(JobSubmitter.newJobFileDownload(
                        new DownloadOccurrencesJob(territory, clippingPolygon1, (rls.getHistoricalThreshold() + 1)
                                , filter1 == null ? null : new HashSet<>(Arrays.asList(filter1)))
                        , "all-occurrences.csv", driver).getID());
                break;

            case "downloadtaxainpolygon":
                territory = thisRequest.getParameterAsString("territory");
                String polygonWKT = thisRequest.getParameterAsString("polygon");
                // TODO clipping polygon must be a user configuration
                PolygonTheme clippingPolygon2 = new PolygonTheme(this.getClass().getResourceAsStream("PT_buffer.geojson"), null);
                rls = driver.getRedListSettings(territory);

                Map<INodeKey, Object> taxaSet = new HashMap<>();
                Set<RedListDataEntity> rldeSet = new HashSet<>();

                Iterator<Occurrence> itOcc = driver.getQueryDriver().findOccurrencesContainedIn(polygonWKT, null
                        // NOTE: this filter is to select which taxa to consider, only!
                        , new BasicOccurrenceFilter(rls.getHistoricalThreshold() + 1, null, false, null));

                TaxEnt tmp;
                while(itOcc.hasNext()) {
                    Occurrence occ = itOcc.next();
                    // this adds the taxa and records the most recent year of observation of this taxon, in the polygon
                    if((tmp = occ.getOccurrence().getTaxEnt()) != null) {
                        INodeKey tmpKey = driver.asNodeKey(tmp.getID());
                        if(taxaSet.containsKey(tmpKey)) {
                            if(occ.getYear() != null) {
                                Integer tmp1 = ((MutableInt) taxaSet.get(tmpKey)).toInteger();
                                if (tmp1 == null)
                                    ((MutableInt) taxaSet.get(tmpKey)).setValue(occ.getYear());
                                else {
                                    if(occ.getYear() > ((MutableInt) taxaSet.get(tmpKey)).intValue())
                                        ((MutableInt) taxaSet.get(tmpKey)).setValue(occ.getYear());
                                }
                            }
                        } else
                            taxaSet.put(tmpKey, new MutableInt(occ.getYear()));
                    }
                }

/*
                Iterator<Inventory> itInv = driver.getQueryDriver().findInventoriesContainedIn(polygonWKT, null);
                // iterate all inventories falling in polygon and fetch the existing species
                while(itInv.hasNext()) {
                    Inventory inv = itInv.next();
                    for(OBSERVED_IN o : inv._getTaxa()) {
                        if(o.getTaxEnt() != null)
                            taxaSet.add(driver.asNodeKey(o.getTaxEnt().getID()));
                    }
                }
*/

                // make an iterator of RedListData pertaining to those species. We need this because we need red list info
                // about the taxon.
/*
                for(INodeKey nk : taxaSet) {
                    RedListDataEntity rlde3 = driver.getRedListData().getRedListDataEntity(territory, nk);
//                    driver.wrapTaxEnt(nk).isInfrataxonOf()
                    if(rlde3 != null)
                        rldeSet.add(rlde3);
                }
*/

                thisRequest.success(JobSubmitter.newJobFileDownload(
                        new ComputeAOOEOOJobWithInfo(territory, 2000
                            // NOTE this filter is for computing AOO & EOO only!
                                , new BasicOccurrenceFilter(rls.getHistoricalThreshold() + 1, null, false, clippingPolygon2)
                                , null, taxaSet)
                        , "taxa-in-polygon.csv", driver).getID());
                break;

            case "downloadtable":
                territory = thisRequest.getParameterAsString("territory");
                String[] filter = thisRequest.getParameterAsStringArray("tags");
                // TODO clipping polygon and years must be a user configuration
                PolygonTheme clippingPolygon = new PolygonTheme(this.getClass().getResourceAsStream("PT_buffer.geojson"), null);
                rls = driver.getRedListSettings(territory);
                thisRequest.success(JobSubmitter.newJobFileDownload(
/*
                        new ComputeAOOEOOJob(territory, 2000
                                , new BasicOccurrenceFilter(rls.getHistoricalThreshold() + 1, null, false, clippingPolygon)
                                , new BasicRedListDataFilter(filter == null ? null : new HashSet<>(Arrays.asList(filter)))
                        )
*/
                        new ComputeAOOEOOJob(territory, null
                                , null, new BasicRedListDataFilter(filter == null ? null : new HashSet<>(Arrays.asList(filter)))
                                , false
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

            case "migratetotaxon":
                if(!thisRequest.getUser().canCREATE_REDLIST_DATASETS()) throw new FloraOnException("You don't have privileges for this operation");
                territory = thisRequest.getParameterAsString("territory");
                if(StringUtils.isStringEmpty(thisRequest.getParameterAsString("id")) || thisRequest.getParameterAsKey("source") == null)
                    throw new FloraOnException("No ID specified");

                if(driver.getRedListData().getRedListDataEntity(territory, thisRequest.getParameterAsKey("id")) != null)
                    throw new FloraOnException(FieldValues.getString("Error.1"));

                Log.info(thisRequest.getParameterAsKey("source"));
                Log.info(thisRequest.getParameterAsString("id"));
                driver.getNodeWorkerDriver().updateDocument(thisRequest.getParameterAsKey("source")
                        , "taxEntID", thisRequest.getParameterAsString("id"));
                thisRequest.success("Ok");
                break;

            case "removetaxent":
                if(!thisRequest.getUser().canCREATE_REDLIST_DATASETS()) throw new FloraOnException("You don't have privileges for this operation");
                territory = thisRequest.getParameterAsString("territory");
                driver.getRedListData().deleteRedListDataEntity(territory, thisRequest.getParameterAsKey("id"));
                thisRequest.success("Ok");
                break;

            case "downloaddata":
                if(thisRequest.getUser().isGuest()) {
                    thisRequest.error("You're not logged in.");
                    break;
                }

                gs = new GsonBuilder().setPrettyPrinting().create();
                thisRequest.response.setContentType("application/json; charset=utf-8");
                thisRequest.response.setCharacterEncoding("UTF-8");
                thisRequest.response.addHeader("Content-Disposition", "attachment;Filename=\"redlistdata.json\"");
                Iterator<RedListDataEntity> it = driver.getRedListData().getAllRedListData(thisRequest.getParameterAsString("territory"), false, null);
                pw = thisRequest.response.getWriter();
                pw.print("{\"\":[");
                boolean first = true;
                while(it.hasNext()) {
                    rlde = it.next();
                    if(rlde.getRevisions().size() > 0) {
                        if(!first) pw.print("],[");
                        gs.toJson(rlde, pw);
                        first = false;
                    }
                }
                pw.print("]}");
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
                    System.out.println(name +": "+thisRequest.request.getParameterValues(name)[0]);
                    map.put(name, thisRequest.request.getParameterValues(name));
                }

                try {
                    BeanUtils.createBeanUtilsNullSafeHTML().populate(rlde, map);
                } catch (InvocationTargetException | IllegalAccessException e) {
                    e.printStackTrace();
                    thisRequest.error("Could not populate the java bean");
                    return;
                }

                System.out.println(">> Saving sheet on " + Constants.dateTimeFormat.get().format(new Date()));
                System.out.println(gs.toJson(rlde));

                if(thisRequest.getUser().isGuest()) {
                    System.out.println(">> NOTHING WAS SAVED!");
                    thisRequest.error("Nothing was saved, your session has expired. You must login in another window (without closing this one), then come back to this one and save again");
                    break;
                }

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

            case "download-statistics":
//                System.out.println(System.getProperty("java.io.tmpdir"));
                String filename = thisRequest.getParameterAsString("file");
                StringBuilder sb = new StringBuilder();
                String cmd = String.format("print('%s');t <- read.csv('%s');print(t)", System.getProperty("java.io.tmpdir") + "/" + filename, System.getProperty("java.io.tmpdir") + "/" + filename);
                Process pr = Runtime.getRuntime().exec("Rscript -e {" + cmd + "}");

                IOUtils.copy(pr.getInputStream(), pw = thisRequest.response.getWriter());
                pr.destroy();


/*

                FileInputStream fis = new FileInputStream(System.getProperty("java.io.tmpdir") + "/" + filename);
                InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
                //response.setContentType("text/csv; charset=Windows-1252");
                thisRequest.response.setContentType("text/csv; charset=utf-8");
                thisRequest.response.addHeader("Content-Disposition", "attachment;Filename=\"stats.csv\"");
                thisRequest.response.setCharacterEncoding(StandardCharsets.UTF_8.toString());
                //IOUtils.copy(jobInput, response.getOutputStream());
                IOUtils.copy(isr, pw = thisRequest.response.getWriter());
                fis.close();
*/
                pw.flush();
                break;

            case "report-ninv":
            case "report-ntaxatag":
            case "report-listtaxatag":
            case "report-listtaxatagestimates":
            case "report-listtaxatagphoto":
            case "report-listtaxatagspecimen":
            case "report-listtaxatagnrrecords":
            case "report-listutmsquares":
            case "report-listprotectedareas":
            case "report-alltaxa":
            case "report-sheetauthor":
            case "report-sheetassessor":
            case "report-sheetreviewer":
            case "report-listspecimens":
                if(thisRequest.getUser().isGuest()) {thisRequest.forbidden(null); return;}
                IOccurrenceReportDriver ord = driver.getOccurrenceReportDriver();
                Date from = null, to = null;
                OccurrenceReportArangoDriver.TypeOfCollaboration toc = null;
                thisRequest.response.setContentType("text/plain");
                thisRequest.response.setCharacterEncoding("UTF-8");
                thisRequest.response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate, post-check=0, pre-check=0"); // HTTP 1.1
                thisRequest.response.setHeader("Pragma", "no-cache");
                thisRequest.response.setHeader("Expires", "0");
                pw = thisRequest.response.getWriter();

                territory = thisRequest.getParameterAsString("territory");

                try {
                    from = DateParser.parseDateAsDate(thisRequest.getParameterAsString("fromdate"));
                    to = DateParser.parseDateAsDate(thisRequest.getParameterAsString("todate"));
                } catch(IllegalArgumentException e) {
                    pw.println(e.getMessage());
                    break;
                }
                if(from != null && to != null) {
                    DateFormat df = Constants.dateFormat.get();
                    thisRequest.request.setAttribute("fromDate", df.format(from));
                    thisRequest.request.setAttribute("toDate", df.format(to));
                    switch(what) {
                        case "report-ninv":
                            pw.print("<p>" + ord.getNumberOfInventories(
                                    driver.asNodeKey(thisRequest.getUser().getID()), from, to) + "</p>");
                            break;

                        case "report-listtaxatag":
                        case "report-listtaxatagphoto":
                            int count = 0;
                            pw.print("<ul>");
                            Iterator<TaxEnt> it1 = ord.getTaxaWithTag(
                                    driver.asNodeKey(thisRequest.getUser().getID()), from, to, territory
                                    , thisRequest.getParameterAsString("tag")
                                    , what.equals("report-listtaxatagphoto"));
                            while(it1.hasNext()) {
                                    count++;
                                    TaxEnt te1 = it1.next();
                                    pw.print("<li>" + te1.getFullName(true) + "</li>");
                            }
                            pw.print("</ul><p>Nº de taxa: " + count + "</p>");
                            break;

                        case "report-listtaxatagspecimen":
                            pw.print("<table class=\"subtable\"><thead><tr><th>Taxon</th><th>Nº de espécimes colhidos</th></tr></thead>");
                            Iterator<StatisticPerTaxon> it2 = ord.getTaxaWithTagCollected(
                                    driver.asNodeKey(thisRequest.getUser().getID()), from, to, territory
                                    , thisRequest.getParameterAsString("tag"));
                            while(it2.hasNext()) {
                                StatisticPerTaxon te1 = it2.next();
                                pw.print("<tr><td>" + te1.getName() + "</td><td>" + te1.getValue() + "</td></tr>");
                            }
                            pw.print("</table>");
                            break;

                        case "report-listtaxatagestimates":
                            pw.print("<table class=\"subtable\"><thead><tr><th>Taxon</th><th>Nº de núcleos com estimativa</th></tr></thead>");
                            Iterator<StatisticPerTaxon> it3 = ord.getTaxaWithTagEstimates(
                                    driver.asNodeKey(thisRequest.getUser().getID()), from, to, territory
                                    , thisRequest.getParameterAsString("tag"));
                            while(it3.hasNext()) {
                                StatisticPerTaxon te1 = it3.next();
                                pw.print("<tr><td>" + te1.getName() + "</td><td>" + te1.getValue() + "</td></tr>");
                            }
                            pw.print("</table>");
                            break;

                        case "report-listtaxatagnrrecords":
                            pw.print("<table class=\"subtable\"><thead><tr><th>Taxon</th><th>Nº de registos</th></tr></thead>");
                            Iterator<StatisticPerTaxon> it4 = ord.getTaxaWithTagNrRecords(
                                    driver.asNodeKey(thisRequest.getUser().getID()), from, to, territory
                                    , thisRequest.getParameterAsString("tag"));
                            while(it4.hasNext()) {
                                StatisticPerTaxon te1 = it4.next();
                                pw.print("<tr><td>" + te1.getName() + "</td><td>" + te1.getValue() + "</td></tr>");
                            }
                            pw.print("</table>");
                            break;

                        case "report-listutmsquares":
                            Map<String, Integer> lutm = ord.getListOfUTMSquaresWithOccurrences(
                                    driver.asNodeKey(thisRequest.getUser().getID()), from, to, 2000);
                            pw.print("<p>Nº de quadrículas: " + lutm.size() + "</p><table class=\"subtable\"><thead><tr><th>Quadrícula 2x2 km</th><th>Nº de registos</th></tr></thead>");
                            for(Map.Entry<String, Integer> e : lutm.entrySet()) {
                                pw.printf("<tr><td>%s</td><td>%d</td></tr>", e.getKey(), e.getValue());
                            }
                            pw.print("</table>");
                            break;

                        case "report-listprotectedareas":
                            // TODO this should be a user configuration loaded at startup
                            PolygonTheme protectedAreas = new PolygonTheme(this.getClass().getResourceAsStream("SNAC.geojson"), "SITE_NAME");
                            Map<String, Integer> lpa = ord.getListOfPolygonsWithOccurrences(driver.getOccurrenceDriver().getOccurrencesOfObserverWithinDates(
                                    driver.asNodeKey(thisRequest.getUser().getID()), from, to, null, null)
                            , protectedAreas);
/*
                            Map<String, Integer> lpa = ord.getListOfPolygonsWithOccurrences(
                                    driver.asNodeKey(thisRequest.getUser().getID()), from, to, protectedAreas);
*/
                            pw.print("<p>Nº de áreas protegidas: " + lpa.size() + "</p><table class=\"subtable\"><thead><tr><th>Área protegida</th><th>Nº de registos</th></tr></thead>");
                            for(Map.Entry<String, Integer> e : lpa.entrySet()) {
                                pw.printf("<tr><td>%s</td><td>%d</td></tr>", e.getKey(), e.getValue());
                            }
                            pw.print("</table>");
                            break;

                        case "report-alltaxa":
                            pw.print("<table class=\"subtable\"><thead><tr><th>Taxon</th><th>Nº de registos</th></tr></thead>");
                            Iterator<StatisticPerTaxon> it5 = ord.getAllTaxa(
                                    driver.asNodeKey(thisRequest.getUser().getID()), from, to);
                            while(it5.hasNext()) {
                                StatisticPerTaxon te1 = it5.next();
                                pw.print("<tr><td>" + te1.getName() + "</td><td>" + te1.getValue() + "</td></tr>");
                            }
                            pw.print("</table>");
                            break;

                        case "report-listspecimens":
                            PolygonTheme protectedAreas1 = new PolygonTheme(this.getClass().getResourceAsStream("SNAC.geojson"), "SITE_NAME");
                            Map<String, Integer> lpa1;
                            Occurrence so;
                            pw.print("<table class=\"subtable\"><thead><tr><th>Taxon colhido (<i>sic</i>)</th><th>Data de colheita</th>" +
                                    "<th>Local</th><th>Latitude</th><th>Longitude</th><th>Nº de espécimes</th><th>Área protegida</th></tr></thead>");
                            Iterator<Occurrence> it6 = ord.getOccurrencesWithTagCollected(
                                    driver.asNodeKey(thisRequest.getUser().getID()), from, to, territory, thisRequest.getParameterAsString("tag"));

                            while(it6.hasNext()) {
                                so = it6.next();
                                lpa1 = ord.getListOfPolygonsWithOccurrences(
                                        Collections.singleton(so).iterator(), protectedAreas1);
                                pw.printf("<tr><td>%s</td><td>%s</td><td>%s</td><td>%f</td><td>%f</td><td>%d</td><td>%s</td></tr>", so.getOccurrence().getVerbTaxon(), so._getDate()
                                    , so.getLocality() == null ? "" : so.getLocality(), so._getLatitude(), so._getLongitude()
                                    , so.getOccurrence().getHasSpecimen()
                                    , lpa1.size() > 0 ? StringUtils.implode(", ", lpa1.keySet().toArray(new String[0])) : "none");
                            }
                            break;
                    }
                } else {    // dates are null
                    switch(what) {
                        case "report-sheetauthor":
                            toc = OccurrenceReportArangoDriver.TypeOfCollaboration.TEXTAUTHOR;
                        case "report-sheetassessor":
                            if(toc == null) toc = OccurrenceReportArangoDriver.TypeOfCollaboration.ASSESSOR;
                        case "report-sheetreviewer":
                            if(toc == null) toc = OccurrenceReportArangoDriver.TypeOfCollaboration.REVIEWER;
                            int count1 = 0;
                            pw.print("<ul>");
                            Iterator<TaxEnt> it5 = ord.getRedListSheetsIsCollaborator(
                                    driver.asNodeKey(thisRequest.getUser().getID()), territory, toc);
                            while(it5.hasNext()) {
                                count1++;
                                TaxEnt te1 = it5.next();
                                pw.print("<li>" + te1.getFullName(true) + "</li>");
                            }
                            pw.print("</ul><p>Nº de taxa: " + count1 + "</p>");
                            break;
                    }
                }
                pw.flush();
                break;

            case "setoptions":
                String option = thisRequest.getParameterAsString("option");
                territory = thisRequest.getParameterAsString("territory");
                errorIfAnyNull(option, territory);
                rls = driver.getRedListSettings(territory);
                if(rls.getID() == null)
                    rls = driver.getNodeWorkerDriver().createDocument(rls);

                switch(option) {
                    case "lockediting":
                        driver.getNodeWorkerDriver().updateDocument(driver.asNodeKey(rls.getID()), "editionLocked", thisRequest.getParameterAsBoolean("value", false));
                        break;

                    case "lockeditingfortags":
                        String[] tagsToLock = thisRequest.getParameterAsStringArray("tags");
                        if(StringUtils.isArrayEmpty(tagsToLock)) break;
                        for(String t : tagsToLock)
                            rls.lockEditionForTag(t);
                        driver.getNodeWorkerDriver().updateDocument(driver.asNodeKey(rls.getID()), "tagsEditionLocked", rls.getLockedTags());
                        break;

                    case "unlockeditingforalltags":
                        driver.getNodeWorkerDriver().updateDocument(driver.asNodeKey(rls.getID()), "tagsEditionLocked", new HashSet<>());
                        break;

                    case "historicalthreshold":
                        driver.getNodeWorkerDriver().updateDocument(driver.asNodeKey(rls.getID()), "historicalThreshold", thisRequest.getParameterAsInteger("value", 1990));
                        break;

                    case "svgDivisor":
                        driver.getNodeWorkerDriver().updateDocument(driver.asNodeKey(rls.getID()), "svgDivisor", thisRequest.getParameterAsInteger("value", 1));
                        break;

                    case "editionslastndays":
                        driver.getNodeWorkerDriver().updateDocument(driver.asNodeKey(rls.getID()), "editionsLastNDays", thisRequest.getParameterAsInteger("value", 20));
                        break;

                    case "unlockEdition":
                        if(!thisRequest.getUser().canMANAGE_VERSIONS()) {thisRequest.error("You don't have privileges for this operation!"); return;}
                        rls.unlockEditionForTaxon(thisRequest.getParameterAsString("value"));
                        driver.getNodeWorkerDriver().updateDocument(driver.asNodeKey(rls.getID()), "unlockedSheets", rls.getUnlockedSheets());
                        break;

                    case "removeUnlockEdition":
                        if(!thisRequest.getUser().canMANAGE_VERSIONS()) {thisRequest.error("You don't have privileges for this operation!"); return;}
                        rls.removeUnlockEditionException(thisRequest.getParameterAsString("value"));
                        driver.getNodeWorkerDriver().updateDocument(driver.asNodeKey(rls.getID()), "unlockedSheets", rls.getUnlockedSheets());
                        break;

                    case "setMapBounds":
                        Rectangle newBounds = new Rectangle(thisRequest.getParameterAsLong("mapleft", 440000L),
                                thisRequest.getParameterAsLong("mapright", 740000L),
                                thisRequest.getParameterAsLong("maptop", 4687000L),
                                thisRequest.getParameterAsLong("mapbottom", 4090000L)
                                );
                        driver.getNodeWorkerDriver().updateDocument(driver.asNodeKey(rls.getID()), "mapBounds", newBounds);
                        break;

                    case "setBaseMap":
                        String mapWKT = thisRequest.getParameterAsString("mapWKT");
                        if(StringUtils.isStringEmpty(mapWKT)) break;
                        PolygonTheme baseMap = new PolygonTheme(mapWKT);
                        Polygon polygon = baseMap.iterator().next().getValue();
/*
                        StringBuilder sb1 = new StringBuilder();
                        boolean firstPoint = true;
                        Iterator<Point2D> it1 = polygon.getCoordinates();
                        Point2D p;
                        while(it1.hasNext()) {
                            if(firstPoint) {
                                sb1.append("M");
                                firstPoint = false;
                            } else
                                sb1.append("L");
                            p = it1.next();
                            sb1.append(String.format("%.0f", p.x())).append(" ")
                                    .append(String.format("%.0f", p.y()));
                        }
*/

                        driver.getNodeWorkerDriver().updateDocument(driver.asNodeKey(rls.getID()), "baseMap"
                                , new Polygon[] {polygon});
//                                , new String[] {polygon.toSVGPathString()});
                        break;
                }

                driver.reloadSettings();
                thisRequest.success("Ok");
                break;

            case "sheet2PDF":
//                thisRequest.request.setAttribute("inventories", itInv);
                thisRequest.request.getRequestDispatcher("/pdf/redlistsheet-layout.jsp").forward(thisRequest.request, thisRequest.response);
                break;

            case "downloadsheetPDF":
                //TODO HERE
                thisRequest.response.setContentType("application/pdf; charset=utf-8");
                thisRequest.response.addHeader("Content-Disposition", "attachment;Filename=\"sheets.pdf\"");
                thisRequest.response.setCharacterEncoding(StandardCharsets.UTF_8.toString());

                StringBuffer url1 = thisRequest.request.getRequestURL();
                String uri1 = thisRequest.request.getRequestURI();
                String ctx1 = thisRequest.request.getContextPath();
                String base1 = url1.substring(0, url1.length() - uri1.length() + ctx1.length()) + "/" + "redlist/api/sheet2PDF?id=";

//                String idsString = StringUtils.implode(",", ids.toArray(new String[0]));

                PdfRendererBuilder builder = new PdfRendererBuilder();
                final NaiveUserAgent.DefaultUriResolver defaultUriResolver = new NaiveUserAgent.DefaultUriResolver();

                builder.useUriResolver(defaultUriResolver);
                builder.useSVGDrawer(new BatikSVGDrawer());
                builder.withUri(base1 + URLEncoder.encode("as", StandardCharsets.UTF_8.toString()));
                builder.toStream(thisRequest.response.getOutputStream());
                try {
                    builder.run();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;

            default:
                thisRequest.error("Path not found.");
        }
    }

    @Override
    public void doFloraOnPost(ThisRequest thisRequest) throws ServletException, IOException, FloraOnException {
        ListIterator<String> path = thisRequest.getPathIteratorAfter("api");
        String territory;
        RedListDataEntity rlde;
        Gson gs;
        Part filePart;
        InputStream fileContent = null;

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

            case "replacefromtable":
                territory = thisRequest.getParameterAsString("territory");
                boolean dryRun = thisRequest.getParameterAsBoolean("dry", true);
                thisRequest.getUser().resetEffectivePrivileges();
                if(!thisRequest.getUser().isAdministrator()) throw new FloraOnException(FieldValues.getString("Error.2"));

                try {
                    filePart = thisRequest.request.getPart("replaceTable");
                    System.out.println("File size: " + filePart.getSize());

                    if(filePart.getSize() == 0) throw new FloraOnException("You must select a file.");
                    fileContent = filePart.getInputStream();
                } catch (IOException e) {
                    e.printStackTrace();
                    throw new FloraOnException("Some error.");
                }

                Map<String, String> replacementTable = new HashMap<>();
                if(fileContent != null) {
                    Reader freader = new InputStreamReader(fileContent, StandardCharsets.UTF_8);
                    CSVParser records = CSVFormat.TDF.withQuote('\"').withDelimiter('\t').parse(freader);
                    for (CSVRecord record : records) {
                        replacementTable.put(record.get(0), record.get(1));
                    }

                }

//                replacementTable.put("except", "CC");
//                replacementTable.put("declín", "AA");
                if(dryRun) {
                    JobSubmitter.newJobTask(new SearchAndReplaceDryJob(
                            driver.getRedListData().getAllRedListData(territory, false, null)
                            , replacementTable), driver);
                } else {
                    JobSubmitter.newJobTask(new SearchAndReplaceJob(
                            driver.getRedListData().getAllRedListData(territory, false, null)
                            , replacementTable), driver);
                }
//                        , Collections.singletonList(driver.getRedListData().getRedListDataEntity(territory, driver.asNodeKey("taxent/338144474014"))).iterator()
                //                        driver.getRedListData().getAllRedListData(territory, false, null)

                thisRequest.success("Ok");
                break;

            // TODO this should go to the occurrence API!
            case "downloadoccurrencesinpolygon":
                String polygonWKT1 = thisRequest.getParameterAsString("polygon");
                String filter = thisRequest.getParameterAsString("filter");
                Iterator<Occurrence> itOcc = driver.getQueryDriver().findOccurrencesContainedIn(polygonWKT1, filter, null);
                thisRequest.response.setContentType("text/csv; charset=utf-8");
                thisRequest.response.addHeader("Content-Disposition", "attachment;Filename=\"occurrences-in-polygon.csv\"");
                thisRequest.response.setCharacterEncoding(StandardCharsets.UTF_8.toString());

                Common.exportOccurrencesToCSV(itOcc, thisRequest.response.getWriter());

/*
                PrintWriter pw = thisRequest.response.getWriter();
                CSVPrinter csvp = new CSVPrinter(pw, CSVFormat.EXCEL);
                csvp.printRecord("Taxon", "VerbTaxon", "Confidence", "Date", "Latitude", "Longitude", "Precision", "Observers", "Comments", "Taxon notes");

                // iterate all inventories falling in polygon
                while(itOcc.hasNext()) {
                    Occurrence inv = itOcc.next();
                    OBSERVED_IN oin = inv.getOccurrence();
                    csvp.printRecord(oin.getTaxEnt() != null ? oin.getTaxEnt().getFullName(false) : "-"
                            , oin.getConfidence() == null ? "-" : oin.getConfidence().getLabel(), inv._getDate()
                            , oin.getVerbTaxon(), inv._getLatitude(), inv._getLongitude(), inv.getPrecision() == null ? "-" : inv.getPrecision().toString()
                            , StringUtils.implode(", ", inv._getObserverNames())
                            , inv.getPubNotes(), oin.getComment());
                }
                csvp.close();
*/

                break;

            // TODO this should go to the occurrence API!
            case "downloadinventoriesPDF":
                String polygonWKT2 = thisRequest.getParameterAsString("polygon");
                String filter1 = thisRequest.getParameterAsString("filter");
                Iterator<Inventory> itInv1 = driver.getQueryDriver().findInventoriesContainedIn(polygonWKT2, filter1);

                thisRequest.response.setContentType("application/pdf; charset=utf-8");
                thisRequest.response.addHeader("Content-Disposition", "attachment;Filename=\"inventories.pdf\"");
                thisRequest.response.setCharacterEncoding(StandardCharsets.UTF_8.toString());

                StringBuffer url = thisRequest.request.getRequestURL();
                String uri = thisRequest.request.getRequestURI();
                String ctx = thisRequest.request.getContextPath();
                String base = url.substring(0, url.length() - uri.length() + ctx.length()) + "/" + "occurrences/api/exportinventory?ids=";

                Common.exportInventoriesToPDF(itInv1, base, thisRequest.response.getOutputStream());
                break;

            default:
                doFloraOnGet(thisRequest);
        }
    }
}
