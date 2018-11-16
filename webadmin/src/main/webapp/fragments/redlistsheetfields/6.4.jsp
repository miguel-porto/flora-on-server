<%@ page pageEncoding="UTF-8" %>
<c:if test="${user.canEDIT_SECTION6()}">
    <table class="triggergroup">
        <tr><td>Category</td><td>
            <select name="threats_DeclineNrLocations" class="trigger">
                <c:forEach var="tmp" items="${threats_DeclineNrLocations}">
                    <c:if test="${rlde.getThreats().getDeclineNrLocations().toString().equals(tmp.toString())}">
                        <option value="${tmp.toString()}" selected="selected" data-trigger="${tmp.isTrigger() ? 1 : 0}"><fmt:message key="${tmp.getLabel()}"/></option>
                    </c:if>
                    <c:if test="${!rlde.getThreats().getDeclineNrLocations().toString().equals(tmp.toString())}">
                        <option value="${tmp.toString()}" data-trigger="${tmp.isTrigger() ? 1 : 0}"><fmt:message key="${tmp.getLabel()}"/></option>
                    </c:if>
                </c:forEach>
            </select>
        </td></tr>
        <tr class="triggered ${rlde.getThreats().getDeclineNrLocations().isTrigger() ? '' : 'hidden'}"><td>Justification</td><td>
            <div contenteditable="true" class="contenteditable">${rlde.getThreats().getDeclineNrLocationsJustification()}</div>
            <input type="hidden" name="threats_DeclineNrLocationsJustification" value="${fn:escapeXml(rlde.getThreats().getDeclineNrLocationsJustification())}"/>
        </td></tr>
    </table>
</c:if>
<c:if test="${!user.canEDIT_SECTION6()}">
    <table>
        <tr><td>Category</td><td><fmt:message key="${rlde.getThreats().getDeclineNrLocations().getLabel()}"/></td></tr>
        <tr><td>Justification</td><td>${rlde.getThreats().getDeclineNrLocationsJustification()}</td></tr>
    </table>
</c:if>
