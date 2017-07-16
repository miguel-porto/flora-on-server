<%@ tag description="Pager buttons" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>

<div class="pager">
    <t:optionbutton optionname="view250" title="250" defaultvalue="true" />
    <t:optionbutton optionname="view1000" title="1000" defaultvalue="false" />
    <t:optionbutton optionname="view5000" title="5000" defaultvalue="false" />
    <t:optionbutton optionname="viewall" title="all" defaultvalue="false" />
    <c:if test="${occperpage < 10000000}">
    <div style="display:inline-block; vertical-align:middle">showing ${page != null ? ((page - 1) * occperpage + 1) : 1} to ${page != null ? (page * occperpage) : occperpage}</div>
    <c:if test="${page != null && page > 2}"><a class="imagebutton" href="?w=${param.w}&flavour=${param.flavour}&p=1"><img src="images/start.png" /></a></c:if>
    <c:if test="${page != null && page > 1}"><a class="imagebutton" href="?w=${param.w}&flavour=${param.flavour}&p=${page - 1}"><img src="images/left.png" /></a></c:if>
    <a class="imagebutton" href="?w=${param.w}&flavour=${param.flavour}&p=${(page == null ? 1 : page) + 1}"><img src="images/right.png" /></a>
    </c:if>
</div>
