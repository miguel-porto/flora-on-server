<%@ page pageEncoding="UTF-8" %>
<c:if test="${user.canEDIT_SECTION3()}">
    <table class="triggergroup">
        <tr><td>Category</td><td>
            <select name="population_PopulationDecline" class="trigger">
                <c:forEach var="tmp" items="${population_PopulationDecline}">
                    <c:if test="${rlde.getPopulation().getPopulationDecline().toString().equals(tmp.toString())}">
                        <option value="${tmp.toString()}" selected="selected" data-trigger="${tmp.isTrigger() ? 1 : 0}">${tmp.getLabel()}</option>
                    </c:if>
                    <c:if test="${!rlde.getPopulation().getPopulationDecline().toString().equals(tmp.toString())}">
                        <option value="${tmp.toString()}" data-trigger="${tmp.isTrigger() ? 1 : 0}">${tmp.getLabel()}</option>
                    </c:if>
                </c:forEach>
            </select>
        </td></tr>
        <tr class="triggered ${rlde.getPopulation().getPopulationDecline().isTrigger() ? '' : 'hidden'}"><td>Percentage</td><td>
            <input type="text" name="population_PopulationDeclinePercent" value="${rlde.getPopulation().getPopulationDeclinePercent()}" placeholder="percentage"/> %
            <c:if test="${rlde.getPopulation().getPopulationDeclinePercent().getError() != null}"><span class="warning">${rlde.getPopulation().getPopulationDeclinePercent().getError()}</span></c:if>
            <span class="legend alwaysvisible"><fmt:message key="DataSheet.msg.interval"/></span>
        </td></tr>
        <tr class="triggered ${rlde.getPopulation().getPopulationDecline().isTrigger() ? '' : 'hidden'}"><td>Justification</td><td>
            <div contenteditable="true" class="contenteditable">${rlde.getPopulation().getPopulationDeclineJustification()}</div>
            <input type="hidden" name="population_PopulationDeclineJustification" value="${fn:escapeXml(rlde.getPopulation().getPopulationDeclineJustification())}"/>
        </td></tr>
    </table>
</c:if>
<c:if test="${!user.canEDIT_SECTION3()}">
    <table>
        <tr><td>Category</td><td>${rlde.getPopulation().getPopulationDecline().getLabel()}</td></tr>
        <tr><td>Percentage</td><td>${rlde.getPopulation().getPopulationDeclinePercent()}</td></tr>
        <tr><td>Justification</td><td>${rlde.getPopulation().getPopulationDeclineJustification()}</td></tr>
    </table>
</c:if>
