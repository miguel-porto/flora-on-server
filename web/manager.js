//var treeNodeToolbar=createHTML('<div class="tools"><div class="button" data-cmd="delete"/>x</div><div class="button" data-cmd="add"/>add</div></div>');
var clickEvent = new MouseEvent('click', {
	'view': window,
	'bubbles': true,
	'cancelable': true
});

function actionButtonClick(ev) {
	var el=document.getElementById('taxdetails');
	switch(ev.target.id) {
	case 'addsynonym':
		if(!document.getElementById('boxsynonym').hasAttribute('data-key')) {alert('You must select a taxon from the list.');return;}
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
		postJSON('/nodes/update/taxent',obj,function(rt) {
			rt=JSON.parse(rt);
			if(rt.success) {
				loadTaxDetails(parent,el);
				var li=document.getElementById('taxtree').querySelector('li[data-key="'+parent+'"]');
				var par=li.parentNode.parentNode;
				par.removeChild(par.querySelector('ul'));
				par.dispatchEvent(clickEvent);
			} else
				alert(rt.msg);
		});
		break;
	}
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
				ev.target.parentNode.classList.toggle('off');
			});
		}
	});
}

document.addEventListener('DOMContentLoaded', function() {
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
		switch(ev.target.tagName) {
		case 'LI':
			var key=ev.target.getAttribute('data-key');
			loadTaxDetails(key,document.getElementById('taxdetails'));
//			window.location.search='w=tree&tid='+encodeURIComponent(key);
			if(ev.target.querySelector('ul')) return;
			fetchAJAX('/lists/tree?fmt=htmllist&id='+encodeURIComponent(key),function(rt) {
				var html=createHTML(rt);
				ev.target.appendChild(html);
			});
			
			break;
/*			
		case 'DIV':
			if(ev.target.classList.contains('button')) {
				var id=getParentbyTag(ev.target,'li').getAttribute('data-key');
				switch(ev.target.getAttribute('data-cmd')) {
				case 'delete':
					if(!confirm('Are you sure you want to delete this taxon, ALL its attributes and ALL occurrences of this taxon???')) break;
					postJSON('/nodes/delete',{id:id},function(rt) {
						alert(rt);
					});
					break;

				case 'add':
					
					break;
				}
			}
			break;*/
		}
	});

/*
	var els=document.querySelectorAll('.taxtree-holder>ul li');
	for(var i=0;i<els.length;i++) {
		addEvent('mouseover',els[i],hoverTreeNode);
	}*/
});

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

