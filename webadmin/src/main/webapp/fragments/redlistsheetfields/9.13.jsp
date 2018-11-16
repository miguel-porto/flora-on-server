<%@ page pageEncoding="UTF-8" %>
<table class="subtable">
    <tr><th>Date saved</th><th>User</th><th>Number of edits</th></tr>
    <c:forEach var="rev" items="${revisions}">
    <tr><td>${rev.getKey().getFormattedDateSaved()}</td><td>${userMap.get(rev.getKey().getUser())}</td><td>${rev.getValue()}</td></tr>
    </c:forEach>
</table>
