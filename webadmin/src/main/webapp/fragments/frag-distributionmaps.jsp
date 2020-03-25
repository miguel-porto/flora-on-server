<%@ page pageEncoding="UTF-8" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page session="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<fmt:setBundle basename="pt.floraon.redlistdata.fieldValues" />
<c:choose>
<c:when test="${param.maps=='alvo'}">
<div id="allmapholder">
<c:forEach var="taxon" items="${allTaxa}">
    <div>
    <div class="header">${taxon.getName()}</div>
    <t:ajaxloadhtml url="../api/svgmap?basemap=1&size=10000&border=1&shadow=0&pa=0&sa=0&taxon=${taxon._getIDURLEncoded()}" width="200px" height="100px"/>
    <%-- <t:ajaxloadhtml url="http://localhost:8080/api/svgmap?basemap=1&size=10000&border=1&shadow=0&taxon=${taxon._getIDURLEncoded()}" width="200px" height="100px"/> --%>
    </div>
</c:forEach>
</div>
</c:when>

<c:when test="${param.maps=='threats'}">
<h1>Distribuição das plantas ameaçadas</h1>
<p>Coloque o rato numa quadrícula para ver as plantas aí existentes, com a respectiva categoria de ameaça.</p>
<div class="button textbutton"><a href="?w=allmaps&maps=threats&refreshmaps=1">Refresh maps</a></div>
<div id="allmapholder" class="big interactive">
    <%-- <c:set var="refresh" value="${user.isGuest() ? '' : '&refresh=1'}"/> --%>
    <c:set var="refresh" value="${empty param.refreshmaps ? '' : '&refresh=1'}"/>
    <div>
        <h3>Ameaçadas</h3>
        <t:ajaxloadhtml url="../api/svgmap?basemap=1&size=10000&border=2&shadow=0&pa=0&sa=0&category=threatened${refresh}" width="400px" height="200px"/><br/>
        <c:if test="${empty param.refreshmaps}">
        <a href="../api/svgmap?basemap=1&size=10000&border=2&shadow=0&pa=0&sa=0&category=threatened&download=1&fmt=csv">download CSV</a> |
        <a href="../api/svgmap?basemap=1&size=10000&border=0.05&shadow=0&pa=0&sa=0&category=threatened&download=1">download SVG</a>
        </c:if>
    </div>
    <div>
        <h3>Potencialmente extintas</h3>
        <t:ajaxloadhtml url="../api/svgmap?basemap=1&size=10000&border=2&shadow=0&pa=0&sa=0&category=maybeextinct${refresh}" width="400px" height="200px"/><br/>
        <c:if test="${empty param.refreshmaps}"><a href="../api/svgmap?basemap=1&size=10000&border=0.05&shadow=0&pa=0&sa=0&category=maybeextinct&download=1">download SVG</a></c:if>
    </div>
    <div>
        <h3>Criticamente Em Perigo (CR)</h3>
        <t:ajaxloadhtml url="../api/svgmap?basemap=1&size=10000&border=2&shadow=0&pa=0&sa=0&category=CR${refresh}" width="400px" height="200px"/><br/>
        <c:if test="${empty param.refreshmaps}">
        <a href="../api/svgmap?basemap=1&size=10000&border=2&shadow=0&pa=0&sa=0&category=CR&download=1&fmt=csv">download CSV</a> |
        <a href="../api/svgmap?basemap=1&size=10000&border=0.05&shadow=0&pa=0&sa=0&category=CR&download=1">download SVG</a>
        </c:if>
    </div>
    <div>
        <h3>Em Perigo (EN)</h3>
        <t:ajaxloadhtml url="../api/svgmap?basemap=1&size=10000&border=2&shadow=0&pa=0&sa=0&category=EN${refresh}" width="400px" height="200px"/><br/>
        <c:if test="${empty param.refreshmaps}"><a href="../api/svgmap?basemap=1&size=10000&border=0.05&shadow=0&pa=0&sa=0&category=EN&download=1">download SVG</a></c:if>
    </div>
    <div>
        <h3>Vulnerável (VU)</h3>
        <t:ajaxloadhtml url="../api/svgmap?basemap=1&size=10000&border=2&shadow=0&pa=0&sa=0&category=VU${refresh}" width="400px" height="200px"/><br/>
        <c:if test="${empty param.refreshmaps}"><a href="../api/svgmap?basemap=1&size=10000&border=0.05&shadow=0&pa=0&sa=0&category=VU&download=1">download SVG</a></c:if>
    </div>
    <div>
        <h3>Olivais tradicionais de sequeiro</h3>
        <t:ajaxloadhtml url="../api/svgmap?basemap=1&size=10000&border=2&shadow=0&pa=0&sa=0&category=olivais${refresh}" width="400px" height="200px"/>
    </div>
</div>
</c:when>

<c:when test="${param.maps=='threatTypes'}">
<h1>Distribuição das plantas por tipo de ameaça</h1>
<p>Coloque o rato numa quadrícula para ver as plantas aí existentes, com a respectiva categoria de ameaça.</p>
<div id="allmapholder" class="big interactive">
    <c:set var="refresh" value="${user.isGuest() ? '' : '&refresh=1'}"/>
    <div>
        <h3>Agricultura</h3>
        <t:ajaxloadhtml url="${contextPath}/api/svgmap?basemap=1&size=10000&border=2&shadow=0&pa=0&sa=0&threatType=J${refresh}" width="400px" height="200px"/>
        <a href="${contextPath}/api/svgmap?basemap=1&size=10000&border=2&shadow=0&pa=0&sa=0&threatType=J&download=1">download</a>
    </div>
    <div>
        <h3>Climáticas</h3>
        <t:ajaxloadhtml url="${contextPath}/api/svgmap?basemap=1&size=10000&border=2&shadow=0&pa=0&sa=0&threatType=A${refresh}" width="400px" height="200px"/>
        <a href="${contextPath}/api/svgmap?basemap=1&size=10000&border=2&shadow=0&pa=0&sa=0&threatType=A&download=1">download</a>
    </div>
    <div>
        <h3>Espécies exóticas</h3>
        <t:ajaxloadhtml url="${contextPath}/api/svgmap?basemap=1&size=10000&border=2&shadow=0&pa=0&sa=0&threatType=H${refresh}" width="400px" height="200px"/>
        <a href="${contextPath}/api/svgmap?basemap=1&size=10000&border=2&shadow=0&pa=0&sa=0&threatType=H&download=1">download</a>
    </div>
    <div>
        <h3>Construção</h3>
        <t:ajaxloadhtml url="${contextPath}/api/svgmap?basemap=1&size=10000&border=2&shadow=0&pa=0&sa=0&threatType=P${refresh}" width="400px" height="200px"/>
        <a href="${contextPath}/api/svgmap?basemap=1&size=10000&border=2&shadow=0&pa=0&sa=0&threatType=P&download=1">download</a>
    </div>
    <div>
        <h3>Dinâmicas</h3>
        <t:ajaxloadhtml url="${contextPath}/api/svgmap?basemap=1&size=10000&border=2&shadow=0&pa=0&sa=0&threatType=C${refresh}" width="400px" height="200px"/>
        <a href="${contextPath}/api/svgmap?basemap=1&size=10000&border=2&shadow=0&pa=0&sa=0&threatType=C&download=1">download</a>
    </div>
    <div>
        <h3>Hídricas</h3>
        <t:ajaxloadhtml url="${contextPath}/api/svgmap?basemap=1&size=10000&border=2&shadow=0&pa=0&sa=0&threatType=G${refresh}" width="400px" height="200px"/>
        <a href="${contextPath}/api/svgmap?basemap=1&size=10000&border=2&shadow=0&pa=0&sa=0&threatType=G&download=1">download</a>
    </div>
</div>
</c:when>

<c:otherwise>
<h1>Maps</h1>
<div class="outer">
    <div class="bigbutton section2">
        <h1><a href="?w=allmaps&maps=alvo">All Lista Alvo</a></h1>
    </div>
    <div class="bigbutton section3">
        <h1><a href="?w=allmaps&maps=threats">By threat category</a></h1>
    </div>
    <div class="bigbutton section4">
        <h1><a href="?w=allmaps&maps=threatTypes">By threat type</a></h1>
    </div>
</div>
</c:otherwise>

</c:choose>
</div>
