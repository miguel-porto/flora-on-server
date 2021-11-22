<%@ tag description="Single selection box" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ attribute name="name" required="true" %>
<%@ attribute name="values" required="false" type="java.lang.Object[]" %>
<%@ attribute name="trigger" required="false" type="java.lang.Boolean" %>
<%@ attribute name="selectedValue" required="false" type="java.lang.Object" %>
<select name="${name}" class="${trigger ? 'trigger' : ''}">
    <c:forEach var="tmp" items="${values}">
        <c:if test="${selectedValue.toString().equals(tmp.toString())}">
            <c:if test="${trigger}"><option value="${tmp.toString()}" selected="selected" data-trigger="${tmp.isTrigger() ? 1 : 0}">${tmp.getLabel()}</option></c:if>
            <c:if test="${!trigger}"><option value="${tmp.toString()}" selected="selected">${tmp.getLabel()}</option></c:if>
        </c:if>
        <c:if test="${!selectedValue.toString().equals(tmp.toString())}">
            <c:if test="${trigger}"><option value="${tmp.toString()}" data-trigger="${tmp.isTrigger() ? 1 : 0}">${tmp.getLabel()}</option></c:if>
            <c:if test="${!trigger}"><option value="${tmp.toString()}">${tmp.getLabel()}</option></c:if>
        </c:if>
    </c:forEach>
</select>
