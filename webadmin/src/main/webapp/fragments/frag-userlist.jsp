<%@ page pageEncoding="UTF-8" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page session="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<fmt:setBundle basename="pt.floraon.redlistdata.fieldValues" />
<c:if test="${!user.canMANAGE_REDLIST_USERS()}">
    <div class="warning"><b>You&#8217;re not authorized to enter this page</b></div>
</c:if>
<c:if test="${user.canMANAGE_REDLIST_USERS()}">
    <h1>User management</h1>
    <c:if test="${param.viewall != '1'}"><div class="button anchorbutton"><a href="?w=users&viewall=1">View all</a></div></c:if>
    <c:if test="${param.viewall == '1'}"><div class="button anchorbutton selected"><a href="?w=users">View all</a></div></c:if>
    <h2>Existing users</h2>
    <table class="sortable smalltext">
        <tr><th>Name</th><th>Global privileges</th><c:if test="${responsibleTextCounter != null}"><th>Taxon-specific privileges</th><th>Responsible for texts</th><th>Responsible for assessment</th><th>Responsible for revision</th></c:if><th></th></tr>
        <c:forEach var="tmp" items="${users}">
            <c:if test="${user.isAdministrator() || (!user.isAdministrator() && !tmp.isAdministrator())}">
            <tr>
                <td>${tmp.getName()}<c:if test="${tmp.getPassword() == null || tmp.getPassword() == ''}"> <span class="warning">no account</span></c:if></td>
                <td>
                <c:forEach var="tmp1" items="${tmp.getPrivileges()}">
                    <div class="wordtag">${tmp1.getLabel()}</div>
                </c:forEach>
                </td>
                <c:if test="${responsibleTextCounter != null}">
                <td><ul>
                <c:forEach var="tmp2" items="${tmp.getTaxonPrivileges()}">
                    <li>
                    <c:if test="${fn:length(tmp2.getApplicableTaxa()) > 1}">
                    <c:forEach var="tmp3" begin="0" end="${fn:length(tmp2.getApplicableTaxa()) - 2}">
                        <i>${taxonMap.get(tmp2.getApplicableTaxa()[tmp3])}</i>,&nbsp;
                    </c:forEach>
                    </c:if>
                        <i>${taxonMap.get(tmp2.getApplicableTaxa()[fn:length(tmp2.getApplicableTaxa()) - 1])}</i>
                    </li>
                </c:forEach>
                </ul></td>
                <td class="bignumber">${responsibleTextCounter.get(tmp.getID())}</td>
                <td class="bignumber">${responsibleAssessmentCounter.get(tmp.getID())}</td>
                <td class="bignumber">${responsibleRevisionCounter.get(tmp.getID())}</td>
                </c:if>
                <td><div class="button anchorbutton"><a href="?w=edituser&amp;user=${tmp._getIDURLEncoded()}">edit user</a></div></td>
            </tr>
            </c:if>
        </c:forEach>
    </table>
    <h2>Create new user</h2>
    <form class="poster" data-path="../admin/createuser">
        <table>
            <tr><td class="title">Username</td><td><input type="text" name="userName"/></td></tr>
            <tr><td class="title">Person name</td><td><input type="text" name="name"/></td></tr>
            <tr><td class="title">iNaturalist username (login name)</td><td><input type="text" name="iNaturalistUserName"/></td></tr>
            <tr><td class="title">Email</td><td><input type="text" name="email"/></td></tr>
            <tr>
                <td class="title">Global privileges</td>
                <td class="multiplechooser">
                    <c:forEach var="tmp" items="${redlistprivileges}">
                        <input type="checkbox" name="${tmp}" id="priv_${tmp}"/><label for="priv_${tmp}" class="wordtag togglebutton">${tmp.getLabel()}</label>
                    </c:forEach>
                </td>
            </tr>
        </table>
        <input type="submit" value="Create" class="textbutton"/>
    </form>
</c:if>
