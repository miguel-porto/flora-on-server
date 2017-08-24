package pt.floraon.redlistdata;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.lang.ArrayUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import pt.floraon.authentication.Privileges;
import pt.floraon.authentication.entities.TaxonPrivileges;
import pt.floraon.bibliography.BibliographyCompiler;
import pt.floraon.driver.*;
import pt.floraon.driver.datatypes.SafeHTMLString;
import pt.floraon.driver.jobs.JobRunner;
import pt.floraon.driver.jobs.JobSubmitter;
import pt.floraon.ecology.entities.Habitat;
import pt.floraon.geometry.PolygonTheme;
import pt.floraon.occurrences.fieldparsers.DateParser;
import pt.floraon.redlistdata.dataproviders.SimpleOccurrenceDataProvider;
import pt.floraon.redlistdata.entities.*;
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

import static pt.floraon.authentication.Privileges.EDIT_ALL_FIELDS;
import static pt.floraon.driver.utils.StringUtils.cleanArray;
import static pt.floraon.driver.utils.StringUtils.sanitizeHtmlId;

/**
 * Main page of red list data
 * Created by Miguel Porto on 01-11-2016.
 */
@WebServlet("/redlist/*")
public class RedListAdminPages extends FloraOnServlet {
    @Override
    public void doFloraOnPost(ThisRequest thisRequest) throws ServletException, IOException, FloraOnException {
        doFloraOnGet(thisRequest);
    }

    @Override
    public void doFloraOnGet(ThisRequest thisRequest) throws ServletException, IOException, FloraOnException {
        final HttpServletRequest request = thisRequest.request;
        String what;
        TaxEnt te;
        long sizeOfSquare = 2000;
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

        switch (what) {
/*
 * Shows the taxon list
 */
            case "main":
//                List<TaxEnt> taxEntList = driver.getListDriver().getAllSpeciesOrInferiorTaxEnt(true, true, territory, null, null);
//                List<RedListDataEntity> taxEntList = driver.getRedListData().getAllRedListData(territory, getUser().canMANAGE_REDLIST_USERS());
//                thisRequest.setCacheHeaders(60);
                thisRequest.getUser().resetEffectivePrivileges();
//                taxEntList.get(0).getAssessment().getAssessmentStatus().isAssessed()
                Set<String> at = driver.getRedListData().getRedListTags(territory);
                Map<String, String> ate = new HashMap<>();
                for(String s : at)
                    ate.put(sanitizeHtmlId(s), s);


                Iterator<RedListDataEntity> taxEntList =
                        driver.getRedListData().getAllRedListData(territory, true, null); //new String[] {"Lista preliminar", "Lista Alvo", "Lista B"}
                request.setAttribute("allTags", ate.entrySet());
                request.setAttribute("specieslist", taxEntList);
                break;

            case "taxon":
                // enums
                request.setAttribute("geographicalDistribution_DeclineDistribution", RedListEnums.DeclineDistribution.values());
                request.setAttribute("geographicalDistribution_ExtremeFluctuations", RedListEnums.ExtremeFluctuations.values());
                request.setAttribute("population_NrMatureIndividualsCategory", RedListEnums.NrMatureIndividuals.values());
                request.setAttribute("population_TypeOfEstimate", RedListEnums.TypeOfPopulationEstimate.values());
                request.setAttribute("population_PopulationDecline", RedListEnums.DeclinePopulation.values());
                request.setAttribute("population_PopulationSizeReduction", RedListEnums.PopulationSizeReduction.values());
                request.setAttribute("population_SeverelyFragmented", RedListEnums.SeverelyFragmented.values());
                request.setAttribute("population_ExtremeFluctuations", RedListEnums.YesNoNA.values());
                request.setAttribute("population_NrMatureEachSubpop", RedListEnums.NrMatureEachSubpop.values());
                request.setAttribute("population_PercentMatureOneSubpop", RedListEnums.PercentMatureOneSubpop.values());
//                request.setAttribute("ecology_HabitatTypes", RedListEnums.HabitatTypes.values());
//                request.setAttribute("ecology_HabitatTypes", driver.getRedListData().getAllHabitats().toArray(new Habitat[0]));
                request.setAttribute("ecology_DeclineHabitatQuality", RedListEnums.DeclineHabitatQuality.values());
                request.setAttribute("usesAndTrade_Uses", RedListEnums.Uses.values());
                request.setAttribute("usesAndTrade_Overexploitation", RedListEnums.Overexploitation.values());
                request.setAttribute("threats_Threats", RedListEnums.Threats.values());
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

//                request.getRequestDispatcher("/main-redlistinfo.jsp").forward(request, response);

                // TODO this should be a user configuration loaded at startup
                PolygonTheme protectedAreas = new PolygonTheme(this.getClass().getResourceAsStream("SNAC.geojson"), "SITE_NAME");
                String[] ids = request.getParameterValues("id");
                if(ids == null || ids.length == 0) {
                    warnings.add("DataSheet.msg.warning.1a");
                    break;
                }
                if(ids.length == 1) {       // only one taxon requested
                    RedListDataEntity rlde = driver.getRedListData().getRedListDataEntity(territory, thisRequest.getParameterAsKey("id"));
                    if (rlde == null) return;
                    BibliographyCompiler<RedListDataEntity, SafeHTMLString> bc = new BibliographyCompiler<>(Collections.singletonList(rlde), SafeHTMLString.class);
//                    request.setAttribute("bibliography", driver.getNodeWorkerDriver().getDocuments(bc.getBibliography(), Reference.class));
                    request.setAttribute("bibliography", bc.getBibliography(driver));
//                    bc.formatCitations();

                    // set privileges for this taxon
                    thisRequest.getUser().setEffectivePrivilegesFor(driver, thisRequest.getParameterAsKey("id"));

                    request.setAttribute("taxon", rlde.getTaxEnt());
                    request.setAttribute("synonyms", driver.wrapTaxEnt(thisRequest.getParameterAsKey("id")).getSynonyms());
                    request.setAttribute("formerlyIncluded", driver.wrapTaxEnt(thisRequest.getParameterAsKey("id")).getFormerlyIncludedIn());
                    request.setAttribute("includedTaxa", driver.wrapTaxEnt(thisRequest.getParameterAsKey("id")).getIncludedTaxa());
                    List<SimpleOccurrenceDataProvider> sodps = driver.getRedListData().getSimpleOccurrenceDataProviders();
                    for(SimpleOccurrenceDataProvider edp : sodps)
                        edp.executeOccurrenceQuery(rlde.getTaxEnt());

                    // TODO clipping polygon and years must be a user configuration
                    PolygonTheme clippingPolygon = new PolygonTheme(this.getClass().getResourceAsStream("PT_buffer.geojson"), null);
                    OccurrenceProcessor historicalOccurrenceProcessor = new OccurrenceProcessor(
                            sodps, protectedAreas, sizeOfSquare, clippingPolygon, null, 1990, false);

                    OccurrenceProcessor occurrenceProcessor = new OccurrenceProcessor(
                            sodps, protectedAreas, sizeOfSquare, clippingPolygon, 1991, null, false);
                    if(occurrenceProcessor.size() > 0 || historicalOccurrenceProcessor.size() > 0) {
                        // if it is published, AOO and EOO are from the data sheet, otherwise they are computed from
                        // live occurrences
                        Double EOO = null, AOO = null, hEOO = null, hAOO = null;
                        if (rlde.getAssessment().getPublicationStatus() == RedListEnums.PublicationStatus.PUBLISHED) {
                            EOO = rlde.getGeographicalDistribution().getEOO();
                            AOO = rlde.getGeographicalDistribution().getAOO();
                            hEOO = rlde.getGeographicalDistribution().getHistoricalEOO();
                            hAOO = rlde.getGeographicalDistribution().getHistoricalAOO();
                        }
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

                        StringWriter sw = new StringWriter();
                        occurrenceProcessor.exportSVG(new PrintWriter(sw), thisRequest.getUser().canVIEW_FULL_SHEET()
                                , true, true, false, 0, true);
                        request.setAttribute("svgmap", sw.toString());
                        sw.close();

                        if(historicalOccurrenceProcessor.getNQuads() > 0) {
                            sw = new StringWriter();
                            historicalOccurrenceProcessor.exportSVG(new PrintWriter(sw), thisRequest.getUser().canVIEW_FULL_SHEET()
                                    , true, true, false, 0, true);
                            request.setAttribute("historicalsvgmap", sw.toString());
                            sw.close();
                        }

                        Set<String> groupAreasBy = new HashSet<>();
                        groupAreasBy.add("SITE_NAME");
                        groupAreasBy.add("TIPO");   // TODO this should be user configuration
                        request.setAttribute("occurrenceInProtectedAreas"
                                , occurrenceProcessor.getOccurrenceInProtectedAreas(groupAreasBy).entrySet());
                        request.setAttribute("locationsInPA", occurrenceProcessor.getNumberOfLocationsInsideProtectedAreas());

                        if(rlde.getTaxEnt().getOldId() != null) {
                            // FIXME!!!! this only for Flora-On...
                            Map<String, Object> taxonInfo = sodps.get(0).executeInfoQuery(rlde.getTaxEnt().getOldId());

                            if (rlde.getEcology().getDescription() == null || rlde.getEcology().getDescription().toString().trim().equals("")) {
                                if (taxonInfo.containsKey("ecology") && taxonInfo.get("ecology") != null) {
                                    request.setAttribute("ecology", taxonInfo.get("ecology").toString());
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
                        request.setAttribute("occurrences", occurrenceProcessor);
                        request.setAttribute("historicalOccurrences", historicalOccurrenceProcessor);
                    }

                    if(rlde.getTaxEnt().getOldId() == null) {
                        warnings.add("DataSheet.msg.warning.1b");
                        request.setAttribute("ecology", rlde.getEcology().getDescription());
                    }

                    request.setAttribute("rlde", rlde);
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


                    if (rlde.getAssessment().getPublicationStatus() == RedListEnums.PublicationStatus.PUBLISHED) {
                        // if it's published, block editing all fields
                        boolean canEdit9 = thisRequest.getUser().canEDIT_9_9_4();
                        thisRequest.getUser().revokePrivileges(EDIT_ALL_FIELDS);
                        if (canEdit9) thisRequest.getUser().setEDIT_9_9_4(true);
                    }

                    warnings.addAll(rlde.validateCriteria());
                } else {    // multiple IDs provided, batch update
                    thisRequest.getUser().resetEffectivePrivileges();
                    warnings.add("DataSheet.msg.warning.1");
                    request.setAttribute("allTags", driver.getRedListData().getRedListTags(territory));
                    request.setAttribute("multipletaxa", true);
                    List<TaxEnt> taxEnts = driver.getNodeWorkerDriver().getTaxEntByIds(request.getParameterValues("id"));
                    request.setAttribute("taxa", taxEnts);
                    List<PreviousAssessment> prev = new ArrayList<>();
                    for (int i = 0; i < 2; i++) {
                        prev.add(new PreviousAssessment());
                    }
                    request.setAttribute("previousAssessments", prev);
                }

                break;

            case "taxonrecords":
                if (!thisRequest.getUser().canVIEW_OCCURRENCES()) break;
                te = driver.getNodeWorkerDriver().getTaxEntById(thisRequest.getParameterAsKey("id"));
                request.setAttribute("taxon", te);
                PolygonTheme clippingPolygon2 = new PolygonTheme(this.getClass().getResourceAsStream("PT_buffer.geojson"), null);
                List<SimpleOccurrenceDataProvider> sodps = driver.getRedListData().getSimpleOccurrenceDataProviders();

                for(SimpleOccurrenceDataProvider edp : sodps)
                    edp.executeOccurrenceQuery(te);

                OccurrenceProcessor op = OccurrenceProcessor.iterableOf(sodps
                        , clippingPolygon2, "all".equals(thisRequest.getParameterAsString("view")) ? null : 1991
                        , null, "all".equals(thisRequest.getParameterAsString("view")));

                request.setAttribute("occurrences", op);

                if(thisRequest.getParameterAsInteger("group", 0) > 0) {
                    OccurrenceProcessor op1 = OccurrenceProcessor.iterableOf(sodps
                            , clippingPolygon2, "all".equals(thisRequest.getParameterAsString("view")) ? null : 1991
                            , null, "all".equals(thisRequest.getParameterAsString("view")));

                    SimpleOccurrenceClusterer clusters = new SimpleOccurrenceClusterer(op1.iterator()
                            , thisRequest.getParameterAsInteger("group", 0));

                    request.setAttribute("clustoccurrences", clusters.getClusteredOccurrences().asMap().entrySet());
//                clusters.getClusteredOccurrences().asMap().entrySet().iterator().next().getValue().iterator().next().getLocality()
                }
                break;

            case "downloadtaxonrecords":
                if (!thisRequest.getUser().canDOWNLOAD_OCCURRENCES()) throw new FloraOnException("You don't have privileges for this operation");
                te = driver.getNodeWorkerDriver().getTaxEntById(thisRequest.getParameterAsKey("id"));
                List<SimpleOccurrenceDataProvider> sodps1 = driver.getRedListData().getSimpleOccurrenceDataProviders();

                for(SimpleOccurrenceDataProvider edp : sodps1)
                    edp.executeOccurrenceQuery(te);

                thisRequest.response.setContentType("application/vnd.google-earth.kml+xml; charset=utf-8");
                thisRequest.response.addHeader("Content-Disposition", "attachment;Filename=\"occurrences.kml\"");
                PrintWriter wr = thisRequest.response.getWriter();
                OccurrenceProcessor.iterableOf(sodps1).exportKML(wr);
                wr.flush();
                return;

            case "downloadtargetrecords":
//                if(!getUser().canDOWNLOAD_OCCURRENCES()) throw new FloraOnException("You don't have privileges for this operation");
                PolygonTheme clip = thisRequest.getUser()._getUserPolygonsAsTheme();
                if(clip == null || clip.size() == 0) break;
                List<TaxEnt> lt = new ArrayList<>();
                Iterator<TaxEnt> it = driver.getRedListData().getAllRedListTaxa(territory, "Prospecção");

                while(it.hasNext())
                    lt.add(it.next());

                List<SimpleOccurrenceDataProvider> sodps2 = driver.getRedListData().getSimpleOccurrenceDataProviders();
                for(SimpleOccurrenceDataProvider edp : sodps2)
                    edp.executeOccurrenceQuery(lt.iterator());

                thisRequest.response.setContentType("application/vnd.google-earth.kml+xml; charset=utf-8");
                thisRequest.response.addHeader("Content-Disposition", "attachment;Filename=\"occurrences.kml\"");
                PrintWriter wr1 = thisRequest.response.getWriter();
//                OccurrenceProcessor.iterableOf(driver.getRedListData().getSimpleOccurrenceDataProviders()).exportKML(wr1);
                OccurrenceProcessor.iterableOf(sodps2, clip, null, null).exportKML(wr1);
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
                ByteArrayOutputStream baos = new ByteArrayOutputStream();

                PrintWriter wr3 = thisRequest.response.getWriter();//new PrintWriter(baos);
                wr3.println("# " + rlde2.getTaxEnt().getFullName(false));
                wr3.println("## 1. Information");
                wr3.println("### 1.4. Notas taxonómicas");
                wr3.println(rlde2.getTaxonomicProblemDescription().toMarkDownString());
                wr3.println("## 2. Geographical Distribution");
                wr3.println("### 2.1. Distribuição");
                wr3.println(rlde2.getGeographicalDistribution().getDescription().toMarkDownString());
                wr3.println("### 2.4. Declínio da área de distribuição");
                wr3.println(rlde2.getGeographicalDistribution().getDeclineDistributionJustification().toMarkDownString());
                wr3.println("## 3. Population");
                wr3.println("### 3.1. Estado da população");
                wr3.println(rlde2.getPopulation().getDescription().toMarkDownString());
                wr3.println("### 3.3. Tipo de estimativa");
                wr3.println(rlde2.getPopulation().getNrMatureIndividualsDescription().toMarkDownString());
                wr3.println("### 3.4. Declínio populacional");
                wr3.println(rlde2.getPopulation().getPopulationDeclineJustification().toMarkDownString());
                wr3.println("### 3.5. Redução populacional");
                wr3.println(rlde2.getPopulation().getPopulationSizeReductionJustification().toMarkDownString());
                wr3.println("### 3.6. Fragmentação severa");
                wr3.println(rlde2.getPopulation().getSeverelyFragmentedJustification().toMarkDownString());
                wr3.println("### 3.7. Flutuações extremas no tamanho da população");
                wr3.println(rlde2.getPopulation().getExtremeFluctuationsJustification().toMarkDownString());
                wr3.println("## 4. Ecology");
                wr3.println("### 4.1. Ecologia e biologia");
                wr3.println(rlde2.getEcology().getDescription().toMarkDownString());
                wr3.println("### 4.4. Duração de uma geração");
                wr3.println(rlde2.getEcology().getGenerationLengthJustification().toMarkDownString());
                wr3.println("### 4.5. Declínio na qualidade do habitat");
                wr3.println(rlde2.getEcology().getDeclineHabitatQualityJustification().toMarkDownString());
                wr3.println("## 5. Uses and trade");
                wr3.println("### 5.1. Usos e comércio");
                wr3.println(rlde2.getUsesAndTrade().getDescription().toMarkDownString());
                wr3.println("## 6. Threats");
                wr3.println("### 6.1. Descrição de ameaças e pressões");
                wr3.println(rlde2.getThreats().getDescription().toMarkDownString());
                wr3.println("### 6.3. Número do localizações");
                wr3.println(rlde2.getThreats().getNumberOfLocationsJustification().toMarkDownString());
                wr3.println("### 6.4. Declínio continuado no número de localizações ou de subpopulações");
                wr3.println(rlde2.getThreats().getDeclineNrLocationsJustification().toMarkDownString());
                wr3.println("### 6.5. Flutuações extremas no número de localizações ou subpopulações");
                wr3.println(rlde2.getThreats().getExtremeFluctuationsNrLocationsJustification().toMarkDownString());
                wr3.println("## 7. Conservation");
                wr3.println("### 7.1. Medidas de conservação");
                wr3.println(rlde2.getConservation().getDescription().toMarkDownString());
                wr3.println("### 7.2. Existência de planos de gestão/conservação");
                wr3.println(rlde2.getConservation().getConservationPlansJustification().toMarkDownString());
                wr3.println("### 7.3. Conservação *ex-situ*");
                wr3.println(rlde2.getConservation().getExSituConservationJustification().toMarkDownString());
                wr3.println("## 9. Assessment");
                wr3.println("### 9.3. Justificação");
                wr3.println(rlde2.getAssessment().getJustification().toMarkDownString());
                wr3.println("### 9.4.5. Justificação de subida ou descida de categoria");
                wr3.println(rlde2.getAssessment().getUpDownListingJustification().toMarkDownString());
                wr3.println("### 9.4.6. Avaliação final");
                wr3.println(rlde2.getAssessment().getFinalJustification().toMarkDownString());
                wr3.flush();
                return;

            case "contentasxml":
                Iterator<RedListDataEntity> rldeit = driver.getRedListData().getAllRedListData("lu", false, null);
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
                    while (rldeit.hasNext()) {
                        RedListDataEntity rlde1 = rldeit.next();
                        if (rlde1.getAssessment().getAssessmentStatus().isAssessed()) count1++;
                        if (rlde1.getAssessment().getReviewStatus() == RedListEnums.ReviewStatus.REVISED_PUBLISHING) count2++;
                        if (rlde1.getAssessment().getPublicationStatus().isPublished()) count3++;
                        if (rlde1.getAssessment().getAssessmentStatus().isAssessed()
                            && rlde1.getAssessment().getCategory() != null && rlde1.getAssessment().getAdjustedCategory().isThreatened())
                            count4++;
                    }
                    Element nrAssessed = doc.createElement("numberAssessed");
                    Element nrRevised = doc.createElement("numberRevised");
                    Element nrPublished = doc.createElement("numberPublished");
                    Element nrThreatened = doc.createElement("numberThreatened");

                    nrAssessed.appendChild(doc.createTextNode(count1.toString()));
                    nrRevised.appendChild(doc.createTextNode(count2.toString()));
                    nrPublished.appendChild(doc.createTextNode(count3.toString()));
                    nrThreatened.appendChild(doc.createTextNode(count1.equals(0) ? "?" : count4.toString()));

                    rootElement.appendChild(nrAssessed);
                    rootElement.appendChild(nrRevised);
                    rootElement.appendChild(nrPublished);
                    rootElement.appendChild(nrThreatened);
                } else {
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

                    while (rldeit.hasNext()) {
                        RedListDataEntity rlde = rldeit.next();
                        if (Collections.disjoint(Collections.singleton("Lista Alvo"), Arrays.asList(rlde.getTags()))
                                || rlde.getGeographicalDistribution().getDescription() == null
                                || rlde.getGeographicalDistribution().getDescription().equals("")) continue;

                        Element species = doc.createElement("species");

                        Element name = doc.createElement("name");
                        name.appendChild(doc.createTextNode(rlde.getTaxEnt().getName()));

                        Element author = doc.createElement("author");
                        author.appendChild(doc.createTextNode(rlde.getTaxEnt().getAuthor()));

                        Element fullname = doc.createElement("fullname");
                        fullname.appendChild(doc.createTextNode(rlde.getTaxEnt().getFullName(true)));

                        Element distr = doc.createElement("distribution");
//                    distr.appendChild(doc.createTextNode(Jsoup.parse(rlde.getGeographicalDistribution().getDescription()).text()));
                        distr.appendChild(doc.createTextNode(rlde.getGeographicalDistribution().getDescription().toString()));

                        Element pop = doc.createElement("population");
                        pop.appendChild(doc.createTextNode(rlde.getPopulation().getDescription().toString()));

                        Element ecology = doc.createElement("ecology");
                        ecology.appendChild(doc.createTextNode(rlde.getEcology().getDescription().toString()));

                        Element uses = doc.createElement("uses");
                        uses.appendChild(doc.createTextNode(rlde.getUsesAndTrade().getDescription().toString()));

                        Element threats = doc.createElement("threats");
                        threats.appendChild(doc.createTextNode(rlde.getThreats().getDescription().toString()));

                        Element conservation = doc.createElement("conservation");
                        conservation.appendChild(doc.createTextNode(rlde.getConservation().getDescription().toString()));

                        Element assessjust = doc.createElement("assessmentJustification");
                        assessjust.appendChild(doc.createTextNode(rlde.getAssessment().getFinalJustification().toString()));

                        Element assesscrit = doc.createElement("assessmentCriteria");
                        assesscrit.appendChild(doc.createTextNode(rlde.getAssessment()._getCriteriaAsString()));

                        Element assesscat = doc.createElement("assessmentCategory");
                        assesscat.appendChild(doc.createTextNode(rlde.getAssessment()._getCategoryAsString()));

                        Element assesscatcrit = doc.createElement("assessmentCategoryAndCriteria");
                        assesscatcrit.appendChild(doc.createTextNode((rlde.getAssessment()._getCategoryAsString()
                                + " " + rlde.getAssessment()._getCriteriaAsString()).trim()));

                        Element assesscatverb = doc.createElement("assessmentCategoryVerbose");
                        assesscatverb.appendChild(doc.createTextNode(rlde.getAssessment()._getCategoryVerboseAsString()));

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
//                                System.out.println(resp.toString());
                                    occArray = new Gson().fromJson(resp.getAsJsonArray("msg"), listType);
                                    for (Map<String, Object> ph : occArray) {
                                        Element photo = doc.createElement("photo");
                                        photo.appendChild(doc.createTextNode("http://flora-on.pt/" + cn.getGenus() + "-"
                                                + cn.getSpecificEpithet() + "_ori_" + ph.get("guid").toString() + ".jpg"));
                                        photos.appendChild(photo);
                                    }
                                }
                            } catch (URISyntaxException e) {
                                e.printStackTrace();
                            }

                        }

                        species.appendChild(name);
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
                        species.appendChild(assesscatverb);
                        species.appendChild(assesscrit);
                        species.appendChild(assesscatcrit);
                        species.appendChild(citation);
                        species.appendChild(map);
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
                List<User> allusers = driver.getAdministration().getAllUsers(true);
                Map<String, String> taxonMap1 = new HashMap<>();

                Map<String, Integer> responsibleTextCounter = new HashMap<>();
                Map<String, Integer> responsibleAssessmentCounter = new HashMap<>();
                Map<String, Integer> responsibleRevisionCounter = new HashMap<>();

                Iterator<AtomicTaxonPrivilege> tps = driver.getRedListData().getTaxonPrivilegesForAllUsers(territory);
                AtomicTaxonPrivilege atp;
                while(tps.hasNext()) {
                    atp = tps.next();
                    if(atp.isResponsibleForTexts()) {
                        if(responsibleTextCounter.get(atp.getUserId()) == null)
                            responsibleTextCounter.put(atp.getUserId(), 1);
                        else
                            responsibleTextCounter.put(atp.getUserId(), responsibleTextCounter.get(atp.getUserId()) + 1);
                    }

                    if(atp.isResponsibleForAssessment()) {
                        if(responsibleAssessmentCounter.get(atp.getUserId()) == null)
                            responsibleAssessmentCounter.put(atp.getUserId(), 1);
                        else
                            responsibleAssessmentCounter.put(atp.getUserId(), responsibleAssessmentCounter.get(atp.getUserId()) + 1);
                    }

                    if(atp.isResponsibleForRevision()) {
                        if(responsibleRevisionCounter.get(atp.getUserId()) == null)
                            responsibleRevisionCounter.put(atp.getUserId(), 1);
                        else
                            responsibleRevisionCounter.put(atp.getUserId(), responsibleRevisionCounter.get(atp.getUserId()) + 1);
                    }
                }

                // make a map with all taxon names used in privileges
                for(User tmp : allusers) {
                    for (TaxonPrivileges tp : tmp.getTaxonPrivileges()) {
                        for (TaxEnt te1 : driver.getNodeWorkerDriver().getTaxEntByIds(tp.getApplicableTaxa())) {
                            taxonMap1.put(te1.getID(), te1.getName());
                        }
                    }
                }

                request.setAttribute("users", allusers);
                request.setAttribute("taxonMap", taxonMap1);
                request.setAttribute("responsibleTextCounter", responsibleTextCounter);
                request.setAttribute("responsibleAssessmentCounter", responsibleAssessmentCounter);
                request.setAttribute("responsibleRevisionCounter", responsibleRevisionCounter);
                request.setAttribute("redlistprivileges", Privileges.getAllPrivilegesOfTypeAndScope(
                        thisRequest.getUser().getUserType() == User.UserType.ADMINISTRATOR ? null : Privileges.PrivilegeType.REDLISTDATA
                        , null));
                break;

            case "edituser":
                User tmp = driver.getAdministration().getUser(thisRequest.getParameterAsKey("user"));
                List<TaxEnt> applTax = new ArrayList<>();
/*
                for(String i : tmp.getApplicableTaxa()) {
                    applTax.add(driver.getNodeWorkerDriver().getTaxEntById(driver.asNodeKey(i)));
                }
                request.setAttribute("applicableTaxa", applTax);
*/
                Map<String, String> taxonMap = new HashMap<>();
                for(TaxonPrivileges tp : tmp.getTaxonPrivileges()) {
                    for(TaxEnt te1 : driver.getNodeWorkerDriver().getTaxEntByIds(tp.getApplicableTaxa())) {
                        taxonMap.put(te1.getID(), te1.getFullName());
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
                break;

            case "jobs":
                if (!thisRequest.getUser().canMANAGE_REDLIST_USERS()) break;
                String[] allt = driver.getRedListData().getRedListTags(territory).toArray(new String[0]);

                List<JobRunner> jobs = new ArrayList<>();
                for(String jobID : JobSubmitter.getJobList()) {
                    jobs.add(JobSubmitter.getJob(jobID));
                }
                request.setAttribute("jobs", jobs);
                request.setAttribute("allTags", allt);
                break;

            case "allmaps":
                Iterator<TaxEnt> it2 = driver.getRedListData().getAllRedListTaxa(territory, "Lista Alvo");
/*
                int count = 0;
                while(count<600) {
                    count++;
                    it2.next();
                }
*/
                request.setAttribute("allTaxa", it2);
                break;

            case "report":
                DateFormat df = Constants.dateFormat.get();
                Date from = null, to = null;
                try {
                    from = DateParser.parseDateAsDate(thisRequest.getParameterAsString("fromdate"));
                    to = DateParser.parseDateAsDate(thisRequest.getParameterAsString("todate"));
                } catch(IllegalArgumentException e) {
                    warnings.add(e.getMessage());
                    break;
                }
                if(from == null || to == null) {
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
                break;
        }

        request.setAttribute("warning", warnings);
        request.getRequestDispatcher("/main-redlistinfo.jsp").forward(request, thisRequest.response);
    }
}
