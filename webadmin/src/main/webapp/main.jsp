<%@ page pageEncoding="UTF-8" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page session="false" %>
<c:set var="language" value="${not empty param.language ? param.language : not empty language ? language : pageContext.response.locale}" scope="request" />
<fmt:setLocale value="${language}" />
<fmt:setBundle basename="pt.floraon.driver.globalMessages" />
<!DOCTYPE html>
<html>
<head>
	<title>Taxonomy &amp; Checklist Manager</title>
	<link href='http://fonts.googleapis.com/css?family=Lato:300' rel='stylesheet' type='text/css'>
	<link rel="stylesheet" type="text/css" href="base.css?nocache=${uuid}"/>
</head>
<body style="text-align:center" class="enterpage">
    <div id="logindiv">
    <c:choose>
        <c:when test="${user.isGuest()}">
            <form action="login" method="post" id="loginform">
                <table>
                <c:if test="${param.reason!=null}">
                Incorrect username or password.
                </c:if>
                <tr><td>Username:</td><td><input type="text" name="username"/></td></tr>
                <tr><td>Password:</td><td><input type="password" name="password"/></td></tr>
                </table>
                <input type="submit" class="subbutton" value="Login"/>
            </form>
        </c:when>
        <c:otherwise>
            <p>Welcome <c:out value="${user.getName()}"></c:out></p>
            <form action="login" method="post">
                <input type="hidden" name="logout" value="1"/>
                <input type="submit" value="Logout"/>
            </form>
        </c:otherwise>
    </c:choose>
    </div>
    <div class="outer">
        <c:if test="${user.isAdministrator()}">
            <c:if test="${orphan || errors.hasNext()}">
            <div class="warning">
                <c:if test="${orphan}">
                <p>Caro administrador, há táxones não ligados ao grafo principal.</p>
                <a href="checklist?w=graph&show=orphan">Ver táxones</a>
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
            </c:if>
        </c:if>
        <img src="images/logo-LV-cor-fundoclaro_800.png" alt="logo"/>
        <div style="width:100%"></div>
        <p style="font-size:0.7em">
            Este portal está ainda em desenvolvimento, pelo que sofre actualizações frequentes.
            Por segurança, não deverá trabalhar no portal depois da meia noite, pois pode perder os seus dados se houver uma actualização.
        </p>
        <div style="width:100%"></div>
        <div class="bigbutton section2">
            <h1><a href="/floraon/checklist"><fmt:message key="Modules.2"/></a></h1>
        </div>
        <div class="bigbutton section3">
            <h1><fmt:message key="Modules.1"/></h1>
            <c:if test="${user.canCREATE_REDLIST_DATASETS()}">
                <div class="subbutton"><a href="/floraon/redlist">create new red list dataset</a></div>
            </c:if>
            <c:if test="${redlistterritories.size() > 0}">
                <c:forEach var="terr" items="${redlistterritories}">
                    <div class="subbutton"><a href="/floraon/redlist/${terr}">dataset for ${terr}</a></div>
                </c:forEach>
            </c:if>
        </div>
        <c:if test="${!user.isGuest()}">
        <div class="bigbutton section4">
            <h1><a href="/floraon/occurrences?w=occurrenceview"><fmt:message key="Modules.3"/></a></h1>
        </div>
        </c:if>
        <c:if test="${user.isAdministrator()}">
        <div class="bigbutton section2">
            <h1><a href="/floraon/adminpage">Administration</a></h1>
        </div>
        </c:if>
    </div>

    <a href="https://github.com/miguel-porto/flora-on-server"><img style="position: absolute; top: 0; left: 0; border: 0;" src="https://camo.githubusercontent.com/121cd7cbdc3e4855075ea8b558508b91ac463ac2/68747470733a2f2f73332e616d617a6f6e6177732e636f6d2f6769746875622f726962626f6e732f666f726b6d655f6c6566745f677265656e5f3030373230302e706e67" alt="Fork me on GitHub" data-canonical-src="https://s3.amazonaws.com/github/ribbons/forkme_left_green_007200.png"></a>
</body>
</html>

