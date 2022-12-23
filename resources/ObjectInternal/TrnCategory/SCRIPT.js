// TrnCategory front side hook
(function(ui) {
	if (!ui) return;
	var app = ui.getAjax();
	
	function setWiredUriTag(attribute) {
		const elements = $(attribute).find($('div')).find($('a'));
		for(const el of elements) {
			const a = document.createElement('a');
			a.title = el.innerText;
			a.href = el.href.replace('ui', '') + 'category' + el.innerText;	
			const span = document.createElement('span');
			span.innerText = el.innerText;
			a.appendChild(span);
			el.parentNode.replaceChild(a, el);
		}
		const wiredElements = $(attribute).find($('div')).find($('a'));
		wiredElements.on('click', function(event) {
			event.preventDefault();
			window.open(event.currentTarget.href);
		});
		wiredElements.closest("td").off("click");
	}
	
	// Hook called by each object instance
	Simplicite.UI.hooks.TrnCategory = function(o, cbk) {
		try {
			console.log("TrnCategory hooks loading...");
			var p = o.locals.ui;
			if (p && o.isMainInstance()) {
				p.list.onload = function(ctn, obj, params) {
					setWiredUriTag('[data-field="trnCatFrontPath"]');
				};
				/*p.form.onload = function(ctn, obj, params) {
					
				};*/
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
