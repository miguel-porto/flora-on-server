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


window.addEventListener('beforeunload', function (ev) {
    if(isFormSubmitting) return;
    var confirmationMessage = 'You have unsaved occurrences! Are you sure you want to lose them?';
    var sel = document.querySelectorAll('#newoccurrencetable tbody tr');
    if(sel.length > 0) {
        (ev || window.event).returnValue = confirmationMessage;
        return confirmationMessage;
    } else
        return null;
});

document.addEventListener('DOMContentLoaded', function() {
    myMap = L.map('mapcontainer', {zoomSnap: 0, markerZoomAnimation: false}).setView([37.5, -8], 12);
//    L.tileLayer.provider('Esri.WorldImagery').addTo(mymap);
    L.tileLayer.bing({imagerySet:'AerialWithLabels', bingMapsKey: 'AiPknGGGT9nQtbl5Rpa_fhMQxthyZrh5z_bAc-ESzNaaqwQYcyEthgHB-_WowOEP'}).addTo(myMap);

    myMap.on('click', addNewOccurrence);

    attachFormPosters(fileUploadCallback);
    attachOccurrenceTableEvents();

    attachSuggestionHandler('taxonsearchbox', '/floraon/checklist/api/suggestions?limit=20&q=', 'suggestionstaxon', function(ev, name, key, parent, dry) {
        if(!dry) return;
        document.getElementById('taxonsearchwrapper-holder').appendChild(document.getElementById('taxonsearchwrapper'));
        parent.innerHTML = name;
        var id = getParentbyTag(parent, 'tr').getAttribute('data-id');
        if(id)
            insertOrReplaceHiddenInput(parent, id + '_taxa', name);
        else
            insertOrReplaceHiddenInput(parent, 'taxa', name);
    }, true, ',');


    var buttons = document.querySelectorAll('.button:not(.anchorbutton)');
    for(var i = 0; i < buttons.length; i++) {
        addEvent('click', buttons[i], clickButton);
    }

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

function clickButton(ev) {
    var b = getParentbyClass(ev.target, 'button');
    switch(b.id) {
    case 'hidemap':
        ev.target.classList.toggle('selected');
        document.getElementById('occurrencemap').classList.toggle('hidden');
        break;

    case 'deleteselected':
        acceptVisibleSearchbox();
        var sel = document.querySelectorAll('#occurrencetable tr.selected');
        if(sel.length == 0) return;
        var tbody = document.querySelector('#deleteoccurrencetable tbody');
        for(var i=0; i<sel.length; i++)
            tbody.appendChild(sel[i]);

        document.getElementById('deleteoccurrences').classList.remove('hidden');
        break;

    case 'deleteselectednew':
        acceptVisibleSearchbox();
        var sel = document.querySelectorAll('#newoccurrencetable tr.selected');
        var tab = document.getElementById('newoccurrencetable');
        if(sel.length == 0) return;
        for(var i=0; i<sel.length; i++) {
            if(sel[i].marker) {
                sel[i].marker.remove();
            }
            sel[i].parentNode.removeChild(sel[i]);
        }
        break;
    }
}

function clickOccurrenceTable(ev) {
    var cell = getParentbyTag(ev.target, 'td');
    if(!cell.classList) return;
    if(cell.classList.contains('taxon')) { // clicked taxon cell
        if(cell.querySelector('#taxonsearchwrapper')) return;
        var txt = cell.textContent;
        cell.textContent = '';
        displayTaxonSearchbox(cell, txt);
    } else if(cell.classList.contains('select')) {    // select row
        var tr = getParentbyTag(cell, 'tr');
        if(tr.marker) {
            cell.querySelector('.selectbutton').classList.toggle('selected');
            tr.classList.toggle('selected');
            tr.marker.classList.toggle('selected');
        }
    }
}

function attachOccurrenceTableEvents() {
    // click on an occurrence table
    addEvent('click', document.getElementById('occurrencetable'), clickOccurrenceTable);
    addEvent('click', document.getElementById('newoccurrencetable'), clickOccurrenceTable);

    var ot = document.querySelectorAll('.occurrencetable tr');
    var coo;
    for(var i=0; i<ot.length; i++) {
        coo = ot[i].querySelector('td.coordinates');
        if(coo == null) continue;

        addPointMarker(parseFloat(coo.getAttribute('data-lat')), parseFloat(coo.getAttribute('data-lng')), ot[i]);
    }
}

function addPointMarker(lat, lng, bondEl) {
    var marker = L.marker([lat, lng], {icon: redCircle, draggable: false, keyboard: false});
    marker.on('click', markerClick).addTo(myMap);
    if(bondEl) {
        marker.tableRow = bondEl;
        bondEl.marker = marker._icon;
    }
}

function acceptVisibleSearchbox() {
    if(!document.getElementById('taxonsearchwrapper')) return;
    while(document.getElementById('taxonsearchwrapper').offsetParent !== null) {
        var event = new KeyboardEvent('keyup', { 'keyCode': 13});
        document.getElementById('taxonsearchbox').dispatchEvent(event);
    }
}

function displayTaxonSearchbox(el, text) {
    var old = document.getElementById('taxonsearchwrapper');
    if(!el.querySelector('#taxonsearchwrapper') && old.offsetParent !== null) {     // there's one visible in other cell
        var txt = old.querySelector('textarea').value;
        var par = old.parentNode;
        document.getElementById('taxonsearchwrapper-holder').appendChild(old);
        par.innerHTML = txt;
        var id = getParentbyTag(par, 'tr').getAttribute('data-id');
        if(id)
            insertOrReplaceHiddenInput(par, id + '_taxa', txt);
        else
            insertOrReplaceHiddenInput(par, 'taxa', txt);
    }

    el.appendChild(old);
    var inp = old.querySelector('textarea');
    inp.value = text;
    inp.setSelectionRange(0, inp.value.length);
    inp.focus();
}

function markerClick(ev) {
    if(ev.target.tableRow) {
        if(ev.target.tableRow.querySelector('.select .selectbutton'))
            ev.target.tableRow.querySelector('.select .selectbutton').classList.toggle('selected');

        ev.target._icon.classList.toggle('selected');
        ev.target.tableRow.classList.toggle('selected');
    }
}

function fileUploadCallback(resp, ev) {
    if(ev.target.getAttribute('data-refresh') != 'false')
        window.location.reload();
    else {
        if(resp.success) alert(resp.msg);
    }
}

function addNewOccurrence(ev) {
    var id = randomString(6);
    var lat = Math.round(ev.latlng.lat * 100000) / 100000;
    var lng = Math.round(ev.latlng.lng * 100000) / 100000;

    var tab = document.querySelector('#newoccurrencetable tbody');
    var row = document.createElement('tr');
    var cell0 = document.createElement('td');
    var cell1 = document.createElement('td');
    var cell2 = document.createElement('td');
    var cell3 = document.createElement('td');
    var selectbut = document.createElement('div');
    var inp_latitude = createHiddenInputElement(id + '_latitude', lat);
    var inp_longitude = createHiddenInputElement(id + '_longitude', lng);

    row.setAttribute('data-id', id);
    cell0.classList.add('select');
    selectbut.classList.add('selectbutton');
    cell0.appendChild(selectbut);
    cell1.classList.add('taxon');
    cell2.innerHTML = lat + ', ' + lng;
    cell2.appendChild(inp_latitude);
    cell2.appendChild(inp_longitude);
    cell2.setAttribute('data-lat', lat);
    cell2.setAttribute('data-lng', lng);
    addPointMarker(ev.latlng.lat, ev.latlng.lng, row);

    row.appendChild(cell0);
    row.appendChild(cell1);
    row.appendChild(cell2);
    row.appendChild(cell3);
    tab.appendChild(row);

    document.getElementById('addnewoccurrences').classList.remove('hidden');
}

function createHiddenInputElement(name, value) {
    var inp = document.createElement('input');
    inp.setAttribute('name', name);
    inp.setAttribute('type', 'hidden');
    inp.setAttribute('value', value);
    return inp;
}

function insertOrReplaceHiddenInput(parent, name, value) {
    var el = parent.querySelector('input[type=hidden][name=\'' + name +'\']');
    if(!el)
        parent.appendChild(createHiddenInputElement(name, value));
    else
        el.setAttribute(name, value);
}