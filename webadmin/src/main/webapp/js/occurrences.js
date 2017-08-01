var myMap = null;
var filterTimeout = null;
var showPointsOnMap;
var redCircle = L.divIcon({className: 'redcircleicon', bgPos: [-4, -4], iconSize: [8, 8]});
var greenSquare = L.divIcon({className: 'greensquareicon', bgPos: [-4, -4], iconSize: [8, 8]});
/*var redCircle = L.icon({
    iconUrl: 'images/redcircle.png',
    iconSize: [12, 12],
    iconAnchor: [6, 6],
});*/

/*
var Esri_WorldImagery = L.tileLayer('https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}', {
    attribution: 'Tiles &copy; Esri &mdash; Source: Esri, i-cubed, USDA, USGS, AEX, GeoEye, Getmapping, Aerogrid, IGN, IGP, UPR-EGP, and the GIS User Community'
});
*/

document.addEventListener('DOMContentLoaded', function() {
    myMap = L.map('mapcontainer', {zoomSnap: 0, markerZoomAnimation: false}).setView([39.5, -8.1], 8);
//    L.tileLayer.provider('Esri.WorldImagery').addTo(mymap);
    L.tileLayer.bing({imagerySet:'AerialWithLabels', bingMapsKey: 'AiPknGGGT9nQtbl5Rpa_fhMQxthyZrh5z_bAc-ESzNaaqwQYcyEthgHB-_WowOEP'}).addTo(myMap);

    myMap.on('click', mapClick);

//    attachFormPosters(fileUploadCallback);
    attachFormPosters(null, function(ev) {
        acceptVisibleSearchbox();
        return true;
    });

    // click on an occurrence table
    var ot = document.querySelectorAll('.occurrencetable');
    for(var i=0; i<ot.length; i++)
        addEvent('click', ot[i], clickOccurrenceTable);

    showPointsOnMap = projectPointsOnMap();

    attachSuggestionHandler('taxonsearchbox', 'checklist/api/suggestions?limit=20&q=', 'suggestionstaxon', onConfirmEdit, true, '+', tabHandler);
    attachSuggestionHandler('authorsearchbox', 'checklist/api/suggestions?what=user&limit=20&q=', 'suggestionsauthor', onConfirmEdit, true, '+', tabHandler);
    attachSuggestionHandler('threatsearchbox', 'checklist/api/suggestions?what=threats&limit=20&q=', 'suggestionsthreat', onConfirmEdit, true, '+', tabHandler);
    attachSuggestionHandler('editfield', null, null, onConfirmEdit, true, null, tabHandler);

    addEvent('mouseup', document.body, function(ev) {
        if(!document.getElementById('georreferencer')) return;
        if(!document.getElementById('georreferencer').classList.contains('hidden')) {
            var txt = getSelectedText(ev.target);
            if(txt && txt.length > 0)
                document.getElementById('georref-query').value = txt;
        }
    });

/*
    document.getElementById("occurrencetable-holder").addEventListener("scroll", function(){
       var translate = "translate(0,"+this.scrollTop+"px)";
       this.querySelector("#alloccurrencetable thead").style.transform = translate;
    });
*/

    var buttons = document.querySelectorAll('.button:not(.anchorbutton)');
    for(var i = 0; i < buttons.length; i++) {
        addEvent('click', buttons[i], clickButton);
    }

    var optionbuttons = document.querySelectorAll('.button.option');
    for(var i = 0; i < optionbuttons.length; i++) {
        addEvent('click', optionbuttons[i], clickOptionButton);
    }

    var togglebuttons = document.querySelectorAll('.togglebutton');
    for(var i = 0; i < togglebuttons.length; i++) {
        addEvent('click', togglebuttons[i], function(ev) {ev.target.classList.toggle('selected')});
    }

    var inventories = document.querySelectorAll('.inventory:not(.dummy)');
    for(var i = 0; i < inventories.length; i++) {
    // FIXME
        addEvent('click', inventories[i].querySelector('.newtaxon'), addNewTaxon);
        doMouseClick(inventories[i].querySelector('.newtaxon'));
    }

    var clw = document.querySelectorAll('.warning .closewarning');
    for(var i = 0; i < clw.length; i++) {
        addEvent('click', clw[i], function(ev) {
            var war = getParentbyClass(ev.target, 'warning');
            if(war)
                war.parentNode.removeChild(war);
        });
    }

    var georq = document.getElementById('georref-query');
    if(georq) {
        addEvent('keyup', georq, function(ev) {
            if(ev.keyCode == 13)
                doMouseClick(document.getElementById('georref-search'));
        });
    }

    var ft = document.getElementById('filtertable');
    if(ft)
        addEvent('keyup', ft, function(ev) {
            if(ev.keyCode < 65 && ev.keyCode != 8 && ev.keyCode != 32 && !(ev.keyCode >= 48 && ev.keyCode <= 57) ) return;
            var inp = ev.target;
            if(filterTimeout) clearTimeout(filterTimeout);
            filterTimeout = setTimeout(function () {
                filterTimeout = null;
                var val = inp.value.toLowerCase();
                var rows = document.querySelectorAll('#' + inp.getAttribute('data-table') + ' tr.geoelement');
                if(val.trim() == '') {
                    for(var i=0; i<rows.length; i++) {
                        rows[i].classList.remove('hidden');
                        if(!showPointsOnMap && rows[i].marker) {
                            rows[i].marker.remove();
                            rows[i].marker = null;
                        }
                    }
                    if(!showPointsOnMap) {
/*
                        document.getElementById('occurrencemap').classList.add('hidden');
                        myMap.invalidateSize(false);
*/
                    }
                } else {
                    var count = 0;
                    var dispall = false;
                    var dispallasked = false;
                    for(var i=0; i<rows.length; i++) {
                        var gpsc = rows[i].querySelector('td[data-name="gpsCode"]') || rows[i].querySelector('td[data-name="code"]');
                        var loca = rows[i].querySelector('td[data-name="locality"]');
                        var vloca = rows[i].querySelector('td[data-name="verbLocality"]');
                        if(rows[i].querySelector('td.taxon').textContent.toLowerCase().indexOf(val) == -1
                            && rows[i].querySelector('td[data-name="date"]').textContent.toLowerCase().indexOf(val) == -1
                            && (!gpsc || gpsc.textContent.toLowerCase().indexOf(val) == -1)
                            && (!loca || loca.textContent.toLowerCase().indexOf(val) == -1)
                            && (!vloca || vloca.textContent.toLowerCase().indexOf(val) == -1)
                            ) {
                                rows[i].classList.add('hidden');
                                if(!showPointsOnMap && rows[i].marker) {
                                    rows[i].marker.remove();
                                    rows[i].marker = null;
                                }
                        } else {
                            rows[i].classList.remove('hidden');
                            count ++;
                            if(!rows[i].marker) {
                                if(count > 1100 && !dispallasked) {
                                    dispallasked = true;
                                    dispall = confirm('There are more than 1100 points. Do you want to display them all?');
                                }
                                if(count <= 1100 || (count > 1100 && dispall)) {
                                    coo = rows[i].querySelector('.coordinates');
                                    if(!coo.getAttribute('data-lat')) continue;
                                    addPointMarker(parseFloat(coo.getAttribute('data-lat')), parseFloat(coo.getAttribute('data-lng')), rows[i], true);
                                }
                            }
                        }
                    }
                    if(!showPointsOnMap) {
/*
                        if(count < 1000 || dispall)
                            document.getElementById('occurrencemap').classList.remove('hidden');
                        else
                            document.getElementById('occurrencemap').classList.add('hidden');
                        myMap.invalidateSize(false);
*/
                    }
                }
            }, 500);
        })

});

window.addEventListener('beforeunload', function (ev) {
    if(isFormSubmitting) return;
    var confirmationMessage = 'You have unsaved occurrences! Are you sure you want to lose them?';
    var sel = document.querySelectorAll('#addoccurrencetable tbody tr:not(.dummy)');
    var sel1 = document.querySelectorAll('#alloccurrencetable tr.modified');
    var sel2 = document.querySelectorAll('.inventory .modified');
    var sel3 = document.querySelectorAll('#updateoccurrencetable tbody tr.modified');
    var sel4 = document.querySelectorAll('#addnewinventories .inventory');
    if(sel.length > 0 || sel1.length > 0 || sel2.length > 0 || sel3.length > 0 || sel4.length > 0) {
        (ev || window.event).returnValue = confirmationMessage;
        return confirmationMessage;
    } else
        return null;
});

function clickOptionButton(ev) {
    var name = ev.target.getAttribute('data-option');
    var value = ev.target.getAttribute('data-value');
    var elid = ev.target.getAttribute('data-element');
    var vbool = (value == 'true');
    var el = document.getElementById(elid);
    if(el) {
        if(vbool) {
            el.classList.remove('hiddenhard');
            ev.target.classList.add('selected');
            ev.target.setAttribute('data-value', false);
        } else {
            el.classList.add('hiddenhard');
            ev.target.classList.remove('selected');
            ev.target.setAttribute('data-value', true);
        }
        myMap.invalidateSize(false);
    }
    fetchAJAX('occurrences?w=setoption&n=' + encodeURIComponent(name) + '&v=' + encodeURIComponent(value), function(rt) {
        if(!el) window.location.reload();
    });
}

function onConfirmEdit(ev, name, key, parent, dry) {
    if(!dry || !parent.classList.contains('editable')) return;  // that's the bugfix
    var fieldname = parent.getAttribute('data-name');
    if(ev.target)   // move edit box back to its hidden home
        document.getElementById('taxonsearchwrapper-holder').appendChild(ev.target.parentNode);
    var original = parent.getAttribute('data-original');
//    console.log(original+':'+name);
    if(original !== null) {
        parent.removeAttribute('data-original');
        var modified = (original.trim() !== name.trim());
    } else var modified = true;

    if(!modified || ev.keyCode == 27) {
        parent.innerHTML = original;
        return;
    }

    // the bug was here, in some circumstances this was called in the editbox-home, thus erasing the edit box completely
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

/*
    if(fieldname == 'coordinates') {
        var ge = getParentbyClass(parent, 'geoelement');
        projectPointsOnMap(ge);
    }
*/
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
/*
        case 'hidemap':
            ev.target.classList.toggle('selected');
            document.getElementById('occurrencemap').classList.toggle('hidden');
            myMap.invalidateSize(false);
            break;

        case 'hidegeorref':
            ev.target.classList.toggle('selected');
            document.getElementById('georreferencer').classList.toggle('hidden');
            myMap.invalidateSize(false);
            break;

        case 'hideoccurrences':
            ev.target.classList.toggle('selected');
            document.getElementById('occurrencetable-holder').classList.toggle('hidden');
            myMap.invalidateSize(false);
            break;
*/

        case 'georref-helptoggle':
            ev.target.classList.toggle('selected');
            document.getElementById('georref-help').classList.toggle('hidden');
            break;

        case 'georref-clear':
            var r = document.getElementById('georref-results');
            removeGeoElements(r.querySelectorAll('.geoelement'));
            r.innerHTML = '';
            break;

        case 'georref-search':
            var q = document.getElementById('georref-query').value;
            var loader = document.getElementById('loader');
            if(loader) loader.style.display = 'block';

            fetchAJAX('checklist/api/suggestions?what=toponym&q=' + encodeURIComponent(q), function(rt) {
                if(loader) loader.style.display = 'none';
                var r = document.getElementById('georref-results');
                removeGeoElements(r.querySelectorAll('.geoelement'));
                if(r) r.innerHTML = rt;
                projectPointsOnMap(r.querySelectorAll('.geoelement'), {icon: greenSquare, label: true});
            });
            break;

        case 'georref-usecoords':
            var rows = document.querySelectorAll('#alloccurrencetable tr.selected');
            if(rows.length == 0) {
                alert('Select at least one occurrence in the "your occurrences" table.');
                break;
            }
            var geoel = document.querySelector('#georref-results .geoelement.selected');
            if(!geoel) {
                alert('Select one place from the toponym table.');
                break;
            }
            acceptVisibleSearchbox();
            var coo = geoel.querySelector('.coordinates');
            var lat = coo.getAttribute('data-lat');
            var lng = coo.getAttribute('data-lng');
            for(var i=0; i<rows.length; i++) {
                updateCoords(rows[i], lat, lng);
                projectPointsOnMap(rows[i]);
                if(rows[i].marker) rows[i].marker.classList.add('selected');
            }
            deselectGeoElements(geoel.parentNode);
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

        case 'deleteselectedinv':
            acceptVisibleSearchbox();
            var inv = getParentbyClass(b, 'inventory');
            var sel = inv.querySelectorAll('.newoccurrencetable tr.selected');
            if(sel.length == 0) return;
            var tbody = document.querySelector('#deleteoccurrencetable tbody');
            for(var i=0; i<sel.length; i++) {
                if(sel[i].querySelector('input[name=occurrenceUuid]').value != '')
                    tbody.appendChild(sel[i]);
            }

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

function updateCoords(geoel, lat, lng) {
    var cooel = geoel.querySelector('.coordinates');
    var oldlat = cooel.getAttribute('data-lat');
    if(oldlat && parseFloat(oldlat) != 0) console.log('CHANGED');
    cooel.setAttribute('data-lat', lat);
    cooel.setAttribute('data-lng', lng);
    onConfirmEdit({}, Math.round(lat * 1000000) / 1000000 + ', ' + Math.round(lng * 1000000) / 1000000, null, cooel, true);
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
    } else if(cell.classList.contains('threats')) { // clicked authors cell
         if(cell.querySelector('#threatsearchwrapper')) return;
         var txt = cell.textContent;
         cell.textContent = '';
         displaySearchbox(cell, txt, 'threatsearchwrapper');
    } else if(cell.classList.contains('select')) {    // select row
        selectGeoElement(cell);
    } else if(cell.classList.contains('singleselect')) {    // select row
        selectGeoElement(cell, true, true);
    } else if(cell.classList.contains('selectcol')) {    // select all
        var tab = getParentbyClass(cell, 'occurrencetable');
        if(tab) {
            var vr = tab.querySelectorAll('tr:not(.hidden) .select');
            for(var i=0; i<vr.length; i++) {
                selectGeoElement(vr[i]);
            }
        }

    } else if(cell.classList.contains('editable')) {    // editable as plain text
        if(cell.querySelector('#editfieldwrapper')) return;
        selectGeoElement(cell, true, true);
        var txt = cell.textContent;
        cell.textContent = '';
        displayEditField(cell, txt);
    }
}

function deselectGeoElements(par) {
    var selected = par.querySelectorAll('.geoelement.selected');
    for(var i=0; i<selected.length; i++) {
        selected[i].classList.remove('selected');
        if(selected[i].querySelector('.selectbutton'))
            selected[i].querySelector('.selectbutton').classList.remove('selected');
        if(selected[i].marker) selected[i].marker.classList.remove('selected');
    }
}

function selectGeoElement(cell, value, clearothers) {
    var geoel = getParentbyTag(cell, 'tr');
    if(!geoel && cell.classList.contains('geoelement')) geoel = cell;
    if(!geoel) geoel = getParentbyClass(cell, 'geoelement');
    if(!geoel) return;

    if(value === undefined) {
        geoel.classList.toggle('selected');
        if(geoel.querySelector('.selectbutton'))
            geoel.querySelector('.selectbutton').classList.toggle('selected');
        if(geoel.marker) geoel.marker.classList.toggle('selected');
        return;
    }

    if(clearothers) {
        deselectGeoElements(geoel.parentNode);
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

/*function deselectGeoElements(elements) {
    if(elements.length > 0 || elements.length === 0)
        var els = elements;
    else
        var els = [elements];

    for(var i=0; i<els.length; i++) {
        if(els[i].marker) {
            els[i].marker.classList.remove('selected');
            els[i].classList.remove('selected');
        }
    }
}*/

function removeGeoElements(elements) {
    if(elements.length > 0 || elements.length === 0)
        var els = elements;
    else
        var els = [elements];

    for(var i=0; i<els.length; i++) {
        if(els[i].marker) {
            els[i].marker.remove();
            els[i].marker = null;
        }
    }
}

function projectPointsOnMap(ota, markerOptions) {
    markerOptions = Object.assign({icon: redCircle, label: false}, markerOptions);
    if(!ota)
        var ot = document.querySelectorAll('.geoelement');
    else {
        if(ota.length > 0 || ota.length === 0)
            var ot = ota;
        else
            var ot = [ota];
    }

    if(ot.length > 5000) {
        var dispall = confirm('There are more than 5000 points. Do you want to display them all?');
        if(!dispall) return false;
    }
    var coo;
    for(var i=0; i<ot.length; i++) {
        coo = ot[i].querySelectorAll('*:not(.geoelement) .coordinates');
        for(var j=0; j<coo.length; j++) {
            if(!coo[j].getAttribute('data-lat')) continue;
//            console.log("added "+coo[j].getAttribute('data-lat')+coo[j].getAttribute('data-lng'));
            if(markerOptions.label)
                markerOptions.label = coo[j].getAttribute('data-label');
            addPointMarker(parseFloat(coo[j].getAttribute('data-lat')), parseFloat(coo[j].getAttribute('data-lng'))
                , ot[i], coo[j].classList.contains('editable'), markerOptions);
        }
    }
    return true;
}

function addPointMarker(lat, lng, bondEl, draggable, options) {
    options = Object.assign({icon: redCircle}, options);
    var marker = L.marker([lat, lng], {icon: options.icon, draggable: draggable, keyboard: false});
    if(options.label)
        marker.bindPopup(options.label);
    if(draggable) marker.on('dragend', markerMove);
    marker.on('click', markerClick).addTo(myMap);
    if(bondEl) {
        if(bondEl.marker)
            bondEl.marker.remove();
        marker.tableRow = bondEl;
        bondEl.marker = marker._icon;
        bondEl.markerObj = marker;
    }
}

function markerMove(ev) {
    if(ev.target.tableRow) {
        acceptVisibleSearchbox();
        var ll = ev.target.getLatLng();
        updateCoords(ev.target.tableRow, ll.lat, ll.lng);
    }
}

function acceptVisibleSearchbox() {
    var editboxes = document.querySelectorAll('.editbox');
    for(var i = 0; i < editboxes.length; i++) {
        var c = 0;
//        while(editboxes[i].offsetParent !== null && c < 4) {
        while(getParentbyClass(editboxes[i], 'editbox-home') == null && c < 4) { // keep on clicking Enter until box is in its home
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
        var ss = ev.target.tableRow.querySelector('.singleselect') ? true : false;
        selectGeoElement(ev.target.tableRow, ss ? true : undefined, ss);
/*        var container = getParentbyClass(ev.target.tableRow, 'singleselection');
        if(container) {
            deselectGeoElements(container.querySelectorAll('.geoelement'));
        }

        if(ev.target.tableRow.querySelector('.select .selectbutton'))
            ev.target.tableRow.querySelector('.select .selectbutton').classList.toggle('selected');

        ev.target._icon.classList.toggle('selected');
        ev.target.tableRow.classList.toggle('selected');*/
    }
}

function fileUploadCallback(resp, ev) {
    if(ev.target.getAttribute('data-refresh') != 'false')
        window.location.reload();
    else {
        if(resp.success) alert(resp.msg);
    }
}

function mapClick(ev) {
    var opt = document.getElementById('addpointstoggle');

    var editboxes = document.querySelectorAll('.editbox');
    for(var i = 0; i < editboxes.length; i++) {
        if(editboxes[i].offsetParent !== null) {
            var par = editboxes[i].parentNode;
            if(par.classList.contains('coordinates')) { // there's a coordinate box selected, so update it
                acceptVisibleSearchbox();
                var ll = ev.latlng;
                var ge = getParentbyClass(par, 'geoelement');
                updateCoords(ge, ll.lat, ll.lng);
/*                par.setAttribute('data-lat', ll.lat);
                par.setAttribute('data-lng', ll.lng);
                onConfirmEdit({}, Math.round(ll.lat * 1000000) / 1000000 + ', ' + Math.round(ll.lng * 1000000) / 1000000, null, par, true);*/
                projectPointsOnMap(ge);
                if(ge.marker) ge.marker.classList.add('selected');
                return;
            }
        }
    }

    if(opt && !opt.classList.contains('selected')) return;

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

//    inv.querySelector('input[name=code]').setAttribute('name', id + '_code');
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
    addEvent('click', inv.querySelector('.newtaxon'), addNewTaxon);
    doMouseClick(inv.querySelector('.newtaxon'));
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
