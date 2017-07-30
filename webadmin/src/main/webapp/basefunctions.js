if (typeof MouseEvent !== 'function') {
	var clickEvent = document.createEvent("MouseEvent");
	clickEvent.initMouseEvent("click",true,true,window,0,0,0,0,0,false,false,false,false,0,null);
} else {
	var clickEvent = new MouseEvent('click', {
		'view': window,
		'bubbles': true,
		'cancelable': true
	});
}

var $=function(elem) {
  return document.querySelectorAll(elem);
}

function addEvent(evnt, elem, func) {
    if(!elem) return;
   if (elem.addEventListener)  // W3C DOM
      elem.addEventListener(evnt,func,false);
   else if (elem.attachEvent) { // IE DOM
      elem.attachEvent('on'+evnt, func);
   } else { // No much to do
      elem['on'+evnt] = func;
   }
}

function removeEvent(evnt,elem,func) {
   if (elem.removeEventListener)  // W3C DOM
      elem.removeEventListener(evnt,func,false);
   else if (elem.detachEvent) { // IE DOM
      elem.detachEvent('on'+evnt, func);
   } else { // No much to do
      elem['on'+evnt] = null;
   }
}

function postJSON(addr,obj,callback) {
	loadXMLDocPOSTJSON(addr,obj,function(xmlo) {
		xmlo=xmlo.target;
		if(xmlo.readyState == 4) {
			if(xmlo.status == 200) callback(xmlo.responseText); else {
				var rt=JSON.parse(xmlo.responseText);
				alert('ERROR: '+rt.msg);
			}
		}
	});
}

function postAJAXForm(addr,formElement,callback) {
	loadXMLDocPOST(addr,formElement,function(xmlo) {
		xmlo=xmlo.target;
		if(xmlo.readyState == 4) {
			if(xmlo.status == 200) callback(xmlo.responseText); else {
			    console.log(xmlo.responseText);
				var rt=JSON.parse(xmlo.responseText);
				alert('ERROR: '+rt.msg);
				var loader = document.getElementById('loader');
				if(loader) loader.style.display = 'none';
			}
		}
	});
}

function loadXMLDocPOSTJSON(doc,obj,onopen) {
    var xmlhttp = new XMLHttpRequest();
	var params = new FormData();
	for(var k in obj) {
		params.append(k,obj[k]);
	}
	xmlhttp.open("POST", doc, true);
//	xmlhttp.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
	xmlhttp.setRequestHeader("Content-length", params.length);
	xmlhttp.setRequestHeader("Connection", "close");

	xmlhttp.onreadystatechange = onopen;
	xmlhttp.send(params);
}

function loadXMLDocPOST(doc,formElement,onopen) {
    var xmlhttp = new XMLHttpRequest();
	var params = new FormData(formElement);
/*	var tmp;
	for(var pair of params.entries()) {
	   console.log(pair[0]+ ', '+ pair[1]); 
	}*/
	xmlhttp.open("POST", doc, true);
//	xmlhttp.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
	xmlhttp.setRequestHeader("Content-length", params.length);
	xmlhttp.setRequestHeader("Connection", "close");

	xmlhttp.onreadystatechange = onopen;
	xmlhttp.send(params);
}

function loadXMLDoc(doc,onopen) {
    var xmlhttp;

    if (window.XMLHttpRequest) {	// code for IE7+, Firefox, Chrome, Opera, Safari
        xmlhttp = new XMLHttpRequest();
    } else {	// code for IE6, IE5
        xmlhttp = new ActiveXObject("Microsoft.XMLHTTP");
    }
    xmlhttp.onreadystatechange = onopen;
    xmlhttp.open("GET", doc, true);
    xmlhttp.send();
    return xmlhttp;
}

function showLoader() {
	var el=document.getElementById('loader');
	if(el) el.style.display='block';
}

function hideLoader() {
	var el=document.getElementById('loader');
	if(el) el.style.display='none';
}

function fetchAJAX(addr, callback, onerror) {
	//showLoader();
	return loadXMLDoc(addr,function() {
		if(this.readyState == 4) {
			if(this.status == 200) {
			    callback(this.responseText);
            } else {
                if(onerror) {
                    console.log(rt);
                    onerror(this.responseText);
                } else {
                    if(this.responseText == '') return;
                    console.log(this.responseText);
                    var rt=JSON.parse(this.responseText);
                    alert('ERROR: '+rt.msg);
                }
			}
		}
	});
}

function getQueryVariable(query,variable) {
	if(!query) return null;
    var query = query.substring(1);
    var vars = query.split('&');

    for (var i = 0; i < vars.length; i++) {
        var pair = vars[i].split('=');
        if (decodeURIComponent(pair[0]) == variable) {
            return decodeURIComponent(pair[1]);
        }
    }
    return null;
}

/*function updateSVG(el,dots) {
	var keys,k;
	for(var i=0;i<dots.length;i++) {
		var newElement = document.createElementNS("http://www.w3.org/2000/svg", dots[i].type);
		for(var k in dots[i]) {
			if(k!='type' && k!='elements' && k!='text') {
				if(k.indexOf(':')>-1)
					newElement.setAttributeNS('http://www.flora-on.pt',k.split(':')[1],dots[i][k]);
				else
					newElement.setAttributeNS(null,k,dots[i][k]);
			}
		}
		if(dots[i].type=='text') newElement.innerHTML=dots[i].text;
		el.appendChild(newElement);
		if(dots[i].type=='g') updateSVG(newElement,dots[i].elements);
		
//		if(newElement.classList.contains('contour')) attachOverPoly([newElement]);
	}
}*/

function getParentbyTag(el,tagname) {
	while(el.tagName && el.tagName.toLowerCase() != tagname) {
		el = el.parentNode;
	};
	return (!el.tagName || (el.tagName && el.tagName.toLowerCase() != tagname)) ? null : el;
}

function getParentbyClass(el,classname) {
	if(!el) return(null);
	while(!el.classList.contains(classname)) {
		el=el.parentNode;
		if(!el.classList) return null;
	};
	return(el);
}

function getNextSiblingByClass(el, classname) {
	if(!el) return null;
	el = el.nextSibling;
	if(!el) return null;

	while(true) {
	    if(el.classList && el.classList.contains(classname)) return el;
		el = el.nextSibling;
		if(!el) break;
	};
	return null;
}

SVGElement.prototype.addClass = function (className) {
  if (!this.hasClass(className)) {
    this.setAttribute('class', this.getAttribute('class') + ' ' + className);
  }
};

SVGElement.prototype.removeClass = function (className) {
  if (this.hasClass(className)) {
    this.setAttribute('class', this.getAttribute('class').replace(className,''));
  }
};

SVGElement.prototype.hasClass = function (className) {
  return new RegExp('(\\s|^)' + className + '(\\s|$)').test(this.getAttribute('class'));
};

function createHTML(htmlStr) {
    var frag = document.createDocumentFragment(),
        temp = document.createElement('div');
    temp.innerHTML = htmlStr;
    while (temp.firstChild) {
        frag.appendChild(temp.firstChild);
    }
    return frag;
}


function eventFire(el, etype){
  if (el.fireEvent) {
    el.fireEvent('on' + etype);
  } else {
    var evObj = document.createEvent('Events');
    evObj.initEvent(etype, true, false);
    el.dispatchEvent(evObj);
  }
}

/*
** Returns the caret (cursor) position of the specified text field.
** Return value range is 0-oField.value.length.
** http://stackoverflow.com/questions/2897155/get-cursor-position-in-characters-within-a-text-input-field
*/
function doGetCaretPosition (oField) {
  var iCaretPos = 0;

  // IE Support
  if (document.selection) {

    // Set focus on the element
    oField.focus();

    // To get cursor position, get empty selection range
    var oSel = document.selection.createRange();

    // Move selection start to 0 position
    oSel.moveStart('character', -oField.value.length);

    // The caret position is selection length
    iCaretPos = oSel.text.length;
  }

  // Firefox support
  else if (oField.selectionStart || oField.selectionStart == '0')
    iCaretPos = oField.selectionStart;

  return iCaretPos;
}

function randomString(length) {
    var chars = 'abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ';
    var result = '';
    for (var i = length; i > 0; --i) result += chars[Math.floor(Math.random() * chars.length)];
    return result;
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
    console.log(name+" : "+value);
    if(!el)
        parent.appendChild(createHiddenInputElement(name, value));
    else
        el.setAttribute(name, value);
}

function getSelectedText(el) {
/*
    if (window.getSelection) {
        return window.getSelection().toString();
    } else if (document.selection) {
        return document.selection.createRange().text;
    }
*/
    var selectedText;
    // IE version
    if (document.selection !== undefined) {
        el.focus();
        var sel = document.selection.createRange();
        selectedText = sel.text;
    } else if (el.selectionStart !== undefined) {
        var startPos = el.selectionStart;
        var endPos = el.selectionEnd;
        selectedText = el.value.substring(startPos, endPos);
    }
    return selectedText;
}
