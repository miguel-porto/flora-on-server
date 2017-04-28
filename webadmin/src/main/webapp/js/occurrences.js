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

//    attachFormPosters(fileUploadCallback);
    attachFormPosters();
    // click on an occurrence table
    var ot = document.querySelectorAll('.occurrencetable');
    for(var i=0; i<ot.length; i++)
        addEvent('click', ot[i], clickOccurrenceTable);

    projectPointsOnMap();

    attachSuggestionHandler('taxonsearchbox', '/floraon/checklist/api/suggestions?limit=20&q=', 'suggestionstaxon', onConfirmEdit, true, ',', tabHandler);
    attachSuggestionHandler('authorsearchbox', '/floraon/checklist/api/suggestions?what=user&limit=20&q=', 'suggestionsauthor', onConfirmEdit, true, ',', tabHandler);
    attachSuggestionHandler('editfield', null, null, onConfirmEdit, true, null, tabHandler);

    var buttons = document.querySelectorAll('.button:not(.anchorbutton)');
    for(var i = 0; i < buttons.length; i++) {
        addEvent('click', buttons[i], clickButton);
    }

    var inventories = document.querySelectorAll('.inventory:not(.dummy)');
    for(var i = 0; i < inventories.length; i++) {
    // FIXME
        addEvent('click', inventories[i].querySelector('.button'), addNewTaxon);
        doMouseClick(inventories[i].querySelector('.button'));
    }
});

window.addEventListener('beforeunload', function (ev) {
    if(isFormSubmitting) return;
    var confirmationMessage = 'You have unsaved occurrences! Are you sure you want to lose them?';
    var sel = document.querySelectorAll('#addoccurrencetable tbody tr:not(.dummy)');
    var sel1 = document.querySelectorAll('#alloccurrencetable tr.modified');
    var sel2 = document.querySelectorAll('.inventory .modified');
    var sel3 = document.querySelectorAll('#updateoccurrencetable tbody tr.modified');
    if(sel.length > 0 || sel1.length > 0 || sel2.length > 0 || sel3.length > 0) {
        (ev || window.event).returnValue = confirmationMessage;
        return confirmationMessage;
    } else
        return null;
});

function onConfirmEdit(ev, name, key, parent, dry) {
    if(!dry) return;
    var fieldname = parent.getAttribute('data-name');
    if(ev.target) document.getElementById('taxonsearchwrapper-holder').appendChild(ev.target.parentNode);
    var original = parent.getAttribute('data-original');
    console.log(original+':'+name);
    if(original !== null) {
        parent.removeAttribute('data-original');
        var modified = (original.trim() !== name.trim());
    } else var modified = true;

    if(!modified || ev.keyCode == 27) {
        parent.innerHTML = original;
        return;
    }

    parent.innerHTML = name;

    var id1el = getParentbyClass(parent, 'id1holder');
    var id2el = getParentbyClass(parent, 'id2holder');
    if(id1el) {
        var id1 = id1el.getAttribute('data-id');
        if(!id1) {
            id1 = randomString(6);
            id1el.setAttribute('data-id', id1);
        }
    }

    if(id2el) {
        var id2 = id2el.getAttribute('data-id');
        if(!id2) {
            id2 = randomString(6);
            id2el.setAttribute('data-id', id2);
        }
    }

    if(id1 && id2) {    // inventories
        // ad the input for the current modified cell
        insertOrReplaceHiddenInput(parent, id1 + '_' + id2 + '_' + fieldname, name);
        var ou = id2el.querySelector('input[name=occurrenceUuid]');
        var iid = id1el.querySelector('input[name=inventoryId]');
//        var cell = id2el.querySelector('.select');
        insertOrReplaceHiddenInput(ou.parentNode, id1 + '_' + id2 + '_occurrenceUuid', ou.value);
        insertOrReplaceHiddenInput(iid.parentNode, id1 + '_inventoryId', iid.value);

// we're in the add new occurrences of the inventory. add new row if needed.
        if(getParentbyClass(parent, 'newoccurrencetable')) {
            var lastRow = getParentbyClass(parent, 'newoccurrencetable').querySelector('tbody tr:last-of-type');
            if(lastRow.querySelectorAll('input[type=hidden]').length > 1) { // only add if there is no empty row already NOTE empty rows have one hidden input
                doMouseClick(id1el.querySelector('.newtaxon'));
            }
        }
        id1el.classList.add('modified');
    } else if(id1) {    // occurrences or inventory fields
        insertOrReplaceHiddenInput(parent, id1 + '_' + fieldname, name);
        var iid = id1el.querySelector('input[name=inventoryId]');
        if(iid) insertOrReplaceHiddenInput(iid.parentNode, id1 + '_inventoryId', iid.value);
        id1el.classList.add('modified');
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
    if(!el) return;
    var event = new MouseEvent('click', {bubbles: true});
    el.dispatchEvent(event);
}

function clickButton(ev) {
    var b = getParentbyClass(ev.target, 'button');
    if(!b.id) {
        if(b.classList.contains('updatemodifiedinv')) {
        }
    } else {
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

        case 'updatemodified':
            acceptVisibleSearchbox();
            var sel = document.querySelectorAll('#alloccurrencetable tr.modified');
            if(sel.length == 0) return;
            var tbody = document.querySelector('#updateoccurrencetable tbody');
            for(var i=0; i<sel.length; i++) {
                var ou = sel[i].querySelector('input[name=occurrenceUuid]').value;
                var iid = sel[i].querySelector('input[name=inventoryId]').value;
                var cell = sel[i].querySelector('.select');
                var did = sel[i].getAttribute('data-id');
                insertOrReplaceHiddenInput(cell, did + '_occurrenceUuid', ou);
                insertOrReplaceHiddenInput(cell, did + '_inventoryId', iid);
                tbody.appendChild(sel[i]);
            }

            document.getElementById('updateoccurrences').classList.remove('hidden');
            break;

/*
        case 'updatemodifiedinv':
            acceptVisibleSearchbox();
            break;
*/

        case 'deleteselectednew':
            acceptVisibleSearchbox();
            var sel = document.querySelectorAll('#addnewinventories .newoccurrencetable tr.selected');
            if(sel.length == 0) sel = document.querySelectorAll('#addoccurrencetable tr.selected');
            if(sel.length == 0) return;
            for(var i=0; i<sel.length; i++) {
                if(sel[i].marker) {
                    sel[i].marker.remove();
                }
                sel[i].parentNode.removeChild(sel[i]);
            }
            break;

        case 'newoccurrence':
            addNewOccurrence();
            break;
        }
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
//console.log(cell);
    if(cell.classList.contains('taxon')) { // clicked taxon cell
        if(cell.querySelector('#taxonsearchwrapper')) return;
        selectGeoElement(cell, true, true);
        var txt = cell.textContent;
        cell.textContent = '';
        displaySearchbox(cell, txt, 'taxonsearchwrapper');
    } else if(cell.classList.contains('authors')) { // clicked authors cell
        if(cell.querySelector('#authorsearchwrapper')) return;
        var txt = cell.textContent;
        cell.textContent = '';
        displaySearchbox(cell, txt, 'authorsearchwrapper');
    } else if(cell.classList.contains('select')) {    // select row
        selectGeoElement(cell);
    } else if(cell.classList.contains('editable')) {    // editable as plain text
        if(cell.querySelector('#editfieldwrapper')) return;
        selectGeoElement(cell, true, true);
        var txt = cell.textContent;
        cell.textContent = '';
        displayEditField(cell, txt);
    }
}

function selectGeoElement(cell, value, clearothers) {
//    var tr = getParentbyTag(cell, 'tr');
    var geoel = getParentbyTag(cell, 'tr');
//    var geoel = getParentbyClass(cell, 'geoelement');
    if(value === undefined) {
        geoel.classList.toggle('selected');
        if(geoel.querySelector('.selectbutton'))
            geoel.querySelector('.selectbutton').classList.toggle('selected');
        if(geoel.marker) geoel.marker.classList.toggle('selected');
        return;
    }

    if(clearothers) {
        var par = geoel.parentNode;
        var selected = par.querySelectorAll('.geoelement.selected');
        for(var i=0; i<selected.length; i++) {
            selected[i].classList.remove('selected');
            if(selected[i].querySelector('.selectbutton'))
                selected[i].querySelector('.selectbutton').classList.remove('selected');
            if(selected[i].marker) selected[i].marker.classList.remove('selected');
        }
    }
    if(value) {
        geoel.classList.add('selected');
        if(geoel.querySelector('.selectbutton'))
            geoel.querySelector('.selectbutton').classList.add('selected');
        if(geoel.marker) geoel.marker.classList.add('selected');
    } else {
        geoel.classList.remove('selected');
        if(geoel.querySelector('.selectbutton'))
            geoel.querySelector('.selectbutton').classList.remove('selected');
        if(geoel.marker) geoel.marker.classList.remove('selected');
    }
}

function projectPointsOnMap(ot) {
    if(!ot)
        var ot = document.querySelectorAll('.geoelement');
    if(!ot.length) ot = [ot];
    var coo;
    for(var i=0; i<ot.length; i++) {
        coo = ot[i].querySelectorAll('*:not(.geoelement) .coordinates');
        for(var j=0; j<coo.length; j++) {
            if(!coo[j].getAttribute('data-lat')) continue;
//            console.log("added "+coo[j].getAttribute('data-lat')+coo[j].getAttribute('data-lng'));
            addPointMarker(parseFloat(coo[j].getAttribute('data-lat')), parseFloat(coo[j].getAttribute('data-lng')), ot[i], coo[j].classList.contains('editable'));
        }
    }
}

function addPointMarker(lat, lng, bondEl, draggable) {
    var marker = L.marker([lat, lng], {icon: redCircle, draggable: draggable, keyboard: false});
    if(draggable) marker.on('dragend', markerMove);
    marker.on('click', markerClick).addTo(myMap);
    if(bondEl) {
        if(bondEl.marker)
            bondEl.marker.remove();
        marker.tableRow = bondEl;
        bondEl.marker = marker._icon;
    }
}

function markerMove(ev) {
    if(ev.target.tableRow) {
        acceptVisibleSearchbox();
        var ll = ev.target.getLatLng();
        var cooel = ev.target.tableRow.querySelector('.coordinates');
        cooel.setAttribute('data-lat', ll.lat);
        cooel.setAttribute('data-lng', ll.lng);
        onConfirmEdit({}, Math.round(ll.lat * 1000000) / 1000000 + ', ' + Math.round(ll.lng * 1000000) / 1000000, null, cooel, true);
    }
}

function acceptVisibleSearchbox() {
    var editboxes = document.querySelectorAll('.editbox');
    for(var i = 0; i < editboxes.length; i++) {
        var c = 0;
        while(editboxes[i].offsetParent !== null && c < 4) {
            var event = new KeyboardEvent('keyup', { 'keyCode': 13});
            Object.defineProperty(event, 'keyCode', {get:function(){return this.charCodeVal;}});
            event.charCodeVal = 13;
            editboxes[i].querySelector('[name=query]').dispatchEvent(event);
            c++;
        }
    }
}

function displaySearchbox(el, text, whichBox) {
    acceptVisibleSearchbox();
    var old = document.getElementById(whichBox);
    el.appendChild(old);
    el.setAttribute('data-original', text);
    var inp = old.querySelector('textarea');
    inp.value = text;
    inp.setSelectionRange(0, inp.value.length);
    inp.focus();
}

function displayEditField(el, text) {
    acceptVisibleSearchbox();
    var old = document.getElementById('editfieldwrapper');
    el.appendChild(old);
    el.setAttribute('data-original', text);
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

    var editboxes = document.querySelectorAll('.editbox');
    for(var i = 0; i < editboxes.length; i++) {
        if(editboxes[i].offsetParent !== null) {
            var par = editboxes[i].parentNode;
            if(par.classList.contains('coordinates')) {
                acceptVisibleSearchbox();
                var ll = ev.latlng;
                par.setAttribute('data-lat', ll.lat);
                par.setAttribute('data-lng', ll.lng);
                onConfirmEdit({}, Math.round(ll.lat * 1000000) / 1000000 + ', ' + Math.round(ll.lng * 1000000) / 1000000, null, par, true);
                var ge = getParentbyClass(par, 'geoelement');
                projectPointsOnMap(ge);
                if(ge.marker) ge.marker.classList.add('selected');
                return;
            }
        }
    }

    acceptVisibleSearchbox();
    if(document.getElementById('addoccurrencetable'))
        addNewOccurrence.call(this, ev);
    else if(document.getElementById('inventorysummary'))
//        window.location = '?w=openinventory&lat=' + ev.latlng.lat + '&lng=' + ev.latlng.lng;
        addNewInventory.call(this, ev);
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
    addPointMarker(lat, lng, inv, true);
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
    if(ev && ev.latlng) {
        var lat = Math.round(ev.latlng.lat * 100000) / 100000;
        var lng = Math.round(ev.latlng.lng * 100000) / 100000;
    } else {
        var lat = null;
        var lng = null;
    }

    var tab = document.querySelector('#addoccurrencetable tbody');
    var row = document.querySelector('#addoccurrencetable tr.dummy').cloneNode(true);
    var cell2 = row.querySelector('td.coordinates');
    row.classList.remove('dummy');

    if(lat && lng) {
        var inp_latitude = createHiddenInputElement(id + '_latitude', lat);
        var inp_longitude = createHiddenInputElement(id + '_longitude', lng);

        cell2.innerHTML = lat + ', ' + lng;
        cell2.appendChild(inp_latitude);
        cell2.appendChild(inp_longitude);
        cell2.setAttribute('data-lat', lat);
        cell2.setAttribute('data-lng', lng);
        addPointMarker(lat, lng, row, true);
    }

    row.setAttribute('data-id', id);
    tab.appendChild(row);

    document.getElementById('addnewoccurrences').classList.remove('hidden');
}
