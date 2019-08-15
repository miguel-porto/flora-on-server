<%@ tag description="Session option button" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ attribute name="optionname" required="true" %>
<%@ attribute name="title" required="true" %>
<%@ attribute name="defaultvalue" required="false" type="java.lang.Boolean" %>
<%@ attribute name="element" required="false" %>
<%@ attribute name="norefresh" required="false" type="java.lang.Boolean" %>
<%@ attribute name="style" required="false" %>
<%@ attribute name="classes" required="false" %>

<c:set var="name" value="option-${optionname}" />
<c:set var="val" value="${sessionScope[name] == null ? defaultvalue : (sessionScope[name] == false ? false : true)}" />
<c:choose>
<c:when test="${style == 'light'}">
    <div class="${classes} filter option${val ? ' selected' : ''}" data-option="${optionname}" data-value="${val ? 'false' : 'true'}"
        data-element="${element == null ? '' : element}" data-norefresh="${norefresh == null ? 'false' : norefresh}" data-type="boolean"><div class="light"></div><div>${title}</div></div>
</c:when>
<c:when test="${style == 'invisible'}">
    <div class="${classes} option${val ? ' selected' : ''}" data-option="${optionname}" data-value="${val ? 'false' : 'true'}"
        data-element="${element == null ? '' : element}" data-norefresh="${norefresh == null ? 'false' : norefresh}" data-type="boolean"></div>
</c:when>
<c:when test="${style == 'content'}">
    <div class="${classes} option${val ? ' selected' : ''}" data-option="${optionname}" data-value="${val ? 'false' : 'true'}"
        data-element="${element == null ? '' : element}" data-norefresh="${norefresh == null ? 'false' : norefresh}" data-type="boolean"><jsp:doBody var="content" />${content}</div>
</c:when>
<c:otherwise>
    <div class="${classes} button option${val ? ' selected' : ''}" data-option="${optionname}" data-value="${val ? 'false' : 'true'}"
        data-element="${element == null ? '' : element}" data-norefresh="${norefresh == null ? 'false' : norefresh}" data-type="boolean">${title}</div>
</c:otherwise>
</c:choose>
