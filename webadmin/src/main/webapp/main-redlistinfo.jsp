<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html>
<head>
	<title>Red List data portal</title>
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
<div id="title"><a href="/floraon/">Red List data portal</a></div>
<div id="main-holder">
    <div id="left-bar">
        <ul>
            <li><a href="?w=main">Taxon index</a></li>
            <c:if test="${user.canMANAGE_REDLIST_USERS()}">
                <li><a href="?w=users">Manage users</a></li>
            </c:if>
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
        <h1>Taxon index</h1>
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
                <tr class="textual"><td colspan="3" id="sheet-header" class="title">
                    <h1><i>${taxon.getName()}</i></h1>
                    <div class="redlistcategory assess_${rlde.getAssessment().getCategory().toString()}"><h1>${rlde.getAssessment().getCategory().toString()}</h1><p>${rlde.getAssessment().getCategory().getLabel()}</p></div>
                    <div id="header-buttons">
                        <c:if test="${user.canVIEW_FULL_SHEET()}">
                            <div class="wordtag togglebutton" id="summary_toggle">summary</div>
                        </c:if>
                        <c:if test="${user.canVIEW_OCCURRENCES()}">
                            <div class="wordtag togglebutton">
                                <a href="?w=taxonrecords&id=${taxon.getIDURLEncoded()}">view occurrences</a>
                            </div>
                        </c:if>
                    </div>
                </td></tr>
                <c:if test="${user.canVIEW_FULL_SHEET()}">
                    <tr class="section1"><td class="title" colspan="3">Section 1 - General info</td></tr>
                    <tr class="section1">
                        <td class="title">1.1</td>
                        <td>Name</td><td><i>${taxon.getName()}</i>
                            <c:if test="${user.canEDIT_ANY_FIELD()}">
                                <input type="submit" value=""/>
                            </c:if>
                        </td>
                    </tr>
                    <tr class="section1"><td class="title">1.2</td><td>Authority</td><td>${taxon.getAuthor()}</td></tr>
                    <tr class="section1"><td class="title">1.3</td><td>Synonyms</td><td>
                        <ul>
                        <c:forEach var="synonym" items="${synonyms}">
                            <li data-key="${synonym.getID()}"><c:out value="${synonym.getFullName()}"></c:out></li>
                        </c:forEach>
                        </ul>
                    </td></tr>
                    <tr class="section1"><td class="title">1.4</td><td>Taxonomic problems</td><td>
                        <c:if test="${user.canEDIT_1_4()}">
                            <table>
                                <tr><td colspan="2"><label>
                                    <c:if test="${rlde.getHasTaxonomicProblems()}">
                                        <input type="checkbox" name="hasTaxonomicProblems" checked="checked">
                                    </c:if>
                                    <c:if test="${!rlde.getHasTaxonomicProblems()}">
                                        <input type="checkbox" name="hasTaxonomicProblems">
                                    </c:if>
                                    has taxonomic problems</label></td>
                                </tr>
                                <tr><td>Problem description</td>
                                <td><textarea rows="3" name="taxonomicProblemDescription"><c:out value="${rlde.getTaxonomicProblemDescription()}"></c:out></textarea></td></tr>
                            </table>
                        </c:if>
                        <c:if test="${!user.canEDIT_1_4()}">
                            <table>
                                <tr><td colspan="2">Has taxonomic problems: ${rlde.getHasTaxonomicProblems() ? "Yes" : "No"}</td></tr>
                                <tr><td>Problem description</td>
                                <td><c:out value="${rlde.getTaxonomicProblemDescription()}"></c:out></td></tr>
                            </table>
                        </c:if>
                    </td></tr>
                    <tr class="section2"><td class="title" colspan="3">Section 2 - Geographical Distribution</td></tr>
                </c:if>
                <tr class="section2 textual"><td class="title">2.1</td><td>Distribution (textual)</td><td>
                    <table>
                        <tr><td style="width:auto">
                        <c:if test="${user.canEDIT_SECTION2() || user.canEDIT_ALL_TEXTUAL()}">
                            <textarea rows="6" name="geographicalDistribution_Description"><c:out value="${rlde.getGeographicalDistribution().getDescription()}"></c:out></textarea>
                        </c:if>
                        <c:if test="${!user.canEDIT_SECTION2() && !user.canEDIT_ALL_TEXTUAL()}">
                            <c:out value="${rlde.getGeographicalDistribution().getDescription()}"></c:out>
                        </c:if>
                        </td>
                        <td style="width:0">${svgmap}</td>
                        </tr>
                    </table>
                </td></tr>
                <c:if test="${user.canVIEW_FULL_SHEET()}">
                    <tr class="section2"><td class="title">2.2</td><td>Extent Of Occurrence<br/>(EOO)</td><td>
                        <c:if test="${occurrences == null}">
                            No correspondence in Flora-On
                        </c:if>
                        <c:if test="${occurrences != null}">
                            <c:if test="${EOO == null}">
                                Not applicable (${occurrences.size()} occurrences, ${nclusters} locations)
                            </c:if>
                            <c:if test="${EOO != null}">
                                <c:if test="${warning != null}"><div class="warning">${warning}</div></c:if>
                                <b><fmt:formatNumber value="${EOO}" maxFractionDigits="3"/></b> km<sup>2</sup> (${occurrences.size()} occurrences, ${nclusters} locations)
                            </c:if>
                        </c:if>
                    </td></tr>
                    <tr class="section2"><td class="title">2.3</td><td>Area Of Occupancy<br/>(AOO)</td><td>
                        <c:if test="${occurrences == null}">
                            No correspondence in Flora-On
                        </c:if>
                        <c:if test="${occurrences != null}">
                            <b><fmt:formatNumber value="${AOO}" maxFractionDigits="4"/></b> km<sup>2</sup> (${nquads} ${sizeofsquare}x${sizeofsquare} km squares)
                        </c:if>
                    </td></tr>
                    <tr class="section2"><td class="title">2.4</td><td>Decline in distribution</td><td>
                        <c:if test="${user.canEDIT_SECTION2()}">
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
                        </c:if>
                        <c:if test="${!user.canEDIT_SECTION2()}">
                            ${rlde.getGeographicalDistribution().getDeclineDistribution().getLabel()}
                        </c:if>
                    </td></tr>
                    <tr class="section2"><td class="title">2.5</td><td>Elevation</td><td>
                        <c:if test="${user.canEDIT_SECTION2()}">
                            <input name="geographicalDistribution_ElevationRange" type="number" value="${rlde.getGeographicalDistribution().getElevationRange()[0]}"/>
                            <input name="geographicalDistribution_ElevationRange" type="number" value="${rlde.getGeographicalDistribution().getElevationRange()[1]}"/>
                        </c:if>
                        <c:if test="${!user.canEDIT_SECTION2()}">
                            ${rlde.getGeographicalDistribution().getElevationRange()[0]} - ${rlde.getGeographicalDistribution().getElevationRange()[1]}
                        </c:if>
                    </td></tr>
                    <tr class="section3"><td class="title" colspan="3">Section 3 - Population</td></tr>
                </c:if>
                <tr class="section3 textual"><td class="title">3.1</td><td>Population information (textual)</td><td>
                    <c:if test="${user.canEDIT_SECTION3() || user.canEDIT_ALL_TEXTUAL()}">
                        <textarea rows="6" name="population_Description"><c:out value="${rlde.getPopulation().getDescription()}"></c:out></textarea>
                    </c:if>
                    <c:if test="${!user.canEDIT_SECTION3() && !user.canEDIT_ALL_TEXTUAL()}">
                        <c:out value="${rlde.getPopulation().getDescription()}"></c:out>
                    </c:if>
                </td></tr>
                <c:if test="${user.canVIEW_FULL_SHEET()}">
                    <tr class="section3"><td class="title">3.2</td><td>Nº of mature individuals</td><td>
                        <c:if test="${user.canEDIT_SECTION3()}">
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
                                <tr><td>Exact number</td><td><input type="number" min="0" name="population_NrMatureIndividualsExact" value="${rlde.getPopulation().getNrMatureIndividualsExact()}"/></td></tr>
                                <tr><td>Textual description</td><td><input type="text" class="longbox" name="population_NrMatureIndividualsDescription" value="${rlde.getPopulation().getNrMatureIndividualsDescription()}"/></td></tr>
                            </table>
                        </c:if>
                        <c:if test="${!user.canEDIT_SECTION3()}">
                            <table>
                                <tr><td>Category</td><td>${rlde.getPopulation().getNrMatureIndividualsCategory().getLabel()}</td></tr>
                                <tr><td>Exact number</td><td>${rlde.getPopulation().getNrMatureIndividualsExact()}</td></tr>
                                <tr><td>Textual description</td><td>${rlde.getPopulation().getNrMatureIndividualsDescription()}</td></tr>
                            </table>
                        </c:if>
                    </td></tr>
                    <tr class="section3"><td class="title">3.3</td><td>Type of estimate</td><td>
                        <c:if test="${user.canEDIT_SECTION3()}">
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
                        </c:if>
                        <c:if test="${!user.canEDIT_SECTION3()}">
                            ${rlde.getPopulation().getTypeOfEstimate().getLabel()}
                        </c:if>
                    </td></tr>
                    <tr class="section3"><td class="title">3.4</td><td>Population decline</td><td>
                        <c:if test="${user.canEDIT_SECTION3()}">
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
                        </c:if>
                        <c:if test="${!user.canEDIT_SECTION3()}">
                            ${rlde.getPopulation().getPopulationDecline().getLabel()}
                        </c:if>
                    </td></tr>
                    <tr class="section3"><td class="title">3.5</td><td>Population trend</td><td>
                        <c:if test="${user.canEDIT_SECTION3()}">
                            <input type="number" name="population_PopulationTrend" value="${rlde.getPopulation().getPopulationTrend()}"/>
                        </c:if>
                        <c:if test="${!user.canEDIT_SECTION3()}">
                            ${rlde.getPopulation().getPopulationTrend()}
                        </c:if>
                    </td></tr>
                    <tr class="section3"><td class="title">3.6</td><td>Severely fragmented</td><td>
                        <c:if test="${user.canEDIT_SECTION3()}">
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
                        </c:if>
                        <c:if test="${!user.canEDIT_SECTION3()}">
                            ${rlde.getPopulation().getSeverelyFragmented().getLabel()}
                        </c:if>
                    </td></tr>
                    <tr class="section3"><td class="title">3.7</td><td>Extreme fluctuations</td><td>
                        <c:if test="${user.canEDIT_SECTION3()}">
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
                        </c:if>
                        <c:if test="${!user.canEDIT_SECTION3()}">
                            ${rlde.getPopulation().getExtremeFluctuations().getLabel()}
                        </c:if>
                    </td></tr>
                    <tr class="section4"><td class="title" colspan="3">Section 4 - Ecology</td></tr>
                </c:if>
                <tr class="section4 textual"><td class="title">4.1</td><td>Habitats and ecology information (textual)</td><td>
                    <c:if test="${user.canEDIT_SECTION4() || user.canEDIT_ALL_TEXTUAL()}">
                        <textarea rows="6" name="ecology_Description"><c:out value="${rlde.getEcology().getDescription()}"></c:out></textarea>
                    </c:if>
                    <c:if test="${!user.canEDIT_SECTION4() && !user.canEDIT_ALL_TEXTUAL()}">
                        <c:out value="${rlde.getEcology().getDescription()}"></c:out>
                    </c:if>
                </td></tr>
                <c:if test="${user.canVIEW_FULL_SHEET()}">
                    <tr class="section4"><td class="title">4.2</td><td>Habitat types</td><td>
                        <c:if test="${user.canEDIT_SECTION4()}">
                            <c:forEach var="tmp" items="${ecology_HabitatTypes}">
                                <c:if test="${habitatTypes.contains(tmp.toString())}">
                                    <label><input type="checkbox" name="ecology_HabitatTypes" value="${tmp.getLabel()}" checked="checked"/> ${tmp.getLabel()}</label>
                                </c:if>
                                <c:if test="${!habitatTypes.contains(tmp.toString())}">
                                    <label><input type="checkbox" name="ecology_HabitatTypes" value="${tmp.getLabel()}"/> ${tmp.getLabel()}</label>
                                </c:if>
                            </c:forEach>
                        </c:if>
                        <c:if test="${!user.canEDIT_SECTION4()}">
                            <c:forEach var="tmp" items="${habitatTypes}">
                                <div class="wordtag">${tmp}</div>
                            </c:forEach>
                        </c:if>
                    </td></tr>
                    <tr class="section4"><td class="title">4.3</td><td>Life form</td><td>(automatico)</td></tr>
                    <tr class="section4"><td class="title">4.4</td><td>Generation length</td><td>
                        <c:if test="${user.canEDIT_SECTION4()}">
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
                        </c:if>
                        <c:if test="${!user.canEDIT_SECTION4()}">
                            ${rlde.getEcology().getGenerationLength().getLabel()}
                        </c:if>
                    </td></tr>
                    <tr class="section5"><td class="title" colspan="3">Section 5 - Uses and trade</td></tr>
                </c:if>
                <tr class="section5 textual"><td class="title">5.1</td><td>Uses and trade (textual)</td><td>
                    <c:if test="${user.canEDIT_SECTION5() || user.canEDIT_ALL_TEXTUAL()}">
                        <textarea rows="6" name="usesAndTrade_Description"><c:out value="${rlde.getUsesAndTrade().getDescription()}"></c:out></textarea>
                    </c:if>
                    <c:if test="${!user.canEDIT_SECTION5() && !user.canEDIT_ALL_TEXTUAL()}">
                        <c:out value="${rlde.getUsesAndTrade().getDescription()}"></c:out>
                    </c:if>
                </td></tr>
                <c:if test="${user.canVIEW_FULL_SHEET()}">
                    <tr class="section5"><td class="title">5.2</td><td>Uses</td><td>
                        <c:if test="${user.canEDIT_SECTION5()}">
                            <c:forEach var="tmp" items="${usesAndTrade_Uses}">
                                <c:if test="${uses.contains(tmp.toString())}">
                                    <label><input type="checkbox" name="usesAndTrade_Uses" value="${tmp.toString()}" checked="checked"/> ${tmp.getLabel()}</label>
                                </c:if>
                                <c:if test="${!uses.contains(tmp.toString())}">
                                    <label><input type="checkbox" name="usesAndTrade_Uses" value="${tmp.toString()}"/> ${tmp.getLabel()}</label>
                                </c:if>
                            </c:forEach>
                        </c:if>
                        <c:if test="${!user.canEDIT_SECTION5()}">
                            <c:forEach var="tmp" items="${uses}">
                                <div class="wordtag">${tmp.getLabel()}</div>
                            </c:forEach>
                        </c:if>
                    </td></tr>
                    <tr class="section5"><td class="title">5.3</td><td>Trade</td><td>
                        <c:if test="${user.canEDIT_SECTION5()}">
                            <c:if test="${rlde.getUsesAndTrade().isTraded()}">
                                <label><input type="checkbox" name="usesAndTrade_Traded" checked="checked"/> is traded</label>
                            </c:if>
                            <c:if test="${!rlde.getUsesAndTrade().isTraded()}">
                                <label><input type="checkbox" name="usesAndTrade_Traded"/> is traded</label>
                            </c:if>
                        </c:if>
                        <c:if test="${!user.canEDIT_SECTION5()}">
                            Is traded: ${rlde.getUsesAndTrade().isTraded() ? "Yes" : "No"}
                        </c:if>
                    </td></tr>
                    <tr class="section5"><td class="title">5.4</td><td>Overexploitation</td><td>
                        <c:if test="${user.canEDIT_SECTION5()}">
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
                        </c:if>
                        <c:if test="${!user.canEDIT_SECTION5()}">
                            ${rlde.getUsesAndTrade().getOverexploitation().getLabel()}
                        </c:if>
                    </td></tr>
                    <tr class="section6"><td class="title" colspan="3">Section 6 - Threats</td></tr>
                </c:if>
                <tr class="section6 textual"><td class="title">6.1</td><td>Threat description (textual)</td><td>
                    <c:if test="${user.canEDIT_SECTION6() || user.canEDIT_ALL_TEXTUAL()}">
                        <textarea rows="6" name="threats_Description"><c:out value="${rlde.getThreats().getDescription()}"></c:out></textarea>
                    </c:if>
                    <c:if test="${!user.canEDIT_SECTION6() && !user.canEDIT_ALL_TEXTUAL()}">
                        <c:out value="${rlde.getThreats().getDescription()}"></c:out>
                    </c:if>
                </td></tr>
                <c:if test="${user.canVIEW_FULL_SHEET()}">
                    <tr class="section6"><td class="title">6.2</td><td>Threats</td><td>
                        (a fazer...)
                    </td></tr>
                    <tr class="section6"><td class="title">6.3</td><td>Number of locations</td><td>
                        <c:if test="${user.canEDIT_SECTION6()}">
                            <input type="number" min="0" name="threats_NumberOfLocations" value="${rlde.getThreats().getNumberOfLocations()}"/><br/>
                            ${nclusters} locations (automatic estimate)
                        </c:if>
                        <c:if test="${!user.canEDIT_SECTION6()}">
                            ${rlde.getThreats().getNumberOfLocations()}<br/>
                            ${nclusters} locations (automatic estimate)
                        </c:if>
                    </td></tr>

                    <tr class="section7"><td class="title" colspan="3">Section 7 - Conservation</td></tr>
                </c:if>
                <tr class="section7 textual"><td class="title">7.1</td><td>Conservation measures (textual)</td><td>
                    <c:if test="${user.canEDIT_SECTION7() || user.canEDIT_ALL_TEXTUAL()}">
                        <textarea rows="6" name="conservation_Description"><c:out value="${rlde.getConservation().getDescription()}"></c:out></textarea>
                    </c:if>
                    <c:if test="${!user.canEDIT_SECTION7() && !user.canEDIT_ALL_TEXTUAL()}">
                        <c:out value="${rlde.getConservation().getDescription()}"></c:out>
                    </c:if>
                </td></tr>
                <c:if test="${user.canVIEW_FULL_SHEET()}">
                    <tr class="section7"><td class="title">7.2</td><td>Conservation plans</td><td>
                        <c:if test="${user.canEDIT_SECTION7()}">
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
                        </c:if>
                        <c:if test="${!user.canEDIT_SECTION7()}">
                            ${rlde.getConservation().getConservationPlans().getLabel()}
                        </c:if>
                    </td></tr>
                    <tr class="section7"><td class="title">7.3</td><td><i>Ex-situ</i> conservation</td><td>
                        <c:if test="${user.canEDIT_SECTION7()}">
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
                        </c:if>
                        <c:if test="${!user.canEDIT_SECTION7()}">
                            ${rlde.getConservation().getExSituConservation().getLabel()}
                        </c:if>
                    </td></tr>
                    <tr class="section7"><td class="title">7.4</td><td>Occurrence in protected areas</td><td>
                    (automatico)
                    </td></tr>
                    <tr class="section7"><td class="title">7.5</td><td>Proposed conservation actions</td><td>
                        <c:if test="${user.canEDIT_SECTION7()}">
                            <c:forEach var="tmp" items="${conservation_ProposedConservationActions}">
                                <c:if test="${proposedConservationActions.contains(tmp.toString())}">
                                    <label><input type="checkbox" name="conservation_ProposedConservationActions" value="${tmp.toString()}" checked="checked"/> ${tmp.getLabel()}</label>
                                </c:if>
                                <c:if test="${!proposedConservationActions.contains(tmp.toString())}">
                                    <label><input type="checkbox" name="conservation_ProposedConservationActions" value="${tmp.toString()}"/> ${tmp.getLabel()}</label>
                                </c:if>
                            </c:forEach>
                        </c:if>
                        <c:if test="${!user.canEDIT_SECTION7()}">
                            <c:forEach var="tmp" items="${proposedConservationActions}">
                                <div class="wordtag">${tmp}</div>
                            </c:forEach>
                        </c:if>
                    </td></tr>

                    <tr class="section8"><td class="title" colspan="3">Section 8 - Bibliographic references</td></tr>
                    <tr class="section8"><td class="title">8.1</td><td>Reference list</td><td>
                    (a fazer)
                    </td></tr>

                    <tr class="section9"><td class="title" colspan="3">Section 9 - Red List Assessment</td></tr>
                    <tr class="section9"><td class="title">9.1</td><td>Category</td><td>
                        <div id="redlistcategories">
                            <c:if test="${user.canEDIT_9_1_2_3_5()}">
                                <c:forEach var="tmp" items="${assessment_Category}">
                                    <c:if test="${rlde.getAssessment().getCategory().toString().equals(tmp.toString())}">
                                        <input type="radio" name="assessment_Category" value="${tmp.toString()}" id="assess_${tmp.toString()}" checked="checked">
                                    </c:if>
                                    <c:if test="${!rlde.getAssessment().getCategory().toString().equals(tmp.toString())}">
                                        <input type="radio" name="assessment_Category" value="${tmp.toString()}" id="assess_${tmp.toString()}">
                                    </c:if>
                                    <label for="assess_${tmp.toString()}"><h1>${tmp.toString()}</h1><p>${tmp.getLabel()}</p></label>
                                    <c:if test="${tmp == 'RE' || tmp == 'LC'}"><br/></c:if>
                                </c:forEach>
                            </c:if>
                            <c:if test="${!user.canEDIT_9_1_2_3_5()}">
                                <div class="redlistcategory assess_${rlde.getAssessment().getCategory().toString()}"><h1>${rlde.getAssessment().getCategory().toString()}</h1><p>${rlde.getAssessment().getCategory().getLabel()}</p></div>
                            </c:if>
                        </div>
                    </td></tr>
                    <tr class="section9"><td class="title">9.2</td><td>Criteria</td><td>
                        <c:if test="${user.canEDIT_9_1_2_3_5()}">
                            <input name="assessment_Criteria" type="text" class="longbox" value="${rlde.getAssessment().getCriteria()}"/>
                        </c:if>
                        <c:if test="${!user.canEDIT_9_1_2_3_5()}">
                            ${rlde.getAssessment().getCriteria()}
                        </c:if>
                    </td></tr>
                </c:if>
                <tr class="section9 textual"><td class="title">9.3</td><td>Assessment justification</td><td>
                    <c:if test="${user.canEDIT_9_1_2_3_5()}">
                        <textarea rows="6" name="assessment_Justification"><c:out value="${rlde.getAssessment().getJustification()}"></c:out></textarea>
                    </c:if>
                    <c:if test="${!user.canEDIT_9_1_2_3_5()}">
                        ${rlde.getAssessment().getJustificationHTML()}
                    </c:if>
                </td></tr>
                <c:if test="${user.canVIEW_FULL_SHEET()}">
                    <tr class="section9"><td class="title">9.4</td><td>Authors</td>
                    <c:if test="${user.canEDIT_9_4_9_7()}">
                        <td class="multiplechooser">
                            <c:forEach var="tmp" items="${allUsers}">
                                <c:if test="${authors.contains(tmp.getID())}">
                                    <input type="checkbox" name="assessment_Authors" id="au_${tmp.getNameASCii()}" value="${tmp.getID()}" checked="checked"/><label for="au_${tmp.getNameASCii()}" class="">${tmp.getName()}</label>
                                </c:if>
                                <c:if test="${!authors.contains(tmp.getID())}">
                                    <input type="checkbox" name="assessment_Authors" id="au_${tmp.getNameASCii()}" value="${tmp.getID()}"/><label for="au_${tmp.getNameASCii()}" class="">${tmp.getName()}</label>
                                </c:if>
                            </c:forEach>
                        </td>
                    </c:if>
                    <c:if test="${!user.canEDIT_9_4_9_7()}">
                        <td>
                            <c:forEach var="tmp" items="${rlde.getAssessment().getAuthors()}">
                                <div class="wordtag">${userMap.get(tmp)}</div>
                            </c:forEach>
                        </td>
                    </c:if>
                    </tr>
                    <tr class="section9"><td class="title">9.4.1</td><td>Collaborators</td><td>
                    <c:if test="${user.canEDIT_9_4_9_7()}">
                        <input name="assessment_Collaborators" type="text" class="longbox" value="${rlde.getAssessment().getCollaborators()}"/>
                    </c:if>
                    <c:if test="${!user.canEDIT_9_4_9_7()}">
                        ${rlde.getAssessment().getCollaborators()}
                    </c:if>
                    </td></tr>
                    <tr class="section9"><td class="title">9.5</td><td>Evaluator</td>
                    <c:if test="${user.canEDIT_9_4_9_7()}">
                        <td class="multiplechooser">
                            <c:forEach var="tmp" items="${allUsers}">
                                <c:if test="${evaluator.contains(tmp.getID())}">
                                    <input type="checkbox" name="assessment_Evaluator" id="ev_${tmp.getNameASCii()}" value="${tmp.getID()}" checked="checked"/><label for="ev_${tmp.getNameASCii()}" class="">${tmp.getName()}</label>
                                </c:if>
                                <c:if test="${!evaluator.contains(tmp.getID())}">
                                    <input type="checkbox" name="assessment_Evaluator" id="ev_${tmp.getNameASCii()}" value="${tmp.getID()}"/><label for="ev_${tmp.getNameASCii()}" class="">${tmp.getName()}</label>
                                </c:if>
                            </c:forEach>
                        </td>
                    </c:if>
                    <c:if test="${!user.canEDIT_9_4_9_7()}">
                        <td>
                            <c:forEach var="tmp" items="${rlde.getAssessment().getEvaluator()}">
                                <div class="wordtag">${userMap.get(tmp)}</div>
                            </c:forEach>
                        </td>
                    </c:if>
                    </tr>
                    <tr class="section9"><td class="title">9.6</td><td>Reviewer</td>
                    <c:if test="${user.canEDIT_9_4_9_7()}">
                        <td class="multiplechooser">
                            <c:forEach var="tmp" items="${allUsers}">
                                <c:if test="${reviewer.contains(tmp.getID())}">
                                    <input type="checkbox" name="assessment_Reviewer" id="re_${tmp.getNameASCii()}" value="${tmp.getID()}" checked="checked"/><label for="re_${tmp.getNameASCii()}" class="">${tmp.getName()}</label>
                                </c:if>
                                <c:if test="${!reviewer.contains(tmp.getID())}">
                                    <input type="checkbox" name="assessment_Reviewer" id="re_${tmp.getNameASCii()}" value="${tmp.getID()}"/><label for="re_${tmp.getNameASCii()}" class="">${tmp.getName()}</label>
                                </c:if>
                            </c:forEach>
                        </td>
                    </c:if>
                    <c:if test="${!user.canEDIT_9_4_9_7()}">
                        <td>
                            <c:forEach var="tmp" items="${rlde.getAssessment().getReviewer()}">
                                <div class="wordtag">${userMap.get(tmp)}</div>
                            </c:forEach>
                        </td>
                    </c:if>
                    </tr>
                    <tr class="section9"><td class="title">9.7</td><td>Assessment status</td><td>
                        <c:if test="${user.canEDIT_9_4_9_7()}">
                            <select name="assessment_AssessmentStatus">
                                <c:forEach var="tmp" items="${assessment_AssessmentStatus}">
                                    <c:if test="${rlde.getAssessment().getAssessmentStatus().toString().equals(tmp.toString())}">
                                        <option value="${tmp.toString()}" selected="selected">${tmp.getLabel()}</option>
                                    </c:if>
                                    <c:if test="${!rlde.getAssessment().getAssessmentStatus().toString().equals(tmp.toString())}">
                                        <option value="${tmp.toString()}">${tmp.getLabel()}</option>
                                    </c:if>
                                </c:forEach>
                            </select>
                        </c:if>
                        <c:if test="${!user.canEDIT_9_4_9_7()}">
                            ${rlde.getAssessment().getAssessmentStatus().getLabel()}
                        </c:if>
                    </td></tr>
                </c:if>
            </table>
        </form>
    </c:when>

    <c:when test="${what=='taxonrecords'}">
        <c:if test="${!user.canVIEW_OCCURRENCES()}">
            <div class="warning"><b>You&#8217;re not authorized to enter this page</b></div>
        </c:if>
        <c:if test="${user.canVIEW_OCCURRENCES()}">
            <h1>${taxon.getFullName(true)}</h1>
            <c:if test="${occurrences == null}">
                <div class="warning"><b>Warning</b><br/>This taxon has no correspondence in Flora-On, please contact the checklist administrator</div>
            </c:if>
            <h2>${occurrences.size()} occurrences</h2>
            <table class="sortable smalltext" id="recordtable">
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

    <c:when test="${what=='users'}">
        <c:if test="${!user.canMANAGE_REDLIST_USERS()}">
            <div class="warning"><b>You&#8217;re not authorized to enter this page</b></div>
        </c:if>
        <c:if test="${user.canMANAGE_REDLIST_USERS()}">
            <h1>User management</h1>
            <h2>Create new user</h2>
            <form class="poster" data-path="/floraon/admin/createuser">
                <table>
                    <tr><td class="title">Username</td><td><input type="text" name="userName"/></td></tr>
                    <tr><td class="title">Person name</td><td><input type="text" name="name"/></td></tr>
                    <tr>
                        <td class="title">Privileges</td>
                        <td class="multiplechooser">
                            <c:forEach var="tmp" items="${redlistprivileges}">
                                <input type="checkbox" name="${tmp}" id="priv_${tmp}"/><label for="priv_${tmp}" class="">${tmp}</label>
                            </c:forEach>
                        </td>
                    </tr>
                </table>
                <input type="submit" value="Create" class="textbutton"/>
            </form>
            <h2>Existing users</h2>
            <table>
                <tr><th>Name</th><th>Privileges</th><th></th></tr>
                <c:forEach var="tmp" items="${users}">
                    <c:if test="${user.getUserType() == 'ADMINISTRATOR' || (user.getUserType() != 'ADMINISTRATOR' && tmp.getUserType() != 'ADMINISTRATOR')}">
                    <tr><td>${tmp.getName()}</td>
                        <td>
                        <c:forEach var="tmp1" items="${tmp.getPrivileges()}">
                            <div class="wordtag">${tmp1}</div>
                        </c:forEach>
                        </td>
                        <td><a href="?w=edituser&user=${tmp.getID()}">edit user</a></td>
                    </tr>
                    </c:if>
                </c:forEach>
            </table>
        </c:if>
    </c:when>
    <c:when test="${what=='edituser'}">
        <c:if test="${!user.canMANAGE_REDLIST_USERS()}">
            <div class="warning"><b>You&#8217;re not authorized to enter this page</b></div>
        </c:if>
        <c:if test="${user.canMANAGE_REDLIST_USERS()}">
            <c:if test="${requesteduser == null}">
                <h1>User not found</h1>
                <h2><a href="?w=users">go back</a></h2>
            </c:if>
            <c:if test="${requesteduser != null}">
                <h1>${requesteduser.getFullName()} <span class="info">${requesteduser.getUserType()} ${requesteduser.getID()}</span></h1>
                <form class="poster" data-path="/floraon/admin/deleteuser" style="float:right" data-callback="?w=users">
                    <input type="hidden" name="databaseId" value="${requesteduser.getID()}"/>
                    <input type="submit" value="Delete user" class="textbutton"/>
                </form>
                <h2>Edit user</h2>
                <form class="poster" data-path="/floraon/admin/updateuser" data-callback="?w=users">
                    <input type="hidden" name="databaseId" value="${requesteduser.getID()}"/>
                    <input type="hidden" name="userType" value="${requesteduser.getUserType()}"/>
                    <table>
                        <tr><td class="title">Username</td><td><input type="text" name="userName" value="${requesteduser.getUserName()}"/></td></tr>
                        <tr><td class="title">Person name</td><td><input type="text" name="name" value="${requesteduser.getFullName()}"/></td></tr>
                        <!--<tr><td class="title">Password</td><td></td></tr>-->
                        <tr>
                            <td class="title">Privileges</td>
                            <td class="multiplechooser">
                                <c:forEach var="tmp" items="${redlistprivileges}">
                                    <c:if test="${requesteduser.hasPrivilege(tmp)}">
                                        <input type="checkbox" name="${tmp}" id="priv_${tmp}" checked="checked"/><label for="priv_${tmp}" class="">${tmp}</label>
                                    </c:if>
                                    <c:if test="${!requesteduser.hasPrivilege(tmp)}">
                                        <input type="checkbox" name="${tmp}" id="priv_${tmp}"/><label for="priv_${tmp}" class="">${tmp}</label>
                                    </c:if>
                                </c:forEach>
                            </td>
                        </tr>
                    </table>
                    <input type="submit" value="Update" class="textbutton"/>
                </form>
            </c:if>
        </c:if>
    </c:when>
    </c:choose>
    </div>
</div>

</body>
</html>
