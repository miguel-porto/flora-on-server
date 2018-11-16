<%@ page pageEncoding="UTF-8" %>
<c:if test="${user.canEDIT_SECTION7() || user.canEDIT_7_3()}">
    <table class="triggergroup">
        <tr><td>Category</td><td>
            <select name="conservation_ExSituConservation" class="trigger">
                <c:forEach var="tmp" items="${conservation_ExSituConservation}">
                    <c:if test="${rlde.getConservation().getExSituConservation().toString().equals(tmp.toString())}">
                        <option value="${tmp.toString()}" selected="selected" data-trigger="${tmp.isTrigger() ? 1 : 0}">${tmp.getLabel()}</option>
                    </c:if>
                    <c:if test="${!rlde.getConservation().getExSituConservation().toString().equals(tmp.toString())}">
                        <option value="${tmp.toString()}" data-trigger="${tmp.isTrigger() ? 1 : 0}">${tmp.getLabel()}</option>
                    </c:if>
                </c:forEach>
            </select>
        </td></tr>
        <tr class="triggered ${rlde.getConservation().getExSituConservation().isTrigger() ? '' : 'hidden'}"><td>Justification</td><td>
            <div contenteditable="true" class="contenteditable">${rlde.getConservation().getExSituConservationJustification()}</div>
            <input type="hidden" name="conservation_ExSituConservationJustification" value="${fn:escapeXml(rlde.getConservation().getExSituConservationJustification())}"/>
        </td></tr>
    </table>
</c:if>
<c:if test="${!user.canEDIT_SECTION7() && !user.canEDIT_7_3()}">
    <table>
        <tr><td>Category</td><td>${rlde.getConservation().getExSituConservation().getLabel()}</td></tr>
        <tr><td>Justification</td><td>${rlde.getConservation().getExSituConservationJustification()}</td></tr>
    </table>
</c:if>
