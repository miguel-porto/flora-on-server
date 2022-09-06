<%@ page pageEncoding="UTF-8" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page session="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<fmt:setBundle basename="pt.floraon.occurrences.occurrencesMessages" />
<div id="occurrencefilter">
    <form method="get" action="occurrences" class="inlineblock">
        <input type="hidden" name="w" value="${param.w}" />
        <input type="hidden" name="p" value="1" />
        <%-- <input type="text" name="filter" style="width:300px" placeholder="<fmt:message key="occurrences.1e"/>" value="${filter}"/> --%>
        <textarea id="filter-textarea" rows="2" name="filter" style="width:400px" placeholder="<fmt:message key="occurrences.1e"/>">${filter}</textarea>
        <t:helpbutton msgid="filterhelp"><t:filterhelp /></t:helpbutton>
        <input type="submit" class="button" value="Filter" />
    </form>
    <c:url value="" var="url1">
        <c:param name="w" value="${param.w}" />
        <c:param name="p" value="1" />
        <c:param name="filter" value="${filter} date:na" />
    </c:url>
    <div class="button anchorbutton hideincompactview"><a href="${url1}"><fmt:message key="occurrences.1f"/></a></div>
    <c:url value="" var="url2">
        <c:param name="w" value="${param.w}" />
        <c:param name="p" value="1" />
        <c:param name="filter" value="${filter} detected:1" />
    </c:url>
    <div class="button anchorbutton hideincompactview"><a href="${url2}"><fmt:message key="occurrences.1g"/></a></div>
    <c:url value="" var="url3">
        <c:param name="w" value="${param.w}" />
        <c:param name="p" value="1" />
        <c:param name="filter" value="tag:lista alvo" />
    </c:url>
    <div class="button anchorbutton hideincompactview"><a href="${url3}">Lista Alvo</a></div>
    <c:if test="${filter != null && filter != ''}">
    <c:url value="" var="url4">
        <c:param name="w" value="${param.w}" />
        <c:param name="p" value="1" />
        <c:param name="filter" value="" />
    </c:url>
    <div class="button anchorbutton"><a href="${url4}"><fmt:message key="occurrences.1h"/></a></div>
    </c:if>
    <t:option-radiobutton optionprefix="baseFilter" optionnames="${savedFilters}" allowdeselect="true"/>
</div>
