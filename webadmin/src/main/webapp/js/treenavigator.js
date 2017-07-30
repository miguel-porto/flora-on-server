function TreeExpander(els, callback, url) {
    this.elements = els;
    this.callback = callback;
    this.url = url;
    return this;
}

TreeExpander.prototype.init = function() {
    var parentO = this;
    for(var i=0; i<this.elements.length; i++)
        addEvent('click', this.elements[i], function(ev) {
            parentO.clickTaxTree.call(parentO, ev);
        });
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
    if(el.querySelector('ul')) {
      el.removeChild(el.querySelector('ul'));
      return;
    }
    el.classList.add('loading');
    this.loadTreeNode(el, null);
}
