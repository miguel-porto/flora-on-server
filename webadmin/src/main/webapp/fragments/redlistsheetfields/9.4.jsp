<%@ page pageEncoding="UTF-8" %>
<table class="subtable">
    <tr>
        <td class="title">9.4.1</td>
        <td><fmt:message key="DataSheet.label.9.4.1" /></td>
        <td>
        <c:if test="${user.canEDIT_9_1_2_3_4() || user.canEDIT_SECTION9()}">
            <select name="assessment_PropaguleImmigration">
                <c:forEach var="tmp" items="${assessment_RegionalAssessment}">
                    <c:if test="${rlde.getAssessment().getPropaguleImmigration().toString().equals(tmp.toString())}">
                        <option value="${tmp.toString()}" selected="selected">${tmp.getLabel()}</option>
                    </c:if>
                    <c:if test="${!rlde.getAssessment().getPropaguleImmigration().toString().equals(tmp.toString())}">
                        <option value="${tmp.toString()}">${tmp.getLabel()}</option>
                    </c:if>
                </c:forEach>
            </select>
        </c:if>
        <c:if test="${!user.canEDIT_9_1_2_3_4() && !user.canEDIT_SECTION9()}">
            ${rlde.getAssessment().getPropaguleImmigration().getLabel()}
        </c:if>
        </td>
    </tr>
    <tr>
        <td class="title">9.4.2</td>
        <td><fmt:message key="DataSheet.label.9.4.2" /></td>
        <td>
        <c:if test="${user.canEDIT_9_1_2_3_4() || user.canEDIT_SECTION9()}">
            <select name="assessment_DecreaseImmigration">
                <c:forEach var="tmp" items="${assessment_RegionalAssessment}">
                    <c:if test="${rlde.getAssessment().getDecreaseImmigration().toString().equals(tmp.toString())}">
                        <option value="${tmp.toString()}" selected="selected">${tmp.getLabel()}</option>
                    </c:if>
                    <c:if test="${!rlde.getAssessment().getDecreaseImmigration().toString().equals(tmp.toString())}">
                        <option value="${tmp.toString()}">${tmp.getLabel()}</option>
                    </c:if>
                </c:forEach>
            </select>
        </c:if>
        <c:if test="${!user.canEDIT_9_1_2_3_4() && !user.canEDIT_SECTION9()}">
            ${rlde.getAssessment().getDecreaseImmigration().getLabel()}
        </c:if>
        </td>
    </tr>
    <tr>
        <td class="title">9.4.3</td>
        <td><fmt:message key="DataSheet.label.9.4.3" /></td>
        <td>
        <c:if test="${user.canEDIT_9_1_2_3_4() || user.canEDIT_SECTION9()}">
            <select name="assessment_IsSink">
                <c:forEach var="tmp" items="${assessment_RegionalAssessment}">
                    <c:if test="${rlde.getAssessment().getIsSink().toString().equals(tmp.toString())}">
                        <option value="${tmp.toString()}" selected="selected">${tmp.getLabel()}</option>
                    </c:if>
                    <c:if test="${!rlde.getAssessment().getIsSink().toString().equals(tmp.toString())}">
                        <option value="${tmp.toString()}">${tmp.getLabel()}</option>
                    </c:if>
                </c:forEach>
            </select>
        </c:if>
        <c:if test="${!user.canEDIT_9_1_2_3_4() && !user.canEDIT_SECTION9()}">
            ${rlde.getAssessment().getIsSink().getLabel()}
        </c:if>
        </td>
    </tr>
    <tr>
        <td class="title" rowspan="2">9.4.4</td>
        <td><fmt:message key="DataSheet.label.9.4.4" /></td>
        <td>
        <c:if test="${user.canEDIT_9_1_2_3_4() || user.canEDIT_SECTION9()}">
            <select name="assessment_UpDownListing">
                <c:forEach var="tmp" items="${assessment_UpDownListing}">
                    <c:if test="${rlde.getAssessment().getUpDownListing().toString().equals(tmp.toString())}">
                        <option value="${tmp.toString()}" selected="selected">${tmp.getLabel()}</option>
                    </c:if>
                    <c:if test="${!rlde.getAssessment().getUpDownListing().toString().equals(tmp.toString())}">
                        <option value="${tmp.toString()}">${tmp.getLabel()}</option>
                    </c:if>
                </c:forEach>
            </select>
        </c:if>
        <c:if test="${!user.canEDIT_9_1_2_3_4() && !user.canEDIT_SECTION9()}">
            ${rlde.getAssessment().getUpDownListing().getLabel()}
        </c:if>
        </td>
    </tr>
    <tr><td style="width:auto">Suggested action</td><td>${assessment_UpDownList}</td></tr>
    <tr>
        <td class="title">9.4.5</td><td><fmt:message key="DataSheet.label.9.4.5" /><div class="fieldhelp"><fmt:message key="DataSheet.help.9.4.5" /></div></td>
        <td>
            <t:editabletext
                privilege="${user.canEDIT_9_1_2_3_4() || user.canEDIT_9_3_9_45() || user.canEDIT_SECTION9()}"
                value="${rlde.getAssessment().getUpDownListingJustification()}"
                name="assessment_UpDownListingJustification"/>
        </td>
    </tr>
    <tr>
        <td class="title">9.4.6</td>
        <td><fmt:message key="DataSheet.label.9.4.6" /></td>
        <td>${rlde.getAssessment().getFinalJustification()}</td>
    </tr>
</table>
