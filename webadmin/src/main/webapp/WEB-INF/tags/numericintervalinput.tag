<%@ tag description="Input box for numeric interval, with error message" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ attribute name="name" required="true" %>
<%@ attribute name="value" required="true" type="pt.floraon.driver.datatypes.IntegerInterval" %>
<%@ attribute name="placeholder" required="false" %>
<c:set var="language" value="${not empty param.language ? param.language : not empty language ? language : pageContext.response.locale}" scope="request" />
<fmt:setLocale value="${language}" />
<fmt:setBundle basename="pt.floraon.redlistdata.fieldValues" />
<input type="text" name="${name}" value="${value}"/>
<c:if test="${value.getError() != null}"><span class="warning">${value.getError()}</span></c:if>
<span class="legend alwaysvisible"><fmt:message key="DataSheet.msg.interval"/></span>
