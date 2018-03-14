<%@ tag description="Inventory row" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ attribute name="fields" required="true" type="java.lang.String[]" %>
<%@ attribute name="inv" required="false" type="pt.floraon.occurrences.entities.Inventory"%>
<%@ attribute name="tax" required="false" type="pt.floraon.occurrences.entities.OBSERVED_IN"%>

<jsp:useBean id="collapseField" class="java.util.HashMap"/>
<c:forEach var="flf" items="${fields}">
    <t:isoptionselected optionname="collapse-${flf}" value="true"><c:set target="${collapseField}" property="${flf}" value="true"/></t:isoptionselected>
</c:forEach>

<%-- the model for new taxa --%>
<c:if test="${tax == null}">
<tr class="geoelement dummy id2holder">
    <td class="selectcol clickable"><div class="selectbutton"></div><input type="hidden" name="occurrenceUuid" value=""/></td>
    <c:forEach var="flf" items="${fields}">
    <t:occurrence-cell field="${flf}" collapsed="${collapseField[flf]}" noInventory="true"/>
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
    <td class="hidden editable coordinates" data-name="observationCoordinates" data-lat="${tax.getObservationLatitude()}" data-lng="${tax.getObservationLongitude()}" data-symbol="2">${tax._getObservationCoordinates()}</td>
    <c:forEach var="flf" items="${fields}">
    <t:occurrence-cell taxon="${tax}" field="${flf}" collapsed="${collapseField[flf]}" noInventory="true"/>
    </c:forEach>
</tr>
</c:if>




<%--
<c:if test="${tax == null}">
<tr class="dummy id2holder geoelement">
    <td class="select clickable selectcol"><div class="selectbutton"></div><input type="hidden" name="occurrenceUuid" value=""/></td>
    <c:choose>
        <c:when test="${flavour == null || flavour == '' || flavour == 'simple'}">
        <td class="taxon editable" data-name="taxa"></td>
        <td class="editable" data-name="confidence"></td>
        <td class="editable" data-name="phenoState"></td>
        <td class="editable" data-name="abundance"></td>
        <td class="editable" data-name="coverIndex"></td>
        <td class="editable" data-name="comment"></td>
        <td class="editable" data-name="privateNote"></td>
        </c:when>

        <c:when test="${flavour == 'redlist'}">
        <td class="editable" data-name="gpsCode"></td>
        <td class="taxon editable" data-name="taxa"></td>
        <td class="editable" data-name="confidence"></td>
        <td class="editable" data-name="phenoState"></td>
        <td class="editable" data-name="abundance"></td>
        <td class="editable" data-name="typeOfEstimate"></td>
        <td class="editable" data-name="coverIndex"></td>
        <td class="editable" data-name="hasPhoto"></td>
        <td class="editable" data-name="hasSpecimen"></td>
        <td class="editable" data-name="comment"></td>
        <td class="editable" data-name="privateNote"></td>
        <td class="editable threats" data-name="specificThreats"></td>
        <td class="editable" data-name="presenceStatus"></td>
        </c:when>
    </c:choose>
</tr>
</c:if>

<c:if test="${tax != null}">
    <c:set var="unm" value="${tax.getTaxEnt() == null ? 'unmatched' : ''}"/>
    <tr class="${unm} id2holder geoelement">
        <td class="select clickable selectcol"><div class="selectbutton"></div>
            <input type="hidden" name="occurrenceUuid" value="${tax.getUuid()}"/>
            <input type="hidden" name="inventoryId" value="${inv.getID()}"/>
        </td>
        <td class="hidden editable coordinates" data-name="observationCoordinates" data-lat="${tax.getObservationLatitude()}" data-lng="${tax.getObservationLongitude()}" data-symbol="2">${tax._getObservationCoordinates()}</td>
        <c:set var="disptax" value="${tax.getTaxEnt() == null ? tax.getVerbTaxon() : tax.getTaxEnt().getName()}"/>
        <c:choose>
            <c:when test="${flavour == null || flavour == '' || flavour == 'simple'}">
            <td class="taxon editable" data-name="taxa">${disptax}</td>
            <td class="editable" data-name="confidence">${tax._getConfidenceLabel()}</td>
            <td class="editable" data-name="phenoState">${tax._getPhenoStateLabel()}</td>
            <td class="editable" data-name="abundance">${tax.getAbundance()}</td>
            <td class="editable" data-name="coverIndex">${tax.getCoverIndex()}</td>
            <td class="editable" data-name="comment">${tax.getComment()}</td>
            <td class="editable" data-name="privateNote">${tax.getPrivateComment()}</td>
            </c:when>

            <c:when test="${flavour == 'redlist'}">
            <td class="editable" data-name="gpsCode">${tax.getGpsCode()}</td>
            <td class="taxon editable" data-name="taxa">${disptax}</td>
            <td class="editable" data-name="confidence">${tax._getConfidenceLabel()}</td>
            <td class="editable" data-name="phenoState">${tax._getPhenoStateLabel()}</td>
            <td class="editable" data-name="abundance">${tax.getAbundance()}</td>
            <td class="editable" data-name="typeOfEstimate">${tax._getTypeOfEstimateLabel()}</td>
            <td class="editable" data-name="coverIndex">${tax.getCoverIndex()}</td>
            <td class="editable" data-name="hasPhoto">${tax._getHasPhotoLabel()}</td>
            <td class="editable" data-name="hasSpecimen">${tax.getHasSpecimen()}</td>
            <td class="editable" data-name="comment">${tax.getComment()}</td>
            <td class="editable" data-name="privateNote">${tax.getPrivateComment()}</td>
            <td class="editable threats" data-name="specificThreats">${tax.getSpecificThreats()}</td>
            <td class="editable" data-name="presenceStatus">${tax._getPresenceStatusLabel()}</td>
            </c:when>
        </c:choose>
    </tr>
</c:if>
--%>