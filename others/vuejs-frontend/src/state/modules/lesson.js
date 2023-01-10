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
      commit('tree/OPEN_NODE', payload.lesson.path, { root: true });
      await dispatch("fetchLesson", payload);
    },

    async openHomePage({dispatch}, payload) {
      return new Promise((resolve, reject) => {
        let page = payload.smp.getBusinessObject("TrnPage");
        page.search(
          {"trnPageType": "homepage", "TrnPage_TrnLesson_id__trnLsnPublish": true}, {inlineDocs: 'infos'}
        ).then(async array => {
          if(array[0]) {
            payload.lesson.row_id = array[0].TrnPage_TrnLesson_id;
            payload.lesson.viz = array[0].TrnPage_TrnLesson_id__trnLsnVisualization;
            await dispatch("fetchLesson", payload);
            resolve();
          }
          reject('Unable to fetch homepage from backend');
        });
      });  
    },

    async openPage({dispatch}, payload) {
      return new Promise((resolve) => {
        let page = payload.smp.getBusinessObject("TrnPage");
        page.search({"TrnPage_TrnLesson_id__trnLsnFrontPath": payload.path, "TrnPage_TrnLesson_id__trnLsnPublish": true}, {inlineDocs: 'infos'})
        .then(async array => {
          if(array[0]) {
            payload.lesson.row_id = array[0].TrnPage_TrnLesson_id;
            payload.lesson.viz = array[0].TrnPage_TrnLesson_id__trnLsnVisualization;
            await dispatch("fetchLesson", payload);
            resolve();
          }
        }).catch(e => console.log(e));
      })
    },

    async fetchLesson({dispatch}, payload) {
      await dispatch("fetchLessonTag", payload);
      await dispatch("fetchLessonContent", payload);
      await dispatch("fetchLessonImages", payload);
      if(payload.lesson.viz === 'LINEAR') dispatch("setLinearImgSrc");
    },

    setLinearImgSrc({commit, state}) {
      const imgRegex = new RegExp('src\\s*=\\s*"(.+?)"', 'g');
      let match = imgRegex.exec(state.lesson.html);
      let newHtml = state.lesson.html;
      while (match != null) {
        const foundImg = state.lessonImages.find((img) => img.filename === match[1].replace('./', ''));
        if(foundImg) {
          newHtml = newHtml.replace(match[1], foundImg.filesrc);
        }
        match = imgRegex.exec(state.lesson.html);
      }
      commit(SET_LESSON_HTML, newHtml);
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
