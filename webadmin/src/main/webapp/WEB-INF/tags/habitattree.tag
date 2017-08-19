<%@ tag description="Inline habitat tree" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ attribute name="taxentid" required="true" %>
<%@ attribute name="startlevel" required="true" %>
<%@ attribute name="loaduptolevel" required="false" %>
<%@ attribute name="hideafterlevel" required="false" %>
<%@ attribute name="minselectablelevel" required="false" %>
<jsp:include page="/checklist/api/lists">
    <jsp:param name="w" value="tree" />
    <jsp:param name="id" value="" />
    <jsp:param name="type" value="habitat" />
    <jsp:param name="taxent" value="${taxentid}" />
    <jsp:param name="level" value="${startlevel}" />
    <jsp:param name="maxlevel" value="${loaduptolevel}" />
    <jsp:param name="territory" value="${territory}" />
    <jsp:param name="hideafterlevel" value="${hideafterlevel}" />
    <jsp:param name="minselectablelevel" value="${minselectablelevel}" />
</jsp:include>