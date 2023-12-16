<%@ page pageEncoding="UTF-8" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page session="false" %>
<c:set var="language" value="${not empty param.language ? param.language : not empty language ? language : pageContext.request.locale}" scope="request" />
<fmt:setLocale value="${language}" />
<fmt:setBundle basename="pt.floraon.driver.globalMessages" />
<!DOCTYPE html>
<html>
<head>
	<title>LVI - O projeto</title>
	<c:if test="${!offline}"><link href='//fonts.googleapis.com/css?family=Lato:300' rel='stylesheet' type='text/css'></c:if>
	<link rel="stylesheet" type="text/css" href="base.css?nocache=${uuid}"/>
    <link rel="apple-touch-icon" sizes="57x57"          href="icon/apple-icon-57x57.png">
    <link rel="apple-touch-icon" sizes="60x60"          href="icon/apple-icon-60x60.png">
    <link rel="apple-touch-icon" sizes="72x72"          href="icon/apple-icon-72x72.png">
    <link rel="apple-touch-icon" sizes="76x76"          href="icon/apple-icon-76x76.png">
    <link rel="apple-touch-icon" sizes="114x114"        href="icon/apple-icon-114x114.png">
    <link rel="apple-touch-icon" sizes="120x120"        href="icon/apple-icon-120x120.png">
    <link rel="apple-touch-icon" sizes="144x144"        href="icon/apple-icon-144x144.png">
    <link rel="apple-touch-icon" sizes="152x152"        href="icon/apple-icon-152x152.png">
    <link rel="apple-touch-icon" sizes="180x180"        href="icon/apple-icon-180x180.png">
    <link rel="icon" type="image/png" sizes="192x192"   href="icon/android-icon-192x192.png">
    <link rel="icon" type="image/png" sizes="32x32"     href="icon/favicon-32x32.png">
    <link rel="icon" type="image/png" sizes="96x96"     href="icon/favicon-96x96.png">
    <link rel="icon" type="image/png" sizes="16x16"     href="icon/favicon-16x16.png">
    <link rel="manifest" href="icon/manifest.json">
    <meta name="msapplication-TileColor" content="#ffffff">
    <meta name="msapplication-TileImage" content="icon/ms-icon-144x144.png">
    <meta name="theme-color" content="#ffffff">
    <style>
    h1 {
      text-align:left;
      margin:revert;
      font-size:revert;
    }
    p {
        margin:revert;
        font-size:1.1em;
    }
    </style>
</head>
<body class="enterpage">
    <div style="margin-left:24%;margin-right:10%">
    <img style="position:absolute; top:0; left:0; width:20%; margin:2%" src="images/logo-LVI.png" alt="logo"/>
    <h1>Contactos</h1>
    <p><b>Projeto Lista Vermelha dos Invertebrados</b></p>
    <p></p>
    <p>FCiências.ID – Associação para a Investigação e Desenvolvimento de Ciências<br/>
    Campo Grande, edifício C1, 3.º piso, 1749-016 Lisboa, Portugal<br/>
    Email: fciencias.id@fciencias-id.pt</p>
    <p></p>
    <p>Coordenação - Mário Boieiro<br/>
    Centro de Ecologia, Evolução e Alterações Ambientais<br/>
    Email: mboieiro@fc.ul.pt</p>
    <p></p>
    <p>Geral<br/>
    Email: lv.invertebrados@gmail.com</p>
    </div>
    <jsp:include page="LVI-logos.html" flush="true" />
</body>
</html>