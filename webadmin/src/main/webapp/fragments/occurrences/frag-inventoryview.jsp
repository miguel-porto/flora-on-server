<%@ page pageEncoding="UTF-8" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page session="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<fmt:setBundle basename="pt.floraon.occurrences.occurrencesMessages" />
<div class="button anchorbutton"><a href="?w=uploads"><fmt:message key="button.1"/></a></div>
<div class="button anchorbutton"><a href="?w=occurrenceview&p=1"><fmt:message key="button.4"/></a></div>
<c:if test="${user.canMODIFY_OCCURRENCES()}"><t:optionbutton optionname="allusers" title="All users inventories" defaultvalue="false"/></c:if>
</div>  <!-- top buttons -->

<t:inventorymodel />

<form id="addnewinventories" class="poster hidden" data-path="occurrences/api/addoccurrences" data-refresh="true">
    <div class="heading2">
        <h2><fmt:message key="inventory.add"/></h2>
        <label><input type="checkbox" name="mainobserver" checked="checked"/> <fmt:message key="options.1"/><div class="legend"><fmt:message key="options.1.desc"/></div></label>
        <!--<label><input type="checkbox" name="createTaxa"/> <fmt:message key="options.3"/><div class="legend"><fmt:message key="options.3.desc"/></div></label>-->
        <div class="button" id="deleteselectednew">Delete selected</div>
        <input type="submit" class="textbutton" value="Save"/>
    </div>
</form>

<div class="heading2">
    <h2>Your inventories - ${nrtotaloccurrences}</h2>
    <t:isoptionselected optionname="allusers" value="false">
    <div class="button anchorbutton"><a href="?w=openinventory">Expand all inventories</a></div>
    </t:isoptionselected>
    <div class="button" id="addemptyinventory">Novo</div>
    <div id="occurrencefilter">
        <form method="get" action="occurrences" class="inlineblock">
            <input type="hidden" name="w" value="${param.w}" />
            <input type="hidden" name="flavour" value="${param.flavour}" />
            <input type="hidden" name="p" value="1" />
            <input type="text" name="filter" style="width:250px" placeholder="<fmt:message key="occurrences.1e"/>" value="${filter}"/>
            <t:helpbutton msgid="filterhelp"><t:filterhelp /></t:helpbutton>
            <input type="submit" class="button" value="Filter" />
        </form>
        <form method="get" action="occurrences" class="inlineblock">
            <input type="hidden" name="w" value="${param.w}" />
            <input type="hidden" name="flavour" value="${param.flavour}" />
            <input type="hidden" name="p" value="1" />
            <input type="hidden" name="filter" value="date:na" />
            <input type="submit" class="button" value="Sem data" />
        </form>
        <form method="get" action="occurrences" class="inlineblock">
            <input type="hidden" name="w" value="${param.w}" />
            <input type="hidden" name="flavour" value="${param.flavour}" />
            <input type="hidden" name="p" value="1" />
            <input type="hidden" name="filter" value="tag:lista*alvo" />
            <input type="submit" class="button" value="Lista Alvo" />
        </form>
        <c:if test="${filter != null && filter != ''}">
        <form method="get" action="occurrences" class="inlineblock">
            <input type="hidden" name="w" value="${param.w}" />
            <input type="hidden" name="flavour" value="${param.flavour}" />
            <input type="hidden" name="p" value="1" />
            <input type="hidden" name="filter" value="" />
            <input type="submit" class="button" value="Show all" />
        </form>
        </c:if>
    </div>
    <t:pager />
</div>
<table id="inventorysummary" class="occurrencetable verysmalltext sortable">
    <tr><th><fmt:message key="inventory.2a"/></th><th><fmt:message key="inventory.3"/></th>
    <th><fmt:message key="inventory.4"/></th><th><fmt:message key="inventory.2c"/></th><th>Species</th></tr>
    <c:forEach var="inv" items="${inventories}">
    <tr class="geoelement">
        <td data-name="code">${inv.getCode()}</td>
        <td data-name="locality">${inv.getLocality()}</td>
        <td sorttable_customkey="${inv._getDateYMD()}" data-name="date">${inv._getDate()}</td>
        <td class="coordinates" data-lat="${inv._getLatitude()}" data-lng="${inv._getLongitude()}">${inv._getInventoryCoordinates()}</td>
        <td class="taxon"><a href="?w=openinventory&flavour=redlist&id=${inv._getIDURLEncoded()}"><c:if test="${inv._hasDuplicatedTaxa()}"><span class="warning">duplicated taxa</span> </c:if>${inv._getSampleTaxa(100)}</a></td>
    </tr>
</c:forEach>
</table>
