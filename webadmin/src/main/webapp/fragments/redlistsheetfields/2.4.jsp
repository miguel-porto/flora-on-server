<%@ page pageEncoding="UTF-8" %>
<c:if test="${user.canEDIT_SECTION2()}">
<table class="triggergroup">
    <tr><td>Category</td><td>
        <t:dropdown name="geographicalDistribution_DeclineDistribution" values="${geographicalDistribution_DeclineDistribution}" selectedValue="${rlde.getGeographicalDistribution().getDeclineDistribution()}" trigger="true"/>
    </td></tr>
    <tr class="triggered ${rlde.getGeographicalDistribution().getDeclineDistribution().isTrigger() ? '' : 'hidden'}"><td>Justification</td><td>
        <t:editabletext
            privilege="${true}"
            value="${rlde.getGeographicalDistribution().getDeclineDistributionJustification()}"
            name="geographicalDistribution_DeclineDistributionJustification"/>
    </td></tr>
</table>
</c:if>
<c:if test="${!user.canEDIT_SECTION2()}">
<table>
    <tr><td class="hide-in-print">Categoria</td><td><b>${rlde.getGeographicalDistribution().getDeclineDistribution().getLabel()}</b></td></tr>
    <tr><td class="hide-in-print">Justificação</td><td>${rlde.getGeographicalDistribution().getDeclineDistributionJustification()}</td></tr>
</table>
</c:if>
