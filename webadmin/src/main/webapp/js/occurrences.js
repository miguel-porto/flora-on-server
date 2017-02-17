var Esri_WorldImagery = L.tileLayer('http://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}', {
    attribution: 'Tiles &copy; Esri &mdash; Source: Esri, i-cubed, USDA, USGS, AEX, GeoEye, Getmapping, Aerogrid, IGN, IGP, UPR-EGP, and the GIS User Community'
});

document.addEventListener('DOMContentLoaded', function() {
    var mymap = L.map('mapcontainer').setView([37.5, -8], 12);
//    L.tileLayer.provider('Esri.WorldImagery').addTo(mymap);
    L.tileLayer.bing({imagerySet:'AerialWithLabels', bingMapsKey: 'AiPknGGGT9nQtbl5Rpa_fhMQxthyZrh5z_bAc-ESzNaaqwQYcyEthgHB-_WowOEP'}).addTo(mymap);

    attachFormPosters(fileUploadCallback);

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

function fileUploadCallback(resp) {
    if(resp.success) alert("AAAA"+resp.msg);
}