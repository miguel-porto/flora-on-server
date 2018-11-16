<%@ page pageEncoding="UTF-8" %>
<t:multiplechooser
    privilege="${user.canEDIT_SECTION7()}"
    values="${proposedConservationActions}"
    allvalues="${conservation_ProposedConservationActions}"
    name="conservation_ProposedConservationActions"
    layout="list"
    idprefix="pca" />
