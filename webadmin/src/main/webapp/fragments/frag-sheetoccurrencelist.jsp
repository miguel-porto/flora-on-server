<%@ page pageEncoding="UTF-8" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page session="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<fmt:setBundle basename="pt.floraon.redlistdata.fieldValues" />
<c:if test="${!user.canVIEW_OCCURRENCES()}">
    <div class="warning"><b>You&#8217;re not authorized to enter this page</b></div>
</c:if>

<c:if test="${user.canVIEW_OCCURRENCES()}">
    <a href="?w=taxon&id=${taxon._getIDURLEncoded()}">&lt;&lt; voltar à ficha</a>
    <h1>${taxon.getFullName(true)}</h1>
    <c:if test="${occurrences == null}">
        <div class="warning"><p><fmt:message key="DataSheet.msg.warning"/></p>This taxon has no correspondence in Flora-On, please contact the checklist administrator</div>
    </c:if>
    <h2>${occurrences.size()} occurrences</h2>

    <c:set var="pgroup" value="${param.group==null ? '' : (param.group==500 ? '&group=500' : '&group=2500')}" />
    <c:set var="pview" value="${param.view==null ? '' : '&view=all'}" />

    <c:if test="${user.canDOWNLOAD_OCCURRENCES() || user.hasEDIT_ALL_1_8()}">
        <div class="button anchorbutton"><a href="?w=downloadtaxonrecords&id=${taxon._getIDURLEncoded()}${pview}"><fmt:message key="button.1" /></a></div>
    </c:if>

    <c:if test="${param.view == 'all'}"><div class="button anchorbutton selected"><a href="?w=taxonrecords&id=${taxon._getIDURLEncoded()}${pgroup}"><fmt:message key="button.4" /></a></div></c:if>
    <c:if test="${param.view == null}"><div class="button anchorbutton"><a href="?w=taxonrecords&id=${taxon._getIDURLEncoded()}${pgroup}&view=all"><fmt:message key="button.4" /></a></div></c:if>
    <div class="button" id="unselectrecords"><fmt:message key="button.5" /></div>
    <div class="button-section">
        <fmt:message key="button.2" /><br/>
        <div class="button anchorbutton ${param.group==null ? 'selected' : ''}"><a href="?w=taxonrecords&id=${taxon._getIDURLEncoded()}${pview}"><fmt:message key="button.3" /></a></div>
        <div class="button anchorbutton ${param.group==500 ? 'selected' : ''}"><a href="?w=taxonrecords&id=${taxon._getIDURLEncoded()}&group=500${pview}">&lt; 500m</a></div>
        <div class="button anchorbutton ${param.group==2500 ? 'selected' : ''}"><a href="?w=taxonrecords&id=${taxon._getIDURLEncoded()}&group=2500${pview}">&lt; 2500m</a></div>
    </div>
    <c:if test="${param.view == 'all'}"><p><fmt:message key="button.4a" /></p></c:if>
    <c:if test="${param.view == null}"><p><fmt:message key="button.4b" /></p></c:if>
    <c:if test="${param.group == 500}"><p><fmt:message key="button.2a" /></p></c:if>
    <c:if test="${param.group == 2500}"><p><fmt:message key="button.2b" /></p></c:if>
    <c:if test="${param.group > 0}"><p>${clustoccurrences.size()} grupos.</p></c:if>
    <table id="taxonrecordtable" class="smalltext ${param.group==null ? 'sortable' : ''}" id="recordtable">
        <thead>
            <tr><th>Taxon</th><c:if test="${user.canDOWNLOAD_OCCURRENCES()}"><th>Latitude</th><th>Longitude</th></c:if><th>Date</th>
            <th>Author</th><th style="width:180px">Notes</th><th>Locality</th><th>Precision</th><th>Confid</th><th>Pheno</th>
            <th>Ameaças</th><th>Nº ind</th><th>Met</th><th>Fot</th><th>Colh</th><th>Excl</th><th>CodHerb</th>
            </tr>
        </thead>
        <c:if test="${param.group > 0}">
        <c:forEach var="entr" items="${clustoccurrences.iterator()}">
            <tr><td class="separator" colspan="17">&nbsp;</td></tr>
            <c:forEach var="occ" items="${entr.getValue()}">
            <tr data-mgrs="${occ._getMGRSString(2000)}">
                <!--<td>${occ.getDataSource()}</td>-->
                <td><i>${occ.getOccurrence().getVerbTaxon()}</i></td>
                <c:if test="${user.canDOWNLOAD_OCCURRENCES()}">
                <td><fmt:formatNumber value="${occ._getLatitude()}" maxFractionDigits="4"/></td>
                <td><fmt:formatNumber value="${occ._getLongitude()}" maxFractionDigits="4"/></td>
                </c:if>
                <td sorttable_customkey="${occ._getDateYMD()}">${occ._getDate()}</td>
                <td>${occ._getObserverNames()[0]}</td>
                <td style="width:180px">${occ.getOccurrence().getComment()}</td>
                <td>${occ.getLocality()} ${occ.getVerbLocality()}</td>
                <td>${occ.getPrecision()}</td>
                <td>${occ.getOccurrence().getConfidence()}</td>
                <td>${occ.getOccurrence().getPhenoState()}</td>
                <td>${occ.getThreats()} ${occ.getOccurrence().getSpecificThreats()}</td>
                <td>${occ.getOccurrence().getAbundance()}</td>
                <td>${occ.getOccurrence().getTypeOfEstimate()}</td>
                <td>${occ.getOccurrence().getHasPhoto().getLabel()}</td>
                <td>${occ.getOccurrence().getHasSpecimen()}</td>
                <td>${occ.getOccurrence()._getPresenceStatusLabel()}</td>
                <td>${occ.getOccurrence().getAccession()}</td>
            </tr>
            </c:forEach>
        </c:forEach>
        </c:if>
        <c:if test="${param.group == null}">
        <c:forEach var="occ" items="${occurrences.iterator()}">
            <tr data-mgrs="${occ._getMGRSString(2000)}">
                <!--<td>${occ.getDataSource()}</td>-->
                <td><i>${occ.getOccurrence().getVerbTaxon()}</i></td>
                <c:if test="${user.canDOWNLOAD_OCCURRENCES()}">
                <td><fmt:formatNumber value="${occ._getLatitude()}" maxFractionDigits="4"/></td>
                <td><fmt:formatNumber value="${occ._getLongitude()}" maxFractionDigits="4"/></td>
                </c:if>
                <td sorttable_customkey="${occ._getDateYMD()}">${occ._getDate()}</td>
                <td>${occ._getObserverNames()[0]}</td>
                <td style="width:180px">${occ.getOccurrence().getComment()}</td>
                <td>${occ.getLocality()} ${occ.getVerbLocality()}</td>
                <td>${occ.getPrecision()}</td>
                <td>${occ.getOccurrence().getConfidence()}</td>
                <td>${occ.getOccurrence().getPhenoState()}</td>
                <td>${occ.getThreats()} ${occ.getOccurrence().getSpecificThreats()}</td>
                <td>${occ.getOccurrence().getAbundance()}</td>
                <td>${occ.getOccurrence().getTypeOfEstimate()}</td>
                <td>${occ.getOccurrence().getHasPhoto().getLabel()}</td>
                <td>${occ.getOccurrence().getHasSpecimen()}</td>
                <td>${occ.getOccurrence()._getPresenceStatusLabel()}</td>
                <td>${occ.getOccurrence().getAccession()}</td>
            </tr>
        </c:forEach>
        </c:if>
    </table>
    <t:ajaxloadhtml url="../api/svgmap?basemap=1&size=2000&view=${param.view}&pa=true&border=0&taxon=${taxon._getIDURLEncoded()}" width="150px" height="100px" text="carregando mapa" id="taxonrecords-map"/>
    <%-- <t:ajaxloadhtml url="http://localhost:8080/api/svgmap?basemap=1&size=2000&view=${param.view}&border=0&taxon=${taxon._getIDURLEncoded()}" width="150px" height="100px" text="carregando mapa" id="taxonrecords-map"/> --%>
</c:if>
