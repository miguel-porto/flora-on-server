<%@ tag description="Inventory row" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ attribute name="fields" required="true" type="pt.floraon.occurrences.fields.flavours.IOccurrenceFlavour" %>
<%@ attribute name="inv" required="false" type="pt.floraon.occurrences.entities.Inventory"%>
<%@ attribute name="tax" required="false" type="pt.floraon.occurrences.entities.OBSERVED_IN"%>
<%@ attribute name="locked" required="false" type="java.lang.Boolean" %>

<jsp:useBean id="collapseField" class="java.util.HashMap"/>
<c:forEach var="flf" items="${fields.getFields()}">
    <t:isoptionselected optionname="collapse-${flf}" value="true"><c:set target="${collapseField}" property="${flf}" value="true"/></t:isoptionselected>
</c:forEach>

<%-- the model for new taxa --%>
<c:if test="${tax == null && !locked}">
<tr class="geoelement dummy empty id2holder">
    <td class="selectcol clickable"><div class="selectbutton"></div><input type="hidden" name="occurrenceUuid" value=""/></td>
    <c:forEach var="flf" items="${fields.getFields()}">
    <t:occurrence-cell field="${flf}" collapsed="${collapseField[flf]}" view="inventory" fields="${fields}"/>
    </c:forEach>
</tr>
</c:if>

<c:if test="${tax != null}">
<c:set var="unmatched" value="${tax.getTaxEnt() == null ? 'unmatched' : ''}"/>
<tr class="${unmatched} geoelement id2holder">
    <td class="selectcol clickable">
        <input type="hidden" name="occurrenceUuid" value="${tax.getUuid()}"/>
        <input type="hidden" name="inventoryId" value="${inv.getID()}"/>
        <div class="selectbutton"></div>
    </td>
    <td class="hidden ${locked ? '' : 'editable'} coordinates" data-name="observationCoordinates" data-lat="${tax.getObservationLatitude()}" data-lng="${tax.getObservationLongitude()}" data-symbol="2">${tax._getObservationCoordinates()}</td>
    <c:forEach var="flf" items="${fields.getFields()}">
    <t:occurrence-cell taxon="${tax}" field="${flf}" collapsed="${collapseField[flf]}" view="inventory" fields="${fields}" locked="${locked}"/>
    </c:forEach>
</tr>
</c:if>