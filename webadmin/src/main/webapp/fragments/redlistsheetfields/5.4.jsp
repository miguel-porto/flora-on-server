<%@ page pageEncoding="UTF-8" %>
<c:if test="${user.canEDIT_SECTION5()}">
    <select name="usesAndTrade_Overexploitation">
        <c:forEach var="tmp" items="${usesAndTrade_Overexploitation}">
            <c:if test="${rlde.getUsesAndTrade().getOverexploitation().toString().equals(tmp.toString())}">
                <option value="${tmp.toString()}" selected="selected">${tmp.getLabel()}</option>
            </c:if>
            <c:if test="${!rlde.getUsesAndTrade().getOverexploitation().toString().equals(tmp.toString())}">
                <option value="${tmp.toString()}">${tmp.getLabel()}</option>
            </c:if>
        </c:forEach>
    </select>
</c:if>
<c:if test="${!user.canEDIT_SECTION5()}">
    ${rlde.getUsesAndTrade().getOverexploitation().getLabel()}
</c:if>
