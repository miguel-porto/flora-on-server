<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<table>
    <tr><td style="width:auto">
        <c:if test="${user.canVIEW_OCCURRENCES()}">
            <div class="wordtag togglebutton"><a href="?w=taxonrecords&group=500&id=${taxon._getIDURLEncoded()}">clique para ver lista de ocorrências</a></div>
        </c:if>
        <t:editabletext
            privilege="${user.canEDIT_SECTION2() || user.canEDIT_ALL_TEXTUAL()}"
            value="${rlde.getGeographicalDistribution().getDescription()}"
            name="geographicalDistribution_Description"/>
    </td>
    <c:if test="${historicalSquares != null && user.canVIEW_FULL_SHEET()}">
        <td style="width:0; text-align:center;">
        <c:if test="${user.canVIEW_OCCURRENCES()}"><a href="?w=taxonrecords&group=500&id=${taxon._getIDURLEncoded()}"></c:if>
        <t:svgmap
            mapBounds="${redListSettings.getMapBounds()}"
            svgDivisor="${redListSettings.getSvgMapDivisor()}"
            baseMap="${redListSettings.getBaseMapPathString()}"
            squares="${historicalSquares}"
            convexHull="${historicalConvexHull}"
            standAlone="${!user.canVIEW_FULL_SHEET()}"/>
        <c:if test="${user.canVIEW_OCCURRENCES()}"></a></c:if>
        </td>
    </c:if>
        <td style="width:0; text-align:center;">
        <c:if test="${user.canVIEW_OCCURRENCES()}"><a href="?w=taxonrecords&group=500&id=${taxon._getIDURLEncoded()}"></c:if>
        <t:svgmap
            mapBounds="${redListSettings.getMapBounds()}"
            svgDivisor="${redListSettings.getSvgMapDivisor()}"
            baseMap="${redListSettings.getBaseMapPathString()}"
            squares="${currentSquares}"
            convexHull="${user.canVIEW_FULL_SHEET() ? currentConvexHull : null}"
            standAlone="${!user.canVIEW_FULL_SHEET()}"/>
        <c:if test="${user.canVIEW_OCCURRENCES()}"></a></c:if>
        </td>
    </tr>
    <tr><td style="width:auto"></td>
        <c:if test="${historicalSquares != null && user.canVIEW_FULL_SHEET()}"><td style="width:0; text-align:center;"><fmt:message key="DataSheet.label.2.1a"/></td></c:if>
        <td style="width:0; text-align:center;"><fmt:message key="DataSheet.label.2.1b"/></td>
    </tr>
</table>
