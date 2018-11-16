<%@ page pageEncoding="UTF-8" %>
<c:if test="${user.canEDIT_SECTION2()}">
    <select name="geographicalDistribution_ExtremeFluctuations">
        <c:forEach var="tmp" items="${geographicalDistribution_ExtremeFluctuations}">
            <c:if test="${rlde.getGeographicalDistribution().getExtremeFluctuations().toString().equals(tmp.toString())}">
                <option value="${tmp.toString()}" selected="selected">${tmp.getLabel()}</option>
            </c:if>
            <c:if test="${!rlde.getGeographicalDistribution().getExtremeFluctuations().toString().equals(tmp.toString())}">
                <option value="${tmp.toString()}">${tmp.getLabel()}</option>
            </c:if>
        </c:forEach>
    </select>
</c:if>
<c:if test="${!user.canEDIT_SECTION2()}">
    ${rlde.getGeographicalDistribution().getExtremeFluctuations().getLabel()}
</c:if>
