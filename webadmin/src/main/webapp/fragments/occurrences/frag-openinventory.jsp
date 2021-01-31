<%@ page pageEncoding="UTF-8" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page session="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<fmt:setBundle basename="pt.floraon.occurrences.occurrencesMessages" />
<div class="button anchorbutton"><a href="?w=main&p=1"><fmt:message key="button.8"/></a></div>
<div class="button anchorbutton"><a href="?w=occurrenceview&p=1&filter=iid:${param.id}"><fmt:message key="button.11"/></a></div>
<%--
<div id="flavourlist">
    <div class="label"><fmt:message key="button.4a"/></div>
    <c:forEach var="flv" items="${flavourList}" varStatus="loop">
    <c:if test="${flv.getValue().showInInventoryView()}">
        <c:set var="sel" value="${(param.flavour == null || param.flavour == '') ? (loop.index == 0 ? 'selected' : '') : (param.flavour == flv.getKey() ? 'selected' : '')}"/>
        <div class="button anchorbutton ${sel}"><a href="?w=openinventory&flavour=${flv.getKey()}&id=${param.id}">${flv.getValue().getName()}</a></div>
    </c:if>
    </c:forEach>
</div>
--%>
<div id="flavourlist">
    <div class="label"><fmt:message key="button.4a"/></div>
    <t:option-radiobutton optionprefix="flavour" optionnames="${flavourList}" defaultvalue="simple" style="light" classes="small"/>
</div>
</div>  <!-- top buttons -->
<div id="deleteoccurrences" class="hidden">
    <form class="poster" data-path="occurrences/api/deleteoccurrences" data-refresh="true" data-confirm="true">
        <div class="heading2">
            <h2><fmt:message key="occurrences.5" /></h2>
            <input type="submit" class="textbutton" value="Delete"/>
        </div>
        <table id="deleteoccurrencetable" class="verysmalltext sortable">
            <thead><tr><t:occurrenceheader fields="${flavourfields}" view="inventory"/></tr></thead>
            <tbody></tbody>
        </table>
    </form>
</div>

<t:inventorymodel fields="${flavourfields}"/>

<form id="addnewinventories" class="poster hidden" data-path="occurrences/api/addoccurrences" data-refresh="true">
    <div class="heading2">
        <h2><fmt:message key="inventory.add"/></h2>
        <label><input type="checkbox" name="mainobserver" checked="checked"/> <fmt:message key="options.1"/><div class="legend"><fmt:message key="options.1.desc"/></div></label>
        <div class="button" id="deleteselectednew">Delete selected</div>
        <input type="submit" class="textbutton" value="Save"/>
    </div>
</form>

<div id="allinventories">
    <c:if test="${param.id == null}"><h2>Your inventories</h2></c:if>
    <c:forEach var="inv" items="${inventories}">
    <c:set var="locked" value="${inv.getMaintainer() != user.getID() && !user.canMODIFY_OCCURRENCES()}"/>
    <c:set var="editable" value="${locked ? '' : 'editable'}"/>
    <div class="inventory geoelement">
        <h3><fmt:message key="inventory.1"/> ${inv.getCode()} <c:if test="${inv._getInventoryLatitude() != null}">${inv._getInventoryCoordinates()}</c:if> - ${inv._getNumberOfTaxa()} taxa</h3>
        <c:if test="${!locked}">
        <form class="poster" data-path="occurrences/api/deleteoccurrences" data-confirm="true" data-callback="?w=main">
            <input type="hidden" name="inventoryId" value="${inv.getID()}"/>
            <input type="submit" class="textbutton" value="Delete inventory" style="float:left"/>
        </form>
        </c:if>
        <form class="poster id1holder" data-path="occurrences/api/updateinventory" data-refresh="true">
            <input type="hidden" name="inventoryId" value="${inv.getID()}"/>
            <c:if test="${!locked}"><input type="submit" class="textbutton onlywhenmodified" value="<fmt:message key="inventory.upd"/>"/></c:if>
            <c:if test="${inv._hasMultipleCoordinates()}"><br/><span class="info">This inventory has multiple coordinates</span></c:if>
            <table class="verysmalltext occurrencetable">
                <tr>
                    <th><fmt:message key="inventory.2"/></th><th><fmt:message key="inventory.2b"/></th>
                    <th><fmt:message key="inventory.4"/></th>
                    <th>Complete</th><th>Area (m<sup>2</sup>)</th>
                </tr>
                <tr>
                    <td class="field ${editable} coordinates" data-name="coordinates" data-lat="${inv._getInventoryLatitude()}" data-lng="${inv._getInventoryLongitude()}">${inv._getInventoryCoordinates()}</td>
                    <td class="field ${editable}" data-name="precision">${inv.getPrecision().toString()}</td>
                    <td class="field ${editable}" data-name="date" sorttable_customkey="${inv._getDateYMD()}">${inv._getDate()}</td>
                    <td class="field ${editable}" data-name="complete"><t:yesno test="${inv.getComplete()}"/></td>
                    <td class="field ${editable}" data-name="area">${inv.getArea()}</td>
                </tr>
            </table>
            <table class="verysmalltext occurrencetable">
                <thead><tr>
                   <th><fmt:message key="inventory.2a"/></th><th><fmt:message key="inventory.3"/></th>
                   <th><fmt:message key="inventory.5"/></th>
                </tr></thead>
                <tbody><tr>
                    <td class="field ${editable}" data-name="code">${inv.getCode()}</td>
                    <td class="field ${editable}" data-name="locality">${inv.getLocality()}</td>
                    <td class="field ${editable} authors" data-name="observers"><t:usernames idarray="${inv.getObservers()}" usermap="${userMap}"/></td>
                </tr></tbody>
            </table>
            <table class="verysmalltext occurrencetable">
                <thead><tr>
                    <th><fmt:message key="inventory.7"/></th><th>Public notes</th><th>Private notes</th><th><fmt:message key="inventory.8"/></th>
                </tr></thead>
                <tbody><tr>
                    <td class="field ${editable} multiline" data-name="habitat">${inv.getHabitat()}</td>
                    <td class="field ${editable} multiline" data-name="pubNotes">${inv.getPubNotes()}</td>
                    <td class="field ${editable} multiline" data-name="privNotes">${inv.getPrivNotes()}</td>
                    <td class="field ${editable} threats" data-name="threats">${inv.getThreats()}</td>
                </tr></tbody>
            </table>
            <c:if test="${inv._hasDuplicatedTaxa()}"><div class="warning">There are duplicated taxa in this inventory</div></c:if>
            <table class="verysmalltext occurrencetable sortable newoccurrencetable">
                <thead><tr><t:occurrenceheader fields="${flavourfields}" view="inventory"/></tr></thead>
                <tbody>
                    <c:forEach var="tax" items="${inv._getTaxa()}">
                    <t:inventoryrow tax="${tax}" inv="${inv}" fields="${flavourfields}" locked="${locked}"/>
                    </c:forEach>
                    <t:inventoryrow fields="${flavourfields}" locked="${locked}"/>
                </tbody>
                <tfoot></tfoot>
            </table>
            <c:if test="${!locked}">
            <div class="button" id="deleteselectedinv">Delete selected taxa</div>
            <div class="button newtaxon">Add taxon</div>
            </c:if>
        </form>
    </div>
    </c:forEach>
    <div style="height:200px">&nbsp;</div>
</div>
