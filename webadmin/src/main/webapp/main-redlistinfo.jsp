<%@ page pageEncoding="UTF-8" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page session="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="language" value="${not empty param.language ? param.language : not empty language ? language : pageContext.response.locale}" scope="request" />
<fmt:setLocale value="${language}" />
<fmt:setBundle basename="pt.floraon.redlistdata.fieldValues" />
<!DOCTYPE html>
<html>
<head>
	<title>Red List data portal</title>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
	<link href='http://fonts.googleapis.com/css?family=Lato:300' rel='stylesheet' type='text/css'>
	<link rel="stylesheet" type="text/css" href="/floraon/base.css?nocache=${uuid}"/>
	<link rel="stylesheet" type="text/css" href="/floraon/redlist.css?nocache=${uuid}"/>
	<script type="text/javascript" src="/floraon/sorttable.js"></script>
	<script type="text/javascript" src="/floraon/basefunctions.js?nocache=${uuid}"></script>
	<script type="text/javascript" src="/floraon/ajaxforms.js"></script>
	<script type="text/javascript" src="/floraon/suggestions.js?nocache=${uuid}"></script>
	<script type="text/javascript" src="/floraon/redlistadmin.js?nocache=${uuid}"></script>
</head>
<body>
<div id="title"><a href="/floraon/"><fmt:message key="DataSheet.title"/></a></div>
<div id="main-holder">
    <div id="left-bar">
        <ul>
            <li><a href="?w=main">Taxon index</a></li>
            <c:if test="${user.canMANAGE_REDLIST_USERS()}">
                <li><a href="?w=users">Manage users</a></li>
                <li><a href="api/downloaddata?territory=${territory}">Download data</a></li>
            </c:if>
            <c:if test="${user.canCREATE_REDLIST_DATASETS()}">
                <li><a href="api/updatenativestatus?territory=${territory}">Update native status for ${territory}</a></li>
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
        <c:if test="${user.canMANAGE_REDLIST_USERS()}">
        <table class="small">
            <thead><tr><th colspan="2">Statistics</th></tr></thead>
            <tr><td>Nr. taxa with a responsible</td><td class="bignumber">${nrsppwithresponsible}</td></tr>
            <tr><td>Nr. taxa with preliminary assessment</td><td class="bignumber">${nrspppreliminaryassessment}</td></tr>
            <tr><td>Nr. taxa with texts ready</td><td class="bignumber">${nrspptextsready}</td></tr>
        </table>
        </c:if>
        <div id="filters">
            <h3><fmt:message key="TaxonIndex.filters.1"/></h3>
            <c:if test="${!user.isGuest()}">
            <div class="filter wordtag togglebutton" id="onlyresponsible"><div class="light"></div><div><fmt:message key="TaxonIndex.filters.3"/></div></div>
            </c:if>
            <div class="filter" id="onlynative"><div class="light"></div><div><fmt:message key="TaxonIndex.filters.4"/></div></div>
            <div class="filter" id="onlyassessed"><div class="light"></div><div><fmt:message key="TaxonIndex.filters.6"/></div></div>
            <div class="filter" id="onlypublished"><div class="light"></div><div><fmt:message key="TaxonIndex.filters.5"/></div></div>
        </div>
        <form method="post" action="/floraon/redlist/${territory}">
            <input type="hidden" name="w" value="taxon"/>
            <c:if test="${user.canEDIT_ANY_FIELD()}">
            <div class="floatingtoolbar">
                <input type="submit" value="" id="editselectedtaxa" class="hidden"/>
            </div>
            </c:if>
            <table id="speciesindex" class="sortable smalltext">
                <thead>
                    <tr>
                    <c:if test="${user.canEDIT_ANY_FIELD()}"><th class="sorttable_nosort"><div class="button" id="toggleselectedtaxa">Toggle</div></th></c:if>
                        <th>Taxon</th><th>Native Status</th>
                    <c:if test="${user.canMANAGE_REDLIST_USERS()}">
                        <th>Responsible for texts</th>
                        <th>Responsible for assessment</th>
                        <th>Responsible for revision</th>
                    </c:if>
                        <th>Assessment status</th>
                    <c:if test="${!user.canMANAGE_REDLIST_USERS()}">
                        <th>Assessor</th><th>Reviewer</th>
                    </c:if>
                        <th>Category</th></tr>
                </thead>
                <tbody>
                <c:forEach var="taxon" items="${specieslist.iterator()}">
                    <c:set var="taxonclasses" value=""/>
                    <c:if test="${taxon.getTaxEnt().isSpecies()}">
                        <c:set var="taxonclasses" value="${taxonclasses} species"/>
                    </c:if>
                    <c:if test="${taxon.getResponsibleAuthors_Texts().contains(user.getID()) || taxon.getResponsibleAuthors_Assessment().contains(user.getID()) || taxon.getResponsibleAuthors_Revision().contains(user.getID())}">
                        <c:set var="taxonclasses" value="${taxonclasses} responsible"/>
                    </c:if>
                    <c:if test="${taxon.getInferredStatus().getNativeStatus().isNative()}">
                        <c:set var="taxonclasses" value="${taxonclasses} native"/>
                    </c:if>
                    <c:if test="${taxon.getAssessment().getPublicationStatus().isPublished()}">
                        <c:set var="taxonclasses" value="${taxonclasses} published"/>
                    </c:if>
                    <c:if test="${taxon.getAssessment().getAssessmentStatus().isAssessed()}">
                        <c:set var="taxonclasses" value="${taxonclasses} assessed"/>
                    </c:if>
                        <tr class="${taxonclasses}">
                        <c:if test="${user.canEDIT_ANY_FIELD()}">
                            <td>
                                <input type="checkbox" name="id" value="${taxon.getTaxEnt().getID()}" class="selectionbox" id="selbox_${taxon.getTaxEnt().getIDURLEncoded()}"/>
                                <label for="selbox_${taxon.getTaxEnt().getIDURLEncoded()}"></label>
                            </td>
                        </c:if>
                        <td><a href="?w=taxon&id=${taxon.getTaxEnt().getIDURLEncoded()}">${taxon.getTaxEnt().getFullName(true)}</a></td>
                        <td>${taxon.getInferredStatus().getStatusSummary()}</td>
                        <c:if test="${user.canMANAGE_REDLIST_USERS()}">
                        <td><c:forEach var="ra" items="${taxon.getResponsibleAuthors_Texts()}">${userMap.get(ra)}<br/></c:forEach></td>
                        <td><c:forEach var="ra" items="${taxon.getResponsibleAuthors_Assessment()}">${userMap.get(ra)}<br/></c:forEach></td>
                        <td><c:forEach var="ra" items="${taxon.getResponsibleAuthors_Revision()}">${userMap.get(ra)}<br/></c:forEach></td>
                        </c:if>
                        <td>
                        <c:if test="${taxon.getAssessment().fetchSequentialAssessmentStatus() != null}">
                            <fmt:message key="${taxon.getAssessment().fetchSequentialAssessmentStatus()[0]}"/><br/>
                            <fmt:message key="${taxon.getAssessment().fetchSequentialAssessmentStatus()[1]}"/>
                        </c:if>
                        </td>

                        <c:if test="${!user.canMANAGE_REDLIST_USERS()}">
                        <td><c:forEach var="eval" items="${taxon.getAssessment().getEvaluator()}">
                            ${userMap.get(eval)}&nbsp;
                        </c:forEach></td>
                        <td><c:forEach var="eval" items="${taxon.getAssessment().getReviewer()}">
                            ${userMap.get(eval)}&nbsp;
                        </c:forEach></td>
                        </c:if>
                        <td>
                        <c:if test="${taxon.getAssessment().getCategory() != null}">
                            <div class="redlistcategory assess_${taxon.getAssessment().getAdjustedCategory().getEffectiveCategory().toString()}"><h1>
                                ${taxon.getAssessment().getAdjustedCategory().getShortTag()}
                                <c:if test="${taxon.getAssessment().getCategory().toString().equals('CR') && !taxon.getAssessment().getSubCategory().toString().equals('NO_TAG')}"><sup>${taxon.getAssessment().getSubCategory().toString()}</sup></c:if>
                            </h1></div>
                        </c:if>
                        </td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </form>
    </c:when>
    <c:when test="${what=='taxon'}">
        <c:if test="${warning != null}">
            <div class="warning"><b><fmt:message key="DataSheet.msg.warning"/></b><br/><fmt:message key="${warning}"/></div>
        </c:if>
        <c:if test="${!multipletaxa && occurrences == null}">
            <div class="warning"><b><fmt:message key="DataSheet.msg.warning"/></b><br/>This taxon has no correspondence in Flora-On, please contact the checklist administrator</div>
        </c:if>
        <c:if test="${user.canVIEW_FULL_SHEET()}">
        <div class="navigator">
            <div class="button anchorbutton section2"><a href="#distribution">2. Distribution</a></div>
            <div class="button anchorbutton section3"><a href="#population">3. Population</a></div>
            <div class="button anchorbutton section4"><a href="#ecology">4. Ecology</a></div>
            <div class="button anchorbutton section5"><a href="#uses">5. Uses and trade</a></div>
            <div class="button anchorbutton section6"><a href="#threats">6. Threats</a></div>
            <div class="button anchorbutton section7"><a href="#conservation">7. Conservation</a></div>
            <div class="button anchorbutton section9"><a href="#assessment">9. Assessment</a></div>
        </div>
        </c:if>
        <c:if test="${multipletaxa}">
        <form class="poster" data-path="/floraon/redlist/api/updatedata" id="maindataform" data-callback="?w=main">
        </c:if>
        <c:if test="${!multipletaxa}">
        <form class="poster" data-path="/floraon/redlist/api/updatedata" id="maindataform" data-refresh="true">
        </c:if>
            <input type="hidden" name="territory" value="${territory}"/>
            <c:if test="${!multipletaxa}">
            <input type="hidden" name="databaseId" value="${rlde.getID()}"/>
            <input type="hidden" name="taxEntID" value="${rlde.getTaxEntID()}"/>
            </c:if>

            <table class="sheet">
                <tr class="textual"><td colspan="3" id="sheet-header" class="title">
                <c:if test="${multipletaxa}">
                    <h1><fmt:message key="DataSheet.msg.multipletaxa"/></h1>
                    <ul class="inlinelistitems">
                    <c:forEach var="taxon" items="${taxa}">
                        <li>
                            <input type="hidden" name="taxEntID" value="${taxon.getID()}"/>
                            <i>${taxon.getName()}</i>
                        </li>
                    </c:forEach>
                    </ul>
                </c:if>
                <c:if test="${!multipletaxa}">
                    <h1><i>${taxon.getName()}</i></h1>
                    <div class="redlistcategory assess_${rlde.getAssessment().getAdjustedCategory().getEffectiveCategory().toString()}">
                        <h1>
                            ${rlde.getAssessment().getAdjustedCategory().getShortTag()}
                            <c:if test="${rlde.getAssessment().getCategory().toString().equals('CR') && !rlde.getAssessment().getSubCategory().toString().equals('NO_TAG')}"><sup>${rlde.getAssessment().getSubCategory().toString()}</sup></c:if>
                        </h1>
                        <p>${rlde.getAssessment().getAdjustedCategory().getLabel()}</p>
                    </div>
                    <div id="header-buttons">
                        <!--<div class="wordtag togglebutton" id="highlight_toggle">needs review</div>-->
                        <c:if test="${user.canVIEW_FULL_SHEET()}">
                            <div class="wordtag togglebutton" id="summary_toggle">summary</div>
                        </c:if>
                        <c:if test="${user.canVIEW_OCCURRENCES()}">
                            <div class="wordtag togglebutton">
                                <a href="?w=taxonrecords&id=${taxon.getIDURLEncoded()}">view occurrences</a>
                            </div>
                        </c:if>
                    </div>
                </c:if>
                </td></tr>
                <c:if test="${user.canVIEW_FULL_SHEET()}">
                    <tr class="section1"><td class="title" colspan="3"><fmt:message key="DataSheet.label.1" /></td></tr>
                    <tr class="section1">
                        <td class="title">1.1</td>
                        <td>Name</td><td><i>${taxon.getName()}</i>
                            <div class="floatingtoolbar">
                                <div tabindex="0" id="removeformatting" class="hidden"></div>
                            <c:if test="${user.canEDIT_ANY_FIELD()}">
                                <input type="submit" value="" id="mainformsubmitter" class="hidden"/>
                            </c:if>
                                <div id="toggle_help"></div>
                            </div>
                        </td>
                    </tr>
                    <tr class="section1"><td class="title">1.2</td><td><fmt:message key="DataSheet.label.1.2" /></td><td>${taxon.getAuthor()}</td></tr>
                    <tr class="section1"><td class="title">1.3</td><td><fmt:message key="DataSheet.label.1.3" /></td><td>
                        <ul>
                        <c:forEach var="synonym" items="${synonyms}">
                            <li data-key="${synonym.getID()}"><c:out value="${synonym.getFullName()}"></c:out></li>
                        </c:forEach>
                        </ul>
                    </td></tr>
                    <tr class="section1"><td class="title">1.4</td><td><fmt:message key="DataSheet.label.1.4" /></td><td>
                        <c:if test="${user.canEDIT_1_4()}">
                            <table>
                                <tr><td colspan="2"><label>
                                    <c:if test="${rlde.getHasTaxonomicProblems()}">
                                        <input type="checkbox" name="hasTaxonomicProblems" checked="checked">
                                    </c:if>
                                    <c:if test="${!rlde.getHasTaxonomicProblems()}">
                                        <input type="checkbox" name="hasTaxonomicProblems">
                                    </c:if>
                                    <fmt:message key="DataSheet.label.1.4a" /></label></td>
                                </tr>
                                <tr><td><fmt:message key="DataSheet.label.1.4b" /></td>
                                <td>
                                    <div contenteditable="true" class="contenteditable">${rlde.getTaxonomicProblemDescription()}</div>
                                    <input type="hidden" name="taxonomicProblemDescription" value="${fn:escapeXml(rlde.getTaxonomicProblemDescription())}"/>
                                </td></tr>
                            </table>
                        </c:if>
                        <c:if test="${!user.canEDIT_1_4()}">
                            <table>
                                <tr><td colspan="2"><fmt:message key="DataSheet.label.1.4a" />: ${rlde.getHasTaxonomicProblems() ? "Yes" : "No"}</td></tr>
                                <tr><td><fmt:message key="DataSheet.label.1.4b" /></td>
                                <td>${rlde.getTaxonomicProblemDescription()}</td></tr>
                            </table>
                        </c:if>
                    </td></tr>
                    <tr class="section1"><td class="title">1.5</td><td><fmt:message key="DataSheet.label.1.5" /></td><td>
                    (a fazer...)
                    </td></tr>
                    <tr class="section2"><td class="title" colspan="3"><a name="distribution"></a><fmt:message key="DataSheet.label.2" /></td></tr>
                </c:if>
                <tr class="section2 textual"><td class="title">2.1</td><td><fmt:message key="DataSheet.label.2.1" /></td><td>
                    <table>
                        <tr><td style="width:auto">
                        <c:if test="${user.canEDIT_SECTION2() || user.canEDIT_ALL_TEXTUAL()}">
                            <div contenteditable="true" class="contenteditable">${rlde.getGeographicalDistribution().getDescription()}</div>
                            <input type="hidden" name="geographicalDistribution_Description" value="${fn:escapeXml(rlde.getGeographicalDistribution().getDescription())}"/>
                        </c:if>
                        <c:if test="${!user.canEDIT_SECTION2() && !user.canEDIT_ALL_TEXTUAL()}">
                            ${rlde.getGeographicalDistribution().getDescription()}
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
                        <table class="subtable">
                            <tr><td><b>EOO</b></td><td>
                                <input type="hidden" name="geographicalDistribution_EOO" value="${EOO}"/>
                                <b><fmt:formatNumber value="${EOO}" maxFractionDigits="0" groupingUsed="false"/> km<sup>2</sup></b> (${occurrences.size()} occurrences, ${nclusters} sites)
                            </td></tr>
                            <c:if test="${realEOO != null && realEOO != EOO}">
                            <tr><td>Real EOO</td><td>
                                <fmt:formatNumber value="${realEOO}" maxFractionDigits="0" groupingUsed="false"/> km<sup>2</sup>
                            </td></tr>
                            </c:if>
                            <tr><td>UTM square EOO</td><td>
                                <fmt:formatNumber value="${squareEOO}" maxFractionDigits="0" groupingUsed="false"/> km<sup>2</sup>
                            </td></tr>
                        </table>
                        </c:if>
                    </td></tr>
                    <tr class="section2"><td class="title">2.3</td><td>Area Of Occupancy<br/>(AOO)</td><td>
                        <c:if test="${occurrences == null}">
                            No correspondence in Flora-On
                        </c:if>
                        <c:if test="${occurrences != null}">
                            <input type="hidden" name="geographicalDistribution_AOO" value="${AOO}"/>
                            <b><fmt:formatNumber value="${AOO}" maxFractionDigits="4" groupingUsed="false"/></b> km<sup>2</sup> (${nquads} ${sizeofsquare}x${sizeofsquare} km squares)
                        </c:if>
                    </td></tr>
                    <tr class="section2"><td class="title">2.4</td><td>Decline in distribution</td><td>
                        <c:if test="${user.canEDIT_SECTION2()}">
                        <table class="triggergroup">
                            <tr><td>Category</td><td>
                                <select name="geographicalDistribution_DeclineDistribution" class="trigger">
                                    <c:forEach var="tmp" items="${geographicalDistribution_DeclineDistribution}">
                                        <c:if test="${rlde.getGeographicalDistribution().getDeclineDistribution().toString().equals(tmp.toString())}">
                                            <option value="${tmp.toString()}" selected="selected" data-trigger="${tmp.isTrigger() ? 1 : 0}"><fmt:message key="${tmp.getLabel()}" /></option>
                                        </c:if>
                                        <c:if test="${!rlde.getGeographicalDistribution().getDeclineDistribution().toString().equals(tmp.toString())}">
                                            <option value="${tmp.toString()}" data-trigger="${tmp.isTrigger() ? 1 : 0}"><fmt:message key="${tmp.getLabel()}" /></option>
                                        </c:if>
                                    </c:forEach>
                                </select>
                            </td></tr>
                            <tr class="triggered ${rlde.getGeographicalDistribution().getDeclineDistribution().isTrigger() ? '' : 'hidden'}"><td>Justification</td><td>
                                <div contenteditable="true" class="contenteditable">${rlde.getGeographicalDistribution().getDeclineDistributionJustification()}</div>
                                <input type="hidden" name="geographicalDistribution_DeclineDistributionJustification" value="${fn:escapeXml(rlde.getGeographicalDistribution().getDeclineDistributionJustification())}"/>
                            </td></tr>
                        </table>
                        </c:if>
                        <c:if test="${!user.canEDIT_SECTION2()}">
                        <table>
                            <tr><td>Category</td><td><fmt:message key="${rlde.getGeographicalDistribution().getDeclineDistribution().getLabel()}" /></td></tr>
                            <tr><td>Justification</td><td>${rlde.getGeographicalDistribution().getDeclineDistributionJustification()}</td></tr>
                        </table>
                        </c:if>
                    </td></tr>
                    <tr class="section2"><td class="title">2.5</td><td>Elevation</td><td>
                        <c:if test="${user.canEDIT_SECTION2()}">
                            <input name="geographicalDistribution_ElevationRange" type="number" min="0" value="${rlde.getGeographicalDistribution().getElevationRange()[0] == null ? '' : rlde.getGeographicalDistribution().getElevationRange()[0]}"/>
                            <input name="geographicalDistribution_ElevationRange" type="number" min="0" value="${rlde.getGeographicalDistribution().getElevationRange()[1] == null ? '' : rlde.getGeographicalDistribution().getElevationRange()[1]}"/>
                        </c:if>
                        <c:if test="${!user.canEDIT_SECTION2()}">
                            ${rlde.getGeographicalDistribution().getElevationRange()[0]} - ${rlde.getGeographicalDistribution().getElevationRange()[1]}
                        </c:if>
                    </td></tr>
                    <tr class="section2"><td class="title">2.6</td><td>Extreme fluctuations</td><td>
                        <c:if test="${user.canEDIT_SECTION2()}">
                            <select name="geographicalDistribution_ExtremeFluctuations">
                                <c:forEach var="tmp" items="${geographicalDistribution_ExtremeFluctuations}">
                                    <c:if test="${rlde.getGeographicalDistribution().getExtremeFluctuations().toString().equals(tmp.toString())}">
                                        <option value="${tmp.toString()}" selected="selected">${tmp.getLabel()}</option>
                                    </c:if>
                                    <c:if test="${!rlde.getGeographicalDistribution().getExtremeFluctuations().toString().equals(tmp.toString())}">
                                        <option value="${tmp.toString()}">${tmp.getLabel()}</option>
                                    </c:if>
                                </c:forEach>
                            </select>
                        </c:if>
                        <c:if test="${!user.canEDIT_SECTION2()}">
                            ${rlde.getGeographicalDistribution().getExtremeFluctuations().getLabel()}
                        </c:if>
                    </td></tr>

                    <tr class="section3"><td class="title" colspan="3"><a name="population"></a>Section 3 - Population</td></tr>
                </c:if>
                <tr class="section3 textual"><td class="title">3.1</td><td><fmt:message key="DataSheet.label.3.1" /></td><td>
                    <c:if test="${user.canEDIT_SECTION3() || user.canEDIT_ALL_TEXTUAL()}">
                        <div contenteditable="true" class="contenteditable">${rlde.getPopulation().getDescription()}</div>
                        <input type="hidden" name="population_Description" value="${fn:escapeXml(rlde.getPopulation().getDescription())}"/>
                    </c:if>
                    <c:if test="${!user.canEDIT_SECTION3() && !user.canEDIT_ALL_TEXTUAL()}">
                        ${rlde.getPopulation().getDescription()}
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
                                <tr><td>Exact number</td><td><input type="number" name="population_NrMatureIndividualsExact" value="${rlde.getPopulation().getNrMatureIndividualsExact()}"/></td></tr>
                            </table>
                        </c:if>
                        <c:if test="${!user.canEDIT_SECTION3()}">
                            <table>
                                <tr><td>Category</td><td>${rlde.getPopulation().getNrMatureIndividualsCategory().getLabel()}</td></tr>
                                <tr><td>Exact number</td><td>${rlde.getPopulation().getNrMatureIndividualsExact()}</td></tr>
                            </table>
                        </c:if>
                    </td></tr>
                    <tr class="section3"><td class="title">3.3</td><td>Type of estimate</td><td>
                        <c:if test="${user.canEDIT_SECTION3()}">
                        <table>
                            <tr><td>Type</td><td>
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
                            <tr><td>Description</td><td>
                                <div contenteditable="true" class="contenteditable">${rlde.getPopulation().getNrMatureIndividualsDescription()}</div>
                                <input type="hidden" name="population_NrMatureIndividualsDescription" value="${fn:escapeXml(rlde.getPopulation().getNrMatureIndividualsDescription())}"/>
                            </td></tr>
                        </table>
                        </c:if>
                        <c:if test="${!user.canEDIT_SECTION3()}">
                        <table>
                            <tr><td>Type</td><td>${rlde.getPopulation().getTypeOfEstimate().getLabel()}</td></tr>
                            <tr><td>Description</td><td>${rlde.getPopulation().getNrMatureIndividualsDescription()}</td></tr>
                        </table>
                        </c:if>
                    </td></tr>
                    <tr class="section3"><td class="title">3.4</td><td><fmt:message key="DataSheet.label.3.4" /></td><td>
                        <c:if test="${user.canEDIT_SECTION3()}">
                        <table class="triggergroup">
                            <tr><td>Category</td><td>
                                <select name="population_PopulationDecline" class="trigger">
                                    <c:forEach var="tmp" items="${population_PopulationDecline}">
                                        <c:if test="${rlde.getPopulation().getPopulationDecline().toString().equals(tmp.toString())}">
                                            <option value="${tmp.toString()}" selected="selected" data-trigger="${tmp.isTrigger() ? 1 : 0}"><fmt:message key="${tmp.getLabel()}" /></option>
                                        </c:if>
                                        <c:if test="${!rlde.getPopulation().getPopulationDecline().toString().equals(tmp.toString())}">
                                            <option value="${tmp.toString()}" data-trigger="${tmp.isTrigger() ? 1 : 0}"><fmt:message key="${tmp.getLabel()}" /></option>
                                        </c:if>
                                    </c:forEach>
                                </select>
                            </td></tr>
                            <tr class="triggered ${rlde.getPopulation().getPopulationDecline().isTrigger() ? '' : 'hidden'}"><td>Percentage</td><td>
                                <input type="number" name="population_PopulationDeclinePercent" value="${rlde.getPopulation().getPopulationDeclinePercent()}" placeholder="percentage"/> %
                            </td></tr>
                            <tr class="triggered ${rlde.getPopulation().getPopulationDecline().isTrigger() ? '' : 'hidden'}"><td>Justification</td><td>
                                <div contenteditable="true" class="contenteditable">${rlde.getPopulation().getPopulationDeclineJustification()}</div>
                                <input type="hidden" name="population_PopulationDeclineJustification" value="${fn:escapeXml(rlde.getPopulation().getPopulationDeclineJustification())}"/>
                            </td></tr>
                        </table>
                        </c:if>
                        <c:if test="${!user.canEDIT_SECTION3()}">
                        <table>
                            <tr><td>Category</td><td><fmt:message key="${rlde.getPopulation().getPopulationDecline().getLabel()}" /></td></tr>
                            <tr><td>Percentage</td><td>${rlde.getPopulation().getPopulationDeclinePercent()}</td></tr>
                            <tr><td>Justification</td><td>${rlde.getPopulation().getPopulationDeclineJustification()}</td></tr>
                        </table>
                        </c:if>
                    </td></tr>
                    <tr class="section3"><td class="title">3.5</td><td><fmt:message key="DataSheet.label.3.5" /></td><td>
                        <c:if test="${user.canEDIT_SECTION3()}">
                        <table class="triggergroup">
                            <tr><td>Category</td><td>
                                <div class="checkboxes list" tabindex="0">
                                    <c:forEach var="tmp" items="${population_PopulationSizeReduction}">
                                        <c:if test="${rlde.getPopulation().getPopulationSizeReduction().toString().equals(tmp.toString())}">
                                            <input type="radio" class="trigger" name="population_PopulationSizeReduction" value="${tmp.toString()}" checked="checked" id="psr_${tmp}" data-trigger="${tmp.isTrigger() ? 1 : 0}"/>
                                            <label for="psr_${tmp}"> <fmt:message key="${tmp.getLabel()}" /></label>
                                        </c:if>
                                        <c:if test="${!rlde.getPopulation().getPopulationSizeReduction().toString().equals(tmp.toString())}">
                                            <input type="radio" class="trigger" name="population_PopulationSizeReduction" value="${tmp.toString()}" id="psr_${tmp}" data-trigger="${tmp.isTrigger() ? 1 : 0}"/>
                                            <label for="psr_${tmp}"> <fmt:message key="${tmp.getLabel()}" /></label>
                                        </c:if>
                                    </c:forEach>
                                    <label class="placeholder"><fmt:message key="DataSheet.msg.clickxpand"/></label>
                                </div>
                            </td></tr>
                            <tr class="triggered ${rlde.getPopulation().getPopulationSizeReduction().isTrigger() ? '' : 'hidden'}"><td>Percentage</td><td>
                                <input type="number" name="population_PopulationTrend" value="${rlde.getPopulation().getPopulationTrend()}" placeholder="percentage"/> %
                            </td></tr>
                            <tr class="triggered ${rlde.getPopulation().getPopulationSizeReduction().isTrigger() ? '' : 'hidden'}"><td>Justification</td><td>
                                <div contenteditable="true" class="contenteditable">${rlde.getPopulation().getPopulationSizeReductionJustification()}</div>
                                <input type="hidden" name="population_PopulationSizeReductionJustification" value="${fn:escapeXml(rlde.getPopulation().getPopulationSizeReductionJustification())}"/>
                            </td></tr>
                        </table>
                        </c:if>
                        <c:if test="${!user.canEDIT_SECTION3()}">
                        <table>
                            <tr><td>Percentage</td><td>${rlde.getPopulation().getPopulationTrend()} %</td></tr>
                            <tr><td>Category</td><td>${rlde.getPopulation().getPopulationSizeReduction().getLabel()}</td></tr>
                            <tr><td>Justification</td><td>${rlde.getPopulation().getPopulationSizeReductionJustification()}</td></tr>
                        </table>
                        </c:if>
                    </td></tr>
                    <tr class="section3"><td class="title">3.6</td><td>Severely fragmented</td><td>
                        <c:if test="${user.canEDIT_SECTION3()}">
                        <table class="triggergroup">
                            <tr><td>Category</td><td>
                                <select name="population_SeverelyFragmented" class="trigger">
                                    <c:forEach var="tmp" items="${population_SeverelyFragmented}">
                                        <c:if test="${rlde.getPopulation().getSeverelyFragmented().toString().equals(tmp.toString())}">
                                            <option value="${tmp.toString()}" selected="selected" data-trigger="${tmp.isTrigger() ? 1 : 0}">${tmp.getLabel()}</option>
                                        </c:if>
                                        <c:if test="${!rlde.getPopulation().getSeverelyFragmented().toString().equals(tmp.toString())}">
                                            <option value="${tmp.toString()}" data-trigger="${tmp.isTrigger() ? 1 : 0}">${tmp.getLabel()}</option>
                                        </c:if>
                                    </c:forEach>
                                </select>
                            </td></tr>
                            <tr class="triggered ${rlde.getPopulation().getSeverelyFragmented().isTrigger() ? '' : 'hidden'}"><td>Justification</td><td>
                                <div contenteditable="true" class="contenteditable">${rlde.getPopulation().getSeverelyFragmentedJustification()}</div>
                                <input type="hidden" name="population_SeverelyFragmentedJustification" value="${fn:escapeXml(rlde.getPopulation().getSeverelyFragmentedJustification())}"/>
                            </td></tr>
                        </c:if>
                        <c:if test="${!user.canEDIT_SECTION3()}">
                        <table>
                            <tr><td>Category</td><td>${rlde.getPopulation().getSeverelyFragmented().getLabel()}</td></tr>
                            <tr><td>Justification</td><td>${rlde.getPopulation().getSeverelyFragmentedJustification()}</td></tr>
                        </c:if>
                            <tr><td>Mean area of sites</td><td><fmt:formatNumber value="${meanLocationArea}" maxFractionDigits="1"/> hectares</td></tr>
                        </table>
                    </td></tr>
                    <tr class="section3"><td class="title">3.7</td><td>Extreme fluctuations in population size</td><td>
                    <c:if test="${user.canEDIT_SECTION3()}">
                        <table class="triggergroup">
                            <tr><td>Category</td><td>
                                <select name="population_ExtremeFluctuations" class="trigger">
                                    <c:forEach var="tmp" items="${population_ExtremeFluctuations}">
                                        <c:if test="${rlde.getPopulation().getExtremeFluctuations().toString().equals(tmp.toString())}">
                                            <option value="${tmp.toString()}" selected="selected" data-trigger="${tmp.isTrigger() ? 1 : 0}">${tmp.getLabel()}</option>
                                        </c:if>
                                        <c:if test="${!rlde.getPopulation().getExtremeFluctuations().toString().equals(tmp.toString())}">
                                            <option value="${tmp.toString()}" data-trigger="${tmp.isTrigger() ? 1 : 0}">${tmp.getLabel()}</option>
                                        </c:if>
                                    </c:forEach>
                                </select>
                            </td></tr>
                            <tr class="triggered ${rlde.getPopulation().getExtremeFluctuations().isTrigger() ? '' : 'hidden'}"><td>Justification</td><td>
                                <div contenteditable="true" class="contenteditable">${rlde.getPopulation().getExtremeFluctuationsJustification()}</div>
                                <input type="hidden" name="population_ExtremeFluctuationsJustification" value="${fn:escapeXml(rlde.getPopulation().getExtremeFluctuationsJustification())}"/>
                            </td></tr>
                        </table>
                    </c:if>
                    <c:if test="${!user.canEDIT_SECTION3()}">
                        <table>
                            <tr><td>Category</td><td>${rlde.getPopulation().getExtremeFluctuations().getLabel()}</td></tr>
                            <tr><td>Justification</td><td>${rlde.getPopulation().getExtremeFluctuationsJustification()}</td></tr>
                        </table>
                    </c:if>
                    </td></tr>
                    <tr class="section3"><td class="title">3.8</td><td>Number of mature individuals in each subpopulation</td><td>
                    <c:if test="${user.canEDIT_SECTION3()}">
                        <select name="population_NrMatureEachSubpop">
                            <c:forEach var="tmp" items="${population_NrMatureEachSubpop}">
                                <c:if test="${rlde.getPopulation().getNrMatureEachSubpop().toString().equals(tmp.toString())}">
                                    <option value="${tmp.toString()}" selected="selected">${tmp.getLabel()}</option>
                                </c:if>
                                <c:if test="${!rlde.getPopulation().getNrMatureEachSubpop().toString().equals(tmp.toString())}">
                                    <option value="${tmp.toString()}">${tmp.getLabel()}</option>
                                </c:if>
                            </c:forEach>
                        </select>
                    </c:if>
                    <c:if test="${!user.canEDIT_SECTION3()}">
                        ${rlde.getPopulation().getNrMatureEachSubpop().getLabel()}
                    </c:if>
                    </td></tr>
                    <tr class="section3"><td class="title">3.9</td><td>% of mature individuals in one subpopulation</td><td>
                    <c:if test="${user.canEDIT_SECTION3()}">
                        <select name="population_PercentMatureOneSubpop">
                            <c:forEach var="tmp" items="${population_PercentMatureOneSubpop}">
                                <c:if test="${rlde.getPopulation().getPercentMatureOneSubpop().toString().equals(tmp.toString())}">
                                    <option value="${tmp.toString()}" selected="selected">${tmp.getLabel()}</option>
                                </c:if>
                                <c:if test="${!rlde.getPopulation().getPercentMatureOneSubpop().toString().equals(tmp.toString())}">
                                    <option value="${tmp.toString()}">${tmp.getLabel()}</option>
                                </c:if>
                            </c:forEach>
                        </select>
                    </c:if>
                    <c:if test="${!user.canEDIT_SECTION3()}">
                        ${rlde.getPopulation().getPercentMatureOneSubpop().getLabel()}
                    </c:if>
                    </td></tr>
                    <tr class="section4"><td class="title" colspan="3"><a name="ecology"></a>Section 4 - Ecology</td></tr>
                </c:if>     <!-- can view full sheet -->
                <tr class="section4 textual"><td class="title">4.1</td><td>Habitats and ecology information</td><td>
                    <c:if test="${user.canEDIT_SECTION4() || user.canEDIT_ALL_TEXTUAL()}">
                        <div contenteditable="true" class="contenteditable">${ecology}</div>
                        <input type="hidden" name="ecology_Description" value="${fn:escapeXml(ecology)}"/>
                    </c:if>
                    <c:if test="${!user.canEDIT_SECTION4() && !user.canEDIT_ALL_TEXTUAL()}">
                        ${ecology}
                    </c:if>
                </td></tr>
                <c:if test="${user.canVIEW_FULL_SHEET()}">
                    <tr class="section4"><td class="title">4.2</td><td>Habitat types</td><td>
                        <c:if test="${user.canEDIT_SECTION4()}">
                            <c:forEach var="tmp" items="${ecology_HabitatTypes}">
                                <c:if test="${habitatTypes.contains(tmp)}">
                                    <label><input type="checkbox" name="ecology_HabitatTypes" value="${tmp.toString()}" checked="checked"/> ${tmp.getLabel()}</label>
                                </c:if>
                                <c:if test="${!habitatTypes.contains(tmp)}">
                                    <label><input type="checkbox" name="ecology_HabitatTypes" value="${tmp.toString()}"/> ${tmp.getLabel()}</label>
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
                        <table class="triggergroup">
                            <tr><td>Length (exact or interval)</td><td>
                                <input name="ecology_GenerationLength" type="text" class="trigger" value="${rlde.getEcology().getGenerationLength()}"/>
                            </td></tr>
                            <tr class="triggered ${(rlde.getEcology().getGenerationLength() != null && rlde.getEcology().getGenerationLength().length() > 0) ? '' : 'hidden'}"><td>Justification</td><td>
                                <div contenteditable="true" class="contenteditable">${rlde.getEcology().getGenerationLengthJustification()}</div>
                                <input type="hidden" name="ecology_GenerationLengthJustification" value="${fn:escapeXml(rlde.getEcology().getGenerationLengthJustification())}"/>
                            </td></tr>
                        </table>
                    </c:if>
                    <c:if test="${!user.canEDIT_SECTION4()}">
                        <table>
                            <tr><td>Length (exact or interval)</td><td>${rlde.getEcology().getGenerationLength()}</td></tr>
                            <tr><td>Justification</td><td>${rlde.getEcology().getGenerationLengthJustification()}</td></tr>
                        </table>
                    </c:if>
                    </td></tr>
                    <tr class="section4"><td class="title">4.5</td><td>Decline in habitat quality</td><td>
                    <c:if test="${user.canEDIT_SECTION4()}">
                    <table class="triggergroup">
                        <tr><td>Category</td><td>
                            <select name="ecology_DeclineHabitatQuality" class="trigger">
                                <c:forEach var="tmp" items="${ecology_DeclineHabitatQuality}">
                                    <c:if test="${rlde.getEcology().getDeclineHabitatQuality().toString().equals(tmp.toString())}">
                                        <option value="${tmp.toString()}" selected="selected" data-trigger="${tmp.isTrigger() ? 1 : 0}"><fmt:message key="${tmp.getLabel()}" /></option>
                                    </c:if>
                                    <c:if test="${!rlde.getEcology().getDeclineHabitatQuality().toString().equals(tmp.toString())}">
                                        <option value="${tmp.toString()}" data-trigger="${tmp.isTrigger() ? 1 : 0}"><fmt:message key="${tmp.getLabel()}" /></option>
                                    </c:if>
                                </c:forEach>
                            </select>
                        </td></tr>
                        <tr class="triggered ${rlde.getEcology().getDeclineHabitatQuality().isTrigger() ? '' : 'hidden'}"><td>Justification</td><td>
                            <div contenteditable="true" class="contenteditable">${rlde.getEcology().getDeclineHabitatQualityJustification()}</div>
                            <input type="hidden" name="ecology_DeclineHabitatQualityJustification" value="${fn:escapeXml(rlde.getEcology().getDeclineHabitatQualityJustification())}"/>
                        </td></tr>
                    </table>
                    </c:if>
                    <c:if test="${!user.canEDIT_SECTION4()}">
                    <table>
                        <tr><td>Category</td><td><fmt:message key="${rlde.getEcology().getDeclineHabitatQuality().getLabel()}" /></td></tr>
                        <tr><td>Justification</td><td>${rlde.getEcology().getDeclineHabitatQualityJustification()}</td></tr>
                    </table>
                    </c:if>
                    </td></tr>
                    <tr class="section5"><td class="title" colspan="3"><a name="uses"></a><fmt:message key="DataSheet.label.5" /></td></tr>
                </c:if>
                <tr class="section5 textual"><td class="title">5.1</td><td><fmt:message key="DataSheet.label.5.1" /></td><td>
                    <c:if test="${user.canEDIT_SECTION5() || user.canEDIT_ALL_TEXTUAL()}">
                        <div contenteditable="true" class="contenteditable">${rlde.getUsesAndTrade().getDescription()}</div>
                        <input type="hidden" name="usesAndTrade_Description" value="${fn:escapeXml(rlde.getUsesAndTrade().getDescription())}"/>
                    </c:if>
                    <c:if test="${!user.canEDIT_SECTION5() && !user.canEDIT_ALL_TEXTUAL()}">
                        ${rlde.getUsesAndTrade().getDescription()}
                    </c:if>
                </td></tr>
                <c:if test="${user.canVIEW_FULL_SHEET()}">
                    <tr class="section5"><td class="title">5.2</td><td><fmt:message key="DataSheet.label.5.2" /></td><td>
                        <c:if test="${user.canEDIT_SECTION5()}">
                            <div class="checkboxes list" tabindex="0">
                                <input type="hidden" name="usesAndTrade_Uses" value=""/>
                            <c:forEach var="tmp" items="${usesAndTrade_Uses}">
                                <c:if test="${uses.contains(tmp)}">
                                    <input type="checkbox" name="usesAndTrade_Uses" value="${tmp.toString()}" checked="checked" id="uses_${tmp}"/>
                                    <label for="uses_${tmp}"> <fmt:message key="${tmp.getLabel()}" /><div class="legend"><fmt:message key="${tmp.getDescription()}" /></div></label>
                                </c:if>
                                <c:if test="${!uses.contains(tmp)}">
                                    <input type="checkbox" name="usesAndTrade_Uses" value="${tmp.toString()}" id="uses_${tmp}"/>
                                    <label for="uses_${tmp}"> <fmt:message key="${tmp.getLabel()}" /><div class="legend"><fmt:message key="${tmp.getDescription()}" /></div></label>
                                </c:if>
                            </c:forEach>
                            <label class="placeholder"><fmt:message key="DataSheet.msg.clickxpand"/></label>
                            </div>
                        </c:if>
                        <c:if test="${!user.canEDIT_SECTION5()}">
                            <ul>
                            <c:forEach var="tmp" items="${uses}">
                                <li><fmt:message key="${tmp.getLabel()}" /></li>
                            </c:forEach>
                            </ul>
                        </c:if>
                    </td></tr>
                    <tr class="section5"><td class="title">5.3</td><td><fmt:message key="DataSheet.label.5.3" /></td><td>
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
                    <tr class="section5"><td class="title">5.4</td><td><fmt:message key="DataSheet.label.5.4" /></td><td>
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
                    <tr class="section6"><td class="title" colspan="3"><a name="threats"></a>Section 6 - Threats</td></tr>
                </c:if>
                <tr class="section6 textual"><td class="title">6.1</td><td>Threat description</td><td>
                    <c:if test="${user.canEDIT_SECTION6() || user.canEDIT_ALL_TEXTUAL()}">
                        <div contenteditable="true" class="contenteditable">${rlde.getThreats().getDescription()}</div>
                        <input type="hidden" name="threats_Description" value="${fn:escapeXml(rlde.getThreats().getDescription())}"/>
                    </c:if>
                    <c:if test="${!user.canEDIT_SECTION6() && !user.canEDIT_ALL_TEXTUAL()}">
                        ${rlde.getThreats().getDescription()}
                    </c:if>
                </td></tr>
                <c:if test="${user.canVIEW_FULL_SHEET()}">
                    <tr class="section6"><td class="title">6.2</td><td>Threats</td><td>
                        (a fazer...)
                    </td></tr>
                    <tr class="section6"><td class="title">6.3</td><td>Number of locations</td><td>
                    <c:if test="${user.canEDIT_SECTION6()}">
                        <table>
                            <tr><td>Number</td><td>
                                <input type="number" min="0" name="threats_NumberOfLocations" value="${rlde.getThreats().getNumberOfLocations()}"/><br/>
                            </td></tr>
                            <tr><td>Justification</td><td>
                                <div contenteditable="true" class="contenteditable">${rlde.getThreats().getNumberOfLocationsJustification()}</div>
                                <input type="hidden" name="threats_NumberOfLocationsJustification" value="${fn:escapeXml(rlde.getThreats().getNumberOfLocationsJustification())}"/>
                            </td></tr>
                            <tr><td>Automatic estimate</td><td>${nclusters} sites</td></tr>
                        </table>
                    </c:if>
                    <c:if test="${!user.canEDIT_SECTION6()}">
                        ${rlde.getThreats().getNumberOfLocations()}<br/>
                        ${nclusters} sites (automatic estimate)
                    </c:if>
                    </td></tr>
                    <tr class="section6"><td class="title">6.4</td><td>Decline in number of locations or subpopulations</td><td>
                    <c:if test="${user.canEDIT_SECTION6()}">
                    <table class="triggergroup">
                        <tr><td>Category</td><td>
                            <select name="threats_DeclineNrLocations" class="trigger">
                                <c:forEach var="tmp" items="${threats_DeclineNrLocations}">
                                    <c:if test="${rlde.getThreats().getDeclineNrLocations().toString().equals(tmp.toString())}">
                                        <option value="${tmp.toString()}" selected="selected" data-trigger="${tmp.isTrigger() ? 1 : 0}">${tmp.getLabel()}</option>
                                    </c:if>
                                    <c:if test="${!rlde.getThreats().getDeclineNrLocations().toString().equals(tmp.toString())}">
                                        <option value="${tmp.toString()}" data-trigger="${tmp.isTrigger() ? 1 : 0}">${tmp.getLabel()}</option>
                                    </c:if>
                                </c:forEach>
                            </select>
                        </td></tr>
                        <tr class="triggered ${rlde.getThreats().getDeclineNrLocations().isTrigger() ? '' : 'hidden'}"><td>Justification</td><td>
                            <div contenteditable="true" class="contenteditable">${rlde.getThreats().getDeclineNrLocationsJustification()}</div>
                            <input type="hidden" name="threats_DeclineNrLocationsJustification" value="${fn:escapeXml(rlde.getThreats().getDeclineNrLocationsJustification())}"/>
                        </td></tr>
                    </table>
                    </c:if>
                    <c:if test="${!user.canEDIT_SECTION6()}">
                    <table>
                        <tr><td>Category</td><td>${rlde.getThreats().getDeclineNrLocations().getLabel()}</td></tr>
                        <tr><td>Justification</td><td>${rlde.getThreats().getDeclineNrLocationsJustification()}</td></tr>
                    </table>
                    </c:if>
                    </td></tr>
                    <tr class="section6"><td class="title">6.5</td><td>Extreme fluctuations in number of locations or subpopulations</td><td>
                    <c:if test="${user.canEDIT_SECTION6()}">
                    <table class="triggergroup">
                        <tr><td>Category</td><td>
                            <select name="threats_ExtremeFluctuationsNrLocations" class="trigger">
                                <c:forEach var="tmp" items="${threats_ExtremeFluctuationsNrLocations}">
                                    <c:if test="${rlde.getThreats().getExtremeFluctuationsNrLocations().toString().equals(tmp.toString())}">
                                        <option value="${tmp.toString()}" selected="selected" data-trigger="${tmp.isTrigger() ? 1 : 0}">${tmp.getLabel()}</option>
                                    </c:if>
                                    <c:if test="${!rlde.getThreats().getExtremeFluctuationsNrLocations().toString().equals(tmp.toString())}">
                                        <option value="${tmp.toString()}" data-trigger="${tmp.isTrigger() ? 1 : 0}">${tmp.getLabel()}</option>
                                    </c:if>
                                </c:forEach>
                            </select>
                        </td></tr>
                        <tr class="triggered ${rlde.getThreats().getExtremeFluctuationsNrLocations().isTrigger() ? '' : 'hidden'}"><td>Justification</td><td>
                            <div contenteditable="true" class="contenteditable">${rlde.getThreats().getExtremeFluctuationsNrLocationsJustification()}</div>
                            <input type="hidden" name="threats_ExtremeFluctuationsNrLocationsJustification" value="${fn:escapeXml(rlde.getThreats().getExtremeFluctuationsNrLocationsJustification())}"/>
                        </td></tr>
                    </table>
                    </c:if>
                    <c:if test="${!user.canEDIT_SECTION6()}">
                    <table>
                        <tr><td>Category</td><td>${rlde.getThreats().getExtremeFluctuationsNrLocations().getLabel()}</td></tr>
                        <tr><td>Justification</td><td>${rlde.getThreats().getExtremeFluctuationsNrLocationsJustification()}</td></tr>
                    </table>
                    </c:if>
                    </td></tr>

                    <tr class="section7"><td class="title" colspan="3"><a name="conservation"></a>Section 7 - Conservation</td></tr>
                </c:if>
                <tr class="section7 textual"><td class="title">7.1</td><td>Conservation measures</td><td>
                    <c:if test="${user.canEDIT_SECTION7() || user.canEDIT_ALL_TEXTUAL()}">
                        <div contenteditable="true" class="contenteditable">${rlde.getConservation().getDescription()}</div>
                        <input type="hidden" name="conservation_Description" value="${fn:escapeXml(rlde.getConservation().getDescription())}"/>
                    </c:if>
                    <c:if test="${!user.canEDIT_SECTION7() && !user.canEDIT_ALL_TEXTUAL()}">
                        ${rlde.getConservation().getDescription()}
                    </c:if>
                </td></tr>
                <c:if test="${user.canVIEW_FULL_SHEET()}">
                    <tr class="section7"><td class="title">7.2</td><td>Conservation plans</td><td>
                    <c:if test="${user.canEDIT_SECTION7()}">
                        <table class="triggergroup">
                            <tr><td>Category</td><td>
                                <select name="conservation_ConservationPlans" class="trigger">
                                    <c:forEach var="tmp" items="${conservation_ConservationPlans}">
                                        <c:if test="${rlde.getConservation().getConservationPlans().toString().equals(tmp.toString())}">
                                            <option value="${tmp.toString()}" selected="selected" data-trigger="${tmp.isTrigger() ? 1 : 0}">${tmp.getLabel()}</option>
                                        </c:if>
                                        <c:if test="${!rlde.getConservation().getConservationPlans().toString().equals(tmp.toString())}">
                                            <option value="${tmp.toString()}" data-trigger="${tmp.isTrigger() ? 1 : 0}">${tmp.getLabel()}</option>
                                        </c:if>
                                    </c:forEach>
                                </select>
                            </td></tr>
                            <tr class="triggered ${rlde.getConservation().getConservationPlans().isTrigger() ? '' : 'hidden'}"><td>Justification</td><td>
                                <div contenteditable="true" class="contenteditable">${rlde.getConservation().getConservationPlansJustification()}</div>
                                <input type="hidden" name="conservation_ConservationPlansJustification" value="${fn:escapeXml(rlde.getConservation().getConservationPlansJustification())}"/>
                            </td></tr>
                        </table>
                    </c:if>
                    <c:if test="${!user.canEDIT_SECTION7()}">
                        <table>
                            <tr><td>Category</td><td>${rlde.getConservation().getConservationPlans().getLabel()}</td></tr>
                            <tr><td>Justification</td><td>${rlde.getConservation().getConservationPlansJustification()}</td></tr>
                        </table>
                    </c:if>
                    </td></tr>
                    <tr class="section7"><td class="title">7.3</td><td><i>Ex-situ</i> conservation</td><td>
                    <c:if test="${user.canEDIT_SECTION7()}">
                        <table class="triggergroup">
                            <tr><td>Category</td><td>
                                <select name="conservation_ExSituConservation" class="trigger">
                                    <c:forEach var="tmp" items="${conservation_ExSituConservation}">
                                        <c:if test="${rlde.getConservation().getExSituConservation().toString().equals(tmp.toString())}">
                                            <option value="${tmp.toString()}" selected="selected" data-trigger="${tmp.isTrigger() ? 1 : 0}">${tmp.getLabel()}</option>
                                        </c:if>
                                        <c:if test="${!rlde.getConservation().getExSituConservation().toString().equals(tmp.toString())}">
                                            <option value="${tmp.toString()}" data-trigger="${tmp.isTrigger() ? 1 : 0}">${tmp.getLabel()}</option>
                                        </c:if>
                                    </c:forEach>
                                </select>
                            </td></tr>
                            <tr class="triggered ${rlde.getConservation().getExSituConservation().isTrigger() ? '' : 'hidden'}"><td>Justification</td><td>
                                <div contenteditable="true" class="contenteditable">${rlde.getConservation().getExSituConservationJustification()}</div>
                                <input type="hidden" name="conservation_ExSituConservationJustification" value="${fn:escapeXml(rlde.getConservation().getExSituConservationJustification())}"/>
                            </td></tr>
                        </table>
                    </c:if>
                    <c:if test="${!user.canEDIT_SECTION7()}">
                        <table>
                            <tr><td>Category</td><td>${rlde.getConservation().getExSituConservation().getLabel()}</td></tr>
                            <tr><td>Justification</td><td>${rlde.getConservation().getExSituConservationJustification()}</td></tr>
                        </table>
                    </c:if>
                    </td></tr>
                    <tr class="section7"><td class="title">7.4</td><td>Occurrence in protected areas</td><td>
                        <c:if test="${occurrences.size() > 0}">
                            <p><fmt:formatNumber value="${(locationsInPA / nclusters) * 100}" maxFractionDigits="1"/>% sites inside protected areas (${locationsInPA}/${nclusters})</p>
                            <table class="sortable smalltext">
                                <tr><th>Protected Area</th><th>Type</th><th>Number of sites</th></tr>
                                <c:forEach var="tmp" items="${occurrenceInProtectedAreas}">
                                    <tr>
                                        <td>${tmp.getKey().getProperties().get("SITE_NAME")}</td>
                                        <td>${tmp.getKey().getProperties().get("TIPO")}</td>
                                        <td>${tmp.getValue()}</td>
                                    </tr>
                                </c:forEach>
                            </table>
                        </c:if>
                        <c:if test="${occurrences.size() == 0}">
                            <p>No occurrences</p>
                        </c:if>
                    </td></tr>
                    <tr class="section7"><td class="title">7.4.1</td><td>Legally protected?</td><td>
                        (a fazer)
                    </td></tr>
                    <tr class="section7"><td class="title">7.5</td><td><fmt:message key="DataSheet.label.7.5" /></td><td>
                        <c:if test="${user.canEDIT_SECTION7()}">
                        <div class="checkboxes list" tabindex="0">
                            <input type="hidden" name="conservation_ProposedConservationActions" value=""/>
                            <c:forEach var="tmp" items="${conservation_ProposedConservationActions}">
                                <c:if test="${proposedConservationActions.contains(tmp)}">
                                    <input type="checkbox" name="conservation_ProposedConservationActions" value="${tmp.toString()}" checked="checked" id="pca_${tmp}"/>
                                    <label for="pca_${tmp}"> <fmt:message key="${tmp.getLabel()}" /><div class="legend"><fmt:message key="${tmp.getDescription()}" /></div></label>
                                </c:if>
                                <c:if test="${!proposedConservationActions.contains(tmp)}">
                                    <input type="checkbox" name="conservation_ProposedConservationActions" value="${tmp.toString()}" id="pca_${tmp}"/>
                                    <label for="pca_${tmp}"> <fmt:message key="${tmp.getLabel()}" /><div class="legend"><fmt:message key="${tmp.getDescription()}" /></div></label>
                                </c:if>
                            </c:forEach>
                            <label class="placeholder"><fmt:message key="DataSheet.msg.clickxpand"/></label>
                        </div>
                        </c:if>
                        <c:if test="${!user.canEDIT_SECTION7()}">
                            <ul>
                            <c:forEach var="tmp" items="${proposedConservationActions}">
                                <li><fmt:message key="${tmp.getLabel()}" /></li>
                            </c:forEach>
                            </ul>
                        </c:if>
                    </td></tr>

                    <tr class="section7"><td class="title">7.6</td><td><fmt:message key="DataSheet.label.7.6" /></td><td>
                        <c:if test="${user.canEDIT_SECTION7()}">
                        <div class="checkboxes list" tabindex="0">
                            <input type="hidden" name="conservation_ProposedStudyMeasures" value=""/>
                            <c:forEach var="tmp" items="${conservation_ProposedStudyMeasures}">
                                <c:if test="${proposedStudyMeasures.contains(tmp)}">
                                    <input type="checkbox" name="conservation_ProposedStudyMeasures" value="${tmp.toString()}" checked="checked" id="psm_${tmp}"/>
                                    <label for="psm_${tmp}"> <fmt:message key="${tmp.getLabel()}" /><div class="legend"><fmt:message key="${tmp.getDescription()}" /></div></label>
                                </c:if>
                                <c:if test="${!proposedStudyMeasures.contains(tmp)}">
                                    <input type="checkbox" name="conservation_ProposedStudyMeasures" value="${tmp.toString()}" id="psm_${tmp}"/>
                                    <label for="psm_${tmp}"> <fmt:message key="${tmp.getLabel()}" /><div class="legend"><fmt:message key="${tmp.getDescription()}" /></div></label>
                                </c:if>
                            </c:forEach>
                            <label class="placeholder"><fmt:message key="DataSheet.msg.clickxpand"/></label>
                        </div>
                        </c:if>
                        <c:if test="${!user.canEDIT_SECTION7()}">
                            <ul>
                            <c:forEach var="tmp" items="${proposedStudyMeasures}">
                                <li><fmt:message key="${tmp.getLabel()}" /></li>
                            </c:forEach>
                            </ul>
                        </c:if>
                    </td></tr>

                    <tr class="section8"><td class="title" colspan="3">Section 8 - Bibliographic references</td></tr>
                    <tr class="section8"><td class="title">8.1</td><td>Reference list</td><td>
                    (a fazer)
                    </td></tr>

                    <tr class="section9"><td class="title" colspan="3"><a name="assessment"></a>Section 9 - Red List Assessment</td></tr>
                    <tr class="section9"><td class="title">9.1</td><td>Category</td><td class="triggergroup">
                        <div id="redlistcategories">
                            <c:if test="${user.canEDIT_9_1_2_3_4()}">
                                <c:forEach var="tmp" items="${assessment_Category}">
                                    <c:if test="${rlde.getAssessment().getAdjustedCategory().getEffectiveCategory().equals(tmp)}">
                                        <input type="radio" name="assessment_Category" value="${rlde.getAssessment().getAdjustedCategory().toString()}" id="assess_${tmp.toString()}" checked="checked" class="trigger" data-trigger="${tmp.isTrigger() ? 1 : 0}">
                                    </c:if>
                                    <c:if test="${!rlde.getAssessment().getAdjustedCategory().getEffectiveCategory().equals(tmp)}">
                                        <input type="radio" name="assessment_Category" value="${tmp.toString()}" id="assess_${tmp.toString()}" class="trigger" data-trigger="${tmp.isTrigger() ? 1 : 0}">
                                    </c:if>
                                    <label for="assess_${tmp.toString()}">
                                        <h1>
                                            ${tmp.toString()}<c:if test="${rlde.getAssessment().getAdjustedCategory().getEffectiveCategory().equals(tmp) && rlde.getAssessment().getAdjustedCategory().isUpDownListed()}">º</c:if>
                                            <c:if test="${tmp == 'CR' && rlde.getAssessment().getCategory().toString().equals(tmp.toString()) && !rlde.getAssessment().getSubCategory().toString().equals('NO_TAG')}"><sup>${rlde.getAssessment().getSubCategory().toString()}</sup></c:if>
                                        </h1>
                                        <p>${tmp.getLabel()}</p>
                                    </label>
                                    <c:if test="${tmp == 'VU'}"><br/></c:if>
                                </c:forEach>
                            </c:if>
                            <c:if test="${!user.canEDIT_9_1_2_3_4()}">
                                <div class="redlistcategory assess_${rlde.getAssessment().getCategory().toString()}"><h1>${rlde.getAssessment().getCategory().toString()}</h1><p>${rlde.getAssessment().getCategory().getLabel()}</p></div>
                            </c:if>
                        </div>
                        <div class="triggered ${rlde.getAssessment().getCategory().isTrigger() ? '' : 'hidden'}">
                        <c:if test="${user.canEDIT_9_1_2_3_4()}">
                            <select name="assessment_SubCategory">
                                <c:forEach var="tmp" items="${assessment_SubCategory}">
                                    <c:if test="${rlde.getAssessment().getSubCategory().toString().equals(tmp.toString())}">
                                        <option value="${tmp.toString()}" selected="selected">${tmp.getLabel()}</option>
                                    </c:if>
                                    <c:if test="${!rlde.getAssessment().getSubCategory().toString().equals(tmp.toString())}">
                                        <option value="${tmp.toString()}">${tmp.getLabel()}</option>
                                    </c:if>
                                </c:forEach>
                            </select>
                        </c:if>
                        <c:if test="${!user.canEDIT_9_1_2_3_4()}">
                            ${rlde.getAssessment().getSubCategory().getLabel()}
                        </c:if>
                        </div>
                    </td></tr>
                    <tr class="section9"><td class="title">9.2</td><td>Criteria</td><td>
                        <c:if test="${user.canEDIT_9_1_2_3_4()}">
                            <input name="assessment_Criteria" type="text" class="longbox" value="${rlde.getAssessment().getCriteria()}"/>
                        </c:if>
                        <c:if test="${!user.canEDIT_9_1_2_3_4()}">
                            ${rlde.getAssessment().getCriteria()}
                        </c:if>
                    </td></tr>
                </c:if>
                <tr class="section9 textual"><td class="title">9.3</td><td>Assessment justification</td><td>
                    <c:if test="${user.canEDIT_9_1_2_3_4() || user.canEDIT_9_3_9_45()}">
                        <div contenteditable="true" class="contenteditable">${rlde.getAssessment().getJustification()}</div>
                        <input type="hidden" name="assessment_Justification" value="${fn:escapeXml(rlde.getAssessment().getJustification())}"/>
                    </c:if>
                    <c:if test="${!user.canEDIT_9_1_2_3_4() && !user.canEDIT_9_3_9_45()}">
                        ${rlde.getAssessment().getJustification()}
                    </c:if>
                </td></tr>
                <c:if test="${user.canVIEW_FULL_SHEET()}">
                    <tr class="section9"><td class="title">9.4</td><td>Regional assessment analysis</td><td>
                        <table class="subtable">
                            <tr>
                                <td class="title">9.4.1</td>
                                <td>Does the regional population experience any significant immigration of propagules likely to reproduce in the region?</td>
                                <td>
                                <c:if test="${user.canEDIT_9_1_2_3_4()}">
                                    <select name="assessment_PropaguleImmigration">
                                        <c:forEach var="tmp" items="${assessment_RegionalAssessment}">
                                            <c:if test="${rlde.getAssessment().getPropaguleImmigration().toString().equals(tmp.toString())}">
                                                <option value="${tmp.toString()}" selected="selected">${tmp.getLabel()}</option>
                                            </c:if>
                                            <c:if test="${!rlde.getAssessment().getPropaguleImmigration().toString().equals(tmp.toString())}">
                                                <option value="${tmp.toString()}">${tmp.getLabel()}</option>
                                            </c:if>
                                        </c:forEach>
                                    </select>
                                </c:if>
                                <c:if test="${!user.canEDIT_9_1_2_3_4()}">
                                    ${rlde.getAssessment().getPropaguleImmigration().getLabel()}
                                </c:if>
                                </td>
                            </tr>
                            <tr>
                                <td class="title">9.4.2</td>
                                <td>Is the immigration expected to decrease?</td>
                                <td>
                                <c:if test="${user.canEDIT_9_1_2_3_4()}">
                                    <select name="assessment_DecreaseImmigration">
                                        <c:forEach var="tmp" items="${assessment_RegionalAssessment}">
                                            <c:if test="${rlde.getAssessment().getDecreaseImmigration().toString().equals(tmp.toString())}">
                                                <option value="${tmp.toString()}" selected="selected">${tmp.getLabel()}</option>
                                            </c:if>
                                            <c:if test="${!rlde.getAssessment().getDecreaseImmigration().toString().equals(tmp.toString())}">
                                                <option value="${tmp.toString()}">${tmp.getLabel()}</option>
                                            </c:if>
                                        </c:forEach>
                                    </select>
                                </c:if>
                                <c:if test="${!user.canEDIT_9_1_2_3_4()}">
                                    ${rlde.getAssessment().getDecreaseImmigration().getLabel()}
                                </c:if>
                                </td>
                            </tr>
                            <tr>
                                <td class="title">9.4.3</td>
                                <td>Is the regional population a sink?</td>
                                <td>
                                <c:if test="${user.canEDIT_9_1_2_3_4()}">
                                    <select name="assessment_IsSink">
                                        <c:forEach var="tmp" items="${assessment_RegionalAssessment}">
                                            <c:if test="${rlde.getAssessment().getIsSink().toString().equals(tmp.toString())}">
                                                <option value="${tmp.toString()}" selected="selected">${tmp.getLabel()}</option>
                                            </c:if>
                                            <c:if test="${!rlde.getAssessment().getIsSink().toString().equals(tmp.toString())}">
                                                <option value="${tmp.toString()}">${tmp.getLabel()}</option>
                                            </c:if>
                                        </c:forEach>
                                    </select>
                                </c:if>
                                <c:if test="${!user.canEDIT_9_1_2_3_4()}">
                                    ${rlde.getAssessment().getIsSink().getLabel()}
                                </c:if>
                                </td>
                            </tr>
                            <tr>
                                <td class="title" rowspan="2">9.4.4</td>
                                <td>Uplist or downlist category</td>
                                <td>
                                <c:if test="${user.canEDIT_9_1_2_3_4()}">
                                    <select name="assessment_UpDownListing">
                                        <c:forEach var="tmp" items="${assessment_UpDownListing}">
                                            <c:if test="${rlde.getAssessment().getUpDownListing().toString().equals(tmp.toString())}">
                                                <option value="${tmp.toString()}" selected="selected">${tmp.getLabel()}</option>
                                            </c:if>
                                            <c:if test="${!rlde.getAssessment().getUpDownListing().toString().equals(tmp.toString())}">
                                                <option value="${tmp.toString()}">${tmp.getLabel()}</option>
                                            </c:if>
                                        </c:forEach>
                                    </select>
                                </c:if>
                                <c:if test="${!user.canEDIT_9_1_2_3_4()}">
                                    ${rlde.getAssessment().getUpDownListing().getLabel()}
                                </c:if>
                                </td>
                            </tr>
                            <tr><td style="width:auto">Suggested action</td><td>${assessment_UpDownList}</td></tr>
                            <tr>
                                <td class="title">9.4.5</td>
                                <td>Justification</td>
                                <td>
                                <c:if test="${user.canEDIT_9_1_2_3_4() || user.canEDIT_9_3_9_45()}">
                                    <div contenteditable="true" class="contenteditable">${rlde.getAssessment().getUpDownListingJustification()}</div>
                                    <input type="hidden" name="assessment_UpDownListingJustification" value="${fn:escapeXml(rlde.getAssessment().getUpDownListingJustification())}"/>
                                </c:if>
                                <c:if test="${!user.canEDIT_9_1_2_3_4() && !user.canEDIT_9_3_9_45()}">
                                    ${rlde.getAssessment().getUpDownListingJustification()}
                                </c:if>
                                </td>
                            </tr>
                        </table>
                    </td></tr>

                    <tr class="section9"><td class="title">9.5</td><td>Previous published Red List assessments</td><td>
                        <table><tr><th>Year published</th><th>Category</th></tr>
                        <c:if test="${user.canEDIT_9_5_9_6_9_61_9_91()}">
                        <c:forEach var="tmp" items="${previousAssessments}">
                            <tr>
                                <td><input name="assessment_PreviousAssessmentListYear" type="number" min="1900" max="2020" value="${tmp.getYear()}"/></td>
                                <td><select name="assessment_PreviousAssessmentListCategory">
                                    <option value="">(not assigned)</option>
                                    <c:forEach var="tmp1" items="${assessment_Category}">
                                        <c:if test="${tmp.getCategory().toString().equals(tmp1.toString())}">
                                            <option value="${tmp1.toString()}" selected="selected">${tmp1.getLabel()}</option>
                                        </c:if>
                                        <c:if test="${!tmp.getCategory().toString().equals(tmp1.toString())}">
                                            <option value="${tmp1.toString()}">${tmp1.getLabel()}</option>
                                        </c:if>
                                    </c:forEach>
                                </select></td>
                            </tr>
                        </c:forEach>
                        </c:if>
                        <c:if test="${!user.canEDIT_9_5_9_6_9_61_9_91()}">
                        <c:forEach var="tmp" items="${previousAssessments}">
                            <tr><td>${tmp.getYear()}</td><td>${tmp.getCategory().getLabel()}</td></tr>
                        </c:forEach>
                        </c:if>
                        </table>
                    </td></tr>
                    <tr class="section9"><td class="title">9.6</td><td>Text author(s)</td>
                    <c:if test="${user.canEDIT_9_5_9_6_9_61_9_91()}">
                        <td>
                            <div class="multiplechooser left" id="textauthors">
                                <input type="hidden" name="assessment_Authors" value=""/>
                            <c:forEach var="tmp" items="${authors}">
                                <input type="checkbox" name="assessment_Authors" id="aa_${tmp}" value="${tmp}" checked="checked"/>
                                <label for="aa_${tmp}" class="wordtag togglebutton">${userMap.get(tmp)}</label>
                            </c:forEach>
                            </div>
                            <div class="withsuggestions">
                                <input type="text" class="nochangeevent" name="query" placeholder="type first letters" autocomplete="off" id="authorbox"/>
                                <div id="authorsuggestions"></div>
                            </div>
                            <input type="button" value="Create new..." class="button" id="newauthor"/>
                        </td>
                    </c:if>
                    <c:if test="${!user.canEDIT_9_5_9_6_9_61_9_91()}">
                        <td>
                            <c:forEach var="tmp" items="${rlde.getAssessment().getAuthors()}">
                                <div class="wordtag">${userMap.get(tmp)}</div>
                            </c:forEach>
                        </td>
                    </c:if>
                    </tr>
                    <tr class="section9"><td class="title">9.6.1</td><td>Collaborators</td><td>
                    <c:if test="${user.canEDIT_9_5_9_6_9_61_9_91()}">
                        <input name="assessment_Collaborators" type="text" class="longbox" value="${rlde.getAssessment().getCollaborators()}"/>
                    </c:if>
                    <c:if test="${!user.canEDIT_9_5_9_6_9_61_9_91()}">
                        ${rlde.getAssessment().getCollaborators()}
                    </c:if>
                    </td></tr>
                    <tr class="section9"><td class="title">9.7</td><td>Assessor(s)</td>
                    <c:if test="${user.canEDIT_9_7_9_92()}">
                        <td>
                            <div class="multiplechooser left" id="assessors">
                            <input type="hidden" name="assessment_Evaluator" value=""/>
                            <c:forEach var="tmp" items="${evaluator}">
                                <input type="checkbox" name="assessment_Evaluator" id="aas_${tmp}" value="${tmp}" checked="checked"/>
                                <label for="aas_${tmp}" class="wordtag togglebutton">${userMap.get(tmp)}</label>
                            </c:forEach>
                            </div>
                            <div class="withsuggestions">
                                <input type="text" class="nochangeevent" name="query" placeholder="type first letters" autocomplete="off" id="assessorbox"/>
                                <div id="assessorsuggestions"></div>
                            </div>
                            <input type="button" value="Create new..." class="button" id="newevaluator"/>
                        </td>
                    </c:if>
                    <c:if test="${!user.canEDIT_9_7_9_92()}">
                        <td>
                            <c:forEach var="tmp" items="${rlde.getAssessment().getEvaluator()}">
                                <div class="wordtag">${userMap.get(tmp)}</div>
                            </c:forEach>
                        </td>
                    </c:if>
                    </tr>
                    <tr class="section9"><td class="title">9.8</td><td>Reviewer(s)</td>
                    <c:if test="${user.canEDIT_9_8_9_93()}">
                        <td>
                            <div class="multiplechooser left" id="reviewers">
                            <input type="hidden" name="assessment_Reviewer" value=""/>
                            <c:forEach var="tmp" items="${reviewer}">
                                <input type="checkbox" name="assessment_Reviewer" id="are_${tmp}" value="${tmp}" checked="checked"/>
                                <label for="are_${tmp}" class="wordtag togglebutton">${userMap.get(tmp)}</label>
                            </c:forEach>
                            </div>
                            <div class="withsuggestions">
                                <input type="text" class="nochangeevent" name="query" placeholder="type first letters" autocomplete="off" id="reviewerbox"/>
                                <div id="reviewersuggestions"></div>
                            </div>
                            <input type="button" value="Create new..." class="button" id="newreviewer"/>
                        </td>
                    </c:if>
                    <c:if test="${!user.canEDIT_9_8_9_93()}">
                        <td>
                            <c:forEach var="tmp" items="${rlde.getAssessment().getReviewer()}">
                                <div class="wordtag">${userMap.get(tmp)}</div>
                            </c:forEach>
                        </td>
                    </c:if>
                    </tr>
                    <tr class="section9"><td class="title">9.9</td><td>Assessment status</td><td>
                        <table class="subtable">
                            <tr><td class="title">9.9.1</td><td>Texts</td><td>
                            <c:if test="${user.canEDIT_9_5_9_6_9_61_9_91()}">
                                <select name="assessment_TextStatus">
                                    <c:forEach var="tmp" items="${assessment_TextStatus}">
                                        <c:if test="${rlde.getAssessment().getTextStatus().toString().equals(tmp.toString())}">
                                            <option value="${tmp.toString()}" selected="selected"><fmt:message key="${tmp.getLabel()}"/></option>
                                        </c:if>
                                        <c:if test="${!rlde.getAssessment().getTextStatus().toString().equals(tmp.toString())}">
                                            <option value="${tmp.toString()}"><fmt:message key="${tmp.getLabel()}"/></option>
                                        </c:if>
                                    </c:forEach>
                                </select>
                            </c:if>
                            <c:if test="${!user.canEDIT_9_5_9_6_9_61_9_91()}">
                                <fmt:message key="${rlde.getAssessment().getTextStatus().getLabel()}"/>
                            </c:if>
                            </td></tr>
                            <tr><td class="title">9.9.2</td><td>Assessment status</td><td>
                            <c:if test="${user.canEDIT_9_7_9_92()}">
                                <select name="assessment_AssessmentStatus">
                                    <c:forEach var="tmp" items="${assessment_AssessmentStatus}">
                                        <c:if test="${rlde.getAssessment().getAssessmentStatus().toString().equals(tmp.toString())}">
                                            <option value="${tmp.toString()}" selected="selected"><fmt:message key="${tmp.getLabel()}"/></option>
                                        </c:if>
                                        <c:if test="${!rlde.getAssessment().getAssessmentStatus().toString().equals(tmp.toString())}">
                                            <option value="${tmp.toString()}"><fmt:message key="${tmp.getLabel()}"/></option>
                                        </c:if>
                                    </c:forEach>
                                </select>
                            </c:if>
                            <c:if test="${!user.canEDIT_9_7_9_92()}">
                                <fmt:message key="${rlde.getAssessment().getAssessmentStatus().getLabel()}"/>
                            </c:if>
                            </td></tr>
                            <tr><td class="title">9.9.3</td><td>Review status</td><td>
                            <c:if test="${user.canEDIT_9_8_9_93()}">
                                <select name="assessment_ReviewStatus">
                                    <c:forEach var="tmp" items="${assessment_ReviewStatus}">
                                        <c:if test="${rlde.getAssessment().getReviewStatus().toString().equals(tmp.toString())}">
                                            <option value="${tmp.toString()}" selected="selected"><fmt:message key="${tmp.getLabel()}"/></option>
                                        </c:if>
                                        <c:if test="${!rlde.getAssessment().getReviewStatus().toString().equals(tmp.toString())}">
                                            <option value="${tmp.toString()}"><fmt:message key="${tmp.getLabel()}"/></option>
                                        </c:if>
                                    </c:forEach>
                                </select>
                            </c:if>
                            <c:if test="${!user.canEDIT_9_8_9_93()}">
                                <fmt:message key="${rlde.getAssessment().getReviewStatus().getLabel()}"/>
                            </c:if>
                            </td></tr>
                            <tr><td class="title">9.9.4</td><td>Publication status</td><td>
                            <c:if test="${user.canEDIT_9_9_4()}">
                                <select name="assessment_PublicationStatus">
                                    <c:forEach var="tmp" items="${assessment_PublicationStatus}">
                                        <c:if test="${rlde.getAssessment().getPublicationStatus().toString().equals(tmp.toString())}">
                                            <option value="${tmp.toString()}" selected="selected"><fmt:message key="${tmp.getLabel()}"/></option>
                                        </c:if>
                                        <c:if test="${!rlde.getAssessment().getPublicationStatus().toString().equals(tmp.toString())}">
                                            <option value="${tmp.toString()}"><fmt:message key="${tmp.getLabel()}"/></option>
                                        </c:if>
                                    </c:forEach>
                                </select>
                            </c:if>
                            <c:if test="${!user.canEDIT_9_9_4()}">
                                <fmt:message key="${rlde.getAssessment().getPublicationStatus().getLabel()}"/>
                            </c:if>
                            </td></tr>
                        </table>
                    </td></tr>
                    <tr class="section9"><td class="title">9.10</td><td>Date assessed</td><td>
                        ${rlde.getDateAssessed()}
                    </td></tr>
                    <tr class="section9"><td class="title">9.11</td><td>Date published</td><td>
                        ${rlde.getDatePublished()}
                    </td></tr>
                </c:if>
                <tr class="section9"><td class="title">9.12</td><td>Citation</td><td>
                <c:if test="${authors.size() > 0}">
                <c:if test="${authors.size() > 1}">
                    <c:forEach var="i" begin="0" end="${authors.size() - 2}">${userMap.get(authors.get(i))}, </c:forEach>
                </c:if>
                ${userMap.get(authors.get(authors.size() - 1))}. ${rlde.getAssessment().getPublicationStatus().isPublished() ? rlde.getYearPublished() : 'Unpublished'}. <i>${taxon.getName()}</i>. Lista Vermelha da Flora Vascular de Portugal Continental.
                </c:if>
                </td></tr>
            </table>
        </form>
        <c:if test="${!multipletaxa}">
        <h1><fmt:message key="DataSheet.msg.revhistory"/></h1>
        <table class="small">
            <tr><th>Date saved</th><th>User</th><th>Number of edits</th></tr>
        <c:forEach var="rev" items="${revisions}">
            <tr><td>${rev.getKey().getFormattedDateSaved()}</td><td>${userMap.get(rev.getKey().getUser())}</td><td>${rev.getValue()}</td></tr>
        </c:forEach>
        </table>
        </c:if>
    </c:when>

    <c:when test="${what=='taxonrecords'}">
        <c:if test="${!user.canVIEW_OCCURRENCES()}">
            <div class="warning"><b>You&#8217;re not authorized to enter this page</b></div>
        </c:if>
        <c:if test="${user.canVIEW_OCCURRENCES()}">
            <h1>${taxon.getFullName(true)}</h1>
            <c:if test="${occurrences == null}">
                <div class="warning"><b><fmt:message key="DataSheet.msg.warning"/></b><br/>This taxon has no correspondence in Flora-On, please contact the checklist administrator</div>
            </c:if>
            <h2>${occurrences.size()} occurrences</h2>
            <table class="sortable smalltext" id="recordtable">
                <thead>
                    <tr><th>Record ID</th><th>Taxon</th><c:if test="${user.canDOWNLOAD_OCCURRENCES()}"><th>Latitude</th><th>Longitude</th></c:if><th>Year</th><th>Month</th>
                    <th>Day</th><th>Author</th><th style="width:180px">Notes</th><th>Precision</th><th>ID in doubt?</th><th>In flower?</th></tr>
                </thead>
                <c:forEach var="occ" items="${occurrences.iterator()}">
                    <tr>
                        <td>${occ.getId_reg()}</td>
                        <td><i>${occ.getGenus()} ${occ.getSpecies()} ${occ.getInfrataxon() == null ? '' : occ.getInfrataxon()}</i></td>
                        <c:if test="${user.canDOWNLOAD_OCCURRENCES()}">
                        <td><fmt:formatNumber value="${occ.getLatitude()}" maxFractionDigits="4"/></td>
                        <td><fmt:formatNumber value="${occ.getLongitude()}" maxFractionDigits="4"/></td>
                        </c:if>
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
            <h2>Existing users</h2>
            <table class="sortable smalltext">
                <tr><th>Name</th><th>Global privileges</th><th>Taxon-specific privileges</th><th>Responsible for texts</th><th>Responsible for assessment</th><th>Responsible for revision</th><th></th></tr>
                <c:forEach var="tmp" items="${users}">
                    <c:if test="${user.getUserType() == 'ADMINISTRATOR' || (user.getUserType() != 'ADMINISTRATOR' && tmp.getUserType() != 'ADMINISTRATOR')}">
                    <tr><td>${tmp.getName()}</td>
                        <td>
                        <c:forEach var="tmp1" items="${tmp.getPrivileges()}">
                            <div class="wordtag">${tmp1.getLabel()}</div>
                        </c:forEach>
                        </td>
                        <td><ul>
                        <c:forEach var="tmp2" items="${tmp.getTaxonPrivileges()}">
                            <li>
                            <c:if test="${fn:length(tmp2.getApplicableTaxa()) > 1}">
                            <c:forEach var="tmp3" begin="0" end="${fn:length(tmp2.getApplicableTaxa()) - 2}">
                                <i>${taxonMap.get(tmp2.getApplicableTaxa()[tmp3])}</i>,&nbsp;
                            </c:forEach>
                            </c:if>
                                <i>${taxonMap.get(tmp2.getApplicableTaxa()[fn:length(tmp2.getApplicableTaxa()) - 1])}</i>
                            </li>
                        </c:forEach>
                        </ul></td>
                        <td class="bignumber">${responsibleTextCounter.get(tmp.getID())}</td>
                        <td class="bignumber">${responsibleAssessmentCounter.get(tmp.getID())}</td>
                        <td class="bignumber">${responsibleRevisionCounter.get(tmp.getID())}</td>
                        <td><div class="button anchorbutton"><a href="?w=edituser&amp;user=${tmp.getIDURLEncoded()}">edit user</a></div></td>
                    </tr>
                    </c:if>
                </c:forEach>
            </table>
            <h2>Create new user</h2>
            <form class="poster" data-path="/floraon/admin/createuser">
                <table>
                    <tr><td class="title">Username</td><td><input type="text" name="userName"/></td></tr>
                    <tr><td class="title">Person name</td><td><input type="text" name="name"/></td></tr>
                    <tr>
                        <td class="title">Global privileges</td>
                        <td class="multiplechooser">
                            <c:forEach var="tmp" items="${redlistprivileges}">
                                <input type="checkbox" name="${tmp}" id="priv_${tmp}"/><label for="priv_${tmp}" class="wordtag togglebutton">${tmp.getLabel()}</label>
                            </c:forEach>
                        </td>
                    </tr>
                </table>
                <input type="submit" value="Create" class="textbutton"/>
            </form>
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
                        <tr>
                            <td class="title">Global privileges (apply to all taxa)</td>
                            <td class="multiplechooser">
                                <c:forEach var="tmp" items="${redlistprivileges}">
                                    <c:if test="${requesteduser.hasAssignedPrivilege(tmp)}">
                                        <input type="checkbox" name="${tmp}" id="priv_${tmp}" checked="checked"/><label for="priv_${tmp}" class="wordtag togglebutton">${tmp.getLabel()}</label>
                                    </c:if>
                                    <c:if test="${!requesteduser.hasAssignedPrivilege(tmp)}">
                                        <input type="checkbox" name="${tmp}" id="priv_${tmp}"/><label for="priv_${tmp}" class="wordtag togglebutton">${tmp.getLabel()}</label>
                                    </c:if>
                                </c:forEach>
                            </td>
                        </tr>
                        <tr><td colspan="2"><input type="submit" value="Update user" class="textbutton"/></td></tr>
                    </table>
                </form>
                <h2>Taxon-specific privileges</h2>
                <c:if test="${tsprivileges.size() > 0}">
                <h3>Existing privilege sets</h3>
                <table>
                    <thead><tr><th>Taxa</th><th>Privileges</th><th></th></tr></thead>
                    <tbody>
                    <c:forEach var="tsp" items="${tsprivileges}" varStatus="loop">
                        <tr>
                            <td><ul>
                            <c:forEach var="tax" items="${tsp.getApplicableTaxa()}">
                                <li>${taxonMap.get(tax)}</li>
                            </c:forEach>
                            </ul></td>
                            <td>
                            <c:forEach var="pri" items="${tsp.getPrivileges()}">
                                <div class="wordtag">${pri.toString()}</div>
                            </c:forEach>
                            </td>
                            <td>
                                <form class="poster" data-path="/floraon/admin/removetaxonprivileges" data-refresh="true">
                                    <input type="hidden" name="userId" value="${requesteduser.getID()}"/>
                                    <input type="hidden" name="index" value="${loop.index}"/>
                                    <input type="submit" value="Remove this privilege set" class="textbutton"/>
                                </form>
                            </td>
                        </tr>
                    </c:forEach>
                </tbody>
                </table>
                </c:if>
                <h3>Add a new set of privileges to specific taxa</h3>
                <form class="poster" data-path="/floraon/admin/addtaxonprivileges" data-refresh="true">
                    <input type="hidden" name="userId" value="${requesteduser.getID()}"/>
                    <table>
                        <thead><tr><th>Taxa</th><th>Privileges</th></tr></thead>
                        <tbody>
                            <tr>
                                <td style="width:20%; vertical-align:top;">
                                    <div class="multiplechooser" id="taxonprivileges"></div>
                                    <div class="withsuggestions">
                                        <input type="text" name="query" class="nochangeevent" placeholder="type some letters to find a taxon" autocomplete="off" id="taxonbox"/>
                                        <div id="suggestions"></div>
                                    </div>
                                </td>
                                <td class="multiplechooser">
                                    <c:forEach var="tmp" items="${redlisttaxonprivileges}">
                                        <input type="checkbox" name="taxonPrivileges" value="${tmp}" id="tspriv_${tmp}"/><label for="tspriv_${tmp}" class="wordtag togglebutton">${tmp.getLabel()}</label>
                                    </c:forEach>
                                </td>
                            </tr>
                        <tr><td colspan="2"><input type="submit" value="Add privileges for these taxa" class="textbutton"/></td></tr>
                        </tbody>
                    </table>
                </form>
            </c:if>
        </c:if>
    </c:when>
    </c:choose>
    </div>
</div>

</body>
</html>
