<%@ page pageEncoding="UTF-8" %>
<c:if test="${user.canEDIT_SECTION4()}">
    <div id="habitat-tree">
    <t:habitattree taxentid="${rlde.getTaxEntID()}"
        startlevel="1"
        loaduptolevel="5"
        minselectablelevel="2"
        hideafterlevel="1" />
    </div>
</c:if>
<c:if test="${!user.canEDIT_SECTION4()}">
    <ul>
        <c:forEach var="hab" items="${habitatTypes}">
        <li>${hab.getName()}</li>
        </c:forEach>
    </ul>
</c:if>
