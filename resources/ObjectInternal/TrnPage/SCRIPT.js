// TrnPage front side hook
(function(ui) {
	if (!ui) return;
	var app = ui.getAjax();
	// Hook called by each object instance
	Simplicite.UI.hooks.TrnPage = function(o, cbk) {
		function setWiredUriTag(attribute) {
			const elements = $(attribute).find($('div')).find($('a'));
			console.log(elements);
			for(const el of elements) {
				const a = document.createElement('a');
				a.title = el.innerText;
				a.href = el.href.replace('ui', '') + 'page' + el.innerText;	
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
		}
		try {
			console.log("TrnPage hooks loading...");
			var p = o.locals.ui;
			if (p && (o.isMainInstance() || o.isPanelInstance())) {
				p.list.onload = function(ctn, obj, params) {
					setWiredUriTag('[data-field="trnLsnFrontPath"]');
				};
				p.form.onload = function(ctn, obj, params) {
					const btn = $('[data-action="burl_field_trnLsnFrontPath"]');
					console.log(btn);
					const frontPath = $('[id="field_trnLsnFrontPath"]')[0].defaultValue;
					/*btn.addEventListener('click', function() {
						console.log('please');
					})*/
				};
			}
			//...
		} catch (e) {
			app.error("Error in Simplicite.UI.hooks.TrnPage: " + e.message);
		} finally {
			console.log("TrnPage hooks loaded.");
			cbk && cbk(); // final callback
		}
	};
})(window.$ui);

