<%@ page pageEncoding="UTF-8" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page session="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<fmt:setBundle basename="pt.floraon.redlistdata.fieldValues" />
<c:if test="${!(user.canMANAGE_REDLIST_USERS() || user.canDOWNLOAD_OCCURRENCES())}">
    <div class="warning"><b>You&#8217;re not authorized to enter this page</b></div>
</c:if>
<c:if test="${user.canMANAGE_REDLIST_USERS() || user.canDOWNLOAD_OCCURRENCES()}">
<h1><fmt:message key="Separator.12"/></h1>
<ul class="mainmenu">
    <c:if test="${user.canCREATE_REDLIST_DATASETS()}">
        <li><div class="bullet"></div><span><a href="api/updatenativestatus?territory=${territory}"><fmt:message key="Separator.4"/> ${territory}</a></span></li>
    </c:if>
    <c:if test="${user.isAdministrator()}">
        <li><div class="bullet"></div><span><a href="?w=batch"><fmt:message key="Separator.5"/></a></span></li>
        <li><div class="bullet"></div><span><a href="?w=replacetools"><fmt:message key="Separator.13"/></a></span></li>
    </c:if>
    <c:if test="${user.canDOWNLOAD_OCCURRENCES()}">
        <li><div class="bullet"></div><span><a href="?w=debug">Diagnósticos</a></span></li>
    </c:if>
    <li><div class="bullet"></div><span><a href="api/downloaddata?territory=${territory}"><fmt:message key="Separator.3"/></a></span></li>
</ul>
</c:if>