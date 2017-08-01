var regex_highlight = /\*([\w çãõáàâéêíóôú\.,;:!?()ñ&'\"-]+)\*/gi;
var regex_under = /_([\w çãõáàâéêíóôú\.,;:!?()ñ&'\"-]+)_/gi;
var regex_sup = /\+([\w çãõáàâéêíóôú\.,;:!?()ñ&'\"-]+)\+/gi;
var regex_htmltag = /<\/?\w+((\s+\w+(\s*=\s*(?:".*?"|'.*?'|[\^'">\s]+))?)+\s*|\s*)\/?>/i;
var regex_htmltagreplace = /<\/?\w+((\s+\w+(\s*=\s*(?:".*?"|'.*?'|[\^'">\s]+))?)+\s*|\s*)\/?>/gi;
var focusedEditableDiv = null;
var panZoom = null;
var isPanning = false;
var habitatExpander;

document.addEventListener('DOMContentLoaded', function() {
    attachFormPosters();
    attachAJAXContent(function(el) {
        addEvent('click', el, function(ev) {
            if(isPanning) {isPanning = false; return;}
            // clicked an UTM square in the taxon record table map
            if(ev.target.classList.contains('utmsquare')) {
                var mgrs = ev.target.getAttribute('lvf:quad');
                ev.target.classList.toggle('selected');

                var rows = document.querySelectorAll('#taxonrecordtable tr[data-mgrs="' + mgrs + '"]');
                for(var i=0; i<rows.length; i++) {
                    if(ev.target.classList.contains('selected'))
                        rows[i].classList.add('selected');
                    else
                        rows[i].classList.remove('selected');
                }

                var selquad = document.querySelectorAll('#taxonrecords-map rect.utmsquare.selected');
                if(selquad.length > 0)
                    document.getElementById('taxonrecordtable').classList.add('filtered');
                else
                    document.getElementById('taxonrecordtable').classList.remove('filtered');
            }
            // expand/collapse map
            if(ev.target.classList.contains('portugal')) {
                var svg = getParentbyTag(ev.target, 'svg');
                if(!svg.classList.contains('selected')) {
                    expandMap(svg);
                } else {    // destroy and reset SVG to initial state
                    contractMap(svg);
                }
            }
        });
    });

    /********************
        DATA SHEET
    *********************/

    // any change in the fields will show save button
    var inputs = document.querySelectorAll('#maindataform input:not(.nochangeevent), #maindataform select:not(.nochangeevent), #maindataform textarea:not(.nochangeevent)');
    for (var i = 0; i < inputs.length; i++) {
        addEvent('change', inputs[i], changeHandler);
    }

    // save all
    addEvent('submit', document.getElementById('maindataform'), function(ev) {
        document.getElementById('mainformsubmitter').classList.add('hidden');
    });

    addEvent('click', document.getElementById('removeformatting'), function(ev) {
        if(focusedEditableDiv.hasAttribute('contenteditable')) {
            focusedEditableDiv.innerHTML = focusedEditableDiv.innerHTML.replace(regex_htmltagreplace, '');
            document.getElementById('removeformatting').classList.add('hidden');
        }
    });

    // toggle summary / full view
    addEvent('click', document.getElementById('summary_toggle'), function(ev) {
        document.querySelector('table.sheet').classList.toggle('summary');
    });

    // toggle help tips
    addEvent('click', document.getElementById('toggle_help'), function(ev) {
        document.querySelector('table.sheet').classList.toggle('help');
    });

    /********************
        SPECIES INDEX
    *********************/
    // click event for taxon list checkboxes
    addEvent('click', document.getElementById('speciesindex'), function(ev) {
        if(ev.target.classList.contains('selectionbox')) {
            var row = getParentbyTag(ev.target, 'tr');
            if(ev.target.checked)
                row.classList.add('selected');
            else
                row.classList.remove('selected');

            var sel = document.querySelectorAll('#speciesindex tr.selected');
            updateSelectedInfo(sel.length);
            if(sel.length == 0)
                document.getElementById('editselectedtaxa').classList.add('hidden');
            else
                document.getElementById('editselectedtaxa').classList.remove('hidden');
        }
    });

    // select all checked rows
    var sel = document.querySelectorAll('#speciesindex input.selectionbox:checked');
    if(sel > 0) {
        for(var i = 0; i < sel.length; i++) {
            var row = getParentbyTag(sel[i], 'tr');
            row.classList.add('selected');
        }
        if(sel.length > 0) document.getElementById('editselectedtaxa').classList.remove('hidden');
        updateSelectedInfo(sel.length);
    }

    addEvent('click', document.getElementById('selectall'), function(ev) {
        var sel = document.querySelectorAll('#speciesindex input.selectionbox');
        var count = 0;
        for(var i = 0; i < sel.length; i++) {
            if(sel[i].parentNode.offsetParent === null) continue;
            sel[i].checked = true;
            count++;
        }
        updateSelectedInfo(count);
        var row = document.querySelectorAll('#speciesindex tbody tr');
        for(var i = 0; i < row.length; i++) {
            if(row[i].offsetParent === null) continue;
            row[i].classList.add('selected');
        }
        document.getElementById('editselectedtaxa').classList.remove('hidden');
    });

    // toggle selection
    addEvent('click', document.getElementById('toggleselectedtaxa'), function(ev) {
        var sel = document.querySelectorAll('#speciesindex input.selectionbox');
        var count = 0;
        for(var i = 0; i < sel.length; i++) {
            if(sel[i].parentNode.offsetParent === null) continue;
            if(sel[i].checked)
                sel[i].checked = false;
            else {
                sel[i].checked = true;
                count++;
            }
        }
        updateSelectedInfo(count);
        var row = document.querySelectorAll('#speciesindex tbody tr');
        for(var i = 0; i < row.length; i++) {
            if(row[i].offsetParent === null) continue;
            if(row[i].querySelector('input:checked'))
                row[i].classList.add('selected');
            else
                row[i].classList.remove('selected');
        }
        document.getElementById('editselectedtaxa').classList.remove('hidden');
    });

    addEvent('click', document.getElementById('selecttaxa'), function(ev) {
        var tids = window.prompt('Enter taxon IDs to select separated by commas');
        tids = tids.replace(/ /g, '');
        tids = tids.split(',');
        var count = 0;
        var sel = document.querySelectorAll('#speciesindex input.selectionbox');
        for(var i = 0; i < sel.length; i++) {
            if(tids.indexOf(sel[i].getAttribute('value')) > -1) {
                sel[i].checked = true;
                count++;
                var row = getParentbyTag(sel[i], 'tr');
                row.classList.add('selected');
            } else {
                sel[i].checked = false;
                var row = getParentbyTag(sel[i], 'tr');
                row.classList.remove('selected');
            }
        }
        updateSelectedInfo(count);
    });

    addEvent('click', document.getElementById('addtag'), function(ev) {
        var tag = window.prompt('Add the following tag to all the selected taxa:');
        if(!tag || tag.trim() == '') return;
        var sel = document.querySelectorAll('#speciesindex input.selectionbox:checked');
        if(sel.length == 0) {
            alert('No taxon selected.');
            return;
        }
        var form = document.getElementById('addtagform');
        var el = createHiddenInputElement('tag', tag);
        form.appendChild(el);
        for(var i = 0; i < sel.length; i++) {
            el = createHiddenInputElement('taxEntID', sel[i].value);
            form.appendChild(el);
        }
        postAJAXForm(form.getAttribute('data-path'), form, function(rt) {
            var rt1=JSON.parse(rt);
            if(rt1.success) {
                alert(rt1.msg);
                window.location.reload();
            } else
                alert(rt1.msg);
        });
    });

    var filters = document.querySelectorAll('#filters .filter');
    for(var i=0; i<filters.length; i++) {
        addEvent('click', filters[i], function(ev) {
            var el = getParentbyClass(ev.target, 'filter');
            el.classList.toggle('selected');
            document.getElementById('speciesindex').classList.toggle(el.id);
        });
    }

    var sel = document.querySelectorAll('.filterpanel');
    for(i = 0; i < sel.length; i++)
        sel[i].classList.remove('inactive');
/*
    addEvent('click', document.getElementById('highlight_toggle'), function(ev) {
        document.querySelector('table.sheet').classList.toggle('showhighlights');
    });
*/

    // attach events to all editable divs (which fake textareas with highlights)
    var editabledivs = document.querySelectorAll('#maindataform div[contenteditable=true]');
    for (var i = 0; i < editabledivs.length; i++) {
        addEvent('keyup', editabledivs[i], addHighlightOnType);
        addEvent('focus', editabledivs[i], contentEditableFocused);
        addEvent('blur', editabledivs[i], contentEditableBlurred);
    }
    // attach remove event to any highlights that the text may have
    var highs = document.querySelectorAll('#maindataform div[contenteditable=true] .highlight');
    for(var i = 0; i < highs.length; i++) {
        removeEvent('click', highs[i], removeHighlight);
        addEvent('click', highs[i], removeHighlight);
    }


/*
    addEvent('mousedown', document.getElementById('highlight'), function(ev) {
        ev.preventDefault();
        var sel = getSelected();
        if(sel.anchorNode != sel.focusNode) return;
        var el = document.getElementById('eeee');
        var ih = el.textContent;
        alert(ih);
        el.innerHTML = ih.slice(0, sel.anchorOffset - 1) + "<b>" + sel.toString() + "</b>" + ih.slice(sel.focusOffset);
    });
*/

    addEvent('click', document.getElementById('newauthor'), createNewAuthor);
    addEvent('click', document.getElementById('newevaluator'), createNewAuthor);
    addEvent('click', document.getElementById('newreviewer'), createNewAuthor);
    addEvent('click', document.getElementById('newtag'), createNewTag);
    addEvent('keydown', document.getElementById('tagbox'), function(ev) {
        if(ev.keyCode == 13) {
            ev.preventDefault();
            createNewTag.call(this, ev);
            return true;
        }
    });

    var maps = document.querySelectorAll('.svgmap');
    for(var i = 0; i < maps.length; i++) {
        (function(j) {
            addEvent('click', maps[j], function(ev) {
                document.querySelectorAll('.svgmap')[j].classList.toggle('floating');
                document.querySelector('body').classList.toggle('relative');
            });
        })(i);
    }

    // for user privileges
    attachSuggestionHandler('taxonbox', '../checklist/api/suggestions?limit=10&q=', 'suggestions', function(ev, name, key) {
        clickAddTag2(name, key, 'applicableTaxa', 'ta_', 'taxonprivileges');
    });

    var i = 0;
    while(document.getElementById('taxonbox_group_' + i)) {
        (function(j) {
            attachSuggestionHandler('taxonbox_group_' + j, '../checklist/api/suggestions?limit=10&q=', 'suggestions_group_' + j, function(ev, name, key) {
                clickAddTag2(name, key, 'applicableTaxa', 'ta_group_' + j + '_', 'taxa_group_' + j);
            });
        })(i);
        i++;
    }

    attachSuggestionHandler('authorbox', '../checklist/api/suggestions?limit=10&what=user&q=', 'authorsuggestions', function(ev, name, key) {
        if(clickAddTag2(name, key, 'assessment_Authors', 'aa_', 'textauthors'))
            changeHandler.call(this, ev);
    });
/*
    addEvent('click', document.getElementById('addtextauthor'), function(ev) {
        if(clickAddTag('authorbox', 'assessment_Authors', 'aa_', 'textauthors'))
            changeHandler.call(this, ev);
    });
*/

    attachSuggestionHandler('assessorbox', '../checklist/api/suggestions?limit=10&what=user&q=', 'assessorsuggestions', function(ev, name, key) {
        if(clickAddTag2(name, key, 'assessment_Evaluator', 'aas_', 'assessors'))
            changeHandler.call(this, ev);
    });
/*
    addEvent('click', document.getElementById('addassessor'), function(ev) {
        if(clickAddTag('assessorbox', 'assessment_Evaluator', 'aas_', 'assessors'))
            changeHandler.call(this, ev);
    });
*/

    attachSuggestionHandler('reviewerbox', '../checklist/api/suggestions?limit=10&what=user&q=', 'reviewersuggestions', function(ev, name, key) {
        if(clickAddTag2(name, key, 'assessment_Reviewer', 'are_', 'reviewers'))
            changeHandler.call(this, ev);
    });

    attachSuggestionHandler('addtaxonbox', '../checklist/api/suggestions?limit=20&q=', 'addtaxsuggestions', function(ev, name, key) {
        var box = document.getElementById('addtaxonbox');
        box.value = name + ' <' + key + '>';
        box.setAttribute('data-key', key);
        var el = document.querySelector('#addtaxon2redlist input[name=id]');
        if(!el) {
            el = document.createElement('INPUT');
            el.setAttribute('type', 'hidden');
            el.setAttribute('name', 'id');
            el.setAttribute('value', key);
            document.getElementById('addtaxon2redlist').appendChild(el);
        } else {
            el.value = key;
        }
    });

/*
        if(!document.getElementById('taxonbox').hasAttribute('data-key')) {
            alert("Type some letters to find a taxon and select from drop down list.");
            return;
        }
        var key = document.getElementById('taxonbox').getAttribute('data-key');
        var el = document.createElement('INPUT');
        el.setAttribute('type', 'checkbox');
        el.setAttribute('name', 'applicableTaxa');
        el.setAttribute('id', 'ta_'+key);
        el.setAttribute('value', key);
        el.setAttribute('checked', 'checked');

        var el1 = document.createElement('LABEL');
        el1.setAttribute('class', 'wordtag togglebutton');
        el1.setAttribute('for', 'ta_'+key);
        el1.appendChild(document.createTextNode(document.getElementById('taxonbox').value));

        document.getElementById('taxonprivileges').appendChild(el);
        document.getElementById('taxonprivileges').appendChild(el1);

        document.getElementById('taxonbox').removeAttribute('data-key')
        document.getElementById('taxonbox').value = '';
    });
*/
    /**** record table ***********/
    addEvent('click', document.getElementById('unselectrecords'), function(ev) {
        // unselect all records
        var rows = document.querySelectorAll('#taxonrecordtable tr.selected');
        for(var i=0; i<rows.length; i++)
            rows[i].classList.remove('selected');

        document.getElementById('taxonrecordtable').classList.remove('filtered');

        var quads = document.querySelectorAll('#taxonrecords-map rect.utmsquare.selected');
        for(var i=0; i<quads.length; i++)
            quads[i].classList.remove('selected');
    });

    addEvent('click', document.getElementById('taxonrecordtable'), function(ev) {
    // click record table rows and select map squares
        var tr = getParentbyTag(ev.target, 'tr');
        if(tr) {
            document.getElementById('taxonrecordtable').classList.remove('filtered');
            var mgrs = tr.getAttribute('data-mgrs');
            if(mgrs) {
                var map = document.getElementById('taxonrecords-map');
                tr.classList.toggle('selected');

                var quads = map.querySelectorAll('rect.utmsquare[lvf\\:quad="' + mgrs + '"]');
                for(var i=0; i<quads.length; i++) {
                    if(tr.classList.contains('selected'))
                        quads[i].classList.add('selected');
                    else
                        quads[i].classList.remove('selected');
                }

                var rows = document.querySelectorAll('#taxonrecordtable tr[data-mgrs="' + mgrs + '"]');
                for(var i=0; i<rows.length; i++) {
                    if(tr.classList.contains('selected'))
                        rows[i].classList.add('selected');
                    else
                        rows[i].classList.remove('selected');
                }

                if(rows.length > 0)
                    expandMap(document.getElementById('taxonrecords-map'));
            }
        }
    });

    if(document.querySelector('input[name=taxEntID]')) {
        var did = document.querySelector('input[name=taxEntID]').value;
        var terr = document.querySelector('input[name=territory]').value;
        habitatExpander = new TreeExpander(document.querySelectorAll('#habitat-tree ul'), function(ev, key) {
            // tree node was clicked
            if(ev.target.classList.contains('button')) {
                ev.preventDefault();
                return false;
            } else {
                var all = this.elements[0].querySelectorAll('li');
                var cickedel = getParentbyTag(ev.target, 'li').querySelector('input');
                for(var i=0; i<all.length; i++) {
                    if(all[i].getAttribute('data-key') == key) {
                        if(cickedel.checked) {
                            all[i].querySelector('input').setAttribute('checked', 'checked');
                            all[i].querySelector('input').checked = true;
                        } else {
                            all[i].querySelector('input').removeAttribute('checked');
                            all[i].querySelector('input').checked = false;
                        }
                    }
                }
                changeHandler.call(this, ev);
            }
            return true;
        }, '../checklist/api/lists?w=tree&territory=' + encodeURIComponent(terr) + '&taxent=' + encodeURIComponent(did) + '&id={id}').init();
    }
});

window.addEventListener('beforeunload', function (ev) {
    if(isFormSubmitting) return;
    var confirmationMessage = 'You have unsaved changes! If you leave you lose them! Are you sure?';
    if(document.getElementById('mainformsubmitter') && !document.getElementById('mainformsubmitter').classList.contains('hidden')) {
        (ev || window.event).returnValue = confirmationMessage;
        return confirmationMessage;
    } else {
        return null;
    }
});

function expandMap(svg) {
    if(svg.getAttribute('viewBox'))
        svg.setAttributeNS('http://flora-on.pt', 'vb', svg.getAttribute('viewBox'));
    addEvent('transitionend', svg, attachSVGZoomPan);
    svg.classList.add('selected');
}

function contractMap(svg) {
    removeEvent('transitionend', svg, attachSVGZoomPan);
    panZoom.destroy();
    delete panZoom;
    svg.removeAttribute('style');
    if(svg.getAttributeNS('http://flora-on.pt', 'vb'))
        svg.setAttribute('viewBox', svg.getAttributeNS('http://flora-on.pt', 'vb'));
    var vp = svg.querySelector('.svg-pan-zoom_viewport');
    svg.appendChild(vp.querySelector('g'));
    svg.removeChild(vp);
    svg.classList.remove('selected');
}

function contentEditableFocused(ev) {
    focusedEditableDiv = ev.target;
    checkHtmlTags(ev.target.innerHTML);
}

function attachSVGZoomPan(ev) {
    if(ev.target) {
        ev.target.style = 'width:'+ev.target.getBoundingClientRect().width+'px';
        panZoom = svgPanZoom(ev.target, {
            onPan: function() {isPanning = true;}
            , zoomScaleSensitivity: 0.5
        });
    }
}


function contentEditableBlurred(ev) {
    setTimeout(function() {
        if(document.activeElement.id != 'removeformatting' && !document.activeElement.hasAttribute('contenteditable')) {
            focusedEditableDiv = null;
            document.getElementById('removeformatting').classList.add('hidden');
        }
    }, 10);
}

function clickAddTag2(name, key, inputName, prefix, multipleChooserId) {
        var el = document.createElement('INPUT');
        el.setAttribute('type', 'checkbox');
        el.setAttribute('name', inputName);
        el.setAttribute('id', prefix + key);
        el.setAttribute('value', key);
        el.setAttribute('checked', 'checked');

        var el1 = document.createElement('LABEL');
        el1.setAttribute('class', 'wordtag togglebutton');
        el1.setAttribute('for', prefix + key);
        el1.appendChild(document.createTextNode(name));

        document.getElementById(multipleChooserId).appendChild(el);
        document.getElementById(multipleChooserId).appendChild(el1);

        return true;

}

/*
function clickAddTag(inputBoxId, inputName, prefix, multipleChooserId) {
        var inputBox = document.getElementById(inputBoxId);
        if(!inputBox.hasAttribute('data-key')) {
            alert("Type some letters to find an entity and select it from the drop down list.");
            return false;
        }
        var key = inputBox.getAttribute('data-key');
        var el = document.createElement('INPUT');
        el.setAttribute('type', 'checkbox');
        el.setAttribute('name', inputName);
        el.setAttribute('id', prefix + key);
        el.setAttribute('value', key);
        el.setAttribute('checked', 'checked');

        var el1 = document.createElement('LABEL');
        el1.setAttribute('class', 'wordtag togglebutton');
        el1.setAttribute('for', prefix + key);
        el1.appendChild(document.createTextNode(inputBox.value));

        document.getElementById(multipleChooserId).appendChild(el);
        document.getElementById(multipleChooserId).appendChild(el1);

        inputBox.removeAttribute('data-key')
        inputBox.value = '';
        return true;
}
*/

function changeHandler(ev) {
    if(ev != null && ev.target.classList.contains('nochangeevent')) return;

    if(ev != null && ev.target.classList.contains('trigger')) {   // this field triggers display/hide other fields
        var triggered = getParentbyClass(ev.target, 'triggergroup');
        if(triggered)
            triggered = triggered.querySelectorAll('.triggered');
        else
            return;

        switch(ev.target.tagName) {
        case 'SELECT':
            var trigger = parseInt(ev.target.querySelector('option:checked').getAttribute('data-trigger')) == 1 ? true : false;
            break;

        case 'INPUT':
            var va = ev.target.value;
            switch(ev.target.getAttribute('type').toLowerCase()) {
            case 'text':
                if(va && va.trim().length > 0) var trigger = true;
                break;

            case 'radio':
                var trigger = parseInt(ev.target.getAttribute('data-trigger')) == 1 ? true : false;
                break;
            }
            break;
        }

        for(var i = 0; i < triggered.length; i++) {
            if(trigger)
                triggered[i].classList.remove('hidden');
            else
                triggered[i].classList.add('hidden');
        }
    }
    showSaveButton();
}

function showSaveButton() {
    document.getElementById('mainformsubmitter').classList.remove('hidden');
}

function createNewTag(ev) {
    var name = document.getElementById('tagbox').value;
    if(name != null && name.trim() != '') {
        var el = document.createElement('INPUT');
        el.setAttribute('type', 'checkbox');
        el.setAttribute('name', 'tags');
        el.setAttribute('id', 'tags_' + name);
        el.setAttribute('value', name);
        el.setAttribute('checked', 'checked');

        var el1 = document.createElement('LABEL');
        el1.setAttribute('class', 'wordtag togglebutton');
        el1.setAttribute('for', 'tags_' + name);
        el1.appendChild(document.createTextNode(name));

        document.getElementById('tagchooser').appendChild(el);
        document.getElementById('tagchooser').appendChild(el1);

        document.getElementById('tagbox').value = '';
        changeHandler.call(this, null);
    }
}

function createNewAuthor(ev) {
    if(!document.getElementById('mainformsubmitter').classList.contains('hidden')) {
        alert('You must save the form before creating a new author.');
        return;
    }

    var name = prompt("Enter new author's name.\nNote that this user will not have a login account created.");
    if(name != null) {
        postJSON('admin/createuser', {name: name}, function(rt) {
            var rt1=JSON.parse(rt);
            if(rt1.success) {
                if(rt1.msg && rt1.msg.alert)
                    alert(rt1.msg.text);
                window.location.reload();
            } else
                alert(rt1.msg);
        });
    }
}

function checkHtmlTags(text) {
    if(regex_htmltag.test(text)) {
        document.getElementById('removeformatting').classList.remove('hidden');
    } else {
        document.getElementById('removeformatting').classList.add('hidden');
    }
}

function addHighlightOnType(ev) {
    if(ev.keyCode < 65 && ev.keyCode != 8 && ev.keyCode != 32) return;
    showSaveButton();
    var el = ev.target;
    if(el.nextElementSibling.tagName.toLowerCase() != 'input') {
        alert("Error! Changes will not be saved. Contact the programmer.");
        return;
    }

    checkHtmlTags(el.innerHTML);

    if(!regex_highlight.test(el.innerHTML) && !regex_under.test(el.innerHTML) && !regex_sup.test(el.innerHTML) ) {
        el.nextElementSibling.value = el.innerHTML;
        return;
    }
    var spanhighlight = function(a, b) { return '<span class="highlight yellow">' + b + '</span>';};
    var spanitalic = function(a, b) { return '<span class="highlight italic">' + b + '</span>';};
    var spansup = function(a, b) { return '<sup>' + b + '</sup>';};

    var ma = el.innerHTML.replace(regex_highlight, spanhighlight);
    ma = ma.replace(regex_under, spanitalic);
    ma = ma.replace(regex_sup, spansup);
    el.innerHTML = ma;
    el.nextElementSibling.value = ma;

    var highs = el.querySelectorAll('.highlight');
    for(var i = 0; i < highs.length; i++) {
        removeEvent('click', highs[i], removeHighlight);
        addEvent('click', highs[i], removeHighlight);
    }
}

function removeHighlight(ev) {
    var el = ev.target;
    var div = getParentbyClass(el, 'contenteditable');
    el.outerHTML = el.innerHTML;
    if(div) {
        div.nextElementSibling.value = div.innerHTML;
        showSaveButton();
    } else
        alert("Error! Changes will not be saved. Contact the programmer.");
}

function updateSelectedInfo(n) {
    document.getElementById('selectedmsg').innerHTML = n + ' selected taxa';
}
/*
function getSelected() {
    if (window.getSelection) {
        return window.getSelection();
    } else if (document.getSelection) {
        return document.getSelection();
    }
    else {
        var selection = document.selection && document.selection.createRange();
        if (selection.text) {
            return selection.text;
        }
        return false;
    }
    return false;
}
*/
