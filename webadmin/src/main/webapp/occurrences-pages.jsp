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

    <t:inventorymodel flavour="${param.flavour}"/>

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
            <td class="coordinates" data-lat="${inv._getLatitude()}" data-lng="${inv._getLongitude()}">${inv._getInventoryCoordinates()}</td>
            <td class="taxon"><a href="?w=openinventory&id=${inv._getIDURLEncoded()}">${inv._getSampleTaxa(100)}</a></td>
        </tr>
    </c:forEach>
    </table>
</c:when>

<c:when test="${param.w == 'openinventory'}">
    <div class="button anchorbutton"><a href="?w=uploads"><fmt:message key="button.1"/></a></div>
    <div class="button anchorbutton"><a href="?w=main&p=1"><fmt:message key="button.8"/></a></div>
    <div class="button anchorbutton"><a href="?w=occurrenceview&p=1&filter=iid:${param.id}"><fmt:message key="button.11"/></a></div>
    <div>
        <fmt:message key="button.4a"/>
        <div class="button anchorbutton ${(param.flavour == null || param.flavour == '' || param.flavour == 'simple') ? 'selected' : ''}"><a href="?w=openinventory&flavour=simple&id=${param.id}"><fmt:message key="button.5"/></a></div>
        <div class="button anchorbutton ${param.flavour == 'redlist' ? 'selected' : ''}"><a href="?w=openinventory&flavour=redlist&id=${param.id}"><fmt:message key="button.6"/></a></div>
    </div>
    </div>  <!-- top buttons -->
    <div id="deleteoccurrences" class="hidden">
        <form class="poster" data-path="occurrences/api/deleteoccurrences" data-refresh="true" data-confirm="true">
            <div class="heading2">
                <h2><fmt:message key="occurrences.5" /></h2>
                <input type="submit" class="textbutton" value="Delete"/>
            </div>
            <table id="deleteoccurrencetable" class="verysmalltext sortable">
                <t:inventorytaxonheader flavour="${param.flavour}"/>
                <tbody></tbody>
            </table>
        </form>
    </div>

    <t:inventorymodel flavour="${param.flavour}"/>

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
                    <t:inventorytaxonheader flavour="${param.flavour}"/>
                    <tbody>
                        <c:forEach var="tax" items="${inv._getTaxa()}">
                        <t:inventoryrow tax="${tax}" inv="${inv}" flavour="${param.flavour}"/>
                        </c:forEach>
                        <t:inventoryrow flavour="${param.flavour}"/>
                    </tbody>
                </table>
                <div class="button" id="deleteselectedinv">Delete selected taxa</div>
                <div class="button newtaxon">Add taxon</div>
            </form>
        </div>
        </c:forEach>
        <div style="height:200px">&nbsp;</div>
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
    <c:if test="${filesList.size() > 0}">
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
                <td class="coordinates" data-lat="${inv._getLatitude()}" data-lng="${inv._getLongitude()}">${inv._getCoordinates()}</td>
                <td>${inv._getSampleTaxa(5)}</td>
            </tr>
        </c:forEach>
        </table>
    </c:forEach>
    </c:if>
    <h2>How to prepare a table for upload</h2>
    <p>Save a table with a header row, in a tab-separated text file. In the header row, write the name of the fields that you need. Note that <b>no field is compulsory</b>. You can include in the table <em>only the fields you need, in any order</em>.</p>
    <p><b>If you use the field <code>code</code>, all the occurrences with the same code will be forcibly merged in the same inventory, even if they have different coordinates!</b> In this case, ensure that all the inventory-level fields of the records with the same <code>code</code> have the same values (or leave them blank except for the first record), as only the first record will be used. Note that you can still have different coordinates for each occurrence in the same inventory - for this, it is recommended to use <code>observationlatitude</code> and <code>observationlongitude</code> fields along with <code>code</code> for grouping.</p>
    <p>Follows the list of field names that are recognized. Those more commonly used are highlighted.</p>
    <table style="empty-cells: hide; border-spacing:5px;" class="smalltext">
        <tr><th colspan="2"></th><th>Field name</th><th>Scope</th><th>Description</th></tr>
        <tr class="highlight"><td rowspan="7" class="grouper">only one of</td><td rowspan="2" class="grouper">&#8277</td><td><code>latitude</code></td><td>Inventory</td><td>The latitude of the inventory, in decimal degrees (e.g. <code>37.54778</code>) or DMS (e.g. <code>37º26'13.4'' N</code>)</td></tr>
        <tr class="highlight"><td><code>longitude</code></td><td>Inventory</td><td>The longitude of the inventory, in decimal degrees (e.g. <code>37.54778 -8.34667</code>) or DMS (e.g. <code>7º12'56.3'' W</code>)</td></tr>
        <tr><td class="grouper">&#8277</td><td><code>coordinates</code></td><td>Inventory</td><td>The complete coordinates of the inventory in the form latitude, longitude. e.g. <code>37.54778 -8.34667</code></td></tr>
        <tr><td class="grouper">&#8277</td><td><code>wkt_geom</code></td><td>Inventory</td><td>The geographical expression of the inventory, in WKT format. Currently only pointz is supported.</td></tr>
        <tr><td rowspan="2" class="grouper">&#8277</td><td><code>x</code></td><td>Inventory</td><td>The X coordinate (easting), in UTM WGS84. This assumes the UTM zone 29</td></tr>
        <tr><td><code>y</code></td><td>Inventory</td><td>The Y coordinate (northing), in UTM WGS84. This assumes the UTM zone 29</td></tr>
        <tr class="highlight"><td class="grouper">&#8277</td><td><code>mgrs</code></td><td>Inventory</td><td>The UTM square in <a href="https://en.wikipedia.org/wiki/Military_Grid_Reference_System" target="_blank">MGRS format</a>, e.g. <code>29T NG3486</code></td></tr>
        <tr><td></td><td></td><td><code>elevation</code></td><td>Inventory</td><td>The altitude</td></tr>
        <tr class="highlight"><td></td><td></td><td><code>taxa</code></td><td>Inventory</td><td>The list of taxa observed in this inventory. This can be a single taxon or a list of taxa separated by <code>+</code>. e.g. <code>cistus ladanifer+cistus crispus</code>. Taxa with doubtful identification can be suffixed with <code>?</code> and those in flower suffixed by <code>#</code>, e.g. <code>lavandula multifida#+sideritis hirsuta?</code></td></tr>
        <tr class="highlight"><td rowspan="4" class="grouper">only one of</td><td class="grouper">&#8277</td><td><code>date</code></td><td>Inventory</td><td>The date of the inventory. This can be a precise date e.g. <code>12-9-2007</code> or a vague date using question marks e.g. <code>23-5-?</code></td></tr>
        <tr><td rowspan="3" class="grouper">&#8277</td><td><code>year</code> <code>ano</code></td><td>Inventory</td><td>The year.</td></tr>
        <tr><td><code>month</code></td><td>Inventory</td><td>The month.</td></tr>
        <tr><td><code>day</code></td><td>Inventory</td><td>The day.</td></tr>
        <tr class="highlight"><td></td><td></td><td><code>precision</code></td><td>Inventory</td><td>The precision of the coordinates. This can be a single number (e.g. <code>1km</code>), which denotes a radius around the given coordinate, or a square (e.g. <code>500x500m</code>), which denotes the size of the UTM square where the point is located.</td></tr>
        <tr class="highlight"><td></td><td></td><td><code>locality</code></td><td>Inventory</td><td>The description of the locality.</td></tr>
        <tr><td></td><td></td><td><code>municipality</code></td><td>Inventory</td><td>The municipality of the locality.</td></tr>
        <tr><td></td><td></td><td><code>province</code></td><td>Inventory</td><td>The province of the locality.</td></tr>
        <tr><td></td><td></td><td><code>county</code></td><td>Inventory</td><td>The county of the locality.</td></tr>
        <tr><td></td><td></td><td><code>code</code> <code>código</code> <code>inventário</code></td><td>Inventory</td><td>The short code of the inventory. <b>All records with the same value in this field will be merged in the same inventory.</b></td></tr>
        <tr><td></td><td></td><td><code>habitat</code></td><td>Inventory</td><td>The habitat applicable to all taxa in this inventory.</td></tr>
        <tr><td></td><td></td><td><code>threats</code></td><td>Inventory</td><td>The threats applicable to all taxa in this inventory.</td></tr>
        <tr class="highlight"><td></td><td></td><td><code>observers</code></td><td>Inventory</td><td>The observers, as a comma-separated list of person names.</td></tr>
        <tr><td></td><td></td><td><code>collectors</code></td><td>Inventory</td><td>The collectors, as a comma-separated list of person names.</td></tr>
        <tr><td></td><td></td><td><code>determiners</code></td><td>Inventory</td><td>The determiners, as a comma-separated list of person names.</td></tr>
        <tr class="highlight"><td></td><td></td><td><code>gpscode</code></td><td>Occurrence</td><td>The short code of the GPS point of the occurrence.</td></tr>
        <tr class="highlight"><td></td><td></td><td><code>abundance</code></td><td>Occurrence</td><td>The estimation of the number of individuals present of this taxon. Can be a number or an interval (e.g.<code>1200-1600</code>). Not to be confused with <code>coverIndex</code>.</td></tr>
        <tr><td></td><td></td><td><code>coverIndex</code></td><td>Occurrence</td><td>The cover of this taxon, as text. Can be written in any user-defined scale.</td></tr>
        <tr class="highlight"><td></td><td></td><td><code>typeofestimate</code></td><td>Occurrence</td><td>The type of estimation for the number of individuals. One of: <code>e</code> Numerical estimate <code>c</code> Exact count <code>g</code> Rough estimate</td></tr>
        <tr class="highlight"><td></td><td></td><td><code>comment</code></td><td>Occurrence</td><td>Public notes about this occurrence.</td></tr>
        <tr><td></td><td></td><td><code>privatenote</code></td><td>Occurrence</td><td>Private notes about this occurrence.</td></tr>
        <tr class="highlight"><td></td><td></td><td><code>phenostate</code></td><td>Occurrence</td><td>The phenological state of this taxon. One of: <code>f</code> Flower <code>d</code> Dispersion <code>fd</code> Flower+Dispersion <code>v</code> Vegetative <code>r</code> Resting <code>c</code> Immature fruit <code>fc</code> Flower+Immature fruit</td></tr>
        <tr class="highlight"><td></td><td></td><td><code>confidence</code></td><td>Occurrence</td><td>The confidence in the identification by the observers. One of: <code>c</code> Quite certain <code>a</code> Almost sure <code>d</code> Doubtful</td></tr>
        <tr><td></td><td></td><td><code>excludeReason</code></td><td>Occurrence</td><td>If this occurrence is to be excluded from analyses, for what reason. One of: <code>d</code> Destroyed <code>m</code> Probably misidentified <code>w</code> Wrong coordinates <code>e</code> Escaped from cultivation <code>i</code> Introduced <code>o</code> Other reason</td></tr>
        <tr><td></td><td></td><td><code>hasphoto</code></td><td>Occurrence</td><td>Whether and which photos were taken. One of: <code>s</code> Specimen photo <code>a</code> Threat photo <code>sa</code> Specimen+Threat photo</td></tr>
        <tr><td></td><td></td><td><code>hasspecimen</code></td><td>Occurrence</td><td>How many specimens were collected, if any.</td></tr>
        <tr><td rowspan="3" class="grouper">only one of</td><td rowspan="2" class="grouper">&#8277</td><td><code>observationlatitude</code></td><td>Occurrence</td><td>The latitude of this occurrence, which can be different from the Inventory. In decimal degrees or DMS.</td></tr>
        <tr><td><code>observationlongitude</code></td><td>Occurrence</td><td>The longitude of this occurrence, which can be different from the Inventory. In decimal degrees or DMS.</td></tr>
        <tr><td class="grouper">&#8277</td><td><code>observationcoordinates</code></td><td>Occurrence</td><td>The complete coordinates of this occurrence, which can be different from the Inventory.</td></tr>
        <tr><td></td><td></td><td><code>labeldata</code></td><td>Occurrence</td><td>The data written in the label of the herbarium voucher.</td></tr>
        <tr><td></td><td></td><td><code>accession</code> <code>codHerbario</code></td><td>Occurrence</td><td>The accession code of the herbarium voucher.</td></tr>
        <tr><td></td><td></td><td><code>specificthreats</code></td><td>Occurrence</td><td>The threats applicable to this taxon only.</td></tr>
    </table>
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
                    <td class="coordinates" data-lat="${occ._getLatitude()}" data-lng="${occ._getLongitude()}"
                        data-symbol="${occ.getOccurrence().getConfidence().toString() == 'DOUBTFUL' ? 1 : (occ.getOccurrence().getPresenceStatus() == null || occ.getOccurrence().getPresenceStatus().toString() == 'ASSUMED_PRESENT' ? 2 : 1)}"></td>
                </tr>
            </c:forEach>
            </tbody>
        </table>
    </div>
</c:when>
</c:choose>
