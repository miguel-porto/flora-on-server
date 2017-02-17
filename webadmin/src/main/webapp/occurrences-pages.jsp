<%@ page pageEncoding="UTF-8" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page session="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:choose>
<c:when test="${param.w == 'ba'}">
    <h1>BA</h1>
    <div>AAAAAAAA</div>
</c:when>

<c:when test="${param.w == null}">
    <h1>Main</h1>
    <a href="?w=uploads">Uploads</a>
    <form action="upload/occurrences" method="post" enctype="multipart/form-data" class="poster" data-path="upload/occurrences">
        <input type="file" name="occurrenceTable" />
        <input type="submit"/>
    </form>
</c:when>

<c:when test="${param.w == 'uploads'}">
    <h1>Uploads</h1>
    <c:forEach var="file" items="${filesList}">
        <h2>File</h1>
        <table>
        <c:forEach var="inv" items="${file}">
            <tr><td>${inv.getInventoryData().getLatitude()}</td><td>${inv.getInventoryData().getLongitude()}</td><td>${inv.getObservedIn().size()} species</td></tr>
        </c:forEach>
        </table>
    </c:forEach>
</c:when>
</c:choose>
