<%@ tag description="Inline SVG map" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ attribute name="mapBounds" required="true" type="pt.floraon.driver.datatypes.Rectangle" %>
<%@ attribute name="svgDivisor" required="false" type="java.lang.Integer" %>
<%@ attribute name="baseMap" required="false" type="java.util.List<String>" %>
<%@ attribute name="squares" required="true" type="pt.floraon.geometry.gridmaps.GridMap" %>
<%@ attribute name="protectedAreas" required="false" type="java.util.Iterator" %>  <%-- latlong --%>
<%@ attribute name="showShadow" required="false" type="java.lang.Boolean" %>
<%@ attribute name="standAlone" required="false" type="java.lang.Boolean" %>
<%@ attribute name="borderWidth" required="false" type="java.lang.Integer" %>
<%@ attribute name="showOccurrences" required="false" type="java.lang.Boolean" %>
<%@ attribute name="convexHull" required="false" type="pt.floraon.geometry.Polygon" %>  <%-- UTM --%>
<c:set var="svgDivisor" value="${(empty svgDivisor) ? 1 : svgDivisor}" scope="page"/>
<c:set var="showShadow" value="${(empty showShadow) ? true : showShadow}" scope="page"/>
<c:set var="showOccurrences" value="${(empty showOccurrences) ? true : showOccurrences}" scope="page"/>
<c:set var="standAlone" value="${(empty standAlone) ? true : standAlone}" scope="page"/>
<c:set var="borderWidth" value="${(empty borderWidth) ? 1 : borderWidth}" scope="page"/>
<svg class="svgmap" xmlns="http://www.w3.org/2000/svg" xmlns:lvf="http://flora-on.pt" preserveAspectRatio="xMidYMin meet"
    viewBox="${mapBounds.toString(svgDivisor)}">
    <c:if test="${showShadow}">
    <defs>
        <filter id="dropshadow" height="130%">
            <feGaussianBlur in="SourceAlpha" stdDeviation="${10000 / svgDivisor}"></feGaussianBlur> <!-- stdDeviation is how much to blur -->
            <feOffset dx="0" dy="0" result="offsetblur"></feOffset> <!-- how much to offset -->
            <feMerge>
                <feMergeNode></feMergeNode> <!-- this contains the offset blurred image -->
                <feMergeNode in="SourceGraphic"></feMergeNode> <!-- this contains the element that the filter is applied to -->
            </feMerge>
        </filter>
    </defs>
    <c:set var="shadow" value="filter:url(#dropshadow);"/>
    </c:if>
    <g transform="translate(0,${(mapBounds.getBottom() + mapBounds.getTop() - 10000) / svgDivisor}) scale(1,-1)">
        <c:if test="${baseMap != null}">
            <c:forEach var="poly" items="${baseMap}">
            <path style="${shadow}" class="portugal" d="${poly}"></path>
            </c:forEach>
        </c:if>
        <c:if test="${protectedAreas != null}">
            <c:if test="${standAlone}">
            <c:forEach var="paentry" items="${protectedAreas}"><path class="protectedarea" style="fill: rgba(139, 195, 74, 0.5); stroke-width:0px;" d="${paentry.value.toSVGPathStringUTM(svgDivisor)}"></path></c:forEach>
            </c:if>
            <c:if test="${!standAlone}">
            <c:forEach var="paentry" items="${protectedAreas}"><path class="protectedarea" d="${paentry.value.toSVGPathStringUTM(svgDivisor)}"></path></c:forEach>
            </c:if>
        </c:if>
        <c:if test="${convexHull != null}"><path class="convexhull" vector-effect="non-scaling-stroke" d="${convexHull.toSVGPathString(svgDivisor)}"></path></c:if>
        <c:if test="${showOccurrences}">
            <c:if test="${standAlone || squares.isColored()}">
            <c:forEach var="sqentry" items="${squares.iterator()}">
                <c:set var="sqcoords" value="${sqentry.key.getSquare()}"/>
                <c:set var="color" value="${squares.isColored() ? sqentry.key.getColor() : '#f55145'}"/>
                <rect class="utmsquare" style="fill:${color}; stroke:white; stroke-width:${borderWidth}px"
                    vector-effect="non-scaling-stroke" lvf:quad="${sqentry.key.getMGRS()}" x="${sqcoords.getMinX() / svgDivisor}"
                    y="${sqcoords.getMinY() / svgDivisor}" width="${sqcoords.getWidth() / svgDivisor}" height="${sqcoords.getHeight() / svgDivisor}">
                    <title>${sqentry.value.getText()}</title>
                </rect>
            </c:forEach>
            </c:if>
            <c:if test="${!standAlone && !squares.isColored()}">
            <c:forEach var="sqentry" items="${squares.iterator()}">
                <c:set var="sqcoords" value="${sqentry.key.getSquare()}"/>
                <rect class="utmsquare" vector-effect="non-scaling-stroke" lvf:quad="${sqentry.key.getMGRS()}" x="${sqcoords.getMinX() / svgDivisor}" y="${sqcoords.getMinY() / svgDivisor}"
                    width="${sqcoords.getWidth() / svgDivisor}" height="${sqcoords.getHeight() / svgDivisor}"></rect>
            </c:forEach>
            </c:if>
        </c:if>
    </g>
</svg>