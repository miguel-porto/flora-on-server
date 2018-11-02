<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <style type="text/css">
        body {font-family: Arial, Helvetica, Sans Serif;}

        h1 {margin:0; padding:10px; background-color:#2196f3; color:white; font-size:1.6em;}

        .header {
            border: 0px solid black;
            padding:0px;
            background-color:#90caf9;
        }

        .padder {padding:10px;}

        table {
            width:100%;
            text-align:left;
        }

        table td {padding:2px;}

        .header table td {background-color: #bbdefb}

        table.compact {border:2px solid #90caf9}
        table.compact td {padding:2px;font-size:0.8em;}
        table th {padding:0px; border-bottom: 0px solid black;}
        table.compact th {padding:4px;}

        page-after {
          display: block;
/*          page-break-after: always;*/
          page-break-inside: avoid;
          margin-bottom: 20px;
        }
    </style>
</head>
<body>
    <c:forEach var="inv" items="${inventories}">
    <page-after>
    <div class="header">
        <h1>Inventário ${inv.getCode() == null ? inv._getCoordinates() : inv.getCode()}</h1>
        <div class="padder">
            <table cellspacing="4px">
                <tr><th>Coordenadas</th><th>Data</th><th>Observadores</th><th>Nome do local</th></tr>
                <tr><td>${inv._getCoordinates()}</td><td>${inv._getDate()}</td><td>${inv._getObserverNames()[0]}</td><td>${inv.getLocality()}</td></tr>
            </table>
            <table cellspacing="4px">
                <tr><th>Descrição do habitat</th><th>Ameaças gerais</th></tr>
                <tr><td>${inv.getHabitat() == null ? '-' : inv.getHabitat()}</td><td>${inv.getThreats() == null ? '-' : inv.getThreats()}</td></tr>
            </table>
        </div>
    </div>
    <table class="compact">
        <tr><th>Taxon</th><th>Fenologia</th><th>Abundância</th><th>Notas</th></tr>
        <c:forEach var="occ" items="${inv._getTaxa()}">
        <tr>
            <td><c:out value="${occ.getTaxEnt() == null ? occ.getVerbTaxon() : occ.getTaxEnt().getName()}"/></td>
            <td>${occ.getPhenoState() == null ? '-' : occ.getPhenoState().getLabel()}</td>
            <td>${occ.getAbundance() == null ? '-' : occ.getAbundance()}</td>
            <td><c:out value="${occ.getComment() == null ? '-' : occ.getComment()}"/></td>
        </tr>
        </c:forEach>
    </table>
    </page-after>

    </c:forEach>
</body>
</html>