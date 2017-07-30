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
<%@ attribute name="namedDbNode" required="false" type="java.lang.Boolean" %>

<c:set var="language" value="${not empty param.language ? param.language : not empty language ? language : pageContext.response.locale}" scope="request" />
<fmt:setLocale value="${language}" />
<fmt:setBundle basename="pt.floraon.redlistdata.fieldValues" />

<c:if test="${privilege}">
<div class="checkboxes ${layout}" tabindex="0">
    <input type="hidden" name="${name}" value=""/>
    <c:forEach var="tmp" items="${allvalues}">
        <c:if test="${namedDbNode}">
            <c:set var="containsTest" value="${tmp.getID()}" />
            <c:set var="inputvalue" value="${tmp.getID()}" />
            <c:set var="idvalue" value="${idprefix}_${tmp._getNameURLEncoded()}" />
            <c:set var="lab" value="${tmp.getName()}" />
            <c:set var="description" value="${tmp.getDescription()}" />
        </c:if>
        <c:if test="${!namedDbNode}">
            <c:set var="containsTest" value="${tmp}" />
            <c:set var="inputvalue" value="${tmp.toString()}" />
            <c:set var="idvalue" value="${idprefix}_${tmp}" />
            <fmt:message key="${tmp.getLabel()}" var="lab"/>
            <fmt:message key="${tmp.getDescription()}" var="description"/>
        </c:if>
        <c:if test="${values != null && values.contains(containsTest)}">
            <input type="checkbox" name="${name}" value="${inputvalue}" checked="checked" id="${idvalue}"/>
        </c:if>
        <c:if test="${values == null || !values.contains(containsTest)}">
            <input type="checkbox" name="${name}" value="${inputvalue}" id="${idvalue}"/>
        </c:if>
        <c:if test="${categorized}"><label for="${idvalue}" class="${tmp.getCategory()}"> <div class="light"></div><span>${lab}</span></c:if>
        <c:if test="${!categorized}"><label for="${idvalue}"> <div class="light"></div><span>${lab}</span></c:if>
        <div class="legend">${description}</div>
        </label>
    </c:forEach>
    <label class="placeholder"><fmt:message key="DataSheet.msg.clickxpand"/></label>
</div>
</c:if>
<c:if test="${!privilege}">
    <ul>
    <c:forEach var="tmp" items="${values}">
        <c:if test="${namedDbNode}"><c:set var="lab" value="${tmp.getName()}" /></c:if>
        <c:if test="${!namedDbNode}"><fmt:message key="${tmp.getLabel()}" var="lab"/></c:if>
        <li>${lab}</li>
    </c:forEach>
    </ul>
</c:if>
