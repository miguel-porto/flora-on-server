<%@ page pageEncoding="UTF-8" %>
<%@ page session="true" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<fmt:setBundle basename="pt.floraon.redlistdata.fieldValues" />
<tr class="section1">
    <td class="title">1.1</td>
    <td><fmt:message key="DataSheet.label.1.1" /></td><td>${taxon.getCanonicalName().toString(true)}
        <div class="floatingtoolbar">
            <div tabindex="0" id="removeformatting" class="hidden"></div>
        <c:if test="${user.canEDIT_ANY_FIELD()}">
            <input type="submit" value="" id="mainformsubmitter" class="hidden"/>
        </c:if>
            <div id="toggle_help"></div>
        </div>
    </td>
</tr>
<tr class="section1"><t:redlistsheetrow field="1.2" help="false"/><td>${taxon.getAuthor()}</td></tr>
<tr class="section1"><t:redlistsheetrow field="1.3" help="false"/><td><%@ include file="/fragments/redlistsheetfields/1.3.jsp" %></td></tr>
<tr class="section1 textual"><t:redlistsheetrow field="1.4" help="true"/><td>
    <t:editabletext
        privilege="${user.canEDIT_1_4()}"
        value="${rlde.getTaxonomicProblemDescription()}"
        name="taxonomicProblemDescription"/>
</td></tr>
<tr class="section1"><t:redlistsheetrow field="1.5" help="false"/><td><c:out value="${commonNames}"/></td></tr>
<c:if test="${user.canVIEW_FULL_SHEET()}">
<tr class="section1"><td class="title">1.6</td><td><fmt:message key="DataSheet.label.1.6" /></td><td><%@ include file="/fragments/redlistsheetfields/1.6.jsp" %></td></tr>
</c:if>
<tr class="section2"><td class="title" colspan="3"><a name="distribution"></a><fmt:message key="DataSheet.label.section"/> 2 - <fmt:message key="DataSheet.label.2" /></td></tr>
<tr class="section2 textual"><t:redlistsheetrow field="2.1" help="true"/><td><%@ include file="/fragments/redlistsheetfields/2.1.jsp" %></td></tr>
<tr class="section2"><t:redlistsheetrow field="2.2" help="false"/><td><%@ include file="/fragments/redlistsheetfields/2.2.jsp" %><%@ include file="/fragments/redlistsheetfields/2.2a.jsp" %></td></tr>
<tr class="section2"><t:redlistsheetrow field="2.3" help="true" /><td><%@ include file="/fragments/redlistsheetfields/2.3.jsp" %><%@ include file="/fragments/redlistsheetfields/2.3a.jsp" %></td></tr>
<tr class="section2 declines"><t:redlistsheetrow field="2.4" help="true" /><td><%@ include file="/fragments/redlistsheetfields/2.4.jsp" %></td></tr>
<tr class="section2"><t:redlistsheetrow field="2.5" help="false"/><td><%@ include file="/fragments/redlistsheetfields/2.5.jsp" %></td></tr>
<tr class="section2"><t:redlistsheetrow field="2.6" help="true" /><td><%@ include file="/fragments/redlistsheetfields/2.6.jsp" %></td></tr>

<tr class="section3"><td class="title" colspan="3"><a name="population"></a><fmt:message key="DataSheet.label.section"/> 3 - <fmt:message key="DataSheet.label.3" /></td></tr>
<tr class="section3 textual"><t:redlistsheetrow field="3.1" help="true"/><td>
    <t:editabletext
        privilege="${user.canEDIT_SECTION3() || user.canEDIT_ALL_TEXTUAL()}"
        value="${rlde.getPopulation().getDescription()}"
        name="population_Description"/>
</td></tr>
<tr class="section3"><t:redlistsheetrow field="3.2" help="false"/><td><%@ include file="/fragments/redlistsheetfields/3.2.jsp" %></td></tr>
<tr class="section3"><t:redlistsheetrow field="3.3"/><td><%@ include file="/fragments/redlistsheetfields/3.3.jsp" %></td></tr>
<tr class="section3 declines"><t:redlistsheetrow field="3.4"/><td><%@ include file="/fragments/redlistsheetfields/3.4.jsp" %></td></tr>
<tr class="section3"><t:redlistsheetrow field="3.5"/><td><%@ include file="/fragments/redlistsheetfields/3.5.jsp" %></td></tr>
<tr class="section3 declines"><t:redlistsheetrow field="3.6"/><td><%@ include file="/fragments/redlistsheetfields/3.6.jsp" %></td></tr>
<tr class="section3"><t:redlistsheetrow field="3.7"/><td><%@ include file="/fragments/redlistsheetfields/3.7.jsp" %></td></tr>
<tr class="section3"><t:redlistsheetrow field="3.8"/><td><%@ include file="/fragments/redlistsheetfields/3.8.jsp" %></td></tr>
<tr class="section3"><t:redlistsheetrow field="3.9"/><td><%@ include file="/fragments/redlistsheetfields/3.9.jsp" %></td></tr>

<tr class="section4"><td class="title" colspan="3"><a name="ecology"></a><fmt:message key="DataSheet.label.section"/> 4 - <fmt:message key="DataSheet.label.4" /></td></tr>
<tr class="section4 textual"><t:redlistsheetrow field="4.1"/><td>
    <t:editabletext
        privilege="${user.canEDIT_SECTION4() || user.canEDIT_ALL_TEXTUAL()}"
        value="${ecology}"
        name="ecology_Description"/>
</td></tr>
<tr class="section4"><t:redlistsheetrow field="4.2" help="false"/><td><%@ include file="/fragments/redlistsheetfields/4.2.jsp" %></td></tr>
<tr class="section4"><t:redlistsheetrow field="4.3" help="false"/><td>${lifeform}</td></tr>
<tr class="section4"><t:redlistsheetrow field="4.4" help="false"/><td><%@ include file="/fragments/redlistsheetfields/4.4.jsp" %></td></tr>
<tr class="section4 declines"><t:redlistsheetrow field="4.5" help="true" /><td><%@ include file="/fragments/redlistsheetfields/4.5.jsp" %></td></tr>

<tr class="section5"><td class="title" colspan="3"><a name="uses"></a><fmt:message key="DataSheet.label.section"/> 5 - <fmt:message key="DataSheet.label.5" /></td></tr>
<tr class="section5 textual"><t:redlistsheetrow field="5.1" help="true" /><td>
    <t:editabletext
        privilege="${user.canEDIT_SECTION5() || user.canEDIT_ALL_TEXTUAL()}"
        value="${rlde.getUsesAndTrade().getDescription()}"
        name="usesAndTrade_Description"/>
</td></tr>
<tr class="section5"><t:redlistsheetrow field="5.2" help="false"/><td><%@ include file="/fragments/redlistsheetfields/5.2.jsp" %></td></tr>
<tr class="section5"><t:redlistsheetrow field="5.3" help="false"/><td><%@ include file="/fragments/redlistsheetfields/5.3.jsp" %></td></tr>
<tr class="section5"><t:redlistsheetrow field="5.4" help="false"/><td><%@ include file="/fragments/redlistsheetfields/5.4.jsp" %></td></tr>

<tr class="section6"><td class="title" colspan="3"><a name="threats"></a><fmt:message key="DataSheet.label.section"/> 6 - <fmt:message key="DataSheet.label.6" /></td></tr>
<tr class="section6 textual"><t:redlistsheetrow field="6.1" help="true" /><td>
    <t:editabletext
        privilege="${user.canEDIT_SECTION6() || user.canEDIT_ALL_TEXTUAL()}"
        value="${rlde.getThreats().getDescription()}"
        name="threats_Description"/>
</td></tr>
<tr class="section6"><t:redlistsheetrow field="6.2" help="true"/><td><%@ include file="/fragments/redlistsheetfields/6.2.jsp" %></td></tr>
<tr class="section6"><t:redlistsheetrow field="6.3" help="true"/><td><%@ include file="/fragments/redlistsheetfields/6.3.jsp" %></td></tr>
<tr class="section6 declines"><t:redlistsheetrow field="6.4" help="true"/><td><%@ include file="/fragments/redlistsheetfields/6.4.jsp" %></td></tr>
<tr class="section6"><t:redlistsheetrow field="6.5" help="true"/><td><%@ include file="/fragments/redlistsheetfields/6.5.jsp" %></td></tr>

<tr class="section7"><td class="title" colspan="3"><a name="conservation"></a><fmt:message key="DataSheet.label.section"/> 7 - <fmt:message key="DataSheet.label.7" /></td></tr>
<tr class="section7 textual"><t:redlistsheetrow field="7.1" help="true" /><td>
    <t:editabletext
        privilege="${user.canEDIT_SECTION7() || user.canEDIT_ALL_TEXTUAL()}"
        value="${rlde.getConservation().getDescription()}"
        name="conservation_Description"/>
</td></tr>
<tr class="section7"><t:redlistsheetrow field="7.2" help="false"/><td><%@ include file="/fragments/redlistsheetfields/7.2.jsp" %></td></tr>
<tr class="section7"><t:redlistsheetrow field="7.3" help="false"/><td><%@ include file="/fragments/redlistsheetfields/7.3.jsp" %></td></tr>
<tr class="section7"><t:redlistsheetrow field="7.4" help="false"/><td><%@ include file="/fragments/redlistsheetfields/7.4.jsp" %></td></tr>
<tr class="section7"><t:redlistsheetrow field="7.4.1" help="false"/><td>
    <ul>
        <c:forEach var="tmp" items="${legalProtection}">
        <li>${tmp}</li>
        </c:forEach>
    </ul>
</td></tr>
<tr class="section7"><t:redlistsheetrow field="7.5" help="true"/><td><%@ include file="/fragments/redlistsheetfields/7.5.jsp" %></td></tr>
<tr class="section7"><t:redlistsheetrow field="7.6" help="true"/><td><%@ include file="/fragments/redlistsheetfields/7.6.jsp" %></td></tr>

<tr class="section8"><td class="title" colspan="3"><fmt:message key="DataSheet.label.section"/> 8 - <fmt:message key="DataSheet.label.8" /></td></tr>
<tr class="section8"><t:redlistsheetrow field="8.1" help="true"/><td><%@ include file="/fragments/redlistsheetfields/8.1.jsp" %></td></tr>
<tr class="section8"><t:redlistsheetrow field="8.2" help="true"/><td><%@ include file="/fragments/redlistsheetfields/8.2.jsp" %></td></tr>
