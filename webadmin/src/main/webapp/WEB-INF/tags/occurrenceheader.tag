<%@ tag description="Occurrence table header" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ attribute name="flavour" required="false" %>

<thead>
    <tr>
        <th class="sorttable_nosort selectcol clickable"><div class="selectbutton"></div></th>
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
        <th class="smallcol hideincompactview">Observer</th>
        <th class="smallcol hideincompactview">Coord</th>
        <th class="smallcol">Local</th>
        <th class="smallcol">Preci</th>
        <th class="smallcol">GPS</th>
        <th class="bigcol">Taxon</th>
        <th class="smallcol hideincompactview">Excl</th>
        <th class="smallcol hideincompactview">Conf</th>
        <th class="smallcol hideincompactview">Fen</th>
        <th class="smallcol hideincompactview">Nº</th>
        <th class="smallcol hideincompactview">Met</th>
        <th class="smallcol hideincompactview">Foto</th>
        <th class="smallcol hideincompactview">Colh</th>
        <th class="bigcol">Ameaças esp</th>
        <th class="bigcol">Notas esp</th>
        <th class="bigcol">Notas priv</th>
        </c:when>

        <c:when test="${flavour == 'herbarium'}">
        <th class="smallcol">Cod.Herb.</th>
        <th class="bigcol">Taxon</th>
        <th class="smallcol">Excl</th>
        <th class="smallcol">Coord</th>
        <th class="smallcol">Preci</th>
        <th class="smallcol">verbLocal</th>
        <th class="smallcol">Date</th>
        <th class="smallcol">Collectors</th>
        <th class="smallcol">Label</th>
        <th class="bigcol">Notas priv</th>
        <!--<th class="smallcol">Dets</th>-->
        </c:when>

        <c:when test="${flavour == 'management'}">
        <th class="smallcol">Code</th>
        <th class="smallcol hideincompactview">Coord</th>
        <th class="smallcol">Preci</th>
        <th class="bigcol">Taxon</th>
        <th class="smallcol">Conf</th>
        <th class="smallcol">Date</th>
        <th class="smallcol">Local</th>
        <th class="smallcol">Excl</th>
        <th class="smallcol">Auth</th>
        <th class="bigcol">Notas</th>
        <th class="bigcol">Notas priv</th>
        <th class="smallcol hideincompactview">Nº</th>
        <th class="smallcol hideincompactview">Met</th>
        <th class="smallcol hideincompactview">Foto</th>
        <th class="smallcol hideincompactview">Colh</th>
        <th class="bigcol">Ameaças esp</th>
        <th class="smallcol hideincompactview">Fen</th>
        </c:when>

        </c:choose>
    </tr>
</thead>
