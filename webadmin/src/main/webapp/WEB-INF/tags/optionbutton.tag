<%@ tag description="Session option button" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ attribute name="optionname" required="true" %>
<%@ attribute name="title" required="true" %>
<%@ attribute name="defaultvalue" required="false" type="java.lang.Boolean" %>
<%@ attribute name="type" required="false" %>
<%@ attribute name="element" required="false" %>

<c:set var="name" value="option-${optionname}" />
<c:set var="val" value="${sessionScope[name] == null ? defaultvalue : (sessionScope[name] == false ? false : true)}" />

<div class="button option ${val ? 'selected' : ''}" data-option="${optionname}" data-value="${val ? 'false' : 'true'}" data-element="${element == null ? '' : element}">${title}</div>
