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
    <div class="withsuggestions editbox large" id="taxonsearchwrapper"><textarea id="taxonsearchbox" name="query" rows="7" placeholder="type taxon and Enter, or Esc to cancel | you can use taxon abbreviations like Clad and enter multiple taxa separated by +"></textarea><div id="suggestionstaxon"></div></div>
    <div class="withsuggestions editbox large" id="authorsearchwrapper"><textarea id="authorsearchbox" name="query" rows="2" placeholder="start typing author name, or Esc to cancel | separate multiple authors with +"></textarea><div id="suggestionsauthor"></div></div>
    <div class="withsuggestions editbox large" id="threatsearchwrapper"><textarea id="threatsearchbox" name="query" rows="2" placeholder="type cell value and Enter, or Esc to cancel"></textarea><div id="suggestionsthreat"></div></div>
    <div class="withsuggestions editbox large" id="multilinefieldwrapper"><textarea id="multilinefield" name="query" rows="4" placeholder="type cell value and Enter, or Esc to cancel"></textarea></div>
    <div class="withsuggestions editbox" id="editfieldwrapper"><input id="editfield" type="text" name="query" autocomplete="off" placeholder="type cell value and Enter, or Esc to cancel"/></div>
    <div class="withsuggestions editbox" id="uploadfilewrapper">
        <input id="imageidfield" type="text" name="query"/>
        <form class="posternoattach" data-path="upload/image" enctype="multipart/form-data" data-refresh="false">
        <input id="uploadfile" type="file" name="imageFile" accept=".jpg,.jpeg"/>
        <input type="submit" class="textbutton" value="Upload"/>
        </form>
    </div>
</div>
<div id="topbuttons" class="hideincompactview">
<a class="returntomain" href="./"><img src="images/cap-cor.png" alt="logo"/></a>
<!--<h1> ${user.getFullName()}</h1>-->
<c:choose>
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
    <form action="upload/occurrences" method="post" enctype="multipart/form-data" class="poster bigoption" data-path="upload/occurrences">
    <h3>Sincronizar com iNaturalist</h3>
    <c:choose>
    <c:when test="${!user.canMODIFY_OCCURRENCES() && (user.getiNaturalistUserName() == null || user.getiNaturalistUserName() == '')}">
    <p>You must set your iNaturalist login name before, in your <a href="${contextPath}/adminpage">personal area</a>.</p>
    </c:when>
    <c:when test="${!user.canMODIFY_OCCURRENCES() && !(user.getiNaturalistUserName() == null || user.getiNaturalistUserName() == '')}">
    <p>This feature is yet being tested, don't click the button below.</p>
    <input type="hidden" name="type" value="iNat"/>
    <input type="submit" class="textbutton" value="Sincronizar"/>
    </c:when>
    <c:when test="${user.canMODIFY_OCCURRENCES() && (user.getiNaturalistFilter().getProject_id() == null || user.getiNaturalistFilter().getProject_id() == '')}">
    <p>You must set a project name from which to import records, in your <a href="${contextPath}/adminpage">personal area</a>.</p>
    </c:when>
    <c:otherwise>
    <input type="hidden" name="type" value="iNat"/>
    <input type="submit" class="textbutton" value="Sincronizar"/>
    <p>Active filters:<br/><span class="info">you can change filters in your personal area</span></p>
    <ul>
        <li>Belonging to this project: ${user.getiNaturalistFilter().getProject_id()}</li>
        <li>Of these observer(s): ${user.getiNaturalistFilter().getUser_idAsString(", ")}</li>
        <li>At least with one of these identifiers: ${user.getiNaturalistFilter().getIdent_user_idAsString(", ")}</li>
        <li>Of these taxa only: ${user.getiNaturalistFilter().getTaxon_namesAsString(", ")}</li>
    </ul>
    <p>Number of records that will be fetched: <t:ajaxloadhtml url="${contextPath}/upload/getInatCount" classes="inlineblock"/></p>
    </c:otherwise>
    </c:choose>
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
        <tr><td></td><td></td><td><code>project</code></td><td>Inventory</td><td>The acronym of the project where this inventory was conducted.</td></tr>
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
        <tr><td></td><td></td><td><code>privatenote</code> <code>privateComment</code></td><td>Occurrence</td><td>Private notes about this occurrence.</td></tr>
        <tr class="highlight"><td></td><td></td><td><code>phenostate</code></td><td>Occurrence</td><td>The phenological state of this taxon. One of: <code>f</code> Flower <code>d</code> Dispersion <code>fd</code> Flower+Dispersion <code>v</code> Vegetative <code>r</code> Resting <code>c</code> Immature fruit <code>fc</code> Flower+Immature fruit</td></tr>
        <tr class="highlight"><td></td><td></td><td><code>confidence</code></td><td>Occurrence</td><td>The confidence in the identification by the observers. One of: <code>c</code> Quite certain <code>a</code> Almost sure <code>d</code> Doubtful</td></tr>
        <tr><td></td><td></td><td><code>naturalization</code></td><td>Occurrence</td><td>If this specimen is wild, cultivated or escaped from culture. One of: <code>w</code> Wild <code>e</code> Escaped <code>c</code> Cultivated</td></tr>
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
    <jsp:include page="fragments/occurrences/frag-occurrenceview.jsp"></jsp:include>
</c:when>
<c:when test="${param.w == null || param.w == 'main'}">
    <jsp:include page="fragments/occurrences/frag-inventorysummary.jsp"></jsp:include>
</c:when>
<c:when test="${param.w == 'openinventory'}">
    <jsp:include page="fragments/occurrences/frag-openinventory.jsp"></jsp:include>
</c:when>
</c:choose>
