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
<div id="flavourlist">
    <div class="label"><fmt:message key="button.4a"/></div>
    <t:option-radiobutton optionprefix="flavour" optionnames="${flavourList}" defaultvalue="simple" style="light" classes="small"/>
</div>
<t:inventorymodel fields="${flavourfields}"/>

<form id="addnewinventories" class="poster hidden" data-path="occurrences/api/addoccurrences" data-refresh="true">
    <div class="heading2">
        <h2><fmt:message key="inventory.add"/></h2>
        <label><input type="checkbox" name="mainobserver" checked="checked"/> <fmt:message key="options.1"/><div class="legend"><fmt:message key="options.1.desc"/></div></label>
        <!--<label><input type="checkbox" name="createTaxa"/> <fmt:message key="options.3"/><div class="legend"><fmt:message key="options.3.desc"/></div></label>-->
        <div class="button" id="deleteselectednew">Delete selected</div>
        <input type="submit" class="textbutton" value="Save"/>
    </div>
</form>

<div id="updateoccurrences" class="hidden">
    <form class="poster" data-path="occurrences/api/updateoccurrences" data-refresh="true">
        <div class="heading2">
            <h2><fmt:message key="inventory.upd1"/></h2>
            <label><input type="checkbox" name="createUsers"/> <fmt:message key="options.2"/><div class="legend"><fmt:message key="options.2.desc"/></div></label>
            <input type="submit" class="textbutton" value="Update"/>
            <div class="button" id="cancelupdate"><fmt:message key="occurrences.cancel"/></div>
        </div>
<%--
        <table id="updateoccurrencetable" class="verysmalltext sortable occurrencetable">
            <tr>
                <th class="sorttable_nosort selectcol clickable"><div class="selectbutton"></div></th>
                <th><fmt:message key="inventory.2a"/></th><th><fmt:message key="inventory.3"/></th>
                <th><fmt:message key="inventory.4"/></th><th><fmt:message key="inventory.2c"/></th>
                <th>Species</th>
            </tr>
            <tbody></tbody>
        </table>
--%>
        <table id="updateoccurrencetable" class="verysmalltext sortable occurrencetable">
            <thead><tr>
                <t:occurrenceheader fields="${summaryfields}" view="inventorySummary"/>
            </tr></thead>
            <tbody></tbody>
        </table>
    </form>
</div>
<div class="heading2">
    <h2>Your inventories - ${nrtotaloccurrences}</h2>
    <%--<t:isoptionselected optionname="allusers" value="false"><div class="button anchorbutton"><a href="?w=openinventory">Expand all inventories</a></div></t:isoptionselected>--%>
    <div class="button" id="addemptyinventory">Novo</div>
    <div class="button" id="updatemodified"><fmt:message key="inventory.upd"/></div>
    <div id="occurrencefilter">
        <form method="get" action="occurrences" class="inlineblock">
            <input type="hidden" name="w" value="${param.w}" />
            <input type="hidden" name="p" value="1" />
            <input type="text" name="filter" style="width:250px" placeholder="<fmt:message key="occurrences.1e"/>" value="${filter}"/>
            <t:helpbutton msgid="filterhelp"><t:filterhelp /></t:helpbutton>
            <input type="submit" class="button" value="Filter" />
        </form>
        <form method="get" action="occurrences" class="inlineblock">
            <input type="hidden" name="w" value="${param.w}" />
            <input type="hidden" name="p" value="1" />
            <input type="hidden" name="filter" value="date:na" />
            <input type="submit" class="button" value="Sem data" />
        </form>
        <form method="get" action="occurrences" class="inlineblock">
            <input type="hidden" name="w" value="${param.w}" />
            <input type="hidden" name="p" value="1" />
            <input type="hidden" name="filter" value="tag:lista*alvo" />
            <input type="submit" class="button" value="Lista Alvo" />
        </form>
        <c:if test="${filter != null && filter != ''}">
        <form method="get" action="occurrences" class="inlineblock">
            <input type="hidden" name="w" value="${param.w}" />
            <input type="hidden" name="p" value="1" />
            <input type="hidden" name="filter" value="" />
            <input type="submit" class="button" value="Show all" />
        </form>
        </c:if>
    </div>
    <t:pager />
</div>
<div class="newfeature">NOVO! Esta tabela agora é directamente editável!</div>
<div id="alloccurrences">
    <table id="alloccurrencetable" class="occurrencetable verysmalltext sortable inventorysummary">
        <thead><tr>
            <t:occurrenceheader fields="${summaryfields}" view="inventorySummary"/>
        </tr></thead>
        <tbody>
        <c:forEach var="inv" items="${inventories}">
            <t:occurrencerow fields="${summaryfields}" occ="${inv}" userMap="${userMap}" view="inventorySummary" />
        </c:forEach>
        </tbody>
    </table>
</div>

<%--
<div id="alloccurrences">
    <table id="alloccurrencetable" class="occurrencetable verysmalltext sortable inventorysummary">
        <tr>
            <th class="sorttable_nosort selectcol clickable"><div class="selectbutton"></div></th>
            <th><fmt:message key="inventory.2a"/></th><th><fmt:message key="inventory.3"/></th>
            <th><fmt:message key="inventory.4"/></th><th><fmt:message key="inventory.2c"/></th>
            <th>Species</th>
        </tr>
        <c:forEach var="inv" items="${inventories}">
        <tr class="geoelement id1holder">
            <td class="selectcol clickable">
                <input type="hidden" name="inventoryId" value="${inv.getID()}"/>
                <div class="selectbutton"></div>
            </td>
            <td data-name="code" class="editable">${inv.getCode()}</td>
            <td data-name="locality" class="editable">${inv.getLocality()}</td>
            <td sorttable_customkey="${inv._getDateYMD()}" data-name="date" class="editable">${inv._getDate()}</td>
            <td class="coordinates editable" data-lat="${inv._getLatitude()}" data-lng="${inv._getLongitude()}" data-name="coordinates">${inv._getInventoryCoordinates()}</td>
            <td class="taxon"><a href="?w=openinventory&id=${inv._getIDURLEncoded()}"><c:if test="${inv._hasDuplicatedTaxa()}"><span class="warning">duplicated taxa</span> </c:if>${inv._getSampleTaxa(100)}</a></td>
        </tr>
        </c:forEach>
    </table>
</div>
--%>
<%--
<table id="inventorysummary" class="occurrencetable verysmalltext sortable">
    <tr><th><fmt:message key="inventory.2a"/></th><th><fmt:message key="inventory.3"/></th>
    <th><fmt:message key="inventory.4"/></th><th><fmt:message key="inventory.2c"/></th><th>Species</th></tr>
    <c:forEach var="inv" items="${inventories}">
    <tr class="geoelement">
        <td data-name="code">${inv.getCode()}</td>
        <td data-name="locality">${inv.getLocality()}</td>
        <td sorttable_customkey="${inv._getDateYMD()}" data-name="date">${inv._getDate()}</td>
        <td class="coordinates" data-lat="${inv._getLatitude()}" data-lng="${inv._getLongitude()}">${inv._getInventoryCoordinates()}</td>
        <td class="taxon"><a href="?w=openinventory&id=${inv._getIDURLEncoded()}"><c:if test="${inv._hasDuplicatedTaxa()}"><span class="warning">duplicated taxa</span> </c:if>${inv._getSampleTaxa(100)}</a></td>
    </tr>
</c:forEach>
</table>
--%>