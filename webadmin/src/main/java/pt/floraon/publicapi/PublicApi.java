package pt.floraon.publicapi;

import jline.internal.Log;
import org.apache.commons.io.IOUtils;
import pt.floraon.authentication.Privileges;
import pt.floraon.authentication.entities.User;
import pt.floraon.driver.FloraOnException;
import pt.floraon.driver.interfaces.INodeKey;
import pt.floraon.geometry.PolygonTheme;
import pt.floraon.redlistdata.*;
import pt.floraon.redlistdata.dataproviders.SimpleOccurrenceDataProvider;
import pt.floraon.redlistdata.entities.RedListDataEntity;
import pt.floraon.redlistdata.entities.RedListSettings;
import pt.floraon.redlistdata.occurrences.BasicOccurrenceFilter;
import pt.floraon.driver.interfaces.OccurrenceFilter;
import pt.floraon.redlistdata.occurrences.OccurrenceProcessor;
import pt.floraon.redlistdata.occurrences.TaxonOccurrenceProcessor;
import pt.floraon.server.FloraOnServlet;
import pt.floraon.taxonomy.entities.TaxEnt;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by miguel on 15-07-2017.
 */
@WebServlet("/api/*")
public class PublicApi extends FloraOnServlet {
    static Pattern svgURL = Pattern.compile("^[a-zçA-Z]+_[a-zç-]+_(?<id>[0-9]+).svg$");
    @Override
    public void doFloraOnGet(ThisRequest thisRequest) throws ServletException, IOException, FloraOnException {
        PrintWriter writer = null;
        ListIterator<String> path;
        String territory = driver.getDefaultRedListTerritory();
        List<SimpleOccurrenceDataProvider> sodps;
        RedListSettings rls = driver.getRedListSettings(territory);
        try {
            path = thisRequest.getPathIteratorAfter("api");
        } catch (FloraOnException e) {
            thisRequest.error("Missing parameters.");
            return;
        }

        User user = thisRequest.getUser();
        switch(path.next()) {
            case "svgmap":
                INodeKey key;
                String format = "." + thisRequest.getParameterAsString("fmt", "svg").toLowerCase();
                boolean historical = false;
                String category = null;
                String threatType = null, mapFileName = "";
                Integer squareSize;
                Float borderWidth;
                String squareFill = null;
                boolean standAlone, searchForCached;
                boolean viewAll, showProtectedAreas;
                Matcher m;
                Set<TaxEnt> filteredTaxa = new HashSet<>();

                // a kind of mod_rewrite
                if(path.hasNext() && (m = svgURL.matcher(path.next())).find()) {
                    key = driver.asNodeKey("taxent/" + m.group("id"));
                    squareSize = 10000;
                    borderWidth = 0.2f;
                    viewAll = false;
                    standAlone = true;
                    showProtectedAreas = false;
                } else {
                    key = thisRequest.getParameterAsKey("taxon");
                    category = thisRequest.getParameterAsString("category");
                    threatType = thisRequest.getParameterAsString("threatType");
                    squareSize = thisRequest.getParameterAsInteger("size", 10000);
                    borderWidth = thisRequest.getParameterAsFloat("border", 2f);
                    viewAll = "all".equals(thisRequest.getParameterAsString("view"));
                    historical = thisRequest.getParameterAsBoolean("historical", false);
                    standAlone = thisRequest.getParameterAsBoolean("sa", true);
                    squareFill = "#" + thisRequest.getParameterAsString("squareFill");
                    showProtectedAreas = thisRequest.getParameterAsBoolean("pa", true);
                }

                if(squareSize < 10000 && !user.canVIEW_OCCURRENCES()) {
                    thisRequest.response.sendError(HttpServletResponse.SC_FORBIDDEN, "No public access for this precision.");
                    return;
                }

                sodps = driver.getRedListData().getSimpleOccurrenceDataProviders();
                if(key == null && category == null && threatType == null) return;

                switch(format) {
                    case ".svg":
                        thisRequest.response.setContentType("image/svg+xml; charset=utf-8");
                        break;

                    case ".csv":
                        thisRequest.response.setContentType("text/csv; charset=utf-8");
                        break;

                    default:
                        thisRequest.error("Format unknown.");
                }
                thisRequest.response.setCharacterEncoding("UTF-8");
                thisRequest.setCacheHeaders(60 * 10);
//                thisRequest.response.addHeader("Access-Control-Allow-Origin", "*");

                PolygonTheme protectedAreas = showProtectedAreas ? rls.getProtectedAreas() : null;

                GridMapExporter processor = null;
                OccurrenceFilter occurrenceFilter;

                if("maybeextinct".equals(category))
                    occurrenceFilter = BasicOccurrenceFilter.RedListCurrentMapFilter(driver, territory).withoutDateFilter();//  OnlyCertainRecords(driver, territory);
                else
                    occurrenceFilter = viewAll
                            ? BasicOccurrenceFilter.RedListCurrentMapFilter(driver, territory).withoutDateFilter()
                            : (historical ? BasicOccurrenceFilter.RedListHistoricalMapFilter(driver, territory)
                            : BasicOccurrenceFilter.RedListCurrentMapFilter(driver, territory));

                if(key != null) {   // we want one taxon
                    TaxEnt te2 = driver.getNodeWorkerDriver().getTaxEntById(key);
                    if(te2 == null) return;

                    if(thisRequest.getParameterAsBoolean("download", false))
                        thisRequest.setDownloadFileName(String.format("map-%s%s", te2._getNameURLEncoded(), format));

                    for(SimpleOccurrenceDataProvider edp : sodps)
                        edp.executeOccurrenceQuery(te2);

                    processor = new OccurrenceProcessor(sodps, protectedAreas, squareSize, occurrenceFilter);
                    // we output directly to page
                    writer = thisRequest.response.getWriter();
                    searchForCached = false;
                } else if(category != null) {   // we want a threat category
                    List<String> dosOlivais = rls.getTaxonGroup("olivais");

                    Iterator<RedListDataEntity> it =
                            driver.getRedListData().getAllRedListData(territory, false, null);
                    RedListDataEntity rlde;
                    while(it.hasNext()) {
                        rlde = it.next();
                        if(rlde.getAssessment().getFinalCategory() == null) continue;

                        RedListEnums.RedListCategories cat = rlde.getAssessment().getFinalCategory().getEffectiveCategory();
                        switch(category) {
                            case "CR":
                                if(cat.equals(RedListEnums.RedListCategories.CR)) filteredTaxa.add(rlde.getTaxEnt());
                                break;
                            case "EN":
                                if(cat.equals(RedListEnums.RedListCategories.EN)) filteredTaxa.add(rlde.getTaxEnt());
                                break;
                            case "VU":
                                if(cat.equals(RedListEnums.RedListCategories.VU)) filteredTaxa.add(rlde.getTaxEnt());
                                break;
                            case "threatened":
                                if(cat.isThreatened()) filteredTaxa.add(rlde.getTaxEnt());
                                break;
                            case "maybeextinct":
                                if(cat.isPossiblyExtinct() || (cat.equals(RedListEnums.RedListCategories.CR)
                                        && rlde.getAssessment().getSubCategory() != null && !rlde.getAssessment().getSubCategory().equals(RedListEnums.CRTags.NO_TAG)))
                                    filteredTaxa.add(rlde.getTaxEnt());
                                break;
                            case "olivais":
                                if(dosOlivais.contains(rlde.getTaxEnt().getID()))
                                    filteredTaxa.add(rlde.getTaxEnt());
                                break;
                        }
                    }
                    mapFileName = "map-" + category + format;
                    searchForCached = true;
                    if(thisRequest.getParameterAsBoolean("download", false))
                        thisRequest.setDownloadFileName(mapFileName);
                } else {   // we want per threat types
                    threatType = threatType.toUpperCase();
                    Iterator<RedListDataEntity> it =
                            driver.getRedListData().getAllRedListData(territory, false, null);
                    RedListDataEntity rlde;
                    while(it.hasNext()) {
                        rlde = it.next();
                        // only include threatened species
                        if(rlde.getAssessment().getFinalCategory() == null || !rlde.getAssessment().getFinalCategory().isThreatened()) continue;
                        for(RedListEnums.Threats thr : rlde.getThreats().getThreats()) {
                            if(threatType.contains(thr.getType().getLetter())) {
                                filteredTaxa.add(rlde.getTaxEnt());
                                break;
                            }
                        }
                    }
                    mapFileName = "map-threats-" + threatType + format;
                    searchForCached = true;
                    if(thisRequest.getParameterAsBoolean("download", false))
                        thisRequest.setDownloadFileName(mapFileName);
                }

                if(searchForCached) {
                    if(driver.getProperties().getProperty("folder") == null) {
                        Log.info("Temporary folder is not defined in floraon.properties");
                        writer = thisRequest.response.getWriter();
                    } else {
                        // use cached map if possible
                        File outfile = new File(driver.getProperties().getProperty("folder"), mapFileName);

                        if (thisRequest.getParameterAsBoolean("refresh", false)) {
                            outfile.delete();
                            if (!outfile.createNewFile()) {
                                Log.info("Couldn't create SVG file.");
                                writer = thisRequest.response.getWriter();
                            } else writer = new PrintWriter(outfile);
                        } else {
                            if (outfile.exists()) {
                                IOUtils.copy(new FileReader(outfile), thisRequest.response.getWriter());
                                break;
                            } else writer = new PrintWriter(outfile);
                        }
                    }
                    processor = new TaxonOccurrenceProcessor(sodps, filteredTaxa.iterator(), squareSize, occurrenceFilter);
                }
//                thisRequest.includeSVGMap(processor, territory, thisRequest.getParameterAsBoolean("basemap", false)
//                        , borderWidth, thisRequest.getParameterAsBoolean("shadow", true), protectedAreas, standAlone, true);
                if(writer == null) break;

                switch(format) {
                    case ".svg":
                        thisRequest.exportSVGMap(writer, processor, territory, thisRequest.getParameterAsBoolean("basemap", false)
                                , borderWidth, thisRequest.getParameterAsBoolean("shadow", true), protectedAreas
                                , standAlone, true, thisRequest.getParameterAsBoolean("stroke", false), squareFill);
                        break;

                    case ".csv":
                        thisRequest.exportSVGMapAsCSV(writer, processor);
                        break;
                }

/*
                processor.exportSVG(writer, true, false
                        , thisRequest.getParameterAsBoolean("basemap", false)
                        , true
                        , borderWidth
                        , thisRequest.getParameterAsBoolean("shadow", true), false
                        , driver.getRedListSettings(territory));
                writer.flush();
*/
                if(thisRequest.getParameterAsBoolean("close", true))
                    writer.close();

                if(writer != thisRequest.response.getWriter()) {
                    File outfile = new File(driver.getProperties().getProperty("folder"), "map-" + category + format);
                    if(outfile.exists())
                        IOUtils.copy(new FileReader(outfile), thisRequest.response.getWriter());
                }
                break;


            case "wktmap":
                key = thisRequest.getParameterAsKey("taxon");
                squareSize = thisRequest.getParameterAsInteger("size", 10000);
                viewAll = "all".equals(thisRequest.getParameterAsString("view"));

                if(squareSize < 10000)
                    thisRequest.ensurePrivilege(Privileges.VIEW_OCCURRENCES, "No public access for this precision.");

                if(key == null) return;
                sodps = driver.getRedListData().getSimpleOccurrenceDataProviders();

                thisRequest.response.setContentType("text/csv; charset=utf-8");
                thisRequest.response.setCharacterEncoding("UTF-8");
                thisRequest.setCacheHeaders(60);

                TaxEnt te3 = driver.getNodeWorkerDriver().getTaxEntById(key);
                if(te3 == null) {
                    thisRequest.errorServer(String.format("Taxon %s not found.", key));
                    return;
                }

                thisRequest.setDownloadFileName(String.format("map-%s.wkt", te3._getNameURLEncoded()));

                for(SimpleOccurrenceDataProvider edp : sodps)
                    edp.executeOccurrenceQuery(te3);
                occurrenceFilter = viewAll
//                        ? BasicOccurrenceFilter.OnlyCertainRecords(driver, territory)
                        ? BasicOccurrenceFilter.RedListCurrentMapFilter(driver, territory).withoutDateFilter()
                        : BasicOccurrenceFilter.RedListCurrentMapFilter(driver, territory);

                processor = new OccurrenceProcessor(sodps, null, squareSize, occurrenceFilter);

                processor.squares().toWKT(thisRequest.response.getWriter());
                break;
        }
    }

}
