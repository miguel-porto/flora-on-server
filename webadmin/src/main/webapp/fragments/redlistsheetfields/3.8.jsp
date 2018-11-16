<%@ page pageEncoding="UTF-8" %>
<c:if test="${user.canEDIT_SECTION3()}">
    <select name="population_NrMatureEachSubpop">
        <c:forEach var="tmp" items="${population_NrMatureEachSubpop}">
            <c:if test="${rlde.getPopulation().getNrMatureEachSubpop().toString().equals(tmp.toString())}">
                <option value="${tmp.toString()}" selected="selected">${tmp.getLabel()}</option>
            </c:if>
            <c:if test="${!rlde.getPopulation().getNrMatureEachSubpop().toString().equals(tmp.toString())}">
                <option value="${tmp.toString()}">${tmp.getLabel()}</option>
            </c:if>
        </c:forEach>
    </select>
</c:if>
<c:if test="${!user.canEDIT_SECTION3()}">
    ${rlde.getPopulation().getNrMatureEachSubpop().getLabel()}
</c:if>
