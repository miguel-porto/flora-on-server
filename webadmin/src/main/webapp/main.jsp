<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
	<title>Taxonomy &amp; Checklist Manager</title>
	<link rel="stylesheet" type="text/css" href="base.css"/>
</head>
<body style="text-align:center">
    <div class="outer">
        <div class="bigbuttonwrapper">
            <div class="bigbutton section1">
            <c:choose>
                <c:when test="${user.getPrivileges().size() == 0}">
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
        </div>

        <div class="bigbuttonwrapper">
            <div class="bigbutton section2">
                <h1><a href="/floraon/checklist">Checklist manager</a></h1>
            </div>
        </div>

        <div class="bigbuttonwrapper">
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
        </div>
    </div>
</body>
</html>

