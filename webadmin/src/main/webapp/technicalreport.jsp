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

<c:url value="/redlist/api/report-ntaxatag" var="url2">
  <c:param name="territory" value="${territory}" />
  <c:param name="fromdate" value="${fromDate}" />
  <c:param name="todate" value="${toDate}" />
  <c:param name="tag" value="Lista Alvo" />
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

<table class="small">
    <thead><tr><th colspan="2">Indicadores</th></tr></thead>
    <tr><td>Nº de inventários</td><td><t:ajaxloadhtml url="${url1}" /></td></tr>
    <tr><td>Nº de taxa alvo registados</td><td><t:ajaxloadhtml url="${url2}" /></td></tr>
    <tr><td>Taxa alvo registados</td><td><t:ajaxloadhtml url="${url3}" /></td></tr>
    <tr><td>Taxa alvo registados com fotografia</td><td><t:ajaxloadhtml url="${url4}" /></td></tr>
</table>
</c:if>