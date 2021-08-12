<%@ page pageEncoding="UTF-8" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page session="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<fmt:setBundle basename="pt.floraon.redlistdata.fieldValues" />
<c:if test="${!user.canMANAGE_REDLIST_USERS()}">
    <div class="warning"><b>You&#8217;re not authorized to enter this page</b></div>
</c:if>
<c:if test="${user.canMANAGE_REDLIST_USERS()}">
    <c:if test="${requesteduser == null}">
        <h1>User not found</h1>
        <h2><a href="?w=users">go back</a></h2>
    </c:if>
    <c:if test="${requesteduser != null}">
        <h1>${requesteduser.getFullName()} <span class="info">${requesteduser.getUserType()} ${requesteduser.getID()}</span></h1>
        <form class="poster" data-path="${contextPath}/admin/deleteuser" style="float:right" data-callback="?w=users">
            <input type="hidden" name="databaseId" value="${requesteduser.getID()}"/>
            <input type="submit" value="Delete user" class="textbutton"/>
        </form>
        <form class="poster" data-path="${contextPath}/admin/newpassword" style="float:right" data-callback="?w=users">
            <input type="hidden" name="databaseId" value="${requesteduser.getID()}"/>
            <input type="hidden" name="userName" value="${requesteduser.getUserName()}"/>
            <input type="hidden" name="userType" value="${requesteduser.getUserType()}"/>
            <input type="submit" value="Generate new password" class="textbutton"/>
        </form>
        <h2>Edit user</h2>
        <form class="poster" data-path="${contextPath}/admin/updateuser" data-callback="?w=users">
            <input type="hidden" name="databaseId" value="${requesteduser.getID()}"/>
            <input type="hidden" name="userType" value="${requesteduser.getUserType()}"/>
            <table>
                <tr><td class="title">Username</td><td><input type="text" name="userName" value="${requesteduser.getUserName()}"/></td></tr>
                <tr><td class="title">Person name</td><td><input type="text" name="name" value="${requesteduser.getFullName()}"/></td></tr>
                <tr><td class="title">iNaturalist username (login name)</td><td><input type="text" name="iNaturalistUserName" value="${requesteduser.getiNaturalistUserName()}"/></td></tr>
                <tr><td class="title">Email</td><td><input type="text" name="email" value="${requesteduser.getEmail()}"/></td></tr>
                <tr>
                    <td class="title">Global privileges (apply to all taxa)</td>
                    <td class="multiplechooser">
                        <c:forEach var="tmp" items="${redlistprivileges}">
                            <c:if test="${requesteduser.hasAssignedPrivilege(tmp)}">
                                <input type="checkbox" name="${tmp}" id="priv_${tmp}" checked="checked"/><label for="priv_${tmp}" class="wordtag togglebutton">${tmp.getLabel()}</label>
                            </c:if>
                            <c:if test="${!requesteduser.hasAssignedPrivilege(tmp)}">
                                <input type="checkbox" name="${tmp}" id="priv_${tmp}"/><label for="priv_${tmp}" class="wordtag togglebutton">${tmp.getLabel()}</label>
                            </c:if>
                        </c:forEach>
                    </td>
                </tr>
                <tr><td colspan="2"><input type="submit" value="Update user" class="textbutton"/></td></tr>
            </table>
        </form>
        <c:if test="${context == 'redlist'}">
            <h2>User polygons</h2>
            <table>
                <tr><th>Attributes</th><th>Nr. vertices</th></tr>
                <c:forEach var="pol" items="${userPolygon}" >
                    <tr><td>${pol.getValue().getProperties().values().toString()}</td><td>${pol.getValue().size()}</td></tr>
                </c:forEach>
            </table>
            <form class="poster" data-path="${contextPath}/admin/setuserpolygon" data-callback="?w=users">
                <input type="hidden" name="databaseId" value="${requesteduser.getID()}"/>
                <table>
                    <tr><td class="title">Set/replace user area with a polygon file (GeoJSON)</td>
                    <td><input type="file" name="userarea"/><input type="submit" value="Set area" class="textbutton"/></td>
                </table>
            </form>
            <form class="poster" data-path="${contextPath}/admin/setuserpolygon" data-callback="?w=users">
                <input type="hidden" name="databaseId" value="${requesteduser.getID()}"/>
                <input type="hidden" name="userarea" value=""/>
                <input type="submit" value="Delete all areas" class="textbutton"/>
            </form>
            <h2>Taxon-specific privileges</h2>
            <c:if test="${tsprivileges.size() > 0}">
            <h3>Existing privilege sets</h3>
            <table>
                <thead><tr><th>Taxa</th><th>Privileges</th><th></th></tr></thead>
                <tbody>
                <c:forEach var="tsp" items="${tsprivileges}" varStatus="loop">
                    <tr>
                        <td style="vertical-align:top;">
                            <table class="smalltext">
                            <c:forEach var="tax" items="${tsp.getApplicableTaxa()}">
                                <tr>
                                    <td>${taxonMap.containsKey(tax) ? taxonMap.get(tax) : 'ERROR'}</td>
                                    <td><form class="poster inlineblock" data-path="${contextPath}/admin/removetaxonfromset" data-refresh="true">
                                        <input type="submit" value="Remove" class="textbutton compact"/>
                                        <input type="hidden" name="userId" value="${requesteduser.getID()}"/>
                                        <input type="hidden" name="taxEntId" value="${tax}"/>
                                        <input type="hidden" name="index" value="${loop.index}"/>
                                    </form></td>
                                </tr>
                            </c:forEach>
                            </table>
                            <form class="poster" data-path="${contextPath}/admin/updatetaxonprivileges" data-refresh="true">
                                <input type="hidden" name="userId" value="${requesteduser.getID()}"/>
                                <input type="hidden" name="privilegeSet" value="${loop.index}"/>
                                <div class="withsuggestions">
                                    <input type="text" name="query" class="nochangeevent" placeholder="<fmt:message key="DataSheet.msg.typeletters"/>" autocomplete="off" id="taxonbox_group_${loop.index}"/>
                                    <div id="suggestions_group_${loop.index}"></div>
                                </div>
                                <div class="multiplechooser" id="taxa_group_${loop.index}"></div>
                                <input type="submit" value="Add taxa to this group" class="textbutton"/>
                            </form>
                        </td><td>
                        <c:forEach var="pri" items="${tsp.getPrivileges()}">
                            <div class="wordtag">${pri.toString()}</div>
                        </c:forEach>
                        </td>
                        <td style="width:0;">
                            <form class="poster" data-path="${contextPath}/admin/removetaxonprivileges" data-refresh="true">
                                <input type="hidden" name="userId" value="${requesteduser.getID()}"/>
                                <input type="hidden" name="index" value="${loop.index}"/>
                                <input type="submit" value="Remove this privilege set" class="textbutton"/>
                            </form>
                        </td>
                    </tr>
                </c:forEach>
            </tbody>
            </table>
            </c:if>
            <h3>Add a new set of privileges to specific taxa</h3>
            <form class="poster" data-path="${contextPath}/admin/addtaxonprivileges" data-refresh="true">
                <input type="hidden" name="userId" value="${requesteduser.getID()}"/>
                <table>
                    <thead><tr><th>Taxa</th><th>Privileges</th></tr></thead>
                    <tbody>
                        <tr>
                            <td style="width:20%; vertical-align:top;">
                                <div class="multiplechooser" id="taxonprivileges"></div>
                                <div class="withsuggestions">
                                    <input type="text" name="query" class="nochangeevent" placeholder="<fmt:message key="DataSheet.msg.typeletters"/>" autocomplete="off" id="taxonbox"/>
                                    <div id="suggestions"></div>
                                </div>
                            </td>
                            <td class="multiplechooser">
                                <c:forEach var="tmp" items="${redlisttaxonprivileges}">
                                    <input type="checkbox" name="taxonPrivileges" value="${tmp}" id="tspriv_${tmp}"/><label for="tspriv_${tmp}" class="wordtag togglebutton">${tmp.getLabel()}</label>
                                </c:forEach>
                            </td>
                        </tr>
                    <tr><td colspan="2"><input type="submit" value="Add privileges for these taxa" class="textbutton"/></td></tr>
                    </tbody>
                </table>
            </form>
            <h3>Add a new set of privileges multiple taxa by ID</h3>
            <form class="poster" data-path="${contextPath}/admin/addtaxonprivileges" data-refresh="true">
                <input type="hidden" name="userId" value="${requesteduser.getID()}"/>
                <table>
                    <thead><tr><th>Taxa</th><th>Privileges</th></tr></thead>
                    <tbody>
                        <tr>
                            <td style="vertical-align:top;">
                                Paste comma-separated IDs here<br/>
                                <textarea style="width:400px; height:200px" name="applicableTaxa"></textarea>
                            </td>
                            <td class="multiplechooser">
                                <c:forEach var="tmp" items="${redlisttaxonprivileges}">
                                    <input type="checkbox" name="taxonPrivileges" value="${tmp}" id="tspriv2_${tmp}"/><label for="tspriv2_${tmp}" class="wordtag togglebutton">${tmp.getLabel()}</label>
                                </c:forEach>
                            </td>
                        </tr>
                    <tr><td colspan="2"><input type="submit" value="Add privileges for these taxa" class="textbutton"/></td></tr>
                    </tbody>
                </table>
            </form>
        </c:if>
    </c:if>
</c:if>
