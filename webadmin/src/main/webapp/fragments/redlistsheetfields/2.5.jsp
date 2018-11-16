<%@ page pageEncoding="UTF-8" %>
<c:if test="${user.canEDIT_SECTION2()}">
    <input name="geographicalDistribution_ElevationRange" type="number" min="0" value="${rlde.getGeographicalDistribution().getElevationRange()[0] == null ? '' : rlde.getGeographicalDistribution().getElevationRange()[0]}"/>
    <input name="geographicalDistribution_ElevationRange" type="number" min="0" value="${rlde.getGeographicalDistribution().getElevationRange()[1] == null ? '' : rlde.getGeographicalDistribution().getElevationRange()[1]}"/>
</c:if>
<c:if test="${!user.canEDIT_SECTION2()}">
    ${rlde.getGeographicalDistribution().getElevationRange()[0]} - ${rlde.getGeographicalDistribution().getElevationRange()[1]}
</c:if>
