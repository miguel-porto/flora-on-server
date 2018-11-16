<%@ tag description="Field help for red list sheet" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ attribute name="field" required="true" %>
<%@ attribute name="help" required="false" type="java.lang.Boolean" %>
<c:set var="language" value="${not empty param.language ? param.language : not empty language ? language : pageContext.response.locale}" scope="request" />
<fmt:setLocale value="${language}" />
<fmt:setBundle basename="pt.floraon.redlistdata.fieldValues" />
<c:set var="help" value="${(empty help) ? true : help}" />
<td class="title">${field}</td><td><fmt:message key="DataSheet.label.${field}" /><c:if test="${help && user.canVIEW_FULL_SHEET()}"><div class="fieldhelp"><fmt:message key="DataSheet.help.${field}" /></div></c:if></td>
