<%@ tag description="Session option radio button" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ attribute name="optionprefix" required="true" %>
<%@ attribute name="optionnames" required="true" type="java.util.Map<String, String>" %>
<%@ attribute name="defaultvalue" required="false" type="java.lang.String" %>
<%@ attribute name="persistent" required="false" type="java.lang.Boolean" %>
<%@ attribute name="norefresh" required="false" type="java.lang.Boolean" %>
<%@ attribute name="allowdeselect" required="false" type="java.lang.Boolean" %>
<%@ attribute name="style" required="false" %>
<%@ attribute name="classes" required="false" %>

<%-- First check to see if we should use default value --%>
<c:set var="name" value="option-${optionprefix}" />
<c:set var="useDefault" value="${sessionScope[name] == null}" />

<c:forEach var="opt" items="${optionnames.entrySet()}">
<c:set var="optionName" value="${optionprefix}" />
<c:set var="name" value="option-${optionName}" />
<c:set var="val" value="${useDefault ? (opt.getKey() == defaultvalue) : ((sessionScope[name] == null || sessionScope[name] != opt.getKey()) ? false : true)}" />
<c:choose>
<c:when test="${style == 'light'}">
    <div class="${classes} filter option${val ? ' selected' : ''}" data-option="${optionName}" data-value="${opt.getKey()}"
        data-norefresh="${norefresh == null ? 'false' : norefresh}" data-persistent="${persistent == null ? 'false' : persistent}" data-type="radio" data-allow-deselect="${allowdeselect == null ? 'false' : allowdeselect}"><div class="light"></div><div>${opt.getValue()}</div></div>
</c:when>
<c:otherwise>
    <div class="${classes} button option${val ? ' selected' : ''}" data-option="${optionName}" data-value="${opt.getKey()}"
        data-norefresh="${norefresh == null ? 'false' : norefresh}" data-persistent="${persistent == null ? 'false' : persistent}" data-type="radio" data-allow-deselect="${allowdeselect == null ? 'false' : allowdeselect}">${opt.getValue()}</div>
</c:otherwise>
</c:choose>
</c:forEach>

<%--
<c:set var="useDefault" value="${true}" />
<c:forEach var="opt" items="${optionnames}">
<c:set var="name" value="option-${optionprefix}-${opt}" />
<c:set var="useDefault" value="${useDefault && sessionScope[name] == null}" />
</c:forEach>

<c:forEach var="opt" items="${optionnames}">
<c:set var="optionName" value="${optionprefix}-${opt}" />
<c:set var="name" value="option-${optionName}" />
<c:set var="val" value="${useDefault ? (opt == defaultvalue) : ((sessionScope[name] == null || sessionScope[name] == false) ? false : true)}" />
<c:choose>
<c:when test="${style == 'light'}">
    <div class="${classes} filter option${val ? ' selected' : ''}" data-option="${optionName}" data-value="${val ? 'false' : 'true'}"
        data-norefresh="${norefresh == null ? 'false' : norefresh}" data-type="radio"><div class="light"></div><div>${opt}</div></div>
</c:when>
<c:otherwise>
    <div class="${classes} button option${val ? ' selected' : ''}" data-option="${optionName}" data-value="${val ? 'false' : 'true'}"
        data-norefresh="${norefresh == null ? 'false' : norefresh}" data-type="radio">${opt}</div>
</c:otherwise>
</c:choose>
</c:forEach>
--%>