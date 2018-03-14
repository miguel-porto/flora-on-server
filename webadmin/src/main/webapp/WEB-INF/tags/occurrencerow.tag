<%@ tag description="Occurrence table row" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ attribute name="occ" required="false" type="pt.floraon.occurrences.entities.Inventory"%>
<%@ attribute name="fields" required="true" type="java.lang.String[]" %>
<%@ attribute name="userMap" required="false" type="java.util.Map" %>
<%@ attribute name="locked" required="false" type="java.lang.Boolean" %>
<%@ attribute name="symbol" required="false" %>
<%@ attribute name="cssclass" required="false" %>

<%-- This iterates through all the field names and outputs a table cell per each, formatted accordingly --%>

<jsp:useBean id="collapseField" class="java.util.HashMap"/>
<c:forEach var="flf" items="${fields}">
    <t:isoptionselected optionname="collapse-${flf}" value="true"><c:set target="${collapseField}" property="${flf}" value="true"/></t:isoptionselected>
</c:forEach>

<c:if test="${occ == null}">
<tr class="geoelement dummy id1holder">
    <td class="selectcol clickable"><div class="selectbutton"></div></td>
    <c:forEach var="flf" items="${fields}">
    <t:occurrence-cell field="${flf}" collapsed="${collapseField[flf]}" symbol="${symbol}"/>
    </c:forEach>
</tr>
</c:if>

<c:if test="${occ != null}">
<c:set var="unmatched" value="${occ._getTaxa()[0].getTaxEnt() == null ? 'unmatched' : ''}"/>
<tr class="${unmatched} geoelement id1holder ${cssclass}">
    <td class="selectcol clickable">
        <input type="hidden" name="occurrenceUuid" value="${occ._getTaxa()[0].getUuid()}"/>
        <input type="hidden" name="inventoryId" value="${occ.getID()}"/>
        <div class="selectbutton"></div>
    </td>

    <c:forEach var="flf" items="${fields}">
    <t:occurrence-cell inventory="${occ}" field="${flf}" collapsed="${collapseField[flf]}" userMap="${userMap}" locked="${locked}" symbol="${symbol}"/>
    </c:forEach>
</tr>
</c:if>