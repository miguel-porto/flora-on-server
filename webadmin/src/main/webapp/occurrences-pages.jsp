<%@ page pageEncoding="UTF-8" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page session="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<c:set var="language" value="${not empty param.language ? param.language : not empty language ? language : pageContext.response.locale}" scope="request" />
<fmt:setLocale value="${language}" />
<fmt:setBundle basename="pt.floraon.occurrences.occurrencesMessages" />

<div id="taxonsearchwrapper-holder" class="hidden">
    <div class="withsuggestions editbox" id="taxonsearchwrapper">
        <!--<input id="taxonsearchbox" type="text" name="query" placeholder="taxon" autocomplete="off"/>-->
        <textarea id="taxonsearchbox" name="query" rows="4"></textarea>
        <div id="suggestionstaxon"></div>
    </div>
    <div class="withsuggestions editbox" id="authorsearchwrapper">
        <textarea id="authorsearchbox" name="query" rows="2"></textarea>
        <div id="suggestionsauthor"></div>
    </div>
    <div class="withsuggestions editbox" id="editfieldwrapper">
        <input id="editfield" type="text" name="query" autocomplete="off"/>
    </div>
</div>
<h1>${user.getFullName()}</h1>
<!--<div class="button" id="selectpoints">Select</div>-->
<c:choose>
<c:when test="${param.w == null || param.w == 'main'}">
    <div class="button anchorbutton"><a href="?w=uploads">Upload tables</a></div>
    <div class="button anchorbutton"><a href="?w=occurrenceview">View as occurrences</a></div>
    <!-- This is the model for the inventory. It will be cloned when adding new. -->
    <div class="inventory dummy id1holder">
        <input type="hidden" name="inventoryId" value=""/>
        <h3><fmt:message key="inventory.1"/> <input type="text" name="code" placeholder="<fmt:message key="inventory.6"/>"/></h3>
        <table class="verysmalltext occurrencetable">
            <tr>
                <th><fmt:message key="inventory.2"/></th><th><fmt:message key="inventory.3"/></th>
                <th><fmt:message key="inventory.4"/></th><th><fmt:message key="inventory.5"/></th>
            </tr>
            <tr>
                <td class="field coordinates" data-name="coordinates"></td>
                <td class="field editable" data-name="locality"></td>
                <td class="field editable" data-name="date"></td>
                <td class="field editable authors" data-name="observers">${user.getFullName()}</td>
            </tr>
        </table>
        <table class="verysmalltext occurrencetable">
            <thead><tr>
                <th><fmt:message key="inventory.7"/></th><th><fmt:message key="inventory.8"/></th>
            </tr></thead>
            <tbody><tr>
                <td class="field editable" data-name="habitat"></td>
                <td class="field editable" data-name="threats"></td>
            </tr></tbody>
        </table>
        <table class="verysmalltext occurrencetable sortable newoccurrencetable">
            <thead>
                <tr>
                    <th class="sorttable_nosort selectcol"></th>
                    <th class="smallcol">Coord</th>
                    <th class="bigcol">Taxon</th>
                    <th class="smallcol">Abundance</th>
                    <th class="smallcol">Type of estimate</th>
                    <th class="bigcol">Comment</th>
                    <th class="smallcol">Has specimen</th>
                    <th class="smallcol">Has photo</th>
                </tr>
            </thead>
            <tbody>
                <tr class="dummy id2holder geoelement">
                    <td class="select clickable"><input type="hidden" name="occurrenceUuid" value=""/><div class="selectbutton"></div></td>
                    <td class="coordinates" data-name="coordinates"></td>
                    <td class="taxon editable" data-name="taxa"></td>
                    <td class="editable" data-name="abundance"></td>
                    <td class="editable" data-name="typeOfEstimate"></td>
                    <td class="editable" data-name="comment"></td>
                    <td class="editable" data-name="hasSpecimen"></td>
                    <td class="editable" data-name="hasPhoto"></td>
                </tr>
            </tbody>
        </table>
        <div class="button newtaxon hidden">Add taxon</div>
    </div>

    <form id="addnewinventories" class="poster hidden" data-path="/floraon/occurrences/api/addoccurrences" data-refresh="true">
        <div class="heading2">
            <h2><fmt:message key="inventory.add"/></h2>
            <div class="button" id="deleteselectednew">Delete selected</div>
            <input type="submit" class="textbutton" value="Save"/>
        </div>
    </form>

    <div id="allinventories">
        <h2>Your inventories</h2>
        <c:forEach var="inv" items="${inventories}">
        <c:if test="${inv.getLatitude() == null}"><div class="inventory"></c:if>
        <c:if test="${inv.getLatitude() != null}"><div class="inventory geoelement"></c:if>
            <h3><fmt:message key="inventory.1"/> ${inv.getCode()}
            <c:if test="${inv.getLatitude() != null}"> ${inv._getCoordinates()}</c:if>
            </h3>
            <form class="poster" data-path="/floraon/occurrences/api/deleteoccurrences" data-refresh="true" data-confirm="true">
                <input type="hidden" name="inventoryId" value="${inv.getID()}"/>
                <input type="submit" class="textbutton" value="Delete inventory" style="float:left"/>
            </form>
            <form class="poster id1holder" data-path="/floraon/occurrences/api/updateinventory" data-refresh="true">
                <input type="hidden" name="inventoryId" value="${inv.getID()}"/>
                <input type="submit" class="textbutton onlywhenmodified" value="Update inventory"/>
                <table class="verysmalltext occurrencetable">
                    <tr>
                        <th><fmt:message key="inventory.2"/></th><th><fmt:message key="inventory.2a"/></th><th><fmt:message key="inventory.3"/></th>
                        <th><fmt:message key="inventory.4"/></th><th><fmt:message key="inventory.5"/></th>
                    </tr>
                    <tr>
                        <td class="field editable coordinates" data-name="coordinates" data-lat="${inv.getLatitude()}" data-lng="${inv.getLongitude()}">${inv._getCoordinates()}</td>
                        <td class="field editable" data-name="code">${inv.getCode()}</td>
                        <td class="field editable" data-name="locality">${inv.getLocality()}</td>
                        <td class="field editable" data-name="date">${inv._getDate()}</td>
                        <td class="field editable authors" data-name="observers"><t:usernames idarray="${inv.getObservers()}" usermap="${userMap}"/></td>
                    </tr>
                </table>
                <table class="verysmalltext occurrencetable">
                    <thead><tr>
                        <th><fmt:message key="inventory.7"/></th><th><fmt:message key="inventory.8"/></th>
                    </tr></thead>
                    <tbody><tr>
                        <td class="field editable" data-name="habitat">${inv.getHabitat()}</td>
                        <td class="field editable" data-name="threats">${inv.getThreats()}</td>
                    </tr></tbody>
                </table>
                <table class="verysmalltext occurrencetable sortable">
                    <thead>
                        <tr>
                            <th class="sorttable_nosort selectcol"></th>
                            <th class="smallcol">Code</th>
                            <th class="smallcol">Coord</th>
                            <th class="bigcol">Taxon</th>
                            <th class="smallcol">Abund</th>
                            <th class="smallcol">Estim</th>
                            <th class="bigcol">Comment</th>
                            <th class="smallcol">Specimen</th>
                            <th class="smallcol">Photo</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="tax" items="${inv._getTaxa()}">
                        <c:if test="${tax.getObservationLatitude() == null}"><tr class="id2holder"></c:if>
                        <c:if test="${tax.getObservationLatitude() != null}"><tr class="id2holder geoelement"></c:if>
                            <td class="select clickable">
                                <div class="selectbutton"></div>
                                <input type="hidden" name="occurrenceUuid" value="${tax.getUuid()}"/>
                            </td>
                            <td class="editable" data-name="gpsCode">${tax.getGpsCode()}</td>
                            <c:if test="${tax.getObservationLatitude() != null && tax.getObservationLongitude() != null}">
                            <td class="coordinates" data-lat="${tax.getObservationLatitude()}" data-lng="${tax.getObservationLongitude()}">${tax._getObservationCoordinates()}</td>
                            </c:if>
                            <c:if test="${tax.getObservationLatitude() == null || tax.getObservationLongitude() == null}">
                            <td>*</td>
                            </c:if>
                            <c:if test="${tax.getTaxEnt() == null}">
                            <td class="taxon editable" data-name="taxa">${tax.getVerbTaxon()}</td>
                            </c:if>
                            <c:if test="${tax.getTaxEnt() != null}">
                            <td class="taxon editable" data-name="taxa">${tax.getTaxEnt().getName()}</td>
                            </c:if>
                            <td class="editable" data-name="abundance">${tax.getAbundance()}</td>
                            <td class="editable" data-name="typeOfEstimate">${tax.getTypeOfEstimate()}</td>
                            <td class="editable" data-name="comment">${tax.getComment()}</td>
                            <td class="editable" data-name="hasSpecimen"><t:yesno test="${tax.getHasSpecimen()}"/></td>
                            <td class="editable" data-name="hasPhoto"><t:yesno test="${tax.getHasPhoto()}"/></td>
                        </tr>
                        </c:forEach>
                    </tbody>
                </table>
                <div class="button newtaxon hidden">Add taxon</div>
            </form>
        </div>
        </c:forEach>
    </div>
</c:when>

<c:when test="${param.w == 'uploads'}">
    <div class="button anchorbutton"><a href="?w=main">View as inventories</a></div>
    <div class="button anchorbutton"><a href="?w=occurrenceview">View as occurrences</a></div>
    <h1>Uploads</h1>
    <h2>Upload new table</h2>
    <h3>From a tab-delimited text file</h3>
    <form action="upload/occurrences" method="post" enctype="multipart/form-data" class="poster" data-path="upload/occurrences">
        <input type="file" name="occurrenceTable" />
        <input type="submit" class="textbutton" value="Upload"/>
    </form>
    <h3>From KML</h3>
    <form action="upload/occurrences" method="post" enctype="multipart/form-data" class="poster" data-path="upload/occurrences">
        <input type="hidden" name="type" value="kml"/>
        <input type="file" name="occurrenceTable" />
        <input type="submit" class="textbutton" value="Upload"/>
    </form>
    <h2>Uploaded tables</h2>
    <c:forEach var="file" items="${filesList}">
        <h3>File uploaded on ${file.getUploadDate()}</h3>
        <c:if test="${file.getParseErrors().size() > 0}">
        <div class="warning">
            <b><fmt:message key="error.4"/></b><br/><fmt:message key="error.4a"/><br/><fmt:message key="error.4b"/>
            <ul>
            <c:forEach var="errors" items="${file.getParseErrors()}">
                <li>${errors.getVerbTaxon()}</li>
            </c:forEach>
            </ul>
        </div>
        </c:if>
        <div class="warning">
            <b><fmt:message key="error.5"/></b><br/>
            <form class="poster inlineblock" data-path="occurrences/api/savetable">
                <input type="hidden" name="file" value="${file.getFileName()}"/>
                <label><input type="checkbox" name="mainobserver" checked="checked"/> I am the main observer</label>
                <input type="submit" class="textbutton" value="<fmt:message key="save"/>"/>
            </form>
            <form class="poster inlineblock" data-path="occurrences/api/discardtable" data-refresh="true">
                <input type="hidden" name="file" value="${file.getFileName()}"/>
                <input type="submit" class="textbutton" value="<fmt:message key="discard"/>"/>
            </form>
        </div>
        <table class="occurrencetable">
            <tr><th>Date</th><th>Coord</th><th>Species</th></tr>
            <c:forEach var="inv" items="${file}">
            <tr class="geoelement">
                <td>${inv._getDate()}</td>
                <td class="coordinates" data-lat="${inv.getLatitude()}" data-lng="${inv.getLongitude()}">${inv._getCoordinates()}</td>
                <c:if test="${inv.getUnmatchedOccurrences().size() == 1}">
                <td><c:out value="${inv.getUnmatchedOccurrences().get(0).getVerbTaxon()}"/></td>
                </c:if>
                <c:if test="${inv.getUnmatchedOccurrences().size() != 1}">
                <td>${inv.getUnmatchedOccurrences().size()} species</td>
                </c:if>
            </tr>
        </c:forEach>
        </table>
    </c:forEach>
</c:when>

<c:when test="${param.w == 'occurrenceview'}">
    <div class="button anchorbutton"><a href="?w=uploads">Upload tables</a></div>
    <div class="button anchorbutton"><a href="?w=main">View as inventories</a></div>
    <p>Choose your flavour:</p>
    <div class="button anchorbutton ${(param.flavour == null || param.flavour == '' || param.flavour == 'simple') ? 'selected' : ''}"><a href="?w=occurrenceview&flavour=simple">Simple occurrences</a></div>
    <div class="button anchorbutton ${param.flavour == 'redlist' ? 'selected' : ''}"><a href="?w=occurrenceview&flavour=redlist">Red List ccurrences</a></div>
    <div class="button anchorbutton ${param.flavour == 'herbarium' ? 'selected' : ''}"><a href="?w=occurrenceview&flavour=herbarium">Herbarium data</a></div>
    <div id="deleteoccurrences" class="hidden">
        <form class="poster" data-path="/floraon/occurrences/api/deleteoccurrences" data-refresh="true">
            <div class="heading2">
                <h2>Confirm deletion of occurrences</h2>
                <input type="submit" class="textbutton" value="Delete"/>
            </div>
            <table id="deleteoccurrencetable" class="verysmalltext sortable">
                <t:occurrenceheader flavour="${param.flavour}"/>
                <tbody></tbody>
            </table>
        </form>
    </div>

    <div id="updateoccurrences" class="hidden">
        <form class="poster" data-path="/floraon/occurrences/api/updateoccurrences" data-refresh="true">
            <div class="heading2">
                <h2>Confirm updating the following occurrences</h2>
                <label><input type="checkbox" name="createUsers"/> <fmt:message key="options.2"/><div class="legend"><fmt:message key="options.2.desc"/></div></label>
                <input type="submit" class="textbutton" value="Update"/>
            </div>
            <table id="updateoccurrencetable" class="verysmalltext sortable occurrencetable">
                <t:occurrenceheader flavour="${param.flavour}"/>
                <tbody></tbody>
            </table>
        </form>
    </div>

    <form id="addnewoccurrences" class="poster hidden" data-path="/floraon/occurrences/api/addoccurrences" data-refresh="true">
        <div class="heading2">
            <h2><fmt:message key="inventory.add1"/></h2>
            <c:if test="${param.flavour != 'herbarium'}">
            <label><input type="checkbox" name="mainobserver" checked="checked"/> <fmt:message key="options.1"/><div class="legend"><fmt:message key="options.1.desc"/></div></label>
            </c:if>
            <label><input type="checkbox" name="createUsers"/> <fmt:message key="options.2"/><div class="legend"><fmt:message key="options.2.desc"/></div></label>
            <div class="button" id="deleteselectednew">Delete selected</div>
            <input type="submit" class="textbutton" value="Save"/>
        </div>
        <table id="addoccurrencetable" class="verysmalltext occurrencetable sortable">
            <t:occurrenceheader flavour="${param.flavour}"/>
            <tbody>
                <tr class="geoelement dummy id1holder">
                    <td class="select clickable"><div class="selectbutton"></div></td>
                    <c:choose>
                    <c:when test="${param.flavour == null || param.flavour == 'simple'}">
                    <td class="taxon editable" data-name="taxa"></td>
                    <td class="editable coordinates" data-name="coordinates"></td>
                    <td class="editable" data-name="comment"></td>
                    <td class="editable" data-name="date"></td>
                    <td class="editable" data-name="phenoState"></td>
                    <td class="editable authors" data-name="observers"></td>
                    </c:when>

                    <c:when test="${param.flavour == 'redlist'}">
                    <td class="editable" data-name="gpsCode"></td>
                    <td class="taxon editable" data-name="taxa"></td>
                    <td class="coordinates" data-name="coordinates"></td>
                    <td class="editable" data-name="abundance"></td>
                    <td class="editable" data-name="typeOfEstimate"></td>
                    <td class="editable" data-name="comment"></td>
                    <td class="editable" data-name="hasSpecimen"></td>
                    <td class="editable" data-name="hasPhoto"></td>
                    <td class="editable" data-name="date"></td>
                    <td class="editable" data-name="phenoState"></td>
                    <td class="editable authors" data-name="observers"></td>
                    </c:when>

                    <c:when test="${param.flavour == 'herbarium'}">
                    <td class="taxon editable" data-name="taxa"></td>
                    <td class="editable" data-name="verbLocality"></td>
                    <td class="coordinates" data-name="coordinates"></td>
                    <td class="editable" data-name="labelData"></td>
                    <td class="editable" data-name="date"></td>
                    <td class="editable authors" data-name="collectors"></td>
                    <td class="editable authors" data-name="determiners"></td>
                    </c:when>
                    </c:choose>
                </tr>
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

    <div id="alloccurrences">
        <div class="heading2">
            <h2>Your occurrences</h2>
            <div class="button" id="newoccurrence">New</div>
            <div class="button" id="deleteselected">Delete selected</div>
            <div class="button" id="mergeocc">Re-group selected</div>
            <div class="button" id="updatemodified">Update modified</div>
        </div>
        <table id="alloccurrencetable" class="verysmalltext occurrencetable sortable">
            <t:occurrenceheader flavour="${param.flavour}"/>
            <tbody>
            <c:forEach var="occ" items="${occurrences}">
                <c:if test="${occ._getTaxa()[0].getTaxEnt() == null}">
                <tr class="unmatched geoelement id1holder">
                </c:if>
                <c:if test="${occ._getTaxa()[0].getTaxEnt() != null}">
                <tr class="geoelement id1holder">
                </c:if>
                    <td class="select clickable">
                        <input type="hidden" name="occurrenceUuid" value="${occ._getTaxa()[0].getUuid()}"/>
                        <input type="hidden" name="inventoryId" value="${occ.getID()}"/>
                        <div class="selectbutton"></div>
                    </td>
                    <c:choose>
                    <c:when test="${param.flavour == null || param.flavour == 'simple'}">
                    <c:if test="${occ._getTaxa()[0].getTaxEnt() == null}">
                        <td class="taxon editable" data-name="taxa">${occ._getTaxa()[0].getVerbTaxon()}</td>
                    </c:if>
                    <c:if test="${occ._getTaxa()[0].getTaxEnt() != null}">
                        <td class="taxon editable" data-name="taxa">${occ._getTaxa()[0].getTaxEnt().getName()}</td>
                    </c:if>
                    <td class="editable coordinates" data-name="observationCoordinates" data-lat="${occ.getLatitude()}" data-lng="${occ.getLongitude()}">${occ._getCoordinates()}</td>
                    <td class="editable" data-name="comment">${occ._getTaxa()[0].getComment()}</td>
                    <td class="editable" data-name="date">${occ._getDate()}</td>
                    <td class="editable" data-name="phenoState">${occ._getTaxa()[0].getPhenoState()}</td>
                    <td class="editable authors" data-name="observers"><t:usernames idarray="${occ.getObservers()}" usermap="${userMap}"/></td>
                    </c:when>

                    <c:when test="${param.flavour == 'redlist'}">
                    <td class="editable" data-name="gpsCode">${occ.getCode()}</td>
                    <c:if test="${occ._getTaxa()[0].getTaxEnt() == null}">
                        <td class="taxon editable" data-name="taxa">${occ._getTaxa()[0].getVerbTaxon()}</td>
                    </c:if>
                    <c:if test="${occ._getTaxa()[0].getTaxEnt() != null}">
                        <td class="taxon editable" data-name="taxa">${occ._getTaxa()[0].getTaxEnt().getName()}</td>
                    </c:if>
                    <td class="editable coordinates" data-name="observationCoordinates" data-lat="${occ.getLatitude()}" data-lng="${occ.getLongitude()}">${occ._getCoordinates()}</td>
                    <td class="editable" data-name="abundance">${occ._getTaxa()[0].getAbundance()}</td>
                    <td class="editable" data-name="typeOfEstimate">${occ._getTaxa()[0].getTypeOfEstimate()}</td>
                    <td class="editable" data-name="comment">${occ._getTaxa()[0].getComment()}</td>
                    <td class="editable" data-name="hasSpecimen"><t:yesno test="${occ._getTaxa()[0].getHasSpecimen()}"/></td>
                    <td class="editable" data-name="hasPhoto"><t:yesno test="${occ._getTaxa()[0].getHasPhoto()}"/></td>
                    <td class="editable" data-name="date">${occ._getDate()}</td>
                    <td class="editable" data-name="phenoState">${occ._getTaxa()[0].getPhenoState()}</td>
                    <td class="editable authors" data-name="observers"><t:usernames idarray="${occ.getObservers()}" usermap="${userMap}"/></td>
                    </c:when>

                    <c:when test="${param.flavour == 'herbarium'}">
                    <c:if test="${occ._getTaxa()[0].getTaxEnt() == null}">
                        <td class="taxon editable" data-name="taxa">${occ._getTaxa()[0].getVerbTaxon()}</td>
                    </c:if>
                    <c:if test="${occ._getTaxa()[0].getTaxEnt() != null}">
                        <td class="taxon editable" data-name="taxa">${occ._getTaxa()[0].getTaxEnt().getName()}</td>
                    </c:if>
                    <td class="editable" data-name="verbLocality">${occ.getVerbLocality()}</td>
                    <td class="editable coordinates" data-name="observationCoordinates" data-lat="${occ.getLatitude()}" data-lng="${occ.getLongitude()}">${occ._getCoordinates()}</td>
                    <td class="editable" data-name="labelData">${occ._getTaxa()[0].getLabelData()}</td>
                    <td class="editable" data-name="date">${occ._getDate()}</td>
                    <td class="editable authors" data-name="collectors"><t:usernames idarray="${occ.getCollectors()}" usermap="${userMap}"/></td>
                    <td class="editable authors" data-name="determiners"><t:usernames idarray="${occ.getDets()}" usermap="${userMap}"/></td>
                    </c:when>
                    </c:choose>
                </tr>
            </c:forEach>
            </tbody>
        </table>
    </div>
</c:when>
</c:choose>
