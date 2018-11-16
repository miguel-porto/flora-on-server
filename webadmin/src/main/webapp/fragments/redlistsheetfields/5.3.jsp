<%@ page pageEncoding="UTF-8" %>
<c:if test="${user.canEDIT_SECTION5()}">
    <c:if test="${rlde.getUsesAndTrade().isTraded()}">
        <label><input type="checkbox" name="usesAndTrade_Traded" checked="checked"/> <fmt:message key="DataSheet.label.5.3a" /></label>
    </c:if>
    <c:if test="${!rlde.getUsesAndTrade().isTraded()}">
        <label><input type="checkbox" name="usesAndTrade_Traded"/> <fmt:message key="DataSheet.label.5.3a" /></label>
    </c:if>
</c:if>
<c:if test="${!user.canEDIT_SECTION5()}">
    <fmt:message key="DataSheet.label.5.3a" />: ${rlde.getUsesAndTrade().isTraded() ? "Yes" : "No"}
</c:if>
