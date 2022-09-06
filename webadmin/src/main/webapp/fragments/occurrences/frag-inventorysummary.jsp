<%@ page pageEncoding="UTF-8" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page session="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<fmt:setBundle basename="pt.floraon.occurrences.occurrencesMessages" />
<div id="topbuttons">
    <a class="homebutton" href="${contextPath}"><img src="${contextPath}/images/home.png" alt="home"/></a>
    <div class="button anchorbutton hideincompactview"><a href="?w=uploads"><fmt:message key="button.1"/></a></div>
    <div class="button anchorbutton"><a href="?w=occurrenceview&p=1"><fmt:message key="button.4"/></a></div>
    <t:isoptionselected optionname="advancedview" value="false"><div class="button anchorbutton"><a href="?w=downloadoccurrencetable"><fmt:message key="button.9"/></a></div></t:isoptionselected>
    <t:optionbutton optionname="advancedview" title="Advanced view" defaultvalue="false" persistent="true"/>
    <div class="button anchorbutton hideincompactview"><a href="?w=downloadinventorytable"><fmt:message key="button.12"/></a></div>
    <c:if test="${user.canMODIFY_OCCURRENCES()}"><t:optionbutton optionname="allusers" title="All users inventories" defaultvalue="false"/></c:if>
    <div id="flavourlist" class="hideincompactview">
        <div class="label"><fmt:message key="button.4a"/></div>
        <t:option-radiobutton optionprefix="flavour" optionnames="${flavourList}" defaultvalue="simple" style="light" classes="small"/>
    </div>
</div>  <!-- top buttons -->
<div class="inventory dummy id1holder geoelement">
<t:inventorymodel fields="${flavourfields}"/>
</div>

<form id="addnewinventories" class="poster hidden" data-path="occurrences/api/addoccurrences" data-refresh="true">
    <div class="heading2">
        <h2><fmt:message key="inventory.add"/></h2>
        <label><input type="checkbox" name="mainobserver" checked="checked"/> <fmt:message key="options.1"/><div class="legend"><fmt:message key="options.1.desc"/></div></label>
        <!--<label><input type="checkbox" name="createTaxa"/> <fmt:message key="options.3"/><div class="legend"><fmt:message key="options.3.desc"/></div></label>-->
        <input type="submit" class="textbutton" value="Save"/>
        <div class="button" id="deleteselectednew">Delete selected</div>
        <div class="button" id="cancelnew"><fmt:message key="occurrences.cancel"/></div>
    </div>
</form>

<div id="deleteoccurrences" class="hidden">
    <form class="poster" data-path="occurrences/api/deleteoccurrences" data-refresh="true" data-confirm="true">
        <div class="heading2">
            <h2><fmt:message key="occurrences.5" /></h2>
            <input type="submit" class="textbutton" value="Delete"/>
            <div class="button" id="canceldelete"><fmt:message key="occurrences.cancel"/></div>
        </div>
        <table id="deleteoccurrencetable" class="verysmalltext sortable">
            <thead><tr><t:occurrenceheader fields="${summaryfields}" view="inventorySummary" noSortButton="true"/></tr></thead>
            <tbody></tbody>
        </table>
    </form>
</div>

<div id="updateoccurrences" class="hidden">
    <form class="poster" data-path="occurrences/api/updateoccurrences" data-refresh="true">
        <div class="heading2">
            <h2><fmt:message key="inventory.upd1"/></h2>
            <label><input type="checkbox" name="createUsers"/> <fmt:message key="options.2"/><div class="legend"><fmt:message key="options.2.desc"/></div></label>
            <input type="submit" class="textbutton" value="Update"/>
            <div class="button" id="cancelupdate"><fmt:message key="occurrences.cancel"/></div>
        </div>
        <table id="updateoccurrencetable" class="verysmalltext sortable occurrencetable">
            <thead><tr><t:occurrenceheader fields="${summaryfields}" view="inventorySummary" noSortButton="true"/></tr></thead>
            <tbody></tbody>
        </table>
    </form>
</div>
<div class="heading2" id="maintableheader">
    <h2 class="hideincompactview">${sessionScope['option-allusers'] ? 'All inventories' : (sessionScope['option-viewAsObserver'] ? "Inventories where you're listed as observer"  : 'Your inventories')} - ${nrtotaloccurrences}
    <c:if test="${(filter != null && filter != '') || baseFilter != null}"> [filtered <t:ajaxloadhtml url="${contextPath}/occurrences/api/countNumberFilteredOccurrences?w=inventories" classes="inlineblock"/>]
        <t:isoptionselected optionname="advancedview">
            <c:if test="${baseFilter == null}">
            <form class="poster" style="display:inline-flex; vertical-align:middle" data-path="occurrences/api/saveFilter" data-refresh="true">
                <input type="hidden" name="filter" value="${filter}" />
                <input type="text" name="filterName" placeholder="name" style="width:100px"/>
                <input type="submit" class="button singleline" value="<fmt:message key='occurrences.1i'/>" />
            </form>
            </c:if>
            <c:if test="${baseFilter != null}">
            <form class="poster" style="display:inline-flex; vertical-align:middle" data-path="occurrences/api/deleteSavedFilter" data-refresh="true">
                <input type="hidden" name="filter" value="${baseFilter}" />
                <input type="submit" class="button singleline" value="<fmt:message key='occurrences.1j'/>" />
            </form>
            </c:if>
        </t:isoptionselected>
    </c:if>
    </h2>
    <%--<t:isoptionselected optionname="allusers" value="false"><div class="button anchorbutton"><a href="?w=openinventory">Expand all inventories</a></div></t:isoptionselected>--%>
    <div class="button icon" id="addemptyinventory"><img src="${contextPath}/images/add.png"/><span>Novo</span></div>
    <div class="button icon" id="updatemodified"><img src="${contextPath}/images/ic_menu_save.png"/><span><fmt:message key="inventory.upd"/></span></div>
    <div class="button icon" id="deleteselected"><img src="${contextPath}/images/delete.png"/><span><fmt:message key="occurrences.1b"/></span></div>
    <t:isoptionselected optionname="allusers" value="false"><t:optionbutton optionname="viewAsObserver" title="As observer" defaultvalue="false" /></t:isoptionselected>
    <jsp:include page="frag-filterpanel.jsp"></jsp:include>
    <t:pager />
</div>
<t:isoptionselected optionname="advancedview" value="false"><div class="newfeature info">Para editar as espécies individualmente, pode usar a <a href="?w=occurrenceview">vista de ocorrências</a> ou abrir o inventário desejado.</div></t:isoptionselected>
<div id="alloccurrences">
    <table id="alloccurrencetable" class="occurrencetable verysmalltext inventorysummary">
        <thead><tr>
            <t:occurrenceheader fields="${summaryfields}" view="inventorySummary"/>
        </tr></thead>
        <tbody>
        <c:forEach var="inv" items="${inventories}">
            <t:occurrencerow fields="${summaryfields}" occ="${inv}" isInventory="true" userMap="${userMap}" view="inventorySummary" locked="${(inv.getMaintainer() != user.getID() && !user.canMODIFY_OCCURRENCES()) || inv.getReadOnly()}" />
        </c:forEach>
        </tbody>
    </table>
</div>