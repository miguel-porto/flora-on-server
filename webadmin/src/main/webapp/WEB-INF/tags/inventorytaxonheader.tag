<%@ tag description="Occurrence table header" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ attribute name="flavour" required="false" %>

<thead>
    <tr>
        <th class="sorttable_nosort selectcol"></th>
        <th class="hidden"></th>
    <c:choose>
        <c:when test="${flavour == null || flavour == '' || flavour == 'simple'}">
        <th class="bigcol">Taxon</th>
        <th class="smallcol">Conf</th>
        <th class="smallcol">Fen</th>
        <th class="smallcol">Nº</th>
        <th class="smallcol">Cover</th>
        <th class="smallcol">Comment</th>
        <th class="smallcol">Notas priv</th>
        </c:when>

        <c:when test="${flavour == 'redlist'}">
        <th class="smallcol">GPS</th>
        <th class="bigcol">Taxon</th>
        <th class="smallcol">Conf</th>
        <th class="smallcol">Fen</th>
        <th class="smallcol">Nº</th>
        <th class="smallcol">Met</th>
        <th class="smallcol">Cover</th>
        <th class="smallcol">Fot</th>
        <th class="smallcol">Colh</th>
        <th class="smallcol">Comment</th>
        <th class="smallcol">Notas priv</th>
        <th class="smallcol">Ameaças</th>
        <th class="smallcol">Excl</th>
        </c:when>
    </c:choose>
    </tr>
</thead>
