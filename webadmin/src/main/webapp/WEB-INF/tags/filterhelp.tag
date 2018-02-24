<%@ tag description="Filter help" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<h1>Como usar os filtros avançados</h1>
<p>Pode fazer filtros compostos por vários campos, os quais devolvem os registos que simultaneamente cumprem todas as condições. A estes, pode ser adicionado um filtro textual livre, que procurará em vários campos de texto simultaneamente.</p>
<p>Para definir um filtro para um campo concreto, escreva o nome do campo seguido de <code>:</code>, e em seguida o filtro que pretende. Por exemplo: <code>date:12-8-2009</code></p>
<p>Pode combinar quantos filtros quiser, por exemplo <code>date:?-8-2017 phen:f lat:&lt;38.4</code> devolve todos os registos marcados como em floração, observados em qualquer dia de Agosto de 2017 numa latitude menor que 38.4º.</p>
<p>Os nomes de campos permitidos de momento são:<br/>
<code>date:</code> data de observação<br/><code>conf:</code> confiança na identificação<br/><code>phen:</code> estado fenológico<br/><code>prec:</code> precisão<br/>
<code>lat:</code> latitude<br/><code>long:</code> longitude<br/><code>tax:</code> taxon<br/><code>local:</code> nome do local<br/><code>code:</code> código do local
</p>
<p>Qualquer dos filtros pode ser definido com o valor <code>na</code> para pesquisar registos cujo campo respectivo esteja vazio.</p>
<p>Nos filtros textuais (<code>tax:</code> <code>local:</code> <code>code:</code>), use asteriscos <code>*</code> para significar qualquer sequência de caracteres.
Por exemplo, <code>tax:lavandula</code> irá devolver apenas os registos cujo taxon está definido ao nível do género <i>Lavandula</i> (sem espécie), enquanto que <code>tax:lavandula*</code>
irá devolver todos os registos das várias espécies de <i>Lavandula</i>. Similarmente, <code>tax:e*wel*</code> ira devolver os registos dos taxa que começam por <code>e</code> e que têm
a sequência <code>wel</code> algures no nome (género, espécie ou autoria).</p>
<h2>Filtrar por datas e intervalos de datas</h2>
<table class="smalltext">
    <thead><tr><th>Filtro (n.b. sem espaços)</th><th>Devolve</th></tr></thead>
    <tbody>
    <tr><td><code>date:na</code></td><td>Registos sem data de observação</td></tr>
    <tr><td><code>date:12/8/2002</code></td><td>Registos observados nesta data concreta</td></tr>
    <tr><td><code>date:?/11/?</code></td><td>Registos observados no mês de Novembro, em qualquer ano e qualquer dia</td></tr>
    <tr><td><code>date:20/11/?</code></td><td>Registos observados no dia 20 de Novembro de qualquer ano</td></tr>
    <tr><td><code>date:2004</code></td><td>Registos observados em 2004</td></tr>
    <tr><td><code>date:10/2/2005-24/5/2006</code></td><td>Registos observados no intervalo de datas especificado</td></tr>
    <tr><td><code>date:1/2008-3/2009</code></td><td>Registos observados entre Janeiro de 2008 e Março de 2009, inclusivé</td></tr>
    <tr><td><code>date:2005-2007</code></td><td>Registos observados de 2005 a 2007, inclusivé</td></tr>
    </tbody>
</table>
<h2>Filtrar por coordenadas e intervalos de coordenadas</h2>
<p>Os intervalos de coordenadas podem ser abertos ou fechados. Se for especificada uma coordenada exacta (e não um intervalo), a pesquisa será feita num raio de poucos metros em torno desse ponto.</p>
<table class="smalltext">
    <thead><tr><th>Filtro (n.b. sem espaços)</th><th>Devolve</th></tr></thead>
    <tbody>
    <tr><td><code>lat:38.7-39.1 long:-7.7--8</code></td><td>Registos observados no quadrado definido pelas latitudes 38.7º e 39.1º e as longitudes -7.7º e -8.0º</td></tr>
    <tr><td><code>lat:>40.3</code></td><td>Registos observados acima da latitude 40.3º</td></tr>
    <tr><td><code>long:na</code></td><td>Registos sem a longitude definida</td></tr>
    <tr><td><code>lat:38.5301 long:-8.0168</code></td><td>Registos num raio de poucos metros em torno do ponto definido</td></tr>
    </tbody>
</table>
<h2>Estado fenológico</h2>
<p>São admitidos os acrónimos:<br/>
<code>phen:f</code> floração<br/>
<code>phen:d</code> dispersão<br/>
<code>phen:fd</code> floração+dispersão<br/>
<code>phen:v</code> vegetativo<br/>
<code>phen:r</code> dormência<br/>
<code>phen:c</code> fruto imaturo<br/>
<code>phen:fc</code> flor+fruto imaturo
</p>
<h2>Filtrar na vista de inventários</h2>
<p>Os filtros funcionam da mesma forma na vista de ocorrências e de inventários. Contudo, na vista de inventários, os resultados que são devolvidos são todos os inventários em que, pelo
menos um dos taxa a ele pertencentes corresponda ao filtro na totalidade. Ou seja, <code>conf:d phen:f</code> irá devolver todos os inventários em que pelo menos um dos taxa esteja
marcado como Em Floração <b>e</b> Duvidoso.</p>
