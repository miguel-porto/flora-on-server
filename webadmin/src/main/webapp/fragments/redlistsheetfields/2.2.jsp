<%@ page pageEncoding="UTF-8" %>
<c:if test="${occurrences == null}">
    No occurrence records
</c:if>
<c:if test="${occurrences != null}">
    <table class="subtable">
        <tr><td><b>EOO</b></td><td>
            <input type="hidden" name="geographicalDistribution_EOO" value="${EOO}"/>
            <b><fmt:formatNumber value="${EOO}" maxFractionDigits="0" groupingUsed="false"/> km<sup>2</sup></b> (${occurrences.size()} occurrences, ${nclusters} sites)
        </td></tr>
    <c:if test="${user.canVIEW_FULL_SHEET()}">
        <c:if test="${realEOO != null && realEOO != EOO}">
        <tr><td>Real EOO</td><td>
            <fmt:formatNumber value="${realEOO}" maxFractionDigits="0" groupingUsed="false"/> km<sup>2</sup>
        </td></tr>
        </c:if>
        <tr><td>Historical EOO</td><td>
            <input type="hidden" name="geographicalDistribution_historicalEOO" value="${hEOO}"/>
            <fmt:formatNumber value="${hEOO}" maxFractionDigits="0" groupingUsed="false"/> km<sup>2</sup> (${historicalOccurrences.size()} occurrences)
        </td></tr>
    </c:if>
    </table>
</c:if>
