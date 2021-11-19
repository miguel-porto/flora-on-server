<%@ page pageEncoding="UTF-8" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page session="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<fmt:setBundle basename="pt.floraon.redlistdata.fieldValues" />
<c:if test="${user.canVIEW_FULL_SHEET()}">
    <div class="navigator">
        <fmt:message key="DataSheet.label.2" var="NS2"/>
        <fmt:message key="DataSheet.label.3" var="NS3"/>
        <fmt:message key="DataSheet.label.4" var="NS4"/>
        <fmt:message key="DataSheet.label.5" var="NS5"/>
        <fmt:message key="DataSheet.label.6" var="NS6"/>
        <fmt:message key="DataSheet.label.7" var="NS7"/>
        <fmt:message key="DataSheet.label.9" var="NS9"/>
        <div class="button anchorbutton section2"><a href="#distribution">2. ${fn:substring(NS2, 0, 1)}</a></div>
        <div class="button anchorbutton section3"><a href="#population">3. ${fn:substring(NS3, 0, 1)}</a></div>
        <div class="button anchorbutton section4"><a href="#ecology">4. ${fn:substring(NS4, 0, 1)}</a></div>
        <div class="button anchorbutton section5"><a href="#uses">5. ${fn:substring(NS5, 0, 1)}</a></div>
        <div class="button anchorbutton section6"><a href="#threats">6. ${fn:substring(NS6, 0, 1)}</a></div>
        <div class="button anchorbutton section7"><a href="#conservation">7. ${fn:substring(NS7, 0, 1)}</a></div>
        <div class="button anchorbutton section9"><a href="#assessment">9. ${fn:substring(NS9, 0, 1)}</a></div>
    </div>
</c:if>

<div id="sheet-header" class="title">
<c:if test="${multipletaxa}">
    <h1><fmt:message key="DataSheet.msg.multipletaxa"/></h1>
    <ul class="inlinelistitems">
    <c:forEach var="taxon" items="${taxa}">
        <li>
            <input type="hidden" name="taxEntID" value="${taxon.getID()}"/>
            <i>${taxon.getName()}</i>
        </li>
    </c:forEach>
    </ul>
</c:if>
<c:if test="${!multipletaxa}">
    <h1>${taxon.getCanonicalName().toString(true)}
        <c:if test="${rls.isEditionLocked(rlde) && !rls.isSheetUnlocked(taxon.getID())}"> <img class="lock" src="../images/locked.png" style="height:auto"/></c:if>
        <c:if test="${versiondate != null}"><span style="font-size:0.4em"> [version ${versiondate}]</span></c:if>
        <c:if test="${rls.isEditionLocked(rlde) && rls.isSheetUnlocked(taxon.getID()) && versiondate == null}">
            <img class="lock" style="height:auto" src="../images/unlocked.png"/> <span class="warning">Esta ficha está desbloqueada!</span>
        </c:if>
    </h1>
    <div class="redlistcategory assess_${rlde.getAssessment().getFinalCategory().getEffectiveCategory().toString()}">
        <h1>
            ${rlde.getAssessment().getFinalCategory().getShortTag()}
            <c:if test="${rlde.getAssessment().getCategory().toString().equals('CR') && !rlde.getAssessment().getSubCategory().toString().equals('NO_TAG')}"><sup>${rlde.getAssessment().getSubCategory().toString()}</sup></c:if>
        </h1>
        <p>${rlde.getAssessment().getFinalCategory().getLabel()}</p>
    </div>
    <div id="panels">
        <div id="header-buttons">
            <h3>Tools</h3>
            <div class="wordtag togglebutton"><a href="../checklist?w=taxdetails&id=${taxon._getIDURLEncoded()}">checklist</a></div>
            <div class="wordtag togglebutton"><a href="../api/svgmap?basemap=1&size=${user.canVIEW_OCCURRENCES() ? 2000 : 10000}&border=0.05&shadow=0&download=1&pa=0&stroke=true&squareFill=98d900&taxon=${taxon._getIDURLEncoded()}">download SVG map</a></div>
            <div class="wordtag togglebutton"><a href="../api/svgmap?basemap=1&size=${user.canVIEW_OCCURRENCES() ? 2000 : 10000}&border=0.05&shadow=0&download=1&pa=0&stroke=true&squareFill=98d900&taxon=${taxon._getIDURLEncoded()}&historical=1">download historical SVG map</a></div>
            <c:if test="${user.canVIEW_FULL_SHEET()}">
                <div class="wordtag togglebutton" id="summary_toggle">summary</div>
                <div class="wordtag togglebutton" id="declines_toggle">declines</div>
                <div class="wordtag togglebutton"><a href="?w=downloadsheet&id=${taxon._getIDURLEncoded()}">download sheet</a></div>
                <c:url value="https://lvf.flora-on.pt/redlist/${territory}" var="urlmd">
                  <c:param name="w" value="downloadsheet" />
                  <c:param name="id" value="${taxon._getIDURLEncoded()}" />
                </c:url>
            </c:if>
            <c:if test="${user.canVIEW_OCCURRENCES()}">
                <div class="wordtag togglebutton"><a href="?w=taxonrecords&group=500&id=${taxon._getIDURLEncoded()}">view occurrences</a></div>
                <div class="wordtag togglebutton"><a href="../api/wktmap?size=2000&taxon=${taxon._getIDURLEncoded()}">download WKT</a></div>
            </c:if>
            <c:if test="${user.canDOWNLOAD_OCCURRENCES() || user.hasEDIT_ALL_1_8()}">
                <div class="wordtag togglebutton"><a href="?w=downloadtaxonrecords&id=${taxon._getIDURLEncoded()}">download KML</a></div>
            </c:if>
        </div>
        <c:if test="${user.canMANAGE_VERSIONS() && rls.isEditionLocked(rlde) && versiondate == null}">
        <div>
            <h3>Edition</h3>
            <c:if test="${!rls.isSheetUnlocked(taxon.getID())}">
                <form class="poster inlineblock" data-path="api/setoptions" data-refresh="true">
                    <input type="hidden" name="territory" value="${territory}"/>
                    <input type="hidden" name="option" value="unlockEdition"/>
                    <input type="hidden" name="value" value="${taxon.getID()}"/>
                    <input type="submit" value="Unlock edition" class="textbutton"/>
                </form>
            </c:if>
            <c:if test="${rls.isSheetUnlocked(taxon.getID())}">
                <form class="poster inlineblock" data-path="api/setoptions" data-refresh="true">
                    <input type="hidden" name="territory" value="${territory}"/>
                    <input type="hidden" name="option" value="removeUnlockEdition"/>
                    <input type="hidden" name="value" value="${taxon.getID()}"/>
                    <input type="submit" value="Remove unlock exception" class="textbutton"/>
                </form>
            </c:if>
        </div>
        </c:if>
        <c:if test="${(snapshots.hasNext() && user.canVIEW_FULL_SHEET()) || (user.canMANAGE_VERSIONS() && versiondate == null)}">
        <div id="versions">
            <h3>Versions</h3>
            <c:if test="${snapshots.hasNext()}">
            <c:if test="${user.canMANAGE_VERSIONS() && versiondate != null}">
                <form class="poster inlineblock" data-path="api/deletesnapshot" data-callback="?w=taxon&id=${taxon._getIDURLEncoded()}" data-confirm="true">
                    <input type="hidden" name="id" value="${snapshotid}"/>
                    <input type="hidden" name="territory" value="${territory}"/>
                    <input type="submit" value="Delete this version" class="textbutton"/>
                </form>
            </c:if>
            <c:if test="${versiondate != null}"><div class="wordtag togglebutton"><a href="?w=taxon&id=${taxon._getIDURLEncoded()}">Current<br/>version</a></div></c:if>
            <c:forEach var="snapshot" items="${snapshots}">
                <c:set var="sel" value="${snapshot._getDateSavedFormatted()==versiondate ? ' selected' : ''}" />
                <c:set var="title" value="${snapshot.hasVersionTag() ? ' selected' : ''}" />
                <div class="wordtag togglebutton${sel}"><a href="?w=sheet&id=${snapshot.getKey()}">
                    <c:if test="${snapshot.hasVersionTag()}">${snapshot.getVersionTag()}<br/><span class="info">${snapshot._getDateSavedFormatted()}</span></c:if>
                    <c:if test="${!snapshot.hasVersionTag()}">${snapshot._getDateSavedFormattedTwoLine()}</c:if>
                </a></div>
            </c:forEach>
            </c:if>
            <c:if test="${user.canMANAGE_VERSIONS() && versiondate == null}">
                <form class="poster inlineblock compactform" style="text-align:center;" data-path="api/snapshot" data-refresh="true">
                    <input type="hidden" name="id" value="${taxon.getID()}"/>
                    <input type="hidden" name="territory" value="${territory}"/>
                    <input type="text" name="versiontag" placeholder="type a name" style="width:160px"/><br/>
                    <input type="submit" value="Save this version" class="textbutton"/>
                </form>
            </c:if>
        </div>
        </c:if>
        <c:if test="${user.canCREATE_REDLIST_DATASETS()}">
        <div id="migrate">
            <h3><fmt:message key="TaxonIndex.admin.1"/></h3>
            <form class="poster" data-path="api/migratetotaxon" data-callback="?w=main" id="migrate2taxon">
                <div class="withsuggestions">
                    <input type="text" class="nochangeevent" placeholder="<fmt:message key="DataSheet.msg.typeletters"/>" autocomplete="off" id="migratetaxonbox"/>
                    <div id="migratetaxsuggestions"></div>
                </div>
                <input type="hidden" name="territory" value="${territory}"/>
                <input type="hidden" name="source" value="${rlde.getID()}"/>
                <br/><input type="submit" value="Migrate to taxon" class="textbutton"/>
            </form>
            <form class="poster" data-path="api/removetaxent" data-refresh="false" data-callback="?w=main" data-confirm="true">
                <input type="hidden" name="id" value="${rlde.getTaxEntID()}"/>
                <input type="hidden" name="territory" value="${territory}"/>
                <input type="submit" value="Delete this data sheet" class="textbutton"/>
            </form>
        </div>
        </c:if>
    </div>  <!-- top panels -->
</c:if> <!-- not multiple taxa -->
</div>  <!-- header -->
<c:if test="${multipletaxa}">
<form class="poster" data-path="api/updatedata" id="maindataform" data-callback="?w=main">
</c:if>
<c:if test="${!multipletaxa}">
<c:if test="${warning != null && warning.size() > 0}">
    <div class="warning inline">
        <p><fmt:message key="DataSheet.msg.warning"/></p>
        <ul>
        <c:forEach var="warn" items="${warning}">
            <li><fmt:message key="${warn}"/></li>
        </c:forEach>
        </ul>
    </div>
</c:if>
<form class="poster" data-path="api/updatedata" id="maindataform" data-refresh="true">
</c:if>
    <table class="sheet">
    <c:if test="${user.canVIEW_FULL_SHEET()}">
        <tr class="section1"><td class="title" colspan="3">
            <input type="hidden" name="territory" value="${territory}"/>
            <c:if test="${!multipletaxa}">
            <input type="hidden" name="databaseId" value="${rlde.getID()}"/>
            <input type="hidden" name="taxEntID" value="${rlde.getTaxEntID()}"/>
            </c:if>
            <fmt:message key="DataSheet.label.section"/> 1 - <fmt:message key="DataSheet.label.1" />
        </td></tr>
    </c:if>
<%--        <%@ include file="/fragments/${redListSheetTemplate}.jsp" %> --%>
        <jsp:include page="/fragments/${redListSheetTemplate}.jsp" />
        <tr class="section9"><td class="title" colspan="3"><a name="assessment"></a><fmt:message key="DataSheet.label.section"/> 9 - <fmt:message key="DataSheet.label.9" /></td></tr>
        <tr class="section9"><t:redlistsheetrow field="9.1" help="false"/><td class="triggergroup"><%@ include file="/fragments/redlistsheetfields/9.1.jsp" %></td></tr>
        <tr class="section9"><t:redlistsheetrow field="9.2" help="true"/><td><%@ include file="/fragments/redlistsheetfields/9.2.jsp" %></td></tr>
        <tr class="section9 textual"><t:redlistsheetrow field="9.3" help="true"/><td>
            <t:editabletext
                privilege="${user.canEDIT_9_1_2_3_4() || user.canEDIT_9_3_9_45() || user.canEDIT_SECTION9()}"
                value="${rlde.getAssessment().getJustification()}"
                maxlen="1700"
                name="assessment_Justification"/>
        </td></tr>
        <tr class="section9"><t:redlistsheetrow field="9.4" help="true"/><td><%@ include file="/fragments/redlistsheetfields/9.4.jsp" %></td></tr>
        <tr class="section9"><t:redlistsheetrow field="9.5" help="true"/><td><%@ include file="/fragments/redlistsheetfields/9.5.jsp" %></td></tr>
        <tr class="section9"><t:redlistsheetrow field="9.6" help="false"/><td><%@ include file="/fragments/redlistsheetfields/9.6.jsp" %></td></tr>
        <tr class="section9"><t:redlistsheetrow field="9.6.1" help="false"/><td>
            <c:if test="${user.canEDIT_9_5_9_6_9_61_9_91() || user.canEDIT_SECTION9()}">
                <input name="assessment_Collaborators" type="text" class="longbox" value="${rlde.getAssessment().getCollaborators()}"/>
            </c:if>
            <c:if test="${!user.canEDIT_9_5_9_6_9_61_9_91() && !user.canEDIT_SECTION9()}">
                ${rlde.getAssessment().getCollaborators()}
            </c:if>
        </td></tr>
        <tr class="section9"><t:redlistsheetrow field="9.7" help="false"/><td><%@ include file="/fragments/redlistsheetfields/9.7.jsp" %></td></tr>
        <tr class="section9"><t:redlistsheetrow field="9.8" help="false"/><td><%@ include file="/fragments/redlistsheetfields/9.8.jsp" %></td></tr>
        <tr class="section9"><t:redlistsheetrow field="9.9" help="false"/><td><%@ include file="/fragments/redlistsheetfields/9.9.jsp" %></td></tr>
        <tr class="section9"><t:redlistsheetrow field="9.10" help="false"/><td>${rlde.getDateAssessed()}</td></tr>
        <tr class="section9"><t:redlistsheetrow field="9.11" help="false"/><td>${rlde.getDatePublished()}</td></tr>
        <tr class="section9"><t:redlistsheetrow field="9.12" help="false"/><td>${citation}</td></tr>

    <c:if test="${!multipletaxa && user.canVIEW_FULL_SHEET()}">
        <tr class="section9"><t:redlistsheetrow field="9.13" help="false"/><td><%@ include file="/fragments/redlistsheetfields/9.13.jsp" %></td></tr>

        <tr class="section11"><td class="title" colspan="3"><fmt:message key="DataSheet.label.section"/> 11 - <fmt:message key="DataSheet.label.11" /></td></tr>
        <tr class="section11"><t:redlistsheetrow field="11.1" help="true"/><td>
            <t:editabletext
                privilege="${(rlde.getAssessment().getReviewStatus().toString() == 'REVISED_WORKING' && (authors.contains(user.getID()) || evaluator.contains(user.getID()))) || user.canEDIT_11()}"
                value="${rlde.getReplyToReviewer()}"
                name="replyToReviewer"/>
        </td></tr>

        <c:if test="${user.canVIEW_10_2() || user.canEDIT_9_9_5()}">
        <tr class="section11"><t:redlistsheetrow field="11.2" help="false"/><td>
            <t:editabletext
                privilege="${rlde.getAssessment().getValidationStatus().toString() == 'NEEDS_CORRECTIONS' && user.canEDIT_11() && user.canVIEW_10_2()}"
                value="${rlde.getReplyToValidation()}"
                name="replyToValidation"/>
        </td></tr>
        </c:if>

        <c:if test="${user.canCREATE_REDLIST_DATASETS()}">
            <tr class="section12"><td class="title" colspan="3"><fmt:message key="DataSheet.label.section"/> 12 - <fmt:message key="DataSheet.label.12" /></td></tr>
            <tr class="section12"><t:redlistsheetrow field="12.1" help="false"/><td>
                <input type="text" name="coverPhotoUrl" value="${rlde.getCoverPhotoUrl()}"/>
            </td></tr>
        </c:if>
    </c:if>
    </table>
    <c:if test="${!multipletaxa && user.canVIEW_FULL_SHEET() && (!rlde.getReviewerComments().isEmpty() || !rlde.getValidationComments().isEmpty() || user.canEDIT_10() || user.canEDIT_9_9_5() || (user.canVIEW_10_2() && !rlde.getValidationComments().isEmpty()))}">
    <div id="reviewpanel">
        <div id="reviewpanel-handle"><h1><fmt:message key="DataSheet.label.section"/> 10 - <fmt:message key="DataSheet.label.10" /></h1></div>
        <h2>10.1 <fmt:message key="DataSheet.label.10.1" /></h2>
        <t:editabletext
            privilege="${user.canEDIT_10()}"
            value="${rlde.getReviewerComments()}"
            name="reviewerComments"/>
        <c:if test="${user.canVIEW_10_2() || user.canEDIT_9_9_5()}">
        <h2>10.2 <fmt:message key="DataSheet.label.10.2" /></h2>
        <t:editabletext
            privilege="${user.canEDIT_9_9_5()}"
            value="${rlde.getValidationComments()}"
            name="validationComments"/>
        </c:if>
    </div>
    </c:if>
</form>
