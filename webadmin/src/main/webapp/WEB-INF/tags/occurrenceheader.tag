<%@ tag description="Occurrence table header" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ attribute name="fields" required="true" type="pt.floraon.occurrences.fields.flavours.IOccurrenceFlavour" %>
<%@ attribute name="noInventory" required="false" type="java.lang.Boolean" %>
<%@ attribute name="noOccurrence" required="false" type="java.lang.Boolean" %>

<jsp:useBean id="collapseField" class="java.util.HashMap"/>
<c:forEach var="flf" items="${fields.getFields()}">
    <t:isoptionselected optionname="collapse-${flf}" value="true"><c:set target="${collapseField}" property="${flf}" value="collapsed"/></t:isoptionselected>
</c:forEach>
<th class="sorttable_nosort selectcol clickable"><div class="selectbutton"></div></th>
<c:if test="${noInventory}">    <%-- in the inventory view, we always have coordinates hidden --%>
<th class="sorttable_nosort hidden"></th>
</c:if>
<c:forEach var="field" items="${fields.getFields()}">
<t:isoptionselected optionname="collapse-${field}" value="true"><c:set var="collapsed" value="collapsed"/></t:isoptionselected>
<t:isoptionselected optionname="collapse-${field}" value="false"><c:set var="collapsed" value=""/></t:isoptionselected>

<c:choose>
<%--
<c:when test="${field == 'comment'}">           <th class="bigcol   ${collapsed}"><t:optionbutton optionname="collapse-${field}" title="ex" style="content" classes="expandbutton" norefresh="true"></t:optionbutton>Notas pub</th></c:when>
<c:when test="${field == 'specificThreats'}">   <th class="bigcol   ${collapsed}"><t:optionbutton optionname="collapse-${field}" title="ex" style="content" classes="expandbutton" norefresh="true"></t:optionbutton>Ameaças esp</th></c:when>
<c:when test="${field == 'privateComment'}">    <th class="bigcol   ${collapsed}"><t:optionbutton optionname="collapse-${field}" title="ex" style="content" classes="expandbutton" norefresh="true"></t:optionbutton>Notas priv</th></c:when>
<c:when test="${field == 'gpsCode'}">           <th class="smallcol ${collapsed}"><t:optionbutton optionname="collapse-${field}" title="ex" style="content" classes="expandbutton" norefresh="true"></t:optionbutton>GPS</th></c:when>
<c:when test="${field == 'accession'}">         <th class="smallcol ${collapsed}"><t:optionbutton optionname="collapse-${field}" title="ex" style="content" classes="expandbutton" norefresh="true"></t:optionbutton>Cod Herb</th></c:when>
<c:when test="${field == 'labelData'}">         <th class="smallcol ${collapsed}"><t:optionbutton optionname="collapse-${field}" title="ex" style="content" classes="expandbutton" norefresh="true"></t:optionbutton>Label</th></c:when>
<c:when test="${field == 'confidence'}">        <th class="smallcol ${collapsed} hideincompactview"><t:optionbutton optionname="collapse-${field}" title="ex" style="content" classes="expandbutton" norefresh="true"></t:optionbutton>Conf</th></c:when>
<c:when test="${field == 'phenoState'}">        <th class="smallcol ${collapsed} hideincompactview"><t:optionbutton optionname="collapse-${field}" title="ex" style="content" classes="expandbutton" norefresh="true"></t:optionbutton>Phen</th></c:when>
<c:when test="${field == 'presenceStatus'}">    <th class="smallcol ${collapsed} hideincompactview"><t:optionbutton optionname="collapse-${field}" title="ex" style="content" classes="expandbutton" norefresh="true"></t:optionbutton>Excl</th></c:when>
<c:when test="${field == 'typeOfEstimate'}">    <th class="smallcol ${collapsed} hideincompactview"><t:optionbutton optionname="collapse-${field}" title="ex" style="content" classes="expandbutton" norefresh="true"></t:optionbutton>Met</th></c:when>
<c:when test="${field == 'coverIndex'}">        <th class="smallcol ${collapsed} hideincompactview">Cover</th></c:when>
<c:when test="${field == 'abundance'}">         <th class="smallcol ${collapsed} hideincompactview">Nº</th></c:when>
<c:when test="${field == 'hasPhoto'}">          <th class="smallcol ${collapsed} hideincompactview">Foto</th></c:when>
<c:when test="${field == 'hasSpecimen'}">       <th class="smallcol ${collapsed} hideincompactview">Colh</th></c:when>

<c:when test="${field == 'precision' && !noInventory}"> <th class="smallcol ${collapsed}"><t:optionbutton optionname="collapse-${field}" title="ex" style="content" classes="expandbutton" norefresh="true"></t:optionbutton>Prec</th></c:when>
<c:when test="${field == 'locality' && !noInventory}">  <th class="smallcol ${collapsed}"><t:optionbutton optionname="collapse-${field}" title="ex" style="content" classes="expandbutton" norefresh="true"></t:optionbutton>Local</th></c:when>
<c:when test="${field == 'taxa'}">           <th class="bigcol ${collapsed}"><t:optionbutton optionname="collapse-${field}" title="ex" style="content" classes="expandbutton" norefresh="true"></t:optionbutton>Taxon</th></c:when>
<c:when test="${field == 'observers' && !noInventory}"> <th class="smallcol ${collapsed} hideincompactview"><t:optionbutton optionname="collapse-${field}" title="ex" style="content" classes="expandbutton" norefresh="true"></t:optionbutton>Observers</th></c:when>
<c:when test="${field == 'collectors' && !noInventory}"><th class="smallcol ${collapsed} hideincompactview"><t:optionbutton optionname="collapse-${field}" title="ex" style="content" classes="expandbutton" norefresh="true"></t:optionbutton>Collectors</th></c:when>
<c:when test="${field == 'coordinates' && !noInventory}"><th class="smallcol ${collapsed} hideincompactview"><t:optionbutton optionname="collapse-${field}" title="ex" style="content" classes="expandbutton" norefresh="true"></t:optionbutton>Coord</th></c:when>
<c:when test="${field == 'date' && !noInventory}">      <th class="smallcol ${collapsed}"><t:optionbutton optionname="collapse-${field}" title="ex" style="content" classes="expandbutton" norefresh="true"></t:optionbutton>Data</th></c:when>
<c:when test="${field == 'gpsCode_accession' && !noInventory}"><th class="smallcol ${collapsed}"><t:optionbutton optionname="collapse-${field}" title="ex" style="content" classes="expandbutton" norefresh="true"></t:optionbutton>Code</th></c:when>
<c:when test="${field == 'locality_verbLocality' && !noInventory}"><th class="smallcol ${collapsed}"><t:optionbutton optionname="collapse-${field}" title="ex" style="content" classes="expandbutton" norefresh="true"></t:optionbutton>Local</th></c:when>
<c:when test="${field == 'observers_collectors' && !noInventory}"><th class="smallcol ${collapsed}"><t:optionbutton optionname="collapse-${field}" title="ex" style="content" classes="expandbutton" norefresh="true"></t:optionbutton>Auth</th></c:when>
<c:when test="${field == 'comment_labelData'}"><th class="bigcol ${collapsed}"><t:optionbutton optionname="collapse-${field}" title="ex" style="content" classes="expandbutton" norefresh="true"></t:optionbutton>Notas</th></c:when>
--%>
<c:when test="${field == 'verbLocality' && !noInventory}"><th class="smallcol ${collapsed}"><t:optionbutton optionname="collapse-${field}" title="ex" style="content" classes="expandbutton" norefresh="true"></t:optionbutton>verbLocal</th></c:when>

<c:otherwise>
<%--<c:if test="${!(fields.isInventoryField(field) && noInventory)}">--%>
<c:if test="${(fields.isInventoryField(field) && !noInventory) || (!fields.isInventoryField(field) && !noOccurrence)}">
<th class="${fields.isSmallField(field) ? 'smallcol' : 'bigcol'} ${collapsed} ${fields.hideFieldInCompactView(field) ? 'hideincompactview' : ''} ${fields.isInventoryField(field) ? 'inventoryfield' : 'occurrencefield'}"
    title="${fields.getFieldName(field)}">
    <t:optionbutton optionname="collapse-${field}" title="ex" style="content" classes="expandbutton" norefresh="true"></t:optionbutton>
    ${fields.getFieldShortName(field)}
</th>
</c:if>
</c:otherwise>

</c:choose>
</c:forEach>
