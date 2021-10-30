<%@ page pageEncoding="UTF-8" %><%@ page contentType="text/html; charset=UTF-8" %><%@ page session="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<ul><c:forEach var="tax" items="${taxents}"><li class="${tax.getRank().toString()}${tax.getCurrent() ? '' : ' notcurrent'}" data-key="${tax.getID()}">${tax.getNameWithAnnotationOnly(true)}</li></c:forEach></ul>