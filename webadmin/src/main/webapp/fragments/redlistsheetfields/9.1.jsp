<%@ page pageEncoding="UTF-8" %>
<div id="redlistcategories">
    <c:if test="${user.canEDIT_9_1_2_3_4() || user.canEDIT_SECTION9()}">
        <c:forEach var="tmp" items="${assessment_Category}">
            <c:if test="${rlde.getAssessment().getAdjustedCategory().getEffectiveCategory().equals(tmp)}">
                <input type="radio" name="assessment_Category" value="${rlde.getAssessment().getAdjustedCategory().toString()}" id="assess_${tmp.toString()}" checked="checked" class="trigger" data-trigger="${tmp.isTrigger() ? 1 : 0}">
            </c:if>
            <c:if test="${!rlde.getAssessment().getAdjustedCategory().getEffectiveCategory().equals(tmp)}">
                <input type="radio" name="assessment_Category" value="${tmp.toString()}" id="assess_${tmp.toString()}" class="trigger" data-trigger="${tmp.isTrigger() ? 1 : 0}">
            </c:if>
            <label for="assess_${tmp.toString()}">
                <h1>
                    ${tmp.getShortTag()}<c:if test="${rlde.getAssessment().getAdjustedCategory().getEffectiveCategory().equals(tmp) && rlde.getAssessment().getAdjustedCategory().isUpDownListed()}">º</c:if>
                    <c:if test="${tmp == 'CR' && rlde.getAssessment().getCategory().toString().equals(tmp.toString()) && !rlde.getAssessment().getSubCategory().toString().equals('NO_TAG')}"><sup>${rlde.getAssessment().getSubCategory().toString()}</sup></c:if>
                </h1>
                <p>${tmp.getLabel()}</p>
            </label>
            <c:if test="${tmp == 'VU'}"><br/></c:if>
        </c:forEach>
    </c:if>
    <c:if test="${!user.canEDIT_9_1_2_3_4() && !user.canEDIT_SECTION9()}">
        <div class="redlistcategory assess_${rlde.getAssessment().getCategory().toString()}"><h1>${rlde.getAssessment().getCategory().getShortTag()}</h1><p>${rlde.getAssessment().getCategory().getLabel()}</p></div>
    </c:if>
</div>
<div class="triggered ${rlde.getAssessment().getCategory().isTrigger() ? '' : 'hidden'}">
<c:if test="${user.canEDIT_9_1_2_3_4() || user.canEDIT_SECTION9()}">
    <select name="assessment_SubCategory">
        <c:forEach var="tmp" items="${assessment_SubCategory}">
            <c:if test="${rlde.getAssessment().getSubCategory().toString().equals(tmp.toString())}">
                <option value="${tmp.toString()}" selected="selected">${tmp.getLabel()}</option>
            </c:if>
            <c:if test="${!rlde.getAssessment().getSubCategory().toString().equals(tmp.toString())}">
                <option value="${tmp.toString()}">${tmp.getLabel()}</option>
            </c:if>
        </c:forEach>
    </select>
</c:if>
<c:if test="${!user.canEDIT_9_1_2_3_4() && !user.canEDIT_SECTION9()}">
    ${rlde.getAssessment().getSubCategory().getLabel()}
</c:if>
</div>
