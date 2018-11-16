<%@ page pageEncoding="UTF-8" %>
<ul class="hanging">
    <c:forEach var="bib" items="${bibliography}">
    <li>${bib._getBibliographyEntry()}</li>
    </c:forEach>
</ul>
