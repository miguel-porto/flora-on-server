<%@ page pageEncoding="UTF-8" %>
<t:editabletext
    privilege="${user.canEDIT_SECTION2() || user.canEDIT_SECTION3() || user.canEDIT_SECTION4() || user.canEDIT_SECTION5() || user.canEDIT_SECTION6() || user.canEDIT_SECTION7() || user.canEDIT_ALL_TEXTUAL()}"
    value="${rlde.getOtherInformation()}"
    name="otherInformation"/>
