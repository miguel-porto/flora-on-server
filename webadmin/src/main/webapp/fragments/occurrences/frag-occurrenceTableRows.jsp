<%@ page pageEncoding="UTF-8" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page session="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<table id="alloccurrencetable" class="verysmalltext occurrencetable sortable">
    <thead><tr><t:occurrenceheader fields="${flavourfields}" view="occurrence"/></tr></thead>
    <tbody>
    <c:forEach var="occ" items="${occurrences}">
        <t:occurrencerow fields="${flavourfields}" occ="${occ}" userMap="${userMap}" view="occurrence"/>
    </c:forEach>
    </tbody>
</table>