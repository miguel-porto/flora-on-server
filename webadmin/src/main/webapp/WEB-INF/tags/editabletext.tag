<%@ tag description="Editable DIV" pageEncoding="UTF-8" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ attribute name="value" required="true" %>
<%@ attribute name="privilege" required="true" %>
<%@ attribute name="name" required="true" %>

<c:if test="${privilege}">
    <div contenteditable="true" class="contenteditable">${value}</div>
    <input type="hidden" name="${name}" value="${fn:escapeXml(value)}"/>
</c:if>
<c:if test="${!privilege}">${value}</c:if>
