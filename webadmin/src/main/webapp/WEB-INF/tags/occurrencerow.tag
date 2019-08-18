<%@ tag description="Occurrence table row" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ attribute name="occ" required="false" type="pt.floraon.occurrences.entities.Inventory"%>
<%@ attribute name="fields" required="true" type="pt.floraon.occurrences.fields.flavours.IOccurrenceFlavour" %>
<%@ attribute name="userMap" required="false" type="java.util.Map" %>
<%@ attribute name="locked" required="false" type="java.lang.Boolean" %>
<%@ attribute name="symbol" required="false" %>
<%@ attribute name="cssclass" required="false" %>
<%@ attribute name="view" required="true" type="java.lang.String" %>

<%-- This iterates through all the field names and outputs a table cell per each, formatted accordingly --%>

<jsp:useBean id="collapseField" class="java.util.HashMap"/>
<c:forEach var="flf" items="${fields.getFields()}">
    <t:isoptionselected optionname="collapse-${flf}" value="true"><c:set target="${collapseField}" property="${flf}" value="true"/></t:isoptionselected>
</c:forEach>

<c:if test="${occ == null}">
<tr class="geoelement dummy id1holder">
    <td class="selectcol clickable ${!fields.containsCoordinates() ? 'coordinates nodisplay' : ''}"><div class="selectbutton"></div></td>
    <c:if test="${view == 'inventorySummary'}"><td></td></c:if>
    <c:forEach var="flf" items="${fields.getFields()}">
    <t:occurrence-cell field="${flf}" collapsed="${collapseField[flf]}" symbol="${symbol}" fields="${fields}"
         view="${view}"/>
    </c:forEach>
</tr>
</c:if>

<c:if test="${occ != null}">
    <c:set var="unmatched" value="${occ._getTaxa()[0].getTaxEnt() == null ? 'unmatched' : ''}"/>
    <c:if test="${!fields.containsCoordinates()}">
    <c:set var="symbol" value="${((symbol == null || symbol == '') && occ != null) ? (occ.getYear() != null && occ.getYear() >= historicalYear ? 0 : 1) : symbol}"/>
    </c:if>

    <tr class="${unmatched} geoelement id1holder ${cssclass}">
        <c:if test="${!fields.containsCoordinates()}">
            <c:if test="${view == 'inventorySummary'}">
            <td class="selectcol clickable coordinates ${locked ? '' : 'editable nodisplay'}" data-name="inventoryCoordinates" data-lat="${occ._getInventoryLatitude()}" data-lng="${occ._getInventoryLongitude()}" data-symbol="${symbol}">
            </c:if>
            <c:if test="${view != 'inventorySummary'}">
            <td class="selectcol clickable coordinates ${locked ? '' : 'editable nodisplay'}" data-name="observationCoordinates" data-lat="${occ._getLatitude()}" data-lng="${occ._getLongitude()}" data-symbol="${symbol}">
            </c:if>
        </c:if>
        <c:if test="${fields.containsCoordinates()}">
        <td class="selectcol clickable">
        </c:if>
            <input type="hidden" name="occurrenceUuid" value="${occ._getTaxa()[0].getUuid()}"/>
            <input type="hidden" name="inventoryId" value="${occ.getID()}"/>
            <div class="selectbutton"></div>
        </td>
        <c:if test="${view == 'inventorySummary'}">
        <td><a href="?w=openinventory&id=${occ._getIDURLEncoded()}">open</a></td>
        </c:if>
        <c:forEach var="flf" items="${fields.getFields()}">
        <t:occurrence-cell inventory="${occ}" field="${flf}" collapsed="${collapseField[flf]}" userMap="${userMap}"
            locked="${locked}" symbol="${symbol}" fields="${fields}" view="${view}"/>
        </c:forEach>
    </tr>
</c:if>