<%@ page pageEncoding="UTF-8" %>
<c:if test="${user.canEDIT_SECTION4()}">
    <table class="triggergroup">
        <tr><td>Category</td><td>
            <select name="ecology_DeclineHabitatQuality" class="trigger">
                <c:forEach var="tmp" items="${ecology_DeclineHabitatQuality}">
                    <c:if test="${rlde.getEcology().getDeclineHabitatQuality().toString().equals(tmp.toString())}">
                        <option value="${tmp.toString()}" selected="selected" data-trigger="${tmp.isTrigger() ? 1 : 0}">${tmp.getLabel()}</option>
                    </c:if>
                    <c:if test="${!rlde.getEcology().getDeclineHabitatQuality().toString().equals(tmp.toString())}">
                        <option value="${tmp.toString()}" data-trigger="${tmp.isTrigger() ? 1 : 0}">${tmp.getLabel()}</option>
                    </c:if>
                </c:forEach>
            </select>
        </td></tr>
        <tr class="triggered ${rlde.getEcology().getDeclineHabitatQuality().isTrigger() ? '' : 'hidden'}"><td>Justification</td><td>
            <div contenteditable="true" class="contenteditable">${rlde.getEcology().getDeclineHabitatQualityJustification()}</div>
            <input type="hidden" name="ecology_DeclineHabitatQualityJustification" value="${fn:escapeXml(rlde.getEcology().getDeclineHabitatQualityJustification())}"/>
        </td></tr>
    </table>
</c:if>
<c:if test="${!user.canEDIT_SECTION4()}">
    <table>
        <tr><td>Category</td><td>${rlde.getEcology().getDeclineHabitatQuality().getLabel()}</td></tr>
        <tr><td>Justification</td><td>${rlde.getEcology().getDeclineHabitatQualityJustification()}</td></tr>
    </table>
</c:if>
