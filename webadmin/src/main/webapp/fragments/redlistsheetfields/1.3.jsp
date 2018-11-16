<%@ page pageEncoding="UTF-8" %>
<table class="subtable">
<c:if test="${fn:length(synonyms) > 0}">
    <tr><td><fmt:message key="DataSheet.label.1.3a"/></td><td>
    <ul><c:forEach var="synonym" items="${synonyms}">
        <li data-key="${synonym.getID()}">${synonym.getFullName(true)}</li>
    </c:forEach></ul>
    </td></tr>
</c:if>
<c:if test="${fn:length(includedTaxa) > 0}">
    <tr><td><fmt:message key="DataSheet.label.1.3c"/></td><td>
    <ul><c:forEach var="synonym" items="${includedTaxa}">
        <li data-key="${synonym.getID()}">${synonym.getFullName(true)}</li>
    </c:forEach></ul>
    </td></tr>
</c:if>
<c:if test="${fn:length(formerlyIncluded) > 0}">
<tr><td><fmt:message key="DataSheet.label.1.3b"/></td><td>
    <ul><c:forEach var="synonym" items="${formerlyIncluded}">
        <li data-key="${synonym.getID()}">${synonym.getFullName(true)}</li>
    </c:forEach></ul>
    </td></tr>
</c:if>
</table>
