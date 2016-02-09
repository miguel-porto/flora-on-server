//var treeNodeToolbar=createHTML('<div class="tools"><div class="button" data-cmd="delete"/>x</div><div class="button" data-cmd="add"/>add</div></div>');
var TOPBARSIZE=85;
var nativeStatus=['NULL','NATIVE','ENDEMIC','EXOTIC','UNCERTAIN'];
var clickEvent = new MouseEvent('click', {
	'view': window,
	'bubbles': true,
	'cancelable': true
});
var timer;

document.addEventListener('DOMContentLoaded', function() {
	addEvent('scroll',document,function(ev) {
		var td=document.querySelector('.taxdetails');
		var lb=document.getElementById('left-bar');
		var st=document.body.scrollTop || document.documentElement.scrollTop;
		
		if(st > TOPBARSIZE)
			lb.style.marginTop=(st - TOPBARSIZE)+'px';
		else
			lb.style.marginTop='0';
			
		if(td) {
			var bcr=td.getBoundingClientRect();
			if(bcr.height > window.innerHeight) {
				if(st + window.innerHeight > TOPBARSIZE + bcr.height+16 )
					td.style.marginTop=(window.innerHeight - (bcr.height+16+TOPBARSIZE - st))+'px';
				else
					td.style.marginTop='0';
			} else {
				if(st > TOPBARSIZE)
					td.style.marginTop=(st - TOPBARSIZE)+'px';
				else
					td.style.marginTop='0';
			}
		}
	});

	var tr=document.querySelectorAll('#main.species tr');
	for(var i=0;i<tr.length;i++) {
		addEvent('click',tr[i],function(ev) {
			var par=getParentbyTag(ev.target,'tr');
			var td=par.querySelector('td');
		});
	}
	
	tr=document.querySelector('#main.checklist table');
	addEvent('click',tr,function(ev) {
		if(ev.target.tagName=='DIV' && ev.target.classList.contains('territory')) {
			var terr=ev.target.innerHTML;
			var nst=-1;
			for(var i=0;i<nativeStatus.length;i++) {
				if(ev.target.classList.contains(nativeStatus[i])) {
					if(i==nativeStatus.length-1)
						nst=0;
					else
						nst=i+1;
					break;
				}
			}
			if(nst==-1) nst=1;
			var tr=getParentbyTag(ev.target,'tr');
//			console.log({taxon:tr.getAttribute('data-key'), territory:terr, status:nativeStatus[nst]});
			postJSON('/floraon/api/territories/set',{taxon:tr.getAttribute('data-key'), territory:terr, status:nativeStatus[nst]},function(rt) {
				rt=JSON.parse(rt);
				if(rt.success) {
					if(nst==0)
						ev.target.classList.remove(nativeStatus[nativeStatus.length-1]);
					else {
						ev.target.classList.remove(nativeStatus[nst-1]);
						ev.target.classList.add(nativeStatus[nst]);
					}
				} else
					alert(rt.msg);
			});
		}
	});
	
/*	var forms=document.querySelectorAll('form.poster');
	for(var i=0;i<forms.length;i++) {
		addEvent('submit',forms[i],formPoster);
	}*/
	
// attach the click+expand tree node
	var lis=document.querySelector('.taxtree-holder>ul');
	addEvent('click',lis,function(ev) {
		var el=getParentbyTag(ev.target,'li');
		var key=el.getAttribute('data-key');
		if(el.classList.contains('loading')) return;
		loadTaxDetails(key,document.querySelector('.taxdetails'));
		if(el.querySelector('ul')) {
			el.removeChild(el.querySelector('ul'));
			return;
		}
		el.classList.add('loading');
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
	
	qs=document.getElementById('download-checklist');
	if(qs) {
		addEvent('click',qs,function(ev) {
			if(document.getElementById('checklink') || timer) return;
			fetchAJAX('/floraon/api/lists?w=checklist&fmt=csv',function(rt) {
				rt=JSON.parse(rt);
				if(rt.success) {
					var wnd1=showWindow('<div class="window float center" id="checklink"><div class="closebutton"></div><h1>Please wait while we prepare the checklist...</h1><p class="content" style="text-align:center">don\'t navigate away from this page...</p></div>');
					var link=rt.msg;
					timer=setInterval(function() {
						fetchAJAX('/floraon/api/job/'+rt.msg+'?query=1',function(rt) {
							rt=JSON.parse(rt);
							if(rt.success) {
								if(rt.msg=='true') {
									clearInterval(timer);
									timer=null;
									var wnd1=document.getElementById('checklink');
									if(wnd1) {
										wnd1.querySelector('h1').innerHTML='Checklist ready!';
										wnd1.querySelector('p.content').innerHTML='<a href="/floraon/api/job/'+link+'" target="_blank">click here to download</a>';
									} else {
										var wnd=showWindow('<div class="window float center" id="checklink"><div class="closebutton"></div><h1>Checklist ready!</h1><p class="content" style="text-align:center"><a href="/floraon/api/job/'+link+'" target="_blank">click here to download</a></p></div>');
									}
								}
							} else {
								clearInterval(timer);
								timer=null;
								alert(rt.msg);
							}
						});
					}, 1000);
				} else
					alert(rt.msg);
			});
		});
	}
	
	var td=document.querySelector('.taxdetails');
	if(td) attachTaxDetailsHandlers(td);
});

function showWindow(html) {
	var frag=createHTML(html);
	var wnd=frag.querySelector('.window');
	document.body.appendChild(frag);
	var cb=wnd.querySelector('.closebutton');
	if(cb) addEvent('click',cb,removeWindow);
	return wnd;
}

function removeWindow(ev) {
	var wnd=getParentbyClass(ev.target,'window');
	wnd.parentNode.removeChild(wnd);
}

function actionButtonClick(ev) {
	var el=document.querySelector('.taxdetails');
	switch(ev.target.id) {
	case 'addsynonym':
		if(!document.getElementById('boxsynonym').hasAttribute('data-key')) {alert('You must select a taxon from the drop-down list. Type some initial letters to find taxa.');return;}
		var key=document.getElementById('boxsynonym').getAttribute('data-key');
		var to=getCurrentTaxon();
		postJSON('/floraon/api/update/setsynonym',{from:key,to:to},function(rt) {
			rt=JSON.parse(rt);
			if(rt.success)
				loadTaxDetails(to,el);
			else
				alert(rt.msg);
		});
		break;

	case 'addchild':
		var cb=document.getElementById('addchildbox');
		var parent=getCurrentTaxon();
		var obj={
			name:cb.querySelector('input[name=name]').value
			,author:cb.querySelector('input[name=author]').value
			,annot:cb.querySelector('input[name=annot]').value
			,parent:parent
			,rank:cb.querySelector('select[name=rank]').value
			,current:cb.querySelector('input[name=current]').checked ? 1 : 0
		}

		postJSON('/floraon/api/update/add/inferiortaxent',obj,function(rt) {
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
		if(!confirm('Are you sure you want to delete this taxon? This cannot be undone!')) break;
		var parent=getCurrentTaxon();
		postJSON('/floraon/api/update/deleteleaf',{id:parent},function(rt) {
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
		var parent=getCurrentTaxon();
		// TODO: make formdata from html form directly
		var obj={
			name:cb.querySelector('input[name=name]').value
			,author:cb.querySelector('input[name=author]').value
			,comment:cb.querySelector('input[name=annot]').value
			,rank:cb.querySelector('input[name=rank]').value
			,current:cb.querySelector('input[name=current]').value
			,id:parent
		}
		updateTaxon(obj,true);
		break;
	
	case 'addnativestatus':
		var cb=document.getElementById('addnativestatusbox');
		var taxon=getCurrentTaxon();
		var obj={
			taxon: taxon
			,territory: cb.querySelector('select[name=territory]').value
			,status: cb.querySelector('select[name=status]').value
		};
		postJSON('/floraon/api/territories/set',obj,function(rt) {
			rt=JSON.parse(rt);
			if(rt.success) {
				loadTaxDetails(taxon,document.querySelector('.taxdetails'));
			} else
				alert(rt.msg);
		});

		break;
	}
}

function updateTaxon(obj, replace) {
	obj.replace=replace ? 1 : 0;
	//console.log(obj);
	postJSON('/floraon/api/update/update/taxent',obj,function(rt) {
		var el=document.querySelector('.taxdetails');
		rt=JSON.parse(rt);
		if(rt.success) {
			loadTaxDetails(obj.id,el);
			var li=document.getElementById('taxtree');
			if(!li) return;	// no taxon tree
			li=li.querySelector('li[data-key="'+obj.id+'"]');
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
	fetchAJAX('/floraon/api/taxdetails?id='+encodeURIComponent(key),function(rt) {
		el.innerHTML=rt;
		attachTaxDetailsHandlers(el);
	});
}

function getCurrentTaxon() {
	var parent=document.querySelector('.taxdetails input[name=nodekey]')
	if(parent)
		return parent.value;
	else
		return null;
}

function attachTaxDetailsHandlers(el) {
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
	var els=document.querySelectorAll('.taxdetails ul.currentstatus li');
	var cb=document.getElementById('updatetaxonbox');
	if(cb) {
		for(var i=0;i<els.length;i++) {
			addEvent('click',els[i],function(ev) {
				if(ev.target.tagName!='LI' || ev.target.classList.contains('selected')) return;
				ev.target.parentNode.querySelector('li.selected').classList.remove('selected');
				ev.target.classList.add('selected');
				var cb=document.getElementById('updatetaxonbox');
				var parent=getCurrentTaxon();
				var obj={
					/*name:cb.querySelector('input[name=name]').value
					,author:cb.querySelector('input[name=author]').value
					,comment:cb.querySelector('input[name=annot]').value
					,*/id:parent
					,current:ev.target.classList.contains('current') ? 1 : 0
				}
				console.log(obj);
				updateTaxon(obj,false);
			});
		}
	}	
// detach synonyms
	var els=document.querySelectorAll('.taxdetails ul.synonyms div.button.remove');
	for(var i=0;i<els.length;i++) {
		addEvent('click',els[i],function(ev) {
			var from=getCurrentTaxon();
			var to=ev.target.parentNode.getAttribute('data-key');
			postJSON('/floraon/api/update/detachsynonym',{from:from,to:to},function(rt) {
				rt=JSON.parse(rt);
				if(rt.success)
					loadTaxDetails(from,el);
				else
					alert(rt.msg);
			});
		});
	}
}

function loadTreeNode(el,callback) {
	var key=el.getAttribute('data-key');
	fetchAJAX('/floraon/api/lists?w=tree&fmt=htmllist&id='+encodeURIComponent(key),function(rt) {
		el.classList.remove('loading');
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

