<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
	<title>Taxonomy & Checklist Manager</title>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
	<link href='http://fonts.googleapis.com/css?family=Lato:300' rel='stylesheet' type='text/css'>
	<link rel="stylesheet" type="text/css" href="checklist.css"/>
	<script type="text/javascript" src="basefunctions.js"></script>
	<script type="text/javascript" src="suggestions.js"></script>
	<script type="text/javascript" src="manager.js"></script>
</head>
<body>
<h1>Taxonomy & Checklist Manager</h1>
<div style="position:absolute;right:3px;top:3px"><a href="https://github.com/miguel-porto/flora-on-server" target="_blank">fork me on GitHub</a></div>
<ul class="menu">
<li id="download-checklist">Download checklist</li>
<li><a href="?w=graph">Graphical taxonomy explorer</a></li>
</ul>
<div id="main-holder">
	<div id="left-bar">
		<ul>
			<li><a href="?w=main">Taxon list</a></li>
			<li><a href="?w=families">Family tree</a></li>
			<li><a href="?w=tree">Whole tree</a></li>
			<li><a href="?w=graph&show=territories">Territories</a></li>
			<!--<li><a href="?w=validate">Validate</a></li>-->
			<li><a href="?w=query">Free query</a></li>
		</ul>
	</div>
 
	<c:choose>
	<c:when test="${what==null}">
		<div id="main"><h2>This is the Flora-On taxonomy manager</h2><p>To edit the checklist, choose <a href="?w=families">Family tree</a> on the left.</p></div>
	</c:when>
	<c:when test="${what=='main'}">
		<div id="main" class="checklist noselect"><h1>List of all accepted names<c:out value="${territory}"></c:out></h1>
		<p>Click on a taxon to edit it</p>
		<div><div class="territory NATIVE"></div> native <div class="territory ENDEMIC"></div> endemic <div class="territory EXOTIC"></div> exotic <div class="territory UNCERTAIN"></div> doubtfully native <div class="territory EXISTING"></div> existing</div>
		<div class="paging"><div class="legend">Showing taxa <c:out value="${offset+1}"></c:out> to <c:out value="${offset+PAGESIZE}"></c:out></div><a href="?w=main&offset=${(offset-PAGESIZE < 0 ? 0 : (offset-PAGESIZE))}">&lt; previous</a> | <a href="?w=main&offset=${offset+PAGESIZE}">next &gt;</a></div>
		<jsp:include page="/api/lists?w=speciesterritories&fmt=htmltable&offset=${offset}"></jsp:include>
		<div class="paging"><div class="legend">Showing taxa <c:out value="${offset+1}"></c:out> to <c:out value="${offset+PAGESIZE}"></c:out></div><a href="?w=main&offset=${(offset-PAGESIZE < 0 ? 0 : (offset-PAGESIZE))}">&lt; previous</a> | <a href="?w=main&offset=${offset+PAGESIZE}">next &gt;</a></div>
		</div>
	</c:when>
	<c:when test="${what=='families'}">
		<div id="main" class="taxman-holder"><div id="taxtree" class="taxtree-holder">
		<jsp:include page="/api/lists?w=tree&rank=family&fmt=htmllist"></jsp:include>
		</div>
		<div class="taxdetails"><h2>Click a taxon on the tree to edit</h2></div>
		</div>
	</c:when>
	<c:when test="${what=='taxdetails'}">
		<div id="main" class="taxdetails">
		<jsp:include page="/api/taxdetails"></jsp:include>
		</div>
	</c:when>
	<c:when test="${what=='tree'}">
		<div id="main" class="taxman-holder"><div id="taxtree" class="taxtree-holder">
		<jsp:include page="/api/lists?w=tree&rank=class&fmt=htmllist"></jsp:include>
		</div>
		<div class="taxdetails"><h2>Click a taxon on the tree to edit</h2></div>
		</div>
	</c:when>
	
	<c:when test="${what=='query'}">
		<div id="main"><h2>Enter your query</h2>
		<form id="freequery">
			<input type="text" id="querybox" name="query" value="${query==null ? '' : query }"/>
			<input type="submit" value="Search!"/>
		</form>
		<c:if test="${query!=null }">
		<jsp:include page="/api/query?q=${query}&fmt=html"></jsp:include>
		</c:if>
		</div>
	</c:when>
	</c:choose>
	
</div>
</body>
</html>

