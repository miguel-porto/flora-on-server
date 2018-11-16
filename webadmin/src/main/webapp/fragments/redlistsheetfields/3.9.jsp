<%@ page pageEncoding="UTF-8" %>
<c:if test="${user.canEDIT_SECTION3()}">
    <select name="population_PercentMatureOneSubpop">
        <c:forEach var="tmp" items="${population_PercentMatureOneSubpop}">
            <c:if test="${rlde.getPopulation().getPercentMatureOneSubpop().toString().equals(tmp.toString())}">
                <option value="${tmp.toString()}" selected="selected">${tmp.getLabel()}</option>
            </c:if>
            <c:if test="${!rlde.getPopulation().getPercentMatureOneSubpop().toString().equals(tmp.toString())}">
                <option value="${tmp.toString()}">${tmp.getLabel()}</option>
            </c:if>
        </c:forEach>
    </select>
</c:if>
<c:if test="${!user.canEDIT_SECTION3()}">
    ${rlde.getPopulation().getPercentMatureOneSubpop().getLabel()}
</c:if>
