var myMap = null;
var mapLocationFilter = null;
var mapRectSelect = null;
var filterTimeout = null;
var loader;
var showPointsOnMap;
if (typeof L !== 'undefined') {
    var redCircle = L.divIcon({className: 'redcircleicon', bgPos: [-4, -4], iconSize: [8, 8]});
    var greenSquare = L.divIcon({className: 'greensquareicon', bgPos: [-4, -4], iconSize: [8, 8]});
    var openCircle = L.divIcon({className: 'opencircleicon', bgPos: [-4, -4], iconSize: [8, 8]});
    var blackCircle = L.divIcon({className: 'blackcircleicon', bgPos: [-4, -4], iconSize: [8, 8]});
}
var lastFileUploadCallback = null;
/*
var Esri_WorldImagery = L.tileLayer('https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}', {
    attribution: 'Tiles &copy; Esri &mdash; Source: Esri, i-cubed, USDA, USGS, AEX, GeoEye, Getmapping, Aerogrid, IGN, IGP, UPR-EGP, and the GIS User Community'
});
*/
var Esri_WorldImagery = L.tileLayer('https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}', {
    attribution: 'Tiles &copy; Esri &mdash; Source: Esri, i-cubed, USDA, USGS, AEX, GeoEye, Getmapping, Aerogrid, IGN, IGP, UPR-EGP, and the GIS User Community'
});


function submitOnEnter(event){
    if(event.which === 13){
//        console.log(event.target.form);
//        event.target.form.dispatchEvent(new Event("submit", {cancelable: true}));
        event.target.form.submit();
        event.preventDefault(); // Prevents the addition of a new line in the text field (not needed in a lot of cases)
    }
}

function dimMainTable() {
    var el;
    if(el = document.getElementById('alloccurrences')) el.classList.add('inactive');
    if(el = document.getElementById('allinventories')) el.classList.add('inactive');
    if(el = document.getElementById('maintableheader')) el.classList.add('inactive');
    if(el = document.getElementById('warningpanel')) el.classList.add('inactive');
    if(el = document.querySelector('div.newfeature')) el.classList.add('inactive');
}

document.addEventListener('DOMContentLoaded', function() {
    if (typeof L !== 'undefined') {
        myMap = L.map('mapcontainer', {zoomSnap: 0, markerZoomAnimation: false}).setView([39.5, -8.1], 8);
        L.tileLayer.bing({imagerySet:'AerialWithLabelsOnDemand', bingMapsKey: 'AiPknGGGT9nQtbl5Rpa_fhMQxthyZrh5z_bAc-ESzNaaqwQYcyEthgHB-_WowOEP'}).addTo(myMap);
        //L.tileLayer.provider('Esri.WorldImagery').addTo(myMap);
        myMap.on('click', mapClick);

        if(document.querySelector('input[name=queriedRectangleMinLat]')) {
            var bounds = [[document.querySelector('input[name=queriedRectangleMaxLat]').value, document.querySelector('input[name=queriedRectangleMinLong]').value],
                [document.querySelector('input[name=queriedRectangleMinLat]').value, document.querySelector('input[name=queriedRectangleMaxLong]').value]];

            // create an orange rectangle
            L.rectangle(bounds, {color: "#00bfff", weight: 4, fill:false, dashArray:'4 7', interactive:false}).addTo(myMap);
        }

        if(document.querySelector('input[name=queriedPolygon]')) {
            var latLngs = JSON.parse(document.querySelector('input[name=queriedPolygon]').value);
            var querypoly = L.polygon(latLngs, {color: "#00bfff", weight: 4, fill:false, dashArray:'4 7', interactive:false}).addTo(myMap);     //#ff7800
            // This throws an error! Leaflet Draw Bug?
/*              querypoly.editing.enable();
            querypoly.on('edit', function() {
                console.log('Polygon was edited!');
            });*/
        }

    }

//    L.tileLayer.provider('Esri.WorldImagery').addTo(mymap);
    //L.tileLayer.bing({imagerySet:'Aerial', bingMapsKey: 'AiPknGGGT9nQtbl5Rpa_fhMQxthyZrh5z_bAc-ESzNaaqwQYcyEthgHB-_WowOEP'}).addTo(myMap);

/*    L.gridLayer.googleMutant({
        type: 'satellite'	// valid values are 'roadmap', 'satellite', 'terrain' and 'hybrid'
    }).addTo(myMap);*/



//    attachFormPosters(fileUploadCallback);
    attachFormPosters(null, function(ev) {
        acceptVisibleSearchbox();
        return true;
    });

    // click on an occurrence table
    var ot = document.querySelectorAll('.occurrencetable');
    for(var i=0; i<ot.length; i++)
        addEvent('click', ot[i], clickOccurrenceTable);

    var ot = document.querySelectorAll('.occurrencetable td.editable select, .occurrencetable td.editable input[type=radio], .occurrencetable td.editable input[type=date], .occurrencetable td.editable input[type=checkbox]');
    for(var i=0; i<ot.length; i++)
        addEvent('change', ot[i], cellWidgetChanged);

    showPointsOnMap = projectPointsOnMap();

    attachSuggestionHandler('taxonsearchbox', 'checklist/api/suggestions?limit=20&q=', 'suggestionstaxon', onConfirmEdit, true, '+', tabHandler);
    attachSuggestionHandler('authorsearchbox', 'checklist/api/suggestions?what=user&limit=20&q=', 'suggestionsauthor', onConfirmEdit, true, '+', tabHandler);
    attachSuggestionHandler('threatsearchbox', 'checklist/api/suggestions?what=threats&limit=20&q=', 'suggestionsthreat', onConfirmEdit, true, '+', tabHandler);
    attachSuggestionHandler('multilinefield', null, null, onConfirmEdit, true, null, tabHandler);
    attachSuggestionHandler('editfield', null, null, onConfirmEdit, true, null, tabHandler);
    attachSuggestionHandler('imageidfield', null, null, onConfirmEdit, true, null, tabHandler);

    if(document.getElementById("filter-textarea"))
        document.getElementById("filter-textarea").addEventListener("keypress", submitOnEnter);

    attachAJAXContent();

    // the mechanism for adding species to the taxon edit box
    var territory;
    if(territory = document.querySelector('div.option.selected[data-option=taxtree-territory]'))
        territory = territory.getAttribute('data-value');
    new TreeExpander(document.querySelectorAll('.taxtree-holder>ul'), function(ev, key) {
        var elem = document.getElementById('taxonsearchwrapper');
        // check if there are selected rows, if yes update field in all selected
        var sel = document.querySelectorAll('#alloccurrencetable tr.selected td[data-name=taxa]');
        if(sel.length == 0) sel = document.querySelectorAll('#addoccurrencetable tr.selected td[data-name=taxa]');
        if(sel.length == 0) sel = document.querySelectorAll('table.newoccurrencetable tr.selected td[data-name=taxa]');
        if(sel.length == 0) {sel = document.querySelectorAll('table.newoccurrencetable tr:not(.dummy).empty td[data-name=taxa]'); var separateRecords = true;}

        var el = document.querySelector('.taxtree-holder li[data-key="' + key +'"]');
        var text = '';
        for (var i = 0; i < el.childNodes.length; ++i) {
            if (el.childNodes[i].nodeType === Node.TEXT_NODE || (el.childNodes[i].nodeType === Node.ELEMENT_NODE && el.childNodes[i].tagName === 'I')) {
                text += el.childNodes[i].textContent;
            }
        }
        text = text.trim();
        var isTaxonBoxVisible = !!( elem.offsetWidth || elem.offsetHeight || elem.getClientRects().length );
        if (isTaxonBoxVisible || sel.length > 0) { // taxon input box is visible
            if(el && (el.classList.contains('SPECIES') || el.classList.contains('SUBSPECIES'))) {
                var outtext;
                var currentValue;
                const regex = /([a-z]+) ([a-z]+)/i;
                const match = text.match(regex);
                if(isTaxonBoxVisible) {
                    var box = document.getElementById('taxonsearchbox');
                    currentValue = box.value;
                    // if it contains exactly, don't add again
                    if(currentValue.toLowerCase().indexOf(text.toLowerCase()) != -1) return;
    //                console.log(match[1] + '|' + match[2]);
                    // if it contains a higher taxonomic rank, replace by lower rank
                    var tmp = currentValue.toLowerCase().indexOf(match[1].toLowerCase() + ' ' + match[2].toLowerCase());
                    if(tmp != -1) {
                        const re = new RegExp('(^|\\+) *(' + match[1] + ' ' + match[2] + '[\\w. ]*)($|\\+)');
                        outtext = currentValue.replace(re, '$1 ' + text + ' $3');
                    } else
                        outtext = document.getElementById('taxonsearchbox').value + ' + ' + text;

                    outtext = outtext.replace(/^[ +]*|[ +]*$/, '').trim();
                    box.value = outtext;
                    box.focus();
                } else {
                    for(var i=0; i<sel.length; i++) {
                        currentValue = sel[i].textContent;
                        if(currentValue.toLowerCase().indexOf(text.toLowerCase()) != -1) continue;
        //                console.log(match[1] + '|' + match[2]);
                        if(separateRecords && currentValue.length > 0) {
                        // TODO Handle multiple selection
/*
                            var selected = document.querySelectorAll('table.newoccurrencetable tbody tr.selected');
                            for(var j=0; j<selected.length; j++) {
                                selected[j].classList.remove('selected');
                                selected[j].querySelector('.selectbutton').classList.remove('selected');
                            }
                            var toSelect = document.querySelectorAll('table.newoccurrencetable tbody tr:not(.dummy).empty');
                            for(var j=0; j<toSelect.length; j++) {
                                toSelect[j].classList.add('selected');
                                toSelect[j].querySelector('.selectbutton').classList.add('selected');
                            }
*/
                            onConfirmEdit({}, text, null, sel[i], true, false, true);
/*
                            var geoel = getParentbyClass(sel[i], 'geoelement');
                            var coo = geoel.querySelector('.coordinates');
                            addNewOccurrence({latlng:{lat: coo.getAttribute('data-lat'), lng: coo.getAttribute('data-lng')}});
                            onConfirmEdit({}, text, null, document.querySelector('#addoccurrencetable tr.selected td[data-name=taxa]'), true, false, true);
*/
                        } else {
                            // if it contains a higher taxonomic rank, replace by lower rank
                            var tmp = currentValue.toLowerCase().indexOf(match[1].toLowerCase() + ' ' + match[2].toLowerCase());
                            if(tmp != -1) {
                                const re = new RegExp('(^|\\+) *(' + match[1] + ' ' + match[2] + '[\\w. ]*)($|\\+)');
                                outtext = currentValue.replace(re, '$1 ' + text + ' $3');
                            } else
                                outtext = currentValue + ' + ' + text;

                            outtext = outtext.replace(/^[ +]*|[ +]*$/, '').trim();
                            onConfirmEdit({}, outtext, null, sel[i], true, false, true);
                        }
                    }
                }
            }
        } else {
            if(el && (el.classList.contains('SPECIES') || el.classList.contains('SUBSPECIES'))) {
                alert(document.querySelector('table.inventorysummary')
                    ? 'Adicione um novo inventário para adicionar espécies.'
                    : 'Seleccione um ou mais registos clicando no círculo à sua esquerda para adicionar espécies.');
            }
/*
            if(el && (el.classList.contains('SPECIES') || el.classList.contains('SUBSPECIES')))
                window.location = '?w=occurrenceview&filter=tax:' + text;
*/
        }
    }, 'checklist/api/lists?w=tree&fmt=htmllist&territory=' + territory + '&id={id}').init();

    addEvent('keyup', document.getElementById('taxtree-filter'), function(ev) {
        var txt = ev.target.value.trim();
        console.log(txt);
        var els = document.querySelectorAll('#taxtree li');
        if(txt.length == 0) {
            for(var i=0; i<els.length; i++) {
                els[i].classList.remove('hidden');
            }
        } else {
            for(var i=0; i<els.length; i++) {
                if(els[i].textContent.toLowerCase().startsWith(txt.toLowerCase()))
                    els[i].classList.remove('hidden');
                else
                    els[i].classList.add('hidden');
            }
        }
    });


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

    attachOptionButtonHandler('occurrences');
/*
    var optionbuttons = document.querySelectorAll('.button.option');
    for(var i = 0; i < optionbuttons.length; i++) {
        addEvent('click', optionbuttons[i], clickOptionButton);
    }
*/

    var togglebuttons = document.querySelectorAll('.togglebutton');
    for(var i = 0; i < togglebuttons.length; i++) {
        addEvent('click', togglebuttons[i], function(ev) {
            var b = getParentbyClass(ev.target, 'button');
            b.classList.toggle('selected');
        });
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

    var addemptyinventory = document.getElementById('addemptyinventory');
    if(addemptyinventory) {
        addEvent('click', addemptyinventory, function(ev) {addNewInventory(null);})
    }

/*
    var ft = document.getElementById('filtertable');
    if(ft) {
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
                    }
                }
            }, 500);
        })
    }
*/

    var ft = document.querySelectorAll('.expandbutton');
    for(var i=0; i<ft.length; i++) {
        addEvent('click', ft[i], function(ev) {
//            ev.stopPropagation();
            var th = getParentbyTag(ev.target, 'th');
            var nodes = Array.prototype.slice.call( th.parentNode.children );
            var ncol = nodes.indexOf(th);
            var table = getParentbyTag(th, 'table');
            var tds = table.querySelectorAll('tr td:nth-of-type(' + (ncol + 1) + ')');
            for(var i=0; i<tds.length; i++)
                tds[i].classList.toggle('collapsed');
            var tds = table.querySelectorAll('tr th:nth-of-type(' + (ncol + 1) + ')');
            for(var i=0; i<tds.length; i++)
                tds[i].classList.toggle('collapsed');
        });
    }

    attachHelpButtons();
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

/*
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
*/

function onConfirmEdit(ev, name, key, parent, dry, nocontent, nopropagation) {
    if(!dry || !parent.classList.contains('editable')) return;  // that's the bugfix
    var fieldname = parent.getAttribute('data-name');
    if(ev.target)   // move edit box back to its hidden home
        document.getElementById('taxonsearchwrapper-holder').appendChild(ev.target.parentNode);
    parent.classList.remove('beingedited');

    var original = parent.getAttribute('data-original');
//    console.log(original+':'+name);
    if(original !== null) {
        parent.removeAttribute('data-original');
        var modified = (createHTML(original).textContent.trim() !== name.trim());
    } else var modified = true;

    if(!modified || ev.keyCode == 27) {
        parent.innerHTML = original;
        return;
    }

    // the bug was here, in some circumstances this was called in the editbox-home, thus erasing the edit box completely
    if(!nocontent)
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
        // add the input for the current modified cell
        insertOrReplaceHiddenInput(parent, id1 + '_' + id2 + '_' + fieldname, name);
        var ou = id2el.querySelector('input[name=occurrenceUuid]');
        var iid = id1el.querySelector('input[name=inventoryId]');
        id2el.classList.remove('empty');
//        var cell = id2el.querySelector('.select');
        insertOrReplaceHiddenInput(ou.parentNode, id1 + '_' + id2 + '_occurrenceUuid', ou.value);
        insertOrReplaceHiddenInput(iid.parentNode, id1 + '_inventoryId', iid.value);

// we're in the add new occurrences of the inventory. add new row if needed.
        if(getParentbyClass(parent, 'newoccurrencetable')) {
            var lastRow = getParentbyClass(parent, 'newoccurrencetable').querySelector('tbody tr:last-of-type');
            if(lastRow.querySelectorAll('input[type=hidden]').length > 1) {
                // only add if there is no empty row already NOTE empty rows have ONLY one hidden input
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

    // if there are selected rows, update field in all selected!
    var sel = document.querySelectorAll('#alloccurrencetable tr.selected td[data-name="' + fieldname + '"]');
    if(sel.length == 0) sel = document.querySelectorAll('#addoccurrencetable tr.selected td[data-name="' + fieldname + '"]');
    if(sel.length == 0) sel = document.querySelectorAll('table.newoccurrencetable tr.selected td[data-name="' + fieldname + '"]');

    // if it has a widget, then we set nocontent to true, so that innerHTML is not set
    var selectEl = parent.querySelector('select');
    if(!selectEl) selectEl = parent.querySelector('input[type=radio]');
    if(!selectEl) selectEl = parent.querySelector('input[type=date]');
    if(!selectEl) selectEl = parent.querySelector('input[type=checkbox]');

    var nocontentForNext = null;
    if(selectEl) {
        nocontentForNext = true;
        selectEl.value = name;
    }

    if(sel.length > 0 && !nopropagation) {
        for(var i=0; i<sel.length; i++) {
            onConfirmEdit({}, name, null, sel[i], true, nocontentForNext ? true : sel[i].classList.contains('nodisplay'), true);
        }
    }

/*
    if(fieldname == 'coordinates') {
        var ge = getParentbyClass(parent, 'geoelement');
        projectPointsOnMap(ge);
    }
*/
}

function tabHandler(ev) {
//console.log(parseInt(ev.keyIdentifier.substring(2),16) > 47 && parseInt(ev.keyIdentifier.substring(2),16) < 58);
    var el = ev.target;
    switch(ev.keyCode) {
    case 9:
        ev.preventDefault();
        var cell = el.parentNode.parentNode;
        var next = getNextSiblingByClass(cell, 'editable');
        if(next) doMouseClick(next);
        break;

    case 171:
        var c = doGetCaretPosition(el);
        if(el.value.charAt(c) == '+' || el.value.charAt(c - 1) == '+') ev.preventDefault();
/*
        var v = el.value.split(separator);
        var i = 0;
        var len = 0;
        while(len < c) {
            len += v[i].length + 1;
            i++;
        }
        if(i == 0) i = 1;
        v[i-1] = text;
        for(i=0; i<v.length; i++) v[i] = v[i].trim();
        el.value = v.join(separator + '') + separator;*/

        break;

    default:
        if((ev.keyCode >= 48 && ev.keyCode <= 57)
            || (96 <= ev.keyCode && ev.keyCode <= 105)) {  // numbers
            var c = doGetCaretPosition(el);
            if(el.value.charAt(c - 1) == '+') {
                ev.preventDefault();
                console.log(ev.keyCode);
                el.value = el.value.substring(0, c - 1)
                    + String.fromCharCode((96 <= ev.keyCode && ev.keyCode <= 105)? ev.keyCode - 48 : ev.keyCode)
                    + el.value.substring(c - 1);
            }
        } else {
            if(ev.key == '?') {
                var c = doGetCaretPosition(el);
                if(el.value.charAt(c - 1) == '+') {
                    ev.preventDefault();
                    console.log(ev.keyCode);
                    el.value = el.value.substring(0, c - 1) + '?'
                        + el.value.substring(c - 1);
                }
            }
        }
        break;
    }
}

function doMouseClick(el) {
    if(!el) return;
    var event = new MouseEvent('click', {bubbles: true});
    el.dispatchEvent(event);
}

function disableAllMapButtons() {
    if(document.getElementById('querypoly')) document.getElementById('querypoly').classList.remove('selected');
    if(document.getElementById('queryrect')) document.getElementById('queryrect').classList.remove('selected');
    if(document.getElementById('selectpoints')) document.getElementById('selectpoints').classList.remove('selected');
    document.getElementById('addpointstoggle').classList.remove('selected');
    document.querySelector('.leaflet-container').style.cursor = 'auto';
    if(mapRectSelect) mapRectSelect.disable();
}

function clickButton(ev) {
    var b = getParentbyClass(ev.target, 'button');
    if(!b.id) {
        if(b.classList.contains('updatemodifiedinv')) {
        }
    } else {
        switch(b.id) {
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
            loader = document.getElementById('loader');
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
            disableAllMapButtons();
            if(!b.classList.contains('selected')) {/*
                        var bnd = layer.getBounds();
                        document.querySelector('textarea[name=filter]').value =
                            'long:' + Math.round(bnd.getWest() * 10000) / 10000 + '-' + Math.round(bnd.getEast() * 10000) / 10000
                            + ' lat:' + Math.round(bnd.getSouth() * 10000) / 10000 + '-' + Math.round(bnd.getNorth() * 10000) / 10000;
                        document.getElementById('occurrencefilter').firstElementChild.submit();
*/

                mapLocationFilter = myMap.selectAreaFeature.enable();
                //L.setOptions(mapLocationFilter, {color:'yellow', onMouseUp: function(el, ev) {
                L.setOptions(mapLocationFilter, {color:'yellow'});
                myMap.on('mouseup', function(el, ev) {
                    // select points and deactivate select area
                    var selMarkers = mapLocationFilter.getFeaturesSelected('marker');
                    mapLocationFilter.disable();
                    if(selMarkers) {
                        if(selMarkers.length > 0)
                            selectGeoElement(selMarkers[0].tableRow, true, true);
                        if(selMarkers.length > 1) {
                            for(var i=1; i<selMarkers.length; i++) {
                                selectGeoElement(selMarkers[i].tableRow, true, false);
                            }
                        }
                    } else
                        deselectGeoElements();
                    mapLocationFilter.removeAllArea();
                    b.classList.toggle('selected');
                });
            } else
                if(mapLocationFilter) mapLocationFilter.disable();
            break;

        case 'querypoly':
            disableAllMapButtons();
            if(!b.classList.contains('selected')) {
                mapRectSelect = new L.Draw.Polygon(myMap);
                mapRectSelect.setOptions({allowIntersection: false});
                mapRectSelect.enable();
                myMap.on(L.Draw.Event.CREATED, function (e) {
                    /*loader = document.getElementById('loader');
                    if(loader) loader.style.display = 'block';*/

                    var type = e.layerType,
                        layer = e.layer;
                    if (type === 'polygon') {
                        var vertices = layer.getLatLngs();
                        if(vertices.length > 1) {
                            if(mapRectSelect) mapRectSelect.disable();
                            return;
                        }
                        vertices = vertices[0];
                        var wkt = 'POLYGON ((';
                        for(var i=0; i<vertices.length; i++) {
                            wkt = wkt + Math.round(vertices[i].lng * 10000) / 10000 + ' ' + Math.round(vertices[i].lat * 10000) / 10000 + (i == vertices.length - 1 ? '' : ',');
                        }
                        wkt = wkt + '))';
                        document.querySelector('textarea[name=filter]').value = 'wkt: ' + wkt;
                        document.getElementById('occurrencefilter').firstElementChild.submit();
                    }
                });
            } else {
                if(mapRectSelect) mapRectSelect.disable();
            }
            break;

        case 'addpointstoggle':
            disableAllMapButtons();
            document.querySelector('.leaflet-container').style.cursor = 'crosshair';
            break;

        case 'queryrect':
            disableAllMapButtons();
            if(!b.classList.contains('selected')) {
                mapRectSelect = new L.Draw.Rectangle(myMap);
                mapRectSelect.enable();
                myMap.on(L.Draw.Event.CREATED, function (e) {
                    loader = document.getElementById('loader');
                    if(loader) loader.style.display = 'block';

                    var type = e.layerType,
                    layer = e.layer;
                    if (type === 'rectangle') {
                        var bnd = layer.getBounds();
                        document.querySelector('textarea[name=filter]').value =
                            'long:' + Math.round(bnd.getWest() * 10000) / 10000 + '-' + Math.round(bnd.getEast() * 10000) / 10000
                            + ' lat:' + Math.round(bnd.getSouth() * 10000) / 10000 + '-' + Math.round(bnd.getNorth() * 10000) / 10000;
                        document.getElementById('occurrencefilter').firstElementChild.submit();
                    }
                });
            } else {
                if(mapRectSelect) mapRectSelect.disable();
            }
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
            } else
                alert('Tem de seleccionar as ocorrências a apagar');
            break;

        case 'deleteselected':
            acceptVisibleSearchbox();
            var sel = document.querySelectorAll('#alloccurrencetable tr.selected');
            if(sel.length == 0) {
                alert('Tem de seleccionar as ocorrências a apagar');
                break;
            }
            var tbody = document.querySelector('#deleteoccurrencetable tbody');
            for(var i=0; i<sel.length; i++)
                tbody.appendChild(sel[i]);
            document.getElementById('deleteoccurrences').classList.remove('hidden');
            dimMainTable();
            break;

        case 'deleteselectedinv':
            acceptVisibleSearchbox();
            var count = 0;
            var inv = getParentbyClass(b, 'inventory');
            var sel = inv.querySelectorAll('.newoccurrencetable tr.selected');
            if(sel.length == 0) return;
            var tbody = document.querySelector('#deleteoccurrencetable tbody');
            for(var i=0; i<sel.length; i++) {
                if(sel[i].querySelector('input[name=occurrenceUuid]').value != '') {
                    tbody.appendChild(sel[i]);
                    count++;
                } else {
                    if(sel[i].marker)
                        sel[i].marker.remove();
                    sel[i].parentNode.removeChild(sel[i]);
                }
            }

            if(count > 0) {
                document.getElementById('deleteoccurrences').classList.remove('hidden');
                document.getElementById('deleteoccurrences').scrollIntoView({behavior:'smooth', inline:'start', block:'center'});
                dimMainTable();
            }
            break;

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

        case 'updatemodified':
            acceptVisibleSearchbox();
            var sel = document.querySelectorAll('#alloccurrencetable tr.modified');
            if(sel.length == 0) return;
            var tbody = document.querySelector('#updateoccurrencetable tbody');
            for(var i=0; i<sel.length; i++) {
                var ou = sel[i].querySelector('input[name=occurrenceUuid]');
                if(ou) ou = ou.value;
                var iid = sel[i].querySelector('input[name=inventoryId]').value;
                var cell = sel[i].querySelector('td.selectcol');
                var did = sel[i].getAttribute('data-id');
                if(ou) insertOrReplaceHiddenInput(cell, did + '_occurrenceUuid', ou);
                insertOrReplaceHiddenInput(cell, did + '_inventoryId', iid);
                tbody.appendChild(sel[i]);
            }

            document.getElementById('updateoccurrences').classList.remove('hidden');
            dimMainTable();
            break;

        case 'cancelupdate':
        case 'canceldelete':
        case 'cancelnew':
            window.location.reload(false);
            break;

/*
        case 'updatemodifiedinv':
            acceptVisibleSearchbox();
            break;
*/

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
    onConfirmEdit({}, Math.round(lat * 1000000) / 1000000 + ', ' + Math.round(lng * 1000000) / 1000000, null, cooel
        , true, cooel.classList.contains('nodisplay'));
}

function addNewTaxon(ev) {
    var id = randomString(6);
    var inv = getParentbyClass(ev.target, 'inventory');
    var newRow = inv.querySelector('.newoccurrencetable tr.dummy').cloneNode(true);
    newRow.classList.remove('dummy');
    newRow.setAttribute('data-id', id);
    inv.querySelector('.newoccurrencetable tbody').appendChild(newRow);

    // attach widgets' change handlers
    var ot = newRow.querySelectorAll('td.editable select, td.editable input[type=radio], td.editable input[type=date], td.editable input[type=checkbox]');
    for(var i=0; i<ot.length; i++)
        addEvent('change', ot[i], cellWidgetChanged);
}

function cellWidgetChanged(ev) {
    var cell = getParentbyTag(ev.target, 'td');
    var value;
    if(ev.target && ev.target.type == 'checkbox') {
        if(ev.target.checked)
            value = ev.target.value;
        else value = '';
    } else value = ev.target.value;
    if(cell) onConfirmEdit({}, value, null, cell, true, true, false);
}

function clickOccurrenceTable(ev) {
    var cell = getParentbyClass(ev.target, 'editable') || getParentbyClass(ev.target, 'clickable');
    if(!cell || !cell.classList || cell.querySelector('select, input[type=radio], input[type=date], input[type=checkbox]')) return;
    if(cell.classList.contains('taxon')) { // clicked taxon cell
        if(cell.querySelector('#taxonsearchwrapper')) return;
//        selectGeoElement(cell, true, true);
        var txt = cell.innerHTML;
//        cell.textContent = '';
        displaySearchbox(cell, txt, 'taxonsearchwrapper');
    } else if(cell.classList.contains('authors')) { // clicked authors cell
        if(cell.querySelector('#authorsearchwrapper')) return;
        var txt = cell.innerHTML;
//        cell.textContent = '';
        displaySearchbox(cell, txt, 'authorsearchwrapper');
    } else if(cell.classList.contains('threats')) { // clicked threats cell
         if(cell.querySelector('#threatsearchwrapper')) return;
         var txt = cell.innerHTML;
//         cell.textContent = '';
         displaySearchbox(cell, txt, 'threatsearchwrapper');
    } else if(cell.classList.contains('imageupload')) { // clicked image cell
         if(cell.querySelector('#uploadfilewrapper')) return;
         var txt = cell.innerHTML;
//         cell.textContent = '';
         displayFileUploadField(cell, txt);
    } else if(cell.classList.contains('selectcol') && cell.tagName == 'TD') {    // select row
        if(ev.shiftKey && !cell.parentNode.classList.contains('active')) {
//            document.getSelection().removeAllRanges();
            var tab = getParentbyClass(cell, 'occurrencetable');
            if(tab) {
                var vr = tab.querySelectorAll('tr:not(.hidden):not(.empty) td.selectcol');
                var started = false;
                var dormant = false;

                for(var i=0; i<vr.length; i++) {
                    if(!started && (vr[i] === cell || vr[i].parentNode.classList.contains('active'))) {
                        started = true;
                        dormant = true;
                    }
                    if(started) {
                        selectGeoElement(vr[i], true, false, true);
                        if(!dormant && (vr[i] === cell || vr[i].parentNode.classList.contains('active'))) break;
                    }
                    dormant = false;
                }
            }
        } else
            selectGeoElement(cell);
    } else if(cell.classList.contains('singleselect')) {    // select row
        selectGeoElement(cell, true, true);
    } else if(cell.classList.contains('selectcol') && cell.tagName == 'TH') {    // select all
        var tab = getParentbyClass(cell, 'occurrencetable');
        if(tab) {
            var vr = tab.querySelectorAll('tr:not(.hidden):not(.empty) td.selectcol');
            for(var i=0; i<vr.length; i++) {
                selectGeoElement(vr[i], null, false, true);
            }
        }
    } else if(cell.classList.contains('editable')) {    // editable as plain text
        if(cell.classList.contains('multiline') && cell.querySelector('#multilinefieldwrapper')) return;
        if(!cell.classList.contains('multiline') && cell.querySelector('#editfieldwrapper')) return;
//        selectGeoElement(cell, true, true);
        var txt = cell.innerHTML;
//        var txt = cell.textContent;
//        cell.textContent = '';
        if(cell.classList.contains('coordinates'))
            var hint = 'Pode clicar no mapa para definir as coordenadas';

        displayEditField(cell, txt, cell.classList.contains('multiline'), hint);
    }
}

function deselectGeoElements(parent) {
    if(!parent) {
        var onesel = document.querySelector('.geoelement.selected');
        if(!onesel) return;
        parent = onesel.parentNode;
    }
    var selected = parent.querySelectorAll('.geoelement.selected');
    for(var i=0; i<selected.length; i++) {
        selected[i].classList.remove('selected');
        if(selected[i].querySelector('.selectbutton'))
            selected[i].querySelector('.selectbutton').classList.remove('selected');
        if(selected[i].marker) selected[i].marker.classList.remove('selected');
    }
}

function selectGeoElement(cell, value, clearothers, doNotChangeActiveRow) {
    var geoel = getParentbyTag(cell, 'tr');
    if(!geoel && cell.classList.contains('geoelement')) geoel = cell;
    if(!geoel) geoel = getParentbyClass(cell, 'geoelement');
    if(!geoel) return;

    if(!doNotChangeActiveRow) {
        // set the current row as active, deactivate all others
        var active = geoel.parentNode.querySelectorAll('.geoelement.active');
        for(var i=0; i<active.length; i++)
            active[i].classList.remove('active');
        geoel.classList.add('active');
    }

    if(value === undefined || value === null) {
        geoel.classList.toggle('selected');
        if(geoel.querySelector('.selectbutton'))
            geoel.querySelector('.selectbutton').classList.toggle('selected');
        if(geoel.marker) geoel.marker.classList.toggle('selected');
    } else {
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
    var coo, tmpstyle, minLat = 1000, maxLat = -1000, minLng = 1000, maxLng = -1000, lat, lng;
    for(var i=0; i<ot.length; i++) {
        coo = ot[i].querySelectorAll('.coordinates'); // FIXME this selector selects nested geoelements, it shouldn't
        for(var j=0; j<coo.length; j++) {
            if(!coo[j].getAttribute('data-lat') || !coo[j].getAttribute('data-lng')) continue;
            if(getParentbyClass(coo[j], 'geoelement') != ot[i]) continue;
            tmpstyle = Object.assign({}, markerOptions);
            if(markerOptions.label)
                tmpstyle.label = coo[j].getAttribute('data-label');
            if(parseInt(coo[j].getAttribute('data-symbol')) == 2)
                tmpstyle.icon = openCircle;
            else if(parseInt(coo[j].getAttribute('data-symbol')) == 1)
                tmpstyle.icon = blackCircle;
//    console.log("added "+parseFloat(coo[j].getAttribute('data-lat'))+", "+parseFloat(coo[j].getAttribute('data-lng')));
            lat = parseFloat(coo[j].getAttribute('data-lat'));
            lng = parseFloat(coo[j].getAttribute('data-lng'));
            if(lat < -90 || lat > 90 || lng < -180 || lng > 180) continue;
            if(lat < minLat) minLat = lat;
            if(lat > maxLat) maxLat = lat;
            if(lng < minLng) minLng = lng;
            if(lng > maxLng) maxLng = lng;
            addPointMarker(lat, lng, ot[i], coo[j].classList.contains('editable'), tmpstyle);
        }
    }
    if(minLat < 1000) {
        var ranLat = maxLat - minLat;
        var ranLng = maxLng - minLng;
        if (typeof L !== 'undefined')
            myMap.fitBounds([[minLat - ranLat * 0.1, minLng - ranLng * 0.1], [maxLat + ranLat * 0.1, maxLng + ranLng * 0.1]]);
    }
    return true;
}

function addPointMarker(lat, lng, bondEl, draggable, options) {
    if(isNaN(lat) || isNaN(lng) || typeof L === 'undefined') return;
    options = Object.assign({icon: redCircle}, options);
    var marker = L.marker([lat, lng], {icon: options.icon, draggable: draggable, keyboard: false});
    if(options.label)
        marker.bindPopup(options.label);
    if(draggable) marker.on('dragend', markerMove);
    marker.on('click', markerClick).addTo(myMap);
    if(bondEl) {
        marker.tableRow = bondEl;
        if(bondEl.marker && options.removeIfDuplicate)
            bondEl.marker.remove();
        bondEl.marker = marker._icon;
        bondEl.markerObj = marker;

/*
        if(bondEl.marker)
            bondEl.marker.remove();
        marker.tableRow = bondEl;
        bondEl.marker = marker._icon;
        bondEl.markerObj = marker;
*/
    }
}

function markerMove(ev) {
    if(ev.target.tableRow) {
        acceptVisibleSearchbox();
        var ll = ev.target.getLatLng();
        updateCoords(ev.target.tableRow, ll.lat, ll.lng);
    }
}

// dispatch an enter key to confirm the edit field that is visible
function acceptVisibleSearchbox() {
    var editboxes = document.querySelectorAll('.editbox');
    for(var i = 0; i < editboxes.length; i++) {
        var c = 0;
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
    el.classList.add('beingedited');
    var inp = old.querySelector('textarea');
    inp.value = createHTML(text).textContent;
    inp.setSelectionRange(0, inp.value.length);
    inp.focus();
}

function displayEditField(el, text, multiline, hint) {
    acceptVisibleSearchbox();
    if(multiline) {
        var old = document.getElementById('multilinefieldwrapper');
        el.appendChild(old);
        el.setAttribute('data-original', text);
        el.classList.add('beingedited');
        var inp = old.querySelector('textarea');
        inp.value = createHTML(text).textContent;
        inp.setSelectionRange(0, inp.value.length);
        inp.focus();
    } else {
        var old = document.getElementById('editfieldwrapper');
        el.appendChild(old);
        el.setAttribute('data-original', text);
        el.classList.add('beingedited');
        var inp = old.querySelector('input');
        inp.value = createHTML(text).textContent;
        inp.setSelectionRange(0, inp.value.length);
        inp.focus();
    }

    var hintEl = old.querySelector('.hint');
    if(hintEl) {
        if(hint) {
            hintEl.textContent = hint;
            hintEl.style.display = 'block';
        } else {
            hintEl.textContent = '';
            hintEl.style.display = 'none';
        }
    }
}


/*
function fileUploadCallback(resp, ev) {
    if(ev.target.getAttribute('data-refresh') != 'false')
        window.location.reload();
    else {
        if(resp.success) alert(resp.msg);
    }
}
*/


function displayFileUploadField(el, text) {
    acceptVisibleSearchbox();
    var old = document.getElementById('uploadfilewrapper');
    el.appendChild(old);
    el.setAttribute('data-original', text);
    el.classList.add('beingedited');

    if(lastFileUploadCallback)
        removeEvent('submit', old.querySelector('form.posternoattach'), lastFileUploadCallback);

    lastFileUploadCallback = function (ev) {
        formPoster.call(this, ev, function(rt, ev) {
            old.querySelector("[name=query]").value = rt.msg;
            acceptVisibleSearchbox();
        }, null);
    };

    attachFormPosterTo(old.querySelector('form.posternoattach'), lastFileUploadCallback, null);

    var inp = old.querySelector('input');
    inp.value = createHTML(text).textContent;
//    inp.setSelectionRange(0, inp.value.length);
    inp.focus();
}

function markerClick(ev) {
    if(ev.target.tableRow) {
        var ss = ev.target.tableRow.querySelector('.singleselect') ? true : false;
        selectGeoElement(ev.target.tableRow, ss ? true : undefined, ss);
        if(ev.target.tableRow.classList.contains('selected'))
            ev.target.tableRow.scrollIntoView({behavior:'smooth', inline:'start', block:'center'});

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

function mapClick(ev) {
    var opt = document.getElementById('addpointstoggle');
    var sp = document.getElementById('selectpoints');
    if(sp && sp.classList.contains('selected')) {
        sp.classList.toggle('selected');
        if(mapLocationFilter) {
            mapLocationFilter.removeAllArea();
            mapLocationFilter.disable();
            deselectGeoElements();
        }
        return;
    }
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
                projectPointsOnMap(ge, {removeIfDuplicate: true});
                if(ge.marker) ge.marker.classList.add('selected');
                return;
            }
        }
    }

    if(opt && !opt.classList.contains('selected')) return;

    acceptVisibleSearchbox();
    var ot = document.getElementById('addoccurrencetable')
    if(ot)
        addNewOccurrence.call(this, ev);
    else
        addNewInventory.call(this, ev);

}

function addNewInventory(ev) {
    dimMainTable();
    var id = randomString(6);
    var inv = document.querySelector('.inventory.dummy').cloneNode(true);
    inv.classList.remove('dummy');
    if(ev) {
        var lat = Math.round(ev.latlng.lat * 100000) / 100000;
        var lng = Math.round(ev.latlng.lng * 100000) / 100000;
        var inp_latitude = createHiddenInputElement(id + '_latitude', lat);
        var inp_longitude = createHiddenInputElement(id + '_longitude', lng);
        var cell2 = inv.querySelector('.coordinates');
        cell2.innerHTML = lat + ', ' + lng;
        cell2.appendChild(inp_latitude);
        cell2.appendChild(inp_longitude);
        cell2.setAttribute('data-lat', lat);
        cell2.setAttribute('data-lng', lng);
        addPointMarker(lat, lng, inv, true);
    }
//    var tab = document.querySelector('#newinventorytable tbody');
//    inv.querySelector('input[name=code]').setAttribute('name', id + '_code');

    inv.setAttribute('data-id', id);
    document.getElementById('addnewinventories').appendChild(inv);

    // if the date field is filled in, trigger an update so it gets saved.
    var celldate = inv.querySelector('td[data-name=date] input[type=date]');
    if(celldate) {
        cellWidgetChanged({target: celldate});
    } else {
        celldate = inv.querySelector('td[data-name=date]');
        if(celldate && celldate.textContent) {
            onConfirmEdit({}, celldate.textContent, null, celldate, true, true, false);
        }
    }

    var ot = inv.querySelectorAll('.occurrencetable');
    for(var i=0; i<ot.length; i++)
        addEvent('click', ot[i], clickOccurrenceTable);

    var ot = inv.querySelectorAll('.occurrencetable td.editable select, .occurrencetable td.editable input[type=radio], .occurrencetable td.editable input[type=date], .occurrencetable td.editable input[type=checkbox]');
    for(var i=0; i<ot.length; i++)
        addEvent('change', ot[i], cellWidgetChanged);

    document.getElementById('addnewinventories').classList.remove('hidden');
    addEvent('click', inv.querySelector('.newtaxon'), addNewTaxon);
    doMouseClick(inv.querySelector('.newtaxon'));
/*
    // select last, just added, row
    var lastRow = inv.querySelector('tr.id2holder:not(.dummy)');
    lastRow.classList.add('selected');
    lastRow.querySelector('.selectbutton').classList.add('selected');*/
}

function addNewOccurrence(ev) {
    dimMainTable();
    var id = randomString(6);
    if(ev && ev.latlng) {
        var lat = Math.round(ev.latlng.lat * 100000) / 100000;
        var lng = Math.round(ev.latlng.lng * 100000) / 100000;
    } else {
        var lat = null;
        var lng = null;
    }

    var tab = document.querySelector('#addoccurrencetable tbody');
    var newRow = document.querySelector('#addoccurrencetable tr.dummy').cloneNode(true);
    var cell2 = newRow.querySelector('td.coordinates');
    newRow.classList.remove('dummy');
    newRow.classList.add('selected');
    newRow.querySelector('.selectbutton').classList.add('selected');

    if(lat && lng) {
        var inp_latitude = createHiddenInputElement(id + '_latitude', lat);
        var inp_longitude = createHiddenInputElement(id + '_longitude', lng);
        if(!cell2.classList.contains('nodisplay'))
            cell2.innerHTML = lat + ', ' + lng;
        cell2.appendChild(inp_latitude);
        cell2.appendChild(inp_longitude);
        cell2.setAttribute('data-lat', lat);
        cell2.setAttribute('data-lng', lng);
        addPointMarker(lat, lng, newRow, true);
    }

    newRow.setAttribute('data-id', id);
    tab.appendChild(newRow);

    // if the date field is filled in, trigger an update so it gets saved.
    var celldate = newRow.querySelector('td[data-name=date] input[type=date]');
    if(celldate) {
        cellWidgetChanged({target: celldate});
    } else {
        celldate = newRow.querySelector('td[data-name=date]');
        if(celldate && celldate.textContent) {
            onConfirmEdit({}, celldate.textContent, null, celldate, true, true, false);
        }
    }

    // attach widgets' change handlers
    var ot = newRow.querySelectorAll('td.editable select, td.editable input[type=radio], td.editable input[type=date], td.editable input[type=checkbox]');
    for(var i=0; i<ot.length; i++)
        addEvent('change', ot[i], cellWidgetChanged);

    document.getElementById('addnewoccurrences').classList.remove('hidden');
}
