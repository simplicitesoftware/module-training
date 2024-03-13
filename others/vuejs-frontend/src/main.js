/* eslint-disable no-debugger */
import {createApp} from "vue";
import App from "./App.vue";
import { createPinia } from 'pinia';
import router from "./router";
import { createMetaManager, plugin as metaPlugin } from "vue-meta";
import simplicite from "simplicite";
import vClickOutside from "click-outside-vue3";
const pinia = createPinia();
const vueApp = createApp(App);
vueApp.use(pinia);
vueApp.use(router);
vueApp.use(createMetaManager());
vueApp.use(metaPlugin);

vueApp.use(vClickOutside);

// Make Simplicité login as a promise
function setSimplicitePublicSession() {
    const deploymentType = process.env.NODE_ENV;
    let instanceUrl =
        deploymentType === "remote" || deploymentType === "local"
            ? process.env.VUE_APP_SIM_INSTANCE_URL
            : window.location.origin;
    const cfg = { url: instanceUrl, debug: false };
    const app = simplicite.session(cfg);
    app.info("Version: " + simplicite.constants.MODULE_VERSION);
    app.debug(app.parameters);
    return app;
}
//to redo with vue-meta
function fetchFaviconUrl(smp){
    const siteTheme = smp.getBusinessObject('TrnSiteTheme');
    siteTheme.search().then(async (res) => {
        if (res[0]) {
            let FaviconUrl = siteTheme.getFieldDocumentURL("trnThemeFavicon", res[0]);
            if(FaviconUrl){
                let favicon = document.querySelector('link[rel="icon"]');
                if (!favicon) {
                    favicon = document.createElement('link')
                    favicon.setAttribute('rel', 'icon')
                    favicon.setAttribute('sizes', '192x192')
                    document.head.appendChild(favicon);
                }
                favicon.setAttribute('href', FaviconUrl);
            }
        }
    });
}
window.onload = function() {
    let smp=setSimplicitePublicSession();
    vueApp.config.globalProperties.$smp = smp;
    fetchFaviconUrl(smp);
    // temporary default to this value while index service is being implemented
    vueApp.config.globalProperties.$SEARCH_TYPE = "simplicite";
    vueApp.mount('#app');
    
}
