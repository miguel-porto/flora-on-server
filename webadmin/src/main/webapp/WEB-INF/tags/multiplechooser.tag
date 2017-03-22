<%@ tag description="Multiple selection box" pageEncoding="UTF-8" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ attribute name="values" required="false" type="java.util.List" %>
<%@ attribute name="allvalues" required="true" type="java.lang.Object[]" %>
<%@ attribute name="privilege" required="true" %>
<%@ attribute name="name" required="true" %>
<%@ attribute name="idprefix" required="true" %>
<%@ attribute name="layout" required="false" %>
<%@ attribute name="categorized" required="false" type="java.lang.Boolean" %>

<c:set var="language" value="${not empty param.language ? param.language : not empty language ? language : pageContext.response.locale}" scope="request" />
<fmt:setLocale value="${language}" />
<fmt:setBundle basename="pt.floraon.redlistdata.fieldValues" />

<c:if test="${privilege}">
<div class="checkboxes ${layout}" tabindex="0">
    <input type="hidden" name="${name}" value=""/>
    <c:forEach var="tmp" items="${allvalues}">
        <c:if test="${values != null && values.contains(tmp)}">
            <input type="checkbox" name="${name}" value="${tmp.toString()}" checked="checked" id="${idprefix}_${tmp}"/>
        </c:if>
        <c:if test="${values == null || !values.contains(tmp)}">
            <input type="checkbox" name="${name}" value="${tmp.toString()}" id="${idprefix}_${tmp}"/>
        </c:if>
        <c:if test="${categorized}"><label for="${idprefix}_${tmp}" class="${tmp.getCategory()}"> <fmt:message key="${tmp.getLabel()}" /></c:if>
        <c:if test="${!categorized}"><label for="${idprefix}_${tmp}"> <fmt:message key="${tmp.getLabel()}" /></c:if>
        <div class="legend"><fmt:message key="${tmp.getDescription()}" /></div></label>
    </c:forEach>
    <label class="placeholder"><fmt:message key="DataSheet.msg.clickxpand"/></label>
</div>
</c:if>
<c:if test="${!privilege}">
    <ul>
    <c:forEach var="tmp" items="${values}">
        <li><fmt:message key="${tmp.getLabel()}" /></li>
    </c:forEach>
    </ul>
</c:if>
