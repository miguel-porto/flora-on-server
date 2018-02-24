<%@ page pageEncoding="UTF-8" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page session="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<c:set var="language" value="${not empty param.language ? param.language : not empty language ? language : pageContext.response.locale}" scope="request" />
<fmt:setLocale value="${language}" />
<fmt:setBundle basename="pt.floraon.occurrences.occurrencesMessages" />

<div id="taxonsearchwrapper-holder" class="editbox-home hidden">
    <div class="withsuggestions editbox" id="taxonsearchwrapper"><textarea id="taxonsearchbox" name="query" rows="4" placeholder="type taxon and Enter, or Esc to cancel | you can use taxon abbreviations like Clad and enter multiple taxa separated by +"></textarea><div id="suggestionstaxon"></div></div>
    <div class="withsuggestions editbox" id="authorsearchwrapper"><textarea id="authorsearchbox" name="query" rows="2" placeholder="type cell value and Enter, or Esc to cancel"></textarea><div id="suggestionsauthor"></div></div>
    <div class="withsuggestions editbox" id="threatsearchwrapper"><textarea id="threatsearchbox" name="query" rows="2" placeholder="type cell value and Enter, or Esc to cancel"></textarea><div id="suggestionsthreat"></div></div>
    <div class="withsuggestions editbox" id="editfieldwrapper"><input id="editfield" type="text" name="query" autocomplete="off" placeholder="type cell value and Enter, or Esc to cancel"/></div>
</div>
<div id="topbuttons" class="hideincompactview">
<a class="returntomain" href="./"><img src="images/cap-cor.png" alt="logo"/></a>
<!--<h1> ${user.getFullName()}</h1>-->
<!--<div class="button" id="selectpoints">Select</div>-->
<c:choose>
<c:when test="${param.w == null || param.w == 'main'}">
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
<%--
<form method="get" action="occurrences" class="inlineblock">
    <input type="hidden" name="w" value="${param.w}" />
    <input type="hidden" name="p" value="1" />
    <fmt:message key="occurrences.1d"/>: <input type="text" name="filter" style="width:250px" placeholder="<fmt:message key="occurrences.1e"/>" value="${filter}"/>
    <input type="submit" class="textbutton" value="Filter" />
</form>
<c:if test="${filter != null && filter != ''}">
<form method="get" action="occurrences" class="inlineblock">
    <input type="hidden" name="w" value="${param.w}" />
    <input type="hidden" name="p" value="1" />
    <input type="hidden" name="filter" value="" />
    <input type="submit" class="textbutton" value="Show all" />
</form>
</c:if>
--%>
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
            <td class="coordinates" data-lat="${inv.getLatitude()}" data-lng="${inv.getLongitude()}">${inv._getCoordinates()}</td>
            <td class="taxon"><a href="?w=openinventory&id=${inv._getIDURLEncoded()}">${inv._getSampleTaxa(100)}</a></td>
        </tr>
    </c:forEach>
    </table>
</c:when>

<c:when test="${param.w == 'openinventory'}">
    <div class="button anchorbutton"><a href="?w=uploads"><fmt:message key="button.1"/></a></div>
    <div class="button anchorbutton"><a href="?w=main&p=1"><fmt:message key="button.8"/></a></div>
    </div>  <!-- top buttons -->

    <div id="deleteoccurrences" class="hidden">
        <form class="poster" data-path="occurrences/api/deleteoccurrences" data-refresh="true" data-confirm="true">
            <div class="heading2">
                <h2><fmt:message key="occurrences.5" /></h2>
                <input type="submit" class="textbutton" value="Delete"/>
            </div>
            <table id="deleteoccurrencetable" class="verysmalltext sortable">
                <t:inventorytaxonheader />
                <tbody></tbody>
            </table>
        </form>
    </div>

    <t:inventorymodel />

    <form id="addnewinventories" class="poster hidden" data-path="occurrences/api/addoccurrences" data-refresh="true">
        <div class="heading2">
            <h2><fmt:message key="inventory.add"/></h2>
            <label><input type="checkbox" name="mainobserver" checked="checked"/> <fmt:message key="options.1"/><div class="legend"><fmt:message key="options.1.desc"/></div></label>
            <div class="button" id="deleteselectednew">Delete selected</div>
            <input type="submit" class="textbutton" value="Save"/>
        </div>
    </form>

    <div id="allinventories">
        <c:if test="${param.id == null}">
        <h2>Your inventories</h2>
        </c:if>
        <c:forEach var="inv" items="${inventories}">
        <div class="inventory geoelement">
        <!--<c:if test="${inv._getInventoryLatitude() == null}"><div class="inventory"></c:if>
        <c:if test="${inv._getInventoryLatitude() != null}"><div class="inventory geoelement"></c:if>-->
            <h3><fmt:message key="inventory.1"/> ${inv.getCode()}
            <c:if test="${inv._getInventoryLatitude() != null}"> ${inv._getInventoryCoordinates()}</c:if>
            </h3>
            <form class="poster" data-path="occurrences/api/deleteoccurrences" data-confirm="true" data-callback="?w=main">
                <input type="hidden" name="inventoryId" value="${inv.getID()}"/>
                <input type="submit" class="textbutton" value="Delete inventory" style="float:left"/>
            </form>
            <form class="poster id1holder" data-path="occurrences/api/updateinventory" data-refresh="true">
                <input type="hidden" name="inventoryId" value="${inv.getID()}"/>
                <input type="submit" class="textbutton onlywhenmodified" value="<fmt:message key="inventory.upd"/>"/>
                <table class="verysmalltext occurrencetable">
                    <tr>
                        <th><fmt:message key="inventory.2"/></th><th><fmt:message key="inventory.2b"/></th>
                        <th><fmt:message key="inventory.2a"/></th><th><fmt:message key="inventory.3"/></th>
                        <th><fmt:message key="inventory.4"/></th><th><fmt:message key="inventory.5"/></th>
                    </tr>
                    <tr>
                        <td class="field editable coordinates" data-name="coordinates" data-lat="${inv._getInventoryLatitude()}" data-lng="${inv._getInventoryLongitude()}">${inv._getInventoryCoordinates()}</td>
                        <td class="field editable" data-name="precision">${inv.getPrecision().toString()}</td>
                        <td class="field editable" data-name="code">${inv.getCode()}</td>
                        <td class="field editable" data-name="locality">${inv.getLocality()}</td>
                        <td class="field editable" data-name="date" sorttable_customkey="${inv._getDateYMD()}">${inv._getDate()}</td>
                        <td class="field editable authors" data-name="observers"><t:usernames idarray="${inv.getObservers()}" usermap="${userMap}"/></td>
                    </tr>
                </table>
                <table class="verysmalltext occurrencetable">
                    <thead><tr>
                        <th><fmt:message key="inventory.7"/></th><th><fmt:message key="inventory.8"/></th>
                    </tr></thead>
                    <tbody><tr>
                        <td class="field editable" data-name="habitat">${inv.getHabitat()}</td>
                        <td class="field editable threats" data-name="threats">${inv.getThreats()}</td>
                    </tr></tbody>
                </table>
                <table class="verysmalltext occurrencetable sortable newoccurrencetable">
                    <t:inventorytaxonheader />
                    <tbody>
                        <c:forEach var="tax" items="${inv._getTaxa()}">
                        <t:inventoryrow tax="${tax}" inv="${inv}" />
                        </c:forEach>
                        <t:inventoryrow />
                    </tbody>
                </table>
                <div class="button" id="deleteselectedinv">Delete selected taxa</div>
                <div class="button newtaxon">Add taxon</div>
            </form>
        </div>
        </c:forEach>
    </div>
</c:when>

<c:when test="${param.w == 'uploads'}">
    <div class="button anchorbutton"><a href="?w=main&p=1"><fmt:message key="button.2"/></a></div>
    <div class="button anchorbutton"><a href="?w=occurrenceview&p=1"><fmt:message key="button.4"/></a></div>
    </div>  <!-- top buttons -->
    <h1>Uploads</h1>
    <h2>Upload new table</h2>
    <form action="upload/occurrences" method="post" enctype="multipart/form-data" class="poster bigoption" data-path="upload/occurrences">
        <h3><fmt:message key="upload.1"/></h3>
        <label><input type="checkbox" name="mainobserver" checked="checked"/> <fmt:message key="options.1"/><div class="legend"><fmt:message key="options.1.desc"/></div></label>
        <label><input type="checkbox" name="createUsers"/> <fmt:message key="options.2"/><div class="legend"><fmt:message key="options.2.desc"/></div></label>
        <!--<label><input type="checkbox" name="createTaxa"/> <fmt:message key="options.3"/><div class="legend"><fmt:message key="options.3.desc"/></div></label>-->
        <input type="file" name="occurrenceTable" />
        <input type="submit" class="textbutton" value="Upload"/>
    </form>
    <form action="upload/occurrences" method="post" enctype="multipart/form-data" class="poster bigoption" data-path="upload/occurrences">
        <h3><fmt:message key="upload.2"/></h3>
        <input type="hidden" name="type" value="kml"/>
        <label><input type="checkbox" name="mainobserver" checked="checked"/> <fmt:message key="options.1"/><div class="legend"><fmt:message key="options.1.desc"/></div></label>
        <input type="file" name="occurrenceTable" />
        <input type="submit" class="textbutton" value="Upload"/>
    </form>

    <c:if test="${pendingFiles.size() > 0}">
    <h2>Files being processed</h2>
    <table>
        <thead><tr><th>Date uploaded</th><th>Status</th></tr></thead>
        <tbody>
            <c:forEach var="pending" items="${pendingFiles}">
            <tr><td>${pending.getDateSubmitted()}</td><td>${pending.getState()}</td></tr>
            </c:forEach>
        </tbody>
    </table>
    </c:if>
    <h2>Uploaded tables</h2>
    <c:forEach var="file" items="${filesList}">
        <h3>File uploaded on ${file.getUploadDate()}</h3>
        <c:if test="${file.getParseErrors().size() > 0}">
        <div class="warning">
            <p><fmt:message key="error.4"/></p>
            <fmt:message key="error.4a"/>
            <ul>
            <c:forEach var="errors" items="${file.getParseErrors()}">
                <li>${errors}</li>
            </c:forEach>
            </ul>
        </div>
        </c:if>
        <c:if test="${file.getVerboseErrors().size() > 0}">
        <div class="warning">
            <p><fmt:message key="error.6"/></p>
            <fmt:message key="error.6a"/>
            <ul>
            <c:forEach var="errors" items="${file.getVerboseErrors()}">
                <li>${errors}</li>
            </c:forEach>
            </ul>
        </div>
        </c:if>
        <c:if test="${file.getVerboseWarnings().size() > 0}">
        <div class="warning">
            <p><fmt:message key="error.7"/></p>
            <ul>
            <c:forEach var="errors" items="${file.getVerboseWarnings()}">
                <li>${errors}</li>
            </c:forEach>
            </ul>
        </div>
        </c:if>
        <div class="warning">
            <p><fmt:message key="error.5"/></p>
            <form class="poster inlineblock" data-path="occurrences/api/savetable">
                <input type="hidden" name="file" value="${file.getFileName()}"/>
                <c:if test="${file.getQuestions().size() > 0}">
                    <fmt:message key="error.10a"/>
                    <t:taxonomicquestions questions="${file.getQuestions()}"/>
                </c:if>
                <input type="submit" class="textbutton" value="<fmt:message key="save"/>"/>
            </form>
            <form class="poster inlineblock" data-path="occurrences/api/discardtable" data-refresh="true">
                <input type="hidden" name="file" value="${file.getFileName()}"/>
                <input type="submit" class="textbutton" value="<fmt:message key="discard"/>"/>
            </form>
        </div>
        <table class="occurrencetable sortable">
            <tr><th>GPS code</th><th>Date</th><th>Coord</th><th>Species</th></tr>
            <c:forEach var="inv" items="${file}">
            <tr class="geoelement">
                <td>${inv.getCode()}</td>
                <td sorttable_customkey="${inv._getDateYMD()}">${inv._getDate()}</td>
                <td class="coordinates" data-lat="${inv.getLatitude()}" data-lng="${inv.getLongitude()}">${inv._getCoordinates()}</td>
                <td>${inv._getSampleTaxa(5)}</td>
            </tr>
        </c:forEach>
        </table>
    </c:forEach>
</c:when>

<c:when test="${param.w == 'fixissues'}">
    <div class="button anchorbutton"><a href="?w=uploads"><fmt:message key="button.1"/></a></div>
    <div class="button anchorbutton"><a href="?w=main&p=1"><fmt:message key="button.2"/></a></div>
    <div class="button anchorbutton"><a href="?w=occurrenceview&p=1"><fmt:message key="button.4"/></a></div>
    </div>  <!-- top buttons -->
    <h1><fmt:message key="occurrences.3"/></h1>
    <c:if test="${nomatchquestions.size() == 0 && matchwarnings.size() == 0 && nomatches.size() == 0 && parseerrors.size() == 0}">
    <p><fmt:message key="occurrences.4"/></p>
    </c:if>
    <c:if test="${nomatchquestions.size() > 0}">
    <div class="warning">
        <p><fmt:message key="error.10"/></p>
        <fmt:message key="error.10a"/>
        <form class="poster" data-path="occurrences/api/fixtaxonomicissues" data-refresh="true">
        <t:taxonomicquestions questions="${nomatchquestions}" individualforms="false"/>
        <input type="submit" class="textbutton" value="<fmt:message key="occurrences.2"/>"/>
        </form>
    </div>
    </c:if>
    <c:if test="${matchwarnings.size() > 0}">
    <div class="warning">
        <p><fmt:message key="error.7"/></p>
        <ul><c:forEach var="errors" items="${matchwarnings}"><li>${errors}</li></c:forEach></ul>
    </div>
    </c:if>
    <c:if test="${nomatches.size() > 0 || parseerrors.size() > 0}">
    <div class="warning">
        <p><fmt:message key="error.4"/></p>
        <fmt:message key="error.4b"/>
        <ul><c:forEach var="errors" items="${nomatches}"><li>${errors}</li></c:forEach></ul>
        <ul><c:forEach var="errors" items="${parseerrors}"><li>${errors}</li></c:forEach></ul>
    </div>
    </c:if>
</c:when>

<c:when test="${param.w == 'occurrenceview'}">
    <div class="button anchorbutton"><a href="?w=uploads"><fmt:message key="button.1"/></a></div>
    <div class="button anchorbutton"><a href="?w=main&p=1"><fmt:message key="button.2"/></a></div>
    <div class="button anchorbutton"><a href="?w=downloadoccurrencetable"><fmt:message key="button.9"/></a></div>
    <c:if test="${user.canMODIFY_OCCURRENCES()}"><t:optionbutton optionname="allusers" title="All users occurrences" defaultvalue="false"/></c:if>
    <div>
        <fmt:message key="button.4a"/>
        <div class="button anchorbutton ${(param.flavour == null || param.flavour == '' || param.flavour == 'simple') ? 'selected' : ''}"><a href="?w=occurrenceview&flavour=simple"><fmt:message key="button.5"/></a></div>
        <div class="button anchorbutton ${param.flavour == 'redlist' ? 'selected' : ''}"><a href="?w=occurrenceview&flavour=redlist"><fmt:message key="button.6"/></a></div>
        <div class="button anchorbutton ${param.flavour == 'herbarium' ? 'selected' : ''}"><a href="?w=occurrenceview&flavour=herbarium"><fmt:message key="button.7"/></a></div>
        <div class="button anchorbutton ${param.flavour == 'management' ? 'selected' : ''}"><a href="?w=occurrenceview&flavour=management"><fmt:message key="button.10"/></a></div>
    </div>
    </div>  <!-- top buttons -->
    <div id="deleteoccurrences" class="hidden">
        <form class="poster" data-path="occurrences/api/deleteoccurrences" data-refresh="true" data-confirm="true">
            <div class="heading2">
                <h2><fmt:message key="occurrences.5" /></h2>
                <input type="submit" class="textbutton" value="Delete"/>
            </div>
            <table id="deleteoccurrencetable" class="verysmalltext sortable">
                <t:occurrenceheader flavour="${param.flavour}"/>
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
            </div>
            <table id="updateoccurrencetable" class="verysmalltext sortable occurrencetable">
                <t:occurrenceheader flavour="${param.flavour}"/>
                <tbody></tbody>
            </table>
        </form>
    </div>

    <form id="addnewoccurrences" class="poster hidden" data-path="occurrences/api/addoccurrences" data-refresh="true">
        <div class="heading2">
            <h2><fmt:message key="inventory.add1"/></h2>
            <c:if test="${param.flavour != 'herbarium'}">
            <label><input type="checkbox" name="mainobserver" checked="checked"/> <fmt:message key="options.1"/><div class="legend"><fmt:message key="options.1.desc"/></div></label>
            </c:if>
            <label><input type="checkbox" name="createUsers"/> <fmt:message key="options.2"/><div class="legend"><fmt:message key="options.2.desc"/></div></label>
            <!--<label><input type="checkbox" name="createTaxa"/> <fmt:message key="options.3"/><div class="legend"><fmt:message key="options.3.desc"/></div></label>-->
            <div class="button" id="deleteselectednew">Delete selected</div>
            <input type="submit" class="textbutton" value="Save"/>
        </div>
        <table id="addoccurrencetable" class="verysmalltext occurrencetable sortable">
            <t:occurrenceheader flavour="${param.flavour}"/>
            <tbody>
                <t:occurrencerow flavour="${param.flavour}"/>
            </tbody>
        </table>
    </form>

    <form id="mergeoccurrences" data-path="occurrences/api/mergeoccurrences" method="post" enctype="multipart/form-data" class="hidden poster">
        <div class="heading2">
            <h2>Confirm merge occurrences in the same inventory</h2>
            <input type="submit" class="textbutton" value="Merge"/>
        </div>
        <table id="mergeoccurrencetable" class="verysmalltext">
            <t:occurrenceheader flavour="${param.flavour}"/>
            <tbody></tbody>
        </table>
    </form>

    <c:if test="${nproblems > 0}">
    <div class="warning">
        <p><fmt:message key="error.7"/></p>
        <fmt:message key="error.11"/><br/>
        <div class="button anchorbutton"><a href="?w=fixissues"><fmt:message key="button.3"/></a></div>
    </div>
    </c:if>

    <div id="alloccurrences">
        <div class="heading2">
            <h2 class="hideincompactview"><fmt:message key="${sessionScope['option-allusers'] ? 'occurrences.6' : 'occurrences.1'}"/> - ${nrtotaloccurrences}
            <c:if test="${filter != null && filter != ''}"> [filtered ${filter}]</c:if>
            </h2>
            <div class="button" id="newoccurrence"><fmt:message key="occurrences.1a"/></div>
            <div class="button" id="deleteselected"><fmt:message key="occurrences.1b"/></div>
            <div class="button hideincompactview" id="mergeocc"><fmt:message key="occurrences.1c"/></div>
            <div class="button" id="updatemodified"><fmt:message key="inventory.upd"/></div>
            <c:if test="${param.flavour == 'redlist'}">
            <t:optionbutton optionname="compactview" title="Compact" defaultvalue="false" />
            </c:if>
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
        <table id="alloccurrencetable" class="verysmalltext occurrencetable sortable">
            <t:occurrenceheader flavour="${param.flavour}"/>
            <tbody>
            <c:forEach var="occ" items="${occurrences}">
                <t:occurrencerow flavour="${param.flavour}" occ="${occ}" userMap="${userMap}"/>
            </c:forEach>
            <c:forEach var="occ" items="${externaloccurrences}">
                <tr class="geoelement hidden">
                    <td class="coordinates" data-lat="${occ.getLatitude()}" data-lng="${occ.getLongitude()}"
                        data-symbol="${occ.getOccurrence().getConfidence().toString() == 'DOUBTFUL' ? 1 : (occ.getOccurrence().getPresenceStatus() == null || occ.getOccurrence().getPresenceStatus().toString() == 'ASSUMED_PRESENT' ? 2 : 1)}"></td>
                </tr>
            </c:forEach>
            </tbody>
        </table>
    </div>
</c:when>
</c:choose>
