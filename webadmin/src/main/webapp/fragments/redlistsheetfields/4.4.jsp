<%@ page pageEncoding="UTF-8" %>
<c:if test="${user.canEDIT_SECTION4()}">
    <table class="triggergroup">
        <tr><td>Length</td><td>
            <input name="ecology_GenerationLength" type="text" class="trigger" value="${rlde.getEcology().getGenerationLength()}"/>
            <span class="legend alwaysvisible"><fmt:message key="DataSheet.msg.interval"/></span>
        </td></tr>
        <tr class="triggered ${(rlde.getEcology().getGenerationLength() != null && (rlde.getEcology().getGenerationLength().getMaxValue() != null || rlde.getEcology().getGenerationLength().getMinValue() != null)) ? '' : 'hidden'}"><td>Justification</td><td>
            <div contenteditable="true" class="contenteditable">${rlde.getEcology().getGenerationLengthJustification()}</div>
            <input type="hidden" name="ecology_GenerationLengthJustification" value="${fn:escapeXml(rlde.getEcology().getGenerationLengthJustification())}"/>
        </td></tr>
    </table>
</c:if>
<c:if test="${!user.canEDIT_SECTION4()}">
    <table>
        <tr><td>Length (exact or interval)</td><td>${rlde.getEcology().getGenerationLength()}</td></tr>
        <tr><td>Justification</td><td>${rlde.getEcology().getGenerationLengthJustification()}</td></tr>
    </table>
</c:if>
