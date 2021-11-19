<%@ page pageEncoding="UTF-8" %>
<table>
    <tr><td>EOO justification</td><td>
        <t:editabletext
             privilege="${user.canEDIT_SECTION2()}"
             value="${rlde.getGeographicalDistribution().getEOOJustification()}"
             name="geographicalDistribution_EOOJustification"/>
    </td></tr>
</table>
