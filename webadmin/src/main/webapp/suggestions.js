function attachSuggestionHandler(elid) {
	querybox=document.getElementById(elid);
	addEvent('keydown', querybox, function(ev) {
		var input=ev.target;
		if(input.hasAttribute('data-key')) {
		    input.removeAttribute('data-key');
		    input.value = '';
		    return false;
		}
	});

    addEvent('keypress', querybox, function(ev) {
        if(ev.keyCode == 13) {
            ev.preventDefault();

            return true;
        }
    });

	addEvent('keyup', querybox, function(ev) {
        var input=ev.target;
		if(ev.keyCode < 65 && ev.keyCode != 8 && ev.keyCode != 32) return;
		if(input.value.trim().length==0) {
			document.getElementById('suggestions').innerHTML='';
			return;
		}
		fetchAJAX('/floraon/checklist/api/suggestions?limit=30&q='+encodeURIComponent(input.value), function(rt) {
			document.getElementById('suggestions').innerHTML=rt;
			makeSuggestionBox(document.getElementById('suggestions').querySelector('ul.suggestions'), input.id);
		});
	});
}

function makeSuggestionBox(el,targetInput) {
	el.setAttribute('data-inputel',targetInput);
	
	el.querySelector('li:first-child').classList.add('selected');
	var lis=el.querySelectorAll('li');
	for(var i=0;i<lis.length;i++) {
		addEvent('mouseenter',lis[i],suggestionOver);
	}
	
	addEvent('click',el,suggestionClick);
}

function suggestionOver(ev) {
	if(!ev.target.classList.contains('selected')) {
		var lis=ev.target.parentNode.querySelectorAll('li');
		for(var i=0;i<lis.length;i++) lis[i].classList.remove('selected');
		ev.target.classList.add('selected');
	}
}

function suggestionClick(ev) {
	var li=_getParentbyTag(ev.target,'li');
	var el=li.parentNode.getAttribute('data-inputel');
	var inp=document.getElementById(el);
	inp.value=li.innerHTML.replace(/<[^>]*>/g, '');
	inp.setAttribute('data-key',li.getAttribute('data-key'));
	
	var ul=_getParentbyTag(ev.target,'ul');
	ul.parentNode.removeChild(ul);
}

function _getParentbyTag(el,tagname) {
	while(el.tagName.toLowerCase()!=tagname) {
		el=el.parentNode;
	};
	return(el);
}

