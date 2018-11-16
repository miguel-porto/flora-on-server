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
<%--
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
--%>
    <h1>Toponomy</h1>
    <form action="upload/toponyms" method="post" enctype="multipart/form-data" class="poster" data-path="upload/toponyms">
        <input type="hidden" name="type" value="kml"/>
        <input type="file" name="toponymTable" />
        <input type="submit" class="textbutton" value="Upload"/>
    </form>
    </c:if>

    <c:if test="${!user.isGuest()}">
    <h1>Personal area</h1>
    <p>${user.getID()}</p>
    <%--
    <form class="poster" data-path="admin/updateuser" data-refresh="true">
    <input type="hidden" name="databaseId" value="${user.getID()}"/>
    <table class="small">
        <tr><th colspan="2">Dados da conta</th></tr>
        <tr><td class="title">Name</td><td><input type="text" name="name" value="${user.getName()}" /></td></tr>
        <tr><td class="title">Username <span class="info">Este é o nome que usa para fazer login. Não pode conter espaços.</span></td><td><input type="text" name="userName" value="${user.getUserName()}" /></td></tr>
        <tr><td colspan="2"><input type="submit" class="textbutton" value="Gravar alterações" /></td></tr>
    </table>
    </form>
    --%>

    <h2>Vistas personalizadas</h2>
    <form class="poster" data-path="admin/createcustomoccurrenceflavour" data-refresh="true">
        <h3>Criar nova vista personalizada</h3>
        <table class="small">
        <tr><td>Nome da vista</td><td><input type="text" name="flavourname"/></td></tr>
        <tr><td>Mostrar a vista em</td><td class="multiplechooser left">
        <input type="checkbox" name="showinoccurrenceview" id="showinoccurrenceview" checked="checked"/><label for="showinoccurrenceview" class="wordtag togglebutton">Vista de ocorrências</label>
        <input type="checkbox" name="showininventoryview" id="showininventoryview"/><label for="showininventoryview" class="wordtag togglebutton">Vista de inventários</label>
        </td></tr>
        <tr><td>Incluir na vista os campos</td><td>
            <div class="filter lilac legend"><div class="light"></div><div>Campos do inventário</div></div><div class="filter beige legend"><div class="light"></div><div>Campos do taxon (ocorrência)</div></div>
            <div class="multiplechooser left sized">
            <c:forEach var="entry" items="${occurrencefields}">
            <input type="checkbox" name="fields" value="${entry.key}" id="field_${entry.key}"/><label for="field_${entry.key}" class="wordtag togglebutton" style="background-color:#8bc34a">${entry.key}<div class="info" style="color:black">${entry.value}</div></label>
            </c:forEach>
            <c:forEach var="entry" items="${inventoryfields}">
            <input type="checkbox" name="fields" value="${entry.key}" id="field_${entry.key}"/><label for="field_${entry.key}" class="wordtag togglebutton" style="background-color:#FFC107">${entry.key}<div class="info" style="color:black">${entry.value}</div></label>
            </c:forEach>
            </div>
        </td></tr>
        <tr><td colspan="2"><input type="submit" value="Criar vista" class="textbutton"/></td></tr>
        </table>
    </form>

    <h3>Vistas personalizadas existentes</h3>
    <table class="small">
    <tr><th>Nome</th><th>Show in occurrence view</th><th>Show in inventory view</th><th>Fields</th><th></th></tr>
    <c:forEach var="flv" items="${customflavours}">
    <tr>
        <td>${flv.getName()}</td><td><t:yesno test="${flv.showInOccurrenceView()}"/></td><td><t:yesno test="${flv.showInInventoryView()}"/></td>
        <td>
        <c:forEach var="field" items="${flv.getFields()}"><div class="wordtag">${field}</div></c:forEach>
        </td>
        <td>
            <form class="poster" data-path="admin/deletecustomoccurrenceflavour" data-refresh="true">
            <input type="hidden" name="flavourname" value="${flv.getName()}"/>
            <input type="submit" value="Apagar" class="textbutton"/>
            </form>
        </td>
    </tr>
    </c:forEach>
    </table>



    <c:if test="${showDownload}">
    <h2>Download occurrence records</h2>
    <table>
    <tr>
        <td>Descarregar todas as ocorrências de todos os taxa que lhe estão atribuídos</td>
        <td><div class="button anchorbutton"><a href="admin/downloadallkml" target="_blank">Download KML</a></div></td>
    </tr>
    <tr>
        <td>Descarregar ocorrências LVF de todos os taxa que lhe estão atribuídos filtrando por autores</td>
        <td>
            <form action="admin/downloadallkml" method="post" enctype="multipart/form-data">
                <c:forEach var="user" items="${allusers}">
                <label><input type="checkbox" value="${user.getID()}" name="filterusers">${user.getName()}</input></label> | </c:forEach>
                <br/><input type="submit" class="textbutton" value="Download KML"/>
            </form>
        </td>
    </tr>
    </table>
    </c:if>
    </c:if>

</body>
</html>
