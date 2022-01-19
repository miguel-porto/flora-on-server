package pt.floraon.redlistdata.servlets;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Multimap;
import com.google.common.collect.Table;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.mutable.MutableInt;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import pt.floraon.authentication.Privileges;
import pt.floraon.authentication.entities.TaxonPrivileges;
import pt.floraon.bibliography.BibliographyCompiler;
import pt.floraon.bibliography.entities.Reference;
import pt.floraon.driver.*;
import pt.floraon.driver.datatypes.SafeHTMLString;
import pt.floraon.driver.interfaces.INodeKey;
import pt.floraon.driver.jobs.JobRunner;
import pt.floraon.driver.jobs.JobSubmitter;
import pt.floraon.driver.results.InferredStatus;
import pt.floraon.driver.utils.StringUtils;
import pt.floraon.ecology.entities.Habitat;
import pt.floraon.geometry.PolygonTheme;
import pt.floraon.occurrences.entities.Inventory;
import pt.floraon.occurrences.entities.OBSERVED_IN;
import pt.floraon.occurrences.fields.parsers.DateParser;
import pt.floraon.redlistdata.*;
import pt.floraon.redlistdata.dataproviders.SimpleOccurrenceDataProvider;
import pt.floraon.redlistdata.entities.*;
import pt.floraon.redlistdata.jobs.SearchAndReplaceDryJob;
import pt.floraon.redlistdata.occurrences.BasicOccurrenceFilter;
import pt.floraon.driver.interfaces.OccurrenceFilter;
import pt.floraon.redlistdata.occurrences.OccurrenceProcessor;
import pt.floraon.redlistdata.occurrences.SimpleOccurrenceClusterer;
import pt.floraon.redlistdata.threats.Threat;
import pt.floraon.taxonomy.entities.CanonicalName;
import pt.floraon.taxonomy.entities.TaxEnt;
import pt.floraon.authentication.entities.User;
import pt.floraon.server.FloraOnServlet;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.DateFormat;
import java.util.*;
import java.util.List;

import static java.nio.file.Files.readAllLines;
import static pt.floraon.authentication.Privileges.*;
import static pt.floraon.driver.utils.StringUtils.cleanArray;
import static pt.floraon.driver.utils.StringUtils.sanitizeHtmlId;

/**
 * Main page of red list data
 * The path following /redlist should be the territory short name
 * Created by Miguel Porto on 01-11-2016.
 */
@WebServlet("/redlist/*")
public class RedListMainPages extends FloraOnServlet {
    @Override
    public void doFloraOnPost(ThisRequest thisRequest) throws ServletException, IOException, FloraOnException {
        doFloraOnGet(thisRequest);
    }

    @Override
    public void doFloraOnGet(ThisRequest thisRequest) throws ServletException, IOException, FloraOnException {
        final HttpServletRequest request = thisRequest.request;
        RedListDataEntitySnapshot rldeSnap = null;
        String what;
        TaxEnt te;
        Iterator<TaxEnt> iTaxEnt;
        Iterator<RedListDataEntity> rldeIt;
        RedListDataEntity rlde;
        long sizeOfSquare;
/*
Gson gs = new GsonBuilder().setPrettyPrinting().create();
System.out.println(gs.toJson(getUser()));
*/

        ListIterator<String> path;
        try {
            path = thisRequest.getPathIteratorAfter("redlist");
        } catch (FloraOnException e) {
            if(!thisRequest.getUser().canCREATE_REDLIST_DATASETS()) {
                thisRequest.response.sendRedirect("/floraon");
                return;
            }
            // no territory specified
            request.setAttribute("what", "addterritory");
            request.setAttribute("territories", driver.getListDriver().getAllTerritories(null));
            request.getRequestDispatcher("/main-redlistinfo.jsp").forward(request, thisRequest.response);
            return;
        }

        String territory = path.next();
        request.setAttribute("territory", territory);
        request.setAttribute("what", what = thisRequest.getParameterAsString("w", "main"));

        // make a map of user IDs and names
        List<User> allUsers = driver.getAdministration().getAllUsers(false);
        Map<String, String> userMap = new HashMap<>();
        for(User u : allUsers) {
            userMap.put(u.getID(), u.getName());
//            userMap.put(u.getIDURLEncoded(), u.getName());
        }

        request.setAttribute("allUsers", allUsers);
        request.setAttribute("userMap", userMap);

        List<String> warnings = new ArrayList<>();

        thisRequest.getUser().resetEffectivePrivileges();

        sizeOfSquare = thisRequest.getUser().canVIEW_OCCURRENCES() ? 2000 : 10000;

        switch (what) {
            case "search":
                String searchtmp;
                Multimap<RedListDataEntity, String> searchResults = ArrayListMultimap.create();

                rldeIt = driver.getRedListData().getAllRedListData(territory, false, null);
                while(rldeIt.hasNext()) {
                    rlde = rldeIt.next();

                    if(rlde.getTaxEnt().getNameWithAnnotationOnly(false).toLowerCase()
                            .contains(thisRequest.getParameterAsString("s").toLowerCase()))
                        searchResults.put(rlde, "<span class=\"wordtag compact\">TAXON NAME</span> " + rlde.getTaxEnt().getNameWithAnnotationOnly(false));

                    searchtmp = rlde.getGeographicalDistribution().getDescription().searchString(thisRequest.getParameterAsString("s"));
                    if(searchtmp != null)
                        searchResults.put(rlde, "<span class=\"wordtag compact\">DISTRIBUTION</span> " + searchtmp);

                    searchtmp = rlde.getPopulation().getDescription().searchString(thisRequest.getParameterAsString("s"));
                    if(searchtmp != null)
                        searchResults.put(rlde, "<span class=\"wordtag compact\">POPULATION</span> " + searchtmp );

                    searchtmp = rlde.getEcology().getDescription().searchString(thisRequest.getParameterAsString("s"));
                    if(searchtmp != null)
                        searchResults.put(rlde, "<span class=\"wordtag compact\">ECOLOGY</span> " + searchtmp);

                    searchtmp = rlde.getUsesAndTrade().getDescription().searchString(thisRequest.getParameterAsString("s"));
                    if(searchtmp != null)
                        searchResults.put(rlde, "<span class=\"wordtag compact\">USES</span> " + searchtmp);

                    searchtmp = rlde.getThreats().getDescription().searchString(thisRequest.getParameterAsString("s"));
                    if(searchtmp != null)
                        searchResults.put(rlde, "<span class=\"wordtag compact\">THREATS</span> " + searchtmp);

                    searchtmp = rlde.getConservation().getDescription().searchString(thisRequest.getParameterAsString("s"));
                    if(searchtmp != null)
                        searchResults.put(rlde, "<span class=\"wordtag compact\">CONSERVATION</span> " + searchtmp);

                    searchtmp = rlde.getAssessment().getFinalJustification().searchString(thisRequest.getParameterAsString("s"));
                    if(searchtmp != null)
                        searchResults.put(rlde, "<span class=\"wordtag compact\">JUSTIFICATION</span> " + searchtmp);

                    searchtmp = rlde.getReviewerComments().searchString(thisRequest.getParameterAsString("s"));
                    if(searchtmp != null)
                        searchResults.put(rlde, "<span class=\"wordtag compact\">REVISION</span> " + searchtmp);

                    searchtmp = rlde.getReplyToReviewer().searchString(thisRequest.getParameterAsString("s"));
                    if(searchtmp != null)
                        searchResults.put(rlde, "<span class=\"wordtag compact\">REPLY</span> " + searchtmp);

                }
                request.setAttribute("searchResults", searchResults.asMap().entrySet());
                break;

            case "alleditions": // displays all editions made in the last days (user setting)
                thisRequest.ensurePrivilege(VIEW_OCCURRENCES);
                RedListDataEntity rldetmp;
                Set<RevisionWithTaxEnt> editsall = new TreeSet<>(Collections.reverseOrder(new RevisionWithTaxEnt.RevisionWithTaxEntComparator()));
                RevisionWithTaxEnt daywiserev;
                Date today = new Date();
                Calendar cal = Calendar.getInstance();
                cal.setTime(today);
                cal.add(Calendar.DATE, -driver.getRedListSettings(territory).getEditionsLastNDays());

                // compile edition history
                rldeIt = driver.getRedListData().getAllRedListData(territory, false, null);
                while(rldeIt.hasNext()) {
                    rldetmp = rldeIt.next();
                    for (Revision r : rldetmp.getRevisions()) {
                        if(r.getUser() == null) continue;
                        if(r.getDateTimeSaved().before(cal.getTime())) continue;
                        daywiserev = new RevisionWithTaxEnt(r.getDayWiseRevision());
                        daywiserev.setTaxEnt(rldetmp.getTaxEnt());
                        editsall.add(daywiserev);
                    }
                }

                request.setAttribute("revisions", editsall);
                request.setAttribute("ndays", driver.getRedListSettings(territory).getEditionsLastNDays());

                break;

            case "published":
                Iterator<RedListDataEntitySnapshot> its = driver.getRedListData().getSnapshotsByPublicationStatus(territory, RedListEnums.PublicationStatus.PUBLISHED);
                request.setAttribute("specieslist", its);
                break;
/*
 * Shows the taxon list
 */
            case "main":
//                List<TaxEnt> taxEntList = driver.getListDriver().getAllSpeciesOrInferiorTaxEnt(true, true, territory, null, null);
//                List<RedListDataEntity> taxEntList = driver.getRedListData().getAllRedListData(territory, getUser().canMANAGE_REDLIST_USERS());
                thisRequest.setNoCache();
                thisRequest.getUser().resetEffectivePrivileges();
//                taxEntList.get(0).getAssessment().getAssessmentStatus().isAssessed()
                Set<String> at = driver.getRedListData().getRedListTags(territory);
                Map<String, String> ate = new HashMap<>();
                for(String s : at)
                    ate.put(sanitizeHtmlId(s), s);

                if(!thisRequest.isOptionSet("onlynative"))
                    thisRequest.setOption("onlynative", true);

                Iterator<RedListDataEntity> taxEntList =
                        driver.getRedListData().getAllRedListData(territory, true, null); //new String[] {"Lista preliminar", "Lista Alvo", "Lista B"}
                request.setAttribute("allTags", ate.entrySet());
                request.setAttribute("specieslist", taxEntList);
                RedListSettings rls2 = driver.getRedListSettings(territory);
                request.setAttribute("rls", rls2);
//                taxEntList.next().getAssessment().getAuthors()
                break;

            case "settings":    // settings page
                request.setAttribute("lockediting", driver.getRedListSettings(territory).isEditionGloballyLocked());
                request.setAttribute("historicalthreshold", driver.getRedListSettings(territory).getHistoricalThreshold());
                request.setAttribute("cutRecordsAfter", driver.getRedListSettings(territory).getCutRecordsInsertedAfter() == null
                        ? null : Constants.dateFormatYMD.get().format(driver.getRedListSettings(territory).getCutRecordsInsertedAfter()));
                request.setAttribute("unlockedSheets", driver.getRedListSettings(territory).getUnlockedSheets());
                request.setAttribute("allTags", driver.getRedListData().getRedListTags(territory));
                request.setAttribute("lockedTags", driver.getRedListSettings(territory).getLockedTags());
                request.setAttribute("mapBounds", driver.getRedListSettings(territory).getMapBounds());
                request.setAttribute("svgDivisor", driver.getRedListSettings(territory).getSvgMapDivisor());
                break;

            case "setoption":
                thisRequest.setOption(
                        thisRequest.getParameterAsString("n"),
                        thisRequest.getParameterAsBooleanNoNull("v"));
                return;

            case "sheet":   // read-only sheet
                INodeKey idSnap = driver.asNodeKey("redlist_snapshots_" + territory + "/" + thisRequest.getParameterAsString("id"));
                rldeSnap = driver.getNodeWorkerDriver().getDocument(idSnap, RedListDataEntitySnapshot.class);

            case "taxon":   // this is the entry point for a red list sheet
                String redListSheetTemplate = driver.getProperties().getProperty("redListSheetTemplate");
                if(redListSheetTemplate == null) redListSheetTemplate = "frag-redlistsheet-flora";
                request.setAttribute("redListSheetTemplate", redListSheetTemplate);
                // enums
                request.setAttribute("geographicalDistribution_DeclineDistribution", RedListEnums.DeclineDistribution.values());
                request.setAttribute("DeclineQualifier", RedListEnums.DeclineQualifier.values());
                request.setAttribute("geographicalDistribution_ExtremeFluctuations", RedListEnums.ExtremeFluctuations.values());
                request.setAttribute("population_NrMatureIndividualsCategory", RedListEnums.NrMatureIndividuals.values());
                request.setAttribute("population_TypeOfEstimate", RedListEnums.TypeOfPopulationEstimate.values());
                request.setAttribute("population_PopulationDecline", RedListEnums.DeclinePopulation.values());
                request.setAttribute("population_PopulationSizeReduction", RedListEnums.PopulationSizeReduction.values());
                request.setAttribute("population_SeverelyFragmented", RedListEnums.SeverelyFragmented.values());
                request.setAttribute("population_ExtremeFluctuations", RedListEnums.YesNoNA.values());
                request.setAttribute("population_NrMatureEachSubpop", RedListEnums.NrMatureEachSubpop.values());
                request.setAttribute("population_PercentMatureOneSubpop", RedListEnums.PercentMatureOneSubpop.values());
                request.setAttribute("ecology_DeclineHabitatQuality", RedListEnums.DeclineHabitatQuality.values());
                request.setAttribute("usesAndTrade_Uses", RedListEnums.Uses.values());
                request.setAttribute("usesAndTrade_Overexploitation", RedListEnums.Overexploitation.values());
                request.setAttribute("threats_Threats", driver.getThreatEnum().values());
                request.setAttribute("threats_DeclineNrLocations", RedListEnums.DeclineNrLocations.values());
                request.setAttribute("threats_ExtremeFluctuationsNrLocations", RedListEnums.YesNoNA.values());
                request.setAttribute("conservation_ConservationPlans", RedListEnums.YesNoNA.values());
                request.setAttribute("conservation_ExSituConservation", RedListEnums.YesNoNA.values());
                request.setAttribute("conservation_ProposedConservationActions", RedListEnums.ProposedConservationActions.values());
                request.setAttribute("conservation_ProposedStudyMeasures", RedListEnums.ProposedStudyMeasures.values());
                request.setAttribute("assessment_Category", RedListEnums.RedListCategories.valuesNotUpDownListed());
                request.setAttribute("assessment_SubCategory", RedListEnums.CRTags.values());
                Map<String, List<RedListEnums.AssessmentCriteria>> criteria = new TreeMap<>();
                criteria.put("A", RedListEnums.AssessmentCriteria.getSubCriteriaOf("A"));
                criteria.put("B", RedListEnums.AssessmentCriteria.getSubCriteriaOf("B"));
                criteria.put("C", RedListEnums.AssessmentCriteria.getSubCriteriaOf("C"));
                criteria.put("D", RedListEnums.AssessmentCriteria.getSubCriteriaOf("D"));
                criteria.put("E", RedListEnums.AssessmentCriteria.getSubCriteriaOf("E"));
                request.setAttribute("assessment_Criteria", criteria);
                request.setAttribute("assessment_RegionalAssessment", RedListEnums.YesNoLikelyUnlikely.values());
                request.setAttribute("assessment_UpDownListing", RedListEnums.UpDownList.values());
                request.setAttribute("assessment_TextStatus", RedListEnums.TextStatus.values());
                request.setAttribute("assessment_AssessmentStatus", RedListEnums.AssessmentStatus.values());
                request.setAttribute("assessment_ReviewStatus", RedListEnums.ReviewStatus.values());
                request.setAttribute("assessment_PublicationStatus", RedListEnums.PublicationStatus.values());
                request.setAttribute("assessment_ValidationStatus", RedListEnums.ValidationStatus.values());

//                request.getRequestDispatcher("/main-redlistinfo.jsp").forward(request, response);

                // TODO this should be a user configuration loaded at startup
                PolygonTheme protectedAreas = new PolygonTheme(this.getClass().getResourceAsStream("SNAC.geojson"), "SITE_NAME");
                String[] ids = request.getParameterValues("id");
                if(ids == null || ids.length == 0) {
                    warnings.add("DataSheet.msg.warning.1a");
                    break;
                }
                if(ids.length == 1) {       // only one taxon requested
                    INodeKey thisId;
                    if(rldeSnap == null) {
                        thisId = thisRequest.getParameterAsKey("id");
                        rlde = driver.getRedListData().getRedListDataEntity(territory, thisId);
                        if (rlde == null) return;
                    } else {
                        request.setAttribute("snapshotid", rldeSnap.getID());
                        rlde = rldeSnap;
                        thisId = driver.asNodeKey(rlde.getTaxEntID());
                        rlde.setTaxEnt(driver.getNodeWorkerDriver().getDocument(thisId, TaxEnt.class));
                        request.setAttribute("versiondate", rldeSnap._getDateSavedFormatted());
                    }
                    request.setAttribute("snapshots", driver.getRedListData().getSnapshots(territory, driver.asNodeKey(rlde.getTaxEntID())));
                    request.setAttribute("taxon", rlde.getTaxEnt());
                    request.setAttribute("synonyms", driver.wrapTaxEnt(thisId).getSynonyms());
                    request.setAttribute("formerlyIncluded", driver.wrapTaxEnt(thisId).getFormerlyIncludedIn());
                    request.setAttribute("includedTaxa", driver.wrapTaxEnt(thisId).getIncludedTaxa());

                    // compile citations to make bibliography on the fly
                    BibliographyCompiler<RedListDataEntity, SafeHTMLString> bc =
                            new BibliographyCompiler<>(Collections.singletonList(rlde).iterator(), SafeHTMLString.class, driver);
                    //                    request.setAttribute("bibliography", driver.getNodeWorkerDriver().getDocuments(bc.getBibliography(), Reference.class));
                    bc.collectAllCitations();
                    request.setAttribute("bibliography", bc.getBibliography());
//                    bc.formatCitations();

                    List<SimpleOccurrenceDataProvider> sodps = null;
                    OccurrenceProcessor occurrenceProcessor, historicalOccurrenceProcessor, publicOccurrenceProcessor;
                    RedListSettings rls = driver.getRedListSettings(territory);
                    PolygonTheme clippingPolygon = rls.getClippingPolygon();

                    OccurrenceFilter historicalFilter = BasicOccurrenceFilter.RedListHistoricalMapFilter(driver, territory);
                    OccurrenceFilter currentFilter = BasicOccurrenceFilter.RedListCurrentMapFilter(driver, territory);

                    if(rldeSnap == null) {
                        // set privileges for this taxon

                        //rlde.getAssessment().getReviewStatus() == RedListEnums.ReviewStatus.REVISED_WORKING
                        Set<Privileges> ignorePrivileges = null;
                        // So, if text edition is locked, we only unlock when the user has the section 9 privilege
                        // Also, if the reviewer marked as revised, needs working, then don't lock.
                        if (rls.isEditionLocked(rlde) && !rls.isSheetUnlocked(rlde.getTaxEntID()) && !thisRequest.getUser().canEDIT_SECTION9()) {     // TODO user configuration
//                                && rlde.getAssessment().getReviewStatus() != RedListEnums.ReviewStatus.REVISED_WORKING) {
                            ignorePrivileges = Privileges.TextEditingPrivileges;
                        }   // && Arrays.asList(rlde.getTags()).contains("Diretiva")

                        thisRequest.getUser().setEffectivePrivilegesFor(driver, thisId, ignorePrivileges);

                        sodps = driver.getRedListData().getSimpleOccurrenceDataProviders();
                        for(SimpleOccurrenceDataProvider edp : sodps) {
                            try {
                                edp.executeOccurrenceQuery(rlde.getTaxEnt());
                            } catch (FloraOnException e) {
                                warnings.add(e.getMessage());
                            }
                        }

                        historicalOccurrenceProcessor = new OccurrenceProcessor(sodps, protectedAreas, sizeOfSquare, historicalFilter);
                        occurrenceProcessor = new OccurrenceProcessor(sodps, protectedAreas, sizeOfSquare, currentFilter);
                        publicOccurrenceProcessor = occurrenceProcessor;
                    } else { // we want it read-only
                        thisRequest.getUser().revokeAllPrivilegesExcept(new Privileges[]{MANAGE_VERSIONS});

                        historicalOccurrenceProcessor = OccurrenceProcessor.createFromOccurrences(
                                rldeSnap.getOccurrences(), protectedAreas, sizeOfSquare, historicalFilter);

                        occurrenceProcessor = OccurrenceProcessor.createFromOccurrences(
                                rldeSnap.getOccurrences(), protectedAreas, sizeOfSquare, currentFilter);

                        publicOccurrenceProcessor = occurrenceProcessor;
                    }

                    if(occurrenceProcessor.size() > 0 || historicalOccurrenceProcessor.size() > 0) {
                        // if it is published, AOO and EOO are from the data sheet, otherwise they are computed from
                        // live occurrences
                        Double EOO = null, AOO = null, hEOO = null, hAOO = null;
/*  no sense in locking these because the sheets can be frozen at any stage
                        if (rlde.getAssessment().getPublicationStatus() == RedListEnums.PublicationStatus.PUBLISHED) {
                            EOO = rlde.getGeographicalDistribution().getEOO();
                            AOO = rlde.getGeographicalDistribution().getAOO();
                            hEOO = rlde.getGeographicalDistribution().getHistoricalEOO();
                            hAOO = rlde.getGeographicalDistribution().getHistoricalAOO();
                        }
*/
                        if (EOO == null) EOO = occurrenceProcessor.getEOO();
                        if (AOO == null) AOO = occurrenceProcessor.getAOO();
                        if (hEOO == null) hEOO = historicalOccurrenceProcessor.getEOO();
                        if (hAOO == null) hAOO = historicalOccurrenceProcessor.getAOO();
                        request.setAttribute("EOO", EOO);
                        request.setAttribute("AOO", AOO);
                        request.setAttribute("hEOO", hEOO);
                        request.setAttribute("hAOO", hAOO);
                        request.setAttribute("realEOO", occurrenceProcessor.getRealEOO());
                        request.setAttribute("squareEOO", occurrenceProcessor.getSquareEOO());
                        request.setAttribute("sizeofsquare", sizeOfSquare / 1000);
                        request.setAttribute("nquads", occurrenceProcessor.getNQuads());
                        request.setAttribute("hnquads", historicalOccurrenceProcessor.getNQuads());
                        request.setAttribute("nclusters", occurrenceProcessor.getNLocations());

                        if((!AOO.equals(rlde.getGeographicalDistribution().getAOO())
                                || !EOO.equals(rlde.getGeographicalDistribution().getEOO()))
                                && thisRequest.getUser().canVIEW_OCCURRENCES())
                            warnings.add("DataSheet.msg.warning.AOOEOOnotuptodate");

                        Double[] lAreas = occurrenceProcessor.getLocationAreas();
                        double sum = 0;
                        double[] vals = ArrayUtils.toPrimitive(lAreas, -1);

                        for (int i = 0; i < lAreas.length; i++) {
                            sum += lAreas[i] / 10000;
                            vals[i] = vals[i] / 10000;
                        }
                        sum /= lAreas.length;
                        request.setAttribute("meanLocationArea", sum);

    /*
    HISTOGRAM!
                        HistogramDataset d = new HistogramDataset();
                        d.addSeries("Area", vals, 14, 0, sum * 0.5);
                        JFreeChart ch = ChartFactory.createHistogram("Area", "Sqrt Area", "N", d, PlotOrientation.VERTICAL, false, false, false);

                        XYPlot pl = (XYPlot) ch.getPlot();
                        pl.getRenderer().setSeriesPaint(0, Color.BLUE);
                        ((XYBarRenderer) pl.getRenderer()).setBarPainter(new StandardXYBarPainter());
                        ((XYBarRenderer) pl.getRenderer()).setDrawBarOutline(false);
                        ((XYBarRenderer) pl.getRenderer()).setMargin(0.25);
                        ch.setBackgroundPaint(null);

                        SVGGraphics2D g2 = new SVGGraphics2D(600, 400);
                        ch.draw(g2, new Rectangle(600,400));
                        String svgElement = g2.getSVGElement();
                        request.setAttribute("histogram", svgElement);
    */

/*
                        StringWriter sw = new StringWriter();
                        publicOccurrenceProcessor.exportSVG(new PrintWriter(sw), true
                                , thisRequest.getUser().canVIEW_FULL_SHEET(), true
                                , !thisRequest.getUser().canVIEW_FULL_SHEET()
                                , thisRequest.getUser().canVIEW_FULL_SHEET() ? 0 : 1, true
                                , thisRequest.getUser().canVIEW_FULL_SHEET()
                                , rls);
                        request.setAttribute("svgmap", sw.toString());
                        sw.close();
*/
                        StringWriter sw;
                        // these attributes are for the inline SVG maps
                        request.setAttribute("redListSettings", driver.getRedListSettings(territory));
                        request.setAttribute("currentSquares", publicOccurrenceProcessor.squares());
                        request.setAttribute("currentConvexHull", publicOccurrenceProcessor.getConvexHull());

                        if(historicalOccurrenceProcessor.getNQuads() > 0) {
                            sw = new StringWriter();
                            request.setAttribute("historicalSquares", historicalOccurrenceProcessor.squares());
                            request.setAttribute("historicalConvexHull", historicalOccurrenceProcessor.getConvexHull());
/*
                            historicalOccurrenceProcessor.exportSVG(new PrintWriter(sw), thisRequest.getUser().canVIEW_FULL_SHEET()
                                    , true, true, false, 0, true, true, rls);
                            request.setAttribute("historicalsvgmap", sw.toString());
*/
                            sw.close();
                        }

                        Set<String> groupAreasBy = new HashSet<>();
                        groupAreasBy.add("SITE_NAME");
                        groupAreasBy.add("TIPO");   // TODO this should be user configuration
                        request.setAttribute("occurrenceInProtectedAreas"
                                , occurrenceProcessor.getOccurrenceInProtectedAreas(groupAreasBy).entrySet());
                        request.setAttribute("locationsInPA", occurrenceProcessor.getNumberOfLocationsInsideProtectedAreas());
                        request.setAttribute("pointsOutsidePA", occurrenceProcessor.getNumberOfPointsOutsideProtectedAreas());
                        request.setAttribute("totalPoints", occurrenceProcessor.size());

                        request.setAttribute("occurrences", occurrenceProcessor);
                        request.setAttribute("historicalOccurrences", historicalOccurrenceProcessor);
                    }

                    if(sodps != null && rlde.getTaxEnt().getOldId() != null) {
                        // FIXME!!!! this only for Flora-On...
                        Map<String, Object> taxonInfo = sodps.get(0).executeInfoQuery(rlde.getTaxEnt().getOldId());

                        if (rlde.getEcology().getDescription() == null || rlde.getEcology().getDescription().isEmpty()) {
                            if (taxonInfo.containsKey("ecology") && taxonInfo.get("ecology") != null) {
                                request.setAttribute("ecology", new SafeHTMLString(taxonInfo.get("ecology").toString()));
                            }
                        } else {
                            request.setAttribute("ecology", rlde.getEcology().getDescription());
                        }

                        if (taxonInfo.containsKey("commonName"))
                            request.setAttribute("commonNames", taxonInfo.get("commonName"));

                        if (taxonInfo.containsKey("lifeform"))
                            request.setAttribute("lifeform", taxonInfo.get("lifeform"));
                    } else {
                        request.setAttribute("ecology", rlde.getEcology().getDescription());
                        request.setAttribute("lifeform","<erro>");
                    }

/*                    if(rlde.getTaxEnt() == null || rlde.getTaxEnt().getOldId() == null) {
//                        warnings.add("DataSheet.msg.warning.1b");
                        request.setAttribute("ecology", rlde.getEcology().getDescription());
                    }*/

                    request.setAttribute("rlde", rlde);
                    request.setAttribute("rls", rls);
                    request.setAttribute("identification", rlde.getIdentification());
                    // multiple selection fields
                    request.setAttribute("habitatTypes", driver.getNodeWorkerDriver().getDocuments(new HashSet<>(Arrays.asList(rlde.getEcology().getHabitatTypes())), Habitat.class));
                    request.setAttribute("habitatTypesIds", Arrays.asList(rlde.getEcology().getHabitatTypes()));
                    request.setAttribute("uses", Arrays.asList(rlde.getUsesAndTrade().getUses()));
                    request.setAttribute("proposedConservationActions", Arrays.asList(rlde.getConservation().getProposedConservationActions()));
                    request.setAttribute("proposedStudyMeasures", Arrays.asList(rlde.getConservation().getProposedStudyMeasures()));
                    request.setAttribute("threats", Arrays.asList(rlde.getThreats().getThreats()));
                    request.setAttribute("authors", Arrays.asList(cleanArray(rlde.getAssessment().getAuthors(), true)));
                    request.setAttribute("evaluator", Arrays.asList(cleanArray(rlde.getAssessment().getEvaluator(), true)));
                    request.setAttribute("reviewer", Arrays.asList(cleanArray(rlde.getAssessment().getReviewer(), true)));
                    request.setAttribute("selcriteria", Arrays.asList(rlde.getAssessment().getCriteria()));
                    request.setAttribute("allTags", driver.getRedListData().getRedListTags(territory));
                    request.setAttribute("tags", Arrays.asList(rlde.getTags()));
                    request.setAttribute("legalProtection", Arrays.asList(rlde.getConservation().getLegalProtection()));
                    List<PreviousAssessment> prev = rlde.getAssessment().getPreviousAssessmentList();
                    if (prev.size() > 2) {
                        prev = new ArrayList<>();
                        prev.add(rlde.getAssessment().getPreviousAssessmentList().get(0));
                        prev.add(rlde.getAssessment().getPreviousAssessmentList().get(1));
                    }
                    if (prev.size() == 0) prev = new ArrayList<>();
                    for (int i = prev.size(); i < 2; i++) {
                        prev.add(new PreviousAssessment());
                    }
                    request.setAttribute("previousAssessments", prev);
                    request.setAttribute("assessment_UpDownList", rlde.getAssessment().suggestUpDownList().getLabel());
                    request.setAttribute("citation", driver.getRedListData().buildRedListSheetCitation(rlde, userMap));

                    // compile edition history
                    Revision c1a;
                    Map<Revision, Integer> edits = new TreeMap<>(new Revision.RevisionComparator());
                    for (Revision r : rlde.getRevisions()) {
                        if(r.getUser() == null) continue;
                        c1a = r.getDayWiseRevision();
                        if (!edits.containsKey(c1a))
                            edits.put(c1a, 1);
                        else
                            edits.put(c1a, edits.get(c1a) + 1);
                    }
                    //                request.setAttribute("revisions", rlde.getRevisions());
                    request.setAttribute("revisions", edits.entrySet());
                    //                edits.entrySet().iterator().next().getValue()


                    // TODO: this is a temporary fix
                    if (rlde.getAssessment().getPublicationStatus().isPublished() && !thisRequest.getUser().canEDIT_9_9_4()
                            && !rls.isSheetUnlocked(thisId.getID())) {
                        // if it's published, block editing all fields except the unpublished
                        thisRequest.getUser().revokePrivileges(EDIT_ALL_FIELDS);
                    }

/*
                    if (rlde.getAssessment().getPublicationStatus().isPublished()) {
                        // if it's published, block editing all fields except the unpublished
                        boolean canEdit9 = thisRequest.getUser().canEDIT_9_9_4();
                        thisRequest.getUser().revokePrivileges(EDIT_ALL_FIELDS);
                        if (canEdit9) thisRequest.getUser().setEDIT_9_9_4(true);
                    }
*/
                    //System.out.println(new Gson().toJson(rlde));
                    warnings.addAll(rlde.validateCriteria());
                } else {    // multiple IDs provided, batch update
                    thisRequest.getUser().resetEffectivePrivileges();
                    warnings.add("DataSheet.msg.warning.1");
                    request.setAttribute("allTags", driver.getRedListData().getRedListTags(territory));
                    request.setAttribute("multipletaxa", true);
                    Iterator<TaxEnt> taxEnts = driver.getNodeWorkerDriver().getTaxEntByIds(request.getParameterValues("id"));
                    request.setAttribute("taxa", taxEnts);
                    List<PreviousAssessment> prev = new ArrayList<>();
                    for (int i = 0; i < 2; i++) {
                        prev.add(new PreviousAssessment());
                    }
                    request.setAttribute("previousAssessments", prev);
                }

                break;

            case "taxonrecords":
                thisRequest.getUser().setEffectivePrivilegesFor(driver, thisRequest.getParameterAsKey("id"), null);

                if (!thisRequest.getUser().canVIEW_OCCURRENCES()) break;
                te = driver.getNodeWorkerDriver().getTaxEntById(thisRequest.getParameterAsKey("id"));
                request.setAttribute("taxon", te);
                List<SimpleOccurrenceDataProvider> sodps = driver.getRedListData().getSimpleOccurrenceDataProviders();

                for(SimpleOccurrenceDataProvider edp : sodps)
                    edp.executeOccurrenceQuery(te);

                boolean viewAll = "all".equals(thisRequest.getParameterAsString("view"));

                OccurrenceFilter of = viewAll ?
                        BasicOccurrenceFilter.RedListCurrentMapFilter(driver, territory).withoutDateFilter()
                        : BasicOccurrenceFilter.RedListCurrentMapFilter(driver, territory);

                OccurrenceProcessor op = OccurrenceProcessor.iterableOf(sodps, of);

                request.setAttribute("occurrences", op);

                if(thisRequest.getParameterAsInteger("group", 0) > 0) {
                    OccurrenceProcessor op1 = OccurrenceProcessor.iterableOf(sodps, of);
                    SimpleOccurrenceClusterer clusters = new SimpleOccurrenceClusterer(op1.iterator()
                            , thisRequest.getParameterAsInteger("group", 0));

                    request.setAttribute("clustoccurrences", clusters.getClusteredOccurrences().asMap().entrySet());
//                clusters.getClusteredOccurrences().asMap().entrySet().iterator().next().getValue().iterator().next().getLocality()
                }
                break;

            case "downloadtaxawithtag":
                String tag = thisRequest.getParameterAsString("tag");
                iTaxEnt = driver.getRedListData().getAllRedListTaxa(territory, tag);
                thisRequest.response.setContentType("text/csv; charset=utf-8");
                thisRequest.response.addHeader("Content-Disposition", "attachment;Filename=\"taxonlist.csv\"");

                CSVPrinter wr4 = new CSVPrinter(thisRequest.response.getWriter(), CSVFormat.EXCEL);
                wr4.printRecord("ID", "Family", "Taxon");
                while(iTaxEnt.hasNext()) {
                    te = iTaxEnt.next();
                    TaxEnt family = driver.wrapTaxEnt(driver.asNodeKey(te.getID())).getParentOfRank(Constants.TaxonRanks.FAMILY);
                    wr4.print(te.getID());
                    wr4.print(family.getFullName());
                    wr4.print(te.getNameWithAnnotationOnly(false));
                    wr4.println();
                }
                wr4.close();
                break;

            // TODO move to API
            case "downloadtaxonrecords":
                thisRequest.getUser().setEffectivePrivilegesFor(driver, thisRequest.getParameterAsKey("id"), null);
                if (!thisRequest.getUser().canDOWNLOAD_OCCURRENCES() && !thisRequest.getUser().hasEDIT_ALL_1_8())
                    throw new FloraOnException("You don't have privileges for this operation");

                te = driver.getNodeWorkerDriver().getTaxEntById(thisRequest.getParameterAsKey("id"));
                List<SimpleOccurrenceDataProvider> sodps1 = driver.getRedListData().getSimpleOccurrenceDataProviders();

                for(SimpleOccurrenceDataProvider edp : sodps1)
                    edp.executeOccurrenceQuery(te);

                thisRequest.response.setContentType("application/vnd.google-earth.kml+xml; charset=utf-8");
                thisRequest.response.addHeader("Content-Disposition", "attachment;Filename=\"occurrences.kml\"");
                PrintWriter wr = thisRequest.response.getWriter();
                if(thisRequest.getUser().canDOWNLOAD_OCCURRENCES())
                    OccurrenceProcessor.iterableOf(sodps1, BasicOccurrenceFilter.create()).exportKML(wr);
                else {
                    OccurrenceProcessor.iterableOf(sodps1
                            , "all".equals(thisRequest.getParameterAsString("view"))
                                    ? BasicOccurrenceFilter.RedListCurrentMapFilter(driver, territory).withoutDateFilter()
                                    : BasicOccurrenceFilter.RedListCurrentMapFilter(driver, territory)
                    ).exportKML(wr);
                }
                wr.flush();
                return;

            // TODO move to API
            case "downloadtargetrecords":
//                if(!getUser().canDOWNLOAD_OCCURRENCES()) throw new FloraOnException("You don't have privileges for this operation");
                PolygonTheme clip = thisRequest.getUser()._getUserPolygonsAsTheme();
                if(clip == null || clip.size() == 0) break;
                List<TaxEnt> lt = new ArrayList<>();
                iTaxEnt = driver.getRedListData().getAllRedListTaxa(territory, "Prospecção");

                while(iTaxEnt.hasNext())
                    lt.add(iTaxEnt.next());

                List<SimpleOccurrenceDataProvider> sodps2 = driver.getRedListData().getSimpleOccurrenceDataProviders();
                for(SimpleOccurrenceDataProvider edp : sodps2)
                    edp.executeOccurrenceQuery(lt.iterator());

                thisRequest.response.setContentType("application/vnd.google-earth.kml+xml; charset=utf-8");
                thisRequest.response.addHeader("Content-Disposition", "attachment;Filename=\"occurrences.kml\"");
                PrintWriter wr1 = thisRequest.response.getWriter();
//                OccurrenceProcessor.iterableOf(driver.getRedListData().getSimpleOccurrenceDataProviders()).exportKML(wr1);
                OccurrenceProcessor.iterableOf(sodps2
                        , new BasicOccurrenceFilter(clip)).exportKML(wr1);
                wr1.flush();
                return;

            case "downloadsheet":
                RedListDataEntity rlde2 = driver.getRedListData().getRedListDataEntity(territory, thisRequest.getParameterAsKey("id"));
                if(rlde2 == null) {
                    thisRequest.error("Sheet not found.");
                    return;
                }
                thisRequest.response.setContentType("text/plain; charset=utf-8");
                thisRequest.response.addHeader("Content-Disposition", "attachment;Filename=\"" + rlde2.getTaxEnt()._getNameURLEncoded() + "-sheet.md\"");
//                ByteArrayOutputStream baos = new ByteArrayOutputStream();

                PrintWriter wr3 = thisRequest.response.getWriter();//new PrintWriter(baos);
                wr3.println("# " + rlde2.getTaxEnt().getFullName(false));
                wr3.println("## 1. Information");
                wr3.println("### 1.4. Notas taxonómicas");
                wr3.println(rlde2.getTaxonomicProblemDescription().toMarkDownString());
                wr3.println("## 2. Geographical Distribution");
                wr3.println("### 2.1. Distribuição");
                wr3.println(rlde2.getGeographicalDistribution().getDescription().toMarkDownString());
                wr3.println("### 2.2. EOO");
                wr3.println(rlde2.getGeographicalDistribution().getEOO());
                wr3.println("### 2.3. AOO");
                wr3.println(rlde2.getGeographicalDistribution().getAOO());
                wr3.println("### 2.4. Declínio da área de distribuição");
                wr3.println(" * " + rlde2.getGeographicalDistribution().getDeclineDistribution().getLabel());
                wr3.println(" * " + rlde2.getGeographicalDistribution().getDeclineDistributionJustification().toMarkDownString());
                wr3.println("### 2.5. Elevação");
                wr3.println(rlde2.getGeographicalDistribution().getElevationRange()[0] + " - " + rlde2.getGeographicalDistribution().getElevationRange()[1]);
                wr3.println("### 2.6. Flutuações extremas na distribuição");
                wr3.println(rlde2.getGeographicalDistribution().getExtremeFluctuations().getLabel());
                wr3.println("## 3. Population");
                wr3.println("### 3.1. Estado da população");
                wr3.println(rlde2.getPopulation().getDescription().toMarkDownString());
                wr3.println("### 3.2. Número de indivíduos maduros");
                wr3.println(rlde2.getPopulation().getNrMatureIndividualsCategory().getLabel());
                wr3.println("Número exacto: " + rlde2.getPopulation().getNrMatureIndividualsExact().toString());
                wr3.println("### 3.3. Tipo de estimativa");
                wr3.println(" * " + rlde2.getPopulation().getTypeOfEstimate().getLabel());
                wr3.println(" * " + rlde2.getPopulation().getNrMatureIndividualsDescription().toMarkDownString());
                wr3.println("### 3.4. Declínio populacional");
                wr3.println(" * " + rlde2.getPopulation().getPopulationDecline().getLabel());
                wr3.println(" * " + rlde2.getPopulation().getPopulationDeclinePercent());
                wr3.println(" * " + rlde2.getPopulation().getPopulationDeclineJustification().toMarkDownString());
                wr3.println("### 3.5. Redução populacional");
                for(RedListEnums.PopulationSizeReduction psr : rlde2.getPopulation().getPopulationSizeReduction())
                    wr3.println(" - " + psr.getLabel());
                wr3.println(" * " + rlde2.getPopulation().getPopulationTrend());
                wr3.println(" * " + rlde2.getPopulation().getPopulationSizeReductionJustification().toMarkDownString());
                wr3.println("### 3.6. Fragmentação severa");
                wr3.println(" * " + rlde2.getPopulation().getSeverelyFragmented().getLabel());
                wr3.println(" * " + rlde2.getPopulation().getSeverelyFragmentedJustification().toMarkDownString());
                wr3.println("### 3.7. Flutuações extremas no tamanho da população");
                wr3.println(" * " + rlde2.getPopulation().getExtremeFluctuations().getLabel());
                wr3.println(" * " + rlde2.getPopulation().getExtremeFluctuationsJustification().toMarkDownString());
                wr3.println("### 3.8. Número de indivíduos maduros em cada subpopulação");
                wr3.println(rlde2.getPopulation().getNrMatureEachSubpop().getLabel());
                wr3.println("### 3.9. Percentagem de indivíduos maduros numa única subpopulação");
                wr3.println(rlde2.getPopulation().getPercentMatureOneSubpop().getLabel());
                wr3.println("## 4. Ecology");
                wr3.println("### 4.1. Ecologia e biologia");
                wr3.println(rlde2.getEcology().getDescription().toMarkDownString());
                wr3.println("### 4.2. Tipos de habitat");
                for(String ht : rlde2.getEcology().getHabitatTypes()) {
                    wr3.println(" * " + driver.getNodeWorkerDriver().getHabitatById(driver.asNodeKey(ht)).getName());
                }
                wr3.println("### 4.4. Duração de uma geração");
                wr3.println(" * " + rlde2.getEcology().getGenerationLength());
                wr3.println(" * " + rlde2.getEcology().getGenerationLengthJustification().toMarkDownString());
                wr3.println("### 4.5. Declínio na qualidade do habitat");
                wr3.println(" * " + rlde2.getEcology().getDeclineHabitatQuality().getLabel());
                wr3.println(" * " + rlde2.getEcology().getDeclineHabitatQualityJustification().toMarkDownString());
                wr3.println("## 5. Uses and trade");
                wr3.println("### 5.1. Usos e comércio");
                wr3.println(rlde2.getUsesAndTrade().getDescription().toMarkDownString());
                wr3.println("### 5.2. Usos");
                for(RedListEnums.Uses u : rlde2.getUsesAndTrade().getUses())
                    wr3.println(" * " + FieldValues.getString(u.getLabel()));
                wr3.println("### 5.3. Comércio");
                wr3.println(rlde2.getUsesAndTrade().isTraded() ? "Traded" : "Not known");
                wr3.println("### 5.4. Sobre-exploração");
                wr3.println(rlde2.getUsesAndTrade().getOverexploitation().getLabel());
                wr3.println("## 6. Threats");
                wr3.println("### 6.1. Descrição de ameaças e pressões");
                wr3.println(rlde2.getThreats().getDescription().toMarkDownString());
                wr3.println("### 6.2. Ameaças");
                for(Threat t : rlde2.getThreats().getThreats())
                    wr3.println(" * " + FieldValues.getString(t.getLabel()));
                wr3.println("### 6.3. Número do localizações");
                wr3.println(" * " + rlde2.getThreats().getNumberOfLocations() + " localizações");
                wr3.println(" * " + rlde2.getThreats().getNumberOfLocationsJustification().toMarkDownString());
                wr3.println("### 6.4. Declínio continuado no número de localizações ou de subpopulações");
                wr3.println(" * " + FieldValues.getString(rlde2.getThreats().getDeclineNrLocations().getLabel()));
                wr3.println(" * " + rlde2.getThreats().getDeclineNrLocationsJustification().toMarkDownString());
                wr3.println("### 6.5. Flutuações extremas no número de localizações ou subpopulações");
                wr3.println(" * " + rlde2.getThreats().getExtremeFluctuationsNrLocations().getLabel());
                wr3.println(" * " + rlde2.getThreats().getExtremeFluctuationsNrLocationsJustification().toMarkDownString());
                wr3.println("## 7. Conservation");
                wr3.println("### 7.1. Medidas de conservação");
                wr3.println(rlde2.getConservation().getDescription().toMarkDownString());
                wr3.println("### 7.2. Existência de planos de gestão/conservação");
                wr3.println(" * " + rlde2.getConservation().getConservationPlans().getLabel());
                wr3.println(" * " + rlde2.getConservation().getConservationPlansJustification().toMarkDownString());
                wr3.println("### 7.3. Conservação *ex-situ*");
                wr3.println(" * " + rlde2.getConservation().getExSituConservation().getLabel());
                wr3.println(" * " + rlde2.getConservation().getExSituConservationJustification().toMarkDownString());
                wr3.println("### 7.5. Proposta de ações de conservação");
                for(RedListEnums.ProposedConservationActions pc : rlde2.getConservation().getProposedConservationActions())
                    wr3.println(" * " + FieldValues.getString(pc.getLabel()));
                wr3.println("### 7.6. Proposta de estudos");
                for(RedListEnums.ProposedStudyMeasures pc : rlde2.getConservation().getProposedStudyMeasures())
                    wr3.println(" * " + FieldValues.getString(pc.getLabel()));

                wr3.println("## 9. Assessment");
                wr3.println("### 9.1. Categoria");
                wr3.println(" * " + (rlde2.getAssessment().getCategory() == null ? "sem categoria atribuída" : rlde2.getAssessment().getCategory().getLabel()));
                wr3.println("### 9.2. Critérios");
                wr3.println(" * " + rlde2.getAssessment()._getCriteriaAsString());
                wr3.println("### 9.3. Justificação");
                wr3.println(rlde2.getAssessment().getJustification().toMarkDownString());
                wr3.println("### 9.4. Análise regional");
                wr3.println("#### 9.4.1. A população regional beneficia de qualquer imigração significativa de propágulos que se possam reproduzir na região?");
                wr3.println(rlde2.getAssessment().getPropaguleImmigration().getLabel());
                wr3.println("#### 9.4.2. É expectável um decréscimo da imigração?");
                wr3.println(rlde2.getAssessment().getDecreaseImmigration().getLabel());
                wr3.println("#### 9.4.3. A população regional é um sumidouro?");
                wr3.println(rlde2.getAssessment().getIsSink().getLabel());
                wr3.println("#### 9.4.4. Subida ou descida de categoria");
                wr3.println(rlde2.getAssessment().getUpDownListing().getLabel());
                wr3.println("#### 9.4.5. Justificação de subida ou descida de categoria");
                wr3.println(rlde2.getAssessment().getUpDownListingJustification().toMarkDownString());
                wr3.println("#### 9.4.6. Avaliação final");
                wr3.println(rlde2.getAssessment().getFinalJustification().toMarkDownString());
                wr3.println("### 9.5. Avaliações anteriores");
                for(PreviousAssessment pa : rlde2.getAssessment().getPreviousAssessmentList())
                    if(pa != null && pa.getCategory() != null) wr3.println(" * " + pa.getYear() + ": " + pa.getCategory().getLabel());
                wr3.println("### 9.6. Autoria dos textos");
                for(String id : rlde2.getAssessment().getAuthors())
                    wr3.println(" - " + userMap.get(id));
                wr3.println("### 9.6.1. Contribuidores");
                wr3.println(" * " + rlde2.getAssessment().getCollaborators());
                wr3.println("### 9.7. Avaliadores");
                for(String id : rlde2.getAssessment().getEvaluator())
                    wr3.println(" - " + userMap.get(id));
                wr3.println("### 9.8. Revisores");
                for(String id : rlde2.getAssessment().getReviewer())
                    wr3.println(" - " + userMap.get(id));
                wr3.println("### 10.1. Comentários do revisor");
                wr3.println(rlde2.getReviewerComments().toMarkDownString());
                wr3.flush();
                return;

            case "contentasxml":
                rldeIt = driver.getRedListData().getAllRedListData(territory == null ? driver.getDefaultRedListTerritory() : territory, false, null);
                PrintWriter wr2 = thisRequest.response.getWriter();
                DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder docBuilder;
                try {
                    docBuilder = docFactory.newDocumentBuilder();
                } catch (ParserConfigurationException e) {
                    throw new FloraOnException(e.getMessage());
                }

                Document doc = docBuilder.newDocument();
                Element rootElement;

                if("counters".equals(thisRequest.getParameterAsString("what"))) {
                    rootElement = doc.createElement("counters");
                    doc.appendChild(rootElement);

                    Integer count1 = 0, count2 = 0, count3 = 0, count4 = 0;
                    Integer countCR = 0, countEN = 0, countVU = 0, countNT = 0, countLC = 0, countRE = 0, countEX = 0, countEW = 0, countDD = 0;
                    while (rldeIt.hasNext()) {
                        RedListDataEntity rlde1 = rldeIt.next();
//                        if (Collections.disjoint(Collections.singleton("Diretiva"), Arrays.asList(rlde1.getTags()))) continue;

                        if(rlde1.getAssessment().getFinalCategory() != RedListEnums.RedListCategories.NA) {
                            if (rlde1.getAssessment().getAssessmentStatus().isAssessed()) count1++;
                            if (rlde1.getAssessment().getReviewStatus() == RedListEnums.ReviewStatus.REVISED_PUBLISHING)
                                count2++;
                            if (rlde1.getAssessment().getPublicationStatus().isPublished() || rlde1.getAssessment().getPublicationStatus().isApproved())
                                count3++;
                        }
                        if (rlde1.getAssessment().getAssessmentStatus().isAssessed() && rlde1.getAssessment().getCategory() != null) {
                            RedListEnums.RedListCategories cat = rlde1.getAssessment().getFinalCategory();
                            if(cat.isThreatened()) count4++;
                            if(cat.getEffectiveCategory() == RedListEnums.RedListCategories.CR) countCR++;
                            if(cat.getEffectiveCategory() == RedListEnums.RedListCategories.EN) countEN++;
                            if(cat.getEffectiveCategory() == RedListEnums.RedListCategories.VU) countVU++;
                            if(cat.getEffectiveCategory() == RedListEnums.RedListCategories.NT) countNT++;
                            if(cat.getEffectiveCategory() == RedListEnums.RedListCategories.LC) countLC++;
                            if(cat.getEffectiveCategory() == RedListEnums.RedListCategories.RE) countRE++;
                            if(cat.getEffectiveCategory() == RedListEnums.RedListCategories.EX) countEX++;
                            if(cat.getEffectiveCategory() == RedListEnums.RedListCategories.EW) countEW++;
                            if(cat.getEffectiveCategory() == RedListEnums.RedListCategories.DD) countDD++;
                        }
                    }
                    Element nrAssessed = doc.createElement("numberAssessed");
                    Element nrRevised = doc.createElement("numberRevised");
                    Element nrPublished = doc.createElement("numberPublished");
                    Element nrThreatened = doc.createElement("numberThreatened");

                    nrAssessed.appendChild(doc.createTextNode(count1.toString()));
                    nrRevised.appendChild(doc.createTextNode(count2.toString()));
                    nrPublished.appendChild(doc.createTextNode(count3.toString()));
                    nrThreatened.appendChild(doc.createTextNode(count1.equals(0) ? "?" : count4.toString()));
//                    nrThreatened.appendChild(doc.createTextNode("381"));

                    Element nrPerCategory = doc.createElement("numberPerCategory");
                    Element nrCR = doc.createElement("CR");
                    Element nrEN = doc.createElement("EN");
                    Element nrVU = doc.createElement("VU");
                    Element nrNT = doc.createElement("NT");
                    Element nrLC = doc.createElement("LC");
                    Element nrRE = doc.createElement("RE");
                    Element nrEX = doc.createElement("EX");
                    Element nrEW = doc.createElement("EW");
                    Element nrDD = doc.createElement("DD");

                    nrPerCategory.appendChild(nrCR);
                    nrPerCategory.appendChild(nrEN);
                    nrPerCategory.appendChild(nrVU);
                    nrPerCategory.appendChild(nrNT);
                    nrPerCategory.appendChild(nrLC);
                    nrPerCategory.appendChild(nrRE);
                    nrPerCategory.appendChild(nrEX);
                    nrPerCategory.appendChild(nrEW);
                    nrPerCategory.appendChild(nrDD);

                    nrCR.appendChild(doc.createTextNode(countCR.toString()));
                    nrEN.appendChild(doc.createTextNode(countEN.toString()));
                    nrVU.appendChild(doc.createTextNode(countVU.toString()));
                    nrNT.appendChild(doc.createTextNode(countNT.toString()));
                    nrLC.appendChild(doc.createTextNode(countLC.toString()));
                    nrRE.appendChild(doc.createTextNode(countRE.toString()));
                    nrEX.appendChild(doc.createTextNode(countEX.toString()));
                    nrEW.appendChild(doc.createTextNode(countEW.toString()));
                    nrDD.appendChild(doc.createTextNode(countDD.toString()));

                    rootElement.appendChild(nrAssessed);
                    rootElement.appendChild(nrRevised);
                    rootElement.appendChild(nrPublished);
                    rootElement.appendChild(nrThreatened);
                    rootElement.appendChild(nrPerCategory);
                } else {
                    thisRequest.setCacheHeaders(60 * 24);
                    File dir = new File(getServletContext().getRealPath("/")).getParentFile();
                    Properties properties = new Properties();
                    InputStream propStream;
                    Locale.setDefault(Locale.forLanguageTag("pt"));
                    try {
                        propStream = new FileInputStream(new File(dir.getAbsolutePath() + "/floraon.properties"));
                        properties.load(propStream);
                    } catch (IOException e) {
                        e.printStackTrace();
                        System.err.println("ERROR: " + e.getMessage());
                        return;
                    }

                    URL floraOnURL = null;
                    for (String op1 : BaseFloraOnDriver.getPropertyList(properties, "occurrenceProvider")) {
                        floraOnURL = new URL(op1);
                    }
                    URI oldUri = null;
                    if (floraOnURL != null) {
                        try {
                            oldUri = floraOnURL.toURI();
                        } catch (URISyntaxException e) {
                            e.printStackTrace();
                            throw new FloraOnException(e.getMessage());
                        }
                    }

                    rootElement = doc.createElement("redlistcontent");
                    doc.appendChild(rootElement);

                    while (rldeIt.hasNext()) {
                        rlde = rldeIt.next();
/*
                        if (Collections.disjoint(Collections.singleton("Lista Alvo"), Arrays.asList(rlde.getTags()))
                                || rlde.getGeographicalDistribution().getDescription().isEmpty()) continue;
*/

/*
                        if (Collections.disjoint(Collections.singleton("Diretiva"), Arrays.asList(rlde.getTags()))
                                || (!rlde.getAssessment().getPublicationStatus().isPublished()
                                && !rlde.getAssessment().getPublicationStatus().isApproved())) continue;
*/
                        if ((!rlde.getAssessment().getPublicationStatus().isPublished()
                                && !rlde.getAssessment().getPublicationStatus().isApproved())) continue;

                        Element species = doc.createElement("species");

                        Element name = doc.createElement("name");
                        name.appendChild(doc.createTextNode(rlde.getTaxEnt().getCanonicalName().toString(true)));

                        Element commonname = doc.createElement("commonName");
                        commonname.appendChild(doc.createTextNode(" "));

                        Element genus = doc.createElement("genus");
                        genus.appendChild(doc.createTextNode("<i>" + rlde.getTaxEnt().getCanonicalName().getGenus() + "</i>"));

                        Element family = doc.createElement("family");
                        family.appendChild(doc.createTextNode(driver.wrapTaxEnt(driver.asNodeKey(rlde.getTaxEntID())).getParentOfRank(Constants.TaxonRanks.FAMILY).getName()));

                        Element author = doc.createElement("author");
                        author.appendChild(doc.createTextNode(rlde.getTaxEnt().getAuthor()));

                        Element fullname = doc.createElement("fullname");
                        fullname.appendChild(doc.createTextNode(rlde.getTaxEnt().getFullName(true)));

                        Element distr = doc.createElement("distribution");
                        distr.appendChild(doc.createTextNode(""));//rlde.getGeographicalDistribution().getDescription().toSimpleHTML()));

                        Element pop = doc.createElement("population");
                        pop.appendChild(doc.createTextNode(""));//rlde.getPopulation().getDescription().toSimpleHTML()));

                        Element ecology = doc.createElement("ecology");
                        ecology.appendChild(doc.createTextNode(""));//rlde.getEcology().getDescription().toSimpleHTML()));

                        Element uses = doc.createElement("uses");
                        uses.appendChild(doc.createTextNode(""));//rlde.getUsesAndTrade().getDescription().toSimpleHTML()));

                        Element threats = doc.createElement("threats");
                        threats.appendChild(doc.createTextNode(""));//rlde.getThreats().getDescription().toSimpleHTML()));

                        Element conservation = doc.createElement("conservation");
                        conservation.appendChild(doc.createTextNode(""));//rlde.getConservation().getDescription().toSimpleHTML()));

                        Element assessjust = doc.createElement("assessmentJustification");
                        if(rlde.getAssessment().getPublicationStatus() == RedListEnums.PublicationStatus.PUBLISHED_DRAFT)
                            assessjust.appendChild(doc.createTextNode("(em revisão)"));
                        else
                            assessjust.appendChild(doc.createTextNode(rlde.getAssessment().getFinalJustification().toSimpleHTML()));

                        Element assesscrit = doc.createElement("assessmentCriteria");
                        assesscrit.appendChild(doc.createTextNode(rlde.getAssessment()._getCriteriaAsString()));

                        Element assesscat = doc.createElement("assessmentCategory");
                        assesscat.appendChild(doc.createTextNode(rlde.getAssessment()._getCategoryAsString()));

                        Element effectiveCategory = doc.createElement("effectiveCategory");
                        if(rlde.getAssessment().getFinalCategory() != null)
                            effectiveCategory.appendChild(doc.createTextNode(rlde.getAssessment().getFinalCategory().getEffectiveCategory().getShortTag()
                                    + ", " + rlde.getAssessment().getFinalCategory().getEffectiveCategory().getLabel()));

                        Element isThreatened = doc.createElement("isThreatened");
                        isThreatened.appendChild(doc.createTextNode(rlde.getAssessment().getFinalCategory() == null ? ""
                                        : (rlde.getAssessment().getFinalCategory().isThreatened() ? "Ameaçadas" : "")));

                        Element assesscatcrit = doc.createElement("assessmentCategoryAndCriteria");
                        assesscatcrit.appendChild(doc.createTextNode((rlde.getAssessment()._getCategoryAsString()
                                + " " + rlde.getAssessment()._getCriteriaAsString()).trim()));

                        Element assesscatverb = doc.createElement("assessmentCategoryVerbose");
                        assesscatverb.appendChild(doc.createTextNode(rlde.getAssessment()._getCategoryVerboseAsString(true)));

                        Element fullassesscat = doc.createElement("fullAssessmentCategory");
                        if(rlde.getAssessment().getFinalCategory() != null)
                            fullassesscat.appendChild(doc.createTextNode(rlde.getAssessment()._getCategoryAsString() + ", " + rlde.getAssessment()._getCategoryVerboseAsString(true)));

                        BibliographyCompiler<RedListDataEntity, SafeHTMLString> bc1 = new BibliographyCompiler<>(Collections.singletonList(rlde).iterator(), SafeHTMLString.class, driver);
                        bc1.collectAllCitations();
                        Element bibliography = doc.createElement("bibliography");
                        StringBuilder sb4 = new StringBuilder("<ul>");
                        for (Reference reference : bc1.getBibliography()) {
                            sb4.append("<li>").append(reference._getBibliographyEntry()).append("</li>");
                        }
                        sb4.append("</ul>");
                        bibliography.appendChild(doc.createTextNode(sb4.toString()));

                        Element citation = doc.createElement("citation");
                        citation.appendChild(doc.createTextNode(driver.getRedListData().buildRedListSheetCitation(rlde, userMap)));

                        Element map = doc.createElement("svgMap");

/*
                    map.appendChild(doc.createTextNode("https://lvf.flora-on.pt/api/svgmap?basemap=0&size=10000&taxon=" +
                        rlde.getTaxEnt()._getIDURLEncoded()));
*/
                        CanonicalName cn = rlde.getTaxEnt().getCanonicalName();
                        map.appendChild(doc.createTextNode("https://lvf.flora-on.pt/api/svgmap/" +
                                cn.getGenus() + "_" + cn.getSpecificEpithet() + "_" + driver.asNodeKey(rlde.getTaxEnt().getID()).getDBKey() + ".svg"));

                        Element headerphoto = doc.createElement("headerphoto");
                        Element photos = doc.createElement("photos");
                        if (oldUri != null) {
                            String newQuery = oldUri.getQuery();
                            if (newQuery == null) {
                                newQuery = "what=photos&id=" + rlde.getTaxEnt().getOldId();
                            } else {
                                newQuery += "&what=photos&id=" + rlde.getTaxEnt().getOldId();
                            }

                            URI newUri;
                            URL u;
                            try {
                                newUri = new URI(oldUri.getScheme(), oldUri.getAuthority(), oldUri.getPath(), newQuery, oldUri.getFragment());
                                u = newUri.toURL();
                                InputStreamReader isr = new InputStreamReader(u.openStream());
                                JsonObject resp = new JsonParser().parse(isr).getAsJsonObject();
                                if (resp.getAsJsonPrimitive("success").getAsBoolean()) {
                                    Type listType = new TypeToken<List<Map<String, Object>>>() {
                                    }.getType();
                                    List<Map<String, Object>> occArray;

                                    occArray = new Gson().fromJson(resp.getAsJsonArray("msg"), listType);
                                    for (Map<String, Object> ph : occArray) {
                                        Element photo = doc.createElement("photo");
                                        photo.appendChild(doc.createTextNode("https://flora-on.pt/" + cn.getGenus() + "-"
                                                + cn.getSpecificEpithet() + "_ori_" + ph.get("guid").toString() + ".jpg"));
                                        photos.appendChild(photo);

                                        if(!headerphoto.hasChildNodes()) {
                                            if(StringUtils.isStringEmpty(rlde.getCoverPhotoUrl()))
                                                headerphoto.appendChild(doc.createTextNode("https://flora-on.pt/" + cn.getGenus() + "-"
                                                        + cn.getSpecificEpithet() + "_ori_" + ph.get("guid").toString() + ".jpg"));
                                            else
                                                headerphoto.appendChild(doc.createTextNode(rlde.getCoverPhotoUrl()));
                                        }
                                    }
                                }
                            } catch (URISyntaxException e) {
                                e.printStackTrace();
                            }

                        }

                        species.appendChild(name);
                        species.appendChild(commonname);
                        species.appendChild(genus);
                        species.appendChild(family);
                        species.appendChild(author);
                        species.appendChild(fullname);
                        species.appendChild(distr);
                        species.appendChild(pop);
                        species.appendChild(ecology);
                        species.appendChild(uses);
                        species.appendChild(threats);
                        species.appendChild(conservation);
                        species.appendChild(assessjust);
                        species.appendChild(assesscat);
                        species.appendChild(effectiveCategory);
                        species.appendChild(isThreatened);
                        species.appendChild(assesscatverb);
                        species.appendChild(fullassesscat);
                        species.appendChild(assesscrit);
                        species.appendChild(assesscatcrit);
                        species.appendChild(citation);
                        species.appendChild(bibliography);
                        species.appendChild(map);
                        species.appendChild(headerphoto);
                        species.appendChild(photos);
                        rootElement.appendChild(species);
                    }
                }

                thisRequest.response.setContentType("application/xml; charset=utf-8");
                // write the content into xml file
                TransformerFactory transformerFactory = TransformerFactory.newInstance();
                Transformer transformer;
                try {
                    transformer = transformerFactory.newTransformer();
                    DOMSource source = new DOMSource(doc);
                    StreamResult result = new StreamResult(wr2);
                    transformer.transform(source, result);
                } catch (TransformerException e) {
                    throw new FloraOnException(e.getMessage());
                }
                wr2.flush();
                return;

            case "users":
                boolean viewAllUsers = thisRequest.getParameterAsBoolean("viewall", false);
                List<User> allusers = driver.getAdministration().getAllUsers(!viewAllUsers);
                if(!viewAllUsers) {
                    Map<String, String> taxonMap1 = new HashMap<>();

                    Map<String, Integer> responsibleTextCounter = new HashMap<>();
                    Map<String, Integer> responsibleAssessmentCounter = new HashMap<>();
                    Map<String, Integer> responsibleRevisionCounter = new HashMap<>();

                    Iterator<AtomicTaxonPrivilege> tps = driver.getRedListData().getTaxonPrivilegesForAllUsers(territory);
                    AtomicTaxonPrivilege atp;
                    while (tps.hasNext()) {
                        atp = tps.next();
                        if (atp.isResponsibleForTexts()) {
                            if (responsibleTextCounter.get(atp.getUserId()) == null)
                                responsibleTextCounter.put(atp.getUserId(), 1);
                            else
                                responsibleTextCounter.put(atp.getUserId(), responsibleTextCounter.get(atp.getUserId()) + 1);
                        }

                        if (atp.isResponsibleForAssessment()) {
                            if (responsibleAssessmentCounter.get(atp.getUserId()) == null)
                                responsibleAssessmentCounter.put(atp.getUserId(), 1);
                            else
                                responsibleAssessmentCounter.put(atp.getUserId(), responsibleAssessmentCounter.get(atp.getUserId()) + 1);
                        }

                        if (atp.isResponsibleForRevision()) {
                            if (responsibleRevisionCounter.get(atp.getUserId()) == null)
                                responsibleRevisionCounter.put(atp.getUserId(), 1);
                            else
                                responsibleRevisionCounter.put(atp.getUserId(), responsibleRevisionCounter.get(atp.getUserId()) + 1);
                        }
                    }

                    // make a map with all taxon names used in privileges
                    for (User tmp : allusers) {
                        for (TaxonPrivileges tp : tmp.getTaxonPrivileges()) {
                            Iterator<TaxEnt> teIt1 = driver.getNodeWorkerDriver().getTaxEntByIds(tp.getApplicableTaxa());
                            TaxEnt te1;
                            while(teIt1.hasNext()) {
                                te1 = teIt1.next();
                                taxonMap1.put(te1.getID(), te1.getName());
                            }
                        }
                    }
                    request.setAttribute("taxonMap", taxonMap1);
                    request.setAttribute("responsibleTextCounter", responsibleTextCounter);
                    request.setAttribute("responsibleAssessmentCounter", responsibleAssessmentCounter);
                    request.setAttribute("responsibleRevisionCounter", responsibleRevisionCounter);
                }
                request.setAttribute("users", allusers);
                request.setAttribute("redlistprivileges", Privileges.getAllPrivilegesOfTypeAndScope(
                        thisRequest.getUser().getUserType() == User.UserType.ADMINISTRATOR ? null : Privileges.PrivilegeType.REDLISTDATA
                        , null));
                break;

            case "edituser":
                User tmp = driver.getAdministration().getUser(thisRequest.getParameterAsKey("user"));
//                List<TaxEnt> applTax = new ArrayList<>();
                if(tmp == null) {
                    thisRequest.error("User not found. Check user ID.");
                    return;
                }
/*
                for(String i : tmp.getApplicableTaxa()) {
                    applTax.add(driver.getNodeWorkerDriver().getTaxEntById(driver.asNodeKey(i)));
                }
                request.setAttribute("applicableTaxa", applTax);
*/
                Map<String, String> taxonMap = new HashMap<>();
                for(TaxonPrivileges tp : tmp.getTaxonPrivileges()) {
                    Iterator<TaxEnt> teIt2 = driver.getNodeWorkerDriver().getTaxEntByIds(tp.getApplicableTaxa());
                    TaxEnt te2;
                    while(teIt2.hasNext()) {
                        te2 = teIt2.next();
                        taxonMap.put(te2.getID(), te2.getName());
                    }
                }
                request.setAttribute("taxonMap", taxonMap);
                request.setAttribute("tsprivileges", tmp.getTaxonPrivileges());
                request.setAttribute("requesteduser", tmp);
                if(tmp._getUserPolygonsAsTheme() != null) {
                    request.setAttribute("userPolygon", tmp._getUserPolygonsAsTheme().iterator());
/*
                    Arrays.toString()
                    tmp._getUserPolygonsAsTheme().iterator().next().getValue().getProperties().values().toArray()
*/
                }
                request.setAttribute("redlistprivileges", Privileges.getAllPrivilegesOfTypeAndScope(
                        thisRequest.getUser().getUserType() == User.UserType.ADMINISTRATOR ? null : Privileges.PrivilegeType.REDLISTDATA
                        , null));
                request.setAttribute("redlisttaxonprivileges", Privileges.getAllPrivilegesOfTypeAndScope(
                        thisRequest.getUser().getUserType() == User.UserType.ADMINISTRATOR ? null : Privileges.PrivilegeType.REDLISTDATA
                        , Privileges.PrivilegeScope.PER_SPECIES));
                thisRequest.request.setAttribute("context", "redlist");
                break;

            case "jobs":
                if (!thisRequest.getUser().canDOWNLOAD_OCCURRENCES()) break;
                String[] allt = driver.getRedListData().getRedListTags(territory).toArray(new String[0]);

                List<JobRunner> jobs = new ArrayList<>();
                for(String jobID : JobSubmitter.getJobList()) {
                    if(JobSubmitter.getJob(jobID).getOwner().equals(thisRequest.getUser()) || thisRequest.getUser().isAdministrator())
                        jobs.add(JobSubmitter.getJob(jobID));
                }
                request.setAttribute("jobs", jobs);
                request.setAttribute("allTags", allt);
                break;

            case "allmaps":
                iTaxEnt = driver.getRedListData().getAllRedListTaxa(territory, "Lista Alvo");
/*
                int count = 0;
                while(count<600) {
                    count++;
                    it2.next();
                }
*/
                request.setAttribute("allTaxa", iTaxEnt);
                break;

            case "replacetools":
                thisRequest.request.setAttribute("jobs", JobSubmitter.getJobsOfType(SearchAndReplaceDryJob.class));

/*
                for(JobRunner job : JobSubmitter.getJobsOfType(SearchAndReplaceDryJob.class)) {
                    if(job.isReady()) {

                        job.getDateSubmitted()
//                        BufferedReader resultsReader = new BufferedReader(new FileReader(((SearchAndReplaceDryJob) job.getJob()).getResults()));
//                        Files.readLines(((SearchAndReplaceDryJob) job.getJob()).getResults(), StandardCharsets.UTF_8);
                        byte[] encoded = java.nio.file.Files.readAllBytes(Paths.get(((SearchAndReplaceDryJob) job.getJob()).getResults().getAbsolutePath()));
                        thisRequest.request.setAttribute("aaa", new String(encoded, StandardCharsets.UTF_8));
                    }
                }
*/
                break;

            case "report":
                DateFormat df = Constants.dateFormat.get();
                Date from = null, to = null;
                if("technical".equals(thisRequest.getParameterAsString("type"))) {
                    // Technical report
                    try {
                        from = DateParser.parseDateAsDate(thisRequest.getParameterAsString("fromdate"));
                        to = DateParser.parseDateAsDate(thisRequest.getParameterAsString("todate"));
                    } catch (IllegalArgumentException e) {
                        warnings.add(e.getMessage());
                        break;
                    }
                    if (from == null || to == null) {
                        Calendar now = new GregorianCalendar();
                        now.set(Calendar.DAY_OF_MONTH, 1);
                        now.add(Calendar.MONTH, -1);
                        from = now.getTime();
                        Calendar after = new GregorianCalendar();
                        after.set(Calendar.DAY_OF_MONTH, 1);
                        after.add(Calendar.DAY_OF_MONTH, -1);
                        to = after.getTime();
                    }
                    request.setAttribute("fromDate", df.format(from));
                    request.setAttribute("toDate", df.format(to));
                }

                if("geo".equals(thisRequest.getParameterAsString("type"))) {
                    // download records inside polygon
                    // NOTE: this only uses the records in the internal database!
                    thisRequest.ensurePrivilege(DOWNLOAD_OCCURRENCES);
                    String polygonWKT = thisRequest.getParameterAsString("polygon");
//                    Polygon ((-7.61676305104465801 37.9335618844394773, -7.6221485973442924 37.93207466244877679, -7.62282460734006229 37.92853687680425168, -7.61852067703365954 37.92567510115549112, -7.61444208339251372 37.92497655749319563, -7.61288726040224262 37.92871714613645651, -7.61270699107003779 37.93049730579198098, -7.61462235272471943 37.93252533577929597, -7.61676305104465801 37.9335618844394773))
//Polygon ((-7.96382658287298995 38.43561197463136381, -8.22341442124868394 38.18756137351681446, -8.20755072001461272 38.01738712391497188, -8.05612448096212574 37.92797353514112046, -7.8845080767026392 37.91932060719526731, -7.810958189162859 38.04623021706782993, -7.81672680779342954 38.20054076543559773, -7.96382658287298995 38.43561197463136381))
                    if(polygonWKT != null) {
                        Iterator<Inventory> it = null;
                        Inventory inv;
//                        Set<TaxEnt> speciesList = new TreeSet<>();
                        Map<TaxEnt, Map<String, Object>> speciesList = new TreeMap<>();
                        MutableInt nInventories = new MutableInt(0);
                        MutableInt nEndemic = new MutableInt(0);
                        MutableInt nThreatened = new MutableInt(0);
                        try {
                            it = driver.getQueryDriver().findInventoriesContainedIn(polygonWKT, null);
                            while(it.hasNext()) {
                                inv = it.next();
                                nInventories.increment();
                                for(OBSERVED_IN o : inv._getTaxa()) {
                                    if(o.getTaxEnt() != null && !speciesList.containsKey(o.getTaxEnt())) {
                                        speciesList.put(o.getTaxEnt(), new HashMap<String, Object>());
                                    }
                                }
                            }

                            for(Map.Entry<TaxEnt, Map<String, Object>> en : speciesList.entrySet()) {
                                INodeKey tKey = driver.asNodeKey(en.getKey().getID());
                                InferredStatus is = driver.wrapTaxEnt(tKey).getInferredNativeStatus(territory);
                                boolean endemic = is != null && is.isEndemic();

                                rlde = driver.getRedListData().getRedListDataEntity(territory, tKey);
                                if(rlde != null && rlde.getAssessment().getFinalCategory() != null) {
                                    if(rlde.getAssessment().getFinalCategory().isThreatened())
                                        nThreatened.increment();

                                    en.getValue().put("category", rlde.getAssessment().getFinalCategory().getLabel());
                                }


                                if (endemic) {
                                    en.getValue().put("endemic", true);
                                    nEndemic.increment();
                                }
                            }
                        } catch (IllegalArgumentException e) {
                            request.setAttribute("message", e.getMessage());
                        } finally {
                            request.setAttribute("nInventories", nInventories.intValue());
                            request.setAttribute("nSpecies", speciesList.size());
                            request.setAttribute("nEndemic", nEndemic.intValue());
                            request.setAttribute("nThreatened", nThreatened.intValue());
                            request.setAttribute("speciesList", speciesList);
                        }

                    }
                }
                break;

            case "stats":
                if(!thisRequest.getUser().canVIEW_OCCURRENCES()) break;
                String filtertag = "Lista Alvo";
                Map<String, MutableInt> statsFinalized = new HashMap<>();
                Map<String, MutableInt> statsTag = new HashMap<>();
                String[] statfields = new String[]{"nrWithResponsible", "nrTextsInProgress", "nrTextsAssessed"
                        , "nrTextsAssessedReviewed", "nrReadyToPublish"};
                for(String sf : statfields) {
                    statsFinalized.put(sf, new MutableInt(0));
                    statsTag.put(sf, new MutableInt(0));
                }

/*
                File outCSV = File.createTempFile("stats_", null);
                CSVPrinter csvp = new CSVPrinter(new OutputStreamWriter(new FileOutputStream(outCSV)), CSVFormat.EXCEL);
                csvp.print("Taxon");
                for(RedListEnums.Threats th : RedListEnums.Threats.values()) {
                    csvp.print(th.toString());
                }
                csvp.println();
*/
                Iterator<RedListDataEntity> taxEntList2 = driver.getRedListData().getAllRedListData(territory, true, null);
                RedListDataEntitySummary rldeSumFinalized = new RedListDataEntitySummary();
                RedListDataEntitySummary rldeSumTag = new RedListDataEntitySummary();
                Set<String> habitatTypes = new HashSet<>();     // HT are not hardcoded, so we have to build a set with the values

                while (taxEntList2.hasNext()) {
                    RedListDataEntity rlde1 = taxEntList2.next();
                    boolean isPublished = rlde1.getAssessment().getReviewStatus() == RedListEnums.ReviewStatus.REVISED_PUBLISHING;
                    boolean isFromTag = rlde1.containsTag(filtertag);

//                        csvp.print(rlde1.getTaxEnt().getNameWithAnnotationOnly(false));

                    if (rlde1.hasResponsibleForTexts()) {
                        if(isPublished) statsFinalized.get("nrWithResponsible").increment();
                        if(isFromTag) statsTag.get("nrWithResponsible").increment();
                    }

                    if (rlde1.getAssessment().getTextStatus() == RedListEnums.TextStatus.IN_PROGRESS
                            && rlde1.getAssessment().getAssessmentStatus() == RedListEnums.AssessmentStatus.NOT_EVALUATED
                            && rlde1.getAssessment().getReviewStatus() == RedListEnums.ReviewStatus.NOT_REVISED) {
                        if(isPublished) statsFinalized.get("nrTextsInProgress").increment();
                        if (isFromTag) statsTag.get("nrTextsInProgress").increment();
                    }

                    if (rlde1.getAssessment().getTextStatus() == RedListEnums.TextStatus.READY
                            && rlde1.getAssessment().getAssessmentStatus() != RedListEnums.AssessmentStatus.NOT_EVALUATED
                            && rlde1.getAssessment().getReviewStatus() == RedListEnums.ReviewStatus.NOT_REVISED) {
                        if(isPublished) statsFinalized.get("nrTextsAssessed").increment();
                        if (isFromTag) statsTag.get("nrTextsAssessed").increment();
                    }

                    if ((rlde1.getAssessment().getTextStatus() == RedListEnums.TextStatus.READY
                            || rlde1.getAssessment().getTextStatus() == RedListEnums.TextStatus.REVISION_READY)
                            && rlde1.getAssessment().getAssessmentStatus() != RedListEnums.AssessmentStatus.NOT_EVALUATED
                            && (rlde1.getAssessment().getReviewStatus() == RedListEnums.ReviewStatus.REVISED_WORKING
                                || rlde1.getAssessment().getReviewStatus() == RedListEnums.ReviewStatus.REVISED_MAJOR
                            )) {
                        if(isPublished) statsFinalized.get("nrTextsAssessedReviewed").increment();
                        if (isFromTag) statsTag.get("nrTextsAssessedReviewed").increment();
                    }

                    if (rlde1.getAssessment().getReviewStatus() == RedListEnums.ReviewStatus.REVISED_PUBLISHING) {
                        if(isPublished) statsFinalized.get("nrReadyToPublish").increment();
                        if (isFromTag) statsTag.get("nrReadyToPublish").increment();
                    }

                    for (Threat th : rlde1.getThreats().getThreats()) {
                        if(isPublished) rldeSumFinalized.addForTaxon(rlde1.getTaxEntID(), th);
                        if(isFromTag) rldeSumTag.addForTaxon(rlde1.getTaxEntID(), th);
                    }

                    for (RedListEnums.ProposedConservationActions th : rlde1.getConservation().getProposedConservationActions()) {
                        if(isPublished) rldeSumFinalized.addForTaxon(rlde1.getTaxEntID(), th);
                        if(isFromTag) rldeSumTag.addForTaxon(rlde1.getTaxEntID(), th);
                    }

                    for (RedListEnums.ProposedStudyMeasures th : rlde1.getConservation().getProposedStudyMeasures()) {
                        if(isPublished) rldeSumFinalized.addForTaxon(rlde1.getTaxEntID(), th);
                        if(isFromTag) rldeSumTag.addForTaxon(rlde1.getTaxEntID(), th);
                    }

                    for (RedListEnums.Uses th : rlde1.getUsesAndTrade().getUses()) {
                        if(isPublished) rldeSumFinalized.addForTaxon(rlde1.getTaxEntID(), th);
                        if(isFromTag) rldeSumTag.addForTaxon(rlde1.getTaxEntID(), th);
                    }

                    if (rlde1.getAssessment().getCategory() != null) {
                        if(isPublished) rldeSumFinalized.addForTaxon(rlde1.getTaxEntID(), rlde1.getAssessment().getCategory());
                        if(isFromTag) rldeSumTag.addForTaxon(rlde1.getTaxEntID(), rlde1.getAssessment().getCategory());

                        if(rlde1.getAssessment().getCategory().getEffectiveCategory() == RedListEnums.RedListCategories.CR) {
                            if(rlde1.getAssessment().getSubCategory() != null && rlde1.getAssessment().getSubCategory() == RedListEnums.CRTags.PRE) {
                                if(isPublished) rldeSumFinalized.addForTaxon(rlde1.getTaxEntID(), "CR(PRE)");
                                if(isFromTag) rldeSumTag.addForTaxon(rlde1.getTaxEntID(), "CR(PRE)");
                            }
                        }
                    }

                    if(rlde1.getConservation().getConservationPlans() == RedListEnums.YesNoNA.YES) {
                        if(isPublished) rldeSumFinalized.addForTaxon(rlde1.getTaxEntID(), "Had Conservation Plan");
                        if(isFromTag) rldeSumTag.addForTaxon(rlde1.getTaxEntID(), "Had Conservation Plan");
                    }

                    if(rlde1.getConservation().getExSituConservation() == RedListEnums.YesNoNA.YES) {
                        if(isPublished) rldeSumFinalized.addForTaxon(rlde1.getTaxEntID(), "Had ex-situ conservation");
                        if(isFromTag) rldeSumTag.addForTaxon(rlde1.getTaxEntID(), "Had ex-situ conservation");
                    }

                    if (rlde1.getAssessment().getUpDownListing() != null) {
                        if(isPublished) rldeSumFinalized.addForTaxon(rlde1.getTaxEntID(), rlde1.getAssessment().getUpDownListing());
                        if(isFromTag) rldeSumTag.addForTaxon(rlde1.getTaxEntID(), rlde1.getAssessment().getUpDownListing());
                    }

                    for (RedListEnums.AssessmentCriteria th : rlde1.getAssessment().getCriteria()) {
                        if(isPublished) rldeSumFinalized.addForTaxon(rlde1.getTaxEntID(), th);
                        if(isFromTag) rldeSumTag.addForTaxon(rlde1.getTaxEntID(), th);
                    }

                    for (String h : rlde1.getEcology().getHabitatTypes()) {
                        if (h == null) continue;
                        if(isPublished) rldeSumFinalized.addForTaxon(rlde1.getTaxEntID(), h);
                        if(isFromTag) rldeSumTag.addForTaxon(rlde1.getTaxEntID(), h);
                        habitatTypes.add(h);
                    }
                }
/*
                    List<RedListEnums.Threats> thisThreats = Arrays.asList(rlde1.getThreats().getThreats());
                    for(RedListEnums.Threats th : RedListEnums.Threats.values()) {
                        if(thisThreats.contains(th))
                            csvp.print("1");
                        else
                            csvp.print("");
                    }

                    csvp.println();
*/
//                    csvp.close();

                for(String sf : statfields) {
                    thisRequest.request.setAttribute(sf + "_full", statsFinalized.get(sf).intValue());
                    thisRequest.request.setAttribute(sf + "_tag", statsTag.get(sf).intValue());
                }

                Map<String, Map<String, Integer>> statTableFinalized = new HashMap<>();
                Map<String, Map<String, Integer>> statTableTag = new HashMap<>();
                Map<String, Integer> tableFinalized = new HashMap<>();
                Map<String, Integer> tableTag = new HashMap<>();
                for(Threat th : driver.getThreatEnum().values()) {
                    tableFinalized.put(th.getLabel(), rldeSumFinalized.getCountsForProperty(th));
                    tableTag.put(th.getLabel(), rldeSumTag.getCountsForProperty(th));
                }
                statTableFinalized.put("Ameaças 6.2", tableFinalized);
                statTableTag.put("Ameaças 6.2", tableTag);

                tableFinalized = new HashMap<>();
                tableTag = new HashMap<>();
                for(RedListEnums.ProposedStudyMeasures th : RedListEnums.ProposedStudyMeasures.values()) {
                    tableFinalized.put(th.getLabel(), rldeSumFinalized.getCountsForProperty(th));
                    tableTag.put(th.getLabel(), rldeSumTag.getCountsForProperty(th));
                }
                statTableFinalized.put("Estudos 7.6", tableFinalized);
                statTableTag.put("Estudos 7.6", tableTag);

                tableFinalized = new HashMap<>();
                tableTag = new HashMap<>();
                for(RedListEnums.ProposedConservationActions th : RedListEnums.ProposedConservationActions.values()) {
                    tableFinalized.put(th.getLabel(), rldeSumFinalized.getCountsForProperty(th));
                    tableTag.put(th.getLabel(), rldeSumTag.getCountsForProperty(th));
                }
                statTableFinalized.put("Medidas de conservação 7.5", tableFinalized);
                statTableTag.put("Medidas de conservação 7.5", tableTag);

                tableFinalized = new HashMap<>();
                tableTag = new HashMap<>();
                for(RedListEnums.Uses th : RedListEnums.Uses.values()) {
                    tableFinalized.put(th.getLabel(), rldeSumFinalized.getCountsForProperty(th));
                    tableTag.put(th.getLabel(), rldeSumTag.getCountsForProperty(th));
                }
                statTableFinalized.put("Usos 5.2", tableFinalized);
                statTableTag.put("Usos 5.2", tableTag);

                tableFinalized = new HashMap<>();
                tableTag = new HashMap<>();
                for(RedListEnums.RedListCategories th : RedListEnums.RedListCategories.valuesNotUpDownListed()) {
                    tableFinalized.put(th.getLabel(), rldeSumFinalized.getCountsForProperty(th));
                    tableTag.put(th.getLabel(), rldeSumTag.getCountsForProperty(th));
                }

                tableFinalized.put("CR (PRE)", rldeSumFinalized.getCountsForProperty("CR(PRE)"));
                tableTag.put("CR (PRE)", rldeSumTag.getCountsForProperty("CR(PRE)"));
                statTableFinalized.put("Categorias de ameaça 9.1", tableFinalized);
                statTableTag.put("Categorias de ameaça 9.1", tableTag);

                tableFinalized = new HashMap<>();
                tableTag = new HashMap<>();
                tableFinalized.put("Had Conservation Plan", rldeSumFinalized.getCountsForProperty("Had Conservation Plan"));
                tableTag.put("Had Conservation Plan", rldeSumTag.getCountsForProperty("Had Conservation Plan"));
                tableFinalized.put("Had ex-situ conservation", rldeSumFinalized.getCountsForProperty("Had ex-situ conservation"));
                tableTag.put("Had ex-situ conservation", rldeSumTag.getCountsForProperty("Had ex-situ conservation"));
                statTableFinalized.put("Foi alvo de medidas de conservação?", tableFinalized);
                statTableTag.put("Foi alvo de medidas de conservação?", tableTag);

                tableFinalized = new HashMap<>();
                tableTag = new HashMap<>();
                for(RedListEnums.AssessmentCriteria th : RedListEnums.AssessmentCriteria.values()) {
                    tableFinalized.put(th.getLabel(), rldeSumFinalized.getCountsForProperty(th));
                    tableTag.put(th.getLabel(), rldeSumTag.getCountsForProperty(th));
                }
                statTableFinalized.put("Critérios utilizados 9.2", tableFinalized);
                statTableTag.put("Critérios utilizados 9.2", tableTag);

                tableFinalized = new HashMap<>();
                tableTag = new HashMap<>();
                for(RedListEnums.UpDownList th : RedListEnums.UpDownList.values()) {
                    tableFinalized.put(th.getLabel(), rldeSumFinalized.getCountsForProperty(th));
                    tableTag.put(th.getLabel(), rldeSumTag.getCountsForProperty(th));
                }
                statTableFinalized.put("Up/Down listing 9.4.4", tableFinalized);
                statTableTag.put("Up/Down listing 9.4.4", tableTag);

                // habitat types
                tableFinalized = new HashMap<>();
                tableTag = new HashMap<>();
                Iterator<Habitat> ith = NWD.getDocuments(habitatTypes, Habitat.class);
                while(ith.hasNext()) {
                    Habitat tmp2 = ith.next();
                    if(tmp2 != null) {
                        tableFinalized.put(tmp2.getName(), rldeSumFinalized.getCountsForProperty(tmp2.getID()));
                        tableTag.put(tmp2.getName(), rldeSumTag.getCountsForProperty(tmp2.getID()));
                    }
                }
                statTableFinalized.put("Habitat types", tableFinalized);
                statTableTag.put("Habitat types", tableTag);

                thisRequest.request.setAttribute("statTables", statTableFinalized);
                thisRequest.request.setAttribute("statTablesTag", statTableTag);

                // now crossed stats
                Map<String, Table> crossedStatTable = new HashMap<>();

                Table<String, String, Integer> mkm = HashBasedTable.create();
                ith = NWD.getDocuments(habitatTypes, Habitat.class);
                while(ith.hasNext()) {
                    Habitat tmp2 = ith.next();
                    if(tmp2 != null) {
                        for(RedListEnums.RedListCategories th : RedListEnums.RedListCategories.valuesNotUpDownListed())
                            mkm.put(tmp2.getName(), th.getShortTag(), rldeSumFinalized.getCountsForPropertyIntersection(tmp2.getID(), th));
                    }
                }
                crossedStatTable.put("Habitat", mkm);

                thisRequest.request.setAttribute("crossedStatTables", crossedStatTable);

//                    thisRequest.request.setAttribute("download", outCSV.getName());
                break;

            case "debug":
                RedListDataFilter onlyInvalid = new RedListDataFilter() {
                    @Override
                    public boolean enter(RedListDataEntity rlde) {
                        return rlde.validateCriteria().size() > 0;
                    }
                };

                rldeIt = new RedListDataEntityIterator(
                        driver.getRedListData().getAllRedListData(territory, false, null)
                        , onlyInvalid);
                request.setAttribute("invalidSheets", rldeIt);

                Iterator<RedListDataEntity> rldeIt1;
                RedListDataFilter onlySeverelyFragmented = new RedListDataFilter() {
                    @Override
                    public boolean enter(RedListDataEntity rlde) {
                        return RedListEnums.SeverelyFragmented.SEVERELY_FRAGMENTED.equals(rlde.getPopulation().getSeverelyFragmented());
                    }
                };

                rldeIt1 = new RedListDataEntityIterator(
                    driver.getRedListData().getAllRedListData(territory, false, null)
                    , onlySeverelyFragmented);
                request.setAttribute("severelyFragmented", rldeIt1);

                Iterator<RedListDataEntity> rldeIt2;
                RedListDataFilter onlyExtremeFluctuations = new RedListDataFilter() {
                    @Override
                    public boolean enter(RedListDataEntity rlde) {
                        return RedListEnums.YesNoNA.YES.equals(rlde.getPopulation().getExtremeFluctuations());
                    }
                };

                rldeIt2 = new RedListDataEntityIterator(
                        driver.getRedListData().getAllRedListData(territory, false, null)
                        , onlyExtremeFluctuations);
                request.setAttribute("extremeFluctuations", rldeIt2);
                break;
        }

        request.setAttribute("warning", warnings);
        request.getRequestDispatcher("/main-redlistinfo.jsp").forward(request, thisRequest.response);

        thisRequest.response.flushBuffer();
    }
}
