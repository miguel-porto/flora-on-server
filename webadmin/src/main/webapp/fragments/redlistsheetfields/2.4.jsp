<%@ page pageEncoding="UTF-8" %>
<c:if test="${user.canEDIT_SECTION2()}">
<table class="triggergroup">
    <tr><td>Category</td><td>
        <select name="geographicalDistribution_DeclineDistribution" class="trigger">
            <c:forEach var="tmp" items="${geographicalDistribution_DeclineDistribution}">
                <c:if test="${rlde.getGeographicalDistribution().getDeclineDistribution().toString().equals(tmp.toString())}">
                    <option value="${tmp.toString()}" selected="selected" data-trigger="${tmp.isTrigger() ? 1 : 0}">${tmp.getLabel()}</option>
                </c:if>
                <c:if test="${!rlde.getGeographicalDistribution().getDeclineDistribution().toString().equals(tmp.toString())}">
                    <option value="${tmp.toString()}" data-trigger="${tmp.isTrigger() ? 1 : 0}">${tmp.getLabel()}</option>
                </c:if>
            </c:forEach>
        </select>
    </td></tr>
    <tr class="triggered ${rlde.getGeographicalDistribution().getDeclineDistribution().isTrigger() ? '' : 'hidden'}"><td>Justification</td><td>
        <div contenteditable="true" class="contenteditable">${rlde.getGeographicalDistribution().getDeclineDistributionJustification()}</div>
        <input type="hidden" name="geographicalDistribution_DeclineDistributionJustification" value="${fn:escapeXml(rlde.getGeographicalDistribution().getDeclineDistributionJustification())}"/>
    </td></tr>
</table>
</c:if>
<c:if test="${!user.canEDIT_SECTION2()}">
<table>
    <tr><td>Category</td><td>${rlde.getGeographicalDistribution().getDeclineDistribution().getLabel()}</td></tr>
    <tr><td>Justification</td><td>${rlde.getGeographicalDistribution().getDeclineDistributionJustification()}</td></tr>
</table>
</c:if>
