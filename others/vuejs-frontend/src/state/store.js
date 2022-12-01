/* eslint-disable */
import Vue from 'vue'
import Vuex from 'vuex'
import tree from "./modules/tree";
import lesson from "./modules/lesson";
import ui from "./modules/ui";

Vue.use(Vuex);

export default new Vuex.Store({
  strict: process.env.NODE_ENV !== 'production',
  modules: {
    tree, lesson, ui
  },
});
