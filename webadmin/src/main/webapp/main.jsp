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
    <div class="outer">
        <img src="images/logo-LV-cor-fundoclaro_800.png" alt="logo"/>
        <div style="width:100%"></div>
        <%--<c:if test="${user.isGuest()}"><p style="font-size:3em">portal de trabalho</p></c:if>
        <c:if test="${!user.isGuest()}">--%>
        <%--<p style="font-size:0.7em">Este portal está ainda em desenvolvimento, pelo que sofre actualizações frequentes. Por segurança, não deverá trabalhar no portal depois da meia noite, pois pode perder os seus dados se houver uma actualização.</p>--%>
        <c:if test="${globalSettings.isClosedForAdminTasks()}"><div class="warning"><p><img class="exclamation" src="./images/exclamation.png"/><span>Portal temporariamente fechado para tarefas administrativas</span></p>Por favor volte novamente amanhã.</div></c:if>
        <div style="width:100%"></div>
        <ul id="mainmenu" class="mainmenu">
            <li class="section1"><div class="bullet"></div><span><a href="checklist"><fmt:message key="Modules.2"/></a></span></li>
            <c:if test="${!user.isGuest()}">
            <li class="section2"><div class="bullet"></div><span><a href="redlist/lu"><fmt:message key="Modules.1"/></a>
            <%--
                <c:if test="${redlistterritories.size() > 0}">
                    <c:forEach var="terr" items="${redlistterritories}">
                        <a href="redlist/${terr}"> | dataset for ${terr}</a>
                    </c:forEach>
                </c:if>
            --%>
            </span></li>
            <li class="section3"><div class="bullet"></div><span><a href="occurrences?w=occurrenceview"><fmt:message key="Modules.3"/></a></span></li>
            </c:if>
            <c:if test="${user.isAdministrator()}"><li class="section4"><div class="bullet"></div><span><a href="adminpage">Administration</a></span></li></c:if>
            <c:if test="${!user.isAdministrator() && !user.isGuest()}"><li class="section4"><div class="bullet"></div><span><a href="adminpage">Personal area</a></span></li></c:if>
        </ul>
<%--
        <div class="bigbutton section2">
            <h1><a href="checklist"><fmt:message key="Modules.2"/></a></h1>
        </div>
        <div class="bigbutton section3">
            <h1><fmt:message key="Modules.1"/></h1>
            <c:if test="${user.canCREATE_REDLIST_DATASETS()}">
                <div class="subbutton"><a href="redlist">create new red list dataset</a></div>
            </c:if>
            <c:if test="${redlistterritories.size() > 0}">
                <c:forEach var="terr" items="${redlistterritories}">
                    <div class="subbutton"><a href="redlist/${terr}">dataset for ${terr}</a></div>
                </c:forEach>
            </c:if>
        </div>
        <c:if test="${!user.isGuest()}">
        <div class="bigbutton section4">
            <h1><a href="occurrences?w=occurrenceview"><fmt:message key="Modules.3"/></a></h1>
        </div>
        </c:if>
        <c:if test="${user.isAdministrator()}">
        <div class="bigbutton section2">
            <h1><a href="adminpage">Administration</a></h1>
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
    <a href="https://github.com/miguel-porto/flora-on-server">
    <svg style="position: absolute; top: 0; left: 0; border: 0; width:48px; height:48px; margin:6px" version="1.1" viewBox="0 0 16 16"><path fill-rule="evenodd" d="M8 0C3.58 0 0 3.58 0 8c0 3.54 2.29 6.53 5.47 7.59.4.07.55-.17.55-.38 0-.19-.01-.82-.01-1.49-2.01.37-2.53-.49-2.69-.94-.09-.23-.48-.94-.82-1.13-.28-.15-.68-.52-.01-.53.63-.01 1.08.58 1.23.82.72 1.21 1.87.87 2.33.66.07-.52.28-.87.51-1.07-1.78-.2-3.64-.89-3.64-3.95 0-.87.31-1.59.82-2.15-.08-.2-.36-1.02.08-2.12 0 0 .67-.21 2.2.82.64-.18 1.32-.27 2-.27.68 0 1.36.09 2 .27 1.53-1.04 2.2-.82 2.2-.82.44 1.1.16 1.92.08 2.12.51.56.82 1.27.82 2.15 0 3.07-1.87 3.75-3.65 3.95.29.25.54.73.54 1.48 0 1.07-.01 1.93-.01 2.2 0 .21.15.46.55.38A8.013 8.013 0 0 0 16 8c0-4.42-3.58-8-8-8z"></path></svg>
    </a>
</body>
</html>

