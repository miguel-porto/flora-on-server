<%@ page pageEncoding="UTF-8" %>
<c:if test="${user.canEDIT_SECTION3()}">
    <table class="triggergroup">
        <tr><td>Category</td><td>
            <t:dropdown name="population_PopulationDecline" values="${population_PopulationDecline}" selectedValue="${rlde.getPopulation().getPopulationDecline()}" trigger="true"/>
        </td></tr>
        <tr class="triggered ${rlde.getPopulation().getPopulationDecline().isTrigger() ? '' : 'hidden'}"><td>Percentage</td><td>
            <t:numericintervalinput name="population_PopulationDeclinePercent" value="${rlde.getPopulation().getPopulationDeclinePercent()}" placeholder="percentage"/>
        </td></tr>
        <tr class="triggered ${rlde.getPopulation().getPopulationDecline().isTrigger() ? '' : 'hidden'}"><td>Justification</td><td>
            <t:editabletext
                privilege="${true}"
                value="${rlde.getPopulation().getPopulationDeclineJustification()}"
                name="population_PopulationDeclineJustification"/>
        </td></tr>
    </table>
</c:if>
<c:if test="${!user.canEDIT_SECTION3()}">
    <table>
        <tr><td>Categoria</td><td><b>${rlde.getPopulation().getPopulationDecline().getLabel()}</b></td></tr>
        <tr><td>Percentagem</td><td>${rlde.getPopulation().getPopulationDeclinePercent()}</td></tr>
        <tr><td>Justificação</td><td>${rlde.getPopulation().getPopulationDeclineJustification()}</td></tr>
    </table>
</c:if>
