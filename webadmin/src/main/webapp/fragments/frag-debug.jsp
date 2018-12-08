<%@ page pageEncoding="UTF-8" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page session="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<fmt:setBundle basename="pt.floraon.redlistdata.fieldValues" />
<h1>Diagnósticos</h1>
<h2>Avaliações com erros de aplicação de critérios ou categorias</h2>
<table>
<tr><th>Taxon</th><th>Erros encontrados</th></tr>
<c:forEach var="invSh" items="${invalidSheets}">
<tr>
    <td><a href="?w=taxon&id=${invSh.getTaxEnt()._getIDURLEncoded()}">${invSh.getTaxEnt().getNameWithAnnotationOnly(true)}</a></td>
    <td><ul>
        <c:forEach var="error" items="${invSh.validateCriteria()}">
        <li><fmt:message key="${error}"/></li>
        </c:forEach>
    </ul></td>
</tr>
</c:forEach>
</table>

<h2>Taxa severamente fragmentados</h2>
<table>
<tr><th>Taxon</th><th>Justificação</th></tr>
<c:forEach var="rlde" items="${severelyFragmented}">
<tr>
    <td><a href="?w=taxon&id=${rlde.getTaxEnt()._getIDURLEncoded()}">${rlde.getTaxEnt().getNameWithAnnotationOnly(true)}</a></td>
    <td>${rlde.getPopulation().getSeverelyFragmentedJustification()}</td>
</tr>
</c:forEach>
</table>
<h2>Taxa com flutuações extremas</h2>
<table>
<tr><th>Taxon</th><th>Justificação</th></tr>
<c:forEach var="rlde" items="${extremeFluctuations}">
<tr>
    <td><a href="?w=taxon&id=${rlde.getTaxEnt()._getIDURLEncoded()}">${rlde.getTaxEnt().getNameWithAnnotationOnly(true)}</a></td>
    <td>${rlde.getPopulation().getExtremeFluctuationsJustification()}</td>
</tr>
</c:forEach>
</table>
