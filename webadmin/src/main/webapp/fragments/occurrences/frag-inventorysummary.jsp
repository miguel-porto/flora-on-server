<%@ page pageEncoding="UTF-8" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page session="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<fmt:setBundle basename="pt.floraon.occurrences.occurrencesMessages" />
<div class="button anchorbutton"><a href="?w=uploads"><fmt:message key="button.1"/></a></div>
<div class="button anchorbutton"><a href="?w=occurrenceview&p=1"><fmt:message key="button.4"/></a></div>
<div class="button anchorbutton"><a href="?w=downloadinventorytable"><fmt:message key="button.12"/></a></div>
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
        <table id="updateoccurrencetable" class="verysmalltext sortable occurrencetable">
            <thead><tr>
                <t:occurrenceheader fields="${summaryfields}" view="inventorySummary" noSortButton="true"/>
            </tr></thead>
            <tbody></tbody>
        </table>
    </form>
</div>
<div class="heading2">
    <h2>${sessionScope['option-allusers'] ? 'All inventories' : (sessionScope['option-viewAsObserver'] ? "Inventories where you're listed as observer"  : 'Your inventories')} - ${nrtotaloccurrences}
    <c:if test="${filter != null && filter != ''}"> [filtered <t:ajaxloadhtml url="${contextPath}/occurrences/api/countNumberFilteredOccurrences?w=inventories" classes="inlineblock"/>]</c:if>
    </h2>
    <%--<t:isoptionselected optionname="allusers" value="false"><div class="button anchorbutton"><a href="?w=openinventory">Expand all inventories</a></div></t:isoptionselected>--%>
    <div class="button icon" id="addemptyinventory"><img src="${contextPath}/images/add.png"/><span>Novo</span></div>
    <div class="button icon" id="updatemodified"><img src="${contextPath}/images/ic_menu_save.png"/><span><fmt:message key="inventory.upd"/></span></div>
    <t:isoptionselected optionname="allusers" value="false"><t:optionbutton optionname="viewAsObserver" title="As observer" defaultvalue="false" /></t:isoptionselected>
    <div id="occurrencefilter">
        <form method="get" action="occurrences" class="inlineblock">
            <input type="hidden" name="w" value="${param.w}" />
            <input type="hidden" name="p" value="1" />
            <input type="text" name="filter" style="width:300px" placeholder="<fmt:message key="occurrences.1e"/>" value="${filter}"/>
            <t:helpbutton msgid="filterhelp"><t:filterhelp /></t:helpbutton>
            <input type="submit" class="button" value="Filter" />
        </form>
        <c:url value="" var="url1">
            <c:param name="w" value="${param.w}" />
            <c:param name="p" value="1" />
            <c:param name="filter" value="${filter} date:na" />
        </c:url>
        <div class="button anchorbutton"><a href="${url1}"><fmt:message key="occurrences.1f"/></a></div>
        <c:url value="" var="url2">
            <c:param name="w" value="${param.w}" />
            <c:param name="p" value="1" />
            <c:param name="filter" value="${filter} detected:1" />
        </c:url>
        <div class="button anchorbutton"><a href="${url2}"><fmt:message key="occurrences.1g"/></a></div>
        <c:url value="" var="url3">
            <c:param name="w" value="${param.w}" />
            <c:param name="p" value="1" />
            <c:param name="filter" value="tag:lista alvo" />
        </c:url>
        <div class="button anchorbutton"><a href="${url3}">Lista Alvo</a></div>
        <c:if test="${filter != null && filter != ''}">
        <c:url value="" var="url4">
            <c:param name="w" value="${param.w}" />
            <c:param name="p" value="1" />
            <c:param name="filter" value="" />
        </c:url>
        <div class="button anchorbutton"><a href="${url4}"><fmt:message key="occurrences.1h"/></a></div>
        </c:if>
    </div>
    <t:pager />
</div>
<%-- <div class="newfeature">NOVO! Esta tabela agora é directamente editável!</div> --%>
<div id="alloccurrences">
    <table id="alloccurrencetable" class="occurrencetable verysmalltext inventorysummary">
        <thead><tr>
            <t:occurrenceheader fields="${summaryfields}" view="inventorySummary"/>
        </tr></thead>
        <tbody>
        <c:forEach var="inv" items="${inventories}">
            <t:occurrencerow fields="${summaryfields}" occ="${inv}" userMap="${userMap}" view="inventorySummary" locked="${(inv.getMaintainer() != user.getID() && !user.canMODIFY_OCCURRENCES()) || inv.getReadOnly()}" />
        </c:forEach>
        </tbody>
    </table>
</div>