<%@ tag description="Occurrence table row" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ attribute name="flavour" required="false" %>
<%@ attribute name="occ" required="false" type="pt.floraon.occurrences.entities.Inventory"%>
<%@ attribute name="userMap" required="false" type="java.util.Map" %>

<c:if test="${occ == null}">
<tr class="geoelement dummy id1holder">
    <td class="select clickable"><div class="selectbutton"></div></td>
    <c:choose>
    <c:when test="${param.flavour == null || param.flavour == '' || param.flavour == 'simple'}">
    <td class="taxon editable" data-name="taxa"></td>
    <td class="editable" data-name="confidence"></td>
    <td class="editable coordinates" data-name="coordinates"></td>
    <td class="editable" data-name="precision"></td>
    <td class="editable" data-name="comment"></td>
    <td class="editable" data-name="privateNote"></td>
    <td class="editable" data-name="date"></td>
    <td class="editable" data-name="phenoState"></td>
    <td class="editable authors" data-name="observers"></td>
    </c:when>

    <c:when test="${param.flavour == 'redlist'}">
    <td class="editable" data-name="date"></td>
    <td class="editable authors hideincompactview" data-name="observers"></td>
    <td class="editable coordinates hideincompactview" data-name="coordinates"></td>
    <td class="editable" data-name="locality"></td>
    <td class="editable" data-name="precision"></td>
    <td class="editable" data-name="gpsCode"></td>
    <td class="taxon editable" data-name="taxa"></td>
    <td class="editable hideincompactview" data-name="confidence"></td>
    <td class="editable hideincompactview" data-name="phenoState"></td>
    <td class="editable hideincompactview" data-name="abundance"></td>
    <td class="editable hideincompactview" data-name="typeOfEstimate"></td>
    <td class="editable hideincompactview" data-name="hasPhoto"></td>
    <td class="editable hideincompactview" data-name="hasSpecimen"></td>
    <td class="threats editable" data-name="specificThreats"></td>
    <td class="editable" data-name="comment"></td>
    <td class="editable" data-name="privateNote"></td>
    </c:when>

    <c:when test="${param.flavour == 'herbarium'}">
    <td class="taxon editable" data-name="taxa"></td>
    <td class="editable" data-name="verbLocality"></td>
    <td class="editable coordinates" data-name="coordinates"></td>
    <td class="editable" data-name="labelData"></td>
    <td class="editable" data-name="date"></td>
    <td class="editable authors" data-name="collectors"></td>
    <td class="editable authors" data-name="determiners"></td>
    </c:when>
    </c:choose>
</tr>
</c:if>

<c:if test="${occ != null}">
<c:if test="${occ._getTaxa()[0].getTaxEnt() == null}">
<tr class="unmatched geoelement id1holder">
</c:if>
<c:if test="${occ._getTaxa()[0].getTaxEnt() != null}">
<tr class="geoelement id1holder">
</c:if>
    <td class="select clickable">
        <input type="hidden" name="occurrenceUuid" value="${occ._getTaxa()[0].getUuid()}"/>
        <input type="hidden" name="inventoryId" value="${occ.getID()}"/>
        <div class="selectbutton"></div>
    </td>
    <c:choose>
    <c:when test="${param.flavour == null || param.flavour == '' || param.flavour == 'simple'}">
    <c:if test="${occ._getTaxa()[0].getTaxEnt() == null}">
        <td class="taxon editable" data-name="taxa">${occ._getTaxa()[0].getVerbTaxon()}</td>
    </c:if>
    <c:if test="${occ._getTaxa()[0].getTaxEnt() != null}">
        <td class="taxon editable" data-name="taxa">${occ._getTaxa()[0].getTaxEnt().getNameWithAnnotationOnly(false)}</td>
    </c:if>
    <td class="editable" data-name="confidence">${occ._getTaxa()[0].getConfidence()}</td>
    <td class="editable coordinates" data-name="observationCoordinates" data-lat="${occ.getLatitude()}" data-lng="${occ.getLongitude()}">${occ._getCoordinates()}</td>
    <td class="editable" data-name="precision">${occ.getPrecision().toString()}</td>
    <td class="editable" data-name="comment">${occ._getTaxa()[0].getComment()}</td>
    <td class="editable" data-name="privateNote">${occ._getTaxa()[0].getPrivateComment()}</td>
    <td class="editable" data-name="date" sorttable_customkey="${occ._getDateYMD()}">${occ._getDate()}</td>
    <td class="editable" data-name="phenoState">${occ._getTaxa()[0].getPhenoState()}</td>
    <td class="editable authors" data-name="observers"><t:usernames idarray="${occ.getObservers()}" usermap="${userMap}"/></td>
    </c:when>

    <c:when test="${param.flavour == 'redlist'}">
    <td class="editable" data-name="date" sorttable_customkey="${occ._getDateYMD()}">${occ._getDate()}</td>
    <td class="editable authors hideincompactview" data-name="observers"><t:usernames idarray="${occ.getObservers()}" usermap="${userMap}"/></td>
    <td class="editable coordinates hideincompactview" data-name="observationCoordinates" data-lat="${occ.getLatitude()}" data-lng="${occ.getLongitude()}">${occ._getCoordinates()}</td>
    <td class="editable" data-name="locality">${occ.getLocality()}</td>
    <td class="editable" data-name="precision">${occ.getPrecision().toString()}</td>
    <td class="editable" data-name="gpsCode">${occ.getCode()}</td>
    <c:if test="${occ._getTaxa()[0].getTaxEnt() == null}">
        <td class="taxon editable" data-name="taxa">${occ._getTaxa()[0].getVerbTaxon()}</td>
    </c:if>
    <c:if test="${occ._getTaxa()[0].getTaxEnt() != null}">
        <td class="taxon editable" data-name="taxa">${occ._getTaxa()[0].getTaxEnt().getName()}</td>
    </c:if>
    <td class="editable hideincompactview" data-name="confidence">${occ._getTaxa()[0].getConfidence()}</td>
    <td class="editable hideincompactview" data-name="phenoState">${occ._getTaxa()[0].getPhenoState()}</td>
    <td class="editable hideincompactview" data-name="abundance">${occ._getTaxa()[0].getAbundance()}</td>
    <td class="editable hideincompactview" data-name="typeOfEstimate">${occ._getTaxa()[0].getTypeOfEstimate()}</td>
    <td class="editable hideincompactview" data-name="hasPhoto">${occ._getTaxa()[0].getHasPhoto().getLabel()}</td>
    <td class="editable hideincompactview" data-name="hasSpecimen">${occ._getTaxa()[0].getHasSpecimen()}</td>
    <td class="threats editable" data-name="specificThreats">${occ._getTaxa()[0].getSpecificThreats()}</td>
    <td class="editable" data-name="comment">${occ._getTaxa()[0].getComment()}</td>
    <td class="editable" data-name="privateNote">${occ._getTaxa()[0].getPrivateComment()}</td>
    </c:when>

    <c:when test="${param.flavour == 'herbarium'}">
    <td class="editable" data-name="accession">${occ._getTaxa()[0].getAccession()}</td>
    <c:if test="${occ._getTaxa()[0].getTaxEnt() == null}">
        <td class="taxon editable" data-name="taxa">${occ._getTaxa()[0].getVerbTaxon()}</td>
    </c:if>
    <c:if test="${occ._getTaxa()[0].getTaxEnt() != null}">
        <td class="taxon editable" data-name="taxa">${occ._getTaxa()[0].getTaxEnt().getName()}</td>
    </c:if>
    <td class="editable coordinates" data-name="observationCoordinates" data-lat="${occ.getLatitude()}" data-lng="${occ.getLongitude()}">${occ._getCoordinates()}</td>
    <td class="editable" data-name="precision">${occ.getPrecision().toString()}</td>
    <td class="editable" data-name="verbLocality">${occ.getVerbLocality()}</td>
    <td class="editable" data-name="date" sorttable_customkey="${occ._getDateYMD()}">${occ._getDate()}</td>
    <td class="editable authors" data-name="collectors"><t:usernames idarray="${occ.getCollectors()}" usermap="${userMap}"/></td>
    <td class="editable" data-name="labelData">${occ._getTaxa()[0].getLabelData()}</td>
    <td class="editable authors" data-name="determiners"><t:usernames idarray="${occ.getDets()}" usermap="${userMap}"/></td>
    </c:when>
    </c:choose>
</tr>
</c:if>