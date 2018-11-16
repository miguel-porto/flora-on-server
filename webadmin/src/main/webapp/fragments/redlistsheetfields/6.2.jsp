<%@ page pageEncoding="UTF-8" %>
<t:multiplechooser
    privilege="${user.canEDIT_SECTION6() || user.canEDIT_6_2()}"
    values="${threats}"
    allvalues="${threats_Threats}"
    name="threats_Threats"
    layout="list"
    categorized="true"
    idprefix="thr" />
