var regex_highlight = /\*([\w çãõáàâéêíóôú\.,;:!?()]+)\*/gi;
var regex_under = /_([\w çãõáàâéêíóôú\.,;:!?()]+)_/gi;

document.addEventListener('DOMContentLoaded', function() {
    attachFormPosters();

    // any change in the fields will show save button
    var inputs = document.querySelectorAll('#maindataform input, #maindataform select, #maindataform textarea');
    for (var i = 0; i < inputs.length; i++) {
        addEvent('change', inputs[i], changeHandler);
    }

    // save all
    addEvent('click', document.getElementById('mainformsubmitter'), function(ev) {
        document.getElementById('mainformsubmitter').classList.add('hidden');
    });

    // toggle summary / full view
    addEvent('click', document.getElementById('summary_toggle'), function(ev) {
        document.querySelector('table.sheet').classList.toggle('summary');
    });

    addEvent('click', document.getElementById('highlight_toggle'), function(ev) {
        document.querySelector('table.sheet').classList.toggle('showhighlights');
    });

    // attach events to all editable divs (which fake textareas with highlights)
    var editabledivs = document.querySelectorAll('#maindataform div[contenteditable=true]');
    for (var i = 0; i < editabledivs.length; i++)
        addEvent('keyup', editabledivs[i], addHighlightOnType);

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

    addEvent('click', document.getElementById('map'), function(ev) {
        document.getElementById('map').classList.toggle('floating');
        document.querySelector('body').classList.toggle('relative');
    });

    // for user privileges
    attachSuggestionHandler('boxsynonym');

    addEvent('click', document.getElementById('addtaxonprivilege'), function(ev) {
        if(!document.getElementById('boxsynonym').hasAttribute('data-key')) {
            alert("Type some letters to find a taxon and select from drop down list.");
            return;
        }
        var key = document.getElementById('boxsynonym').getAttribute('data-key');
        var el = document.createElement('INPUT');
        el.setAttribute('type', 'checkbox');
        el.setAttribute('name', 'applicableTaxa');
        el.setAttribute('id', 'ta_'+key);
        el.setAttribute('value', key);
        el.setAttribute('checked', 'checked');

        var el1 = document.createElement('LABEL');
        el1.setAttribute('class', 'wordtag togglebutton');
        el1.setAttribute('for', 'ta_'+key);
        el1.appendChild(document.createTextNode(document.getElementById('boxsynonym').value));

        document.getElementById('taxonprivileges').appendChild(el);
        document.getElementById('taxonprivileges').appendChild(el1);

        document.getElementById('boxsynonym').removeAttribute('data-key')
        document.getElementById('boxsynonym').value = '';
    });

});

function changeHandler(ev) {
    if(ev.target.classList.contains('trigger')) {   // this field triggers display/hide other fields
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
            if(va && va.trim().length > 0) var trigger = true;
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

function createNewAuthor(ev) {
    if(!document.getElementById('mainformsubmitter').classList.contains('hidden')) {
        alert('You must save the form before creating a new author.');
        return;
    }

    var name = prompt("Enter new author's name.\nNote that this user will not have a login account created.");
    if(name != null) {
        postJSON('/floraon/admin/createuser', {name: name}, function(rt) {
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

function addHighlightOnType(ev) {
    if(ev.keyCode < 65 && ev.keyCode != 8 && ev.keyCode != 32) return;
    showSaveButton();
    var el = ev.target;
    if(el.nextElementSibling.tagName.toLowerCase() != 'input') {
        alert("Error! Changes will not be saved. Contact the programmer.");
        return;
    }
    if(!regex_highlight.test(el.innerHTML) && !regex_under.test(el.innerHTML) ) {
        el.nextElementSibling.value = el.innerHTML;
        return;
    }
    var spanhighlight = function(a, b) { return '<span class="highlight yellow">' + b + '</span>';};
    var spanitalic = function(a, b) { return '<span class="highlight italic">' + b + '</span>';};

    var ma = el.innerHTML.replace(regex_highlight, spanhighlight);
    ma = ma.replace(regex_under, spanitalic);
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
