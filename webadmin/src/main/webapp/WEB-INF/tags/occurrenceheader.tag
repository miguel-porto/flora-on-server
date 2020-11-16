<%@ tag description="Occurrence table header" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ attribute name="fields" required="true" type="pt.floraon.occurrences.fields.flavours.IOccurrenceFlavour" %>
<%@ attribute name="view" required="true" type="java.lang.String" %>

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
<c:if test="${(!fields.isAdminField(field) || user.canMODIFY_OCCURRENCES()) && (field == 'taxa' || (fields.isInventoryField(field) && view != 'inventory') || (!fields.isInventoryField(field) && view != 'inventorySummary'))}">
<th class="${fields.isSmallField(field) ? 'smallcol' : 'bigcol'} ${collapsed} ${fields.hideFieldInCompactView(field) ? 'hideincompactview' : ''} ${fields.isInventoryField(field) ? 'inventoryfield' : 'occurrencefield'}"
    title="${fields.getFieldName(field)}">
    <t:optionbutton optionname="collapse-${field}" title="ex" style="content" classes="expandbutton" norefresh="true"></t:optionbutton>
    ${fields.getFieldShortName(field)}
</th>
</c:if>
</c:otherwise>

</c:choose>
</c:forEach>
