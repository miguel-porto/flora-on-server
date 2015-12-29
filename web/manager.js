//var treeNodeToolbar=createHTML('<div class="tools"><div class="button" data-cmd="delete"/>x</div><div class="button" data-cmd="add"/>add</div></div>');
var TOPBARSIZE=85;
var clickEvent = new MouseEvent('click', {
	'view': window,
	'bubbles': true,
	'cancelable': true
});

function actionButtonClick(ev) {
	var el=document.getElementById('taxdetails');
	switch(ev.target.id) {
	case 'addsynonym':
		if(!document.getElementById('boxsynonym').hasAttribute('data-key')) {alert('You must select a taxon from the drop-down list. Type some initial letters to find taxa.');return;}
		var key=document.getElementById('boxsynonym').getAttribute('data-key');
		var to=document.getElementById('taxdetails').querySelector('input[name=nodekey]').value;
		postJSON('/nodes/setsynonym',{from:key,to:to},function(rt) {
			rt=JSON.parse(rt);
			if(rt.success)
				loadTaxDetails(to,el);
			else
				alert(rt.msg);
		});
		break;

	case 'addchild':
		var cb=document.getElementById('addchildbox');
		var parent=document.getElementById('taxdetails').querySelector('input[name=nodekey]').value;
		var obj={
			name:cb.querySelector('input[name=name]').value
			,author:cb.querySelector('input[name=author]').value
			,annot:cb.querySelector('input[name=annot]').value
			,parent:parent
			,rank:cb.querySelector('select[name=rank]').value
			,current:cb.querySelector('input[name=current]').checked ? 1 : 0
		}

		postJSON('/nodes/add/inferiortaxent',obj,function(rt) {
			rt=JSON.parse(rt);
			if(rt.success) {
				loadTaxDetails(rt.msg,el);
				var li=document.getElementById('taxtree').querySelector('li[data-key="'+parent+'"]');
				li.removeChild(li.querySelector('ul'));
				li.dispatchEvent(clickEvent);
			} else
				alert(rt.msg);
		});
		break;

	case 'deletetaxon':
		if(!confirm('Are you sure you want to delete this taxon?')) break;
		var parent=document.getElementById('taxdetails').querySelector('input[name=nodekey]').value;
		postJSON('/nodes/deleteleaf',{id:parent},function(rt) {
			rt=JSON.parse(rt);
			if(rt.success) {
				loadTaxDetails(null,el);
				var li=document.getElementById('taxtree').querySelector('li[data-key="'+parent+'"]');
				li.parentNode.removeChild(li);
			} else
				alert(rt.msg);
		});
		break;
	
	case 'updatetaxon':
		var cb=document.getElementById('updatetaxonbox');
		var parent=document.getElementById('taxdetails').querySelector('input[name=nodekey]').value;
		var obj={
			name:cb.querySelector('input[name=name]').value
			,author:cb.querySelector('input[name=author]').value
			,comment:cb.querySelector('input[name=annot]').value
			,id:parent
		}
		updateTaxon(obj);
		break;
	}
}

function updateTaxon(obj) {
	postJSON('/nodes/update/taxent',obj,function(rt) {
		var el=document.getElementById('taxdetails');
		rt=JSON.parse(rt);
		if(rt.success) {
			loadTaxDetails(obj.id,el);
			var li=document.getElementById('taxtree').querySelector('li[data-key="'+obj.id+'"]');
			var par=li.parentNode.parentNode;
			if(par.tagName=='LI') {
				par.removeChild(par.querySelector('ul'));
				//par.dispatchEvent(clickEvent);
				loadTreeNode(par,function() {
					var li=document.getElementById('taxtree').querySelector('li[data-key="'+obj.id+'"]');
					li.dispatchEvent(clickEvent);
				});
			} else window.location.reload();
		} else
			alert(rt.msg);
	});
}

function loadTaxDetails(key,el) {
	if(!key) {el.innerHTML='';return;}
	fetchAJAX('/admin/taxdetails.html?id='+encodeURIComponent(key),function(rt) {
		el.innerHTML=rt;
		attachSuggestionHandler('boxsynonym');
		
		var act=el.querySelectorAll('.actionbutton');
		for(var i=0;i<act.length;i++) {
			addEvent('click',act[i],actionButtonClick);
		}
		
// togglers
		var tog=el.querySelectorAll('div.toggler h1');
		for(var i=0;i<tog.length;i++) {
			addEvent('click',tog[i],function(ev) {
				var el=getParentbyTag(ev.target,'h1');
				el.parentNode.classList.toggle('off');
			});
		}
		
// current / not current buttons
		var els=document.querySelectorAll('#taxdetails ul.currentstatus li');
		for(var i=0;i<els.length;i++) {
			addEvent('click',els[i],function(ev) {
				if(ev.target.tagName!='LI' || ev.target.classList.contains('selected')) return;
				ev.target.parentNode.querySelector('li.selected').classList.remove('selected');
				ev.target.classList.add('selected');

				var cb=document.getElementById('updatetaxonbox');
				var parent=document.getElementById('taxdetails').querySelector('input[name=nodekey]').value;
				var obj={
					name:cb.querySelector('input[name=name]').value
					,author:cb.querySelector('input[name=author]').value
					,comment:cb.querySelector('input[name=annot]').value
					,id:parent
					,current:ev.target.classList.contains('current') ? 1 : 0
				}
				updateTaxon(obj);
			});
		}
		
// detach synonyms
		var els=document.querySelectorAll('#taxdetails ul.synonyms div.button.remove');
		for(var i=0;i<els.length;i++) {
			addEvent('click',els[i],function(ev) {
				var from=document.getElementById('taxdetails').querySelector('input[name=nodekey]').value;
				var to=ev.target.parentNode.getAttribute('data-key');
				postJSON('/nodes/detachsynonym',{from:from,to:to},function(rt) {
					rt=JSON.parse(rt);
					if(rt.success)
						loadTaxDetails(from,el);
					else
						alert(rt.msg);
				});
			});
		}

	});
}

document.addEventListener('DOMContentLoaded', function() {
	addEvent('scroll',document,function(ev) {
		var td=document.getElementById('taxdetails');
		var lb=document.getElementById('left-bar');
		var st=document.body.scrollTop || document.documentElement.scrollTop;
		if(st>TOPBARSIZE) {
			lb.style.marginTop=(st - TOPBARSIZE)+'px';
			if(td) td.style.marginTop=(st - TOPBARSIZE)+'px';
		} else {
			lb.style.marginTop='0';
			if(td) td.style.marginTop='0';
		}
	});
	
	var tr=document.querySelectorAll('#main.species tr');
	for(var i=0;i<tr.length;i++) {
		addEvent('click',tr[i],function(ev) {
			var par=getParentbyTag(ev.target,'tr');
			var td=par.querySelector('td');
		});
	}
	
	var forms=document.querySelectorAll('form.poster');
	for(var i=0;i<forms.length;i++) {
		addEvent('submit',forms[i],formPoster);
	}
	
// attach the click+expand tree node
	var lis=document.querySelector('.taxtree-holder>ul');
	addEvent('click',lis,function(ev) {
		var el=getParentbyTag(ev.target,'li');
		var key=el.getAttribute('data-key');
		loadTaxDetails(key,document.getElementById('taxdetails'));
		if(el.querySelector('ul')) return;
		loadTreeNode(el,null);
	});
	
	var qs=document.getElementById('freequery');
	if(qs) {
		addEvent('submit',qs,function(ev) {
			ev.preventDefault();
			var q=document.getElementById('querybox').value;
			if(q) {
				window.location.search='w=query&q='+encodeURIComponent(q);
			}
		});
	}
});

function loadTreeNode(el,callback) {
	var key=el.getAttribute('data-key');
	fetchAJAX('/lists/tree?fmt=htmllist&id='+encodeURIComponent(key),function(rt) {
		var html=createHTML(rt);
		el.appendChild(html);
		if(callback) callback();
	});
	
}

/**
	Forms must have a data-path attribute
*/
function formPoster(ev) {
		ev.preventDefault();
		postAJAXForm(ev.target.getAttribute('data-path'),ev.target,function(rt) {
			alert(rt);
		});
}

/*
function hoverTreeNode(ev) {
	if(ev.target.tagName!='LI') return;
	var child=ev.target.children;
	for(var i=0;i<child.length;i++) {
		if(child[i].classList.contains('tools')) return;
	}

	ev.target.appendChild(treeNodeToolbar);
	treeNodeToolbar=ev.target.querySelector('.tools');
//	treeNodeToolbar.querySelector('input[type=hidden]').value=ev.target.getAttribute('data-key');
}*/
