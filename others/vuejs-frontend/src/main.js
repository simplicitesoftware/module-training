/* eslint-disable no-debugger */
import {createApp} from "vue";
import App from "./App.vue";
import { createPinia } from 'pinia';
import router from "./router";
//import store from "./state/store";

//import "@/directives";
import { createMetaManager } from "vue-meta";
import simplicite from "simplicite";
import vClickOutside from "click-outside-vue3";
const pinia = createPinia();
const vueApp = createApp(App);
vueApp.use(pinia);
//vueApp.use(VueRouter);
vueApp.use(router);
vueApp.use(createMetaManager());
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

window.onload = function() {
    vueApp.config.globalProperties.$smp = setSimplicitePublicSession();
    // temporary default to this value while index service is being implemented
    vueApp.config.globalProperties.$SEARCH_TYPE = "simplicite";
    vueApp.mount('#app');
    
}
