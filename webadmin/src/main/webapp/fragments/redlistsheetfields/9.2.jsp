<%@ page pageEncoding="UTF-8" %>
<c:if test="${user.canEDIT_9_1_2_3_4() || user.canEDIT_SECTION9()}">
    <input type="hidden" name="assessment_Criteria" value=""/>
    <table class="subtable">
        <thead><tr><th>Criteria</th><th>Subcriteria</th></th></thead>
        <tbody>
            <c:forEach var="cri" items="${assessment_Criteria.entrySet()}">
                <tr><td class="title">${cri.getKey()}</td>
                <td>
                <div class="multiplechooser left compact">
                <c:forEach var="sub" items="${cri.getValue()}">
                    <c:if test="${selcriteria.contains(sub)}">
                        <input type="checkbox" name="assessment_Criteria" id="acri_${sub}" value="${sub}" checked="checked"/>
                    </c:if>
                    <c:if test="${!selcriteria.contains(sub)}">
                        <input type="checkbox" name="assessment_Criteria" id="acri_${sub}" value="${sub}"/>
                    </c:if>
                    <label for="acri_${sub}" class="wordtag togglebutton notransform">${sub.getLabel()}</label>
                    <c:if test="${sub.isBreak()}"><label class="line-break"></label></c:if>
                </c:forEach>
                </div>
                </td>
                </tr>
            </c:forEach>
        </tbody>
    </table>
    <!--<input name="assessment_Criteria" type="text" class="longbox" value="${rlde.getAssessment().getCriteria()}"/>-->
</c:if>
<c:if test="${!user.canEDIT_9_1_2_3_4() && !user.canEDIT_SECTION9()}">
    <p><b>${rlde.getAssessment()._getCriteriaAsString()}</b></p>
</c:if>
