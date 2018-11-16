<%@ page pageEncoding="UTF-8" %>
<c:if test="${user.canEDIT_SECTION7()}">
    <table class="triggergroup">
        <tr><td>Category</td><td>
            <select name="conservation_ConservationPlans" class="trigger">
                <c:forEach var="tmp" items="${conservation_ConservationPlans}">
                    <c:if test="${rlde.getConservation().getConservationPlans().toString().equals(tmp.toString())}">
                        <option value="${tmp.toString()}" selected="selected" data-trigger="${tmp.isTrigger() ? 1 : 0}">${tmp.getLabel()}</option>
                    </c:if>
                    <c:if test="${!rlde.getConservation().getConservationPlans().toString().equals(tmp.toString())}">
                        <option value="${tmp.toString()}" data-trigger="${tmp.isTrigger() ? 1 : 0}">${tmp.getLabel()}</option>
                    </c:if>
                </c:forEach>
            </select>
        </td></tr>
        <tr class="triggered ${rlde.getConservation().getConservationPlans().isTrigger() ? '' : 'hidden'}"><td>Justification</td><td>
            <div contenteditable="true" class="contenteditable">${rlde.getConservation().getConservationPlansJustification()}</div>
            <input type="hidden" name="conservation_ConservationPlansJustification" value="${fn:escapeXml(rlde.getConservation().getConservationPlansJustification())}"/>
        </td></tr>
    </table>
</c:if>
<c:if test="${!user.canEDIT_SECTION7()}">
    <table>
        <tr><td>Category</td><td>${rlde.getConservation().getConservationPlans().getLabel()}</td></tr>
        <tr><td>Justification</td><td>${rlde.getConservation().getConservationPlansJustification()}</td></tr>
    </table>
</c:if>
