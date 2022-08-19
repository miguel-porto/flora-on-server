var currentSuggestionAjax = null;

function attachSuggestionHandler(elid, url, suggestionBoxId, onClick, allowFreeText, separator, keyDownHandler) {
	var querybox = document.getElementById(elid);

	if(suggestionBoxId) {
        addEvent('keydown', querybox, function(ev) {
            var input = ev.target;
            if(keyDownHandler) keyDownHandler.call(this, ev);

            if(document.getElementById(suggestionBoxId).querySelector('ul.suggestions')) {  // suggestions are visible
                var sel = document.getElementById(suggestionBoxId).querySelector('ul.suggestions li.selected');
                if((ev.keyCode == 38 || ev.keyCode == 40) && !sel) {
                    ev.preventDefault();
                    document.getElementById(suggestionBoxId).querySelector('li:first-child').classList.add('selected');
                    sel = document.getElementById(suggestionBoxId).querySelector('ul.suggestions li.selected');
                } else {
                    if(ev.keyCode == 38 && sel.previousSibling) {
                        ev.preventDefault();
                        sel.previousSibling.classList.add('selected');
                        sel.classList.remove('selected');
                    }
                    if(ev.keyCode == 40 && sel.nextSibling) {
                        ev.preventDefault();
                        sel.nextSibling.classList.add('selected');
                        sel.classList.remove('selected');
                    }
                }
            }

            if(input.hasAttribute('data-key')) {
                input.removeAttribute('data-key');
                input.value = '';
                return false;
            }
        });
    } else if(keyDownHandler) {
        addEvent('keydown', querybox, keyDownHandler);
    }

    addEvent('keypress', querybox, function(ev) {
        if(ev.keyCode == 13) {
            ev.preventDefault();
            return true;
        }
    });

	addEvent('keyup', querybox, function(ev) {
        var input = ev.target;

        if(ev.keyCode == 13) {
            if(suggestionBoxId) {
                var sel = document.getElementById(suggestionBoxId).querySelector('ul.suggestions li.selected');
                if(sel)
                    eventFire(sel, 'click');
                else if(allowFreeText) {
//                    var dry = (document.getElementById(suggestionBoxId).innerHTML == '');   // no suggestions being shown
                    var dry = (document.getElementById(suggestionBoxId).querySelector('li.selected') == null);   // no suggestions being shown or none selected
                    document.getElementById(suggestionBoxId).innerHTML = '';
                    if(onClick) {
                        onClick(ev, querybox.value, null, document.getElementById(suggestionBoxId).parentNode.parentNode, dry);
                    }
                }
                return;
            } else {
                if(onClick) {
                    // the 4th argument is the parent table cell where the editbox is located now
                    onClick(ev, querybox.value, null, querybox.parentNode.parentNode, true);
                }
            }
        }

        if(ev.keyCode == 27) {
            if(suggestionBoxId && document.getElementById(suggestionBoxId).innerHTML != '') {
                document.getElementById(suggestionBoxId).innerHTML = '';
                return;
			} else {
                if(onClick) {
                    onClick(ev, querybox.value, null, querybox.parentNode.parentNode, true);
                }
			}
        }

		if(ev.keyCode < 65 && ev.keyCode != 8 && ev.keyCode != 32) return;

        if(suggestionBoxId) {
            var it = getSuggestionInputText(input, separator);
            if(it.length == 0) {
                document.getElementById(suggestionBoxId).innerHTML='';
                return;
            }
        }

        if(url) {
            if(currentSuggestionAjax !== null) {
                currentSuggestionAjax.abort();
            }

            currentSuggestionAjax = fetchAJAX(url + encodeURIComponent(it), function(rt) {
                currentSuggestionAjax = null;
                var sb = document.getElementById(suggestionBoxId);
                sb.innerHTML = rt;

                if(typeof(getCursorXY) === typeof(Function)) {
                    var cp = getCursorXY(input, input.selectionEnd);
                    sb.style.position='absolute';
                    sb.style.top=(cp.y + 16 + 8) + 'px';
                }
//                document.getElementById(suggestionBoxId).style.left=cp.x + 'px';
                makeSuggestionBox(sb.querySelector('ul.suggestions'), input.id, onClick, separator);
            });
        }
	});
}

function getSuggestionInputText(el, separator) {
    if(!separator) return el.value;
    var c = doGetCaretPosition(el);
    var v = el.value.split(separator);
    if(c == 0) return v[0].trim();

    var i = 0;
    var len = 0;
    while(len <= c) {
        len += v[i].length + 1;
        i++;
    }
    return v[i-1].trim();
}

function makeSuggestionBox(el, targetInput, onClick, separator) {
	el.setAttribute('data-inputel', targetInput);

	var style = window.getComputedStyle(document.getElementById(targetInput), null);
//	el.style.minWidth = style.getPropertyValue('width');

    if(el.querySelector('li:first-child') == null) { // empty list
        el.parentNode.innerHTML = 'no suggestions';
        return;
    }
	//el.querySelector('li:first-child').classList.add('selected');

	var lis=el.querySelectorAll('li');
	for(var i=0;i<lis.length;i++) {
		addEvent('mouseenter',lis[i],suggestionOver);
	}

    addEvent('click', el, function(ev) {
        ev.stopPropagation();
        suggestionClick(ev, onClick, separator);
    });
}

function suggestionOver(ev) {
	if(!ev.target.classList.contains('selected')) {
		var lis=ev.target.parentNode.querySelectorAll('li');
		for(var i=0;i<lis.length;i++) lis[i].classList.remove('selected');
		ev.target.classList.add('selected');
	}
}

function suggestionClick(ev, onClick, separator) {
	var li = _getParentbyTag(ev.target,'li');
	var el = li.parentNode.getAttribute('data-inputel');
	var inp = document.getElementById(el);

	var tmp1 = li.textContent.replace(/<[^>]*>/g, '');
	if(!separator)
	    var key = li.getAttribute('data-key');
	else
        var key = null;

	var ul=_getParentbyTag(ev.target,'ul');
	var parent = ul.parentNode.parentNode.parentNode;
	ul.parentNode.removeChild(ul);

    setSuggestionInputText(inp, separator, tmp1);
    inp.focus();
	if(onClick) {
	    tmp1 = inp.value;
//        inp.value = '';
        onClick(ev, tmp1, key, parent);
	} else {
	    inp.setAttribute('data-key', key);
	}
}

function _getParentbyTag(el,tagname) {
	while(el.tagName.toLowerCase()!=tagname) {
		el=el.parentNode;
	};
	return(el);
}

function setSuggestionInputText(el, separator, text) {
    if(!separator) {
        el.value = text;
        return;
    }
    var c = doGetCaretPosition(el);
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
    el.value = v.join(separator + '');
    if(el.value.substr(-1) != separator) el.value = el.value + separator;
}

function attachOptionButtonHandler(url) {
    var optionbuttons = document.querySelectorAll('.option');
    for(var i = 0; i < optionbuttons.length; i++) {
        addEvent('click', optionbuttons[i], function(ev) {
            ev.stopPropagation();
            var optb = getParentbyClass(ev.target, 'option');
            var name = optb.getAttribute('data-option');
            var value = optb.getAttribute('data-value');
            var elid = optb.getAttribute('data-element');
            var type = optb.getAttribute('data-type');
            var norefresh = optb.getAttribute('data-norefresh');
            var persistent = optb.getAttribute('data-persistent');
            var allowdeselect = optb.getAttribute('data-allow-deselect');
            var vbool = (value == 'true');
            var el = (elid == '' ? null : document.getElementById(elid));
            if(type == 'radio' && allowdeselect && optb.classList.contains('selected'))
                value = 'null';
            if(type == 'radio' && !allowdeselect && optb.classList.contains('selected')) return;
            if(norefresh == 'true') {    // TODO this is specific to occurrence manager
                if(vbool) {
                    if(el) el.classList.remove('hiddenhard');
                    optb.classList.add('selected');
                    optb.setAttribute('data-value', false);
                } else {
                    if(el) el.classList.add('hiddenhard');
                    optb.classList.remove('selected');
                    optb.setAttribute('data-value', true);
                }
                if (typeof myMap !== 'undefined') myMap.invalidateSize(false);
            }
            console.log(url + '?w=setoption&n=' + encodeURIComponent(name) + '&v=' + encodeURIComponent(value) + '&t=' + type + '&persistent=' + (persistent == 'true' ? 1 : 0));
            fetchAJAX(url + '?w=setoption&n=' + encodeURIComponent(name) + '&v=' + encodeURIComponent(value) + '&t=' + type + '&persistent=' + (persistent == 'true' ? 1 : 0), function(rt) {
                if(norefresh == 'false') window.location.reload();
//                if(!el)
            });
        });
    }
}
