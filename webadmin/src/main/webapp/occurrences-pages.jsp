<%@ page pageEncoding="UTF-8" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page session="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="language" value="${not empty param.language ? param.language : not empty language ? language : pageContext.response.locale}" scope="request" />
<fmt:setLocale value="${language}" />
<fmt:setBundle basename="pt.floraon.occurrences.occurrencesMessages" />

<div id="taxonsearchwrapper-holder" class="hidden">
    <div class="withsuggestions" id="taxonsearchwrapper">
        <!--<input id="taxonsearchbox" type="text" name="query" placeholder="taxon" autocomplete="off"/>-->
        <textarea id="taxonsearchbox" name="query" rows="4"></textarea>
        <div id="suggestionstaxon"></div>
    </div>
</div>
<c:choose>
<c:when test="${param.w == null || param.w == 'main'}">
    <h1>${user.getFullName()}</h1>
    <div class="button anchorbutton"><a href="?w=uploads">Upload tables</a></div>
    <div class="button" id="hidemap">Hide map</div>
    <div id="addnewoccurrences" class="hidden">
        <h2>Add new occurrences</h2>
        <form class="poster" data-path="/floraon/occurrences/api/addoccurrences" data-refresh="true">
            <input type="submit" class="textbutton" value="Save"/>
            <table id="newoccurrencetable" class="verysmalltext occurrencetable sortable">
                <thead>
                    <tr><th></th><th>Taxa</th><th>Coordinates</th><th>Date</th></tr>
                </thead>
                <tbody>
                </tbody>
            </table>
        </form>
    </div>
    <div id="deleteoccurrences" class="hidden">
        <h2>Confirm deletion of occurrences</h2>
        <form class="poster" data-path="/floraon/occurrences/api/deleteoccurrences" data-refresh="true">
            <input type="submit" class="textbutton" value="Delete"/>
            <table id="deleteoccurrencetable" class="verysmalltext sortable">
                <thead>
                    <tr><th></th><th>Taxa</th><th>Coordinates</th><th>Date</th></tr>
                </thead>
                <tbody>
                </tbody>
            </table>
        </form>
    </div>
    <div id="alloccurrences">
        <h2>Your occurrences</h2>
        <!--<div class="button anchorbutton"><a href="?w=main">Occurrences</a></div>
        <div class="button anchorbutton"><a href="?w=inventories">Inventories</a></div>-->
        <div class="button" id="deleteselected">Delete selected occurrences</div>
        <table id="occurrencetable" class="verysmalltext occurrencetable sortable">
            <tr><th></th><th>Taxon</th><th>Coordinates</th><th>Date</th></tr>
        <c:forEach var="occ" items="${occurrences}">
            <c:if test="${occ.getTaxa()[0].getTaxEnt() == null}">
            <tr class="unmatched">
            </c:if>
            <c:if test="${occ.getTaxa()[0].getTaxEnt() != null}">
            <tr>
            </c:if>
                <td class="select">
                    <input type="hidden" name="occurrenceUuid" value="${occ.getTaxa()[0].getUuid()}"/>
                    <input type="hidden" name="inventoryId" value="${occ.getID()}"/>
                    <div class="selectbutton"></div>
                </td>
                <c:if test="${occ.getTaxa()[0].getTaxEnt() == null}">
                    <td class="taxon">${occ.getTaxa()[0].getVerbTaxon()}</td>
                </c:if>
                <c:if test="${occ.getTaxa()[0].getTaxEnt() != null}">
                    <td class="taxon">${occ.getTaxa()[0].getTaxEnt().getName()}</td>
                </c:if>
                <td class="coordinates" data-lat="${occ.getLatitude()}" data-lng="${occ.getLongitude()}">${occ.getLatitude()}, ${occ.getLongitude()}</td>
                <td>${occ._getDate()}</td>
            </tr>
        </c:forEach>
        </table>
    </div>
</c:when>

<c:when test="${param.w == 'uploads'}">
    <h1>Uploads</h1>
    <h2>Upload new table</h2>
    <form action="upload/occurrences" method="post" enctype="multipart/form-data" class="poster" data-path="upload/occurrences">
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
            <tr><th>Date</th><th>Coordinates</th><th>Species</th></tr>
            <c:forEach var="inv" items="${file}">
            <tr>
                <td>${inv._getDate()}</td>
                <td class="coordinates" data-lat="${inv.getLatitude()}" data-lng="${inv.getLongitude()}">${inv.getLatitude()}, ${inv.getLongitude()}</td>
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
</c:choose>
