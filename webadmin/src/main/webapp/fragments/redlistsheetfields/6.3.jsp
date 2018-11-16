<%@ page pageEncoding="UTF-8" %>
<c:if test="${user.canEDIT_SECTION6()}">
    <table>
        <tr><td>Number</td><td>
            <input type="text" name="threats_NumberOfLocations" value="${rlde.getThreats().getNumberOfLocations()}"/>
            <span class="legend alwaysvisible"><fmt:message key="DataSheet.msg.interval"/></span>
        </td></tr>
        <tr><td>Justification</td><td>
            <div contenteditable="true" class="contenteditable">${rlde.getThreats().getNumberOfLocationsJustification()}</div>
            <input type="hidden" name="threats_NumberOfLocationsJustification" value="${fn:escapeXml(rlde.getThreats().getNumberOfLocationsJustification())}"/>
        </td></tr>
        <tr><td><fmt:message key="DataSheet.label.6.3b"/></td><td>${nclusters} <fmt:message key="DataSheet.label.6.3a"/><div class="legend alwaysvisible"><fmt:message key="DataSheet.help.6.3a" /></div></td></tr>
    </table>
</c:if>
<c:if test="${!user.canEDIT_SECTION6()}">
    <table>
        <tr><td>Number</td><td>${rlde.getThreats().getNumberOfLocations()}</td></tr>
        <tr><td>Justification</td><td>${rlde.getThreats().getNumberOfLocationsJustification()}</td></tr>
        <tr><td><fmt:message key="DataSheet.label.6.3b"/></td><td>${nclusters} <fmt:message key="DataSheet.label.6.3a"/></td></tr>
    </table>
</c:if>
