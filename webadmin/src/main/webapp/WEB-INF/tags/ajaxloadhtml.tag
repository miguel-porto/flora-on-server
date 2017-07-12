<%@ tag description="AJAX HTML loader" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ attribute name="url" required="true" %>
<%@ attribute name="width" required="false" %>
<%@ attribute name="height" required="false" %>
<%@ attribute name="text" required="false" %>

<c:if test="${width == null}">
<div class="ajaxcontent" data-url="${url}"><img src="/floraon/images/loader.svg" class="ajaxloader"/></div>
</c:if>

<c:if test="${width != null}">
<div class="ajaxcontent" style="position:relative; width:${width}; height:${height}" data-url="${url}">
    <img style="position:absolute; width:100%; height:100%; left:0; top:0" src="/floraon/images/loader.svg" class="ajaxloader"/>
    <p>${text}</p>
</div>
</c:if>