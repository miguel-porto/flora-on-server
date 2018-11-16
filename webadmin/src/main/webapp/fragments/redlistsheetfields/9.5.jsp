<%@ page pageEncoding="UTF-8" %>
<table><tr><th>Year published</th><th>Category</th></tr>
<c:if test="${user.canMANAGE_REDLIST_USERS()}">
<c:forEach var="tmp" items="${previousAssessments}">
    <tr>
        <td><input name="assessment_PreviousAssessmentListYear" type="number" min="1900" max="2020" value="${tmp.getYear()}"/></td>
        <td><select name="assessment_PreviousAssessmentListCategory">
            <option value="">(not assigned)</option>
            <c:forEach var="tmp1" items="${assessment_Category}">
                <c:if test="${tmp.getCategory().toString().equals(tmp1.toString())}">
                    <option value="${tmp1.toString()}" selected="selected">${tmp1.getLabel()}</option>
                </c:if>
                <c:if test="${!tmp.getCategory().toString().equals(tmp1.toString())}">
                    <option value="${tmp1.toString()}">${tmp1.getLabel()}</option>
                </c:if>
            </c:forEach>
        </select></td>
    </tr>
</c:forEach>
</c:if>
<c:if test="${!user.canMANAGE_REDLIST_USERS()}">
<c:forEach var="tmp" items="${previousAssessments}">
    <tr><td>${tmp.getYear()}</td><td>${tmp.getCategory().getLabel()}</td></tr>
</c:forEach>
</c:if>
</table>
