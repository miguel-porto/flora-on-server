<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page session="false" %>
<!DOCTYPE html>
<html>
<head>
	<title>Flora-On Admin</title>
	<link rel="stylesheet" type="text/css" href="base.css?nocache=${uuid}"/>
	<link rel="stylesheet" type="text/css" href="redlist.css?nocache=${uuid}"/>
	<script type="text/javascript" src="sorttable.js"></script>
	<script type="text/javascript" src="ajaxforms.js"></script>
	<script type="text/javascript" src="basefunctions.js?nocache=${uuid}"></script>
	<script type="text/javascript" src="suggestions.js?nocache=${uuid}"></script>
	<script type="text/javascript" src="admin.js?nocache=${uuid}"></script>
</head>
<body>
    <form action="upload/toponyms" method="post" enctype="multipart/form-data" class="poster" data-path="upload/toponyms">
        <input type="hidden" name="type" value="kml"/>
        <input type="file" name="toponymTable" />
        <input type="submit" class="textbutton" value="Upload"/>
    </form>
</body>
</html>
