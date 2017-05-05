<%@ tag description="Inventory model" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<c:set var="language" value="${not empty param.language ? param.language : not empty language ? language : pageContext.response.locale}" scope="request" />
<fmt:setLocale value="${language}" />
<fmt:setBundle basename="pt.floraon.occurrences.occurrencesMessages" />

<!-- This is the model for the inventory. It will be cloned when adding new. -->
<div class="inventory dummy id1holder">
    <input type="hidden" name="inventoryId" value=""/>
    <h3><fmt:message key="inventory.1"/></h3>
    <table class="verysmalltext occurrencetable">
        <tr>
            <th><fmt:message key="inventory.2"/></th><th><fmt:message key="inventory.2b"/></th>
            <th><fmt:message key="inventory.2a"/></th><th><fmt:message key="inventory.3"/></th>
            <th><fmt:message key="inventory.4"/></th><th><fmt:message key="inventory.5"/></th>
        </tr>
        <tr>
            <td class="field coordinates" data-name="coordinates"></td>
            <td class="field editable" data-name="gridPrecision"></td>
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
        <thead>
            <tr>
                <th class="hidden"></th>
                <th class="smallcol">GPS</th>
                <th class="bigcol">Taxon</th>
                <th class="smallcol">Conf</th>
                <th class="smallcol">Fen</th>
                <th class="smallcol">Nº</th>
                <th class="smallcol">Met</th>
                <th class="smallcol">Fot</th>
                <th class="smallcol">Colh</th>
                <th class="smallcol">Comment</th>
            </tr>
        </thead>
        <tbody>
            <tr class="dummy id2holder geoelement">
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
            </tr>
        </tbody>
    </table>
    <div class="button newtaxon hidden">Add taxon</div>
</div>

<form id="addnewinventories" class="poster hidden" data-path="/floraon/occurrences/api/addoccurrences" data-refresh="true">
    <div class="heading2">
        <h2><fmt:message key="inventory.add"/></h2>
        <label><input type="checkbox" name="mainobserver" checked="checked"/> <fmt:message key="options.1"/><div class="legend"><fmt:message key="options.1.desc"/></div></label>
        <div class="button" id="deleteselectednew">Delete selected</div>
        <input type="submit" class="textbutton" value="Save"/>
    </div>
</form>
