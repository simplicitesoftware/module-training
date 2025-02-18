// TrnUrlRewriting front side hook
(function(ui) {
	if (!ui) return;
	const app = ui.getAjax();

	function setWiredUriTag(attribute) {
		const elements = $(attribute).find($('div')).find($('a'));
		for(const el of elements) {
			const a = document.createElement('a');
			a.title = el.innerText;
			a.href = el.href.replace('/ui', '') + el.innerText;	
			const span = document.createElement('span');
			span.innerText = el.innerText;
			a.appendChild(span);
			el.parentNode.replaceChild(a, el);
		}
	}

	Simplicite.UI.hooks.TrnUrlRewriting = function(o, cbk) {
		try {
			const p = o.locals.ui;
			if (p && o.isMainInstance()) {
				p.list.onload = function(ctn, obj, params) {
					setWiredUriTag('[data-field="trnSourceUrl"]');
					setWiredUriTag('[data-field="trnDestinationUrl"]');
				};
				p.form.onload = function(ctn, obj, params) {
					const btn = $('[data-action="burl_field_trnLsnFrontPath"]');
					btn.on("click", function(event) {
						const frontPath = $('#field_trnLsnFrontPath');
						window.open(event.currentTarget.baseURI.replace('/ui', '') + frontPath.get()[0].defaultValue);
					})
				};
			}
			//...
		} catch (e) {
			app.error("Error in Simplicite.UI.hooks.TrnUrlRewriting: " + e.message);
		} finally {
			cbk?.(); // final callback
		}
	};
})(window.$ui);