<%@ page pageEncoding="UTF-8" %>
<table class="subtable">
    <tr><td class="title">9.9.1</td><td><fmt:message key="DataSheet.label.9.9.1" /></td><td>
    <c:if test="${user.canEDIT_9_5_9_6_9_61_9_91() || user.canEDIT_SECTION9()}">
        <select name="assessment_TextStatus">
            <c:forEach var="tmp" items="${assessment_TextStatus}">
                <c:if test="${rlde.getAssessment().getTextStatus().toString().equals(tmp.toString())}">
                    <option value="${tmp.toString()}" selected="selected"><fmt:message key="${tmp.getLabel()}"/></option>
                </c:if>
                <c:if test="${!rlde.getAssessment().getTextStatus().toString().equals(tmp.toString())}">
                    <option value="${tmp.toString()}"><fmt:message key="${tmp.getLabel()}"/></option>
                </c:if>
            </c:forEach>
        </select>
    </c:if>
    <c:if test="${!user.canEDIT_9_5_9_6_9_61_9_91() && !user.canEDIT_SECTION9()}">
        <fmt:message key="${rlde.getAssessment().getTextStatus().getLabel()}"/>
    </c:if>
    </td></tr>
    <tr><td class="title">9.9.2</td><td><fmt:message key="DataSheet.label.9.9.2" /></td><td>
    <c:if test="${user.canEDIT_9_7_9_92() || user.canEDIT_SECTION9()}">
        <select name="assessment_AssessmentStatus">
            <c:forEach var="tmp" items="${assessment_AssessmentStatus}">
                <c:if test="${rlde.getAssessment().getAssessmentStatus().toString().equals(tmp.toString())}">
                    <option value="${tmp.toString()}" selected="selected"><fmt:message key="${tmp.getLabel()}"/></option>
                </c:if>
                <c:if test="${!rlde.getAssessment().getAssessmentStatus().toString().equals(tmp.toString())}">
                    <option value="${tmp.toString()}"><fmt:message key="${tmp.getLabel()}"/></option>
                </c:if>
            </c:forEach>
        </select>
    </c:if>
    <c:if test="${!user.canEDIT_9_7_9_92() && !user.canEDIT_SECTION9()}">
        <fmt:message key="${rlde.getAssessment().getAssessmentStatus().getLabel()}"/>
    </c:if>
    </td></tr>
    <tr><td class="title">9.9.3</td><td><fmt:message key="DataSheet.label.9.9.3" /></td><td>
    <c:if test="${user.canEDIT_9_8_9_93() || user.canEDIT_SECTION9()}">
        <select name="assessment_ReviewStatus">
            <c:forEach var="tmp" items="${assessment_ReviewStatus}">
                <c:if test="${rlde.getAssessment().getReviewStatus().toString().equals(tmp.toString())}">
                    <option value="${tmp.toString()}" selected="selected"><fmt:message key="${tmp.getLabel()}"/></option>
                </c:if>
                <c:if test="${!rlde.getAssessment().getReviewStatus().toString().equals(tmp.toString())}">
                    <option value="${tmp.toString()}"><fmt:message key="${tmp.getLabel()}"/></option>
                </c:if>
            </c:forEach>
        </select>
    </c:if>
    <c:if test="${!user.canEDIT_9_8_9_93() && !user.canEDIT_SECTION9()}">
        <fmt:message key="${rlde.getAssessment().getReviewStatus().getLabel()}"/>
    </c:if>
    </td></tr>
    <tr><td class="title">9.9.4</td><td><fmt:message key="DataSheet.label.9.9.4" /></td><td>
    <c:if test="${user.canEDIT_9_9_4() || user.canEDIT_SECTION9()}">
        <select name="assessment_PublicationStatus">
            <c:forEach var="tmp" items="${assessment_PublicationStatus}">
                <c:if test="${rlde.getAssessment().getPublicationStatus().toString().equals(tmp.toString())}">
                    <option value="${tmp.toString()}" selected="selected"><fmt:message key="${tmp.getLabel()}"/></option>
                </c:if>
                <c:if test="${!rlde.getAssessment().getPublicationStatus().toString().equals(tmp.toString())}">
                    <option value="${tmp.toString()}"><fmt:message key="${tmp.getLabel()}"/></option>
                </c:if>
            </c:forEach>
        </select>
    </c:if>
    <c:if test="${!user.canEDIT_9_9_4() && !user.canEDIT_SECTION9()}">
        <fmt:message key="${rlde.getAssessment().getPublicationStatus().getLabel()}"/>
    </c:if>
    </td></tr>
    <c:if test="${rlde.getAssessment().getPublicationStatus().isApproved()}">
    <tr><td class="title">9.9.5</td><td><fmt:message key="DataSheet.label.9.9.5" /></td><td>
    <c:if test="${user.canEDIT_9_9_5() || user.canEDIT_SECTION9()}">
        <select name="assessment_ValidationStatus">
            <c:forEach var="tmp" items="${assessment_ValidationStatus}">
                <c:if test="${rlde.getAssessment().getValidationStatus().toString().equals(tmp.toString())}">
                    <option value="${tmp.toString()}" selected="selected">${tmp.getLabel()}</option>
                </c:if>
                <c:if test="${!rlde.getAssessment().getValidationStatus().toString().equals(tmp.toString())}">
                    <option value="${tmp.toString()}">${tmp.getLabel()}</option>
                </c:if>
            </c:forEach>
        </select>
    </c:if>
    <c:if test="${!user.canEDIT_9_9_5() && !user.canEDIT_SECTION9()}">
        ${rlde.getAssessment().getValidationStatus().getLabel()}
    </c:if>
    </td></tr>
    </c:if>
</table>
