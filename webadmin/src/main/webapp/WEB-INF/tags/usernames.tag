<%@ tag description="Username mapper from IDs" pageEncoding="UTF-8" %><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ attribute name="idarray" required="false" type="java.lang.String[]" %>
<%@ attribute name="id" required="false" type="java.lang.String" %>
<%@ attribute name="usermap" required="false" type="java.util.Map" %>
<%@ attribute name="separator" required="false" %>
<%@ attribute name="showAsTags" required="false" %>
<c:if test="${idarray != null}"><c:if test="${separator == null}"><c:set var="separator" value=", " /></c:if><c:forEach var="id" items="${idarray}" varStatus="loop"><c:if test="${showAsTags != null && showAsTags}"><span class="wordtag compact"></c:if>${usermap == null ? id : usermap.get(id)}<c:if test="${showAsTags != null && showAsTags}"></span></c:if><c:if test="${!loop.last}">${separator}</c:if></c:forEach></c:if>
<c:if test="${id != null}">${usermap == null ? id : usermap.get(id)}</c:if>