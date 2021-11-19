<%@ page pageEncoding="UTF-8" %>
<table>
    <tr><td>AOO justification</td><td>
        <t:editabletext
             privilege="${user.canEDIT_SECTION2()}"
             value="${rlde.getGeographicalDistribution().getAOOJustification()}"
             name="geographicalDistribution_AOOJustification"/>
    </td></tr>
</table>
