<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<!DOCTYPE html>
<div>
<h1>${taxent.getFullName(true)}</h1>
<ul class="menu currentstatus">
	<li class="current${taxent.getCurrent() ? ' selected' : ''}">current</li>
	<li class="notcurrent${taxent.getCurrent() ? '' : ' selected'}">not current</li>
</ul>
<ul class="menu">
	<!-- <li><a href="?w=graph&q=${taxent.getURLEncodedName()}">View in graph</a></li> -->
	<li><a href="?w=graph&id=${taxent.getIDURLEncoded()}">View in graph</a></li>
	<c:if test="${taxentWrapper.isLeafNode() && sessionScope.user!=null}">
		<li id="deletetaxon" class="actionbutton">Delete taxon</li>
	</c:if>
</ul>
	<input type="hidden" name="nodekey" value="${taxent.getID().toString()}"/>
	<c:catch var ="catchException">
   		<c:out value="${taxent.canBeChildOf(taxentWrapper.getParentTaxon()) }"></c:out>
	</c:catch>
	
	<c:if test = "${catchException != null}">
		<p class="error">There are taxonomic errors in this taxon. Please revise it or its parent relationships:<br/>ERROR: ${catchException.getMessage()}</p>
	</c:if>
	<table>
		<tr><td>ID</td><td><c:out value="${taxent.getID().toString() }"></c:out></td></tr>
		<c:if test = "${taxent.getOldId() != null}">
			<tr><td>Legacy ID</td><td><c:out value="${taxent.getOldId().toString() }"></c:out></td></tr>
		</c:if>
		<tr><td>Rank</td><td><c:out value="${taxent.getRank().toString() }"></c:out></td></tr>
		<tr><td>Endemic to</td><td>
			<c:out value="${fn:join(taxentWrapper.getEndemismDegree(),', ')}, "></c:out>
		</td></tr>
	</table>
	<div id="taxoninfo">
		<div id="taxonnativestatus">
			<h3>Native status</h3>
			${nativeStatusTable }		
		</div>
		<div id="taxonsynonyms"><h3>Synonyms</h3>
			<ul class="synonyms">
			<c:forEach var="synonym" items="${taxentWrapper.getSynonyms()}">
	  			<li data-key="${synonym.getID()}"><a href="admin?w=taxdetails&id=${synonym.getID()}"><c:out value="${synonym.getFullName()}"></c:out></a> <div class="button remove">detach</div></li>
			</c:forEach>
			</ul>
		</div>
		<c:if test="${taxent.isSpeciesOrInferior() && taxent.getCurrent()}">
			<div id="taxonsynonyms"><h3>Included taxa</h3>
				<ul class="synonyms">
				<c:forEach var="included" items="${taxentWrapper.getIncludedTaxa()}">
		  			<li data-key="${included.getID()}"><a href="admin?w=taxdetails&id=${included.getID()}"><c:out value="${included.getFullName()}"></c:out></a></li>
				</c:forEach>
				</ul>
			</div>
		</c:if>
	</div>
	
	<c:if test="${sessionScope.user!=null}">
		<div class="toggler off" id="updatetaxonbox">
			<h1>Change name <span class="info">changes this taxon</span></h1>
			<div class="content">
				<table>
					<tr><td>New name</td><td><input type="text" name="name" value="${taxent.getName()}"/></td></tr>
					<tr><td>New author</td><td><input type="text" name="author" value="${taxent.getAuthor()==null ? '' : taxent.getAuthor()}"/></td></tr>
					<tr><td>New annotation</td><td><input type="text" name="annot" value="${taxent.getAnnotation() == null ? '' : taxent.getAnnotation()}"/></td></tr>
				</table>
				<input type="hidden" name="rank" value="${taxent.getRankValue()}"/>
				<input type="hidden" name="current" value="${taxent.getCurrent() ? 1 : 0}"/>
				<input type="button" value="Update" class="actionbutton" id="updatetaxon"/>
			</div>
		</div>
	
		<div class="toggler off" id="addnativestatusbox">
			<h1>Add/change native status <span class="info">adds a new, or updates, the native status to a territory</span></h1>
			<div class="content">
				This taxon <select name="nativeStatus">
					<option value="NATIVE">is NATIVE to</option>
					<option value="ENDEMIC">is ENDEMIC to</option>
					<option value="EXOTIC">is EXOTIC in</option>
					<option value="UNCERTAIN">is DOUBTFULLY NATIVE to</option>
					<option value="NULL">has no status in</option></select>
				<select name="territory">
					<c:forEach var="territory" items="${territories}">
						<option value="${territory.getShortName()}"><c:out value="${territory.getName()}"></c:out></option>
					</c:forEach>
				</select> and is 
				<select name="occurrenceStatus">
					<c:forEach var="ostatus" items="${occurrenceStatus}">
						<option value="${ostatus.toString()}"><c:out value="${ostatus.toString()}"></c:out></option>
					</c:forEach>
				</select> <input type="button" value="Add / Update" class="actionbutton" id="addnativestatus"/>
			</div>
		</div>
	
		
		<div class="toggler off">
			<h1>Add new synonym <span class="info">binds an existing name as a synonym of this taxon</span></h1>
			<div class="content">
				<p>Add <input type="text" name="query" class="withsuggestions" placeholder="type some letters to find a taxon" autocomplete="off" id="boxsynonym"/> as a synonym of this taxon. <input type="button" value="Add as a synonym" class="actionbutton" id="addsynonym"/></p>
				<div id="suggestions"></div>
			</div>
		</div>
		<div class="toggler off" id="addchildbox">
			<h1>Add new sub-taxon <span class="info">creates a new taxon and adds it as a child of this taxon</span></h1>
			<div class="content">
				<table>
				<tr><td>Name</td><td><input type="text" name="name" placeholder="complete scientific name without author"/></td></tr>
				<tr><td>Author</td><td><input type="text" name="author"/></td></tr>
				<tr><td>Annotation</td><td><input type="text" name="annot"/></td></tr>
				<tr><td>Rank</td><td>
					<select name="rank">
						<c:forEach var="rank" items="${TaxonRanks}">
							<option value="${rank.getValue().toString()}"><c:out value="${rank.getName()}"></c:out></option>
						</c:forEach>
					</select>
				</td></tr>
				<tr><td>Currently accepted?</td><td><input type="checkbox" name="current" checked="checked"/></td></tr>
				</table>
				<input type="button" value="Add" class="actionbutton" id="addchild"/>
			</div>
		</div>
	</c:if>
</div>
