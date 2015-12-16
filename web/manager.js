document.addEventListener('DOMContentLoaded', function() {
	var tr=document.querySelectorAll('#main.species tr');
	for(var i=0;i<tr.length;i++) {
		addEvent('click',tr[i],function(ev) {
			var par=getParentbyTag(ev.target,'tr');
			var td=par.querySelector('td');
//			alert(td.getAttribute('data-key'));
		});
	}
	
	var forms=document.querySelectorAll('form.poster');
	for(var i=0;i<forms.length;i++) {
		addEvent('submit',forms[i],formPoster);
	}
});

/**
	Forms must have a data-path attribute
*/
function formPoster(ev) {
		ev.preventDefault();
//		console.log(ev.target);
		postAJAXForm(ev.target.getAttribute('data-path'),ev.target,function(rt) {
			alert(rt);
		});
}
