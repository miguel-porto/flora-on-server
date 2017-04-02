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
    myMap = L.map('mapcontainer', {zoomSnap: 0, markerZoomAnimation: false}).setView([39.5, -8.1], 8);
//    L.tileLayer.provider('Esri.WorldImagery').addTo(mymap);
    L.tileLayer.bing({imagerySet:'AerialWithLabels', bingMapsKey: 'AiPknGGGT9nQtbl5Rpa_fhMQxthyZrh5z_bAc-ESzNaaqwQYcyEthgHB-_WowOEP'}).addTo(myMap);

    myMap.on('click', addNewFeature);

    attachFormPosters(fileUploadCallback);
    // click on an occurrence table
    var ot = document.querySelectorAll('.occurrencetable');
    for(var i=0; i<ot.length; i++)
        addEvent('click', ot[i], clickOccurrenceTable);

    projectPointsOnMap();

    attachSuggestionHandler('taxonsearchbox', '/floraon/checklist/api/suggestions?limit=20&q=', 'suggestionstaxon', onConfirmEdit, true, ',', tabHandler);
    attachSuggestionHandler('editfield', null, null, onConfirmEdit, true, null, tabHandler);

    var buttons = document.querySelectorAll('.button:not(.anchorbutton)');
    for(var i = 0; i < buttons.length; i++) {
        addEvent('click', buttons[i], clickButton);
    }

/*
    var inventories = document.querySelectorAll('.inventory');
    for(var i = 0; i < inventories.length; i++) {
    // FIXME
        addEvent('click', inventories[i].querySelector('.button'), addNewTaxon);
        doMouseClick(inventories[i].querySelector('.button'));
    }
*/
});

window.addEventListener('beforeunload', function (ev) {
    if(isFormSubmitting) return;
    var confirmationMessage = 'You have unsaved occurrences! Are you sure you want to lose them?';
    var sel = document.querySelectorAll('.newoccurrencetable tbody tr:not(.dummy)');
    if(sel.length > 0) {
        (ev || window.event).returnValue = confirmationMessage;
        return confirmationMessage;
    } else
        return null;
});

function onConfirmEdit(ev, name, key, parent, dry) {
    if(!dry) return;
    var fieldname = parent.getAttribute('data-name');
    document.getElementById('taxonsearchwrapper-holder').appendChild(ev.target.parentNode);
    var changed = (parent.innerHTML.trim() != name.trim());
    if(!changed) return;

    parent.innerHTML = name;

    if(getParentbyClass(parent, 'id1holder'))
        var id1 = getParentbyClass(parent, 'id1holder').getAttribute('data-id');

    if(getParentbyClass(parent, 'id2holder'))
        var id2 = getParentbyClass(parent, 'id2holder').getAttribute('data-id');

    if(id1 && id2) {
        insertOrReplaceHiddenInput(parent, id1 + '_' + id2 + '_' + fieldname, name);
        var inv = getParentbyClass(parent, 'inventory');
        if(inv) {   // we're in the add new occurrences. add new row if needed.
            var lastRow = getParentbyClass(parent, 'newoccurrencetable').querySelector('tbody tr:last-of-type');
            if(lastRow.querySelectorAll('input[type=hidden]').length > 0) { // only add if there is no empty row already
                doMouseClick(inv.querySelector('.button'));
            }
        }
    } else if(id1) {
        insertOrReplaceHiddenInput(parent, id1 + '_' + fieldname, name);
    }
}

function tabHandler(ev) {
    if(ev.keyCode == 9) {
        ev.preventDefault();
        var cell = ev.target.parentNode.parentNode;
        var next = getNextSiblingByClass(cell, 'editable');
        if(next) doMouseClick(next);
    }
}

function doMouseClick(el) {
    var event = new MouseEvent('click', {bubbles: true});
    el.dispatchEvent(event);
}

function clickButton(ev) {
    var b = getParentbyClass(ev.target, 'button');

    switch(b.id) {
    case 'hidemap':
        ev.target.classList.toggle('selected');
        document.getElementById('occurrencemap').classList.toggle('hidden');
        break;

    case 'selectpoints':
        var areaSelect = L.areaSelect({width:200, height:300});
        areaSelect.addTo(myMap);
        break;

    case 'mergeocc':
        var sel = document.querySelectorAll('#alloccurrences .geoelement.selected');
        var form = document.querySelector('#mergeoccurrencetable tbody');
        form.innerHTML = '';
        if(sel.length > 0) {
            for(var i=0; i<sel.length; i++) {
                form.appendChild(sel[i].cloneNode(true));
            }

            document.getElementById('mergeoccurrences').classList.remove('hidden');
        }
        break;

    case 'deleteselected':
        acceptVisibleSearchbox();
        var sel = document.querySelectorAll('#alloccurrencetable tr.selected');
        if(sel.length == 0) return;
        var tbody = document.querySelector('#deleteoccurrencetable tbody');
        for(var i=0; i<sel.length; i++)
            tbody.appendChild(sel[i]);

        document.getElementById('deleteoccurrences').classList.remove('hidden');
        break;

    case 'deleteselectednew':
        acceptVisibleSearchbox();
        var sel = document.querySelectorAll('.newoccurrencetable tr.selected');
        if(sel.length == 0) sel = document.querySelectorAll('#addoccurrencetable tr.selected');
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

function addNewTaxon(ev) {
    var id = randomString(6);
    var inv = getParentbyClass(ev.target, 'inventory');
    var row = inv.querySelector('.newoccurrencetable tr.dummy').cloneNode(true);
    row.classList.remove('dummy');
    row.setAttribute('data-id', id);
    inv.querySelector('.newoccurrencetable tbody').appendChild(row);
}

function clickOccurrenceTable(ev) {
    var cell = getParentbyClass(ev.target, 'editable') || getParentbyClass(ev.target, 'clickable');
    if(!cell || !cell.classList) return;
console.log(cell);
    if(cell.classList.contains('taxon')) { // clicked taxon cell
        if(cell.querySelector('#taxonsearchwrapper')) return;
        var txt = cell.textContent;
        cell.textContent = '';
        displayTaxonSearchbox(cell, txt);
    } else if(cell.classList.contains('select')) {    // select row
        var tr = getParentbyTag(cell, 'tr');
        cell.querySelector('.selectbutton').classList.toggle('selected');
        tr.classList.toggle('selected');
        if(tr.marker) tr.marker.classList.toggle('selected');
    } else if(cell.classList.contains('editable')) {    // select row
        if(cell.querySelector('#editfieldwrapper')) return;
        var txt = cell.textContent;
        cell.textContent = '';
        displayEditField(cell, txt);
    }
}

function projectPointsOnMap() {
    var ot = document.querySelectorAll('.geoelement');
    var coo;
    for(var i=0; i<ot.length; i++) {
        coo = ot[i].querySelector('*:not(.geoelement) .coordinates');
        if(coo == null || !coo.getAttribute('data-lat')) continue;

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
    if(document.getElementById('taxonsearchwrapper')) {
        while(document.getElementById('taxonsearchwrapper').offsetParent !== null) {
            var event = new KeyboardEvent('keyup', { 'keyCode': 13});
            document.getElementById('taxonsearchbox').dispatchEvent(event);
        }
    }

    if(document.getElementById('editfieldwrapper')) {
        while(document.getElementById('editfieldwrapper').offsetParent !== null) {
            var event = new KeyboardEvent('keyup', { 'keyCode': 13});
            document.getElementById('editfield').dispatchEvent(event);
        }
    }
}

function displayTaxonSearchbox(el, text) {
    acceptVisibleSearchbox();
/*
    if(!el.querySelector('#taxonsearchwrapper') && old.offsetParent !== null) {     // there's one visible in other cell
        var txt = old.querySelector('textarea').value;
        onConfirmEdit({target: old.querySelector('textarea')}, txt, null, old.parentNode, true);
    }
*/
    var old = document.getElementById('taxonsearchwrapper');
    el.appendChild(old);
    var inp = old.querySelector('textarea');
    inp.value = text;
    inp.setSelectionRange(0, inp.value.length);
    inp.focus();
}

function displayEditField(el, text) {
    acceptVisibleSearchbox();

/*
    if(!el.querySelector('#editfieldwrapper') && old.offsetParent !== null) {     // there's one visible in other cell
        var txt = old.querySelector('input').value;
        onConfirmEdit({target: old.querySelector('input')}, txt, null, old.parentNode, true);
    }
*/
    var old = document.getElementById('editfieldwrapper');
    el.appendChild(old);
    var inp = old.querySelector('input');
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

function addNewFeature(ev) {
    if(document.getElementById('addoccurrencetable'))
        addNewOccurrence.call(this, ev);
    else
        addNewInventory.call(this, ev);
}

function addNewInventory(ev) {
    var id = randomString(6);
    var lat = Math.round(ev.latlng.lat * 100000) / 100000;
    var lng = Math.round(ev.latlng.lng * 100000) / 100000;

    var inv = document.querySelector('.inventory.dummy').cloneNode(true);
//    var tab = document.querySelector('#newinventorytable tbody');
    var cell2 = inv.querySelector('.coordinates');
    inv.classList.remove('dummy');

    inv.querySelector('input[name=code]').setAttribute('name', id + '_code');
    var inp_latitude = createHiddenInputElement(id + '_latitude', lat);
    var inp_longitude = createHiddenInputElement(id + '_longitude', lng);

    inv.setAttribute('data-id', id);
    cell2.innerHTML = lat + ', ' + lng;
    cell2.appendChild(inp_latitude);
    cell2.appendChild(inp_longitude);
    cell2.setAttribute('data-lat', lat);
    cell2.setAttribute('data-lng', lng);
    addPointMarker(ev.latlng.lat, ev.latlng.lng, inv);
    document.getElementById('addnewinventories').appendChild(inv);

    var ot = inv.querySelectorAll('.occurrencetable');
    for(var i=0; i<ot.length; i++)
        addEvent('click', ot[i], clickOccurrenceTable);

    document.getElementById('addnewinventories').classList.remove('hidden');
    addEvent('click', inv.querySelector('.button'), addNewTaxon);
    doMouseClick(inv.querySelector('.button'));
}


function addNewOccurrence(ev) {
    var id = randomString(6);
    var lat = Math.round(ev.latlng.lat * 100000) / 100000;
    var lng = Math.round(ev.latlng.lng * 100000) / 100000;

    var tab = document.querySelector('#addoccurrencetable tbody');
    var row = document.querySelector('#addoccurrencetable tr.dummy').cloneNode(true);
    var cell2 = row.querySelector('td.coordinates');
    row.classList.remove('dummy');

    var inp_latitude = createHiddenInputElement(id + '_latitude', lat);
    var inp_longitude = createHiddenInputElement(id + '_longitude', lng);

    row.setAttribute('data-id', id);
    cell2.innerHTML = lat + ', ' + lng;
    cell2.appendChild(inp_latitude);
    cell2.appendChild(inp_longitude);
    cell2.setAttribute('data-lat', lat);
    cell2.setAttribute('data-lng', lng);
    addPointMarker(ev.latlng.lat, ev.latlng.lng, row);
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