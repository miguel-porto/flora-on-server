<%@ page pageEncoding="UTF-8" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page session="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<fmt:setBundle basename="pt.floraon.occurrences.occurrencesMessages" />
<div class="button anchorbutton"><a href="?w=uploads"><fmt:message key="button.1"/></a></div>
<div class="button anchorbutton"><a href="?w=main&p=1"><fmt:message key="button.2"/></a></div>
<div class="button anchorbutton"><a href="?w=downloadoccurrencetable"><fmt:message key="button.9"/></a></div>
<c:if test="${user.canMODIFY_OCCURRENCES()}"><t:optionbutton optionname="allusers" title="All users occurrences" defaultvalue="false"/></c:if>
<div id="flavourlist">
    <div class="label"><fmt:message key="button.4a"/></div>
    <t:option-radiobutton optionprefix="flavour" optionnames="${flavourList}" defaultvalue="simple" style="light" classes="small"/>
</div>
<%--
<div id="flavourlist">
    <div class="label"><fmt:message key="button.4a"/></div>
    <c:forEach var="flv" items="${flavourList}" varStatus="loop">
    <c:if test="${flv.getValue().showInOccurrenceView()}">
        <c:set var="sel" value="${(param.flavour == null || param.flavour == '') ? (loop.index == 0 ? 'selected' : '') : (param.flavour == flv.getKey() ? 'selected' : '')}"/>
        <div class="button anchorbutton ${sel}"><a href="?w=occurrenceview&flavour=${flv.getKey()}">${flv.getValue().getName()}</a></div>
    </c:if>
    </c:forEach>
</div>
--%>
</div>  <!-- top buttons -->
<div id="deleteoccurrences" class="hidden">
    <form class="poster" data-path="occurrences/api/deleteoccurrences" data-refresh="true" data-confirm="true">
        <div class="heading2">
            <h2><fmt:message key="occurrences.5" /></h2>
            <input type="submit" class="textbutton" value="Delete"/>
            <div class="button" id="canceldelete"><fmt:message key="occurrences.cancel"/></div>
        </div>
        <table id="deleteoccurrencetable" class="verysmalltext sortable">
            <thead><tr><t:occurrenceheader fields="${flavourfields}" view="occurrence" noSortButton="true"/></tr></thead>
            <tbody></tbody>
        </table>
    </form>
</div>

<div id="updateoccurrences" class="hidden">
    <form class="poster" data-path="occurrences/api/updateoccurrences" data-refresh="true">
        <div class="heading2">
            <h2>Confirm updating the following occurrences</h2>
            <label><input type="checkbox" name="createUsers"/> <fmt:message key="options.2"/><div class="legend"><fmt:message key="options.2.desc"/></div></label>
            <!--<label><input type="checkbox" name="createTaxa"/> <fmt:message key="options.3"/><div class="legend"><fmt:message key="options.3.desc"/></div></label>-->
            <input type="submit" class="textbutton" value="Update"/>
            <div class="button" id="cancelupdate"><fmt:message key="occurrences.cancel"/></div>
        </div>
        <table id="updateoccurrencetable" class="verysmalltext sortable occurrencetable">
            <thead><tr><t:occurrenceheader fields="${flavourfields}" view="occurrence" noSortButton="true"/></tr></thead>
            <tbody></tbody>
        </table>
    </form>
</div>

<form id="addnewoccurrences" class="poster hidden" data-path="occurrences/api/addoccurrences" data-refresh="true">
    <div class="heading2">
        <h2><fmt:message key="inventory.add1"/></h2>
        <c:if test="${sessionScope['option-flavour'] != 'herbarium'}">
        <label><input type="checkbox" name="mainobserver" checked="checked"/> <fmt:message key="options.1"/><div class="legend"><fmt:message key="options.1.desc"/></div></label>
        </c:if>
        <label><input type="checkbox" name="createUsers"/> <fmt:message key="options.2"/><div class="legend"><fmt:message key="options.2.desc"/></div></label>
        <!--<label><input type="checkbox" name="createTaxa"/> <fmt:message key="options.3"/><div class="legend"><fmt:message key="options.3.desc"/></div></label>-->
        <div class="button" id="deleteselectednew">Delete selected</div>
        <input type="submit" class="textbutton" value="Save"/>
    </div>
    <table id="addoccurrencetable" class="verysmalltext occurrencetable sortable">
        <thead><tr><t:occurrenceheader fields="${flavourfields}" view="occurrence" noSortButton="true"/></tr></thead>
        <tbody>
            <t:occurrencerow fields="${flavourfields}" view="occurrence"/>
        </tbody>
    </table>
</form>

<form id="mergeoccurrences" data-path="occurrences/api/mergeoccurrences" method="post" enctype="multipart/form-data" class="hidden poster">
    <div class="heading2">
        <h2>Confirm merge occurrences in the same inventory</h2>
        <input type="submit" class="textbutton" value="Merge"/>
    </div>
    <table id="mergeoccurrencetable" class="verysmalltext">
        <thead><tr><t:occurrenceheader fields="${flavourfields}" view="occurrence" noSortButton="true"/></tr></thead>
        <tbody></tbody>
    </table>
</form>

<%--
<c:if test="${nproblems > 0}">
<div id="warningpanel" class="warning">
    <p><fmt:message key="error.7"/></p>
    <fmt:message key="error.11"/><br/>
    <div class="button anchorbutton"><a href="?w=fixissues"><fmt:message key="button.3"/></a></div>
</div>
</c:if>
--%>

<div id="alloccurrences">
    <div class="heading2">
        <h2 class="hideincompactview"><fmt:message key="${sessionScope['option-allusers'] ? 'occurrences.6' : (sessionScope['option-viewAsObserver'] ? 'occurrences.7' : 'occurrences.1')}"/> - ${nrtotaloccurrences}
        <c:if test="${filter != null && filter != ''}"> [filtered <t:ajaxloadhtml url="${contextPath}/occurrences/api/countNumberFilteredOccurrences?w=occurrences" classes="inlineblock"/>]</c:if>
        </h2>
        <div class="button icon" id="newoccurrence"><img src="${contextPath}/images/add.png"/><span><fmt:message key="occurrences.1a"/></span></div>
        <div class="button icon" id="deleteselected"><img src="${contextPath}/images/delete.png"/><span><fmt:message key="occurrences.1b"/></span></div>
        <div class="button hideincompactview" id="mergeocc"><fmt:message key="occurrences.1c"/></div>
        <div class="button icon" id="updatemodified"><img src="${contextPath}/images/ic_menu_save.png"/><span><fmt:message key="inventory.upd"/></span></div>
        <%-- <c:if test="${sessionScope['option-flavour'] == 'redlist'}"></c:if> --%>
        <t:optionbutton optionname="compactview" title="Compact" defaultvalue="false" />
        <t:isoptionselected optionname="allusers" value="false"><t:optionbutton optionname="viewAsObserver" title="As observer" defaultvalue="false" /></t:isoptionselected>
        <c:if test="${nproblems > 0}"><div class="button anchorbutton"><a href="?w=fixissues"><fmt:message key="button.3"/></a></div></c:if>
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
            <div class="button anchorbutton"><a href="${url1}">Sem data</a></div>
            <c:url value="" var="url2">
                <c:param name="w" value="${param.w}" />
                <c:param name="p" value="1" />
                <c:param name="filter" value="${filter} detected:1" />
            </c:url>
            <div class="button anchorbutton"><a href="${url2}">Só detectados</a></div>
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
            <div class="button anchorbutton"><a href="${url4}">Clear filter</a></div>
            </c:if>
        </div>
        <c:if test="${occurrenceNewFeature != null}"><div class="newfeature">${occurrenceNewFeature}</div></c:if>
        <t:pager />
    </div>
    <%--TODO: AJAX <t:ajaxloadhtml url="${contextPath}/occurrences?w=fetchOccurrenceRows"/>--%>
    <table id="alloccurrencetable" class="verysmalltext occurrencetable">
        <thead><tr><t:occurrenceheader fields="${flavourfields}" view="occurrence"/></tr></thead>
        <tbody>
        <c:forEach var="occ" items="${occurrences}">
            <t:occurrencerow fields="${flavourfields}" occ="${occ}" userMap="${userMap}" view="occurrence" locked="${(occ.getMaintainer() != user.getID() && !user.canMODIFY_OCCURRENCES()) || occ.getReadOnly()}"/>
        </c:forEach>
        <c:forEach var="occ" items="${externaloccurrences}">
            <t:occurrencerow fields="${flavourfields}" occ="${occ}" userMap="${userMap}" locked="true" cssclass="external" view="occurrence"
                symbol="${occ.getOccurrence().getConfidence().toString() == 'DOUBTFUL' ? 1 : (occ.getOccurrence().getPresenceStatus() == null || occ.getOccurrence().getPresenceStatus().toString() == 'ASSUMED_PRESENT' ? 2 : 1)}"/>
        </c:forEach>
        </tbody>
    </table>
</div>
