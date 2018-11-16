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
<h1>Red List Settings</h1>
<table>
    <tr><th>Option</th><th>Value</th></tr>
    <tr>
        <td>Bloquear edição das fichas aos autores</td>
        <td>
            <table class="subtable">
            <tr><td>Corte geral de edição</td><td>
                <form class="poster" data-path="api/setoptions" data-refresh="true">
                    <input type="hidden" name="territory" value="${territory}"/>
                    <input type="hidden" name="option" value="lockediting"/>
                    <c:if test="${lockediting}">
                    <input type="hidden" name="value" value="false"/>
                    <input type="submit" value="Desbloquear" class="textbutton"/>
                    <div class="button inactive">Bloqueado</div>
                    </c:if>
                    <c:if test="${!lockediting}">
                    <input type="hidden" name="value" value="true"/>
                    <div class="button inactive">Desbloqueado</div>
                    <input type="submit" value="Bloquear" class="textbutton"/>
                    </c:if>
                </form>
            </td></tr>
            <c:if test="${lockediting && unlockedSheets.size() > 0}">
            <tr><td>Excepções</td><td>Atenção! Os seguintes taxa estão desbloqueados:
            <ul>
                <c:forEach items="${unlockedSheets}" var="us">
                <li>
                    <c:url value="" var="url">
                      <c:param name="w" value="taxon" />
                      <c:param name="id" value="${us}" />
                    </c:url>
                    <a href="${url}">${us}</a>
                </li>
                </c:forEach>
            </ul>
            </td></tr>
            </c:if>
            </table>
        </td>
    </tr>
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
