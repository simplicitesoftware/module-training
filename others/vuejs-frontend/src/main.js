/* eslint-disable no-debugger */
import Vue from "vue";
import App from "./App.vue";
import VueRouter from "vue-router";
import { createPinia } from 'pinia';
import router from "./router";
//import store from "./state/store";

//import "@/directives";
import Meta from "vue-meta";
import simplicite from "simplicite";
import vClickOutside from "v-click-outside";

Vue.use(VueRouter);
Vue.use(Meta);
Vue.use(vClickOutside);
const pinia = createPinia();
Vue.use(pinia);

// Make Simplicité login as a promise
function setSimplicitePublicSession() {
    const deploymentType = process.env.NODE_ENV;
    let instanceUrl =
        deploymentType === "remote" || deploymentType === "local"
            ? process.env.VUE_APP_SIM_INSTANCE_URL
            : window.location.origin;
    console.log("instance url : " + instanceUrl);

    const cfg = { url: instanceUrl, debug: false };
    const app = simplicite.session(cfg);
    console.log(app.authtoken);
    app.info("Version: " + simplicite.constants.MODULE_VERSION);
    app.debug(app.parameters);
    return app;
}

window.onload = function() {
    Vue.prototype.$smp = setSimplicitePublicSession();
    // temporary default to this value while index service is being implemented
    Vue.prototype.$SEARCH_TYPE = "simplicite";
    new Vue({
        el: "#app",
        render: (h) => h(App),
        router: router,
    });
    
}
