<%@ page pageEncoding="UTF-8" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page session="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<fmt:setBundle basename="pt.floraon.redlistdata.fieldValues" />
<c:if test="${!user.canMANAGE_REDLIST_USERS()}">
    <div class="warning"><b>You&#8217;re not authorized to enter this page</b></div>
</c:if>
<c:if test="${user.canMANAGE_REDLIST_USERS()}">
<h1>Control panel</h1>
<h2>Bloqueio de edição das fichas</h2>
<table>
    <tr><td>Corte geral de edição</td><td>
        <form class="poster" data-path="api/setoptions" data-refresh="true">
            <input type="hidden" name="territory" value="${territory}"/>
            <input type="hidden" name="option" value="lockediting"/>
            <c:if test="${lockediting}">
            <input type="hidden" name="value" value="false"/>
            <input type="submit" value="Desbloquear" class="textbutton"/>
            <div class="button inactive"><img src="../images/locked.png" class="lock"/>Bloqueado</div>
            </c:if>
            <c:if test="${!lockediting}">
            <input type="hidden" name="value" value="true"/>
            <div class="button inactive"><img src="../images/unlocked.png" class="lock"/>Desbloqueado</div>
            <input type="submit" value="Bloquear" class="textbutton"/>
            </c:if>
        </form>
    </td></tr>
    <tr><td>Corte parcial de edição</td><td>
        <form class="poster" data-path="api/setoptions" data-refresh="true">
            <input type="hidden" name="territory" value="${territory}"/>
            <input type="hidden" name="option" value="lockeditingfortags"/>
            <div class="multiplechooser left">
            <c:forEach var="tmp" items="${allTags}">
                <c:if test="${lockedTags.contains(tmp)}"><div class="wordtag"><img src="../images/locked.png" class="lock"/>${tmp}</div></c:if>
                <c:if test="${!lockedTags.contains(tmp)}">
                <input type="checkbox" name="tags" value="${tmp}" id="tags_${tmp}"/>
                <label for="tags_${tmp}" class="wordtag togglebutton"> ${tmp}</label>
                </c:if>
            </c:forEach>
            </div>
            <input type="submit" value="Bloquear tags" class="textbutton"/>
        </form>
        <form class="poster" data-path="api/setoptions" data-refresh="true">
            <input type="hidden" name="territory" value="${territory}"/>
            <input type="hidden" name="option" value="unlockeditingforalltags"/>
            <input type="submit" value="Desbloquear todos" class="textbutton"/>
        </form>
    </td></tr>
    <c:if test="${(lockediting || lockedTags.size() > 0) && unlockedSheets.size() > 0}">
    <tr><td>Excepções (fichas desbloqueadas)</td><td>Atenção! Os seguintes taxa estão desbloqueados:
    <ul>
        <c:forEach items="${unlockedSheets}" var="us">
        <li>
            <c:url value="" var="url">
              <c:param name="w" value="taxon" />
              <c:param name="id" value="${us}" />
            </c:url>
            <img src="../images/unlocked.png" class="lock"/><a href="${url}">${us}</a>
        </li>
        </c:forEach>
    </ul>
    </td></tr>
    </c:if>
</table>
<h2>Red List Settings</h2>
<table>
    <tr><th>Option</th><th>Value</th></tr>
    <tr>
        <td>Ano a partir do qual é considerado registo histórico</td>
        <td>
            <form class="poster" data-path="api/setoptions" data-refresh="true">
                <input type="hidden" name="territory" value="${territory}"/>
                <input type="hidden" name="option" value="historicalthreshold"/>
                <input type="number" name="value" value="${historicalthreshold}"/>
                <input type="submit" value="Set" class="textbutton"/>
            </form>
        </td>
    </tr>
    <tr>
        <td>Mostrar edições dos últimos</td>
        <td>
            <form class="poster" data-path="api/setoptions" data-refresh="true">
                <input type="hidden" name="territory" value="${territory}"/>
                <input type="hidden" name="option" value="editionslastndays"/>
                <input type="number" name="value" value="${editionslastndays}"/> dias
                <input type="submit" value="Set" class="textbutton"/>
            </form>
        </td>
    </tr>
</table>
</c:if>
