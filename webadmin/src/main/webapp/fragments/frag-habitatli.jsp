<%@ page pageEncoding="UTF-8" %><%@ page contentType="text/html; charset=UTF-8" %><%@ page session="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:useBean id="rand" class="pt.floraon.driver.utils.StringUtils" scope="application" />

<c:set var="count" value="0" scope="page" />
<c:set var="started" value="0" scope="page" />
<c:set var="ishidden" value="${param.hidden == 1 ? ' hidden' : ''}"/>
<c:set var="minselectablelevel" value="${param.minselectablelevel == null ? 0 : param.minselectablelevel}" />

<c:forEach var="hab" items="${habitats}">
<c:set var="count" value="${count + 1}" scope="page"/>
<c:set var="randid" value="${rand.randomString(8)}"/>
<c:if test="${started == 0}">
    <c:set var="started" value="1" scope="page" />
    <ul class="checkboxes buttonright alwaysvisible list${ishidden}">
    <c:if test="${param.id == null || param.id == ''}"><input type="hidden" name="ecology_HabitatTypes" value=""/></c:if>
</c:if>
<li data-key="${hab.getID()}">
<c:if test="${habitatTypesIds != null && habitatTypesIds.contains(hab.getID())}">
    <c:if test="${hab.getLevel() == null || hab.getLevel() >= minselectablelevel}">
        <input type="checkbox" name="ecology_HabitatTypes" value="${hab.getID()}" id="${randid}" checked="checked"/>
    </c:if>
    <c:if test="${hab.getLevel() < minselectablelevel}">
        <input type="checkbox" name="ecology_HabitatTypes" value="${hab.getID()}" id="${randid}" checked="checked" disabled="disabled"/>
    </c:if>
</c:if>
<c:if test="${!(habitatTypesIds != null && habitatTypesIds.contains(hab.getID()))}">
    <c:if test="${hab.getLevel() == null || hab.getLevel() >= minselectablelevel}">
        <input type="checkbox" name="ecology_HabitatTypes" value="${hab.getID()}" id="${randid}"/>
    </c:if>
    <c:if test="${hab.getLevel() < minselectablelevel}">
        <input type="checkbox" name="ecology_HabitatTypes" value="${hab.getID()}" id="${randid}" disabled="disabled"/>
    </c:if>
</c:if>
<label for="${randid}"> <div style="flex: 1 1 auto; margin-right:5px"><c:if test="${hab.getLevel() == null || hab.getLevel() >= minselectablelevel}"><div class="light"></div></c:if><span class="title">${hab.getName()}</span></div><div class="legend">${hab.getDescription()}</div>
<div class="button smallround">ver subtipos</div>
</label>
<c:if test="${hab.getLevel() != null && hab.getLevel() < maxlevel}">
<jsp:include page="/checklist/api/lists">
    <jsp:param name="w" value="tree" />
    <jsp:param name="id" value="${hab.getID()}" />
    <jsp:param name="type" value="habitat" />
    <jsp:param name="taxent" value="${param.taxent}" />
    <jsp:param name="hidden" value="${hab.getLevel() >= hideafterlevel ? 1 : 0}" />
</jsp:include>
<%--<jsp:include page="checklist/api/lists?w=tree&type=habitat&id=${hab._getIDURLEncoded()}&hidden=${hab.getLevel() >= hideafterlevel ? 1 : 0}"></jsp:include>--%>
</c:if>
</li>
</c:forEach>
<c:if test="${count > 0}"></ul></c:if>
<c:if test="${count == 0}"><label class="placeholder${ishidden}">sem subtipos</label></c:if>
