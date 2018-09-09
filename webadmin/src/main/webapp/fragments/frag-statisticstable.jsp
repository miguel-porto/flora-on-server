<%@ page pageEncoding="UTF-8" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page session="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<fmt:setBundle basename="pt.floraon.redlistdata.fieldValues" />
<h1>Statistics</h1>
<table class="small">
    <thead>
    <tr><th colspan="3">Statistics</th></tr>
    <tr><th></th><th>All taxa</th><th>Lista Alvo</th></tr>
    </thead>
    <tr><td>Nº taxa com responsável</td><td class="bignumber">${nrWithResponsible_full}</td><td class="bignumber">${nrWithResponsible_tag}</td></tr>
    <tr><td>Nº taxa com textos em curso</td><td class="bignumber">${nrTextsInProgress_full}</td><td class="bignumber">${nrTextsInProgress_tag}</td></tr>
    <tr><td>Nº taxa com textos prontos, com avaliação preliminar pronta</td><td class="bignumber">${nrTextsAssessed_full}</td><td class="bignumber">${nrTextsAssessed_tag}</td></tr>
    <tr><td>Nº taxa revistos em revisão</td><td class="bignumber">${nrTextsAssessedReviewed_full}</td><td class="bignumber">${nrTextsAssessedReviewed_tag}</td></tr>
    <tr><td>Nº taxa revistos prontos para publicar</td><td class="bignumber">${nrReadyToPublish_full}</td><td class="bignumber">${nrReadyToPublish_tag}</td></tr>
</table>
<h2>All taxa</h2>
<c:forEach var="table" items="${statTables}">
<c:set var="tableTag" value="${statTablesTag.get(table.key)}"/>
<table class="sortable small">
<thead><tr><th>${table.key}</th><th>Counts for all taxa</th><th>Counts for Lista Alvo</th></tr></thead>
<c:forEach var="entry" items="${table.value}">
    <tr><td><fmt:message key="${entry.key}"/></td><td class="">${entry.value}</td><td class="">${tableTag.get(entry.key)}</td></tr>
</c:forEach>
</table>
</c:forEach>

<c:forEach var="table" items="${crossedStatTables}">
<table class="sortable small">
<thead><tr><th>${table.key}</th><c:forEach var="col" items="${table.value.columnKeySet()}"><th>${col}</th></c:forEach></tr></thead>
<c:forEach var="row" items="${table.value.rowMap()}">
<tr><td>${row.key}</td>
<c:forEach var="cell" items="${row.value}"><td>${cell.value}</td></c:forEach>
</tr>
</c:forEach>
</table>
</c:forEach>
