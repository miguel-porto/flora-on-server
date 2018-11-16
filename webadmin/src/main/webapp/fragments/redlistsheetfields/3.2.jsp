<%@ page pageEncoding="UTF-8" %>
<c:if test="${user.canEDIT_SECTION3()}">
    <table>
        <tr><td>Category</td><td>
            <select name="population_NrMatureIndividualsCategory">
                <c:forEach var="tmp" items="${population_NrMatureIndividualsCategory}">
                    <c:if test="${rlde.getPopulation().getNrMatureIndividualsCategory().toString().equals(tmp.toString())}">
                        <option value="${tmp.toString()}" selected="selected">${tmp.getLabel()}</option>
                    </c:if>
                    <c:if test="${!rlde.getPopulation().getNrMatureIndividualsCategory().toString().equals(tmp.toString())}">
                        <option value="${tmp.toString()}">${tmp.getLabel()}</option>
                    </c:if>
                </c:forEach>
            </select>
        </td></tr>
        <tr><td>Exact number</td><td>
        <input type="text" name="population_NrMatureIndividualsExact" value="${rlde.getPopulation().getNrMatureIndividualsExact()}"/>
        <c:if test="${rlde.getPopulation().getNrMatureIndividualsExact().getError() != null}"><span class="warning">${rlde.getPopulation().getNrMatureIndividualsExact().getError()}</span></c:if>
        <span class="legend alwaysvisible"><fmt:message key="DataSheet.msg.interval"/></span>
        </td></tr>
    </table>
</c:if>
<c:if test="${!user.canEDIT_SECTION3()}">
    <table>
        <tr><td>Category</td><td>${rlde.getPopulation().getNrMatureIndividualsCategory().getLabel()}</td></tr>
        <tr><td>Exact number</td><td>${rlde.getPopulation().getNrMatureIndividualsExact()}</td></tr>
    </table>
</c:if>
