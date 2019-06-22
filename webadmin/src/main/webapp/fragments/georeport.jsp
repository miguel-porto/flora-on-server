<%@ page pageEncoding="UTF-8" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="language" value="${not empty param.language ? param.language : not empty language ? language : pageContext.response.locale}" scope="request" />
<fmt:setLocale value="${language}" />
<fmt:setBundle basename="pt.floraon.redlistdata.fieldValues" />

<h1>Geographical report</h1>
<c:if test="${param.polygon == null}">
<form action="" id="wktform" method="POST">
    <p>Paste a polygon in WKT format here, in latitude longitude coordinates, for example:<br/><code>Polygon ((-7.6167 37.9335, -7.6221 37.9320, -7.6228 37.9285, -7.6185 37.9256, -7.6144 37.9249, -7.6128 37.9287, -7.6127 37.9304, -7.6146 37.9325, -7.6167 37.9335))</code><br/><span class="info">You can copy a polygon from QGIS and paste it!</span></p>
    <input type="hidden" name="w" value="report"/>
    <input type="hidden" name="type" value="geo"/>
    <textarea style="width: 98%; height: 150px; border: 2px solid #1e88e5; margin: 0 1%; padding: 4px; font-size: 0.75em; border-radius: 3px;" name="polygon"></textarea>
    <input type="submit" value="Produzir relatório" class="textbutton"/>
</form>
</c:if>

<c:if test="${param.polygon != null}">
<table class="small">
    <tr><th>Statistic<br/><img class="exclamation" src="../images/exclamation.png"/> Apenas LVF</th><th>Value</th></tr>
    <tr><td class="title">Number of inventories</td><td>${nInventories}</td></tr>
    <tr><td class="title">Number of taxa</td><td>${nSpecies}</td></tr>
    <tr><td class="title">Number of endemic taxa</td><td>${nEndemic}</td></tr>
    <tr><td class="title">Number of threatened taxa</td><td>${nThreatened}</td></tr>
</table>

<h2>Descarregamentos</h2>
<table class="small">
<tr>
    <td>Tabela dos taxa dentro do polígono (inc. Flora-On)</td>
    <td>
        <form class="poster" data-path="api/downloadtaxainpolygon" data-callback="?w=jobs#jobs">
            <input type="hidden" name="polygon" value="${param.polygon}"/>
            <input type="hidden" name="territory" value="${territory}"/>
            <input type="submit" value="Descarregar" class="textbutton"/>
        </form>
    </td>
</tr>
<tr>
    <td>Todas as ocorrências dentro do polígono<br/><img class="exclamation" src="../images/exclamation.png"/> Apenas LVF</td>
    <td>
        <form action="api/downloadoccurrencesinpolygon" method="POST">
            <input type="hidden" name="polygon" value="${param.polygon}"/>
            <input type="text" name="filter" placeholder="use um filtro avançado"/>
            <t:helpbutton msgid="filterhelp"><t:filterhelp /></t:helpbutton>
            <input type="submit" value="Descarregar" class="textbutton"/>
        </form>
    </td>
</tr>
<tr>
    <td>Ocorrências actuais e de confiança dentro do polígono (inc. Flora-On)</td>
    <td>
        <form class="poster" data-path="api/downloadredlistoccurrencesinpolygon" data-callback="?w=jobs#jobs">
            <input type="hidden" name="polygon" value="${param.polygon}"/>
            <input type="hidden" name="territory" value="${territory}"/>
            <input type="submit" value="Ordenar descarregamento" class="textbutton"/>
        </form>
    </td>
</tr>
<tr>
    <td>PDF dos inventários dentro do polígono<br/><img class="exclamation" src="../images/exclamation.png"/> Apenas LVF</td>
    <td>
        <form action="api/downloadinventoriesPDF" method="POST">
            <input type="hidden" name="polygon" value="${param.polygon}"/>
            <input type="text" name="filter" placeholder="use um filtro avançado"/>
            <t:helpbutton msgid="filterhelp"><t:filterhelp /></t:helpbutton>
            <input type="submit" value="Descarregar" class="textbutton"/>
        </form>
    </td>
</tr>
</table>

<table class="small sortable">
    <tr><th>List of recorded taxa<br/><img class="exclamation" src="../images/exclamation.png"/> Apenas LVF</th><th>Endemic</th><th>Threat category</th></tr>
    <c:forEach var="res" items="${speciesList}">
    <tr><td>${res.key.getNameWithAnnotationOnly(true)}</td><td>${res.value['endemic'] == 'true' ? 'Endémica' : ''}</td><td>${res.value['category']}</td></tr>
    </c:forEach>
</table>

<p>Current polygon:<br/><code>${param.polygon}</code></p>
</c:if>

<c:if test="${message != null}">
<div class="warning">${message}</div>
</c:if>


