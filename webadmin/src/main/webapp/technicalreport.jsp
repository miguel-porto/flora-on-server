<%@ page pageEncoding="UTF-8" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="language" value="${not empty param.language ? param.language : not empty language ? language : pageContext.response.locale}" scope="request" />
<fmt:setLocale value="${language}" />
<fmt:setBundle basename="pt.floraon.redlistdata.fieldValues" />

<h1>Technical report for ${user.getFullName()}</h1>
<c:if test="${fromDate != null}"><h2>Period ${fromDate} to ${toDate}</h2></c:if>
<form>
    <input type="hidden" name="w" value="report"/>
    <input type="hidden" name="tag" value="Lista Alvo"/>
    <p>Período: de <input type="text" name="fromdate" value="${fromDate}" placeholder="${fromDate == null ? 'dd-mm-yyyy' : ''}"/> até <input type="text" name="todate" value="${toDate}" placeholder="${toDate == null ? 'dd-mm-yyyy' : ''}"/></p>
    <input type="submit" class="textbutton" value="Realizar relatório"/>
</form>

<c:if test="${fromDate != null && toDate != null}">
<c:url value="/redlist/api/report-ninv" var="url1">
  <c:param name="territory" value="${territory}" />
  <c:param name="fromdate" value="${fromDate}" />
  <c:param name="todate" value="${toDate}" />
</c:url>

<c:url value="/redlist/api/report-listtaxatag" var="url3">
  <c:param name="territory" value="${territory}" />
  <c:param name="fromdate" value="${fromDate}" />
  <c:param name="todate" value="${toDate}" />
  <c:param name="tag" value="Lista Alvo" />
</c:url>

<c:url value="/redlist/api/report-listtaxatagphoto" var="url4">
  <c:param name="territory" value="${territory}" />
  <c:param name="fromdate" value="${fromDate}" />
  <c:param name="todate" value="${toDate}" />
  <c:param name="tag" value="Lista Alvo" />
</c:url>

<c:url value="/redlist/api/report-listtaxatagspecimen" var="url5">
  <c:param name="territory" value="${territory}" />
  <c:param name="fromdate" value="${fromDate}" />
  <c:param name="todate" value="${toDate}" />
  <c:param name="tag" value="Lista Alvo" />
</c:url>

<c:url value="/redlist/api/report-listtaxatagnrrecords" var="url6">
  <c:param name="territory" value="${territory}" />
  <c:param name="fromdate" value="${fromDate}" />
  <c:param name="todate" value="${toDate}" />
  <c:param name="tag" value="Lista Alvo" />
</c:url>

<c:url value="/redlist/api/report-listutmsquares" var="url7">
  <c:param name="fromdate" value="${fromDate}" />
  <c:param name="todate" value="${toDate}" />
</c:url>

<c:url value="/redlist/api/report-listprotectedareas" var="url8">
  <c:param name="fromdate" value="${fromDate}" />
  <c:param name="todate" value="${toDate}" />
</c:url>

<table class="small">
    <thead><tr><th colspan="2">Indicadores</th></tr></thead>
    <tr><td>Nº de inventários</td><td><t:ajaxloadhtml url="${url1}" /><p class="legend">Este número não tem em conta o nº de taxa observados no inventário, é contabilizado um inventário independentemnte de quantos taxa contém.</p></td></tr>
    <tr><td>Taxa alvo registados</td><td><t:ajaxloadhtml url="${url3}" /></td></tr>
    <tr><td>Taxa alvo registados com fotografia</td><td><t:ajaxloadhtml url="${url4}" /></td></tr>
    <tr><td>Taxa alvo colhidos</td><td><t:ajaxloadhtml url="${url5}" /></td></tr>
    <tr><td>Nº registos por taxon alvo</td><td><t:ajaxloadhtml url="${url6}" /></td></tr>
    <tr><td>Quadrículas UTM visitadas</td><td><t:ajaxloadhtml url="${url7}" /><p class="legend">Nota: no caso de quadrículas 2x2 km, a notação MGRS apresentada corresponde à quadrícula 1x1 km do quadrante SW.</p></td></tr>
    <tr><td>Áreas protegidas visitadas</td><td><t:ajaxloadhtml url="${url8}" /><p class="legend">Nota: no caso de sobreposição de áreas protegidas, o mesmo registo pode aparecer contabilizado em várias linhas.</p></td></tr>
</table>
</c:if>