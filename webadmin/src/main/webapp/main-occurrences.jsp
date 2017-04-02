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
	<link rel="stylesheet" href="https://unpkg.com/leaflet@1.0.2/dist/leaflet.css" />
	<script src="https://unpkg.com/leaflet@1.0.2/dist/leaflet.js"></script>
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
    <!--<div id="occurrencetoolbar">TOOLS</div>-->
    <div id="occurrencetable-holder">
        <jsp:include page="occurrences-pages.jsp"></jsp:include>
    </div>
    <div id="occurrencemap">
        <div id="mapcontainer"></div>
    </div>
    <div class="button" id="hidemap">Hide map</div>
</body>
</html>
