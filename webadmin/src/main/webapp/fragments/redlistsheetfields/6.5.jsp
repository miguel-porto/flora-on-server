<%@ page pageEncoding="UTF-8" %>
<c:if test="${user.canEDIT_SECTION6()}">
    <table class="triggergroup">
        <tr><td>Category</td><td>
            <select name="threats_ExtremeFluctuationsNrLocations" class="trigger">
                <c:forEach var="tmp" items="${threats_ExtremeFluctuationsNrLocations}">
                    <c:if test="${rlde.getThreats().getExtremeFluctuationsNrLocations().toString().equals(tmp.toString())}">
                        <option value="${tmp.toString()}" selected="selected" data-trigger="${tmp.isTrigger() ? 1 : 0}">${tmp.getLabel()}</option>
                    </c:if>
                    <c:if test="${!rlde.getThreats().getExtremeFluctuationsNrLocations().toString().equals(tmp.toString())}">
                        <option value="${tmp.toString()}" data-trigger="${tmp.isTrigger() ? 1 : 0}">${tmp.getLabel()}</option>
                    </c:if>
                </c:forEach>
            </select>
        </td></tr>
        <tr class="triggered ${rlde.getThreats().getExtremeFluctuationsNrLocations().isTrigger() ? '' : 'hidden'}"><td>Justification</td><td>
            <div contenteditable="true" class="contenteditable">${rlde.getThreats().getExtremeFluctuationsNrLocationsJustification()}</div>
            <input type="hidden" name="threats_ExtremeFluctuationsNrLocationsJustification" value="${fn:escapeXml(rlde.getThreats().getExtremeFluctuationsNrLocationsJustification())}"/>
        </td></tr>
    </table>
</c:if>
<c:if test="${!user.canEDIT_SECTION6()}">
    <table>
        <tr><td>Category</td><td>${rlde.getThreats().getExtremeFluctuationsNrLocations().getLabel()}</td></tr>
        <tr><td>Justification</td><td>${rlde.getThreats().getExtremeFluctuationsNrLocationsJustification()}</td></tr>
    </table>
</c:if>
