<%@ tag description="Inventory row" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ attribute name="inv" required="false" type="pt.floraon.occurrences.entities.Inventory"%>
<%@ attribute name="tax" required="false" type="pt.floraon.occurrences.entities.OBSERVED_IN"%>

<c:if test="${tax == null}">
<tr class="dummy id2holder geoelement">
<!-- <td class="select clickable"><div class="selectbutton"></div><input type="hidden" name="occurrenceUuid" value=""/></td> -->
    <td class="hidden"><input type="hidden" name="occurrenceUuid" value=""/></td>
    <td class="editable" data-name="gpsCode"></td>
    <td class="taxon editable" data-name="taxa"></td>
    <td class="editable" data-name="confidence"></td>
    <td class="editable" data-name="phenoState"></td>
    <td class="editable" data-name="abundance"></td>
    <td class="editable" data-name="typeOfEstimate"></td>
    <td class="editable" data-name="hasPhoto"></td>
    <td class="editable" data-name="hasSpecimen"></td>
    <td class="editable" data-name="comment"></td>
    <td class="editable" data-name="privateNote"></td>
    <td class="editable threats" data-name="specificThreats"></td>
    <td class="editable" data-name="presenceStatus"></td>
</tr>
</c:if>

<c:if test="${tax != null}">
    <c:if test="${tax.getTaxEnt() == null}">
    <tr class="unmatched id2holder geoelement">
    </c:if>
    <c:if test="${tax.getTaxEnt() != null}">
    <tr class="id2holder geoelement">
    </c:if>
        <td class="hidden">
            <input type="hidden" name="occurrenceUuid" value="${tax.getUuid()}"/>
            <input type="hidden" name="inventoryId" value="${inv.getID()}"/>
        </td>
<!--
        <td class="select clickable"><div class="selectbutton"></div>
            <input type="hidden" name="occurrenceUuid" value="${tax.getUuid()}"/>
            <input type="hidden" name="inventoryId" value="${inv.getID()}"/>
        </td>
-->
        <td class="editable" data-name="gpsCode">${tax.getGpsCode()}</td>
        <c:if test="${tax.getTaxEnt() == null}">
        <td class="taxon editable" data-name="taxa">${tax.getVerbTaxon()}</td>
        </c:if>
        <c:if test="${tax.getTaxEnt() != null}">
        <td class="taxon editable" data-name="taxa">${tax.getTaxEnt().getName()}</td>
        </c:if>
        <td class="editable" data-name="confidence">${tax._getConfidenceLabel()}</td>
        <td class="editable" data-name="phenoState">${tax._getPhenoStateLabel()}</td>
        <td class="editable" data-name="abundance">${tax.getAbundance()}</td>
        <td class="editable" data-name="typeOfEstimate">${tax._getTypeOfEstimateLabel()}</td>
        <td class="editable" data-name="hasPhoto">${tax._getHasPhotoLabel()}</td>
        <td class="editable" data-name="hasSpecimen">${tax.getHasSpecimen()}</td>
        <td class="editable" data-name="comment">${tax.getComment()}</td>
        <td class="editable" data-name="privateNote">${tax.getPrivateComment()}</td>
        <td class="editable threats" data-name="specificThreats">${tax.getSpecificThreats()}</td>
        <td class="editable" data-name="presenceStatus">${tax._getPresenceStatusLabel()}</td>
    </tr>
</c:if>