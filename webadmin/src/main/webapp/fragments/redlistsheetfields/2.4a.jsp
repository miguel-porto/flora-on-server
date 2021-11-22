<%@ page pageEncoding="UTF-8" %>
<c:if test="${user.canEDIT_SECTION2()}">
<table class="triggergroup">
    <tr><td>Category</td><td>
        <t:dropdown name="geographicalDistribution_DeclineDistribution" values="${geographicalDistribution_DeclineDistribution}" selectedValue="${rlde.getGeographicalDistribution().getDeclineDistribution()}" trigger="true"/>
    </td></tr>
    <tr class="triggered ${rlde.getGeographicalDistribution().getDeclineDistribution().isTrigger() ? '' : 'hidden'}"><td>Qualifier</td><td>
    <t:dropdown name="geographicalDistribution_DeclineQualifier" values="${geographicalDistribution_DeclineQualifier}" selectedValue="${rlde.getGeographicalDistribution().getDeclineQualifier()}"/>
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
    <tr><td>Category</td><td>${rlde.getGeographicalDistribution().getDeclineDistribution().getLabel()}</td></tr>
    <tr><td>Qualifier</td><td>${rlde.getGeographicalDistribution().getDeclineQualifier()}</td></tr>
    <tr><td>Justification</td><td>${rlde.getGeographicalDistribution().getDeclineDistributionJustification()}</td></tr>
</table>
</c:if>