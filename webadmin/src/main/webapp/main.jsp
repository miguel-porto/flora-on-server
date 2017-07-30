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
	<link href='//fonts.googleapis.com/css?family=Lato:300' rel='stylesheet' type='text/css'>
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
                <tr><td>Username:</td><td><input type="text" name="username"/></td>
                <td>Password:</td><td><input type="password" name="password"/></td><td><input type="submit" class="subbutton" value="Login"/></td></tr>
                </table>
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
    <div class="outer">
        <img src="images/logo-LV-cor-fundoclaro_800.png" alt="logo"/>
        <div style="width:100%"></div>
        <%--<c:if test="${user.isGuest()}"><p style="font-size:3em">portal de trabalho</p></c:if>
        <c:if test="${!user.isGuest()}">--%>
        <p style="font-size:0.7em">Este portal está ainda em desenvolvimento, pelo que sofre actualizações frequentes. Por segurança, não deverá trabalhar no portal depois da meia noite, pois pode perder os seus dados se houver uma actualização.</p>
        <div style="width:100%"></div>
        <ul id="mainmenu">
            <li class="section1"><div class="bullet"></div><span><a href="/floraon/checklist"><fmt:message key="Modules.2"/></a></span></li>
            <c:if test="${!user.isGuest()}">
            <li class="section2"><div class="bullet"></div><span><a href="/floraon/redlist/lu"><fmt:message key="Modules.1"/></a>
            <%--
                <c:if test="${redlistterritories.size() > 0}">
                    <c:forEach var="terr" items="${redlistterritories}">
                        <a href="/floraon/redlist/${terr}"> | dataset for ${terr}</a>
                    </c:forEach>
                </c:if>
            --%>
            </span></li>
            <li class="section3"><div class="bullet"></div><span><a href="/floraon/occurrences?w=occurrenceview"><fmt:message key="Modules.3"/></a></span></li>
            </c:if>
            <c:if test="${user.isAdministrator()}"><li class="section4"><div class="bullet"></div><span><a href="/floraon/adminpage">Administration</a></span></li></c:if>
        </ul>
<%--
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
--%>
    </div>
    <div id="stamp">portal de trabalho</div>
    <div id="logobar">
        <div><p>Coordenação</p><div class="logos"><img src="images/logo_SPBotanica.png"/><img src="images/logo_Phytos.jpg"/></div></div>
        <div><p>Parceria</p><div class="logos"><img src="images/logo_ICNF.png"/></div></div>
        <div><p>Co-financiamento</p><div class="logos"><img src="images/logo_poseur.png"/><img src="images/logo_Portugal_2020.png"/><img src="images/logo_UE.png"/><img src="images/logo_FundoAmbiental.png"/></div></div>
        <div><p>Apoio</p><div class="logos"><img src="images/logo_INCD.png"/></div></div>
    </div>
    <a href="https://github.com/miguel-porto/flora-on-server"><img style="position: absolute; top: 0; left: 0; border: 0;" src="https://camo.githubusercontent.com/121cd7cbdc3e4855075ea8b558508b91ac463ac2/68747470733a2f2f73332e616d617a6f6e6177732e636f6d2f6769746875622f726962626f6e732f666f726b6d655f6c6566745f677265656e5f3030373230302e706e67" alt="Fork me on GitHub" data-canonical-src="https://s3.amazonaws.com/github/ribbons/forkme_left_green_007200.png"></a>
</body>
</html>

