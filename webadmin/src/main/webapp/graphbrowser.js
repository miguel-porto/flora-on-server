// FIXME change ajax to postJSON!!!
var force,svg;
var nodes,links,node,link,gdata={nodes:[],links:[]};
var colorcircle=['#f33','#f55','#f77','#f99','#fbb','#fdd'];
var colortext=['#000','#222','#444','#666','#888','#aaa'];
var tracklog=true;
var centernode=null;
//var ranks=['f.','var.','subsp.','species','genus','family','order','phylum'];
var lastZoom;
var zoom;
var reference=null;

var w = window,
    d = document,
    e = d.documentElement,
    g = d.getElementsByTagName('body')[0],
    winwid = w.innerWidth || e.clientWidth || g.clientWidth,
    winhei = w.innerHeight|| e.clientHeight|| g.clientHeight;

function zoomed() {
	if(d3.event.sourceEvent) {	// user has manually zoomed or panned
		d3.event.sourceEvent.stopPropagation();
		d3.event.sourceEvent.preventDefault();
		if(d3.event.sourceEvent.type=='wheel') duration=200;	// to avoid wiggly multitouch gestures
	} else duration=600;	// it's an automatic zoom to fit or button click
	if(d3.event.scale!=lastZoom) {	// has zoomed
		lastZoom=d3.event.scale;
		if(duration)
			svg.transition().duration(duration).attr("transform", "translate(" + d3.event.translate + ")scale(" + d3.event.scale + ")");
		else
			svg.attr("transform", "translate(" + d3.event.translate + ")scale(" + d3.event.scale + ")");
	} else
		svg.attr("transform", "translate(" + d3.event.translate + ")scale(" + d3.event.scale + ")");
}

document.addEventListener('DOMContentLoaded', function() {
	var query=getQueryVariable(window.location.search,'q');
	var ids=getQueryVariable(window.location.search,'id');
	var what=getQueryVariable(window.location.search,'show');
	var depth=getQueryVariable(window.location.search,'depth');
	var dim=document.getElementById('taxbrowser').getBoundingClientRect();
	zoom=d3.behavior.zoom().scaleExtent([0.1, 4]).on("zoom", zoomed);

	force=d3.layout.force().size([dim.width, dim.height]).linkDistance(100).friction(0.5).charge(-1000);//.linkStrength(0.8);
	svg = d3.select('#taxbrowser').append('svg').attr('width', dim.width).attr('height', dim.height).call(zoom);
	svg=svg.append('g');
	
//	node_drag = force.drag().on("dragend", dragend);

	force.nodes(gdata.nodes).links(gdata.links);
	
	nodes = force.nodes();
	links = force.links();
	node = svg.selectAll(".node");
	link = svg.selectAll(".link");

	force.on("tick",forceTick);
	
	fetchAJAX('/floraon/graph/reference/all',function(rt) {
		rt=JSON.parse(rt);
		if(!rt.success) {
			document.getElementById('main-wrap').innerHTML='<p class="error">'+rt.msg+'</p>';
		} else {
			reference={
				rankmap:rt.msg.rankmap
				,facets:rt.msg.facets
//				,reltypes:'<select name="reltype">'+rt.reltypes+'</select>'
			};
		}
		
		if(what) {
			if(what=='territories') addNodeBatch('getallterritories');
		} else
			loadData({query: query ? decodeURIComponent(query.replace(/\+/g, ' ')) : 'Embryopsidae', ids: ids ? ids.split(',') : null},false,getVisibleFacets(), depth ? depth : 3);
	});

//	loadData({query:'crambe'},false,getVisibleFacets(),1);
//	loadData({ids:[4622,5751,5752]},false,['taxonomy'],1);
	
	var buttons=$('#toolbar .button');
	for(var i=0;i<buttons.length;i++) addEvent('click',buttons[i],clickToolbar);

	var entries=$('#legend .entry');
	for(var i=0;i<entries.length;i++) addEvent('click',entries[i],clickLegend);
	
	addEvent('keypress',document.getElementById('loadnode'),function(ev) {
		if(ev.which==13) {
			var v=document.getElementById('loadnode').value;
			loadData({query:v},true,getVisibleFacets(),1);
			document.getElementById('loadnode').value='';
		}
	});

	addEvent('keypress',document.getElementById('querytool'),function(ev) {
		if(ev.which==13) {
			var v=document.getElementById('querytool').value;
			//fetchAJAX('worker.php?w=query&q='+encodeURIComponent(v),function(rt) {
			fetchAJAX('/floraon/api/query?fmt=html&q='+encodeURIComponent(v),function(rt) {
				var el=document.getElementById('queryresults');
				if(el) el.parentNode.removeChild(el);
				var html='<div class="window float center" id="queryresults"><div class="closebutton">close</div><h1>Query results</h1>'+rt
				var el=createHTML(html);
				document.body.appendChild(el);
				addEvent('click',document.querySelector('#queryresults .closebutton'),removeWindow);
			});
		}
	});

//	addEvent('resize',window,handleResize);
});

function substituteWindow(html,el) {
	var curwnd=document.querySelector('.window .wrap');
	if(!curwnd) return;
	var frag=createHTML(html);
	curwnd.replaceChild(frag,curwnd.querySelector('.substitute'));
}

function showWaitScreen() {
	var ws=document.getElementById('wait-screen');
	ws.style.display='block';
	ws.style.opacity=1;	
}

function showWindow(html,props) {
	if(!props) props={};
	var curwnd=document.querySelector('.window');
	if(curwnd) removeEl({target:curwnd});
	var frag=createHTML('<div class="window docked'+(props.classes ? ' '+props.classes : '')+'"'+(props.id ? ' id="'+props.id+'"' : '')+'><div class="wrap">'+html+'</div>'+(props.close ? '<div class="closebutton">close</div>' : '')+'</div>');
	var wnd=frag.querySelector('.window');
	addEvent('click',wnd.querySelector('.closebutton'),hideWindow);
	
	wnd.style.opacity=0;
	document.body.appendChild(frag);
	var hei=wnd.getBoundingClientRect().height;
	wnd.style.top=-hei+'px';
	wnd.offsetHeight;
	wnd.style.transition='top 0.4s';
	wnd.style.opacity=1;
	wnd.offsetHeight;
	wnd.style.top='0px';
	
	if(props.timer) {
		setTimeout(function() {
			hideWindow({target:wnd});
		},props.timer);

	}
	return(wnd);
}

function hideWindow(ev) {
	var wnd=getParentbyClass(ev.target,'window');
	var hei=wnd.getBoundingClientRect().height;
	addEvent('transitionend',wnd,removeEl);
	wnd.style.top=(-hei-20)+'px';
}

function removeWindow(ev) {
	var wnd=getParentbyClass(ev.target,'window');
	wnd.parentNode.removeChild(wnd);
}

function removeEl(ev) {
	if(ev && ev.target) ev.target.parentNode.removeChild(ev.target);
}

function updateLink(d,current) {
	fetchAJAX('/floraon/api/update/update/links?id='+d._id+'&current='+current,function(rt) {
		rt=JSON.parse(rt);
		if(rt.success) {
		console.log(rt.msg);
			updateData(rt.msg.nodes,rt.msg.links);
		/*
			console.log(rt.msg);
			var ind=gdata.links.indexOf(d);
			if(rt.msg.current) gdata.links[ind].current=true; else gdata.links[ind].current=undefined;*/
		} else alert(rt.msg);
		var chg=document.getElementById('changename');
		chg.parentNode.removeChild(chg);
	});
}

function afterUpdateNode(rt) {
	rt=JSON.parse(rt);
	if(rt.success) {
		updateData(rt.msg.nodes,null);
	} else alert(rt.msg);
	var chg=document.getElementById('changename');
	chg.parentNode.removeChild(chg);
}

function updateTaxNode(d,name,rank,author,sensu,comment,current) {
//	fetchAJAX('worker.php?w=changetaxnode&i='+d._id+'&name='+encodeURIComponent(name)+'&rank='+encodeURIComponent(rank)+'&current='+current+'&author='+encodeURIComponent(author)+'&comment='+encodeURIComponent(comment),function(rt) {
	fetchAJAX('/floraon/api/update/update/taxent?id='+d._id
		+'&name='+encodeURIComponent(name)
		+'&rank='+encodeURIComponent(rank)
		+'&current='+current
		+'&author='+encodeURIComponent(author)
		+'&sensu='+encodeURIComponent(sensu)
		+'&comment='+encodeURIComponent(comment)
		+'&replace=true'
		,afterUpdateNode);
}

function updateTerritoryNode(d,name,shortname,type,theme,checklist) {
	fetchAJAX('/floraon/api/update/update/territory?id='+d._id
		+'&name='+encodeURIComponent(name)
		+'&shortName='+encodeURIComponent(shortname)
		+'&type='+encodeURIComponent(type)
		+'&theme='+encodeURIComponent(theme)
		+'&checklist='+checklist
		+'&replace=true'
		,afterUpdateNode);
}

function updateAttributeNode(d,name,desc,shortname) {
	alert('deprecated!');
	fetchAJAX('worker.php?w=changeattributenode&i='+d._id+'&name='+encodeURIComponent(name)+'&desc='+encodeURIComponent(desc)+'&shortname='+encodeURIComponent(shortname),function(rt) {
		rt=JSON.parse(rt);
		if(rt.success) {
			updateData(rt.msg.nodes,null);
		} else alert(rt.msg);
		var chg=document.getElementById('changename');
		chg.parentNode.removeChild(chg);
	});
}

function getVisibleFacets() {
	var visible=document.querySelectorAll('#viewfacets .button.selected');
	visible=[].map.call(visible,function(x) {return x.getAttribute('name');});
	return visible;
}

function clickLegend(ev) {
	ev.target.classList.toggle('selected');
	var sel=ev.target.classList.contains('selected');
	switch(ev.target.id) {
	case 'but-entnames':
		if(sel) document.querySelector('#taxbrowser svg').classList.remove('noentnames'); else document.querySelector('#taxbrowser svg').classList.add('noentnames');
//		var en=document.querySelectorAll('#taxbrowser svg g.node text');
//		for(var i=0;i<en.length;i++) en[i].style.display=sel ? 'block' : 'none';
		break;
	}
}

function getPage() {
	return document.getElementById('toolbar').getAttribute('data-page');
}

function clickToolbar(ev) {
	if(ev.target.classList && ev.target.classList.contains('pin')) {
		var pin=true;
		var el=ev.target.parentNode;
	} else var el=ev.target;
	
	switch(el.id) {
	case 'but-depth':
		var what=getQueryVariable(window.location.search,'show');
		var query=getQueryVariable(window.location.search,'q');
		var ids=getQueryVariable(window.location.search,'id');
		if(what!='territories')
			loadData({query: query ? decodeURIComponent(query.replace(/\+/g, ' ')) : 'Embryopsidae', ids: ids ? ids.split(',') : null}, true, getVisibleFacets(), el.getAttribute('data-depth'));
		break;
	case 'but-showtaxonomy':
	case 'but-showecology':
	case 'but-showmorphology':
	case 'but-showoccurrence':
		var visible=document.querySelectorAll('#viewfacets .button.selected');
		if(pin) {
			for(var i=0;i<visible.length;i++) {
				if(!visible[i].querySelector('.pin').classList.contains('selected')) {
					visible[i].classList.remove('selected');
					visible[i].querySelector('.pin').classList.remove('selected');
				}
			}
			ev.target.classList.toggle('selected');
			if(ev.target.classList.contains('selected')) el.classList.add('selected'); else el.classList.remove('selected');
		} else {
			for(var i=0;i<visible.length;i++) {
				visible[i].classList.remove('selected');
				visible[i].querySelector('.pin').classList.remove('selected');
			}
			el.classList.add('selected');
		}
		visible=getVisibleFacets();
		if(visible.length==0) {
			gdata.links=[];
			onUpdateData();
		} else {
			gdata.links=gdata.links.filter(function(x) {
				return visible.indexOf(reference.facets[x.type])>-1;
//				return visible.indexOf(x.facet)>-1;
			});
			var ids=gdata.nodes.map(function(d) {return(d._id);});
			loadData({ids:ids},true,visible,0);
		}
		break;
	case 'but-empty':
		var cf=window.prompt('THIS WILL ERASE ALL TAXONOMIC NODES AND RELATIONS!\nAre you sure? Answer "yes" if so.');
		if(cf && cf.toLowerCase()=='yes') {
			showWindow('<p class="substitute">Working...</p>',{close:true});
			fetchAJAX('worker.php?w=empty',function(rt) {
				rt=JSON.parse(rt);
				substituteWindow('<p class="substitute">'+rt.msg+'</p>');
			});
		} else showWindow('<p>Nothing done.</p>',{timer:2000});
		return;
	case 'but-uploadbase':
		var idf='filebasetax';
		var wnd=showWindow('<h1>Upload taxonomy from text file</h1><div class="substitute"><p class="info">info</p><p><input name="filebasetax" type="file" id="filebasetax"/><input type="button" value="Upload" id="but-filebasetax"/></p></div>',{close:true});
	case 'but-uploadinter':
		if(!idf) {
			var idf='fileinterc';
			var wnd=showWindow('<h1>Intercalate new taxonomy level from text file</h1><div class="substitute"><p class="info">info</p><p><input name="filebasetax" type="file" id="fileinterc"/><input type="button" value="Upload" id="but-fileinterc"/></p></div>',{close:true});
		}
	case 'but-uploadreloccur':
		if(!idf) {
			var idf='filelinkoccur';
			var wnd=showWindow('<h1>Upload ecological relationships from CSV</h1><div class="substitute"><p class="info">info</p><p><input name="filebasetax" type="file" id="filelinkoccur"/><input type="button" value="Upload" id="but-filelinkoccur"/></p></div>',{close:true});
		}
	case 'but-uploadattributes':
		if(!idf) {
			var idf='fileattributes';
			var wnd=showWindow('<h1>Upload morphological attributes from CSV</h1><div class="substitute"><p class="info">info</p><p><input name="filebasetax" type="file" id="fileattributes"/><input type="button" value="Upload" id="but-fileattributes"/></p></div>',{close:true});
		}
	
		addEvent('click',document.getElementById('but-'+idf),function(){
			var files = document.getElementById(idf).files;
			var formData = new FormData();
			formData.append('w',idf);
			if(files.length<1) {
				alert('Please select a text file');
				return;
			}
			for (var i = 0; i < files.length; i++) {
				var file = files[i];
				if (file.type!='text/csv' && file.type!='text/comma-separated-values') {
					alert('You can only upload CSV files');
					return;		  
				}
				formData.append('csvfile', file, file.name);
			}
//			hideWindow({target:wnd});
			substituteWindow('<p class="substitute">Working...</p>');
			var xhr = new XMLHttpRequest();
			xhr.onreadystatechange = function (xmlo) {
				xmlo=xmlo.target;
				if(xmlo.readyState == 4 && xmlo.status == 200) {
					var rt=JSON.parse(xmlo.responseText);
					substituteWindow('<p>'+rt.msg+'</p>');
				}
			};
			xhr.open('POST', 'worker.php', true);
			xhr.send(formData);
		});
		break;
		
	case 'but-editsession':
		var wnd=showWindow('<h1>Abrir sessão de edição da checklist</h1><p class="info">Faça login para continuar</p><p><form method="POST" action="?p=tax"><label>Nome de utilizador: <input type="text" name="username" id="username"/></label><label> Password: <input type="password" name="password" id="password"/></label><input type="submit" name="login" value="login"/></form></p>',{close:true});
		break;
		
	case 'but-logout':
		window.location='?logout=1';
		break;
		
	case 'but-clean':
		var seln=$('#taxbrowser .node.selected');
		if(seln.length==0)
			gdata={nodes:[],links:[]};
		else {
			var seldatum=d3.select(seln[0]).datum();
			var newgdata={nodes:[],links:[]};
			var ids=gdata.nodes.map(function(d) {return(d._id);});
			newgdata.nodes.push(gdata.nodes[ids.indexOf(seldatum._id)]);
			gdata=newgdata;
			clearSelected(document.querySelector('#taxbrowser'));
		}
		onUpdateData();		
		break;
	case 'but-editnode':	// edits currently selected node
			var d=ev.target.datum;
			if(document.getElementById('changename')) return;
			d.fixed=true;
			force.stop();
			var pos=screenCoordsForSVGEl($('#taxbrowser svg')[0],this);
			switch(d.type) {
			case 'territory':
				var tt=document.getElementById('territorytypes');
				var html='<div class="window float" id="changename"><h1>Edit territory</h1><table><tr><td>Name</td><td><input type="text" name="name" value="'+d.name+'"/></td></tr>'
					+'<tr><td>Short name</td><td><input type="text" name="shortname" value="'+(d.shortName ? d.shortName : '')+'"/></td></tr>'
					+'<tr><td>Type</td><td>'+tt.innerHTML+'</td></tr>'
					+'<tr><td>Theme</td><td><input type="text" name="theme" value="'+(d.theme ? d.theme : '')+'"/></td></tr>'
					+'<tr><td>Show in checklist</td><td class="status"><span data-value="true" class="label'+(d.showInChecklist ? ' selected' : '')+'">yes</span> | <span data-value="false" class="label'+(d.showInChecklist ? '' : ' selected')+'">no</span></td></tr>'
					+'<tr><td colspan="2" style="text-align:center"><div class="button save">Save</div><div class="button cancel">Cancel</div></td></tr></table></div>';
				var el=createHTML(html);
				var r=el.querySelector('option[value="'+d.territoryType+'"]');
				if(r) r.setAttribute('selected','selected');
				var callback=function(ev) {
					var wnd=getParentbyClass(ev.target,'float');
					var name=wnd.querySelector('input[name=name]').value;
					var shortname=wnd.querySelector('input[name=shortname]').value;
					var type=wnd.querySelector('select[name=territorytype]').value;
					var theme=wnd.querySelector('input[name=theme]').value;
					var checklist=wnd.querySelector('.status span.label.selected').getAttribute('data-value');
					d.fixed=false;
					updateTerritoryNode(d,name,shortname,type,theme,checklist);
				};
				break;
			case 'attribute':
				var html='<div class="window float" id="changename"><h1>Edit attribute</h1><table><tr><td>Nome</td><td><input type="text" name="name" value="'+d.name+'"/></td></tr>'
					+'<tr><td>Descrição</td><td><input type="text" name="desc" value="'+(d.desc ? d.desc : '')+'"/></td></tr>'
					+'<tr><td>Nome curto</td><td><input type="text" name="shortname" value="'+(d.shortname ? d.shortname : '')+'"/></td></tr>'
					+'<tr><td colspan="2" style="text-align:center"><div class="button save">Save</div><div class="button cancel">Cancel</div></td></tr></table></div>';
				var el=createHTML(html);
				var callback=function(ev) {
					var wnd=getParentbyClass(ev.target,'float');
					var name=wnd.querySelector('input[name=name]').value;
					var desc=wnd.querySelector('input[name=desc]').value;
					var shortname=wnd.querySelector('input[name=shortname]').value;
					d.fixed=false;
					updateAttributeNode(d,name,desc,shortname);
				};
				break;
			case 'taxent':
				var tt=document.getElementById('taxonranks');
				var html='<div class="window float" id="changename"><h1>Edit node</h1><table><tr><td>Name</td><td><input type="text" name="name" value="'+d.name+'"/></td></tr>'
					+'<tr><td>Rank</td><td>'+tt.innerHTML+'</td></tr>'
					+'<tr><td>Author</td><td><input type="text" name="author" value="'+(d.author ? d.author : '')+'"/></td></tr>'
					+'<tr><td><i>Sensu</i></td><td><input type="text" name="sensu" value="'+(d.sensu ? d.sensu : '')+'"/></td></tr>'
					+'<tr><td>Annotation<br/>(e.g. <i>yellow flowers</i>)</td><td><input type="text" name="comment" value="'+(d.annotation ? d.annotation : '')+'"/></td></tr>'
					+'<tr><td>Status</td><td class="status"><span data-value="1" class="label'+(d.current ? ' selected' : '')+'">current</span> | <span data-value="0" class="label'+(d.current ? '' : ' selected')+'">not current</span></td></tr>'
					+'<tr><td colspan="2" style="text-align:center"><div class="button save">Save</div><div class="button cancel">Cancel</div></td></tr></table></div>';
					//<tr><td>Labels</td><td><span class="label">'+d.l.join('</span><span class="label">')+'</span></td></tr>'
				var el=createHTML(html);
				var r=el.querySelector('option[value="'+d.rank+'"]');
				if(r) r.setAttribute('selected','selected');
				var callback=function(ev) {
					var wnd=getParentbyClass(ev.target,'float');
					var name=wnd.querySelector('input[name=name]').value;
					var rank=wnd.querySelector('select[name=taxonrank]').value;
					var author=wnd.querySelector('input[name=author]').value;
					var sensu=wnd.querySelector('input[name=sensu]').value;
					var comment=wnd.querySelector('input[name=comment]').value;
					//var lbls=[].map.call(wnd.querySelectorAll('span.label'),function(x) {return x.textContent;});
					var current=parseInt(wnd.querySelector('.status span.label.selected').getAttribute('data-value'));
					d.fixed=false;
					updateTaxNode(d,name,rank,author,sensu,comment,current);
				};

				break;
			}
			
			var lbls=el.querySelectorAll('span.label');
			for(var i=0;i<lbls.length;i++) addEvent('click',lbls[i],function(ev) {
				if(ev.target.classList.contains('selected')) return;
				var lbls=ev.target.parentNode.querySelectorAll('span.label');
				for(var i=0;i<lbls.length;i++) lbls[i].classList.remove('selected');
				ev.target.classList.add('selected');
			});
			
			document.body.appendChild(el);
			el=document.getElementById('changename');
			el.style.top=Math.round(pos.y)+'px';
			el.style.left=Math.round(pos.x)+'px';
			el.querySelector('input').focus();
			addEvent('click',el.querySelector('.button.save'), callback);
			addEvent('click',el.querySelector('.button.cancel'),function(ev) {
				var chg=document.getElementById('changename');
				chg.parentNode.removeChild(chg);
				d.fixed=false;
				force.resume();
			});

			return;
		break;

	case 'but-editlink':	// edits currently selected node
			var d=ev.target.datum;
			if(document.getElementById('changename')) return;
			force.stop();
			var pos=screenCoordsForSVGEl($('#taxbrowser svg')[0],this);
			var html='<div class="window float" id="changename"><h1>Edit link</h1><table>'
				+'<tr><td>Type</td><td>'+d.type+'</td></tr>'
				+'<tr><td>Status</td><td class="status"><span data-value="1" class="label'+(d.current ? ' selected' : '')+'">current</span> | <span data-value="0" class="label'+(d.current ? '' : ' selected')+'">not current</span></td></tr>'
				+'<tr><td colspan="2" style="text-align:center"><div class="button delete">Delete</div><div class="button cancel">Cancel</div></td></tr></table></div>';
				//<tr><td>Labels</td><td><span class="label">'+d.l.join('</span><span class="label">')+'</span></td></tr>'
			var el=createHTML(html);
			var r=el.querySelector('option[value='+d.type+']');
			if(r) r.setAttribute('selected','selected');
			document.body.appendChild(el);
			el=document.getElementById('changename');
			el.style.top=Math.round(pos.y)+'px';
			el.style.left=Math.round(pos.x)+'px';

/*			addEvent('click',el.querySelector('.button.save'),function(ev) {
				var wnd=getParentbyClass(ev.target,'float');
				var current=parseInt(wnd.querySelector('.status span.label.selected').getAttribute('data-value'));
				updateLink(d,current);
			});*/
			
			addEvent('click',el.querySelector('.button.cancel'),function(ev) {
				var chg=document.getElementById('changename');
				chg.parentNode.removeChild(chg);
				force.resume();
			});

			addEvent('click',el.querySelector('.button.delete'),function(ev) {
				var seln=$('#taxbrowser .link.selected');
				var seldatum=d3.select(seln[0]).datum();
				deleteEntity(seldatum,'link');
				var chg=document.getElementById('changename');
				chg.parentNode.removeChild(chg);
				force.resume();
			});

			var lbls=el.querySelectorAll('span.label');
			for(var i=0;i<lbls.length;i++) addEvent('click',lbls[i],function(ev) {
				if(ev.target.classList.contains('selected')) return;
				var lbls=ev.target.parentNode.querySelectorAll('span.label');
				for(var i=0;i<lbls.length;i++) lbls[i].classList.remove('selected');
				ev.target.classList.add('selected');

				var wnd=getParentbyClass(ev.target,'float');
				var current=parseInt(wnd.querySelector('.status span.label.selected').getAttribute('data-value'));
				updateLink(d,current);
			});
			return;
		break;


	case 'but-newchar':
		var wnd=showWindow('<h1>Add new character (trait)</h1><p>Character name: <input type="text" name="charactername"/> Short name: <input type="text" name="shortname"/> Description: <input type="text" name="desc"/> <input type="button" value="Add"/></p><p class="info">aaa</p>',{close:true});
		addEvent('click',wnd.querySelector('input[type=button]'),function(ev) {
			var cname=wnd.querySelector('input[name=charactername]').value;
			var shortname=wnd.querySelector('input[name=shortname]').value;
			var desc=wnd.querySelector('input[name=desc]').value;
			fetchAJAX('worker.php?w=addcharacternode&n='+encodeURIComponent(cname)+'&sn='+encodeURIComponent(shortname)+'&desc='+encodeURIComponent(desc),function(rt) {
				rt=JSON.parse(rt);
				console.log(rt.msg);
				updateData(rt.msg.nodes,null);
			});
			hideWindow({target:wnd});
		});
		break;
		
	case 'but-newterritory':
		var tt=document.getElementById('territorytypes');
		var wnd=showWindow('<h1>Add new territory</h1><p>Territory name: <input type="text" name="name"/> Short name (only alphanumeric characters): <input type="text" name="shortname"/> Type: '+tt.innerHTML+' Theme: <input type="text" name="theme"/> <input type="button" value="Add"/></p><p class="info">Theme can be left blank.</p>',{close:true});
		addEvent('click',wnd.querySelector('input[type=button]'),function(ev) {
			var tname=wnd.querySelector('input[name=name]').value;
			var tsname=wnd.querySelector('input[name=shortname]').value;
			var type=wnd.querySelector('select[name=territorytype]').value;
			var theme=wnd.querySelector('input[name=theme]').value;
			fetchAJAX('/floraon/api/update/add/territory?name='+encodeURIComponent(tname)+'&shortName='+encodeURIComponent(tsname)+'&theme='+encodeURIComponent(theme)+'&type='+encodeURIComponent(type),function(rt) {
				rt=JSON.parse(rt);
				if(!rt.success)
					alert(rt.msg);
				else
					updateData(rt.msg.nodes,null);
			});
			hideWindow({target:wnd});
		});
		break;
		
	case 'but-newnode':
		switch(getPage()) {
		case 'tax':
			var tt=document.getElementById('taxonranks');
			var wnd=showWindow('<h1>Add new taxon</h1><p>Taxon name: <input type="text" name="taxonname"/> Authority: <input type="text" name="taxonauth"/> Rank: '+tt.innerHTML+' <input type="button" value="Add"/></p><p class="info">If adding a species or an inferior rank, include the whole name (genus, epithets). Note that you can only connect a species to a genus if the genus part of species name matches the genus.</p>',{close:true});
			addEvent('click',wnd.querySelector('input[type=button]'),function(ev) {
				var tname=wnd.querySelector('input[name=taxonname]').value;
				var tauth=wnd.querySelector('input[name=taxonauth]').value;
				var trank=wnd.querySelector('select[name=taxonrank]').value;
				fetchAJAX('/floraon/api/update/add/taxent?name='+encodeURIComponent(tname)+'&rank='+encodeURIComponent(trank)+'&author='+encodeURIComponent(tauth),function(rt) {				
					rt=JSON.parse(rt);
					console.log(rt.msg);
					updateData(rt.msg.nodes,null);
				});
				hideWindow({target:wnd});
			});
			break;
/*			
		case 'chars':	// TODO here!
			var wnd=showWindow('<h1>Add new attribute</h1><p>Attribute name: <input type="text" name="attributename"/> Short name: <input type="text" name="shortname"/> Description: <input type="text" name="desc"/> <input type="button" value="Add"/></p><p class="info">aaa</p>',{close:true});
			addEvent('click',wnd.querySelector('input[type=button]'),function(ev) {
				var aname=wnd.querySelector('input[name=attributename]').value;
				var shortname=wnd.querySelector('input[name=shortname]').value;
				var desc=wnd.querySelector('input[name=desc]').value;
				fetchAJAX('worker.php?w=addattributenode&n='+encodeURIComponent(aname)+'&sn='+encodeURIComponent(shortname)+'&desc='+encodeURIComponent(desc),function(rt) {
					rt=JSON.parse(rt);
					console.log(rt.msg);
					updateData(rt.msg.nodes,null);
				});
				hideWindow({target:wnd});
			});
			break;	*/	
		}
		break;

	case 'but-delnode':
		var seln=$('#taxbrowser .node.selected');
		if(seln.length==0) {var what='link';seln=$('#taxbrowser .link.selected');} else var what='node';

		if(seln.length==0) {
			showWindow('<p>You must select one node or link to delete</p>',{timer:3000});
			break;
		} else {
			var seldatum=d3.select(seln[0]).datum();
			deleteEntity(seldatum,what);
		}
		break;
	
	case 'but-orphan':
		fetchAJAX('worker.php?w=orphan',function(rt) {
			rt=JSON.parse(rt);
			var onlynew=mergeNodes(rt);
			gdata.nodes=gdata.nodes.concat(onlynew);
			onUpdateData();
		});
		break;
	case 'but-characters':
		addNodeBatch('getallcharacters');
		break;
	case 'but-territories':
		addNodeBatch('getallterritories');
		break;
	case 'but-partof':
		var linktype='PART_OF';
	case 'but-parent':
		if(!linktype) var linktype='HYBRID_OF';
	case 'but-belongs':
		if(!linktype) var linktype='BELONGS_TO';
	case 'but-hasquality':
		if(!linktype) var linktype='HAS_QUALITY';
	case 'but-synonym':
		if(!linktype) var linktype='SYNONYM';
	case 'but-attributeof':
		if(!linktype) var linktype='ATTRIBUTE_OF';
		
		var seln=$('#taxbrowser .node.selected');
		if(seln.length==0) {
			showWindow('<p>You must select one node, where the new link will start</p>',{timer:3000});
			break;
		} else {
			var srcdatum=d3.select(seln[0]).datum();
			showWindow('<input type="hidden" name="linktype" value="'+linktype+'"/>'+
				'<input type="hidden" name="srcid" value="'+srcdatum._id+'"/>'+
				'<input type="hidden" name="command" value="addlink"/>'+
				'<p class="info">Creating '+linktype+' link from '+srcdatum.name+'</p><p>Select destination node</p>',{close:true});
		}
		break;
	}
}

function addNodeBatch(batch) {
	fetchAJAX('/floraon/api/read/'+batch,function(rt) {
		var graph=JSON.parse(rt);
		if(!graph.success) {
			alert(graph.msg);
			return;
		}
		graph=graph.msg;
		
		updateData(graph.nodes,graph.links,null);

/*		rt=JSON.parse(rt);
		var onlynew=mergeNodes(rt.nodes);
		gdata.nodes=gdata.nodes.concat(onlynew);
		onUpdateData();*/
	});
}

function collapseNode(n) {
	var seldatum=d3.select(n).datum();
	var rem=gdata.links.filter(function(l) {	// all connected links
		return !(l.target._id==seldatum._id || l.source._id==seldatum._id);
	});

	var srcids=rem.map(function(d) {return(d.source._id);});
	var tarids=rem.map(function(d) {return(d.target._id);});
	var ids=gdata.nodes.map(function(d) {return(d._id);});
	gdata.nodes=gdata.nodes.filter(function(n) {
		return n._id==seldatum._id || !(srcids.indexOf(n._id)==-1 && tarids.indexOf(n._id)==-1);
	});
	ids=gdata.nodes.map(function(d) {return(d._id);});
	gdata.links=gdata.links.filter(function(l) {
		return ids.indexOf(l.target._id)>-1 && ids.indexOf(l.source._id)>-1;
	});
	clearSelected(document.querySelector('#taxbrowser'));
	onUpdateData();
}

function deleteEntity(d,type) {
//	if(type=='node') {
		fetchAJAX('/floraon/api/update/delete?id='+d._id,function(rt) {
			rt=JSON.parse(rt);
			var toremove=[];
			if(rt.success) {
				showWindow('<p>Deleted '+rt.msg.length+' entities: '+rt.msg+'</p>',{timer:2000});
				for(var i=0;i<gdata.links.length;i++) {
					if(rt.msg.indexOf(gdata.links[i]._id)>-1) {
						gdata.links.splice(i,1);
						i--;
					}
				}
				for(i=0;i<gdata.nodes.length;i++) {
					if(rt.msg.indexOf(gdata.nodes[i]._id)>-1) {
						gdata.nodes.splice(i,1);
						i--;
					}
				}
/*						for(var i=0;i<gdata.links.length;i++) {
					if(gdata.links[i].source.id==seldatum.id || gdata.links[i].target.id==seldatum.id) toremove.push(i);
				}
				for(i=toremove.length-1;i>=0;i--) gdata.links.splice(toremove[i],1);
				gdata.nodes.splice(seldatum.index,1);*/
				onUpdateData();
			} else alert(rt.msg);
		});
/*	} else {
		fetchAJAX('worker.php?w=dellink&i='+d._id,function(rt) {
			rt=JSON.parse(rt);
			if(rt.success) {
				for(var i=0;i<gdata.links.length;i++) {
					if(gdata.links[i]._id==rt.msg[0]) {gdata.links.splice(i,1);break;}
				}
				onUpdateData();
			} else alert(rt.msg);
		});
	}*/
}

function forceTick(e) {
/*	link.attr("x1", function(d) { return d.source.x; })
		.attr("y1", function(d) { return d.source.y; })
		.attr("x2", function(d) { return d.target.x; })
		.attr("y2", function(d) { return d.target.y; });*/
	link.attr('d',function(d) {
		var dx=d.target.x-d.source.x;
		var dy=d.target.y-d.source.y;
		switch(d.type) {
		case 'PART_OF':
		case 'BELONGS_TO':
			return('M'+(d.target.x+dy/40)+' '+(d.target.y-dx/40)+'l'+(-dy/20)+' '+(dx/20)+'L'+d.source.x+' '+d.source.y+'Z');
		case 'SYNONYM':
			return('M'+(d.target.x+dy/30)+' '+(d.target.y-dx/30)+'L'+(d.source.x+dy/30)+' '+(d.source.y-dx/30)+'L'+(d.source.x-dy/30)+' '+(d.source.y+dx/30)+'L'+(d.target.x-dy/30)+' '+(d.target.y+dx/30)+'Z');
		case 'HYBRID_OF':
			return('M'+(d.target.x+dy/40)+' '+(d.target.y-dx/40)+'l'+(-dy/20)+' '+(dx/20)+'L'+d.source.x+' '+d.source.y+'Z');
		case 'FEEDING':
		case 'PARASITIZING':
			var ang=Math.atan2(dy,dx);
			var adx=Math.cos(ang+7*Math.PI/8)*10;
			var ady=Math.sin(ang+7*Math.PI/8)*10;
			var adx1=Math.cos(ang-7*Math.PI/8)*10;
			var ady1=Math.sin(ang-7*Math.PI/8)*10;
			return('M'+d.source.x+' '+d.source.y+'L'+(d.target.x-dx/10)+' '+(d.target.y-dy/10)+'l'+adx+' '+ady+'m'+(-adx)+' '+(-ady)+'l'+(adx1)+' '+ady1);
		case 'ATTRIBUTE_OF':
		case 'HAS_QUALITY':
		case 'OBSERVED_IN':
		case 'OBSERVED_BY':
		case 'EXISTS_IN':
			return('M'+d.target.x+' '+d.target.y+'L'+(d.source.x)+' '+(d.source.y));
			break;
		}
	});
	node.attr("transform", function(d) { return "translate(" + d.x + "," + d.y + ")";});
}

function mergeNodes(newnodes) {
	var oldids=gdata.nodes.map(function(d) {return(d._id);});
	var newids=newnodes.map(function(d) {return(d._id);});
	var inters=newids.filter(function(n) {
	    return oldids.indexOf(n) != -1
	});
// merge node properties
	var tari,srci;
	for(var i=0;i<inters.length;i++) {
		tari=oldids.indexOf(inters[i]);
		srci=newids.indexOf(inters[i]);
		for(var k in newnodes[srci]) gdata.nodes[tari][k]=newnodes[srci][k];

/*
		gdata.nodes[tari].n=newnodes[srci].n;
		gdata.nodes[tari].r=newnodes[srci].r;
		gdata.nodes[tari].l=newnodes[srci].l;
		gdata.nodes[tari].c=newnodes[srci].c;
		gdata.nodes[tari].com=newnodes[srci].com;
		gdata.nodes[tari].a=newnodes[srci].a;
		gdata.nodes[tari].oldid=newnodes[srci].oldid;*/
		//gdata.nodes[oldids.indexOf(inters[i])]=newnodes[newids.indexOf(inters[i])];
	}

	var onlynew=newnodes.filter(function(n) {
		return(inters.indexOf(n._id)==-1);
	});
	return onlynew;
}

function mergeLinks(newlinks) {
	var sourceids=gdata.links.map(function(d) {return(d.source._id);});
	var targetids=gdata.links.map(function(d) {return(d.target._id);});
	var onlynew=newlinks.filter(function(d) {
		//if(sourceids.indexOf(d.source)==-1 || targetids.indexOf(d.target)==-1) return(true);
		if(sourceids.indexOf(d._from)==-1 || targetids.indexOf(d._to)==-1) return(true);
		for(var i=0;i<sourceids.length;i++) {
			if(sourceids[i]==d._from && targetids[i]==d._to) {
				gdata.links[i].current=d.current;
				return(false);
			}
		}
		return(true);
	});
	return onlynew;
}

function updateData(newnodes,newlinks,origin) {
	if(newnodes) {
		var onlynewnodes=mergeNodes(newnodes);
	// set initial coordinates
		if(origin && origin.x) {
			for(var i=0;i<onlynewnodes.length;i++) {
				var ang=Math.random()*2*3.14159;
				onlynewnodes[i].x=origin.x+60*Math.cos(ang);
				onlynewnodes[i].y=origin.y+60*Math.sin(ang);
			}
		}
	// add those nodes
		gdata.nodes=gdata.nodes.concat(onlynewnodes);
	}
	if(newlinks) {
		var onlynewlinks=mergeLinks(newlinks);
		matchLinksToNodes(onlynewlinks);
		gdata.links=gdata.links.concat(onlynewlinks);
	}
	//if((onlynewlinks && onlynewlinks.length>0) || (onlynewnodes && onlynewnodes.length>0)) 
	onUpdateData();
}

function loadData(d,add,facets,depth) {
	if(d._id!==undefined)	// d is a node to expand
		var qs='id='+d._id;
	else {			// d is a query object
		if(d.query) var qs='q='+encodeURIComponent(d.query);
		if(d.ids) var qs='id='+encodeURIComponent(d.ids.join(','));
	}
	if(!facets) facets=['TAXONOMY'];
//	d3.json('worker.php?w=neigh&f='+facets.join(',')+'&'+qs+(depth!==undefined ? '&d='+parseInt(depth) : ''), function(error, graph) {
	d3.json('/floraon/graph/getneighbors?f='+facets.join(',')+'&'+qs+(depth!==undefined ? '&d='+parseInt(depth) : '0'), function(error, graph) {
console.log(graph);
		if(!graph.success) return;
		if(graph.msg.nodes.length>500) {
			showRelationships(d);
			showWindow('<p>Sorry, this would result in '+graph.msg.nodes.length+' nodes. I won\'t do it.</p>',{timer:2000});
			return;
		}
		graph=graph.msg;

		if(!add) {	// replace current graph with this data
			gdata=graph;
			matchLinksToNodes(gdata.links);
			onUpdateData();
		} else {	// merge with current graph (note: this does not remove existing nodes/links)
			updateData(graph.nodes,graph.links,d);
			showRelationships(d);
		}
	});
}

function matchLinksToNodes(links) {
	var nodeids=gdata.nodes.map(function(d) {return(d._id);});
	for(var i=0;i<links.length;i++) {
		links[i].source=nodeids.indexOf(links[i]._from);
		links[i].target=nodeids.indexOf(links[i]._to);
	}
}

function onUpdateData() {
	var drag=force.drag().on('dragstart',function(d) {
		d3.event.sourceEvent.stopPropagation();
	});

	force.stop();
	force.nodes(gdata.nodes).links(gdata.links);
	
	node=node.data(gdata.nodes, function(d) { return d._id;});
	node.attr('class',function(d) {
		return('node '+d.type+' '+(d.rank ? reference.rankmap[''+d.rank].toLowerCase() : '')+(this.hasClass('selected') ? ' selected' : '')+(d.current ? '' : ' notcurrent'));
	}).selectAll('text').text(function(d) { return d.name; });
	
	node.exit().remove();
	link=link.data(gdata.links);
	link.attr('class',function(d) {
		return('link '+d.type+(d.current ? ' current' : ''));
	});
	link.exit().remove();
	updateStrengths();
	force.start();
	
	var tmp=node.enter();	
	tmp=tmp.append('g').attr('class',function(d) {
		return('node '+d.type+' '+(d.rank ? reference.rankmap[''+d.rank].toLowerCase() : '')+(this.hasClass('selected') ? ' selected' : '')+(d.current ? '' : ' notcurrent'));
	}).on('click',clickNode).on('dblclick', dblclick).call(force.drag)
	//.on('mouseover',function(d) {d3.select(d3.event.target).style('fill','black');});
	tmp.append(function(d) {
		var el;
		switch(d.type) {
		case 'taxent':
			el=document.createElementNS("http://www.w3.org/2000/svg", 'circle');
			el.setAttribute('r','10');
			break;
		case 'author':
			el=document.createElementNS("http://www.w3.org/2000/svg", 'path');
			el.setAttribute('d','M0 7.8l9 0l-9 -15.6l-9 15.6Z');
			break;
		case 'attribute':
			el=document.createElementNS("http://www.w3.org/2000/svg", 'path');
			el.setAttribute('d','M0 7.8l9 0l-9 -15.6l-9 15.6Z');
			break;
		case 'specieslist':
			el=document.createElementNS("http://www.w3.org/2000/svg", 'circle');
			el.setAttribute('r','10');
			break;
		case 'character':
			el=document.createElementNS("http://www.w3.org/2000/svg", 'circle');
			el.setAttribute('r','10');
			break;
		case 'territory':
			el=document.createElementNS("http://www.w3.org/2000/svg", 'path');
			el.setAttribute('d','M-7 -7l14 0l0 14l-14 0l0 -14Z');
			break;
		}
		return el;
	});//.attr("r",10);
	tmp.append("text").attr("dx", 0).attr("dy", ".35em").text(function(d) { return d.name; });
	
	tmp=link.enter();
	tmp=tmp.insert('path','svg g').attr('class',function(d) {
		return('link '+d.type+(d.current ? ' current' : ''));
	}).on('click',clickLink);
/*	link.enter().insert("line",'svg g').attr("class", function(d) {
		return('link '+d.type+(d.current ? ' current' : ''));
	});*/
	forceTick();	
}

function clickLink(d) {
	var logged=document.getElementById('loggedin');
	var seln=$('#taxbrowser .link.selected');
	if(seln.length>0 && logged) {
		var seldatum=d3.select(seln[0]).datum();
		if(seldatum._id==d._id) {
			clickToolbar.call(this,{target:{datum:d,id:'but-editlink'}});
			return;
		}
	}

	clearSelected(document.querySelector('#taxbrowser'));
	this.addClass('selected');	

	var infot=document.getElementById('taxontable');
	var el=document.getElementById('toolbar');
	if(infot) el.removeChild(infot);
	var infot=createHTML('<table id="taxontable"><tr><td colspan="2">'+d.type+' link</td></tr><tr><td colspan="2">'+(d.current ? 'current' : 'not current')+'</td></tr><tr><td>ID</td><td>'+d._id+'</td></tr></table>');
	el.insertBefore(infot, el.firstChild);
}

function showRelationships(d) {
	if(d._id===undefined) return;
// count relationships
	var nrelsout=0,nrelsin=0;
	for(var i=0;i<gdata.links.length;i++) {
		if(gdata.links[i].source._id==d._id) nrelsout++;
		if(gdata.links[i].target._id==d._id) nrelsin++;
	}
	
	var infot=document.getElementById('taxontable');
	var el=document.getElementById('toolbar');
	if(infot) el.removeChild(infot);
	
	switch(d.type) {
	case 'specieslist':
		var infot=createHTML('<table id="taxontable"><tr><td class="name" colspan="2">Species list</td></tr>'
			+'<tr><td>Date</td><td>'+d.year+'/'+d.month+'/'+d.day+'</td></tr>'
			+'<tr><td>Lat</td><td>'+d.location[0]+'</td></tr><tr><td>Long</td><td>'+d.location[1]+'</td></tr>'
			+'<tr><td>Links</td><td>'+nrelsout+' (out) '+nrelsin+' (in)</td></tr></table>');
		break;
	case 'attribute':
		var infot=createHTML('<table id="taxontable"><tr><td class="name" colspan="2">Attribute</td></tr>'
			+'<tr><td>Name</td><td>'+d.name+'</td></tr>'
			+'<tr><td>Links</td><td>'+nrelsout+' (out) '+nrelsin+' (in)</td></tr></table>');
		break;
		
	default:
		var infot=createHTML('<table id="taxontable"><tr><td class="name" colspan="2">'+d.name+(d.annotation ? ' ['+d.annotation+']' : '')+'</td></tr>'+(d.author ? '<tr><td class="auth" colspan="2">'+d.author+'</td></tr>' : '')+(d.com ? '<tr><td colspan="2">'+d.com+'</td></tr>' : '')+'<tr><td>ID</td><td class="id">'+d._id+(d.oldid ? ' ('+d.oldid+')': '')+'</td></tr>'
			+(d.rank ? '<tr><td>Rank</td><td class="rank">'+reference.rankmap[''+d.rank]+'</td></tr>' : '')+'<tr><td>Labels</td><td class="labels">'+(d.l ? d.l.join(', ') : '')+'</td></tr><tr><td>Links</td><td>'+nrelsout+' (out) '+nrelsin+' (in)</td></tr></table>');
		break;
	}
	el.insertBefore(infot, el.firstChild);
	var buttons=el.querySelectorAll('.button');
	for(var i=0;i<buttons.length;i++) addEvent('click',buttons[i],clickToolbar);
	
	return;
	
	var infot=document.getElementById('linklist');
	var newel;
	infot.innerHTML='';
	for(var i=0;i<gdata.links.length;i++) {
		if(gdata.links[i].source._id==d._id || gdata.links[i].target._id==d._id) {
			newel=document.createElement('li');
			if(gdata.links[i].source._id==d._id)
				newel.innerHTML='<-'+gdata.links[i].type;
			else
				newel.innerHTML=gdata.links[i].type+'<-';
			infot.appendChild(newel);
		}
	}
}

function screenCoordsForSVGEl(svg,el){
	var pt = svg.createSVGPoint();
	var matrix = el.getScreenCTM();
	var bb=el.getBBox();
	pt.x = bb.x+bb.width/2;
	pt.y = bb.y+bb.height;
	return(pt.matrixTransform(matrix));
  /*pt.x += rect.width.animVal.value;
  corners.ne = pt.matrixTransform(matrix);
  pt.y += rect.height.animVal.value;
  corners.se = pt.matrixTransform(matrix);
  pt.x -= rect.width.animVal.value;
  corners.sw = pt.matrixTransform(matrix);*/
}

function clickNode(d) {
	if(d3.event.defaultPrevented) return; // ignore drag
	d3.event.preventDefault();
	var logged=document.getElementById('loggedin');
	
	var seln=$('#taxbrowser .node.selected');
	if(seln.length>0) {
		var seldatum=d3.select(seln[0]).datum();
		if(seldatum._id==d._id) {
			var chg=document.getElementById('changename');
			if(chg || !logged) {
				if(chg) chg.parentNode.removeChild(chg);
				collapseNode(seln[0]);
				seldatum.fixed=false;
			} else if(!chg && logged) clickToolbar.call(this,{target:{datum:d,id:'but-editnode'}});
			return;
		}
	}
	
	var cmd=document.querySelector('.window input[name=command]');
	if(cmd) {	// any command invoked?
		switch(cmd.value) {
		case 'addlink':
			var srcid=document.querySelector('.window input[name=srcid]').value;
			var tarid=d._id;
			var linktype=document.querySelector('.window input[name=linktype]').value;
			fetchAJAX('/floraon/api/update/add/link?from='+srcid+'&to='+tarid+'&type='+linktype+'&cur=1',function(rt) {			
				rt=JSON.parse(rt);
				if(rt.success) {
					console.log(rt.msg);
					updateData(rt.msg.nodes,rt.msg.links);
					/*
					matchLinksToNodes([rt.msg]);
					gdata.links.push(rt.msg);
					onUpdateData();*/
				} else alert(rt.msg);
				hideWindow({target:document.querySelector('.window')});
			});
			
			break;
		}
	} else {	// just a normal click
		clearSelected(document.querySelector('#taxbrowser'));
		this.addClass('selected');
		loadData(d,true,getVisibleFacets(),1);
	}
}

function clearSelected(parent) {
	var els=parent.querySelectorAll('.selected');
	for(var i=0;i<els.length;i++) els[i].removeClass('selected');

	var chg=document.getElementById('changename');
	if(chg) chg.parentNode.removeChild(chg);	
}

function updateStrengths() {
//	force.charge(function(d) {return(-Math.pow(ranks.indexOf(d.r),6)*0.1);});
//		.linkStrength(function(d) {return(1-1/(ranks.indexOf(d.source.r)^1) );});
//		.linkDistance(function(d) {return(ranks.indexOf(d.source.r)*20);});
	force.linkDistance(function(d) {return(d.type=='PART_OF' ? 100 : d.type=='SYNONYM' ? 50 : 100);});
}

function dblclick(d) {
  d3.select(this).classed("fixed", d.fixed = false);
}

function dragend(d) {
//	d3.select(this).classed("fixed", d.fixed = true);
}

