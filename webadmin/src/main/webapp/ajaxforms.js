var isFormSubmitting = false;
/**
	Forms must have a data-path attribute
*/
function formPoster(ev, callback, beforePost) {
	ev.preventDefault();
	if(beforePost && !beforePost.call(this, ev)) return;

	if(ev.target.getAttribute('data-confirm')) {
	    if(!confirm('Are you sure? There\'s no way back.')) return;
	}
	isFormSubmitting = true;
	var loader = document.getElementById('loader');
	if(loader) {
	    loader.style.display = 'block';
	}

	postAJAXForm(ev.target.getAttribute('data-path'), ev.target, function(rt) {
	    var loader = document.getElementById('loader');
//	    console.log(rt);
		var rt1=JSON.parse(rt);

		if(callback) {
		    console.log(rt1);
		    callback(rt1, ev);
		    isFormSubmitting = false;
		    if(loader) loader.style.display = 'none';
		    return;
		}

		if(rt1.success) {
		    if(rt1.msg && rt1.msg.alert)
		        alert(rt1.msg.text);

		    if(ev.target.getAttribute('data-callback') == null) {
		        if(ev.target.getAttribute('data-refresh') == 'false') {
                    if(!rt1.msg.alert) alert('Ok');     // prevent double alert
                    isFormSubmitting = false;
		        } else {
                    window.location.reload();
                    return;
                }
			} else {
			    window.location = ev.target.getAttribute('data-callback');
			    return;
            }
		} else
			console.log(rt1.msg);
        isFormSubmitting = false;

        if(loader) loader.style.display = 'none';
	});
}

function attachFormPosters(callback, beforePost) {
	var forms=document.querySelectorAll('form.poster');
	for(var i=0;i<forms.length;i++) {
	    if(callback || beforePost) {
	        addEvent('submit', forms[i], function(ev) {
	            formPoster.call(this, ev, callback, beforePost);
	        });
	    } else addEvent('submit', forms[i], formPoster);
	}
}

function attachFormPosterTo(formEl, callback, beforePost) {
    if(callback || beforePost) {
        addEvent('submit', formEl, callback);
    } else {
        removeEvent('submit', formEl, formPoster);
        addEvent('submit', formEl, formPoster);
    }
}

function attachAJAXContent(callback) {
    var ajaxcontent = document.querySelectorAll('div.ajaxcontent');
    function createMyFunction(url, el) {
        return function() {
            fetchAJAX(url, function(rt) {
                var newel = createHTML(rt);
                var parent = newel.firstChild;
                el.classList.remove('ajaxcontent');
                if(parent.getAttribute) {   // node is not a text node, so add classes
                    parent.setAttribute('class', parent.getAttribute('class') + ' ' + el.getAttribute('class'));
                    parent.setAttribute('data-url', url);
                    if(el.id) parent.id = el.id;
                }
                el.parentNode.replaceChild(newel, el);
/*
                el.removeAttribute('style');
                el.innerHTML = rt;
*/
                if(callback) callback(parent);
            }, function(rt) {
                el.innerHTML = 'some error occurred';
            });
        };
    }

    for(var i=0; i<ajaxcontent.length; i++) {
        var url = ajaxcontent[i].getAttribute('data-url');
        console.log('Fetching AJAX content from ' + url);
        if(url)
            createMyFunction(url, ajaxcontent[i])();
    }
}