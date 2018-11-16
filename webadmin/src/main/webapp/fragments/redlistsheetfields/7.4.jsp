<%@ page pageEncoding="UTF-8" %>
<c:if test="${occurrences.size() > 0}">
    <c:if test="${pointsOutsidePA == totalPoints}"><p><b>Todas as ocorrências estão fora do SNAC</b></p></c:if>
    <c:if test="${pointsOutsidePA == 0}"><p><b>Todas as ocorrências estão dentro do SNAC</b></p></c:if>
    <c:if test="${pointsOutsidePA != totalPoints && pointsOutsidePA != 0}"><p><fmt:formatNumber value="${100 - ((pointsOutsidePA / totalPoints) * 100)}" maxFractionDigits="1"/>% dos locais de ocorrência (${totalPoints - pointsOutsidePA}/${totalPoints}) estão dentro do SNAC.<span class="legend alwaysvisible">Nota: este valor pode estar enviesado pela existência de áreas intensamente amostradas.</span></p></c:if>
    <p><fmt:formatNumber value="${(locationsInPA / nclusters) * 100}" maxFractionDigits="1"/>% <fmt:message key="DataSheet.label.7.4a" /> (${locationsInPA}/${nclusters})</p>
    <table class="sortable smalltext">
        <tr><th>Protected Area</th><th>Type</th><th><fmt:message key="DataSheet.label.7.4b" /></th></tr>
        <c:forEach var="tmp" items="${occurrenceInProtectedAreas}">
            <tr>
                <td>${tmp.getKey().getProperties().get("SITE_NAME")}</td>
                <td>${tmp.getKey().getProperties().get("TIPO")}</td>
                <td>${tmp.getValue()}</td>
            </tr>
        </c:forEach>
    </table>
</c:if>
<c:if test="${occurrences.size() == 0}">
    <p>No occurrence records</p>
</c:if>
