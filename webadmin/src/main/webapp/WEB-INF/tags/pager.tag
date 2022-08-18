<%@ tag description="Pager buttons" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>

<div class="pager">
    <t:option-radiobutton optionprefix="view" optionnames="${pagerOptions}" defaultvalue="250" persistent="true"/>
    <c:if test="${occperpage < 10000000}">
    <div style="display:inline-block; vertical-align:middle">showing ${page != null ? ((page - 1) * occperpage + 1) : 1} to ${page != null ? (page * occperpage) : occperpage}</div>
    <c:if test="${page != null && page > 2}"><a class="imagebutton" href="?w=${param.w}&p=1"><img src="images/start.png" /></a></c:if>
    <c:if test="${page != null && page > 1}"><a class="imagebutton" href="?w=${param.w}&p=${page - 1}"><img src="images/left.png" /></a></c:if>
    <a class="imagebutton" href="?w=${param.w}&p=${(page == null ? 1 : page) + 1}"><img src="images/right.png" /></a>
    </c:if>
</div>
