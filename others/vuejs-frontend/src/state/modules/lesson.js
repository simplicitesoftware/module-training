/* eslint-disable no-debugger */
import {SET_LESSON, SET_LESSON_IMAGES, UNSET_LESSON, SET_LESSON_TAGS, SET_LESSON_HTML} from "../mutation-types";

export default {
    namespaced: true,
    state: {
        lesson: {},
        lessonImages: [],
        lessonTags: [],
    },
    actions: {
        async openLesson({dispatch, commit} , payload) {
            // isRoot prevents the following case :
            // clicking on a lesson which has the same name as a category will open the lesson AND open the category node.
            // this case can only occur on root lessons.
            // needs a rework, only works as a dirty fix for /docs/versions (lesson and cat)
            const isRoot =  payload.lesson.path.split('/').length < 4 ? true : false;
            if(!isRoot) {
                commit('tree/OPEN_NODE', payload.lesson.path, { root: true });
            }
            await dispatch("fetchLesson", payload);
        },
    async openHomePage({dispatch}, payload) {
      return new Promise((resolve, reject) => {
        let page = payload.smp.getBusinessObject("TrnPage");
        page.search(
          {"trnPageType": "homepage", "trnPageTrnLessonid__trnLsnPublish": true}, {inlineDocs: 'infos'}
        ).then(async array => {
          if(array[0]) {
            payload.lesson.row_id = array[0].trnPageTrnLessonid;
            payload.lesson.viz = array[0].trnPageTrnLessonid__trnLsnVisualization;
            await dispatch("fetchLesson", payload);
            resolve();
          }
          reject('Unable to fetch homepage from backend');
        });
      });  
    },

    async openPage({dispatch}, payload) {
      return new Promise((resolve, reject) => {
        let page = payload.smp.getBusinessObject("TrnPage");
        page.search({"trnPageTrnLessonid__trnLsnFrontPath": payload.path, "trnPageTrnLessonid__trnLsnPublish": true}, {inlineDocs: 'infos'})
        .then(async array => {
          if(array[0]) {
            payload.lesson.row_id = array[0].trnPageTrnLessonid;
            payload.lesson.viz = array[0].trnPageTrnLessonid__trnLsnVisualization;
            await dispatch("fetchLesson", payload);
            resolve();
          } else {
            reject();
          }
        }).catch((e) => {
            console.log(e);
            reject();
        })
      })
    },

    async fetchLesson({dispatch}, payload) {
      await dispatch("fetchLessonTag", payload);
      await dispatch("fetchLessonContent", payload);
      // only fetch image on other than Linear mode
      // See on Training module in TrnLesson.java -> setLinearPictureContent()
      if(payload.lesson.viz !== 'LINEAR') {
        await dispatch("fetchLessonImages", payload);
      }
    },

    async fetchLessonContent({commit, rootGetters}, payload) {
      return new Promise((resolve) => {
        payload.smp.getExternalObject('TrnTreeService').call(
          {
            lang:rootGetters['ui/lang'],
            getLesson:payload.lesson.row_id
          }
        ).then(function(res){
          commit(SET_LESSON, res);
          resolve();
        })
      })
    },

    async fetchLessonImages({commit}, payload) {  
      return new Promise((resolve, reject) => {
        let picture = payload.smp.getBusinessObject("TrnPicture");
        picture.search(
          {'trnPicLsnId': payload.lesson.row_id}, {inlineDocs: 'infos'}
        ).then(array => {
          if(array){
            commit(SET_LESSON_IMAGES, array.map(pic => ({
              filename: pic.trnPicImage.name,
              filesize: pic.trnPicImage.size,
              filesrc: picture.getFieldDocumentURL("trnPicImage", pic)
            })))
            resolve();
          }
          else
            reject("Impossible to fetch the pictures");
        })
      });
    },

    async fetchLessonTag({commit, rootGetters}, payload) {
      return new Promise((resolve, reject) => {
        let lessonTags = payload.smp.getBusinessObject("TrnTagLsn");
        lessonTags.search(
          {'trnTaglsnLsnId': payload.lesson.row_id}
        ).then(array => {
          if(array) {
            commit(SET_LESSON_TAGS, array.map(tag => rootGetters['ui/getTagDisplayWithRowId'](tag.trnTaglsnTagId)));
            resolve();
          }
          else
            reject("Impossible to fetch the lesson's tag(s)");
        })
      })
    },

    unsetLesson({commit}) {
      commit(UNSET_LESSON);
    }
  },
  mutations: {
    [SET_LESSON](state, lesson) {
      state.lesson = lesson;
    },
    [SET_LESSON_HTML](state, html) {
      state.lesson.html = html;
    },
    [SET_LESSON_IMAGES](state, images) {
      state.lessonImages = images;
    },
    [UNSET_LESSON](state) {
      state.lesson = {};
      state.lessonImages = [];
    },
    [SET_LESSON_TAGS](state, lessonTags) {
      state.lessonTags = lessonTags;
    }
  }
}
