<%@ tag description="Session option button" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ attribute name="optionname" required="true" %>
<%@ attribute name="value" required="false" type="java.lang.Boolean" %>

<jsp:doBody var="message" />
<c:set var="name" value="option-${optionname}" />
<c:set var="val" value="${sessionScope[name] == null ? false : (sessionScope[name] == false ? false : true)}" />
<c:if test="${(value == null) ? val : (val == value)}">${message}</c:if>
