<%@ tag description="Occurrence table cell" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ attribute name="inventory" required="false" type="pt.floraon.occurrences.entities.Inventory"%>
<%@ attribute name="taxon" required="false" type="pt.floraon.occurrences.entities.OBSERVED_IN"%>
<%@ attribute name="field" required="true"%>
<%@ attribute name="noInventory" required="false" type="java.lang.Boolean" %>
<%@ attribute name="userMap" required="false" type="java.util.Map" %>
<%@ attribute name="locked" required="false" type="java.lang.Boolean" %>
<%@ attribute name="collapsed" required="false" type="java.lang.Boolean" %>
<%@ attribute name="symbol" required="false" %>
<%@ attribute name="fields" required="true" type="pt.floraon.occurrences.fields.flavours.IOccurrenceFlavour" %>

<c:set var="taxon" value="${taxon == null ? (inventory == null ? null : (inventory._getTaxa()[0])) : taxon}" />
<%--
    This tag translates a field name into a table cell
    It is the core functionality of the occurrence manager. If occ is provided, the cell is properly filled in.
--%>

<c:if test="${taxon != null}">
<c:set var="editable" value="${locked ? '' : 'editable'}"/>
<c:set var="collapsedClass" value="${collapsed ? 'collapsed' : ''}"/>
<c:set var="symbol" value="${((symbol == null || symbol == '') && inventory != null) ? (inventory.getYear() != null && inventory.getYear() >= historicalYear ? 0 : 1) : symbol}"/>
</c:if>

<c:if test="${taxon == null}">
<c:set var="editable" value="editable"/>
<c:set var="collapsedClass" value="${collapsed ? 'collapsed' : ''}"/>
</c:if>

<c:choose>
<%--    SPECIAL FIELDS      --%>
<c:when test="${field == 'taxa'}"><c:set var="taxa" value="${taxon == null ? '' : (taxon.getTaxEnt() == null ? taxon.getVerbTaxon() : taxon.getTaxEnt().getNameWithAnnotationOnly(false))}" /><td class="${editable} ${collapsedClass} taxon" data-name="taxa">${taxa}</td></c:when>
<c:when test="${field == 'coordinates' && !noInventory}"><c:set var="coordchanged" value="${taxon == null ? '' : (taxon.getCoordinatesChanged() ? 'textemphasis' : '')}" /><td class="${editable} ${collapsedClass} ${coordchanged} coordinates hideincompactview" data-name="observationCoordinates" data-lat="${inventory._getLatitude()}" data-lng="${inventory._getLongitude()}" data-symbol="${symbol}">${taxon == null ? '' : inventory._getCoordinates()}</td></c:when>
<c:when test="${field == 'date' && !noInventory}"><td class="${editable} ${collapsedClass}" data-name="date" sorttable_customkey="${inventory._getDateYMD()}">${inventory == null ? '' : inventory._getDate()}</td></c:when>
<%--
<c:when test="${field == 'observers' && !noInventory}"><td class="${editable} ${collapsedClass} authors hideincompactview" data-name="observers"><c:if test="${fn:length(inventory.getObservers()) > 0}"><t:usernames idarray="${inventory == null ? null : inventory.getObservers()}" usermap="${userMap}"/></c:if><c:if test="${fn:length(inventory.getObservers()) == 0}"><c:forEach var="id" items="${inventory._getObserverNames()}" varStatus="loop">${id}<c:if test="${!loop.last}">, </c:if></c:forEach></c:if></td></c:when>
<c:when test="${field == 'collectors' && !noInventory}"><td class="${editable} ${collapsedClass} authors hideincompactview" data-name="collectors"><t:usernames idarray="${inventory == null ? null : inventory.getCollectors()}" usermap="${userMap}"/></td></c:when>
<c:when test="${field == 'dets' && !noInventory}"><td class="${editable} ${collapsedClass} authors hideincompactview" data-name="dets"><t:usernames idarray="${inventory == null ? null : inventory.getDets()}" usermap="${userMap}"/></td></c:when>
--%>
<%--<c:when test="${field == 'tags' && !noInventory}"><td class="${editable} ${collapsedClass} hideincompactview" data-name="tags"><c:if test="${inventory != null}"><c:forEach var="tag" items="${inventory.getTags()}"><span class="wordtag">${tag}</span></c:forEach></c:if></td></c:when>--%>
<c:when test="${field == 'tags' && !noInventory}"><td class="${editable} ${collapsedClass} hideincompactview" data-name="tags"><t:usernames idarray="${inventory == null ? null : inventory.getTags()}"/></td></c:when>
<c:when test="${field == 'verbLocality' && !noInventory}"><td class="${collapsedClass}" data-name="verbLocality">${inventory == null ? '' : inventory.getVerbLocality()}</td></c:when>

<%-- These are concatenated fields, which are read-only --%>
<c:when test="${field == 'gpsCode_accession' && !noInventory}"><td class="${collapsedClass}">${inventory == null ? '' : inventory.getCode()} ${taxon == null ? '' : taxon.getAccession()}</td></c:when>
<c:when test="${field == 'locality_verbLocality' && !noInventory}"><td class="${collapsedClass}">${inventory == null ? '' : inventory.getVerbLocality()} ${inventory == null ? '' : inventory.getLocality()}</td></c:when>
<c:when test="${field == 'observers_collectors' && !noInventory}"><td class="${collapsedClass}"><t:usernames idarray="${inventory == null ? null : inventory.getObservers()}" usermap="${userMap}"/> <t:usernames idarray="${inventory == null ? null : inventory.getCollectors()}" usermap="${userMap}"/></td></c:when>
<c:when test="${field == 'comment_labelData'}"><td class="${collapsedClass}">${taxon == null ? '' : taxon.getComment()} ${taxon.getLabelData()}</td></c:when>

<%--    NORMAL FIELDS      --%>
<c:when test="${fields.isAuthorField(field) && !noInventory}"><td class="${editable} ${collapsedClass} ${fields.hideFieldInCompactView(field) ? 'hideincompactview' : ''} authors" data-name="${field}"><t:usernames idarray="${inventory == null ? null : fields.getFieldValueRaw(taxon, inventory, field)}" usermap="${userMap}"/></td></c:when>
<c:when test="${fields.isImageField(field)}"><td class="${editable} ${collapsedClass} imageupload" data-name="${field}"><c:if test="${taxon != null}"><c:forEach var="image" items="${fields.getFieldValueRaw(taxon, inventory, field)}"><c:if test="${image != null}"><img src="photos/${image}.jpg"/></c:if></c:forEach></c:if></td></c:when>
<c:otherwise>
    <c:if test="${!(fields.isInventoryField(field) && noInventory)}">
    <td class="${fields.isReadOnly(field) ? '' : editable} ${collapsedClass} ${fields.hideFieldInCompactView(field) ? 'hideincompactview' : ''}" data-name="${field}">${taxon == null ? '' : fields.getFieldValue(taxon, inventory, field)}</td>
    </c:if>
</c:otherwise>
</c:choose>
