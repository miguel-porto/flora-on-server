<%@ page pageEncoding="UTF-8" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page session="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="language" value="${not empty param.language ? param.language : not empty language ? language : pageContext.response.locale}" scope="request" />
<fmt:setLocale value="${language}" />
<fmt:setBundle basename="pt.floraon.redlistdata.fieldValues" />
<!DOCTYPE html>
<html>
<head>
	<title>${taxon.getName()} <fmt:message key="DataSheet.title"/></title>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
	<c:if test="${!offline}"><link href='//fonts.googleapis.com/css?family=Lato:300' rel='stylesheet' type='text/css'></c:if>
	<link rel="stylesheet" type="text/css" href="../base.css?nocache=${uuid}"/>
	<link rel="stylesheet" type="text/css" href="../redlist.css?nocache=${uuid}"/>
	<script type="text/javascript" src="../sorttable.js"></script>
	<script type="text/javascript" src="../basefunctions.js?nocache=${uuid}"></script>
	<script type="text/javascript" src="../ajaxforms.js?nocache=${uuid}"></script>
	<script type="text/javascript" src="../suggestions.js?nocache=${uuid}"></script>
	<script type="text/javascript" src="../js/treenavigator.js"></script>
	<script type="text/javascript" src="../redlistadmin.js?nocache=${uuid}"></script>
	<script type="text/javascript" src="../js/svg-pan-zoom.min.js"></script>
	<c:if test="${what=='main'}">
	<style>
	    <c:forEach var="tmp" items="${allTags}">
	    #speciesindex.filter_${tmp.getKey()} tbody tr:not(.tag_${tmp.getKey()}) {display: none;}
	    </c:forEach>
	</style>
	</c:if>
</head>
<body>
<input type="hidden" name="territory" value="${territory}"/>
<a class="returntomain" href="../"><img src="../images/cap-cor.png" alt="logo"/></a>
<div id="title"><a href="../"><fmt:message key="DataSheet.title"/></a></div>
<div id="main-holder">
    <c:if test="${what != 'taxonrecords' && !user.isGuest()}">
    <%--                THE MAIN MENU ON THE LEFT               --%>
    <div id="left-bar" class="buttonmenu">
        <ul>
            <li><a href="?w=main"><fmt:message key="Separator.1"/></a></li>
            <li><a href="?w=published"><fmt:message key="Separator.9"/></a></li>
            <c:if test="${user.canMANAGE_REDLIST_USERS() || user.canDOWNLOAD_OCCURRENCES()}">
                <li><a href="?w=users"><fmt:message key="Separator.2"/></a></li>
                <li><a href="?w=settings"><fmt:message key="Separator.8"/></a></li>
                <li><a href="?w=admin"><fmt:message key="Separator.12"/></a></li>
                <li><a href="?w=jobs"><fmt:message key="Separator.6"/></a></li>
            </c:if>
            <c:if test="${user.getUserPolygons() != null && !user.getUserPolygons().equals(\"\")}">
                <li><a href="?w=downloadtargetrecords"><fmt:message key="Separator.7"/></a></li>
            </c:if>
            <c:if test="${user.canVIEW_OCCURRENCES()}">
                <li><a href="?w=stats"><fmt:message key="Separator.11"/></a></li>
                <li><a href="?w=allmaps">Todos os mapas</a></li>
            </c:if>
            <c:if test="${!user.isGuest()}">
                <li><a href="?w=references">Bibliografia</a></li>
                <li><a href="?w=report">Relatório</a></li>
                <li><a href="?w=alleditions"><fmt:message key="Separator.10"/></a></li>
            </c:if>
        </ul>
    </div>
    </c:if>
    <div id="main">
    <c:choose>
    <c:when test="${what=='addterritory'}">
        <c:if test="${user.canCREATE_REDLIST_DATASETS()}">
        <h1>Create new red list dataset</h1>
        <h2>Select a territory to create a dataset.</h2>
        <ul>
        <c:forEach var="terr" items="${territories}">
            <li><a href="redlist/api/newdataset?territory=${terr.getShortName()}">${terr.getName()}</a></li>
        </c:forEach>
        </ul>
        </c:if>
    </c:when>
    <c:when test="${what=='batch'}">
        <h1><fmt:message key="Separator.5"/></h1>
        <p><fmt:message key="Update.1"/></p>
        <form class="poster" data-path="api/updatefromcsv" data-refresh="false" method="post" enctype="multipart/form-data">
            <input type="hidden" name="territory" value="${territory}"/>
            <input type="file" name="updateTable" />
            <input type="submit" class="textbutton" value="<fmt:message key='Update.2'/>"/>
        </form>
    </c:when>
    <c:when test="${what=='published'}">
        <h1>Published sheets</h1>
        <table id="speciesindex" class="sortable">
            <thead>
                <tr><th>Taxon</th><th>Category</th></tr>
            </thead>
            <tbody>
            <c:forEach var="snapshot" items="${specieslist}">
                <tr>
                    <td><a href="?w=sheet&id=${snapshot.getKey()}">${snapshot.getTaxEnt().getFullName(true)}</a></td>
                    <td>
                        <c:if test="${snapshot.getAssessment().getCategory() != null}">
                            <div class="redlistcategory assess_${snapshot.getAssessment().getAdjustedCategory().getEffectiveCategory().toString()}"><h1>
                                ${snapshot.getAssessment().getAdjustedCategory().getShortTag()}
                                <c:if test="${snapshot.getAssessment().getCategory().toString().equals('CR') && !snapshot.getAssessment().getSubCategory().toString().equals('NO_TAG')}"><sup>${snapshot.getAssessment().getSubCategory().toString()}</sup></c:if>
                            </h1></div>
                        </c:if>
                    </td>
                </tr>
            </c:forEach>
            </tbody>
        </table>
    </c:when>
    <c:when test="${what=='alleditions'}">
        <h1>All editions in the last ${ndays} days</h1>
        <table class="sortable">
            <tr><th>Taxon</th><th>Date saved</th><th>User</th></tr>
            <c:forEach var="rev" items="${revisions}">
            <tr><td><a href="?w=taxon&id=${rev.getTaxEnt()._getIDURLEncoded()}">${rev.getTaxEnt().getNameWithAnnotationOnly(true)}</a></td><td>${rev.getFormattedDateSaved()}</td><td>${userMap.get(rev.getUser())}</td></tr>
            </c:forEach>
        </table>
    </c:when>
    <c:when test="${what=='main'}">
        <jsp:include page="fragments/redlist-taxonindex.jsp"></jsp:include>
    </c:when>

    <c:when test="${what=='search'}">
        <form method="GET">
            <input type="hidden" name="w" value="search"/>
            <input type="text" name="s" placeholder="type search text"/>
            <input type="submit" value="Search all data sheets" class="textbutton"/>
        </form>

        <table class="sortable">
            <tr><th>Taxon</th><th>Found match</th></tr>
            <c:forEach var="res" items="${searchResults}">
            <tr>
                <td><a href="?w=taxon&id=${res.getKey().getTaxEnt()._getIDURLEncoded()}">${res.getKey().getTaxEnt().getNameWithAnnotationOnly(true)}</a></td>
                <td><ul style="margin:0"><c:forEach var="res1" items="${res.getValue()}"><li>${res1}</li></c:forEach></ul></td>
            </tr>
            </c:forEach>
        </table>
    </c:when>

    <c:when test="${what=='debug'}">
        <jsp:include page="fragments/frag-debug.jsp"></jsp:include>
    </c:when>

    <c:when test="${what=='jobs'}">
        <jsp:include page="fragments/frag-jobs.jsp"></jsp:include>
    </c:when>

    <c:when test="${what=='taxon' || what == 'sheet'}">
        <jsp:include page="fragments/frag-redlistsheet.jsp"></jsp:include>
    </c:when>

    <c:when test="${what=='taxonrecords'}">
        <jsp:include page="fragments/frag-sheetoccurrencelist.jsp"></jsp:include>
    </c:when>

    <c:when test="${what=='settings'}">
        <jsp:include page="fragments/frag-redlistsettings.jsp"></jsp:include>
    </c:when>

    <c:when test="${what=='admin'}">
        <jsp:include page="fragments/frag-redlistadmin.jsp"></jsp:include>
    </c:when>
<%--
    <c:when test="${what=='replacetools'}">
        <jsp:include page="fragments/frag-redlistreplacetools.jsp"></jsp:include>
    </c:when>
--%>
    <c:when test="${what=='users'}">
        <jsp:include page="fragments/frag-userlist.jsp"></jsp:include>
    </c:when>

    <c:when test="${what=='edituser'}">
        <jsp:include page="fragments/frag-edituser.jsp"></jsp:include>
    </c:when>

    <c:when test="${what=='allmaps'}">
        <jsp:include page="fragments/frag-distributionmaps.jsp"></jsp:include>
    </c:when>

    <c:when test="${what=='report'}">
        <c:choose>
        <c:when test="${param.type == 'technical'}">
            <jsp:include page="fragments/technicalreport.jsp"></jsp:include>
        </c:when>
        <c:when test="${param.type == 'geo'}">
            <jsp:include page="fragments/georeport.jsp"></jsp:include>
        </c:when>
        <c:otherwise>
        <h1>Reports</h1>
        <div class="outer">
            <div class="bigbutton section2">
                <h1><a href="?w=report&type=technical">Technical report</a></h1>
            </div>
            <c:if test="${user.canDOWNLOAD_OCCURRENCES()}">
            <div class="bigbutton section3">
                <h1><a href="?w=report&type=geo">Geographical report</a></h1>
            </div>
            </c:if>
        </div>
        </c:otherwise>
        </c:choose>
    </c:when>

    <c:when test="${what=='references'}">
        <jsp:include page="/references/?w=main"></jsp:include>
    </c:when>
    <c:when test="${what=='editreference'}">
        <jsp:include page="/references/?w=edit"></jsp:include>
    </c:when>

    <c:when test="${what=='stats'}">
        <jsp:include page="fragments/frag-statisticstable.jsp"></jsp:include>
    </c:when>
    </c:choose>
    </div>
</div>
<div id="referencelist" class="hidden">
    <h1>Search for reference <span class="info">cancelar</span></h1>
    <input type="text" placeholder="procure por autor, ano ou título" name="refquery"/>
    <div class="content"></div>
    <p>Para adicionar uma nova referência não listada, escolha a opção Bibliografia na barra de menu à esquerda.</p>
</div>
<div id="savingscreen"><div><img src="../images/loader.svg" class="ajaxloader"/>Gravando...</div></div>
</body>
</html>
