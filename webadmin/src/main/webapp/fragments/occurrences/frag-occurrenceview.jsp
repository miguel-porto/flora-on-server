<%@ page pageEncoding="UTF-8" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page session="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<fmt:setBundle basename="pt.floraon.occurrences.occurrencesMessages" />
<div id="topbuttons">
    <a class="homebutton" href="${contextPath == '' ? '/' : contextPath}"><img src="${contextPath}/images/home.png" alt="home"/></a>
    <div class="button anchorbutton hideincompactview"><a href="?w=uploads"><fmt:message key="button.1"/></a></div>
    <div class="button anchorbutton"><a href="?w=main&p=1"><fmt:message key="button.2"/></a></div>
    <div class="button anchorbutton"><a href="?w=downloadoccurrencetable"><fmt:message key="button.9"/></a></div>
    <div class="button anchorbutton hideincompactview"><a href="?w=downloadspeciestable"><fmt:message key="button.10"/></a></div>
    <t:optionbutton optionname="advancedview" title="Advanced view" defaultvalue="false" persistent="true"/>
    <c:if test="${user.canMODIFY_OCCURRENCES()}"><t:optionbutton optionname="allusers" title="All users occurrences" defaultvalue="false"/></c:if>
    <div id="flavourlist" class="hideincompactview">
        <div class="label"><fmt:message key="button.4a"/></div>
        <t:option-radiobutton optionprefix="flavour" optionnames="${flavourList}" defaultvalue="simple" style="light" classes="small"/>
    </div>
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
        <input type="submit" class="textbutton" value="Save"/>
        <div class="button" id="deleteselectednew">Delete selected</div>
        <div class="button" id="cancelnew"><fmt:message key="occurrences.cancel"/></div>
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
    <div class="heading2" id="maintableheader">
        <h2 class="hideincompactview"><fmt:message key="${sessionScope['option-allusers'] ? 'occurrences.6' : (sessionScope['option-viewAsObserver'] ? 'occurrences.7' : 'occurrences.1')}"/> - ${nrtotaloccurrences}
        <c:if test="${(filter != null && filter != '') || baseFilter != null}"> [filtered <t:ajaxloadhtml url="${contextPath}/occurrences/api/countNumberFilteredOccurrences?w=occurrences" classes="inlineblock"/>]
            <t:isoptionselected optionname="advancedview">
            <c:if test="${baseFilter == null}">
            <c:url value="" var="urlNofilter">
                <c:param name="w" value="${param.w}" />
                <c:param name="p" value="1" />
                <c:param name="filter" value="" />
            </c:url>
            <form class="poster" style="display:inline-flex; vertical-align:middle" data-path="occurrences/api/saveFilter" data-callback="${urlNofilter}">
                <input type="hidden" name="filter" value="${filter}" />
                <input type="text" name="filterName" placeholder="name" style="width:100px"/>
                <input type="hidden" name="refreshURL" value="${filter}" />
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
        <div class="button icon" id="newoccurrence"><img src="${contextPath}/images/add.png"/><span><fmt:message key="occurrences.1a"/></span></div>
        <div class="button icon" id="deleteselected"><img src="${contextPath}/images/delete.png"/><span><fmt:message key="occurrences.1b"/></span></div>
        <div class="button hideincompactview" id="mergeocc"><fmt:message key="occurrences.1c"/></div>
        <div class="button icon" id="updatemodified"><img src="${contextPath}/images/ic_menu_save.png"/><span><fmt:message key="inventory.upd"/></span></div>
        <t:isoptionselected optionname="allusers" value="false"><t:optionbutton optionname="viewAsObserver" title="As observer" defaultvalue="false" /></t:isoptionselected>
        <c:if test="${nproblems > 0}"><div class="button anchorbutton"><a href="?w=fixissues"><fmt:message key="button.3"/></a></div></c:if>
        <jsp:include page="frag-filterpanel.jsp"></jsp:include>
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
