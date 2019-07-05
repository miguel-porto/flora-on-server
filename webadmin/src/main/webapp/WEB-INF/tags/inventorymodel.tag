<%@ tag description="Inventory model" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>

<c:set var="language" value="${not empty param.language ? param.language : not empty language ? language : pageContext.response.locale}" scope="request" />
<fmt:setLocale value="${language}" />
<fmt:setBundle basename="pt.floraon.occurrences.occurrencesMessages" />

<!-- This is the model for the inventory. It will be cloned when adding new. -->
<div class="inventory dummy id1holder geoelement">
    <input type="hidden" name="inventoryId" value=""/>
    <h3><fmt:message key="inventory.1a"/></h3>
    <table class="verysmalltext occurrencetable">
        <tr>
            <th><fmt:message key="inventory.2"/></th><th><fmt:message key="inventory.2b"/></th>
            <th><fmt:message key="inventory.2a"/></th><th><fmt:message key="inventory.3"/></th>
            <th><fmt:message key="inventory.4"/></th><th><fmt:message key="inventory.5"/></th>
        </tr>
        <tr>
            <td class="field editable coordinates" data-name="coordinates"></td>
            <td class="field editable" data-name="precision"></td>
            <td class="field editable" data-name="code"></td>
            <td class="field editable" data-name="locality"></td>
            <td class="field editable" data-name="date"></td>
            <td class="field editable authors" data-name="observers"></td>
        </tr>
    </table>
    <table class="verysmalltext occurrencetable">
        <thead><tr>
            <th><fmt:message key="inventory.7"/></th><th><fmt:message key="inventory.8"/></th>
        </tr></thead>
        <tbody><tr>
            <td class="field editable" data-name="habitat"></td>
            <td class="field editable" data-name="threats"></td>
        </tr></tbody>
    </table>
    <table class="verysmalltext occurrencetable sortable newoccurrencetable">
        <t:occurrenceheader fields="${flavourfields}" noInventory="true"/>
        <tbody><t:inventoryrow fields="${flavourfields}"/></tbody>
    </table>
    <div class="button newtaxon">Add taxon</div>
</div>
