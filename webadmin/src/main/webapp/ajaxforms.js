/**
	Forms must have a data-path attribute
*/
function formPoster(ev) {
	ev.preventDefault();
	postAJAXForm(ev.target.getAttribute('data-path'), ev.target, function(rt) {
		var rt1=JSON.parse(rt);
		if(rt1.success) {
		    if(rt1.msg && rt1.msg.alert)
		        alert(rt1.msg.text);
		    if(ev.target.getAttribute('data-callback') == null)
			    window.location.reload();
			else
			    window.location = ev.target.getAttribute('data-callback');
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
