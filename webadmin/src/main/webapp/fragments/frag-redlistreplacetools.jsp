<%@ page pageEncoding="UTF-8" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page session="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<fmt:setBundle basename="pt.floraon.redlistdata.fieldValues" />
<h1><fmt:message key="Separator.13"/></h1>
<h2>Testar substituição automática</h2>
<p>Teste aqui a substituição automática de termos em todos os campos de texto, a partir de uma tabela.</p>
<form class="poster" data-path="api/replacefromtable" method="post" enctype="multipart/form-data" data-refresh="true">
    <input type="hidden" name="territory" value="${territory}"/>
    <input type="hidden" name="dry" value="true"/>
    <input type="file" name="replaceTable" />
    <input type="submit" class="textbutton" value="Test"/>
</form>
<form class="poster" data-path="api/replacefromtable" method="post" enctype="multipart/form-data" data-refresh="true" data-confirm="true">
    <input type="hidden" name="territory" value="${territory}"/>
    <input type="hidden" name="dry" value="false"/>
    <input type="file" name="replaceTable" />
    <input type="submit" class="textbutton" value="<fmt:message key='Update.2'/>"/>
</form>
<c:forEach var="job" items="${jobs}">
<c:if test="${job.isReady()}">
    <h3>Teste submetido em ${job.getDateSubmitted()}</h3>
    <h4>Expressões que irão ser substituídas</h4>
    ${job.getJob().getResults()}
<%--    <ul>
        <c:forEach var="line" items="${job.getJob().getResults()}">
        <li>${line}</li>
        </c:forEach>
    </ul> --%>
</c:if>
</c:forEach>
