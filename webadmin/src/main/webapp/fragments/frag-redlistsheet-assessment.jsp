<%@ page pageEncoding="UTF-8" %>
<%@ page session="true" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<fmt:setBundle basename="pt.floraon.redlistdata.fieldValues" />
</table><table class="sheet">
<thead>
<tr class="hide-in-web"><td colspan="3" class="separator"></td></tr>
<tr class="section9"><td class="title" colspan="3"><a name="assessment"></a><span class="hide-in-print"><fmt:message key="DataSheet.label.section"/> 9 - </span><fmt:message key="DataSheet.label.9" /></td></tr>
</thead>
<tr class="section9 hide-in-print"><t:redlistsheetrow field="9.1" help="false"/><td class="triggergroup"><%@ include file="/fragments/redlistsheetfields/9.1.jsp" %></td></tr>
<tr class="section9 hide-in-print"><t:redlistsheetrow field="9.2" help="true"/><td><%@ include file="/fragments/redlistsheetfields/9.2.jsp" %></td></tr>
<tr class="section9 textual"><t:redlistsheetrow field="9.3" help="true"/><td colspan="${user.isGuest() ? 2 : 1}">
    <c:if test="${what=='taxon' && user.isGuest()}">${rlde.getAssessment().getFinalJustification()}</c:if>
    <c:if test="${!(what=='taxon' && user.isGuest())}">
    <t:editabletext
        privilege="${user.canEDIT_9_1_2_3_4() || user.canEDIT_9_3_9_45() || user.canEDIT_SECTION9()}"
        value="${rlde.getAssessment().getJustification()}"
        maxlen="1700"
        name="assessment_Justification"/>
    </c:if>
</td></tr>
<tr class="section9 hide-in-print"><t:redlistsheetrow field="9.4" help="true"/><td><%@ include file="/fragments/redlistsheetfields/9.4.jsp" %></td></tr>
<tr class="section9 hide-in-print"><t:redlistsheetrow field="9.5" help="true"/><td><%@ include file="/fragments/redlistsheetfields/9.5.jsp" %></td></tr>
