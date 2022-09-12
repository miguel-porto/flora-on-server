<%@ tag description="Inventory model" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ attribute name="fields" required="true" type="pt.floraon.occurrences.fields.flavours.IOccurrenceFlavour" %>
<%@ attribute name="inv" required="false" type="pt.floraon.occurrences.entities.Inventory"%>
<%@ attribute name="showSave" required="false" type="java.lang.Boolean"%>
<jsp:useBean id="now" class="java.util.Date" />
<t:isoptionselected optionname="advancedview"><c:set var="advancedview" value="${true}" /></t:isoptionselected>
<t:isoptionselected optionname="advancedview" value="false"><c:set var="advancedview" value="${false}" /></t:isoptionselected>
<c:set var="language" value="${not empty param.language ? param.language : not empty language ? language : pageContext.response.locale}" scope="request" />
<fmt:setLocale value="${language}" />
<fmt:setBundle basename="pt.floraon.occurrences.occurrencesMessages" />

<!-- This is the model for the inventory. If empty, it will be cloned when adding new. -->

<input type="hidden" name="inventoryId" value="${inv == null ? '' : inv.getID()}"/>
<c:if test="${showSave}"><input type="submit" class="textbutton onlywhenmodified" value="<fmt:message key="inventory.upd"/>"/></c:if>
<c:if test="${!showSave}"><h3><fmt:message key="inventory.1a"/></h3></c:if>
<div class="verysmalltext occurrencetable inventoryheader">
<c:forEach var="field" items="${fields.getFields()}">
<c:if test="${fields.isInventoryField(field) && field != 'taxa'}">
<c:set var="cellClass" value="${fields.getFieldWidget(field, advancedview) == 'CHECKBOX' || fields.getFieldWidget(field, advancedview) == 'DROPDOWN' || fields.getFieldWidget(field, advancedview) == 'RADIO' || fields.getFieldWidget(field, advancedview) == 'DATE' ? ' widget' : ''}"/>
<table class="${cellClass} fieldsize_${fields.getFieldSize(field)}"><tr><th class="fieldsize_${fields.getFieldSize(field)}">${fields.getFieldName(field, advancedview)}</th></tr><tr><t:occurrence-cell field="${field}" view="inventorySummary" fields="${fields}" locked="${!showSave}" inventory="${inv}"/></tr></table>
</c:if>
</c:forEach>
</div>
<%--
<table class="verysmalltext occurrencetable">
    <tr>
        <th><fmt:message key="inventory.2"/></th><th><fmt:message key="inventory.2b"/></th>
        <th><fmt:message key="inventory.2a"/></th><th><fmt:message key="inventory.3"/></th>
        <th><fmt:message key="inventory.4"/></th><th><fmt:message key="inventory.5"/></th>
        <th>Complete</th>
    </tr>
    <tr>
    </tr>
    <tr>
        <td class="field editable coordinates" data-name="coordinates"></td>
        <td class="field editable" data-name="precision"></td>
        <td class="field editable" data-name="code"></td>
        <td class="field editable" data-name="locality"></td>
        <td class="field editable" data-name="date" style="width:0"><c:if test="${!advancedview}"><fmt:formatDate var="today" value="${now}" pattern="yyyy-MM-dd" /><input type="date" value="${today}"/></c:if><c:if test="${advancedview}"><fmt:formatDate var="today" value="${now}" pattern="dd-MM-yyyy" />${today}</c:if></td>
        <td class="field editable authors" data-name="observers"></td>
        <td class="field editable" data-name="complete"></td>
    </tr>
</table>
<table class="verysmalltext occurrencetable">
    <thead><tr>
        <th><fmt:message key="inventory.7"/></th><th>Public notes</th><th>Private notes</th><th><fmt:message key="inventory.8"/></th>
    </tr></thead>
    <tbody><tr>
        <td class="field editable multiline" data-name="habitat"></td>
        <td class="field editable multiline" data-name="pubNotes"></td>
        <td class="field editable multiline" data-name="privNotes"></td>
        <td class="field editable" data-name="threats"></td>
    </tr></tbody>
</table>
--%>
<c:if test="${inv != null && inv._hasDuplicatedTaxa()}"><div class="warning">There are duplicated taxa in this inventory</div></c:if>
<table class="verysmalltext occurrencetable sortable newoccurrencetable">
    <thead><tr><t:occurrenceheader fields="${fields}" view="inventory"/></tr></thead>
    <tbody>
    <c:if test="${inv == null}"><t:inventoryrow fields="${fields}"/></c:if>
    <c:if test="${inv != null}">
        <c:forEach var="tax" items="${inv._getTaxa()}">
        <t:inventoryrow tax="${tax}" inv="${inv}" fields="${fields}" locked="${!showSave}"/>
        </c:forEach>
        <t:inventoryrow fields="${fields}" locked="${!showSave}"/>
    </c:if>
    </tbody>
    <tfoot></tfoot>
</table>
<c:if test="${showSave}"><div class="button" id="deleteselectedinv">Delete selected taxa</div></c:if>
<c:if test="${showSave == null || showSave}"><div class="button newtaxon">Add taxon</div></c:if>
