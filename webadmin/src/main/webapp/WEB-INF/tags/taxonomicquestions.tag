<%@ tag description="Taxonomic questions" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ attribute name="questions" required="true" type="java.util.Map" %>
<%@ attribute name="individualforms" required="false" %>

<c:set var="language" value="${not empty param.language ? param.language : not empty language ? language : pageContext.response.locale}" scope="request" />
<fmt:setLocale value="${language}" />
<fmt:setBundle basename="pt.floraon.occurrences.occurrencesMessages" />

<ul>
<c:forEach var="question" items="${questions}" varStatus="loop">
    <li style="${individualforms ? '' : 'clear:both'}">
        ${question.getKey()}:
        <c:if test="${individualforms}">
        <form class="poster" data-path="/floraon/occurrences/api/fixtaxonomicissues" data-refresh="true" style="display:inline">
        </c:if>
        <input type="hidden" name="question_${loop.index}_key" value="${question.getKey()}"/>
        <input type="hidden" name="question_${loop.index}_uuids" value="${question.getValue().getOccurrenceUUIDs()}"/>
        <div class="multiplechooser left inlineflex" style="${individualforms ? '' : 'float:right'}">
            <c:if test="${question.getValue().getOptions().size() == 0}">
            <input type="radio" name="question_${loop.index}" value="NA" id="question_${loop.index}_na"/>
            <label for="question_${loop.index}_na" class="wordtag togglebutton"> <fmt:message key="error.10c"/></label>
            </c:if>
            <c:if test="${question.getValue().getOptions().size() > 0}">
            <c:forEach var="options" items="${question.getValue().getOptions()}" varStatus="loop2">
                <c:if test="${loop2.isFirst()}">
                <input type="radio" name="question_${loop.index}" value="${options.getID()}" id="question_${loop.index}_${loop2.index}" checked="checked"/>
                </c:if>
                <c:if test="${!loop2.isFirst()}">
                <input type="radio" name="question_${loop.index}" value="${options.getID()}" id="question_${loop.index}_${loop2.index}"/>
                </c:if>
                <label for="question_${loop.index}_${loop2.index}" class="wordtag togglebutton"> ${options.getFullName(true)}</label>
            </c:forEach>
            </c:if>
            <c:if test="${!individualforms}">
            <c:if test="${question.getValue().getOptions().size() == 0}">
            <input type="radio" name="question_${loop.index}" value="NM" id="question_${loop.index}_nm" checked="checked"/>
            </c:if>
            <c:if test="${question.getValue().getOptions().size() > 0}">
            <input type="radio" name="question_${loop.index}" value="NM" id="question_${loop.index}_nm"/>
            </c:if>
            <label for="question_${loop.index}_nm" class="wordtag togglebutton"> <fmt:message key="error.10b"/></label>
            </c:if>
        </div>
        <c:if test="${individualforms}">
        <input type="submit" class="textbutton" value="<fmt:message key="occurrences.2b"/>"/>
        </form>
        </c:if>
    </li>
</c:forEach>
</ul>
