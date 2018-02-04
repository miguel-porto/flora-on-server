<%@ page session="false" %>
<%@ page pageEncoding="UTF-8" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Flora-On error</title>
	<link href='//fonts.googleapis.com/css?family=Lato:300' rel='stylesheet' type='text/css'>
	<link rel="stylesheet" type="text/css" href="base.css?nocache=${uuid}"/>
</head>
<body style="text-align:center" class="enterpage">
    <div class="outer">
        <img src="images/logo-LV-cor-fundoclaro_800.png" alt="logo"/>
        <div style="width:100%"></div>
        <h1 style="margin:40px 0">Oops, there was an error: ${error}</h1>
    </div>
    <div id="logobar">
        <div><p>Coordenação</p><div class="logos"><img src="images/logo_SPBotanica.png"/><img src="images/logo_Phytos.jpg"/></div></div>
        <div><p>Parceria</p><div class="logos"><img src="images/logo_ICNF.png"/></div></div>
        <div><p>Co-financiamento</p><div class="logos"><img src="images/logo_poseur.png"/><img src="images/logo_Portugal_2020.png"/><img src="images/logo_UE.png"/><img src="images/logo_FundoAmbiental.png"/></div></div>
        <div><p>Apoio</p><div class="logos"><img src="images/logo_INCD.png"/></div></div>
    </div>
    <a href="https://github.com/miguel-porto/flora-on-server">
    <svg style="position: absolute; top: 0; left: 0; border: 0; width:48px; height:48px; margin:6px" version="1.1" viewBox="0 0 16 16"><path fill-rule="evenodd" d="M8 0C3.58 0 0 3.58 0 8c0 3.54 2.29 6.53 5.47 7.59.4.07.55-.17.55-.38 0-.19-.01-.82-.01-1.49-2.01.37-2.53-.49-2.69-.94-.09-.23-.48-.94-.82-1.13-.28-.15-.68-.52-.01-.53.63-.01 1.08.58 1.23.82.72 1.21 1.87.87 2.33.66.07-.52.28-.87.51-1.07-1.78-.2-3.64-.89-3.64-3.95 0-.87.31-1.59.82-2.15-.08-.2-.36-1.02.08-2.12 0 0 .67-.21 2.2.82.64-.18 1.32-.27 2-.27.68 0 1.36.09 2 .27 1.53-1.04 2.2-.82 2.2-.82.44 1.1.16 1.92.08 2.12.51.56.82 1.27.82 2.15 0 3.07-1.87 3.75-3.65 3.95.29.25.54.73.54 1.48 0 1.07-.01 1.93-.01 2.2 0 .21.15.46.55.38A8.013 8.013 0 0 0 16 8c0-4.42-3.58-8-8-8z"></path></svg>
    </a>
</body>
</html>

