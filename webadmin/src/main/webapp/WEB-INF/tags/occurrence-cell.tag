<%@ tag description="Occurrence table cell" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ attribute name="inventory" required="false" type="pt.floraon.occurrences.entities.Inventory"%>
<%@ attribute name="taxon" required="false" type="pt.floraon.occurrences.entities.OBSERVED_IN"%>
<%@ attribute name="field" required="true"%>
<%@ attribute name="userMap" required="false" type="java.util.Map" %>
<%@ attribute name="locked" required="false" type="java.lang.Boolean" %>
<%@ attribute name="collapsed" required="false" type="java.lang.Boolean" %>
<%@ attribute name="symbol" required="false" %>
<%@ attribute name="fields" required="true" type="pt.floraon.occurrences.fields.flavours.IOccurrenceFlavour" %>
<%@ attribute name="view" required="true" type="java.lang.String" %>
<jsp:useBean id="rand" class="pt.floraon.driver.utils.StringUtils" scope="application" />
<jsp:useBean id="now" class="java.util.Date" />
<%--
    This tag translates a field name into a table cell
    It is the core functionality of the occurrence manager. If occ is provided, the cell is properly filled in.
--%>
<c:set var="taxon" value="${taxon == null ? (inventory == null ? null : (inventory._getTaxa()[0])) : taxon}" />
<t:isoptionselected optionname="advancedview"><c:set var="advancedview" value="${true}" /></t:isoptionselected>
<t:isoptionselected optionname="advancedview" value="false"><c:set var="advancedview" value="${false}" /></t:isoptionselected>

<c:if test="${taxon != null}">
<c:set var="editable" value="${locked ? '' : 'editable'}"/>
<c:set var="collapsedClass" value="${collapsed ? 'collapsed' : ''}"/>
<c:set var="symbol" value="${((symbol == null || symbol == '') && inventory != null) ? (inventory.getYear() != null && inventory.getYear() >= historicalYear ? 0 : 1) : symbol}"/>
</c:if>

<c:if test="${taxon == null}">
<c:set var="editable" value="editable"/>
<c:set var="collapsedClass" value="${collapsed ? 'collapsed' : ''}"/>
</c:if>

<c:set var="thisfieldeditable" value="${fields.isAdminField(field) && (user.canMODIFY_OCCURRENCES() || user.canMANAGE_EXTERNAL_DATA()) ? 'editable' : ((fields.isReadOnly(field, user.canMODIFY_OCCURRENCES()) || inventory.getReadOnly()) ? '' : editable)}"/>
<c:set var="monospace" value="${fields.isMonospaceFont(field) ? 'monospace' : ''}"/>
<c:set var="breakLines" value="${fields.breakLines(field) ? '' : 'no-space-break'}"/>
<c:set var="multiline" value="${fields.isBigEditWidget(field) ? 'multiline' : ''}"/>

<c:choose>
<%--    SPECIAL FIELDS      --%>
<c:when test="${field == 'taxa'}">
<c:if test="${view != 'inventorySummary'}"><c:set var="taxa" value="${taxon == null ? '' : (taxon.getTaxEnt() == null ? taxon.getVerbTaxon() : taxon.getTaxEnt().getNameWithAnnotationOnly(false))}" /><td class="${thisfieldeditable} ${collapsedClass} ${multiline} ${monospace} ${breakLines} taxon" data-name="taxa">${taxa}</td></c:if>
<c:if test="${view == 'inventorySummary'}"><td class="${collapsedClass} ${multiline} ${monospace} ${breakLines} taxon"><a href="?w=openinventory&id=${inventory._getIDURLEncoded()}"><c:if test="${inventory._hasDuplicatedTaxa()}"><span class="warning">duplicated taxa</span> </c:if>${inventory._getSampleTaxa(100, true, true)}</a></td></c:if>
</c:when>
<c:when test="${field == 'coordinates'}">
    <c:set var="coordchanged" value="${taxon == null ? '' : (taxon.getCoordinatesChanged() ? 'textemphasis' : '')}" />
    <c:if test="${view == 'occurrence'}">
        <td class="${thisfieldeditable} ${collapsedClass} ${multiline} ${monospace} ${breakLines} ${coordchanged} coordinates" data-name="observationCoordinates" data-lat="${inventory._getLatitude()}" data-lng="${inventory._getLongitude()}" data-symbol="${symbol}">${taxon == null ? '' : inventory._getCoordinates()}</td>
    </c:if>
    <c:if test="${view == 'inventorySummary'}">
        <td class="${thisfieldeditable} ${collapsedClass} ${multiline} ${monospace} ${breakLines} ${coordchanged} coordinates" data-name="inventoryCoordinates" data-lat="${inventory._getInventoryLatitude()}" data-lng="${inventory._getInventoryLongitude()}" data-symbol="${symbol}">${taxon == null ? '' : inventory._getInventoryCoordinates()}</td>
    </c:if>
</c:when>
<c:when test="${field == 'inventoryCoordinates' && view != 'inventory'}"><td class="${thisfieldeditable} ${collapsedClass} ${multiline} ${monospace} ${breakLines} ${coordchanged} coordinates" data-name="inventoryCoordinates" data-lat="${inventory.getLatitude()}" data-lng="${inventory.getLongitude()}" data-symbol="${symbol}">${taxon == null ? '' : inventory._getInventoryCoordinates()}</td></c:when>
<%--<c:when test="${field == 'date' && view != 'inventory'}"><td class="${thisfieldeditable} ${collapsedClass} ${multiline} ${monospace} ${breakLines}" data-name="date" sorttable_customkey="${inventory._getDateYMD()}">${inventory == null ? '' : inventory._getDate()}</td></c:when>--%>
<c:when test="${field == 'date' && view != 'inventory'}"><td class="${thisfieldeditable} ${collapsedClass} ${multiline} ${monospace} ${breakLines}" data-name="date" sorttable_customkey="${inventory._getDateYMD()}">
<c:if test="${inventory == null}">
    <c:if test="${!advancedview}"><fmt:formatDate var="today" value="${now}" pattern="yyyy-MM-dd" /><input type="date" value="${today}"/></c:if>
    <c:if test="${advancedview}"><fmt:formatDate var="today" value="${now}" pattern="dd-MM-yyyy" />${today}</c:if>
</c:if>
<c:if test="${inventory != null}">
    <c:if test="${advancedview}">${inventory._getDate()}</c:if>
    <c:if test="${!advancedview}">
        <c:if test="${inventory._isDateOnly()}"><input type="date" value="${inventory._getDateYMDForHtml()}"/></c:if>
        <c:if test="${!inventory._isDateOnly()}">
            <c:if test="${inventory._isDateEmpty()}"><input type="date" /></c:if>
            <c:if test="${!inventory._isDateEmpty()}">${inventory._getDate()}</c:if>
        </c:if>
    </c:if>
</c:if>
</td></c:when>
<c:when test="${field == 'time' && view != 'inventory'}"><td class="${thisfieldeditable} ${collapsedClass} ${multiline} ${monospace} ${breakLines}" data-name="time">${inventory == null ? '' : inventory._getTime()}</td></c:when>
<c:when test="${field == 'tags' && view != 'inventory'}"><td class="${thisfieldeditable} ${collapsedClass} ${multiline} ${monospace} ${breakLines}" data-name="tags"><t:usernames idarray="${inventory == null ? null : inventory.getTags()}" showAsTags="true"/></td></c:when>
<c:when test="${field == 'verbLocality' && view != 'inventory'}"><td class="${collapsedClass} ${multiline} ${monospace} ${breakLines}" data-name="verbLocality">${inventory == null ? '' : inventory.getVerbLocality()}</td></c:when>
<c:when test="${field == 'maintainer' && view != 'inventory'}"><td class="${fields.isReadOnly(field, user.canMODIFY_OCCURRENCES()) ? '' : thisfieldeditable} ${collapsedClass} ${fields.hideFieldInCompactView(field) ? 'hideincompactview' : ''} authors" data-name="${field}"><t:usernames id="${inventory == null ? null : fields.getFieldValueRaw(taxon, inventory, field)}" usermap="${userMap}"/></td></c:when>

<%-- These are concatenated fields, which are read-only --%>
<c:when test="${field == 'observers_collectors' && view != 'inventory'}"><td class="${collapsedClass} ${multiline} ${monospace} ${breakLines}"><t:usernames idarray="${inventory == null ? null : inventory.getObservers()}" usermap="${userMap}"/> <t:usernames idarray="${inventory == null ? null : inventory.getCollectors()}" usermap="${userMap}"/></td></c:when>

<%--    NORMAL FIELDS      --%>
<c:when test="${fields.isAuthorField(field) && view != 'inventory'}"><td class="${thisfieldeditable} ${collapsedClass} ${fields.hideFieldInCompactView(field) ? 'hideincompactview' : ''} authors" data-name="${field}"><t:usernames idarray="${inventory == null ? null : fields.getFieldValueRaw(taxon, inventory, field)}" usermap="${userMap}"/></td></c:when>
<c:when test="${fields.isImageField(field) && view != 'inventorySummary'}"><td class="${thisfieldeditable} ${collapsedClass} imageupload" data-name="${field}"><c:if test="${taxon != null}"><c:forEach var="image" items="${fields.getFieldValueRaw(taxon, inventory, field)}"><c:if test="${image != null}"><img src="photos/${image}.jpg"/></c:if></c:forEach></c:if></td></c:when>

<c:otherwise>
    <c:if test="${(!fields.isAdminField(field) || user.canMODIFY_OCCURRENCES() || user.canMANAGE_EXTERNAL_DATA()) && ((fields.isInventoryField(field) && view != 'inventory') || (!fields.isInventoryField(field) && view != 'inventorySummary'))}">
        <c:choose>
        <c:when test="${fields.isDateField(field)}">
            <c:if test="${taxon != null}">
                <c:set var="thisdate" value="${fields.getFieldValueRaw(taxon, inventory, field)}"/>
                <c:if test="${thisdate != null}">
                <fmt:formatDate value="${thisdate}" var="formattedDateSortKey" type="date" pattern="yyyy-MM-dd HH:mm:ss" />
                <fmt:formatDate value="${thisdate}" var="formattedDate" type="date" pattern="dd-MM-yyyy HH:mm" />
                </c:if>
                <c:if test="${thisdate == null}">
                <c:set var="formattedDateSortKey" value=""/>
                <c:set var="formattedDate" value=""/>
                </c:if>
            </c:if>
        <td class="${thisfieldeditable} ${collapsedClass} ${multiline} ${monospace} ${breakLines} ${fields.hideFieldInCompactView(field) ? 'hideincompactview' : ''}" data-name="${field}" sorttable_customkey="${formattedDateSortKey}">${formattedDate}</td>
        </c:when>
        <c:when test="${field == 'uri'}">
        <td class="${thisfieldeditable} ${collapsedClass} ${multiline} ${monospace} ${breakLines} ${fields.hideFieldInCompactView(field) ? 'hideincompactview' : ''}" data-name="${field}"><a target="_blank" href="${taxon == null ? '' : fields.getFieldValue(taxon, inventory, field)}">${taxon == null ? '' : fields.getFieldValue(taxon, inventory, field)}</a></td>
        </c:when>
        <c:otherwise>
            <c:choose>
            <c:when test="${fields.getFieldWidget(field, advancedview) == 'TEXT' || fields.getFieldWidget(field, advancedview) == 'BIGTEXT'}">
            <td class="${thisfieldeditable} ${collapsedClass} ${multiline} ${monospace} ${breakLines} ${fields.hideFieldInCompactView(field) ? 'hideincompactview' : ''}" data-name="${field}">${taxon == null ? '' : fields.getFieldValue(taxon, inventory, field)}</td>
            </c:when>

            <c:when test="${fields.getFieldWidget(field, advancedview) == 'DROPDOWN'}">
            <td class="${thisfieldeditable} ${collapsedClass} ${multiline} ${monospace} ${breakLines} ${fields.hideFieldInCompactView(field) ? 'hideincompactview' : ''}" data-name="${field}">
            <c:set var="val" value="${taxon == null ? '' : fields.getFieldValueRaw(taxon, inventory, field)}"/>
            <select name="${field}">
                <c:set var="found" value="${false}" />
                <c:if test="${val == '' || val == null || val == 'NULL'}"><c:set var="found" value="${true}" /><option selected value=""></option></c:if>
                <c:if test="${val != '' && val != null && val != 'NULL'}"><option value=""></option></c:if>
                <c:forEach var="option" items="${fields.getFieldValues(field, advancedview)}" varStatus="loop">
                    <c:set var="label" value="${fields.getFieldLabels(field, advancedview)[loop.index]}"/>
                    <c:if test="${val == option}"><c:set var="found" value="${true}" /><option selected value="${option}">${label}</option></c:if>
                    <c:if test="${val != option}"><option value="${option}">${label}</option></c:if>
                </c:forEach>
                <c:if test="${!found}"><option selected disabled value="">&lt;other&gt;</option></c:if>
            </select>
            </td>
            </c:when>

            <c:when test="${fields.getFieldWidget(field, advancedview) == 'RADIO'}">
            <c:set var="randid" value="${rand.randomString(8)}"/>
            <td class="${thisfieldeditable} ${collapsedClass} ${multiline} ${monospace} ${breakLines} ${fields.hideFieldInCompactView(field) ? 'hideincompactview' : ''}" data-name="${field}">
            <c:set var="val" value="${taxon == null ? '' : fields.getFieldValue(taxon, inventory, field)}"/>
            <c:if test="${val == '' || val == null || val == 'NULL'}"><label><input type="radio" name="${randid}" value="" checked/></label></c:if>
            <c:if test="${val != '' && val != null && val != 'NULL'}"><label><input type="radio" name="${randid}" value=""/></label></c:if>
            <c:forEach var="option" items="${fields.getFieldValues(field, advancedview)}" varStatus="loop">
                <c:set var="label" value="${fields.getFieldLabels(field, advancedview)[loop.index]}"/>
                <c:if test="${val == option}"><label class="no-space-break"><input type="radio" name="${randid}" value="${option}" checked/>${label}</label></c:if>
                <c:if test="${val != option}"><label class="no-space-break"><input type="radio" name="${randid}" value="${option}"/>${label}</label></c:if>
            </c:forEach>
            </td>
            </c:when>
            </c:choose>
        </c:otherwise>
        </c:choose>
    </c:if>
</c:otherwise>
</c:choose>
