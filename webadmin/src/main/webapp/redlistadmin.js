document.addEventListener('DOMContentLoaded', function() {
    attachFormPosters();

    // any change in the fields will show save button
    var inputs = document.querySelectorAll('#maindataform input, #maindataform select, #maindataform textarea');
    for (var i = 0; i < inputs.length; i++) {
        addEvent('change', inputs[i], changeHandler);
    }

    addEvent('click', document.getElementById('mainformsubmitter'), function(ev) {
        document.getElementById('mainformsubmitter').classList.add('hidden');
    });

    addEvent('click', document.getElementById('summary_toggle'), function(ev) {
        document.querySelector('table.sheet').classList.toggle('summary');
    });

    addEvent('click', document.getElementById('newauthor'), createNewAuthor);
    addEvent('click', document.getElementById('newevaluator'), createNewAuthor);
    addEvent('click', document.getElementById('newreviewer'), createNewAuthor);

    addEvent('click', document.getElementById('map'), function(ev) {
        document.getElementById('map').classList.toggle('floating');
        document.querySelector('body').classList.toggle('relative');
    });
});

function changeHandler(ev) {
    document.getElementById('mainformsubmitter').classList.remove('hidden');
}

function createNewAuthor(ev) {
    if(!document.getElementById('mainformsubmitter').classList.contains('hidden')) {
        alert('You must save the form before creating a new author.');
        return;
    }

    var name = prompt("Enter new author's name.\nNote that this user will not have a login account created.");
    if(name != null) {
        postJSON('/floraon/admin/createuser', {name: name}, function(rt) {
            var rt1=JSON.parse(rt);
            if(rt1.success) {
                if(rt1.msg && rt1.msg.alert)
                    alert(rt1.msg.text);
                window.location.reload();
            } else
                alert(rt1.msg);
        });
    }
}