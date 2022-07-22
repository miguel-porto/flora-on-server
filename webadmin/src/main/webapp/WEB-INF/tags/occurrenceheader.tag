<%@ tag description="Occurrence table header" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ attribute name="fields" required="true" type="pt.floraon.occurrences.fields.flavours.IOccurrenceFlavour" %>
<%@ attribute name="view" required="true" type="java.lang.String" %>
<%@ attribute name="noSortButton" required="false" type="java.lang.Boolean" %>

<jsp:useBean id="collapseField" class="java.util.HashMap"/>
<c:forEach var="flf" items="${fields.getFields()}">
    <t:isoptionselected optionname="collapse-${flf}" value="true"><c:set target="${collapseField}" property="${flf}" value="collapsed"/></t:isoptionselected>
</c:forEach>
<th class="sorttable_nosort selectcol clickable"><div class="selectbutton"></div></th>
<c:if test="${view == 'inventory'}">    <%-- in the inventory view, we always have coordinates hidden --%>
<th class="sorttable_nosort hidden"></th>
</c:if>
<c:if test="${view == 'inventorySummary'}">   <%-- in the inventory summary view, we have a link to open the inventory --%>
<th class="sorttable_nosort tinycol">inv</th>
</c:if>

<c:forEach var="field" items="${fields.getFields()}">
<t:isoptionselected optionname="collapse-${field}" value="true"><c:set var="collapsed" value="collapsed"/></t:isoptionselected>
<t:isoptionselected optionname="collapse-${field}" value="false"><c:set var="collapsed" value=""/></t:isoptionselected>

<c:choose>
<c:when test="${field == 'verbLocality' && view != 'inventory'}"><th class="smallcol ${collapsed}"><t:optionbutton optionname="collapse-${field}" title="ex" style="content" classes="expandbutton" norefresh="true"></t:optionbutton>verbLocal</th></c:when>

<c:otherwise>
<c:if test="${(!fields.isAdminField(field) || user.canMODIFY_OCCURRENCES() || user.canMANAGE_EXTERNAL_DATA()) && (field == 'taxa' || (fields.isInventoryField(field) && view != 'inventory') || (!fields.isInventoryField(field) && view != 'inventorySummary'))}">
<c:url value="" var="url">
    <c:param name="w" value="${param.w}" />
    <c:param name="p" value="${param.p}" />
    <c:param name="filter" value="${filter}" />
</c:url>
<c:set var="sortedCol" value="${view != 'inventory' && !noSortButton && (occurrenceOrder == field || occurrenceOrder == field.concat('_d')) ? 'sorted' : ''}"/>
<th class="fieldsize_${fields.getFieldSize(field)} ${sortedCol} ${collapsed} ${fields.hideFieldInCompactView(field) ? 'hideincompactview' : ''} ${fields.isInventoryField(field) ? 'inventoryfield' : 'occurrencefield'}"
    title="${fields.getFieldName(field)}" data-field="${field}">
    <t:optionbutton optionname="collapse-${field}" title="ex" style="content" classes="expandbutton" norefresh="true"></t:optionbutton>
    <c:set var="butsel" value="${occurrenceOrder == field ? 'selected' : ''}"/>
    <c:if test="${view != 'inventory' && !noSortButton}">
    <c:choose>
    <c:when test="${occurrenceOrder == field}"><div class="anchorbutton sortbutton button selected"><a href="${url}&order=${field}_d">&blacktriangle;</a></div></c:when>
    <c:when test="${occurrenceOrder == field.concat('_d')}"><div class="anchorbutton sortbutton button selected"><a href="${url}&order=">&blacktriangledown;</a></div></c:when>
    <c:otherwise><div class="anchorbutton sortbutton button"><a href="${url}&order=${field}">&blacktriangle;</a></div></c:otherwise>
    </c:choose>
    </c:if>
    ${fields.getFieldShortName(field)}
</th>
</c:if>
</c:otherwise>

</c:choose>
</c:forEach>
