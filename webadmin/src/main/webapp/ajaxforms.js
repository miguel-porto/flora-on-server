/**
	Forms must have a data-path attribute
*/
function formPoster(ev) {
	ev.preventDefault();
	postAJAXForm(ev.target.getAttribute('data-path'),ev.target,function(rt) {
		var rt1=JSON.parse(rt);
		if(rt1.success) {
			window.location.reload();
		} else
			alert(rt1.msg);

	});
}

function attachFormPosters() {
	var forms=document.querySelectorAll('form.poster');
	for(var i=0;i<forms.length;i++) {
		addEvent('submit',forms[i],formPoster);
	}
}
