<%@ page pageEncoding="UTF-8" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page session="true" %>
<%@ page buffer="3000kb" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="language" value="${not empty param.language ? param.language : not empty language ? language : pageContext.request.locale}" scope="request" />
<fmt:setLocale value="${language}" />
<fmt:setBundle basename="pt.floraon.occurrences.occurrencesMessages" />
<!DOCTYPE html>
<html class="occurrencespage">
<head>
	<title><fmt:message key="page.title"/></title>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
	<c:if test="${!offline}"><link href='//fonts.googleapis.com/css?family=Lato:300' rel='stylesheet' type='text/css'></c:if>
	<link rel="stylesheet" type="text/css" href="base.css?nocache=${uuid}"/>
	<link rel="stylesheet" type="text/css" href="redlist.css?nocache=${uuid}"/>
	<link rel="stylesheet" type="text/css" href="occurrences.css?nocache=${uuid}"/>
	<link rel="stylesheet" type="text/css" href="js/leaflet.draw.css" />
    <link rel="stylesheet" href="https://unpkg.com/leaflet@1.7.1/dist/leaflet.css"
        integrity="sha512-xodZBNTC5n17Xt2atTPuE1HxjVMSvLVW9ocqUKLsCC5CXdbqCmblAshOMAS6/keqq/sMZMZ19scR4PsZChSR7A=="
        crossorigin=""/>
    <script src="https://unpkg.com/leaflet@1.7.1/dist/leaflet.js"
        integrity="sha512-XQoYMqMTK8LvdxXYG3nZ448hOEQiglfqkJs1NOQV44cWnUrBc8PkAOcXy20w0vlaXaVUearIOBhiXZ5V3ynxwA=="
        crossorigin=""></script>
<%--    <script src='https://unpkg.com/leaflet.gridlayer.googlemutant@latest/Leaflet.GoogleMutant.js'></script> --%>

	<script src="js/leaflet-providers.js"></script>
	<script src="js/leaflet-bing-layer.min.js"></script>
	<script src="js/Leaflet.SelectAreaFeature.js"></script>
	<script src="js/leaflet.draw.js"></script>
	<script src="js/getcursorxy.js"></script>
	<script type="text/javascript" src="sorttable.js"></script>
	<script type="text/javascript" src="ajaxforms.js"></script>
	<script type="text/javascript" src="js/treenavigator.js"></script>
	<script type="text/javascript" src="basefunctions.js?nocache=${uuid}"></script>
	<script type="text/javascript" src="suggestions.js?nocache=${uuid}"></script>
	<script type="text/javascript" src="js/occurrences.js?nocache=${uuid}"></script>
	<!-- <script type="text/javascript" src="js/ajax_nav.js?nocache=${uuid}"></script> -->
</head>
<body class="occurrencespage ${sessionScope['option-compactview'] ? 'compactview' : ''}">
    <c:set var="maphidden" value="${sessionScope['option-showmap'] == false ? true : (nroccurrences > 1000 ? true : false)}" />
    <div id="occurrencemap" class="${maphidden ? 'hiddenhard' : ''}">
        <div id="mapcontainer"></div>
        <div class="mapbuttons">
            <div class="button togglebutton round" id="queryrect" title="Realizar consulta de dados em rectângulo"><img src="images/queryrect.png"/></div>
            <div class="button togglebutton round" id="selectpoints" title="Seleccionar pontos no mapa"><img src="images/lasso.png"/></div>
            <div class="button togglebutton round red" id="addpointstoggle" title="Adicionar novos inventários no mapa"><img src="images/add.png"/></div>
        </div>
    </div>
    <div id="georreferencer" class="${sessionScope['option-showgeo'] == true ? '' : 'hiddenhard'}">
        <div class="head">
            <h1 class="hideincompactview">Georreferencer</h1>
            <div id="georref-helptoggle" class="button"></div>
            <p><input id="georref-query" type="text" placeholder="search toponym"/></p>
            <div id="georref-search" class="button">Search</div>
            <div id="georref-usecoords" class="button">Set coordinates</div>
            <div id="georref-clear" class="button">Clear</div>
        </div>
        <div id="georref-results" class="occurrencetable"></div>
        <div id="georref-help" class="hidden">
            <h1>Como usar</h1>
            <ol>
            <li>Escrever um topónimo, ou parte de um topónimo, e clicar em Search<br/>* em alternativa, pode seleccionar o texto em qualquer campo da tabela, para pesquisar por esse texto</li>
            <li>Dos resultados da pesquisa (no mapa e na tabela) seleccionar aquele que corresponde ao desejado clicando na linha da tabela ou no respectivo quadrado verde no mapa</li>
            <li>Seleccionar as ocorrências cujas coordenadas quer alterar/definir, clicando nos botões da 1ª coluna da tabela - podem ser seleccionadas tantas quanto necessário</li>
            <li>Clicar em Set coordinates - todas as ocorrências seleccionadas serão alteradas para o local do topónimo</li>
            <li>Se necessário, ajustar a localização das ocorrências mais finamente, arrastando-as directamente no mapa</li>
            <li>Após concluído, clicar em Gravar alterações e confirmar as alterações no botão Update</li>
            </ol>
        </div>
    </div>
    <div id="occurrencetable-holder" class="${sessionScope['option-showocc'] == false ? 'hiddenhard' : ''}">
        <jsp:include page="occurrences-pages.jsp"></jsp:include>
    </div>
    <t:isoptionselected optionname="showtax">
    <div id="taxtree" class="taxtree-holder compact">
        <input type="text" id="taxtree-filter" style="width:100%; box-sizing: border-box" placeholder="filtrar"/>
        <t:option-radiobutton optionprefix="rootrank" optionnames="${taxonomicRanks}" defaultvalue="genus" style="light" classes="small"/>
        <c:url value="checklist/api/lists" var="urltaxtree">
          <c:param name="w" value="tree" />
          <c:param name="type" value="taxent" />
          <c:param name="rank" value="${sessionScope['option-rootrank'] == null ? 'genus' : sessionScope['option-rootrank']}" />
        </c:url>
        <jsp:include page="${urltaxtree}"></jsp:include>
    </div>
    </t:isoptionselected>
    <div id="floatingswitches">
        <t:optionbutton optionname="showmap" title="Map" defaultvalue="${!maphidden}" element="occurrencemap" norefresh="true" />
        <t:optionbutton optionname="showgeo" title="Geo" defaultvalue="false" element="georreferencer" norefresh="true" />
        <t:optionbutton optionname="showocc" title="Occ" defaultvalue="true" element="occurrencetable-holder" norefresh="true" />
        <t:optionbutton optionname="showtax" title="Tax" defaultvalue="false" element="taxtree" norefresh="false" />
    </div>
    <c:if test="${warning != null}">
    <div class="warning floating"><b>${warning}</b></div>
    </c:if>
    <div id="loader">
        <div id="loadermsg">Um momento...</div>
    </div>
</body>
</html>
