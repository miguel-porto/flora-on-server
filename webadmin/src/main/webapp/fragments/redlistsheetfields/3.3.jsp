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
        <tr><td>Type</td><td>${rlde.getPopulation().getTypeOfEstimate().getLabel()}</td></tr>
        <tr><td>Description</td><td>${rlde.getPopulation().getNrMatureIndividualsDescription()}</td></tr>
    </table>
</c:if>
