<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
	<title>Taxonomy &amp; Checklist Manager</title>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
	<link href='http://fonts.googleapis.com/css?family=Lato:300' rel='stylesheet' type='text/css'>
	<link rel="stylesheet" type="text/css" href="checklist.css"/>
	<script type="text/javascript" src="basefunctions.js"></script>
	<script type="text/javascript" src="suggestions.js"></script>
	<script type="text/javascript" src="manager.js"></script>
</head>
<body>
<c:if test="${sessionScope.user!=null}">
	<form action="login" method="post" id="logoutform">
   		<input type="hidden" name="logout" value="1"/>
   		<input type="submit" value="Logout"/>
	</form>				
</c:if>
<h1>Taxonomy &amp; Checklist Manager</h1>
<div style="position:absolute;right:3px;top:3px"><a href="https://github.com/miguel-porto/flora-on-server" target="_blank">fork me on GitHub</a></div>
<ul class="menu">
<li id="download-checklist">Download checklist</li>
<li><a href="?w=graph&depth=3">Graphical taxonomy explorer</a></li>
</ul>
<div id="main-holder">
	<div id="left-bar">
		<ul>
			<li><a href="?w=login">Login</a></li>
			<li><a href="?w=main">Taxon list</a></li>
			<li><a href="?w=families">Family tree</a></li>
			<li><a href="?w=tree">Whole tree</a></li>
			<li><a href="?w=graph&show=territories">Territories</a></li>
			<!--<li><a href="?w=validate">Validate</a></li>-->
			<li><a href="?w=query">Free query</a></li>
		</ul>
	</div>
 
	<c:choose>
	<c:when test="${what=='login'}">
		<div id="main"><h2>This is the Flora-On taxonomy manager</h2>
		<c:choose>
			<c:when test="${sessionScope.user==null}">
				<p>To edit the checklist, you must login and choose <a href="?w=families">Family tree</a> on the left.</p>
				<form action="login" method="post" id="loginform">
					<table>
					<c:if test="${param.reason!=null }">
					Not found.
					</c:if>
					<tr><td>Username:</td><td><input type="text" name="username"/></td></tr>
					<tr><td>Password:</td><td><input type="password" name="password"/></td></tr>
					</table>
		    		<input type="submit" value="Login"/>
				</form>
			</c:when>
			<c:otherwise>
				<p>Welcome <c:out value="${sessionScope.user.getUsername()} (${sessionScope.user.getRole()})"></c:out>, go ahead and edit!</p>
				<form action="login" method="post">
		    		<input type="hidden" name="logout" value="1"/>
		    		<input type="submit" value="Logout"/>
				</form>				
			</c:otherwise>			
		</c:choose>
		</div>
	</c:when>
	<c:when test="${(what=='main') || (what==null)}">
		<div id="main" class="noselect"><h1>List of all names<c:out value="${territory}"></c:out></h1>
		<c:if test="${sessionScope.user!=null}"><p>Click on a taxon to edit it</p></c:if>
		<div id="legendpanel">
		Native Status: <div class="territory NATIVE"></div> native&nbsp;
		<div class="territory ENDEMIC"></div> endemic&nbsp;
		<div class="territory EXOTIC"></div> exotic&nbsp;
		<div class="territory DOUBTFULLY_NATIVE"></div> doubtfully native&nbsp;
		<div class="territory EXISTING"></div> existing (status not explicitly assigned)
		<br/>
		Occurrence Status: <div class="territory EXISTING"><div class="occurrencestatus RARE"></div></div> rare&nbsp;
		<div class="territory EXISTING"><div class="occurrencestatus POSSIBLY_EXTINCT"></div></div> possibly extinct&nbsp;
		<div class="territory EXISTING"><div class="occurrencestatus EXTINCT"></div></div> extinct &nbsp;
		<div class="territory EXISTING"><div class="occurrencestatus UNCERTAIN_OCCURRENCE"></div></div> of uncertain occurrence&nbsp;
		<div class="territory EXISTING"><div class="occurrencestatus POSSIBLE_OCCURRENCE"></div></div> of possible occurrence&nbsp;
		&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<div class="territory EXISTING"><div class="occurrencestatus uncertain"></div></div><span title="Status is assigned to a taxon of a higher rank"> possibly with this status</span></div>
		<div class="paging"><div id="filterbox">Filter: <input type="text" id="filtertext" placeholder="type filter text" value="${filter}"/></div><div class="legend">Showing taxa <c:out value="${offset+1}"></c:out> to <c:out value="${offset+PAGESIZE}"></c:out></div><a href="?w=main&filter=${filter}&offset=${(offset-PAGESIZE < 0 ? 0 : (offset-PAGESIZE))}">&lt; previous</a> | <a href="?w=main&filter=${filter}&offset=${offset+PAGESIZE}">next &gt;</a><p style="clear:both"/></div>
		<jsp:include page="/api/lists?w=speciesterritories&fmt=htmltable&offset=${offset}&filter=${filter}"></jsp:include>
		<div class="paging"><div class="legend">Showing taxa <c:out value="${offset+1}"></c:out> to <c:out value="${offset+PAGESIZE}"></c:out></div><a href="?w=main&filter=${filter}&offset=${(offset-PAGESIZE < 0 ? 0 : (offset-PAGESIZE))}">&lt; previous</a> | <a href="?w=main&filter=${filter}&offset=${offset+PAGESIZE}">next &gt;</a><p style="clear:both"/></div>
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
<div id="loader"><div id="loadermsg">Loading...</div></div>
</body>
</html>

