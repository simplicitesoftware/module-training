// TrnSiteTheme front side hook
(function(ui) {
	if (!ui) return;
	const app = ui.getAjax();
	// Hook called by each object instance
	Simplicite.UI.hooks.TrnSiteTheme = function(o, cbk) {
		try {
			const p = o.locals.ui;
			if (p && o.isMainInstance()) {
				p.list.preload = function(ctn, obj) {
					let row_id = app.DEFAULT_ROW_ID;
					obj.resetFilters();
					obj.search((res) => {
						if(res.length !== 0) {
							row_id = res[0].row_id;
						}
						ui.displayForm(null, obj, row_id);
					});
				};
			}
			//...
		} catch (e) {
			app.error("Error in Simplicite.UI.hooks.TrnSiteTheme: " + e.message);
		} finally {
			cbk?.(); // final callback
		}
	};
})(window.$ui);