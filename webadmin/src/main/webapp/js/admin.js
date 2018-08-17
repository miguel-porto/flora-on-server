document.addEventListener('DOMContentLoaded', function() {
    attachFormPosters();
    attachAJAXContent();
});

function fileUploadCallback(resp, ev) {
    if(ev.target.getAttribute('data-refresh') != 'false')
        window.location.reload();
    else {
        if(resp.success) alert(resp.msg);
    }
}
