<%@ page pageEncoding="UTF-8" %>
<c:if test="${user.canEDIT_SECTION3()}">
    <table class="triggergroup">
        <tr><td>Type</td><td>
            <select name="population_TypeOfEstimate" class="trigger">
                <c:forEach var="tmp" items="${population_TypeOfEstimate}">
                    <c:if test="${rlde.getPopulation().getTypeOfEstimate().toString().equals(tmp.toString())}">
                        <option value="${tmp.toString()}" selected="selected" data-trigger="${tmp.isTrigger() ? 1 : 0}">${tmp.getLabel()}</option>
                    </c:if>
                    <c:if test="${!rlde.getPopulation().getTypeOfEstimate().toString().equals(tmp.toString())}">
                        <option value="${tmp.toString()}" data-trigger="${tmp.isTrigger() ? 1 : 0}">${tmp.getLabel()}</option>
                    </c:if>
                </c:forEach>
            </select>
        </td></tr>
        <tr class="triggered ${rlde.getPopulation().getTypeOfEstimate().isTrigger() ? '' : 'hidden'}"><td>Description</td><td>
            <div contenteditable="true" class="contenteditable">${rlde.getPopulation().getNrMatureIndividualsDescription()}</div>
            <input type="hidden" name="population_NrMatureIndividualsDescription" value="${fn:escapeXml(rlde.getPopulation().getNrMatureIndividualsDescription())}"/>
        </td></tr>
    </table>
</c:if>
<c:if test="${!user.canEDIT_SECTION3()}">
    <table>
        <tr><td>Type</td><td>${rlde.getPopulation().getTypeOfEstimate().getLabel()}</td></tr>
        <tr><td>Description</td><td>${rlde.getPopulation().getNrMatureIndividualsDescription()}</td></tr>
    </table>
</c:if>
