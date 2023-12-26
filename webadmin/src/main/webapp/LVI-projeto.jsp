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
    <h1>O projeto</h1>
    <p>O projeto “Lista Vermelha de Grupos de Invertebrados Terrestres e Dulçaquícolas de Portugal Continental” (LVI) teve como principal objetivo a avaliação do risco de extinção de 863 espécies selecionadas de vários grupos de invertebrados, onde se incluem insetos, aranhas, caracóis, lesmas, mexilhões de rio e crustáceos. Neste vasto grupo de espécies estão incluídos vários endemismos nacionais e ibéricos, espécies raras e as espécies protegidas listadas na Diretiva Habitats.</p>
    <p>O projeto teve ainda como objetivos gerais a divulgação da rica biodiversidade nacional de invertebrados e a valorização destes organismos em matéria de conservação da natureza e pelo público em geral, dado o seu papel crucial em todos os ecossistemas.</p>
    <p></p>
    <p>Mais informação na <a href="http://lvinvertebrados.pt/">página do projeto (http://lvinvertebrados.pt/)</a></p>
    <h1>Ficha do projeto</h1>
    <p><b>Designação do projeto</b>: Elaboração da Lista Vermelha de grupos de Invertebrados Terrestres e de Água Doce de Portugal Continental</p>
    <p><b>Código do projeto</b>: POSEUR-03-2215-FC-000094</p>
    <p><b>Objetivo principal</b>: Proteger o ambiente e promover a eficiência no uso dos recursos</p>
    <p><b>Região de intervenção</b>: Portugal continental</p>
    <p><b>Entidade beneficiária</b>: FCiências.ID - Associação para a Investigação e Desenvolvimento de Ciências</p>
    <p><b>Entidade parceira</b>: Instituto da Conservação da Natureza e das Florestas, I.P.</p>
    <p><b>Data de início</b>: 01-06-2018</p>
    <p><b>Data de conclusão</b>: 31-07-2023</p>
    <p><b>Custo total elegível</b>: 528.490,23 €</p>
    <p><b>Apoio financeiro da União Europeia (Fundo de Coesão da UE)</b>: 450 000,00 €</p>
    <p><b>Apoio financeiro público nacional/regional (Fundo Ambiental)</b>: 78 490,23 €</p>
    <p><a href="https://fciencias-id.pt/sites/fciencias-id/files/cc392 - Ficha de Projeto.v2.pdf">A ficha de projeto POSEUR</a></p>
    </div>
    <jsp:include page="LVI-logos.html" flush="true" />
</body>
</html>

