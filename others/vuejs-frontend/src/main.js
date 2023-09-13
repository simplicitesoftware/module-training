/* eslint-disable no-debugger */
import Vue from "vue";
import App from "./App.vue";
import VueRouter from "vue-router";
import Vuex from "vuex";
import router from "./router";
import store from "./state/store";
import ReactiveSearch from "@appbaseio/reactivesearch-vue";
import "@/directives";
import Meta from "vue-meta";
import simplicite from "simplicite";
import AsyncComputed from "vue-async-computed";
import vClickOutside from "v-click-outside";

Vue.config.productionTip = false;
Vue.use(VueRouter);
Vue.use(Vuex); // enable vuex store management system
Vue.use(ReactiveSearch);
Vue.use(Meta);
Vue.use(AsyncComputed);
Vue.use(vClickOutside);

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

    app.info("Version: " + simplicite.constants.MODULE_VERSION);
    app.debug(app.parameters);
    return app;
}

(() => {
    Vue.prototype.$smp = setSimplicitePublicSession();
    // temporary default to this value while index service is being implemented
    Vue.prototype.$SEARCH_TYPE = "simplicite";
    new Vue({
        el: "#app",
        store, //injects the store into all child components so they can use it
        render: (h) => h(App),
        router: router,
    }).$mount("#app");
})();
