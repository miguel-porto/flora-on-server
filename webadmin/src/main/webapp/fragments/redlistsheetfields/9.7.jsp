<%@ page pageEncoding="UTF-8" %>
<c:if test="${user.canEDIT_9_7_9_92() || user.canEDIT_SECTION9()}">
    <div class="multiplechooser left" id="assessors">
    <input type="hidden" name="assessment_Evaluator" value=""/>
    <c:forEach var="tmp" items="${evaluator}">
        <input type="checkbox" name="assessment_Evaluator" id="aas_${tmp}" value="${tmp}" checked="checked"/>
        <label for="aas_${tmp}" class="wordtag togglebutton">${userMap.get(tmp)}</label>
    </c:forEach>
    </div>
    <div class="withsuggestions">
        <input type="text" class="nochangeevent" name="query" placeholder="type first letters" autocomplete="off" id="assessorbox"/>
        <div id="assessorsuggestions"></div>
    </div>
    <input type="button" value="Create new..." class="button" id="newevaluator"/>
</c:if>
<c:if test="${!user.canEDIT_9_7_9_92() && !user.canEDIT_SECTION9()}">
    <c:forEach var="tmp" items="${rlde.getAssessment().getEvaluator()}">
    <c:if test="${tmp != null}">
        <div class="wordtag">${userMap.get(tmp)}</div>
    </c:if>
    </c:forEach>
</c:if>
