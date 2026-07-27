<%@ page pageEncoding="UTF-8" %>
<c:if test="${fn:length(synonyms) > 0}">
    <b><fmt:message key="DataSheet.label.1.3a"/>:</b>
    <c:forEach var="synonym" items="${synonyms}">${synonym.getFullName(true)} </c:forEach>
</c:if>
<c:if test="${fn:length(includedTaxa) > 0}">
    <b><fmt:message key="DataSheet.label.1.3c"/>:</b>
    <c:forEach var="synonym" items="${includedTaxa}">${synonym.getFullName(true)} </c:forEach>
</c:if>
<c:if test="${fn:length(formerlyIncluded) > 0}">
    <b><fmt:message key="DataSheet.label.1.3b"/>:</b>
    <c:forEach var="synonym" items="${formerlyIncluded}">${synonym.getFullName(true)} </c:forEach>
</c:if>
