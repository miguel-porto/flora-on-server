<%@ page pageEncoding="UTF-8" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page session="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="language" value="${not empty param.language ? param.language : not empty language ? language : pageContext.response.locale}" scope="request" />
<fmt:setLocale value="${language}" />
<fmt:setBundle basename="pt.floraon.occurrences.occurrencesMessages" />

<c:choose>
<c:when test="${param.w == 'ba'}">
    <h1>BA</h1>
    <div>AAAAAAAA</div>
</c:when>

<c:when test="${param.w == null}">
    <h1>Main</h1>
    <a href="?w=uploads">Uploads</a>
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
        <h3>File</h3>
        <c:if test="${file.getParseErrors().size() > 0}">
        <div class="warning"><b><fmt:message key="error.4"/></b><br/><fmt:message key="error.4a"/></div>
        <table>
            <tr><th><fmt:message key="error.4b"/></th></tr>
            <c:forEach var="errors" items="${file.getParseErrors()}">
            <tr><td>${errors.getVerbTaxon()}</td></tr>
            </c:forEach>
        </table>
        </c:if>
        <table class="occurrencetable">
            <tr><th>Date</th><th>Coordinates</th><th>Nr. species</th></tr>
            <c:forEach var="inv" items="${file}">
            <tr>
                <td>${inv.getInventoryData()._getDate()}</td>
                <td class="coordinates" data-lat="${inv.getInventoryData().getLatitude()}" data-lng="${inv.getInventoryData().getLongitude()}">${inv.getInventoryData().getLatitude()}, ${inv.getInventoryData().getLongitude()}</td>
                <td>${inv.getObservedIn().size()} species</td>
            </tr>
        </c:forEach>
        </table>
    </c:forEach>
</c:when>
</c:choose>
