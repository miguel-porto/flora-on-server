<%@ page pageEncoding="UTF-8" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page session="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="language" value="${not empty param.language ? param.language : not empty language ? language : pageContext.response.locale}" scope="request" />
<fmt:setLocale value="${language}" />
<fmt:setBundle basename="pt.floraon.redlistdata.fieldValues" />
<c:if test="${param.onlylist == null}">
<h1><fmt:message key="biblio.1" /></h1>
<form class="poster" data-path="../upload/references/" data-refresh="true">
    <h2><fmt:message key="biblio.15" /></h2>
    <p><fmt:message key="biblio.16" /></p>
    <input type="file" name="referenceTable" />
    <input type="submit" class="textbutton" value="Upload"/>
</form>
<form class="poster" data-path="../references/" data-refresh="true">
    <h2><fmt:message key="biblio.2" /></h2>
    <input type="hidden" name="what" value="addreference"/>
    <table class="small">
        <tr><td class="title"><fmt:message key="biblio.3" /></td><td><select name="publicationType"/><option value="ARTICLE"><fmt:message key="biblio.3a" /></option><option value="BOOK"><fmt:message key="biblio.3b" /></option><option value="BOOK_CHAPTER"><fmt:message key="biblio.3c" /></option><option value="THESIS"><fmt:message key="biblio.3d" /></option><option value="OTHER"><fmt:message key="biblio.3e" /></option></select></td></tr>
        <tr><td class="title"><fmt:message key="biblio.4" /></td><td><input type="text" name="authors"/></td></tr>
        <tr><td class="title"><fmt:message key="biblio.5" /></td><td><input type="text" name="year"/></td></tr>
        <tr><td class="title"><fmt:message key="biblio.6" /></td><td><input type="text" name="title"/></td></tr>
        <tr><td class="title"><fmt:message key="biblio.7" /></td><td><input type="text" name="publication"/></td></tr>
        <tr><td class="title"><fmt:message key="biblio.8" /></td><td><input type="text" name="coords"/></td></tr>
        <tr><td class="title"><fmt:message key="biblio.9" /></td><td><input type="text" name="volume"/></td></tr>
        <tr><td class="title"><fmt:message key="biblio.10" /></td><td><input type="text" name="editor"/></td></tr>
        <tr><td class="title"><fmt:message key="biblio.11" /></td><td><input type="text" name="city"/></td></tr>
        <tr><td class="title"><fmt:message key="biblio.12" /></td><td><input type="text" name="pages"/></td></tr>
        <tr><td class="title"><fmt:message key="biblio.13" /></td><td><input type="text" name="code"/></td></tr>
    </table>
    <input type="submit" class="textbutton" value="Add"/>
</form>
</c:if>
<table class="sortable smalltext">
    <thead>
        <tr>
        <th><fmt:message key="biblio.14" /></th>
        <th><fmt:message key="biblio.3" /></th>
        <th><fmt:message key="biblio.4" /></th>
        <th><fmt:message key="biblio.5" /></th>
        <th><fmt:message key="biblio.6" /></th>
        <th><fmt:message key="biblio.7" /></th>
        <th><fmt:message key="biblio.8" /></th>
        <th><fmt:message key="biblio.9" /></th>
        <th><fmt:message key="biblio.10" /></th>
        <th><fmt:message key="biblio.11" /></th>
        <th><fmt:message key="biblio.12" /></th>
        <th><fmt:message key="biblio.13" /></th>
        <c:if test="${user.canMANAGE_REDLIST_USERS() && param.onlylist == null}"><th></th></c:if>
        </tr>
    </thead>
    <c:forEach var="ref" items="${references}">
    <tr data-id="${ref.getID()}" data-citation="${ref._getCitation()}">
        <td>${ref._getCitation()}</td>
        <td>${ref.getPublicationType()}</td>
        <td>${ref.getAuthors()}</td>
        <td>${ref.getYear()}</td>
        <td>${ref.getTitle()}</td>
        <td>${ref.getPublication()}</td>
        <td>${ref.getCoords()}</td>
        <td>${ref.getVolume()}</td>
        <td>${ref.getEditor()}</td>
        <td>${ref.getCity()}</td>
        <td>${ref.getPages()}</td>
        <td>${ref.getCode()}</td>
        <c:if test="${user.canMANAGE_REDLIST_USERS() && param.onlylist == null}"><td><form class="poster" data-path="../references/" data-refresh="true" data-confirm="true"><input type="hidden" name="what" value="deletereference"/><input type="hidden" name="id" value="${ref.getID()}"/><input type="submit" class="textbutton" value="Delete"/></form></td></c:if>
    </tr>
    </c:forEach>
</table>