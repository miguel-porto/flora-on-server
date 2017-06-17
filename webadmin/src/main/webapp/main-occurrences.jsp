<%@ page pageEncoding="UTF-8" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page session="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="language" value="${not empty param.language ? param.language : not empty language ? language : pageContext.response.locale}" scope="request" />
<fmt:setLocale value="${language}" />
<fmt:setBundle basename="pt.floraon.occurrences.occurrencesMessages" />
<!DOCTYPE html>
<html class="occurrencespage">
<head>
	<title><fmt:message key="page.title"/></title>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
	<link href='http://fonts.googleapis.com/css?family=Lato:300' rel='stylesheet' type='text/css'>
	<link rel="stylesheet" type="text/css" href="/floraon/base.css?nocache=${uuid}"/>
	<link rel="stylesheet" type="text/css" href="/floraon/redlist.css?nocache=${uuid}"/>
	<link rel="stylesheet" type="text/css" href="/floraon/occurrences.css?nocache=${uuid}"/>
	<link rel="stylesheet" type="text/css" href="/floraon/js/leaflet-areaselect.css"/>
	<!--
	<link rel="stylesheet" href="https://unpkg.com/leaflet@1.0.2/dist/leaflet.css" />
	<script src="https://unpkg.com/leaflet@1.0.2/dist/leaflet.js"></script>
	-->
	<link rel="stylesheet" type="text/css" href="/floraon/js/leaflet.css"/>
	<script src="/floraon/js/leaflet.js"></script>
	<script src="js/leaflet-providers.js"></script>
	<script src="js/leaflet-bing-layer.min.js"></script>
	<script src="js/leaflet-areaselect.js"></script>
	<script type="text/javascript" src="/floraon/sorttable.js"></script>
	<script type="text/javascript" src="/floraon/ajaxforms.js"></script>
	<script type="text/javascript" src="/floraon/basefunctions.js?nocache=${uuid}"></script>
	<script type="text/javascript" src="/floraon/suggestions.js?nocache=${uuid}"></script>
	<script type="text/javascript" src="/floraon/js/occurrences.js?nocache=${uuid}"></script>
</head>
<body class="occurrencespage">
    <div id="occurrencetable-holder">
        <jsp:include page="occurrences-pages.jsp"></jsp:include>
    </div>
    <div id="georreferencer" class="hidden">
        <div class="head">
            <h1>Georreferencer</h1>
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
            <li>Seleccionar as ocorrências cujas coordenadas quer alterar/definir, clicando nos botões da 1ª coluna da tabela</li>
            <li>Clicar em Set coordinates - as ocorrências seleccionadas serão alteradas para o local do topónimo</li>
            <li>Se necessário, ajustar a localização das ocorrências mais finamente, arrastando-as directamente no mapa</li>
            <li>Após concluído, clicar em Gravar alterações e confirmar as alterações no botão Update</li>
            </ol>
        </div>
    </div>
    <div id="occurrencemap" class="${nroccurrences > 1000 ? 'hidden' : ''}">
        <div id="mapcontainer"></div>
    </div>
    <div id="floatingswitches">
        <div class="${nroccurrences > 1000 ? 'button' : 'button selected'}" id="hidemap">M</div>
        <div class="button" id="hidegeorref">G</div>
        <div class="button selected" id="hideoccurrences">O</div>
    </div>
    <div id="loader">
        <div id="loadermsg">Um momento...</div>
    </div>
</body>
</html>
