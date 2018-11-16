<%@ page pageEncoding="UTF-8" %>
<c:if test="${user.canEDIT_SECTION3()}">
    <table class="triggergroup">
        <tr><td>Category</td><td>
            <select name="population_ExtremeFluctuations" class="trigger">
                <c:forEach var="tmp" items="${population_ExtremeFluctuations}">
                    <c:if test="${rlde.getPopulation().getExtremeFluctuations().toString().equals(tmp.toString())}">
                        <option value="${tmp.toString()}" selected="selected" data-trigger="${tmp.isTrigger() ? 1 : 0}">${tmp.getLabel()}</option>
                    </c:if>
                    <c:if test="${!rlde.getPopulation().getExtremeFluctuations().toString().equals(tmp.toString())}">
                        <option value="${tmp.toString()}" data-trigger="${tmp.isTrigger() ? 1 : 0}">${tmp.getLabel()}</option>
                    </c:if>
                </c:forEach>
            </select>
        </td></tr>
        <tr class="triggered ${rlde.getPopulation().getExtremeFluctuations().isTrigger() ? '' : 'hidden'}"><td>Justification</td><td>
            <div contenteditable="true" class="contenteditable">${rlde.getPopulation().getExtremeFluctuationsJustification()}</div>
            <input type="hidden" name="population_ExtremeFluctuationsJustification" value="${fn:escapeXml(rlde.getPopulation().getExtremeFluctuationsJustification())}"/>
        </td></tr>
    </table>
</c:if>
<c:if test="${!user.canEDIT_SECTION3()}">
    <table>
        <tr><td>Category</td><td>${rlde.getPopulation().getExtremeFluctuations().getLabel()}</td></tr>
        <tr><td>Justification</td><td>${rlde.getPopulation().getExtremeFluctuationsJustification()}</td></tr>
    </table>
</c:if>
