<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<!DOCTYPE html>
<div class="${sessionScope.user!=null ? 'editable' : '' }">
<h1>${taxent.getFullName(true)}</h1>
<ul class="menu multiplesel" id="currentstatus">
	<li class="current${taxent.getCurrent() ? ' selected' : ''}">current</li>
	<li class="notcurrent${taxent.getCurrent() ? '' : ' selected'}">not current</li>
</ul>
<ul class="menu">
	<!-- <li><a href="?w=graph&q=${taxent.getURLEncodedName()}">View in graph</a></li> -->
	<li><a href="?w=graph&depth=2&id=${taxent.getIDURLEncoded()}">View in graph</a></li>
	<c:if test="${taxentWrapper.isLeafNode() && sessionScope.user!=null && sessionScope.user.getRole().equals('advanced')}">
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
	<div id="taxoninfo">
		<div id="generalinfo">
			<h3>General info</h3>
			<table>
				<tr><td>ID</td><td><c:out value="${taxent.getID().toString() }"></c:out></td></tr>
				<c:if test = "${taxent.getOldId() != null}">
					<tr><td>Legacy ID</td><td><c:out value="${taxent.getOldId().toString() }"></c:out></td></tr>
				</c:if>
				<tr><td>Rank</td><td><c:out value="${taxent.getRank().toString() }"></c:out></td></tr>
				<tr><td>Endemic to</td><td>
					<c:out value="${fn:join(taxentWrapper.getEndemismDegree(),', ')}"></c:out>
				</td></tr>
			</table>
			<c:if test="${restrictedTo.size() > 0}">
				<h3>Restricted distribution</h3>
				<table>
					<tr><th>Within</th><th>Restricted to</th></tr>
					<c:forEach var="terr" items="${restrictedTo.keySet().iterator()}">
						<tr>
							<td>
								<c:out value="${terr}"></c:out>
							</td>
							<td>
								<c:forEach var="rto" items="${restrictedTo.get(terr).iterator()}">
									<c:out value="${rto.getName()}"></c:out>
								</c:forEach>
							</td>
						</tr>
					</c:forEach>
				</table>
			</c:if>
		</div>
		<div id="taxonsynonyms"><h3>Synonyms</h3>
			<ul class="synonyms">
			<c:forEach var="synonym" items="${taxentWrapper.getSynonyms()}">
	  			<li data-key="${synonym.getID()}"><a href="admin?w=taxdetails&id=${synonym.getID()}"><c:out value="${synonym.getFullName()}"></c:out></a> <div class="button remove">detach</div></li>
			</c:forEach>
			</ul>
		</div>
		<c:if test="${taxent.isSpeciesOrInferior() && taxent.getCurrent()}">
			<div id="taxonincluded"><h3>Included taxa</h3>
				<ul class="synonyms">
				<c:forEach var="included" items="${taxentWrapper.getIncludedTaxa()}">
		  			<li data-key="${included.getID()}"><a href="admin?w=taxdetails&id=${included.getID()}"><c:out value="${included.getFullName()}"></c:out></a></li>
				</c:forEach>
				</ul>
			</div>
		</c:if>
		<div id="taxonnativestatus">
			<h3>Status assigned to this taxon</h3>
			<form class="poster" data-path="/floraon/api/territories/set">
			<input type="hidden" name="taxon" value="${taxent.getID()}"/>
			<table>
				<tr><th>Territory</th><th>Native Status</th><th>Occurrence Status</th><th>Abundance Level</th><th>Introduced Status</th><th>Naturalization Degree</th>
				<c:if test="${sessionScope.user != null}"><th></th></c:if>
				</tr>
				<c:forEach var="nativeStatus" items="${assignedNativeStatus}">
					<tr>
						<td><c:out value="${nativeStatus.getTerritory().getName()}"></c:out></td>
						<td><c:out value="${nativeStatus.getExistsIn().getNativeStatus()}"></c:out></td>
						<td><c:out value="${nativeStatus.getExistsIn().getOccurrenceStatus()}"></c:out><c:if test="${nativeStatus.getExistsIn().isUncertainOccurrenceStatus()}"> (uncertain)</c:if></td>
						<td><c:out value="${nativeStatus.getExistsIn().getAbundanceLevel()}"></c:out></td>
						<td><c:out value="${nativeStatus.getExistsIn().getIntroducedStatus()}"></c:out></td>
						<td><c:out value="${nativeStatus.getExistsIn().getNaturalizationDegree()}"></c:out></td>
						<c:if test="${sessionScope.user != null}">
							<td><form class="poster" data-path="/floraon/api/territories/set">
								<input type="hidden" name="taxon" value="${taxent.getID()}"/>
								<input type="hidden" name="territory" value="${nativeStatus.getTerritory().getShortName()}"/>
								<input type="hidden" name="nativeStatus" value="NULL"/>
								<input type="submit" value="Remove status"/>
							</form></td>
						</c:if>
					</tr>
				</c:forEach>
			</table>
			</form>
		</div>
		<div id="distributioncompleteness">
			<h3>Distribution completeness</h3>
			<table>
				<tr><td>World native distribution completeness</td><td>
					<c:if test="${sessionScope.user == null}">
						<ul class="menu">
							<li><c:out value="${taxent.getWorldDistributionCompleteness().toString()}"></c:out></li>
						</ul>
					</c:if>
					<c:if test="${sessionScope.user != null}">
						<ul class="menu multiplesel" id="worlddistribution">
							<li data-value="DISTRIBUTION_COMPLETE" class="${taxent.getWorldDistributionCompleteness()=='DISTRIBUTION_COMPLETE' ? ' selected' : ''}">complete distribution</li>
							<li data-value="DISTRIBUTION_INCOMPLETE" class="${taxent.getWorldDistributionCompleteness()=='DISTRIBUTION_INCOMPLETE' ? ' selected' : ''}">incomplete distribution</li>
							<li data-value="NOT_KNOWN" class="${taxent.getWorldDistributionCompleteness()=='NOT_KNOWN' ? ' selected' : ''}">not known</li>
						</ul>
					</c:if>
				</td></tr>
				<tr><td>Territories with complete distributions</td><td>
					<c:if test="${sessionScope.user == null}">
						<ul class="menu">
							<c:forEach var="territory" items="${taxentWrapper.getTerritoriesWithCompleteDistribution()}">
								<li><c:out value="${territory.getName()}"></c:out></li>
							</c:forEach>
						</ul>
					</c:if>
					<c:if test="${sessionScope.user != null}">
						<form class="poster" data-path="/floraon/api/update/unsetcompleteterritory">
							<input type="hidden" name="id" value="${taxent.getID()}"/>
							<c:forEach var="territory" items="${taxentWrapper.getTerritoriesWithCompleteDistribution()}">
								<label><input type="radio" name="territory" value="${territory.getID() }"><c:out value="${territory.getName()}"></c:out></label>
							</c:forEach>
							<input type="submit" value="Remove territory"/>
						</form>
					</c:if>
				</td></tr>
			</table>
		</div>
		<div>
			<h3>Inferred Native Status</h3>
			<table>
				<c:forEach var="ins" items="${inferredNativeStatus}">
					<tr>
					<td><c:out value="${ins.getValue().getTerritoryName()}"></c:out></td>
					<td><c:out value="${ins.getValue().getVerbatimNativeStatus()}"></c:out></td>
					</tr>
				</c:forEach>
			</table>
		</div>
		<c:if test="${sessionScope.user!=null}">
			<div id="editbox">
				<h3>Add or modify data</h3>
				<div class="toggler off" id="updatetaxonbox">
					<h1>Change name <span class="info">changes this taxon</span></h1>
					<div class="content">
						<form class="poster" data-path="/floraon/api/update/update/taxent">
							<table>
								<tr><td>New name</td><td><input type="text" name="name" value="${taxent.getName()}"/></td></tr>
								<tr><td>New author</td><td><input type="text" name="author" value="${taxent.getAuthor()==null ? '' : taxent.getAuthor()}"/></td></tr>
								<tr><td>New <i>sensu</i></td><td><input type="text" name="sensu" value="${taxent.getSensu() == null ? '' : taxent.getSensu()}"/></td></tr>
								<tr><td>New annotation</td><td><input type="text" name="annotation" value="${taxent.getAnnotation() == null ? '' : taxent.getAnnotation()}"/></td></tr>
								<tr><td>New legacy ID</td><td><input type="text" name="oldId" value="${taxent.getOldId() == null ? '' : taxent.getOldId()}"/></td></tr>
							</table>
							<input type="hidden" name="id" value="${taxent.getID()}"/>
							<!--<input type="hidden" name="rank" value="${taxent.getRankValue()}"/>
							<input type="hidden" name="current" value="${taxent.getCurrent() ? 1 : 0}"/>
							<input type="hidden" name="worldDistributionCompleteness" value="${taxent.getWorldDistributionCompleteness()}"/>
							 -->
							<input type="hidden" name="replace" value="0"/>
							<input type="submit" value="Update"/>
						</form>
					</div>
				</div>
			
				<div class="toggler off">
					<h1>Add new synonym <span class="info">binds an existing name as a synonym of this taxon</span></h1>
					<div class="content">
						<p>Add <input type="text" name="query" class="withsuggestions" placeholder="type some letters to find a taxon" autocomplete="off" id="boxsynonym"/> as a synonym of this taxon. <input type="button" value="Add as a synonym" class="actionbutton" id="addsynonym"/></p>
						<div id="suggestions"></div>
					</div>
				</div>
				<div class="toggler off">
					<h1>Add status in territory <span class="info">adds or changes the status of this taxon in territories</span></h1>
					<div class="content">
						<form class="poster" data-path="/floraon/api/territories/set">
						<input type="hidden" name="taxon" value="${taxent.getID()}"/>
						<table>
							<tr>
								<td>Territory</td>
								<td>
									<select name="territory">
										<c:forEach var="territory" items="${territories.iterator()}">
											<option value="${territory.getShortName()}"><c:out value="${territory.getName()}"></c:out></option>
										</c:forEach>
									</select>
								</td>
							</tr><tr>
								<td>Native Status</td>
								<td>
									<select name="nativeStatus">
										<c:forEach var="nstatus" items="${nativeStatus}">
											<c:if test="${!nstatus.isReadOnly()}">
												<option value="${nstatus.toString()}"><c:out value="${nstatus.toVerboseString()}"></c:out></option>
											</c:if>
										</c:forEach>
									</select>
								</td>
							</tr><tr>
								<td>Occurrence Status</td>
								<td>
									<select name="occurrenceStatus">
										<c:forEach var="ostatus" items="${occurrenceStatus}">
											<option value="${ostatus.toString()}"><c:out value="${ostatus.toString()}"></c:out></option>
										</c:forEach>
									</select>
									<label style="display:inline-block"><input type="checkbox" value="1" name="uncertain"/> occurrence is uncertain</label>
								</td>
							</tr><tr>
								<td>Abundance Level</td>
								<td>
									<select name="abundanceLevel">
										<c:forEach var="alevel" items="${abundanceLevel}">
											<option value="${alevel.toString()}"><c:out value="${alevel.toString()}"></c:out></option>
										</c:forEach>
									</select>
								</td>
							</tr><tr>
								<td>Introduced Status</td>
								<td>
									<select name="introducedStatus">
										<c:forEach var="istat" items="${introducedStatus}">
											<option value="${istat.toString()}"><c:out value="${istat.toString()}"></c:out></option>
										</c:forEach>
									</select>
								</td>
							</tr><tr>
								<td>Naturalization Degree</td>
								<td>
									<select name="naturalizationDegree">
										<c:forEach var="ndeg" items="${naturalizationDegree}">
											<option value="${ndeg.toString()}"><c:out value="${ndeg.toString()}"></c:out></option>
										</c:forEach>
									</select>
								</td>
							</tr>
						</table>
						<input type="submit" value="Add / update"/>
						</form>
					</div>
				</div>
				<div class="toggler off">
					<h1>Set distribution completeness <span class="info">set the territories in which the distribution is complete</span></h1>
					<div class="content">
						<form class="poster" data-path="/floraon/api/update/add/completeterritory">
							<input type="hidden" name="id" value="${taxent.getID()}"/>
							Set the distribution as complete (both <b>native and exotic</b>) in <select name="territory">
								<c:forEach var="territory" items="${territories.iterator()}">
									<option value="${territory.getID()}"><c:out value="${territory.getName()}"></c:out></option>
								</c:forEach>
							</select>
							<input type="submit" value="Add"/>
						</form>
					</div>
				</div>
				<div class="toggler off" id="addchildbox">
					<h1>Add new sub-taxon <span class="info">creates a new taxon and adds it as a child of this taxon</span></h1>
					<div class="content">
						<form class="poster" data-path="/floraon/api/update/add/inferiortaxent">
							<table>
							<tr><td>Name</td><td><input type="text" name="name" placeholder="complete scientific name without author"/></td></tr>
							<tr><td>Author</td><td><input type="text" name="author"/></td></tr>
							<tr><td><i>sensu</i></td><td><input type="text" name="sensu"/></td></tr>
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
							<input type="hidden" name="parent" value="${taxent.getID().toString()}"/>
							<!-- <input type="button" value="Add" class="actionbutton" id="addchild"/> -->
							<input type="submit" value="Add"/>
						</form>
					</div>
				</div>
			</div>
		</c:if>
	</div>
</div>
