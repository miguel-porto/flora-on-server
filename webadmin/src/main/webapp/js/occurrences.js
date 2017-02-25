var myMap = null;
//var redCircle = L.divIcon({className: 'redcircleicon', bgPos: [-5, -5]});
var redCircle = L.icon({
    iconUrl: 'images/redcircle.png',
    iconSize: [12, 12],
    iconAnchor: [6, 6],
});
var Esri_WorldImagery = L.tileLayer('http://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}', {
    attribution: 'Tiles &copy; Esri &mdash; Source: Esri, i-cubed, USDA, USGS, AEX, GeoEye, Getmapping, Aerogrid, IGN, IGP, UPR-EGP, and the GIS User Community'
});

document.addEventListener('DOMContentLoaded', function() {
    myMap = L.map('mapcontainer', {zoomSnap: 0, markerZoomAnimation: false}).setView([37.5, -8], 12);
//    L.tileLayer.provider('Esri.WorldImagery').addTo(mymap);
    L.tileLayer.bing({imagerySet:'AerialWithLabels', bingMapsKey: 'AiPknGGGT9nQtbl5Rpa_fhMQxthyZrh5z_bAc-ESzNaaqwQYcyEthgHB-_WowOEP'}).addTo(myMap);

    attachFormPosters(fileUploadCallback);
    attachOccurrenceTableEvents();

/*
    var viewer = OpenSeadragon({
        id: 'mapcontainer',
        showNavigator: false,
        wrapHorizontal: true,
        zoomPerScroll: 1.2,
        minZoomImageRatio:0.5,
        tileSources: {
            height: 512*256,
            width:  512*256,
            tileSize: 256,
            minLevel: 8,
            getTileUrl: function( level, x, y ){
                return "http://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/" +
                        (level-8) + "/" + y + "/" + x + ".jpg";
            }
        }
    });
*/
});

function attachOccurrenceTableEvents() {
    var ot = document.querySelectorAll('table.occurrencetable tr');
    var coo;
    for(var i=0; i<ot.length; i++) {
        coo = ot[i].querySelector('td.coordinates');
        if(coo == null) continue;
        var marker = L.marker([parseFloat(coo.getAttribute('data-lat')), parseFloat(coo.getAttribute('data-lng'))]
            , {icon: redCircle, draggable: true, keyboard: false});
        marker.on('click', markerClick).addTo(myMap);
        marker.tableRow = ot[i];
        ot[i].marker = marker._icon;
        addEvent('click', ot[i], function(ev) {
            var tr = getParentbyTag(ev.target, 'tr');
            if(tr.marker) {
                tr.classList.toggle('selected');
                tr.marker.classList.toggle('selected');
            }
        })
    }
}

function markerClick(ev) {
    if(ev.target.tableRow) {
        ev.target._icon.classList.toggle('selected');
        ev.target.tableRow.classList.toggle('selected');
    }
}

function fileUploadCallback(resp) {
    if(resp.success) alert("AAAA"+resp.msg);
}