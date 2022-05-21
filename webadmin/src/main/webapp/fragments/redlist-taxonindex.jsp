<%@ page pageEncoding="UTF-8" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="language" value="${not empty param.language ? param.language : not empty language ? language : pageContext.response.locale}" scope="request" />
<fmt:setLocale value="${language}" />
<fmt:setBundle basename="pt.floraon.redlistdata.fieldValues" />
<c:if test="${user.isGuest()}">
    <div class="warning"><b>You&#8217;re not authorized to enter this page</b></div>
</c:if>
<c:if test="${!user.isGuest()}">
<h1>Taxon index</h1>
<c:if test="${user.canCREATE_REDLIST_DATASETS()}">
<div class="filterpanel">
    <h3><fmt:message key="TaxonIndex.admin.1"/></h3>
    <form class="poster" data-path="api/addnewtaxent" data-refresh="true" id="addtaxon2redlist">
        <div class="withsuggestions">
            <input type="text" class="nochangeevent" placeholder="<fmt:message key="DataSheet.msg.typeletters"/>" autocomplete="off" id="addtaxonbox"/>
            <div id="addtaxsuggestions"></div>
        </div>
        <input type="hidden" name="territory" value="${territory}"/>
        <input type="submit" value="<fmt:message key="TaxonIndex.admin.2"/>" class="textbutton"/>
    </form>
</div>
</c:if>
<div class="filterpanel">
    <h3>Tools</h3>
    <div style="display:inline-block">
    <form method="GET">
        <input type="hidden" name="w" value="search"/>
        <input type="text" name="s" placeholder="type search text"/>
        <input type="submit" value="Search all data sheets" class="textbutton"/>
    </form></div>
    <c:url value="../redlist/${territory}" var="urldt">
      <c:param name="w" value="downloadtaxawithtag" />
      <c:param name="tag" value="Lista Alvo" />
    </c:url>
    <div style="display:inline-block"><div class="button anchorbutton"><a href="${urldt}">Download «Lista Alvo»</a></div></div>
</div>
<c:if test="${user.canEDIT_ANY_FIELD()}">
<div class="filterpanel inactive">
    <h3><fmt:message key="TaxonIndex.selecting.1"/></h3>
    <p id="selectedmsg" style="padding:0"></p>
    <div class="button" id="selectall"><fmt:message key="TaxonIndex.selecting.4"/></div>
    <div class="button" id="toggleselectedtaxa"><fmt:message key="TaxonIndex.selecting.2"/></div>
    <div class="button" id="selecttaxa"><fmt:message key="TaxonIndex.selecting.3"/></div>
    <div class="button" id="addtag"><fmt:message key="TaxonIndex.selecting.5"/></div>
</div>
<form data-path="api/addtag" id="addtagform">
    <input type="hidden" name="territory" value="${territory}"/>
</form>
</c:if>
<div id="filters" class="filterpanel inactive">
    <h3><fmt:message key="TaxonIndex.filters.1"/></h3>
    <c:if test="${!user.isGuest()}">
    <div class="filter-section">
        <fmt:message key="TaxonIndex.filters.3" var="tmp"/>
        <t:optionbutton optionname="onlyresponsible" title="${tmp}" defaultvalue="false" norefresh="true" style="light"/>
        <fmt:message key="TaxonIndex.filters.3h" var="tmp"/>
        <t:optionbutton optionname="onlyauthor" title="${tmp}" defaultvalue="false" norefresh="true" style="light"/>
        <fmt:message key="TaxonIndex.filters.3a" var="tmp"/>
        <t:optionbutton optionname="onlyassessor" title="${tmp}" defaultvalue="false" norefresh="true" style="light"/>
        <fmt:message key="TaxonIndex.filters.3b" var="tmp"/>
        <t:optionbutton optionname="onlyreviewer" title="${tmp}" defaultvalue="false" norefresh="true" style="light"/>
    </div>
    <div class="filter-section">
        <fmt:message key="TaxonIndex.filters.3c" var="tmp"/>
        <t:optionbutton optionname="onlyrevised" title="${tmp}" defaultvalue="false" norefresh="true" style="light"/>
        <fmt:message key="TaxonIndex.filters.3d" var="tmp"/>
        <t:optionbutton optionname="onlyreassessment" title="${tmp}" defaultvalue="false" norefresh="true" style="light"/>
        <fmt:message key="TaxonIndex.filters.3f" var="tmp"/>
        <t:optionbutton optionname="onlyapproved" title="${tmp}" defaultvalue="false" norefresh="true" style="light"/>
        <fmt:message key="TaxonIndex.filters.3e" var="tmp"/>
        <t:optionbutton optionname="onlyvalidated" title="${tmp}" defaultvalue="false" norefresh="true" style="light"/>
        <fmt:message key="TaxonIndex.filters.3g" var="tmp"/>
        <t:optionbutton optionname="onlyneedscorrections" title="${tmp}" defaultvalue="false" norefresh="true" style="light"/>
        <fmt:message key="TaxonIndex.filters.6" var="tmp"/>
        <t:optionbutton optionname="onlyassessed" title="${tmp}" defaultvalue="false" norefresh="true" style="light"/>
        <fmt:message key="TaxonIndex.filters.5" var="tmp"/>
        <t:optionbutton optionname="onlypublished" title="${tmp}" defaultvalue="false" norefresh="true" style="light"/>
        <fmt:message key="TaxonIndex.filters.8" var="tmp"/>
        <t:optionbutton optionname="onlyvalidationerror" title="${tmp}" defaultvalue="false" norefresh="true" style="light"/>
    </div>
    </c:if>
    <div class="filter-section">
        <c:forEach var="tmp" items="${allTags}">
        <t:optionbutton optionname="${tmp.getKey()}" title="${tmp.getValue()}" defaultvalue="false" norefresh="true" style="light" classes="tag"/>
        </c:forEach>
        <fmt:message key="TaxonIndex.filters.4" var="tmp"/>
        <t:optionbutton optionname="onlynative" title="${tmp}" defaultvalue="true" norefresh="true" style="light"/>
    </div>
    <c:if test="${user.canEDIT_ANY_FIELD()}"><div class="filter" id="onlyselected"><div class="light"></div><div><fmt:message key="TaxonIndex.filters.7"/></div></div></c:if>
</div>

<form method="post" action="../redlist/${territory}">
    <input type="hidden" name="w" value="taxon"/>
    <c:if test="${user.canEDIT_ANY_FIELD()}">
    <div class="floatingtoolbar">
        <input type="submit" value="" id="editselectedtaxa" class="hidden"/>
    </div>
    </c:if>

    <c:set var="indexclasses" value=""/>
    <c:forEach var="tmp" items="${allTags}">
    <c:set var="indexclasses" value="${indexclasses}${sessionScope['option-'.concat(tmp.getKey())] ? (' filter_'.concat(tmp.getKey())) : ''}" />
    </c:forEach>
    <c:set var="indexclasses" value="${indexclasses}${sessionScope['option-onlyresponsible'] ? (' filter_onlyresponsible') : ''}" />
    <c:set var="indexclasses" value="${indexclasses}${sessionScope['option-onlyauthor'] ? (' filter_onlyauthor') : ''}" />
    <c:set var="indexclasses" value="${indexclasses}${sessionScope['option-onlyassessor'] ? (' filter_onlyassessor') : ''}" />
    <c:set var="indexclasses" value="${indexclasses}${sessionScope['option-onlyreviewer'] ? (' filter_onlyreviewer') : ''}" />
    <c:set var="indexclasses" value="${indexclasses}${sessionScope['option-onlyrevised'] ? (' filter_onlyrevised') : ''}" />
    <c:set var="indexclasses" value="${indexclasses}${sessionScope['option-onlyreassessment'] ? (' filter_onlyreassessment') : ''}" />
    <c:set var="indexclasses" value="${indexclasses}${sessionScope['option-onlyvalidated'] ? (' filter_onlyvalidated') : ''}" />
    <c:set var="indexclasses" value="${indexclasses}${sessionScope['option-onlyneedscorrections'] ? (' filter_onlyneedscorrections') : ''}" />
    <c:set var="indexclasses" value="${indexclasses}${sessionScope['option-onlyapproved'] ? (' filter_onlyapproved') : ''}" />
    <c:set var="indexclasses" value="${indexclasses}${sessionScope['option-onlynative'] ? (' filter_onlynative') : ''}" />
    <c:set var="indexclasses" value="${indexclasses}${sessionScope['option-onlyassessed'] ? (' filter_onlyassessed') : ''}" />
    <c:set var="indexclasses" value="${indexclasses}${sessionScope['option-onlyvalidationerror'] ? (' filter_onlyvalidationerror') : ''}" />
    <c:set var="indexclasses" value="${indexclasses}${sessionScope['option-onlypublished'] ? (' filter_onlypublished') : ''}" />
    <table id="speciesindex" class="sortable selectable smalltext${indexclasses}">
        <thead>
            <tr>
            <c:if test="${user.canEDIT_ANY_FIELD()}">
                <th class="sorttable_nosort"></th>
            </c:if>
                <th>Taxon</th><!-- <th>Native Status</th>-->
            <c:if test="${user.canVIEW_FULL_SHEET()}">
                <th>Responsible for texts</th>
            </c:if>
            <c:if test="${user.canMANAGE_REDLIST_USERS()}">
                <th>Responsible for assessment</th>
                <th>Responsible for revision</th>
            </c:if>
            <c:if test="${user.canVIEW_FULL_SHEET()}">
                <th>Text authors</th>
                <th>Assessor</th>
                <th>Reviewer</th>
            </c:if>
                <th>Assessment status</th>
                <th>Category</th>
                <th>Criteria</th>
            </tr>
        </thead>
        <tbody>
        <c:forEach var="taxon" items="${specieslist}">
            <c:set var="taxonclasses" value=""/>
            <c:if test="${taxon.getTaxEnt().isSpecies()}">
                <c:set var="taxonclasses" value="${taxonclasses} species"/>
            </c:if>
            <c:if test="${taxon.getResponsibleAuthors_Texts().contains(user.getID())}">
                <c:set var="taxonclasses" value="${taxonclasses} responsible"/>
            </c:if>
            <c:if test="${taxon.getAssessment().containsAuthor(user.getID())}">
                <c:set var="taxonclasses" value="${taxonclasses} author"/>
            </c:if>
            <c:if test="${taxon.getResponsibleAuthors_Assessment().contains(user.getID()) || taxon.getAssessment().containsEvaluator(user.getID())}">
                <c:set var="taxonclasses" value="${taxonclasses} assessor"/>
            </c:if>
            <c:if test="${taxon.getResponsibleAuthors_Revision().contains(user.getID()) || taxon.getAssessment().containsReviewer(user.getID())}">
                <c:set var="taxonclasses" value="${taxonclasses} reviewer"/>
            </c:if>
            <c:if test="${taxon.getInferredStatus() != null && taxon.getInferredStatus().getNativeStatus().isNative()}">
                <c:set var="taxonclasses" value="${taxonclasses} native"/>
            </c:if>
            <c:if test="${taxon.getAssessment().getPublicationStatus().isPublished()}">
                <c:set var="taxonclasses" value="${taxonclasses} published"/>
            </c:if>
            <c:if test="${taxon.getAssessment().getAssessmentStatus().isAssessed()}">
                <c:set var="taxonclasses" value="${taxonclasses} assessed"/>
            </c:if>
            <c:if test="${taxon.validateCriteria().size() > 0}">
                <c:set var="taxonclasses" value="${taxonclasses} validationerror"/>
            </c:if>
            <c:if test="${taxon.getAssessment().getReviewStatus() == 'REVISED_WORKING' || taxon.getAssessment().getReviewStatus() == 'REVISED_MAJOR'}">
                <c:set var="taxonclasses" value="${taxonclasses} revised"/>
            </c:if>
            <c:if test="${taxon.getAssessment().getAssessmentStatus() == 'READY_REASSESSMENT' && taxon.getAssessment().getTextStatus() == 'REVISION_READY'}">
                <c:set var="taxonclasses" value="${taxonclasses} reassessment"/>
            </c:if>
            <c:if test="${taxon.getAssessment().getPublicationStatus().isApproved() && taxon.getAssessment().getValidationStatus() == 'IN_ANALYSIS'}">
                <c:set var="taxonclasses" value="${taxonclasses} approved"/>
            </c:if>
            <c:if test="${taxon.getAssessment().getValidationStatus() == 'VALIDATED'}">
                <c:set var="taxonclasses" value="${taxonclasses} validated"/>
            </c:if>
            <c:if test="${taxon.getAssessment().getValidationStatus() == 'NEEDS_CORRECTIONS'}">
                <c:set var="taxonclasses" value="${taxonclasses} needscorrections"/>
            </c:if>
            <c:forEach var="tmp" items="${taxon._getHTMLEscapedTags()}">
                <c:set var="taxonclasses" value="${taxonclasses} tag_${tmp}"/>
            </c:forEach>

                <tr class="${taxonclasses}">
                <c:if test="${user.canEDIT_ANY_FIELD()}">
                    <td>
                        <input type="checkbox" name="id" value="${taxon.getTaxEnt().getID()}" class="selectionbox" id="selbox_${taxon.getTaxEnt()._getIDURLEncoded()}"/>
                        <label for="selbox_${taxon.getTaxEnt()._getIDURLEncoded()}">${taxon._getSingleLetterTag()}</label>
                    </td>
                </c:if>
                    <td class="taxonname">
                        <%-- <c:if test="${taxon.getAssessment().getJustification().getLength() > 1700}"><img class="exclamation" src="../images/exclamation.png"/></c:if> --%>
                        <c:if test="${taxon.validateCriteria().size() > 0}"><img class="exclamation" src="../images/exclamation.png"/> <span class="warning" title="${taxon.validateCriteria().size()} warnings/errors">${taxon.validateCriteria().size()}</span></c:if>
                        <c:if test="${rls.isEditionLocked(taxon) && rls.isSheetUnlocked(taxon.getTaxEnt().getID())}"><img class="exclamation" src="../images/unlocked.png"/></c:if>
                        <a href="?w=taxon&id=${taxon.getTaxEnt()._getIDURLEncoded()}">${taxon.getTaxEnt().getNameWithAnnotationOnly(true)}</a>
                    </td>
                <%--
                    <td>
                    <c:if test="${taxon.getInferredStatus() != null}">
                        ${taxon.getInferredStatus().getStatusSummary()}
                    </c:if>
                    <c:if test="${taxon.getInferredStatus() == null}">
                        NONEXISTENT
                    </c:if>
                    </td>
                --%>
                <c:if test="${user.canVIEW_FULL_SHEET()}">
                    <td><c:forEach var="ra" items="${taxon.getResponsibleAuthors_Texts()}">${userMap.get(ra)}<br/></c:forEach></td>
                </c:if>
                <c:if test="${user.canMANAGE_REDLIST_USERS()}">
                    <td><c:forEach var="ra" items="${taxon.getResponsibleAuthors_Assessment()}">${userMap.get(ra)}<br/></c:forEach></td>
                    <td><c:forEach var="ra" items="${taxon.getResponsibleAuthors_Revision()}">${userMap.get(ra)}<br/></c:forEach></td>
                </c:if>
                <c:if test="${user.canVIEW_FULL_SHEET()}">
                    <td><c:forEach var="eval" items="${taxon.getAssessment().getAuthors()}">${userMap.get(eval)} </c:forEach></td>
                    <td><c:forEach var="eval" items="${taxon.getAssessment().getEvaluator()}">${userMap.get(eval)} </c:forEach></td>
                    <td><c:forEach var="eval" items="${taxon.getAssessment().getReviewer()}">${userMap.get(eval)} </c:forEach></td>
                </c:if>
                    <td>
                    <c:if test="${taxon.getAssessment().fetchSequentialAssessmentStatus() != null}">
                        <fmt:message key="${taxon.getAssessment().fetchSequentialAssessmentStatus()[0]}"/><br/>
                        <fmt:message key="${taxon.getAssessment().fetchSequentialAssessmentStatus()[1]}"/>
                    </c:if>
                    </td>
                <td>
                <c:if test="${taxon.getAssessment().getCategory() != null}">
                    <div class="redlistcategory assess_${taxon.getAssessment().getFinalCategory().getEffectiveCategory().toString()}"><h1>
                        ${taxon.getAssessment().getFinalCategory().getShortTag()}
                        <c:if test="${taxon.getAssessment().getCategory().toString().equals('CR') && !taxon.getAssessment().getSubCategory().toString().equals('NO_TAG')}"><sup>${taxon.getAssessment().getSubCategory().toString()}</sup></c:if>
                    </h1></div>
                </c:if>
                </td>
                <td>${taxon.getAssessment()._getCriteriaAsString()}</td>
            </tr>
        </c:forEach>
        </tbody>
    </table>
</form>
</c:if>