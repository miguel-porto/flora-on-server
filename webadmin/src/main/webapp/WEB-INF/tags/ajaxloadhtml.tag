<%@ tag description="AJAX HTML loader" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ attribute name="url" required="true" %>
<%@ attribute name="width" required="false" %>
<%@ attribute name="height" required="false" %>
<%@ attribute name="text" required="false" %>
<%@ attribute name="classes" required="false" %>
<%@ attribute name="id" required="false" %>

<c:if test="${width == null}">
<div class="ajaxcontent ${classes}" data-url="${url}" id="${id}"><img src="/images/loader.svg" class="ajaxloader"/></div>
</c:if>

<c:if test="${width != null}">
<div class="ajaxcontent ${classes}" style="position:relative; width:${width}; height:${height};display:inline-block" data-url="${url}" <c:if test="${id != null}">id="${id}"</c:if> >
    <img style="position:absolute; width:100%; height:100%; left:0; top:0" src="/images/loader.svg" class="ajaxloader"/>
    <p>${text}</p>
</div>
</c:if>