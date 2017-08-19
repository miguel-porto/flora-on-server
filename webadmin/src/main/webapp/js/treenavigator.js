function TreeExpander(els, callback, url) {
    this.elements = els;
    this.callback = callback;
    this.url = url;
    return this;
}

TreeExpander.prototype.init = function() {
    var parentO = this;
    for(var i=0; i<this.elements.length; i++) {
        addEvent('click', this.elements[i], function(ev) {
            parentO.clickTaxTree.call(parentO, ev);
        });

        var lis = this.elements[i].querySelectorAll('li');
        for(var j=0; j<lis.length; j++) {
            if(!lis[j].id) lis[j].id = randomString(8);
            var nrsel = lis[j].querySelectorAll('#' + lis[j].id + ' ul input[checked]').length;    // ul to exclude self
            if(nrsel > 0)
                lis[j].querySelector('.title').textContent += ' (' + nrsel + ')';
        }
    }

}

TreeExpander.prototype.loadTreeNode = function(el, loadcallback) {
    var key = el.getAttribute('data-key');

    fetchAJAX(this.url.replace(/{id}/, encodeURIComponent(key)), function(rt) {
        el.classList.remove('loading');
        var html=createHTML(rt);
        el.appendChild(html);
        if(loadcallback) loadcallback();
    });
}

TreeExpander.prototype.clickTaxTree = function(ev) {
    var el = getParentbyTag(ev.target, 'li');
    var cancel = false;
    if(!el) return;
    var key = el.getAttribute('data-key');
    if(this.callback) cancel = this.callback.call(this, ev, key);
    if(cancel) return;
    if(el.classList.contains('loading')) return;
    // get only direct children
    if(!el.id) el.id = randomString(8);
    var children = el.querySelector('#' + el.id + ' > ul') || el.querySelector('#' + el.id + ' > .placeholder');

    if(children)  // if it was already loaded, just display
        children.classList.toggle('hidden');
    else {  // otherwise load
        el.classList.add('loading');
        this.loadTreeNode(el, null);
    }
}
