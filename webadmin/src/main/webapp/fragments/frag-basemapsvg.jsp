<%-- This is the template for SVG distribution maps in squares --%>
<%@ page contentType="image/svg+xml; charset=utf-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ page session="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<t:svgmap
    mapBounds="${mapBounds}"
    svgDivisor="${svgDivisor}"
    baseMap="${baseMap}"
    squares="${squares}"
    showShadow="${showShadow}"
    standAlone="${standAlone}"
    borderWidth="${borderWidth}"
    scaleStroke="${scaleStroke}"
    showOccurrences="${showOccurrences}"
    />