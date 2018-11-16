<%@ page pageEncoding="UTF-8" %>
<t:multiplechooser
    privilege="${user.canEDIT_SECTION5()}"
    values="${uses}"
    allvalues="${usesAndTrade_Uses}"
    name="usesAndTrade_Uses"
    layout="list"
    idprefix="uses" />
