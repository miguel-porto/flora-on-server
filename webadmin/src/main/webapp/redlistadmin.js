document.addEventListener('DOMContentLoaded', function() {
    attachFormPosters();

    addEvent('click', document.getElementById('summary_toggle'), function(ev) {
        document.querySelector('table.sheet').classList.toggle('summary');
    });
});