<%@ tag description="Filter help" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<h1>Como usar os filtros avançados</h1>
<p>Pode fazer filtros compostos por vários campos, os quais devolvem os registos que simultaneamente cumprem todas as condições. Em alternativa, pode usar um filtro simples textual livre, que procurará em vários campos de texto simultaneamente.</p>
<p>Para definir um filtro para um campo concreto, escreva o nome do campo seguido de <code>:</code>, e em seguida o filtro que pretende (pode incluir espaços). Por exemplo: <code>date:12-8-2009</code></p>
<p>Pode combinar quantos filtros quiser, por exemplo <code>date:?-8-2017 phen:f lat:&lt;38.4</code> devolve todos os registos marcados como em floração, observados em qualquer dia de Agosto de 2017 numa latitude menor que 38.4º.</p>
<p>Os nomes de campos permitidos de momento são:</p>
<table class="smalltext">
    <thead><tr><th>Filtro</th><th>Campo</th><th>Tipo</th></tr></thead>
    <tbody>
        <tr><td><code>date:</code></td><td>Data de observação</td><td>Data e intervalo de datas (ver em baixo)</td></tr>
        <tr><td><code>conf:</code></td><td>Confiança na identificação</td><td>Valores pré-definidos: <code>c</code> certo <code>a</code> quase certo <code>d</code> duvidoso</td></tr>
        <tr><td><code>phen:</code></td><td>Estado fenológico</td><td>Valores pré-definidos: <code>f</code> floração <code>d</code> dispersão <code>fd</code> floração+dispersão <code>v</code> vegetativo <code>r</code> dormência <code>c</code> fruto imaturo <code>fc</code> flor+fruto imaturo <code>b</code> em botão</td></tr>
        <tr><td><code>prec:</code></td><td>Precisão da coordenada</td><td>Numérico com unidade</td></tr>
        <tr><td><code>lat:</code></td><td>Latitude</td><td>Numérico e intervalos numéricos (ver em baixo)</td></tr>
        <tr><td><code>long:</code></td><td>Longitude</td><td>Numérico e intervalos numéricos (ver em baixo)</td></tr>
        <tr><td><code>tax:</code></td><td>Taxon</td><td>Texto livre com <i>wildcards</i> (<code>*</code>)</td></tr>
        <tr><td><code>tag:</code></td><td>Etiquetas</td><td>Texto livre com <i>wildcards</i> (<code>*</code>)</td></tr>
        <tr><td><code>local:</code></td><td>Nome do local</td><td>Texto livre com <i>wildcards</i> (<code>*</code>)</td></tr>
        <tr><td><code>code:</code></td><td>Código do local</td><td>Texto livre com <i>wildcards</i> (<code>*</code>)</td></tr>
        <tr><td><code>gps:</code></td><td>Código GPS</td><td>Texto livre com <i>wildcards</i> (<code>*</code>)</td></tr>
        <tr><td><code>proj:</code></td><td>Projecto ou instituição</td><td>Texto livre com <i>wildcards</i> (<code>*</code>)</td></tr>
        <tr><td><code>priv:</code></td><td>Notas privadas da ocorrência</td><td>Texto livre com <i>wildcards</i> (<code>*</code>)</td></tr>
        <tr><td><code>acc:</code></td><td>Código de herbário (<i>accession</i>)</td><td>Texto livre com <i>wildcards</i> (<code>*</code>)</td></tr>
        <tr><td><code>obs:</code></td><td>Observador</td><td>Texto livre com <i>wildcards</i> (<code>*</code>)</td></tr>
        <tr><td><code>coll:</code></td><td>Colector</td><td>Texto livre com <i>wildcards</i> (<code>*</code>)</td></tr>
        <tr><td><code>maint:</code></td><td>Responsável pelo registo</td><td>Texto livre com <i>wildcards</i> (<code>*</code>)</td></tr>
        <tr><td><code>uid:</code></td><td>Responsável, observador, colector ou determinador</td><td>Identificador de utilizador</td></tr>
        <tr><td><code>ilat:</code></td><td>Latitude do inventário</td><td>Numérico e intervalos numéricos (ver em baixo)</td></tr>
        <tr><td><code>ilong:</code></td><td>Longitude do inventário</td><td>Numérico e intervalos numéricos (ver em baixo)</td></tr>
        <tr><th colspan="3">Filtros especiais</th></tr>
        <tr><td><code>nsp:</code></td><td>Nº de taxa registados por inventário</td><td>Numérico e intervalos numéricos (ver em baixo)</td></tr>
    </tbody>
</table>
<p>Qualquer dos filtros pode ser definido com o valor <code>na</code> para pesquisar registos cujo campo respectivo esteja vazio.</p>
<h2>Filtrar por texto livre</h2>
<p>Nos filtros textuais (ver tabela supra), use asteriscos <code>*</code> para significar qualquer sequência de caracteres.
Por exemplo, <code>tax:lavandula</code> irá devolver apenas os registos cujo taxon está definido ao nível do género <i>Lavandula</i> (sem espécie), enquanto que <code>tax:lavandula*</code>
irá devolver todos os registos das várias espécies de <i>Lavandula</i>. Similarmente, <code>tax:e*wel*</code> ira devolver os registos dos taxa que começam por <code>e</code> e que têm
a sequência <code>wel</code> algures no nome (género, espécie ou autoria), por exemplo, <i>Euphorbia welwitschii</i>.</p>
<h2>Filtrar por datas e intervalos de datas</h2>
<table class="smalltext">
    <thead><tr><th>Filtro</th><th>Devolve</th></tr></thead>
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
<h2>Filtrar por números e intervalos numéricos</h2>
<p>Aplica-se aos filtros de coordenadas <code>lat:</code> <code>long:</code> e nº de taxa <code>nsp:</code>. Os intervalos numéricos podem ser abertos ou fechados.
No caso de coordenadas, se for especificada uma coordenada exacta (e não um intervalo), a pesquisa será feita num raio de poucos metros em torno desse ponto.</p>
<table class="smalltext">
    <thead><tr><th>Filtro</th><th>Devolve</th></tr></thead>
    <tbody>
    <tr><td><code>lat:38.7-39.1 long:-7.7--8</code></td><td>Registos observados no quadrado definido pelas latitudes 38.7º e 39.1º e as longitudes -7.7º e -8.0º</td></tr>
    <tr><td><code>lat:&gt;40.3</code></td><td>Registos observados acima da latitude 40.3º</td></tr>
    <tr><td><code>long:na</code></td><td>Registos sem a longitude definida</td></tr>
    <tr><td><code>lat:38.5301 long:-8.0168</code></td><td>Registos num raio de poucos metros em torno do ponto definido</td></tr>
    <tr><td><code>nsp:&gt;8</code></td><td>Inventários com mais de 8 taxa registados</td></tr>
    <tr><td><code>nsp:0</code></td><td>Inventários vazios, sem taxa registados</td></tr>
    </tbody>
</table>
<h2>Filtrar na vista de inventários</h2>
<p>Os filtros funcionam da mesma forma na vista de ocorrências e de inventários. Contudo, na vista de inventários, os resultados que são devolvidos são todos os inventários em que, pelo
menos um dos taxa a ele pertencentes corresponda ao filtro na totalidade. Ou seja, <code>conf:d phen:f</code> irá devolver todos os inventários em que pelo menos um dos taxa esteja
marcado como Em Floração <b>e</b> Duvidoso.</p>
