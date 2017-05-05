<%@ tag description="Occurrence table header" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ attribute name="flavour" required="false" %>

<thead>
    <tr>
        <th class="sorttable_nosort selectcol"></th>
        <c:choose>
        <c:when test="${flavour == null || flavour == '' || flavour == 'simple'}">
        <th class="bigcol">Taxon</th>
        <th class="smallcol">Conf</th>
        <th class="smallcol">Coord</th>
        <th class="smallcol">Preci</th>
        <th class="bigcol">Notas pub</th>
        <th class="bigcol">Notas priv</th>
        <th class="smallcol">Date</th>
        <th class="smallcol">Phen</th>
        <th class="smallcol">Observer</th>
        </c:when>

        <c:when test="${flavour == 'redlist'}">
        <th class="smallcol">Date</th>
        <th class="smallcol">Observer</th>
        <th class="smallcol">Coord</th>
        <th class="smallcol">Preci</th>
        <th class="smallcol">GPS</th>
        <th class="bigcol">Taxon</th>
        <th class="smallcol">Conf</th>
        <th class="smallcol">Fen</th>
        <th class="smallcol">Nº</th>
        <th class="smallcol">Met</th>
        <th class="smallcol">Foto</th>
        <th class="smallcol">Colh</th>
        <th class="bigcol">Ameaças</th>
        <th class="bigcol">Notas esp</th>
        </c:when>

        <c:when test="${flavour == 'herbarium'}">
        <th class="bigcol">Taxon</th>
        <th class="smallcol">Local</th>
        <th class="smallcol">Coord</th>
        <th class="bigcol">Label</th>
        <th class="smallcol">Date</th>
        <th class="smallcol">Collectors</th>
        <th class="smallcol">Dets</th>
        </c:when>

        </c:choose>

    </tr>
</thead>
