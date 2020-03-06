<%@ tag description="SVG map as CSV table" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ attribute name="squares" required="true" type="pt.floraon.geometry.gridmaps.GridMap" %>

MGRS,WKT,MinX,MinY,Taxa,NrTaxa
<c:forEach var="sqentry" items="${squares.iterator()}">
<c:set var="sqcoords" value="${sqentry.key.getSquare()}"/>
${sqentry.key.getMGRS()},"${sqentry.key.toWKT()}",${sqcoords.getCenterX()},${sqcoords.getCenterY()},"${sqentry.value.getText('+')}",${sqentry.value.getNumber()}
</c:forEach>
