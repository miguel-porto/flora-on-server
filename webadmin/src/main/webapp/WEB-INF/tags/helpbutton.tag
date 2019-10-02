<%@ tag body-content="scriptless" %>
<%@ tag description="Clickable help button" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ attribute name="msgid" required="true" %>

<jsp:doBody var="message" />
<div class="button round help" data-msgid="${msgid}"></div>
<div id="${msgid}" class="floatinghelp hidden">
<div class="button close"></div>
${message}
</div>
