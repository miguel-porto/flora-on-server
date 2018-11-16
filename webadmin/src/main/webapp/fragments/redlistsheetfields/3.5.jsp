<%@ page pageEncoding="UTF-8" %>
<c:if test="${user.canEDIT_SECTION3()}">
    <table class="triggergroup">
        <tr><td>Category</td><td>
            <div class="checkboxes list" tabindex="0">
                <input type="hidden" name="population_PopulationSizeReduction" value=""/>
                <c:forEach var="tmp" items="${population_PopulationSizeReduction}">
                    <c:if test="${rlde.getPopulation()._getPopulationSizeReductionAsList().contains(tmp)}">
                        <input type="checkbox" class="trigger" name="population_PopulationSizeReduction" value="${tmp.toString()}" checked="checked" id="psr_${tmp}" data-trigger="${tmp.isTrigger() ? 1 : 0}"/>
                        <label for="psr_${tmp}"> <div class="light"></div><span>${tmp.getLabel()}</span></label>
                    </c:if>
                    <c:if test="${!rlde.getPopulation()._getPopulationSizeReductionAsList().contains(tmp)}">
                        <input type="checkbox" class="trigger" name="population_PopulationSizeReduction" value="${tmp.toString()}" id="psr_${tmp}" data-trigger="${tmp.isTrigger() ? 1 : 0}"/>
                        <label for="psr_${tmp}"> <div class="light"></div><span>${tmp.getLabel()}</span></label>
                    </c:if>
                </c:forEach>
                <label class="placeholder"><fmt:message key="DataSheet.msg.clickxpand"/></label>
            </div>
        </td></tr>

        <tr class="triggered ${rlde.getPopulation()._isAnyPopulationSizeReductionSelected() ? '' : 'hidden'}"><td>Percentage</td><td>
            <input type="text" name="population_PopulationTrend" value="${rlde.getPopulation().getPopulationTrend()}" placeholder="percentage"/> %
            <c:if test="${rlde.getPopulation().getPopulationTrend().getError() != null}"><span class="warning">${rlde.getPopulation().getPopulationTrend().getError()}</span></c:if>
            <span class="legend alwaysvisible"><fmt:message key="DataSheet.msg.interval"/></span>
        </td></tr>
        <tr class="triggered ${rlde.getPopulation()._isAnyPopulationSizeReductionSelected() ? '' : 'hidden'}"><td>Justification</td><td>
            <div contenteditable="true" class="contenteditable">${rlde.getPopulation().getPopulationSizeReductionJustification()}</div>
            <input type="hidden" name="population_PopulationSizeReductionJustification" value="${fn:escapeXml(rlde.getPopulation().getPopulationSizeReductionJustification())}"/>
        </td></tr>
    </table>
</c:if>
<c:if test="${!user.canEDIT_SECTION3()}">
    <table>
        <tr><td>Percentage</td><td>${rlde.getPopulation().getPopulationTrend()} %</td></tr>
        <tr><td>Categories</td><td><ul>
            <c:forEach var="psr" items="${rlde.getPopulation().getPopulationSizeReduction()}"><li>${psr.getLabel()}</li></c:forEach>
        </ul></td></tr>
        <tr><td>Justification</td><td>${rlde.getPopulation().getPopulationSizeReductionJustification()}</td></tr>
    </table>
</c:if>
