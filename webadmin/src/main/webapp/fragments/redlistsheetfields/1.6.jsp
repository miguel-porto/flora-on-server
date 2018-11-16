<%@ page pageEncoding="UTF-8" %>
<c:if test="${user.canMANAGE_REDLIST_USERS()}">
    <div class="multiplechooser left" id="tagchooser">
        <input type="hidden" name="tags" value=""/>
    <c:forEach var="tmp" items="${allTags}">
        <c:if test="${tags.contains(tmp)}">
            <input type="checkbox" name="tags" value="${tmp}" checked="checked" id="tags_${tmp}"/>
            <label for="tags_${tmp}" class="wordtag togglebutton"> ${tmp}</label>
        </c:if>
        <c:if test="${!tags.contains(tmp)}">
            <input type="checkbox" name="tags" value="${tmp}" id="tags_${tmp}"/>
            <label for="tags_${tmp}" class="wordtag togglebutton"> ${tmp}</label>
        </c:if>
    </c:forEach>
    </div>
    <input type="text" class="nochangeevent" name="query" placeholder="<fmt:message key="DataSheet.msg.newtag" />" autocomplete="off" id="tagbox"/>
    <input type="button" value="Create new..." class="button" id="newtag"/>
</c:if>
<c:if test="${!user.canEDIT_1_4()}">
    <ul><c:forEach var="tmp" items="${tags}"><li><c:out value="${tmp}"></c:out></li></c:forEach></ul>
</c:if>
