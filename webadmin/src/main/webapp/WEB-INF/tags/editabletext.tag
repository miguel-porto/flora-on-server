<%@ tag description="Editable DIV" pageEncoding="UTF-8" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ attribute name="value" required="true" type="pt.floraon.driver.datatypes.SafeHTMLString" %>
<%@ attribute name="privilege" required="true" %>
<%@ attribute name="name" required="true" %>
<%@ attribute name="maxlen" required="false" %>

<c:if test="${privilege}">
    <div contenteditable="true" class="contenteditable">${value}</div>
    <input type="hidden" name="${name}" value="${fn:escapeXml(value)}"/>
    <c:if test="${maxlen != null && value.getLength() > maxlen}"><span class="warning">Text has ${value.getLength()} characters but the maximum number of characters is ${maxlen}</span></c:if>
</c:if>
<c:if test="${!privilege}">${value}</c:if>
