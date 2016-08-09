<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
	<title>Taxonomy browser</title>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
	<link rel="stylesheet" type="text/css" href="graphbrowser.css"/>
	<script type="text/javascript" src="d3.min.js"></script>
	<script type="text/javascript" src="basefunctions.js"></script>
	<script type="text/javascript" src="graphbrowser.js"></script>
</head>
<body>
<div id="main-wrap">
	<div id="taxbrowser"></div>
	<div id="toolbar" data-page="tax">
		<ul id="linklist"></ul>
		<c:if test="${sessionScope.user != null && sessionScope.user.getRole().equals('advanced') }">
			<input type="hidden" id="loggedin" name="loggedin" value="${sessionScope.user}"/>
		</c:if>
		<div class="box">
			<div class="title"><p>Expand depth</p></div>
			<div id="but-depth" data-depth="1" class="button round">1</div>
			<div id="but-depth" data-depth="2" class="button round">2</div>
			<div id="but-depth" data-depth="3" class="button round">3</div>
			<div id="but-depth" data-depth="4" class="button round">4</div>
			<p style="clear:both"/>
		</div>
		<div class="box">
			<div id="but-clean" class="button">Clean</div>
			<!--<div id="but-orphan" class="button">Load orphan</div>-->
			<div id="but-characters" class="button">Load characters</div>
			<div id="but-territories" class="button">Load territories</div>
		</div>
		<c:if test="${sessionScope.user != null && sessionScope.user.getRole().equals('advanced')}">
			<div class="box">
				<div class="title"><p>Node tools</p></div>
				<div id="but-delnode" class="button">Delete node/link</div>
				<div id="but-newnode" class="button">New taxon</div>
				<div id="but-newterritory" class="button">New territory</div>
			</div>
			<div class="box">
				<div class="title"><p>Create new link</p></div>
				<div id="but-partof" class="button">PART_OF</div>
				<div id="but-synonym" class="button">SYNONYM</div>
				<div id="but-parent" class="button">HYBRID_OF</div>
				<div id="but-belongs" class="button">BELONGS_TO</div>
				<div id="but-hasquality" class="button">HAS_QUALITY</div>
			</div>
		</c:if>
		
		<!--<div class="box">
		<div class="title"><p>Upload CSVs</p></div>
		<div id="but-uploadbase" class="button">Base taxonomy</div>
		<div id="but-uploadinter" class="button">Intercalate level</div>
		<div id="but-empty" class="button">Empty taxonomy!</div>
		</div>-->
		<div class="box">
		<div class="title"><p>General query</p></div>
		<div class="button dead"><input type="text" name="query" id="querytool"/></div>
		<div class="title"><p>Add node</p></div>
		<div class="button dead"><input type="text" name="query" id="loadnode"/></div>
		</div>
		<div id="viewfacets" class="box">
		<div class="title"><p>Facets</p></div>
		<div id="but-showtaxonomy" name="TAXONOMY" class="button selected">Taxonomic links<div class="pin selected"></div></div>
		<!--<div id="but-showecology" name="ECOLOGY" class="button selected">Ecological links<div class="pin selected"></div></div>-->
		<div id="but-showmorphology" name="MORPHOLOGY" class="button">Morphological links<div class="pin"></div></div>
		<div id="but-showoccurrence" name="OCCURRENCE" class="button">Occurrence links<div class="pin"></div></div>
		</div>
	</div>
	<div id="legend">
		<div class="entry selected" id="but-entnames"><div class="pin"></div>Entity names</div>
	</div>
</div>
<div id="wait-screen">Working...</div>
<div id="territorytypes"><jsp:include page="/graph/reference/territorytypes"></jsp:include></div>
<div id="taxonranks"><jsp:include page="/graph/reference/ranks"></jsp:include></div>
</body>
</html>

