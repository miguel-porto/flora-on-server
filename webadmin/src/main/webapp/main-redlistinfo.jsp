<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html>
<head>
	<title>Red list info Manager</title>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
	<link href='http://fonts.googleapis.com/css?family=Lato:300' rel='stylesheet' type='text/css'>
	<link rel="stylesheet" type="text/css" href="/floraon/base.css"/>
	<link rel="stylesheet" type="text/css" href="/floraon/redlist.css"/>
	<script type="text/javascript" src="/floraon/sorttable.js"></script>
	<script type="text/javascript" src="/floraon/basefunctions.js"></script>
	<script type="text/javascript" src="/floraon/ajaxforms.js"></script>
	<script type="text/javascript" src="/floraon/redlistadmin.js"></script>
</head>
<body>
<div id="title">Red List data portal</div>
<div id="main-holder">
    <div id="left-bar">
        <ul>
            <li><a href="?w=main">Index of taxa</a></li>
        </ul>
    </div>
    <div id="main">
    <c:choose>
    <c:when test="${what=='addterritory'}">
        <h1>Create new red list dataset</h1>
        <h2>Select a territory to create a dataset.</h2>
        <ul>
        <c:forEach var="terr" items="${territories}">
            <li><a href="redlist/api/newdataset?territory=${terr.getShortName()}">${terr.getName()}</a></li>
        </c:forEach>
        </ul>
    </c:when>
    <c:when test="${what=='main'}">
        <table id="speciesindex">
        <c:forEach var="taxon" items="${specieslist.iterator()}">
            <c:if test="${taxon.getTaxEnt().isSpecies()}">
                <tr class="species">
            </c:if>
            <c:if test="${!taxon.getTaxEnt().isSpecies()}">
                <tr>
            </c:if>
                <td><input type="checkbox"/></td>
                <td><a href="?w=taxon&id=${taxon.getTaxEnt().getIDURLEncoded()}">${taxon.getTaxEnt().getFullName(true)}</a></td>
                <td>${taxon.getInferredStatus().getStatusSummary()}</td>
            </tr>
        </c:forEach>
        </table>
    </c:when>
    <c:when test="${what=='taxon'}">
        <c:if test="${occurrences == null}">
            <div class="warning"><b>Warning</b><br/>This taxon has no correspondence in Flora-On, please contact the checklist administrator</div>
        </c:if>
        <form class="poster" data-path="/floraon/redlist/api/updatedata">
            <input type="hidden" name="databaseId" value="${rlde.getID()}"/>
            <input type="hidden" name="territory" value="${territory}"/>
            <input type="hidden" name="taxEntID" value="${rlde.getTaxEntID()}"/>

            <table class="sheet">
                <tr class="textual"><td colspan="3" id="sheet-header">
                    <h1>Data sheet</h1>
                    <div class="togglebutton" id="summary_toggle">summary</div>
                </td></tr>
                <tr class="section1"><td class="number" colspan="3">Section 1 - General info</td></tr>
                <tr class="section1 textual">
                    <td class="number">1.1</td>
                    <td colspan="2">
                        <h1><i>${taxon.getName()}</i></h1>
                        <c:if test="${sessionScope.user != null && sessionScope.user.getRole() >= 30}">
                            <p style="text-align:center"><a href="?w=taxonrecords&id=${taxon.getIDURLEncoded()}">view occurrences</a></p>
                            <input type="submit" value=""/>
                        </c:if>
                    </td>
                </tr>
                <tr class="section1"><td class="number">1.2</td><td>Authority</td><td>${taxon.getAuthor()}</td></tr>
                <tr class="section1"><td class="number">1.3</td><td>Synonyms</td><td>
                    <ul>
                    <c:forEach var="synonym" items="${synonyms}">
                        <li data-key="${synonym.getID()}"><c:out value="${synonym.getFullName()}"></c:out></li>
                    </c:forEach>
                    </ul>
                </td></tr>
                <tr class="section1"><td class="number">1.4</td><td>Taxonomic problems</td><td>
                    <table>
                        <tr><td>Has taxonomic problems</td><td>
                            <c:if test="${rlde.getHasTaxonomicProblems()}">
                                <input type="checkbox" name="hasTaxonomicProblems" checked="checked">
                            </c:if>
                            <c:if test="${!rlde.getHasTaxonomicProblems()}">
                                <input type="checkbox" name="hasTaxonomicProblems">
                            </c:if>
                        </td></tr>
                        <tr><td>Problem description</td><td><input type="text" name="taxonomicProblemDescription" value="${rlde.getTaxonomicProblemDescription()}"/></td></tr>
                    </table>
                </td></tr>
                <tr class="section2"><td class="number" colspan="3">Section 2 - Geographical Distribution</td></tr>
                <tr class="section2 textual"><td class="number">2.1</td><td>Distribution (textual)</td><td>
                    <textarea rows="6" name="geographicalDistribution_Description"><c:out value="${rlde.getGeographicalDistribution().getDescription()}"></c:out></textarea>
                </td></tr>
                <tr class="section2"><td class="number">2.2</td><td>Extent Of Occurrence<br/>(EOO)</td><td>
                    <c:if test="${occurrences == null}">
                        No correspondence in Flora-On
                    </c:if>
                    <c:if test="${occurrences != null}">
                        <c:if test="${EOO == null}">
                            Not applicable (${occurrences.size()} occurrences)
                        </c:if>
                        <c:if test="${EOO != null}">
                            <b><fmt:formatNumber value="${EOO}" maxFractionDigits="3"/></b> km<sup>2</sup> (${occurrences.size()} occurrences)
                        </c:if>
                    </c:if>
                </td></tr>
                <tr class="section2"><td class="number">2.3</td><td>Area Of Occupancy<br/>(AOO)</td><td>
                    <c:if test="${occurrences == null}">
                        No correspondence in Flora-On
                    </c:if>
                    <c:if test="${occurrences != null}">
                        <b><fmt:formatNumber value="${AOO}" maxFractionDigits="4"/></b> km<sup>2</sup> (${nquads} ${sizeofsquare}x${sizeofsquare} km squares)
                    </c:if>
                </td></tr>
                <tr class="section2"><td class="number">2.4</td><td>Decline in distribution</td><td>
                    <select name="geographicalDistribution_DeclineDistribution">
                        <c:forEach var="tmp" items="${geographicalDistribution_DeclineDistribution}">
                            <c:if test="${rlde.getGeographicalDistribution().getDeclineDistribution().toString().equals(tmp.toString())}">
                                <option value="${tmp.toString()}" selected="selected">${tmp.getLabel()}</option>
                            </c:if>
                            <c:if test="${!rlde.getGeographicalDistribution().getDeclineDistribution().toString().equals(tmp.toString())}">
                                <option value="${tmp.toString()}">${tmp.getLabel()}</option>
                            </c:if>
                        </c:forEach>
                    </select>
                </td></tr>
                <tr class="section2"><td class="number">2.5</td><td>Elevation</td><td>
                    <input name="geographicalDistribution_ElevationRange" type="text" value="${rlde.getGeographicalDistribution().getElevationRange()[0]}"/>
                    <input name="geographicalDistribution_ElevationRange" type="text" value="${rlde.getGeographicalDistribution().getElevationRange()[1]}"/>
                </td></tr>
                <tr class="section3"><td class="number" colspan="3">Section 3 - Population</td></tr>
                <tr class="section3 textual"><td class="number">3.1</td><td>Population information (textual)</td><td>
                    <textarea rows="6" name="population_Description"><c:out value="${rlde.getPopulation().getDescription()}"></c:out></textarea>
                </td></tr>
                <tr class="section3"><td class="number">3.2</td><td>Nº of mature individuals</td><td>
                    <table>
                        <tr><td>Category</td><td>
                            <select name="population_NrMatureIndividualsCategory">
                                <c:forEach var="tmp" items="${population_NrMatureIndividualsCategory}">
                                    <c:if test="${rlde.getPopulation().getNrMatureIndividualsCategory().toString().equals(tmp.toString())}">
                                        <option value="${tmp.toString()}" selected="selected">${tmp.getLabel()}</option>
                                    </c:if>
                                    <c:if test="${!rlde.getPopulation().getNrMatureIndividualsCategory().toString().equals(tmp.toString())}">
                                        <option value="${tmp.toString()}">${tmp.getLabel()}</option>
                                    </c:if>
                                </c:forEach>
                            </select>
                        </td></tr>
                        <tr><td>Exact number</td><td><input type="text" name="population_NrMatureIndividualsExact" value="${rlde.getPopulation().getNrMatureIndividualsExact()}"/></td></tr>
                        <tr><td>Textual description</td><td><input type="text" name="population_NrMatureIndividualsDescription" value="${rlde.getPopulation().getNrMatureIndividualsDescription()}"/></td></tr>
                    </table>
                </td></tr>
                <tr class="section3"><td class="number">3.3</td><td>Type of estimate</td><td>
                    <select name="population_TypeOfEstimate">
                        <c:forEach var="tmp" items="${population_TypeOfEstimate}">
                            <c:if test="${rlde.getPopulation().getTypeOfEstimate().toString().equals(tmp.toString())}">
                                <option value="${tmp.toString()}" selected="selected">${tmp.getLabel()}</option>
                            </c:if>
                            <c:if test="${!rlde.getPopulation().getTypeOfEstimate().toString().equals(tmp.toString())}">
                                <option value="${tmp.toString()}">${tmp.getLabel()}</option>
                            </c:if>
                        </c:forEach>
                    </select>
                </td></tr>
                <tr class="section3"><td class="number">3.4</td><td>Population decline</td><td>
                    <select name="population_PopulationDecline">
                        <c:forEach var="tmp" items="${population_PopulationDecline}">
                            <c:if test="${rlde.getPopulation().getPopulationDecline().toString().equals(tmp.toString())}">
                                <option value="${tmp.toString()}" selected="selected">${tmp.getLabel()}</option>
                            </c:if>
                            <c:if test="${!rlde.getPopulation().getPopulationDecline().toString().equals(tmp.toString())}">
                                <option value="${tmp.toString()}">${tmp.getLabel()}</option>
                            </c:if>
                        </c:forEach>
                    </select>
                </td></tr>
                <tr class="section3"><td class="number">3.5</td><td>Population trend</td><td>
                    <input type="text" name="population_PopulationTrend" value="${rlde.getPopulation().getPopulationTrend()}"/>
                </td></tr>
                <tr class="section3"><td class="number">3.6</td><td>Severely fragmented</td><td>
                    <select name="population_SeverelyFragmented">
                        <c:forEach var="tmp" items="${population_SeverelyFragmented}">
                            <c:if test="${rlde.getPopulation().getSeverelyFragmented().toString().equals(tmp.toString())}">
                                <option value="${tmp.toString()}" selected="selected">${tmp.getLabel()}</option>
                            </c:if>
                            <c:if test="${!rlde.getPopulation().getSeverelyFragmented().toString().equals(tmp.toString())}">
                                <option value="${tmp.toString()}">${tmp.getLabel()}</option>
                            </c:if>
                        </c:forEach>
                    </select>
                </td></tr>
                <tr class="section3"><td class="number">3.7</td><td>Extreme fluctuations</td><td>
                    <select name="population_ExtremeFluctuations">
                        <c:forEach var="tmp" items="${population_ExtremeFluctuations}">
                            <c:if test="${rlde.getPopulation().getExtremeFluctuations().toString().equals(tmp.toString())}">
                                <option value="${tmp.toString()}" selected="selected">${tmp.getLabel()}</option>
                            </c:if>
                            <c:if test="${!rlde.getPopulation().getExtremeFluctuations().toString().equals(tmp.toString())}">
                                <option value="${tmp.toString()}">${tmp.getLabel()}</option>
                            </c:if>
                        </c:forEach>
                    </select>
                </td></tr>
                <tr class="section4"><td class="number" colspan="3">Section 4 - Ecology</td></tr>
                <tr class="section4 textual"><td class="number">4.1</td><td>Habitats and ecology information (textual)</td><td>
                    <textarea rows="6" name="ecology_Description"><c:out value="${rlde.getEcology().getDescription()}"></c:out></textarea>
                </td></tr>
                <tr class="section4"><td class="number">4.2</td><td>Habitat types</td><td>
                    <c:forEach var="tmp" items="${ecology_HabitatTypes}">
                        <c:if test="${habitatTypes.contains(tmp.toString())}">
                            <label><input type="checkbox" name="ecology_HabitatTypes" value="${tmp.toString()}" checked="checked"/> ${tmp.getLabel()}</label>
                        </c:if>
                        <c:if test="${!habitatTypes.contains(tmp.toString())}">
                            <label><input type="checkbox" name="ecology_HabitatTypes" value="${tmp.toString()}"/> ${tmp.getLabel()}</label>
                        </c:if>
                    </c:forEach>
                </td></tr>
                <tr class="section4"><td class="number">4.3</td><td>Life form</td><td>(automatico)</td></tr>
                <tr class="section4"><td class="number">4.4</td><td>Generation length</td><td>
                    <select name="ecology_GenerationLength">
                        <c:forEach var="tmp" items="${ecology_GenerationLength}">
                            <c:if test="${rlde.getEcology().getGenerationLength().toString().equals(tmp.toString())}">
                                <option value="${tmp.toString()}" selected="selected">${tmp.getLabel()}</option>
                            </c:if>
                            <c:if test="${!rlde.getEcology().getGenerationLength().toString().equals(tmp.toString())}">
                                <option value="${tmp.toString()}">${tmp.getLabel()}</option>
                            </c:if>
                        </c:forEach>
                    </select>
                </td></tr>

                <tr class="section5"><td class="number" colspan="3">Section 5 - Uses and trade</td></tr>
                <tr class="section5 textual"><td class="number">5.1</td><td>Uses and trade (textual)</td><td>
                    <textarea rows="6" name="usesAndTrade_Description"><c:out value="${rlde.getUsesAndTrade().getDescription()}"></c:out></textarea>
                </td></tr>
                <tr class="section5"><td class="number">5.2</td><td>Uses</td><td>
                    <c:forEach var="tmp" items="${usesAndTrade_Uses}">
                        <c:if test="${uses.contains(tmp.toString())}">
                            <label><input type="checkbox" name="usesAndTrade_Uses" value="${tmp.toString()}" checked="checked"/> ${tmp.getLabel()}</label>
                        </c:if>
                        <c:if test="${!uses.contains(tmp.toString())}">
                            <label><input type="checkbox" name="usesAndTrade_Uses" value="${tmp.toString()}"/> ${tmp.getLabel()}</label>
                        </c:if>
                    </c:forEach>
                </td></tr>
                <tr class="section5"><td class="number">5.3</td><td>Trade</td><td>
                    <c:if test="${rlde.getUsesAndTrade().isTraded()}">
                        <label><input type="checkbox" name="usesAndTrade_Traded" checked="checked"/> is traded</label>
                    </c:if>
                    <c:if test="${!rlde.getUsesAndTrade().isTraded()}">
                        <label><input type="checkbox" name="usesAndTrade_Traded"/> is traded</label>
                    </c:if>
                </td></tr>
                <tr class="section5"><td class="number">5.4</td><td>Overexploitation</td><td>
                    <select name="usesAndTrade_Overexploitation">
                        <c:forEach var="tmp" items="${usesAndTrade_Overexploitation}">
                            <c:if test="${rlde.getUsesAndTrade().getOverexploitation().toString().equals(tmp.toString())}">
                                <option value="${tmp.toString()}" selected="selected">${tmp.getLabel()}</option>
                            </c:if>
                            <c:if test="${!rlde.getUsesAndTrade().getOverexploitation().toString().equals(tmp.toString())}">
                                <option value="${tmp.toString()}">${tmp.getLabel()}</option>
                            </c:if>
                        </c:forEach>
                    </select>
                </td></tr>

                <tr class="section6"><td class="number" colspan="3">Section 6 - Threats</td></tr>
                <tr class="section6 textual"><td class="number">6.1</td><td>Threat description (textual)</td><td>
                    <textarea rows="6" name="threats_Description"><c:out value="${rlde.getThreats().getDescription()}"></c:out></textarea>
                </td></tr>
                <tr class="section6"><td class="number">6.2</td><td>Threats</td><td>
                    (a fazer...)
                </td></tr>
                <tr class="section6"><td class="number">6.3</td><td>Number of locations</td><td>
                    <input type="text" name="threats_NumberOfLocations" value="${rlde.getThreats().getNumberOfLocations()}"/><br/>
                    (nº de subpops sugerido)
                </td></tr>

                <tr class="section7"><td class="number" colspan="3">Section 7 - Conservation</td></tr>
                <tr class="section7 textual"><td class="number">7.1</td><td>Conservation measures (textual)</td><td>
                    <textarea rows="6" name="conservation_Description"><c:out value="${rlde.getConservation().getDescription()}"></c:out></textarea>
                </td></tr>
                <tr class="section7"><td class="number">7.2</td><td>Conservation plans</td><td>
                    <select name="conservation_ConservationPlans">
                        <c:forEach var="tmp" items="${conservation_ConservationPlans}">
                            <c:if test="${rlde.getConservation().getConservationPlans().toString().equals(tmp.toString())}">
                                <option value="${tmp.toString()}" selected="selected">${tmp.getLabel()}</option>
                            </c:if>
                            <c:if test="${!rlde.getConservation().getConservationPlans().toString().equals(tmp.toString())}">
                                <option value="${tmp.toString()}">${tmp.getLabel()}</option>
                            </c:if>
                        </c:forEach>
                    </select>
                </td></tr>
                <tr class="section7"><td class="number">7.3</td><td><i>Ex-situ</i> conservation</td><td>
                    <select name="conservation_ExSituConservation">
                        <c:forEach var="tmp" items="${conservation_ExSituConservation}">
                            <c:if test="${rlde.getConservation().getExSituConservation().toString().equals(tmp.toString())}">
                                <option value="${tmp.toString()}" selected="selected">${tmp.getLabel()}</option>
                            </c:if>
                            <c:if test="${!rlde.getConservation().getExSituConservation().toString().equals(tmp.toString())}">
                                <option value="${tmp.toString()}">${tmp.getLabel()}</option>
                            </c:if>
                        </c:forEach>
                    </select>
                </td></tr>
                <tr class="section7"><td class="number">7.4</td><td>Occurrence in protected areas</td><td>
                (automatico)
                </td></tr>
                <tr class="section7"><td class="number">7.5</td><td>Proposed conservation actions</td><td>
                    <c:forEach var="tmp" items="${conservation_ProposedConservationActions}">
                        <c:if test="${proposedConservationActions.contains(tmp.toString())}">
                            <label><input type="checkbox" name="conservation_ProposedConservationActions" value="${tmp.toString()}" checked="checked"/> ${tmp.getLabel()}</label>
                        </c:if>
                        <c:if test="${!proposedConservationActions.contains(tmp.toString())}">
                            <label><input type="checkbox" name="conservation_ProposedConservationActions" value="${tmp.toString()}"/> ${tmp.getLabel()}</label>
                        </c:if>
                    </c:forEach>
                </td></tr>

                <tr class="section8"><td class="number" colspan="3">Section 8 - Bibliographic references</td></tr>
                <tr class="section8"><td class="number">8.1</td><td>Reference list</td><td>
                (a fazer)
                </td></tr>
            </table>
        </form>
    </c:when>

    <c:when test="${what=='taxonrecords'}">
        <c:if test="${sessionScope.user == null}">
            <div class="warning"><b>You&#8217;re not authorized to enter this page</b></div>
        </c:if>
        <c:if test="${sessionScope.user != null && sessionScope.user.getRole() >= 30}">
            <h1>${taxon.getFullName(true)}</h1>
            <c:if test="${occurrences == null}">
                <div class="warning"><b>Warning</b><br/>This taxon has no correspondence in Flora-On, please contact the checklist administrator</div>
            </c:if>
            <h2>${occurrences.size()} occurrences</h2>
            <table class="sortable">
                <thead>
                    <tr><th>Record ID</th><th>Taxon</th><th>Latitude</th><th>Longitude</th><th>Year</th><th>Month</th>
                    <th>Day</th><th>Author</th><th style="width:180px">Notes</th><th>Precision</th><th>ID in doubt?</th><th>In flower?</th></tr>
                </thead>
                <c:forEach var="occ" items="${occurrences.iterator()}">
                    <tr>
                        <td>${occ.getId_reg()}</td>
                        <td><i>${occ.getGenus()} ${occ.getSpecies()} ${occ.getInfrataxon() == null ? '' : occ.getInfrataxon()}</i></td>
                        <td><fmt:formatNumber value="${occ.getLatitude()}" maxFractionDigits="4"/></td>
                        <td><fmt:formatNumber value="${occ.getLongitude()}" maxFractionDigits="4"/></td>
                        <td>${occ.getYear()}</td>
                        <td>${occ.getMonth()}</td>
                        <td>${occ.getDay()}</td>
                        <td>${occ.getAuthor()}</td>
                        <td style="width:180px">${occ.getNotes()}</td>
                        <td>${occ.getPrecision()}</td>
                        <td>${occ.getConfidence() ? '' : 'Yes'}</td>
                        <td>${occ.getFlowering() ? 'Yes' : ''}</td>
                    </tr>
                </c:forEach>
            </table>
        </c:if>
    </c:when>
    </c:choose>
    </div>
</div>

</body>
</html>
