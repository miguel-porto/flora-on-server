document.addEventListener('DOMContentLoaded', function() {
	var tr=document.querySelectorAll('#main.species tr');
	for(var i=0;i<tr.length;i++) {
		addEvent('click',tr[i],function(ev) {
			var par=getParentbyTag(ev.target,'tr');
			var td=par.querySelector('td');
			alert(td.getAttribute('data-key'));
		});
	}
});
