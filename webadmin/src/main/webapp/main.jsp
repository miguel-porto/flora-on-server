<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page session="false" %>
<!DOCTYPE html>
<html>
<head>
	<title>Taxonomy &amp; Checklist Manager</title>
	<link rel="stylesheet" type="text/css" href="base.css"/>
</head>
<body style="text-align:center">
    <div class="outer">
        <div class="bigbutton section1">
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

        <div class="bigbutton section2">
            <h1><a href="/floraon/checklist">Checklist manager</a></h1>
        </div>

        <div class="bigbutton section3">
            <h1>Red list data portal</h1>
            <c:if test="${user.canCREATE_REDLIST_DATASETS()}">
                <div class="subbutton"><a href="/floraon/redlist">create new red list dataset</a></div>
            </c:if>
            <c:if test="${redlistterritories.size() > 0}">
                <c:forEach var="terr" items="${redlistterritories}">
                    <div class="subbutton"><a href="/floraon/redlist/${terr}">dataset for ${terr}</a></div>
                </c:forEach>
            </c:if>
        </div>

<!--        <div class="bigbutton section4">
            <h1><a href="/floraon/occurrences">Occurrence manager</a></h1>
        </div>-->
    </div>

    <a href="https://github.com/miguel-porto/flora-on-server"><img style="position: absolute; top: 0; left: 0; border: 0;" src="https://camo.githubusercontent.com/121cd7cbdc3e4855075ea8b558508b91ac463ac2/68747470733a2f2f73332e616d617a6f6e6177732e636f6d2f6769746875622f726962626f6e732f666f726b6d655f6c6566745f677265656e5f3030373230302e706e67" alt="Fork me on GitHub" data-canonical-src="https://s3.amazonaws.com/github/ribbons/forkme_left_green_007200.png"></a>
</body>
</html>

