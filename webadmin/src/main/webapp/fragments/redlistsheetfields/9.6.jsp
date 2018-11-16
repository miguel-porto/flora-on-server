<%@ page pageEncoding="UTF-8" %>
<c:if test="${user.canEDIT_9_5_9_6_9_61_9_91() || user.canEDIT_SECTION9()}">
    <div class="multiplechooser left" id="textauthors">
        <input type="hidden" name="assessment_Authors" value=""/>
    <c:forEach var="tmp" items="${authors}">
        <input type="checkbox" name="assessment_Authors" id="aa_${tmp}" value="${tmp}" checked="checked"/>
        <label for="aa_${tmp}" class="wordtag togglebutton">${userMap.get(tmp)}</label>
    </c:forEach>
    </div>
    <div class="withsuggestions">
        <input type="text" class="nochangeevent" name="query" placeholder="type first letters" autocomplete="off" id="authorbox"/>
        <div id="authorsuggestions"></div>
    </div>
    <input type="button" value="Create new..." class="button" id="newauthor"/>
</c:if>
<c:if test="${!user.canEDIT_9_5_9_6_9_61_9_91() && !user.canEDIT_SECTION9()}">
    <c:forEach var="tmp" items="${rlde.getAssessment().getAuthors()}">
    <c:if test="${tmp != null}">
        <div class="wordtag">${userMap.get(tmp)}</div>
    </c:if>
    </c:forEach>
</c:if>
