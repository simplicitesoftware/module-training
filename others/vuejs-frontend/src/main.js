/* eslint-disable no-debugger */
import Vue from 'vue'
import App from './App.vue'
import VueRouter from 'vue-router'
import Vuex from 'vuex'
import VueHighlightJS from 'vue-highlightjs'
import router from './router'
import store from './state/store'
import ReactiveSearch from '@appbaseio/reactivesearch-vue';
import '@/directives';
import Meta from 'vue-meta';
import simplicite from 'simplicite';
import AsyncComputed from 'vue-async-computed';
import vClickOutside from 'v-click-outside'


Vue.use(VueHighlightJS);
Vue.config.productionTip = false;
Vue.use(VueRouter);
Vue.use(Vuex); // enable vuex store management system 
Vue.use(ReactiveSearch);
Vue.use(Meta);
Vue.use(AsyncComputed);
Vue.use(vClickOutside);

// Make Simplicité login as a promise
function setSimplicitePublicSession(){
  var deploymentType = process.env.NODE_ENV;
  console.log(deploymentType);
  let instanceUrl = deploymentType === 'remote' || deploymentType === 'local' ? process.env.VUE_APP_SIM_INSTANCE_URL : window.location.origin;
  console.log('instance url : ' + instanceUrl);

  const cfg = { url: instanceUrl, debug: false };
  const app = simplicite.session(cfg);

  app.info('Version: ' + simplicite.constants.MODULE_VERSION);
  app.debug(app.parameters);
  return app;
}

function fecthAppParams() {
  return new Promise((resolve) => {
    Vue.prototype.$smp.getExternalObject("TrnPublicService")
    .call()
    .then(res => {
      Vue.prototype.$SEARCH_TYPE = res.search_type;  
      if(process.env.NODE_ENV === "local") Vue.prototype.$ES_INSTANCE = process.env.VUE_APP_ESI_URL;
      else Vue.prototype.$ES_INSTANCE = res.es_instance;  
      Vue.prototype.$ES_INDEX = res.es_index;
      Vue.prototype.$ES_CREDENTIALS = res.es_credentials;
      resolve(true)
    }).catch((e) => {
      console.error("fetchAppParams has failed: " + JSON.stringify(e, null, 4));
      resolve(false)
    });
  })
}

// maximum attempts if backend does not respond
const maxAttempt = 5;
let attempt = 0;

// return true if server has responded or return false and wait 0.5sec before retrying 
function initFront() {
    if(attempt === maxAttempt) {
        console.log("Backend is not responding...");
        return;
    }
    attempt++;
      // set the vue after fetching the parameters
    fecthAppParams().then((res) => {
        if(res) {
            //5. Creating the Vue instance with the router, the store and el:'#app' as the root instance of vue
            new Vue({
                el: '#app',
                store, //injects the store into all child components so they can use it
                render: h => h(App),
                router: router,
            }).$mount('#app');
        } else {
            setTimeout(() => initFront, 500);
        }
        
    });
    return false;
}

(() => {

  Vue.prototype.$smp = setSimplicitePublicSession();
  initFront()
})()



