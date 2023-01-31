// TrnUrlRewriting front side hook
(function(ui) {
	if (!ui) return;
	var app = ui.getAjax();
	// Hook called by each object instance
	Simplicite.UI.hooks.TrnUrlRewriting = function(o, cbk) {
		try {
			console.log("TrnUrlRewriting hooks loading...");
			var p = o.locals.ui;
			if (p && o.isMainInstance()) {
				p.form.onload = function(ctn, obj, params) {
					//...
				};
			}
			//...
		} catch (e) {
			app.error("Error in Simplicite.UI.hooks.TrnUrlRewriting: " + e.message);
		} finally {
			console.log("TrnUrlRewriting hooks loaded.");
			cbk && cbk(); // final callback
		}
	};
})(window.$ui);
