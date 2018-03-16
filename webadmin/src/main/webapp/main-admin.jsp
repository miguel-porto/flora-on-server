<%@ page pageEncoding="UTF-8" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ page session="false" %>

<c:set var="language" value="${not empty param.language ? param.language : not empty language ? language : pageContext.response.locale}" scope="request" />
<fmt:setLocale value="${language}" />
<fmt:setBundle basename="pt.floraon.occurrences.occurrencesMessages" />

<!DOCTYPE html>
<html>
<head>
	<title>Flora-On Admin</title>
	<link href='//fonts.googleapis.com/css?family=Lato:300' rel='stylesheet' type='text/css'>
	<link rel="stylesheet" type="text/css" href="base.css?nocache=${uuid}"/>
	<link rel="stylesheet" type="text/css" href="redlist.css?nocache=${uuid}"/>
	<script type="text/javascript" src="sorttable.js"></script>
	<script type="text/javascript" src="ajaxforms.js"></script>
	<script type="text/javascript" src="basefunctions.js?nocache=${uuid}"></script>
	<script type="text/javascript" src="suggestions.js?nocache=${uuid}"></script>
	<script type="text/javascript" src="js/admin.js?nocache=${uuid}"></script>
</head>
<body>
    <c:if test="${user.isAdministrator()}">
    <c:if test="${matchwarnings.size() > 0}">
    <div class="warning">
        <p><fmt:message key="error.7"/></p>
        <ul>
        <c:forEach var="errors" items="${matchwarnings}">
            <li>${errors}</li>
        </c:forEach>
        </ul>
    </div>
    </c:if>
    <h1>Problems</h1>
    <c:if test="${nomatchquestions.size() > 0}">
    <h2><fmt:message key="error.10"/></h2>
    <p><fmt:message key="error.10a"/></p>
    <!--<form class="poster" data-path="occurrences/api/fixtaxonomicissues" data-refresh="true">-->
    <form method="POST" action="occurrences/api/fixtaxonomicissues">
    <t:taxonomicquestions questions="${nomatchquestions}" individualforms="false"/>
    <input type="submit" class="textbutton" value="<fmt:message key="occurrences.2"/>"/>
    </form>
    </c:if>

    <h1>Toponomy</h1>
    <form action="upload/toponyms" method="post" enctype="multipart/form-data" class="poster" data-path="upload/toponyms">
        <input type="hidden" name="type" value="kml"/>
        <input type="file" name="toponymTable" />
        <input type="submit" class="textbutton" value="Upload"/>
    </form>
    </c:if>

    <c:if test="${!user.isAdministrator() && !user.isGuest()}">
    <h1>Personal area</h1>
    <form class="poster" data-path="admin/updateuser" data-refresh="true">
    <input type="hidden" name="databaseId" value="${user.getID()}"/>
    <table class="small">
        <tr><th colspan="2">Dados da conta</th></tr>
        <tr><td class="title">Name</td><td><input type="text" name="name" value="${user.getName()}" /></td></tr>
        <tr><td class="title">Username <span class="info">Este é o nome que usa para fazer login. Não pode conter espaços.</span></td><td><input type="text" name="userName" value="${user.getUserName()}" /></td></tr>
        <tr><td colspan="2"><input type="submit" class="textbutton" value="Gravar alterações" /></td></tr>
    </table>
    </form>
    <c:if test="${showDownload}">
    <h2>Tools</h2>
    <div class="button anchorbutton"><a href="admin/downloadallkml" target="_blank">Download KML with all taxa</a></div>
    </c:if>
    </c:if>

</body>
</html>
