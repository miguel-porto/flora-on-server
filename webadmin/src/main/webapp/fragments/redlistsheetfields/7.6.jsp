<%@ page pageEncoding="UTF-8" %>
<t:multiplechooser
    privilege="${user.canEDIT_SECTION7()}"
    values="${proposedStudyMeasures}"
    allvalues="${conservation_ProposedStudyMeasures}"
    name="conservation_ProposedStudyMeasures"
    layout="list"
    idprefix="psm" />
