<%@ page pageEncoding="UTF-8" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page session="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<fmt:setBundle basename="pt.floraon.redlistdata.fieldValues" />
<iframe style="visibility: hidden; width:0; height:0; margin:0; padding:0" name="trash"></iframe>
<c:if test="${user.canDOWNLOAD_OCCURRENCES()}">
    <h1><fmt:message key="Separator.6"/></h1>
    <p><fmt:message key="Downloads.1"/></p>
    <form class="poster orderdownload" data-path="api/downloadtable" data-refresh="true">
        <input type="hidden" name="territory" value="${territory}"/>
        <h2>Tabela de taxa</h2>
        <p>Descarregar uma tabela com um taxon por linha (opcionalmente filtrada por etiquetas), e com o respectivo EOO, AOO, etc.</p>
            <label><input type="checkbox" name="recalc"/> Recalcular AOO e EOO com base em todos os registos até ao momento<br/><span class="info">se desactivado, os valores corresponderão aos que estão nas fichas de avaliação</span></label><br/>
            <label><input type="checkbox" name="historical"/> Incluir registos históricos nos cálculos (só aplicável se a opção anterior estiver seleccionada)</label>
            <div class="multiplechooser left">
            <c:forEach var="tmp" items="${allTags}">
                <input type="checkbox" name="tags" value="${tmp}" id="tags_${tmp}"/>
                <label for="tags_${tmp}" class="wordtag togglebutton"> ${tmp}</label>
            </c:forEach>
            </div>
        <input type="submit" value="Descarregar" class="textbutton"/>
    </form>
<%--
    <form class="poster orderdownload" data-path="api/downloadalloccurrences" data-refresh="true">
        <h2>Tabela de todas as ocorrências</h2>
        <p>Descarregar uma tabela com todas as ocorrências (opcionalmente dos taxa filtrados por etiquetas), incluindo as duvidosas. Esta tabela inclui ocorrências de todos os provedores de dados.</p>
        <input type="hidden" name="territory" value="${territory}"/>
        <div class="multiplechooser left">
        <c:forEach var="tmp" items="${allTags}">
            <input type="checkbox" name="tags" value="${tmp}" id="tags1_${tmp}"/>
            <label for="tags1_${tmp}" class="wordtag togglebutton"> ${tmp}</label>
        </c:forEach>
        </div>
        <input type="submit" value="Descarregar" class="textbutton"/>
    </form>
--%>
    <form class="poster orderdownload" data-path="api/downloadoccurrencesusedinassessments" data-refresh="true">
        <h2>Tabela de todas as ocorrências usadas nos taxa avaliados</h2>
        <p>Descarregar uma tabela com todas as ocorrências que foram de facto usadas nos mapas de distribuição das espécies avaliadas, actuais ou históricos. Exclui registos duvidosos e imprecisos. Esta tabela inclui ocorrências de todos os provedores de dados.</p>
        <input type="hidden" name="territory" value="${territory}"/>
        <c:if test="${user.canMODIFY_OCCURRENCES()}">
        <p><label><input type="checkbox" name="useUpToDate"/> Quero descarregar também os dados mais actuais, e não apenas os que foram usados nas avaliações</label></p>
        </c:if>
        <div class="multiplechooser left inlineflex">
            <input type="radio" name="currentOrHistorical" value="current" id="butCurrent" checked="checked"/>
            <label for="butCurrent" class="wordtag togglebutton"> Current records</label>
            <input type="radio" name="currentOrHistorical" value="historical" id="butHistorical"/>
            <label for="butHistorical" class="wordtag togglebutton"> Historical records</label>
            <input type="radio" name="currentOrHistorical" value="both" id="butBoth"/>
            <label for="butBoth" class="wordtag togglebutton"> Both sets</label>
        </div>
        <h3>Filtrar por área geográfica</h3>
        <p>Paste a polygon in WKT format here, in latitude longitude coordinates, for example:<br/><code>Polygon ((-7.6167 37.9335, -7.6221 37.9320, -7.6228 37.9285, -7.6185 37.9256, -7.6144 37.9249, -7.6128 37.9287, -7.6127 37.9304, -7.6146 37.9325, -7.6167 37.9335))</code><br/><span class="info">You can copy a polygon from QGIS and paste it!</span></p>
        <p>NOTE: polygons with holes and multipart polygons are NOT supported.</p>
        <textarea style="width: 98%; height: 150px; border: 2px solid #1e88e5; margin: 0 1%; padding: 4px; font-size: 0.75em; border-radius: 3px;" name="polygon"></textarea>
        <input type="submit" value="Descarregar" class="textbutton"/>
    </form>

    <c:if test="${user.canMODIFY_OCCURRENCES()}">
    <form class="poster orderdownload" data-path="api/downloadtaxainpolygon" data-refresh="true">
        <h2>Tabela de taxa numa área</h2>
        <p>Descarregar uma tabela com os taxa existentes dentro do polígono fornecido, e o respectivo EOO e AOO.</p>
        <p>Paste a polygon in WKT format here, in latitude longitude coordinates, for example:<br/><code>Polygon ((-7.6167 37.9335, -7.6221 37.9320, -7.6228 37.9285, -7.6185 37.9256, -7.6144 37.9249, -7.6128 37.9287, -7.6127 37.9304, -7.6146 37.9325, -7.6167 37.9335))</code><br/><span class="info">You can copy a polygon from QGIS and paste it!</span></p>
        <p>NOTE: polygons with holes and multipart polygons are NOT supported.</p>
        <input type="hidden" name="territory" value="${territory}"/>
        <textarea style="width: 98%; height: 150px; border: 2px solid #1e88e5; margin: 0 1%; padding: 4px; font-size: 0.75em; border-radius: 3px;" name="polygon"></textarea>
        <input type="submit" value="Descarregar" class="textbutton"/>
    </form>
    </c:if>

    <form class="orderdownload" action="api/downloadallsummaries" method="post">
        <h2>Documento de texto com todos os sumários (9.3)</h2>
        <input type="hidden" name="territory" value="${territory}"/>
        <p>Para filtrar por TaxEntID, introduzir os IDs separados por vírgulas<p>
        <textarea style="width: 98%; height: 150px; border: 2px solid #1e88e5; margin: 0 1%; padding: 4px; font-size: 0.75em; border-radius: 3px;" name="texentids"></textarea>
        <input type="submit" value="Descarregar" class="textbutton"/>
    </form>
    <c:if test="${jobs.size() > 0}">
        <a name="jobs"></a>
        <h1><fmt:message key="Downloads.2"/></h1>
        <table>
            <tr>
                <th>Download type</th>
                <th>Date started</th>
                <th>Owner</th>
                <th>Ready</th>
                <th>Status</th>
                <th>Download</th>
            </tr>
            <c:forEach var="job" items="${jobs}">
            <tr>
                <td>${job.getDescription()}</td>
                <td>${job.getDateSubmitted()}</td>
                <td>${job.getOwner() != null ? job.getOwner().getName() : '-'}</td>
                <td><t:yesno test="${job.isReady()}"/></td>
                <td>${job.getState()}</td>
                <td>
                    <c:if test="${job.isFileDownload() && job.isReady()}">
                    <a href="../job/${job.getID()}">Download file</a>
                    </c:if>
                </td>
            </tr>
            </c:forEach>
        </table>
    </c:if>
</c:if>
