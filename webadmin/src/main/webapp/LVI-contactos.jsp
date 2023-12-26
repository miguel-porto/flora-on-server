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
	<link rel="apple-touch-icon" sizes="180x180" href="/icons-lvi/apple-touch-icon.png">
    <link rel="icon" type="image/png" sizes="32x32" href="/icons-lvi/favicon-32x32.png">
    <link rel="icon" type="image/png" sizes="16x16" href="/icons-lvi/favicon-16x16.png">
    <link rel="manifest" href="/icons-lvi/site.webmanifest">
    <link rel="mask-icon" href="/icons-lvi/safari-pinned-tab.svg" color="#5bbad5">
    <link rel="shortcut icon" href="/icons-lvi/favicon.ico">
    <meta name="msapplication-TileColor" content="#da532c">
    <meta name="msapplication-config" content="/icons-lvi/browserconfig.xml">
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