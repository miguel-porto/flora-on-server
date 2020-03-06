<%-- This is the template for SVG distribution maps in squares --%>
<%@ page contentType="text/csv; charset=utf-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ page session="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<t:svgMapAsCSV squares="${squares}"/>
