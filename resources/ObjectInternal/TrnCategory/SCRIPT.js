// TrnCategory front side hook
(function(ui) {
	if (!ui) return;
	var app = ui.getAjax();
	// Hook called by each object instance
	Simplicite.UI.hooks.TrnCategory = function(o, cbk) {
		try {
			console.log("TrnCategory hooks loading...");
			var p = o.locals.ui;
			if (p && o.isMainInstance()) {
				p.list.onload = function(ctn, obj, params) {
					const div = $('div');
					const aj = $('a');
					const elements = $('[data-field="trnCatFrontPath"]').find(div).find(aj);
					console.log(elements);
					for(const el of elements) {
						const a = document.createElement('a');
						a.title = el.innerText;
						a.href = el.href.replace('ui', '') + 'lesson' + el.innerText;
						const span = document.createElement('span');
						span.innerText = el.innerText;
						a.appendChild(span);
						el.parentNode.replaceChild(a, el);
					}
				};
			}
			//...
		} catch (e) {
			app.error("Error in Simplicite.UI.hooks.TrnCategory: " + e.message);
		} finally {
			console.log("TrnCategory hooks loaded.");
			cbk && cbk(); // final callback
		}
	};
})(window.$ui);
