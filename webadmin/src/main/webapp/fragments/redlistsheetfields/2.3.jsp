<%@ page pageEncoding="UTF-8" %>
<c:if test="${occurrences == null}">
    No occurrence records
</c:if>
<c:if test="${occurrences != null}">
    <table class="subtable">
        <tr><td class="hide-in-print"><b>AOO</b></td><td>
            <input type="hidden" name="geographicalDistribution_AOO" value="${AOO}"/>
            <b><fmt:formatNumber value="${AOO}" maxFractionDigits="4" groupingUsed="false"/> km<sup>2</sup></b><span class="hide-in-print"> (${nquads} ${sizeofsquare}x${sizeofsquare} km squares)</span>
        </td></tr>
    <c:if test="${user.canVIEW_FULL_SHEET()}">
        <tr><td>Historical AOO</td><td>
            <input type="hidden" name="geographicalDistribution_historicalAOO" value="${hAOO}"/>
            <fmt:formatNumber value="${hAOO}" maxFractionDigits="0" groupingUsed="false"/> km<sup>2</sup> (${hnquads} ${sizeofsquare}x${sizeofsquare} km squares)
            <span class="legend alwaysvisible">Nota: a AOO histórica deve ser considerada apenas no caso de redução da AOO e após análise crítica da fiabilidade dos dados</span>
        </td></tr>
    </c:if>
    </table>
</c:if>
