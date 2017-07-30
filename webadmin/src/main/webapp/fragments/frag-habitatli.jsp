<%@ page pageEncoding="UTF-8" %><%@ page contentType="text/html; charset=UTF-8" %><%@ page session="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:useBean id="rand" class="pt.floraon.driver.utils.StringUtils" scope="application" />

<c:set var="count" value="0" scope="page" />
<ul class="checkboxes buttonright alwaysvisible list">
    <c:forEach var="hab" items="${habitats}">
    <c:set var="count" value="${count + 1}" scope="page"/>
    <c:set var="randid" value="${rand.randomString(8)}"/>
    <li data-key="${hab.getID()}">
    <c:if test="${habitatTypes != null && habitatTypes.contains(hab.getID())}">
    <input type="checkbox" name="ecology_HabitatTypes" value="${hab.getID()}" id="${randid}" checked="checked"/>
    </c:if>
    <c:if test="${!(habitatTypes != null && habitatTypes.contains(hab.getID()))}">
    <input type="checkbox" name="ecology_HabitatTypes" value="${hab.getID()}" id="${randid}"/>
    </c:if>
    <label for="${randid}"> <div style="flex: 1 1 auto; margin-right:5px"><div class="light"></div><span>${hab.getName()}</span></div><div class="legend">${hab.getDescription()}</div>
    <div class="button smallround">ver subtipos</div>
    </label>
    </li>
    </c:forEach>
    <c:if test="${count == 0}"><label class="placeholder">sem subtipos</label></c:if>
</ul>