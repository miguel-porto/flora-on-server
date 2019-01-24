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

        svg {width:300px}

        td {padding:2px;}

        .header table td {background-color: #bbdefb}

        table.compact {border:2px solid #90caf9}
        table.compact td {padding:2px;font-size:0.8em;}
        table th {padding:0px; border-bottom: 0px solid black;}
        table.compact th {padding:4px;}

        page-after {
          display: block;
          page-break-inside: avoid;
          margin-bottom: 20px;
        }
    </style>
</head>
<body>
    <jsp:include page="/api/svgmap?basemap=1&size=10000&border=1&shadow=0&close=false&taxon=taxent%2F335830856606"></jsp:include>

    <page-after>
    taxon._getIDURLEncoded()
    </page-after>

</body>
</html>