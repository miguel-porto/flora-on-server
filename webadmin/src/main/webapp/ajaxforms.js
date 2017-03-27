/**
	Forms must have a data-path attribute
*/
function formPoster(ev, callback) {
	ev.preventDefault();
	postAJAXForm(ev.target.getAttribute('data-path'), ev.target, function(rt) {
//	    console.log(rt);
		var rt1=JSON.parse(rt);
		if(callback) {
		    callback(rt1, ev);
		    return;
		}

		if(rt1.success) {
		    if(rt1.msg && rt1.msg.alert)
		        alert(rt1.msg.text);

		    if(ev.target.getAttribute('data-callback') == null) {
		        if(ev.target.getAttribute('data-refresh') == 'false') {
                    alert('Ok');
		        } else
                    window.location.reload();
			} else
			    window.location = ev.target.getAttribute('data-callback');
		} else
			alert(rt1.msg);

	});
}

function attachFormPosters(callback) {
	var forms=document.querySelectorAll('form.poster');
	for(var i=0;i<forms.length;i++) {
	    if(callback) {
	        addEvent('submit', forms[i], function(ev) {
	            formPoster.call(this, ev, callback);
	        });
	    } else addEvent('submit', forms[i], formPoster);
	}
}
