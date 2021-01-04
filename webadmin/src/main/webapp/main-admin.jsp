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
            <li><a href="#personal">Personal</a></li>
            <li><a href="#views">Custom views</a></li>
            <c:if test="${showDownload}">
            <li><a href="#downloads">Downloads</a></li>
            </c:if>
            <c:if test="${user.isAdministrator()}">
            <li><a href="#admin">Administrative tasks</a></li>
            <li><a href="?w=users">User management</a></li>
            </c:if>
        </ul>
    </div>
    <div id="main">
        <c:if test="${!user.isGuest()}">
        <h1><a name="personal"></a>Personal area</h1>
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

        <h1><a name="views"></a>Vistas personalizadas</h1>
        <h2>Criar nova vista personalizada</h2>
        <div class="block">
        <form class="poster" data-path="admin/createcustomoccurrenceflavour" data-refresh="true">
            <table class="small nohovereffects">
            <tr><td>Nome da vista</td><td><input type="text" name="flavourname"/></td></tr>
            <tr><td>Mostrar a vista em</td><td class="multiplechooser left">
            <input type="checkbox" name="showinoccurrenceview" id="showinoccurrenceview" checked="checked"/><label for="showinoccurrenceview" class="wordtag togglebutton">Vista de ocorrências</label>
            <input type="checkbox" name="showininventoryview" id="showininventoryview" checked="checked"/><label for="showininventoryview" class="wordtag togglebutton">Vista de inventários</label>
            </td></tr>
            <tr><td>Incluir na vista os campos</td><td>
                <table class="nohovereffects">
                    <tr><th><div class="filter legend"><div class="light inventoryfield"></div><div>Campos do inventário</div></div></th><th><div class="filter legend"><div class="light occurrencefield"></div><div>Campos do taxon (ocorrência)</div></div></th></tr>
                    <tr>
                        <td style="vertical-align:top"><table class="sortable smalltext">
                        <tr><th>Descrição</th><th>Nome do campo</th></tr>
                        <c:forEach var="entry" items="${inventoryfields}">
                        <tr class="${fieldData.isImportantField(entry.key) ? 'highlight' : ''}"><td sorttable_customkey="${entry.value[0]}"><label title="${entry.value[1]}"><input type="checkbox" name="fields" value="${entry.key}"/>${entry.value[0]}<c:if test="${entry.value[2] == 'RO'}"><span class="info" style="color:red"> read only</span></c:if></label></td><td>${entry.key}</td></tr>
                        </c:forEach>
                        </table></td>
                        <td style="vertical-align:top"><table class="sortable smalltext">
                        <tr><th>Descrição</th><th>Nome do campo</th></tr>
                        <c:forEach var="entry" items="${occurrencefields}">
                        <tr class="${fieldData.isImportantField(entry.key) ? 'highlight' : ''}"><td sorttable_customkey="${entry.value[0]}"><label title="${entry.value[1]}"><input type="checkbox" name="fields" value="${entry.key}"/>${entry.value[0]}<c:if test="${entry.value[2] == 'RO'}"><span class="info" style="color:red"> read only</span></c:if></label></td><td>${entry.key}</td></tr>
                        </c:forEach>
                        </table></td>
                    </tr>
                </table>
            </td></tr>
            <tr><td colspan="2"><input type="submit" value="Criar vista" class="textbutton"/></td></tr>
            </table>
        </form>
        </div>
        <h2>Vistas personalizadas existentes</h2>
        <div class="block">
            <table class="small">
            <tr><th>Nome</th><th>Show in occurrence view</th><th>Show in inventory view</th><th>Fields (click arrows to reorder)</th><th></th></tr>
            <c:forEach var="flv" items="${customflavours}">
            <tr>
                <td>${flv.getName()}</td><td><t:yesno test="${flv.showInOccurrenceView()}"/></td><td><t:yesno test="${flv.showInInventoryView()}"/></td>
                <td>
                <c:forEach var="field" items="${flv.getFields()}" varStatus="loop">
                <c:set var="fieldColor" value="${fieldData.isInventoryField(field) ? 'inventoryfield' : 'occurrencefield'}"/>
                <div class="filter legend nopadding ${fieldColor}">
                    <c:if test="${!loop.isFirst()}">
                        <div class="light"><form class="poster" data-path="admin/changefieldorder" data-refresh="true">
                        <input type="hidden" name="flavourname" value="${flv.getName()}"/>
                        <input type="hidden" name="index" value="${loop.index}"/>
                        <input type="hidden" name="action" value="decrease"/>
                        <input type="submit" class="light" value="&lt;"/>
                        </form></div></c:if><div>${field}</div><c:if test="${!loop.isLast()}">
                        <div class="light right"><form class="poster" data-path="admin/changefieldorder" data-refresh="true">
                        <input type="hidden" name="flavourname" value="${flv.getName()}"/>
                        <input type="hidden" name="index" value="${loop.index}"/>
                        <input type="hidden" name="action" value="increase"/>
                        <input type="submit" class="light" value="&gt;"/>
                        </form></div>
                    </c:if>
                </div>
                </c:forEach>
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
        </div>
        <c:if test="${showDownload}">
        <h1><a name="downloads"></a>Downloads</h1>
        <h2>Download occurrence records</h2>
        <div class="block">
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
        </div>
        </c:if>

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
        <h1><a name="admin"></a>Administrative tasks</h1>
        <h2>Occurrence database</h2>
        <div class="block">
        <form action="admin/downloadallrecords" method="get">
            <h3><img class="lock" src="${contextPath}/images/download.png"/> Download internal records</h3>
            <p>This downloads internal records which are of valid species, identified to species level.</p>
            <p>Minimum precision ${contextPath}</p>
            <p><label><input type="radio" name="precision" value="100" checked="1"/>100m</label>
            <label><input type="radio" name="precision" value="1000"/>1000m</label></p>
            <p>Include:</p>
            <p><label><input type="checkbox" name="certain" checked="checked"/>Certain</label>
            <label><input type="checkbox" name="almostsure" checked="checked"/>Almost sure</label>
            <label><input type="checkbox" name="doubtful"/>Doubtful</label></p>
            <input type="submit" class="textbutton" value="Download"/>
        </form>
        <hr/>
        <h3><a href="admin/downloadallrecords?all=1"><img class="lock" src="${contextPath}/images/download.png"/> Download all records</a></h3>
        </div>
        <h2>Online users</h2>
        <div class="block">
            <table class="sortable">
            <tr><th>Name</th><th>User ID</th></tr>
            <c:forEach var="user" items="${logins}"><tr><td>${user.getName()}</td><td>${user.getID()}</td></tr></c:forEach>
            </table>
        </div>
        <h2>Taxonomic issues</h2>
        <div class="block">
            <c:if test="${orphan}">
            <p>Caro administrador, há táxones não ligados ao grafo principal.</p>
            <div class="button anchorbutton"><a href="checklist?w=graph&show=orphan">Ver táxones</a></div>
            </c:if>
            <c:if test="${errors.hasNext()}">
            <p>Os seguintes taxa estão incorrectamente ligados no grafo:</p>
            <ul>
            <c:forEach var="err" items="${errors}">
                <li><a href="checklist?w=graph&depth=1&q=${err._getNameURLEncoded()}">${err.getName()}</a></li>
            </c:forEach>
            </ul>
            </c:if>
        </div>
        <h2>Toponomy</h2>
        <p>Upload a toponomy table for the georreferencer</p>
        <div class="block">
            <form action="upload/toponyms" method="post" enctype="multipart/form-data" class="poster" data-path="upload/toponyms">
                <input type="hidden" name="type" value="kml"/>
                <input type="file" name="toponymTable" />
                <input type="submit" class="textbutton" value="Upload"/>
            </form>
        </div>
        <h2>Global settings</h2>
        <div class="block">
            <h3>Block user logins</h3>
            <p>Blocks login for all users except administrators.</p>
            <form class="poster" data-path="admin/setglobaloptions" data-refresh="true">
                <input type="hidden" name="option" value="lockediting"/>
                <c:if test="${globalSettings.isClosedForAdminTasks()}">
                <input type="hidden" name="value" value="false"/>
                <input type="submit" value="Desbloquear" class="textbutton"/>
                <div class="button inactive"><img src="../images/locked.png" class="lock"/>Bloqueado</div>
                </c:if>
                <c:if test="${!globalSettings.isClosedForAdminTasks()}">
                <input type="hidden" name="value" value="true"/>
                <div class="button inactive"><img src="../images/unlocked.png" class="lock"/>Desbloqueado</div>
                <input type="submit" value="Bloquear" class="textbutton"/>
                </c:if>
            </form>
        </div>
        </c:if>

        </c:if>
    </div>
</div>

</body>
</html>
