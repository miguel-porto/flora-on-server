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
    <h1>O Livro Vermelho</h1>
    <p>O primeiro Livro Vermelho dos Invertebrados de Portugal Continental é uma obra há muito aguardada e fundamental para a conservação da biodiversidade do nosso país. Reúne os resultados da avaliação do risco de extinção de 863 espécies de invertebrados terrestres e dulçaquícolas, uma pequena fração da rica biodiversidade nacional conhecida, que é superior a 15.000 espécies. As espécies avaliadas pertencem a vários grupos taxonómicos, tais como gastrópodes, bivalves, aranhas, crustáceos e insetos, e incluem um número assinalável de endemismos lusitânicos e ibéricos. Cerca de 200 espécies foram classificadas como ameaçadas e esta obra dá a conhecer a sua ecologia, distribuição em Portugal continental e tendências populacionais, a par da identificação das principais ameaças às suas populações e das medidas necessárias à sua conservação.</p>
    <p>O trabalho aqui apresentado resulta da colaboração de mais de 360 pessoas e de dezenas de instituições e pretende-se que venha a constituir uma referência no apoio à tomada de decisão em assuntos relacionados com a conservação da natureza e a gestão do território, mas também como instrumento educativo e de sensibilização para a conservação da biodiversidade.</p>
    <p></p>
    <p>O Livro Vermelho dos Invertebrados de Portugal continental pode ser descarregado aqui</p>
    <h1>A lista de espécies avaliadas</h1>
    <p>A avaliação incidiu sobre um conjunto de espécies previamente selecionado, por ser inviável fazê-lo para todas as espécies de invertebrados de Portugal. Numa primeira fase, efetuou-se a seleção dos grupos para avaliação do risco de extinção tendo em consideração a informação fornecida pelos especialistas taxonómicos a realizar trabalho ativo nessa área do conhecimento. A seleção das espécies dentro de cada grupo de invertebrados procurou incluir os endemismos de Portugal continental e da Península Ibérica, as espécies com um reduzido número de registos de ocorrência no nosso país e aquelas que se suspeitava estarem ameaçadas ou que os seus habitats se encontram em regressão. Foram ainda incluídas no processo de avaliação as espécies de invertebrados terrestres e dulçaquícolas listadas na Diretiva Habitats.</p>
    <p>Ao elenco inicial de espécies a avaliar (707) foram adicionadas algumas espécies que se suspeitou estarem em declínio e para as quais se obteve informação suficiente para a sua avaliação durante o desenvolvimento do projeto, e foram eliminadas espécies cuja ocorrência em Portugal se mostrou duvidosa ou para as quais ocorreram alterações taxonómicas importantes. Desta forma, o número de espécies avaliadas no âmbito do projeto foi de 863, sendo 22 crustáceos, 79 gastrópodes, 10 bivalves, 43 aracnídeos e 709 insetos.</p>
    <p></p>
    <p>A lista de espécies avaliadas e o resultado do processo de avaliação podem ser consultados aqui</p>
    </div>
    <jsp:include page="LVI-logos.html" flush="true" />
</body>
</html>

