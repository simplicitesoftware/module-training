// TrnPage front side hook
(function(ui) {
	if (!ui) return;
	const app = ui.getAjax();
	// Hook called by each object instance
	
	function setWiredUriTag(attribute) {
		const elements = $(attribute).find($('div')).find($('a'));
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
		wiredElements.closest("td").off("click");
	}
	
	
	Simplicite.UI.hooks.TrnPage = function(o, cbk) {
		try {
			const p = o.locals.ui;
			if (p && o.isMainInstance()) {
				p.list.onload = function(ctn, obj, params) {
					setWiredUriTag('[data-field="TrnPage_TrnLesson_id__trnLsnFrontPath"]');
				};
				p.form.onload = function(ctn, obj, params) {
					const btn = $('[data-action="burl_field_TrnPage_TrnLesson_id__trnLsnFrontPath"]');
					btn.on("click", function(event) {
						event.stopPropagation();
						event.preventDefault();
						const frontPath = $('#field_TrnPage_TrnLesson_id__trnLsnFrontPath');
						window.open(event.currentTarget.baseURI.replace('ui', 'page') + frontPath.get()[0].defaultValue);
					});
				};
			}
			//...
		} catch (e) {
			app.error("Error in Simplicite.UI.hooks.TrnPage: " + e.message);
		} finally {
			cbk?.(); // final callback
		}
	};
})(window.$ui);