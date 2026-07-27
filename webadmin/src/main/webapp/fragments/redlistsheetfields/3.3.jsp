<%@ page pageEncoding="UTF-8" %>
<c:if test="${user.canEDIT_SECTION3()}">
    <table class="triggergroup">
        <tr><td>Type</td><td>
            <t:dropdown name="population_TypeOfEstimate" values="${population_TypeOfEstimate}" selectedValue="${rlde.getPopulation().getTypeOfEstimate()}" trigger="true"/>
        </td></tr>
        <tr class="triggered ${rlde.getPopulation().getTypeOfEstimate().isTrigger() ? '' : 'hidden'}"><td>Description</td><td>
            <div contenteditable="true" class="contenteditable">${rlde.getPopulation().getNrMatureIndividualsDescription()}</div>
            <input type="hidden" name="population_NrMatureIndividualsDescription" value="${fn:escapeXml(rlde.getPopulation().getNrMatureIndividualsDescription())}"/>
        </td></tr>
    </table>
</c:if>
<c:if test="${!user.canEDIT_SECTION3()}">
    <table>
        <tr><td>Tipo</td><td><b>${rlde.getPopulation().getTypeOfEstimate().getLabel()}</b></td></tr>
        <tr><td>Descrição</td><td>${rlde.getPopulation().getNrMatureIndividualsDescription()}</td></tr>
    </table>
</c:if>
