<%@ page pageEncoding="UTF-8" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page session="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="language" value="${not empty param.language ? param.language : not empty language ? language : pageContext.response.locale}" scope="request" />
<fmt:setLocale value="${language}" />
<fmt:setBundle basename="pt.floraon.occurrences.occurrencesMessages" />
<!DOCTYPE html>
<html class="occurrencespage">
<head>
	<title><fmt:message key="page.title"/></title>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
	<link href='//fonts.googleapis.com/css?family=Lato:300' rel='stylesheet' type='text/css'>
	<link rel="stylesheet" type="text/css" href="base.css?nocache=${uuid}"/>
	<link rel="stylesheet" type="text/css" href="redlist.css?nocache=${uuid}"/>
	<link rel="stylesheet" type="text/css" href="occurrences.css?nocache=${uuid}"/>
	<link rel="stylesheet" type="text/css" href="js/leaflet-areaselect.css"/>

	<link rel="stylesheet" href="https://unpkg.com/leaflet@1.0.2/dist/leaflet.css" />
	<script src="https://unpkg.com/leaflet@1.0.2/dist/leaflet.js"></script>

	<!--<link rel="stylesheet" type="text/css" href="js/leaflet.css"/>
	<script src="js/leaflet.js"></script>-->
	<script src="js/leaflet-providers.js"></script>
	<script src="js/leaflet-bing-layer.min.js"></script>
	<!--<script src="js/leaflet-areaselect.js"></script>-->
	<script type="text/javascript" src="sorttable.js"></script>
	<script type="text/javascript" src="ajaxforms.js"></script>
	<script type="text/javascript" src="basefunctions.js?nocache=${uuid}"></script>
	<script type="text/javascript" src="suggestions.js?nocache=${uuid}"></script>
	<script type="text/javascript" src="js/occurrences.js?nocache=${uuid}"></script>
</head>
<body class="occurrencespage ${sessionScope['option-compactview'] ? 'compactview' : ''}">
    <div id="occurrencetable-holder" class="${sessionScope['option-showocc'] == false ? 'hiddenhard' : ''}">
        <jsp:include page="occurrences-pages.jsp"></jsp:include>
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
    <c:set var="maphidden" value="${sessionScope['option-showmap'] == false ? true : (nroccurrences > 1000 ? true : false)}" />
    <div id="occurrencemap" class="${maphidden ? 'hiddenhard' : ''}">
        <div id="mapcontainer"></div>
        <div class="button togglebutton" id="addpointstoggle">Adicionar ocorrências</div>
    </div>
    <div id="floatingswitches">
        <t:optionbutton optionname="showmap" title="Map" defaultvalue="${!maphidden}" element="occurrencemap"/>
        <t:optionbutton optionname="showgeo" title="Geo" defaultvalue="false" element="georreferencer"/>
        <t:optionbutton optionname="showocc" title="Occ" defaultvalue="true" element="occurrencetable-holder"/>
    </div>
    <div id="loader">
        <div id="loadermsg">Um momento...</div>
    </div>
</body>
</html>
