<%@ page pageEncoding="UTF-8" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ page session="true" %>

<c:set var="language" value="${not empty param.language ? param.language : not empty language ? language : pageContext.response.locale}" scope="request" />
<fmt:setLocale value="${language}" />
<fmt:setBundle basename="pt.floraon.occurrences.occurrencesMessages" />

<!DOCTYPE html>
<html>
<head>
	<title>User management</title>
	<c:if test="${!offline}"><link href='//fonts.googleapis.com/css?family=Lato:300' rel='stylesheet' type='text/css'></c:if>
	<link rel="stylesheet" type="text/css" href="base.css?nocache=${uuid}"/>
	<link rel="stylesheet" type="text/css" href="redlist.css?nocache=${uuid}"/>
	<script type="text/javascript" src="sorttable.js"></script>
	<script type="text/javascript" src="ajaxforms.js"></script>
	<script type="text/javascript" src="basefunctions.js?nocache=${uuid}"></script>
	<script type="text/javascript" src="suggestions.js?nocache=${uuid}"></script>
	<script type="text/javascript" src="js/admin.js?nocache=${uuid}"></script>
</head>
<body>
<div id="main-holder" class="fixed">
    <%--                THE MAIN MENU ON THE LEFT               --%>
    <div id="left-bar" class="buttonmenu">
        <ul>
            <li><a href="./">Home</a></li>
            <li><a href="?w=main">Admin page</a></li>
        </ul>
    </div>
    <div id="main">
    <c:choose>
        <c:when test="${param.w == 'edituser'}">
            <jsp:include page="fragments/frag-edituser.jsp"></jsp:include>
        </c:when>
        <c:otherwise>
            <table class="sortable verysmalltext">
            <tr><th>Name</th><th>ID</th><th>Username</th><th>Has account?</th><th>Non-standard name?</th><th>Nr. of observations where involved</th><th>Actions</th></tr>
            <c:forEach items="${allusers}" var="user">
            <tr>
                <td><pre>${user.getName()}</pre></td>
                <td><pre>${user.getID()}</pre></td>
                <td><pre>${user.getUserName()}</pre></td>
                <td><t:yesno test="${user.getPassword() != null}"/></td>
                <td><t:yesno test="${user.hasNonStandardName()}"/></td>
                <td>${nrOccurrencesMap[user.getID()]}</td>
                <td>
                    <c:url value="" var="urledituser">
                      <c:param name="w" value="edituser" />
                      <c:param name="user" value="${user.getID()}" />
                    </c:url>
                    <div class="button anchorbutton"><a href="${urledituser}">Edit user</a></div>
                    <c:if test="${nrOccurrencesMap[user.getID()] == 0}">
                    <form class="poster" data-path="admin/deleteuser" data-refresh="false">
                        <input type="hidden" name="databaseId" value="${user.getID()}"/>
                        <input type="submit" value="Delete user" class="textbutton"/>
                    </form>
                    </c:if>
                    <c:url value="occurrences" var="urlocc">
                      <c:param name="w" value="occurrenceview" />
                      <c:param name="filter" value="uid:${user.getID()}" />
                    </c:url>
                    <div class="anchorbutton"><a href="${urlocc}" target="_blank">View occurrences</a></div>
                </td>
            </tr>
            </c:forEach>
            </table>
        </c:otherwise>
    </c:choose>
    </div>
</div>

</body>
</html>
