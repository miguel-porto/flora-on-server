<%@ page pageEncoding="UTF-8" %>
<c:if test="${user.canEDIT_SECTION3()}">
    <table>
        <tr><td>Category</td><td>
            <t:dropdown name="population_NrMatureIndividualsCategory" values="${population_NrMatureIndividualsCategory}" selectedValue="${rlde.getPopulation().getNrMatureIndividualsCategory()}" trigger="false"/>
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
