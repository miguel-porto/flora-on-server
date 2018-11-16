<%@ page pageEncoding="UTF-8" %>
<c:if test="${user.canEDIT_9_8_9_93() || user.canEDIT_SECTION9()}">
    <div class="multiplechooser left" id="reviewers">
    <input type="hidden" name="assessment_Reviewer" value=""/>
    <c:forEach var="tmp" items="${reviewer}">
        <input type="checkbox" name="assessment_Reviewer" id="are_${tmp}" value="${tmp}" checked="checked"/>
        <label for="are_${tmp}" class="wordtag togglebutton">${userMap.get(tmp)}</label>
    </c:forEach>
    </div>
    <div class="withsuggestions">
        <input type="text" class="nochangeevent" name="query" placeholder="type first letters" autocomplete="off" id="reviewerbox"/>
        <div id="reviewersuggestions"></div>
    </div>
    <input type="button" value="Create new..." class="button" id="newreviewer"/>
</c:if>
<c:if test="${!user.canEDIT_9_8_9_93() && !user.canEDIT_SECTION9()}">
    <c:forEach var="tmp" items="${rlde.getAssessment().getReviewer()}">
    <c:if test="${tmp != null}">
        <div class="wordtag">${userMap.get(tmp)}</div>
    </c:if>
    </c:forEach>
</c:if>
